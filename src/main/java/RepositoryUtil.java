import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;


public class RepositoryUtil {

    // !!! IMPORTANT: Set MAVEN_HOME to the PATH on your computer
    final static String MAVEN_HOME = "/usr/share/maven";

    public static class JarNotFoundException extends Exception {
        public JarNotFoundException(String message) {
            super(message);
        }
    }

    static class Pair {
        private String left;
        private String right;

        public Pair(String left, String right) {
            this.left = left;
            this.right = right;
        }

        public String getLeft() {
            return left;
        }

        public String getRight() {
            return right;
        }
    }

    public static Pair getRepoAndLink(String link) {
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
        return new Pair(repositoryName, link);
    }

    /**
     * Downloads a zip file of a given GitHub repository and extracts the zip file to a folder
     * @param link: string containing the url of the GitHub repository source code to download
     * @param defaultBranch: default branch of the repository
     */
    public static String downloadRepository(String link, String repositoryName, String defaultBranch) {
        // Download zip file.
        String zipPath = repositoryName + ".zip";
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
        return repositoryName + "/" + repositoryName + "-" + defaultBranch;
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

    private static void buildGradleProject(String repositoryName, String branch) {
        try {
            String path = repositoryName + "/" + repositoryName + "-" + branch;
            File repoDir = new File(path);
            File gradlewFile = new File(path + "/gradlew");
            Set<PosixFilePermission> permissions = new HashSet<>();
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(gradlewFile.toPath(), permissions);

            String[] cmdArray = new String[1];
            cmdArray[0] = "sudo bash ./gradlew assemble";
            Runtime.getRuntime().exec(cmdArray, null, repoDir.getAbsoluteFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void buildRepositoryJAR(String repositoryName, String defaultBranch) {
        String pomXMLPath = repositoryName + "/" + repositoryName + "-" + defaultBranch + "/pom.xml";

        // Check whether pom.xml file exists in project
        if (new File(pomXMLPath).exists()) {
            buildMavenProject(pomXMLPath);
        }
        // No known dependency file in project.
    }

    private static String getJarPath(String repositoryName, String defaultBranch) throws JarNotFoundException {
        String repositoryTargetPath = repositoryName + "/" + repositoryName + "-" + defaultBranch + "/target";
        for (File file : Objects.requireNonNull(new File(repositoryTargetPath).listFiles())) {
            if (file.getName().endsWith(".jar")) {
                return file.getAbsolutePath();
            }
        }
        throw new JarNotFoundException("Jar file not found for repository " + repositoryName);
    }

    public static String getRepositoryJAR(String repositoryName, String defaultBranch) throws JarNotFoundException {
        buildRepositoryJAR(repositoryName, defaultBranch);
        return getJarPath(repositoryName, defaultBranch);
    }
}
