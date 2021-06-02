import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
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
        String zipPath = "downloaded-repos/" + repositoryName + ".zip";
        try {
           FileUtils.copyURLToFile(new URL(link + "/archive/refs/heads/" + defaultBranch + ".zip"), new File(zipPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Extract zip file
        try {
            ZipFile zipFile = new ZipFile(zipPath);
            zipFile.extractAll("downloaded-repos/" + repositoryName);
        } catch (ZipException e) {
            e.printStackTrace();
        }

        // Delete downloaded zip file.
        new File(zipPath).delete();
        return "downloaded-repos/" + repositoryName + "/" + repositoryName + "-" + defaultBranch;
    }

    static boolean buildMavenProject(String pomXMLPath) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(pomXMLPath));
        // Run `mvn package -Dmaven.test.skip=true`. Last part of command skips run of tests
        request.setGoals(Collections.singletonList("package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true"));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(Paths.get(MAVEN_HOME).toUri()));
        try {
            var result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                return false;
            }
            return true;
        } catch (MavenInvocationException e) {
            try {
                Writer output = new FileWriter(Const.LOG_FILE_PATH, true);
                output.append("BUILD FAILED FOR POM XML PATH " + pomXMLPath);
                output.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return false;
    }

    private static void buildGradleProject(String repositoryName, String defaultBranch, String relativeDirectoryPath) {
        try {
            String path = "downloaded-repos/" + repositoryName + "/" + repositoryName + "-" + defaultBranch;
            if (!relativeDirectoryPath.equals("/")) {
                path += relativeDirectoryPath;
            }
            File repoDir = new File(path);

            // https://docs.gradle.org/current/userguide/userguide_single.html#installing_manually
            String[] env = {"PATH=$PATH:/opt/gradle/gradle-7.0.2/bin"};
            Process process = Runtime.getRuntime().exec("bash gradlew assemble", env, new File(repoDir.getAbsolutePath()));

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("BUILD SUCCESS!");
            } else {
                //abnormal...
                System.out.println("BUILD FAILED!");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void buildRepositoryJAR(ProjectInfo projectInfo) {
        String repositoryName = projectInfo.getRepository();
        String defaultBranch = projectInfo.getDefaultBranch();
        ProjectType projectType = projectInfo.getProjectType();

        // Check whether pom.xml file exists in project
        if (projectType != null) {
            if (projectType == ProjectType.MAVEN) {
                String pomXMLPath = "downloaded-repos/" + repositoryName + "/" + repositoryName + "-" + defaultBranch;
                if (projectInfo.isInnerProject()) {
                    pomXMLPath += projectInfo.getRelativeDepFilePath();
                } else {
                    pomXMLPath += "/pom.xml";
                }
                buildMavenProject(pomXMLPath);
            } else {
                buildGradleProject(repositoryName, defaultBranch, projectInfo.getRelativeDirectoryPath("/"));
            }
        }
        // No known dependency file in project.
    }

    private static String getJarPath(ProjectInfo projectInfo) throws JarNotFoundException {
        String repositoryName = projectInfo.getRepository();
        String defaultBranch = projectInfo.getDefaultBranch();
        ProjectType projectType = projectInfo.getProjectType();

        if (projectType != null) {
            String repositoryTargetPath = "downloaded-repos/" + repositoryName + "/" + repositoryName + "-" + defaultBranch;
            if (projectInfo.isInnerProject()) {
                String relativeDirPath = projectInfo.getRelativeDirectoryPath("/");
                if (!relativeDirPath.equals("/")) repositoryTargetPath += relativeDirPath;
            }
            if (projectType == ProjectType.MAVEN) {
                repositoryTargetPath += "/target";
            } else {
                repositoryTargetPath += "/build/libs";
            }
            File[] files = new File(repositoryTargetPath).listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".jar")) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        throw new JarNotFoundException("Jar file not found for repository " + repositoryName);
    }

    public static String getRepositoryJAR(ProjectInfo projectInfo) throws JarNotFoundException {
        buildRepositoryJAR(projectInfo);
        return getJarPath(projectInfo);
    }
}
