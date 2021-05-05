import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.*;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class RepositoryUtil {

    // !!! IMPORTANT: Set MAVEN_HOME to the PATH on your computer
    final static String MAVEN_HOME = "/usr/share/maven";

    /**
     * Downloads a zip file of a given GitHub repository and extracts the zip file to a folder
     * @param link: string containing the url of the GitHub repository source code to download
     *
     * @param repositoryName: repository name
     * @param defaultBranch: default branch of the repository
     */
    private static void downloadRepository(String link, String repositoryName, String defaultBranch) {
        // Download zip file.
        String zipPath = repositoryName + ".zip";
        System.out.println("LINK IS " + link + "/archive/refs/heads/" + defaultBranch + ".zip" );
        try {
           FileUtils.copyURLToFile(new URL(link + "/archive/refs/heads/" + defaultBranch + ".zip"), new File(zipPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Extract zip file
        try {
            ZipFile zipFile = new ZipFile(zipPath);
            zipFile.extractAll(repositoryName);
        } catch (ZipException e) {
            e.printStackTrace();
        }

        // Delete downloaded zip file.
        new File(zipPath).delete();
    }

    private static boolean buildMavenProject(String pomXMLPath) {
        // Run mvn clean install to build jar of project.
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(pomXMLPath));
        // Run `mvn package -Dmaven.test.skip=true`. Last part of command skips run of tests
        request.setGoals(Collections.singletonList("package -Dmaven.test.skip=true"));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(Paths.get(MAVEN_HOME).toUri()));
        try {
            invoker.execute(request);
            return true;
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean buildGradleProject(String repositoryName, String branch) {
//        GradleConnector gradleConnector = GradleConnector.newConnector();
//        gradleConnector.forProjectDirectory(new File(repositoryName + "/" + repositoryName + "-" + branch));

        try {
//            ProjectConnection connection = gradleConnector.connect();
//            connection.newBuild().forTasks("assemble").run();
//            connection.close();
            String path = repositoryName + "/" + repositoryName + "-" + branch;
            File repoDir = new File(path);
            File gradlewFile = new File(path + "/gradlew");
            Set<PosixFilePermission> permissions = new HashSet<>();
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(gradlewFile.toPath(), permissions);

            String[] cmdArray = new String[1];
            cmdArray[0] = "sudo bash ./gradlew assemble";
            Runtime.getRuntime().exec(cmdArray, null, repoDir.getAbsoluteFile());
            return true;
        } catch (IOException e) {
        //catch (GradleConnectionException | IllegalStateException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean buildRepositoryJAR(String repositoryName, String defaultBranch) {
        String pomXMLPath = repositoryName + "/" + repositoryName + "-" + defaultBranch + "/pom.xml";
        String buildGradlePath = repositoryName + "/" + repositoryName + "-" + defaultBranch + "/gradlew";

        // Check whether pom.xml file exists in project
        if (new File(pomXMLPath).exists()) {
            return buildMavenProject(pomXMLPath);
        // Check whether build.gradle file exists in project
        } else if (new File(buildGradlePath).exists()) {
            return buildGradleProject(repositoryName, defaultBranch);
        }
        // No known dependency file in project.
        return false;
    }

    public static void getRepositoryJAR(String link, String defaultBranch) {
        // Extract repoName and standardize link format
        String repositoryName;
        if (link.startsWith("https://")) {
            repositoryName = link.split("/")[4];
        } else if (link.startsWith("http://")) {
            repositoryName = link.split("/")[4];
            link = "https://" + link.substring(7);
        } else {
            repositoryName = link.split("/")[2];
            link = "https://" + link;
        }
        downloadRepository(link, repositoryName,defaultBranch);
        buildRepositoryJAR(repositoryName, defaultBranch);
    }

    public static void main(String[] args) {
        //getRepositoryJAR("https://github.com/belaban/JGroups", "master");
        //getRepositoryJAR("https://github.com/anatawa12/ForgeGradle-1.2", "FG_1.2");
        getRepositoryJAR("https://github.com/TNG/ArchUnit", "main");
    }

}
