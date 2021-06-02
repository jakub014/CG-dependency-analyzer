import DependencyAnalyzer.Dependency;
import DependencyAnalyzer.PomAnalyzer;
import DependencyAnalyzer.TextDepFileAnalyzer;
import DependencyAnalyzer.VulnsNotFoundException;
import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import org.apache.commons.io.FileUtils;
import org.jooq.tools.csv.CSVReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainScript {

    // Booleans
    private static final boolean filterEnabled = false;
    private static final boolean scanMaven = true;
    private static final boolean scanGradle = false;

    // Constants
    private static final int startFrom = 286;
    private static final int upTo = 100000000;
    private static final int[] skip = {};

    public static void main(String[] args) throws ParseException, IOException {

        // Load projects to be analyzed
        ArrayList<Integer> skipList = new ArrayList<>();
        for (int idx : skip) skipList.add(idx);
        List<ProjectInfo> projectInfoList = new ArrayList<>();
        if (scanMaven) projectInfoList.addAll(getProjectInfoList(Const.MAVEN_PROJECT_CSV_PATH));
        if (scanGradle) projectInfoList.addAll(getProjectInfoList(Const.GRADLE_PROJECT_CSV_PATH));

        // Open logging file
        File file = new File(Const.LOG_FILE_PATH);
        file.createNewFile();

        // Analyze projects
        int counter = -1;
        for (ProjectInfo projectInfo : projectInfoList) {
            counter++;
            if (counter >= startFrom && counter <= upTo && !skipList.contains(counter)) {
                Long lastUpdated = projectInfo.getLastUpdated();
                if (!filterEnabled || Const.TIMESTAMP_FEBRUARY_2021 < lastUpdated) {
                    String packageName = projectInfo.getRepository();
                    System.out.println("START ANALYSIS ON PROJECT NO." + counter + ": " + packageName);
                    String groupID = projectInfo.getUser();
                    String subdir = projectInfo.getRelativeDirectoryPath("/");

                    String depFilePath = "src/main/resources/" + projectInfo.getDownloadedDepFilePath();
                    try {
                        List<Dependency> dependencyList;
                        if (projectInfo.getProjectType() == ProjectType.MAVEN) {
                            dependencyList = PomAnalyzer.getProjectDependencies(depFilePath);
                        } else {
                            dependencyList = TextDepFileAnalyzer.getProjectDependencies(depFilePath);
                        }
                        analyzeRepository(projectInfo, dependencyList);
                        String result = "SUCCESSFULLY ANALYZED " + groupID + "/" + packageName + subdir + "\n\n";
                        Writer output = new FileWriter(Const.LOG_FILE_PATH, true);
                        output.append(result);
                        output.close();
                    } catch (VulnsNotFoundException e) {
                        System.out.println("NO VULNERABLE DEPENDENCIES FOUND");
                    } catch (IOException | OPALException | MissingArtifactException | ParseException | RepositoryUtil.JarNotFoundException | OutOfMemoryError e) {
                        String result = "EXCEPTION " + e.getClass() + " THROWN FOR " + groupID + "/" + packageName + subdir + "\n";
                        Writer output = new FileWriter(Const.LOG_FILE_PATH, true);
                        output.append(result);
                        output.close();
                    }
                }
            }
        }
    }

    private static List<String> getProjectsToInspect() throws IOException {
        return Arrays.asList(FileUtils.readFileToString(
                new File(Const.INSPECTING_PROJECTS_PATH), StandardCharsets.UTF_8
        ).split("\n"));
    }

    /**
     * Read CSV file and intersects with FILTER_DATA_PATH, creating a ProjectInfo class for each entry.
     *
     * @return The list of projects to be analyzed.
     */
    private static List<ProjectInfo> getProjectInfoList(String csvPath) throws IOException, ParseException {
        System.out.println("LOADING PROJECTS FROM " + csvPath);
        List<String> projectsToInspect = getProjectsToInspect();
        //projectsToInspect.add("eclipse/dirigible");
        for (String s : projectsToInspect) System.out.println(s);

        JSONParser parser = new JSONParser();
        JSONArray data = (JSONArray) parser.parse(new FileReader(Const.FILTER_DATA_PATH));
        CSVReader reader = new CSVReader(new FileReader(csvPath));

        List<ProjectInfo> projectInfoList = new ArrayList<>();
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            String downloadedDepFilePath = nextLine[0];
            String currentUserAndRepo = nextLine[1];
            String relativeDepFilePath = nextLine[2];
            String projectTypeStr = nextLine[3];
            ProjectType projectType;
            if (projectTypeStr.equals("maven")) {
                projectType = ProjectType.MAVEN;
            } else if (projectTypeStr.equals("gradle")) {
                projectType = ProjectType.GRADLE;
            } else {
                continue;
            }

            var found = false;
            for (String p : projectsToInspect) {
                if (p.trim().equals(currentUserAndRepo.trim())) {
                    found = true;
                    break;
                }
            }
            if (!found) continue;

            for (Object o : data) {
                JSONObject jsonProject = (JSONObject) o;
                String repository = (String) jsonProject.get("repository");
                String user = (String) jsonProject.get("user");
                String userAndRepo = user + "/" + repository;

                if (((projectType == ProjectType.GRADLE && scanGradle)
                        || (projectType == ProjectType.MAVEN && scanMaven))
                        && (userAndRepo.trim().equals(currentUserAndRepo.trim()))) {
                    projectInfoList.add(new ProjectInfo(projectType, downloadedDepFilePath, relativeDepFilePath, jsonProject));
                    break;
                }
            }
        }
        System.out.println("SUCCESSFULLY LOADED " + projectInfoList.size() + " " + projectInfoList.get(0).getProjectType() + " PROJECTS");
        return projectInfoList;
    }

    public static void listOfFiles(File dirPath, List<File> targetDirs) {
        File filesList[] = dirPath.listFiles();
        for (File file : filesList) {
            if (file.isDirectory() && file.getName().equals("target")) {
                targetDirs.add(file);
            } else {
                if (file.isDirectory()) {
                    listOfFiles(file, targetDirs);
                }
            }
        }
    }

    private static String getInnerJarPath(File f) {
        String innerJarPath = null;
        var res = f.listFiles();
        if (res != null) {
            for (File f2 : res) {
                if (f2.getName().endsWith(".jar")) {
                    innerJarPath = f2.getAbsolutePath();
                    break;
                }
            }
        }
        return innerJarPath;
    }

    private static String getInnerPomXMLPath(File f) {
        String pomXMLPath = null;
        var res2 = f.getParentFile().listFiles();
        if (res2 != null) {
            for (File f3 : res2) {
                if (f3.isFile() && f3.getName().equals("pom.xml")) {
                    pomXMLPath = f3.getAbsolutePath();
                }
            }
        }
        return pomXMLPath;
    }

    public static void analyzeRepository(ProjectInfo projectInfo, List<Dependency> dependencyList) throws IOException, OPALException, MissingArtifactException, ParseException, RepositoryUtil.JarNotFoundException, VulnsNotFoundException {
        // Get project link, name, and default branch
        String link = projectInfo.getLink();
        String defaultBranch = projectInfo.getDefaultBranch();
        RepositoryUtil.Pair pair = RepositoryUtil.getRepoAndLink(link);
        String repositoryName = pair.getLeft();
        link = pair.getRight();

//        try {
        // Download repository from GitHub
        String repositoryPath = "downloaded-repos/" + repositoryName + "/" + repositoryName + "-" + defaultBranch;
        if (!new File(repositoryPath).exists()) {
            System.out.println("DOWNLOADING REPOSITORY FROM LINK " + link);
            repositoryPath = RepositoryUtil.downloadRepository(link, repositoryName, defaultBranch);
            System.out.println("SUCCESSFULLY DOWNLOADED REPOSITORY " + repositoryPath);
        } else {
            System.out.println("REPOSITORY ALREADY DOWNLOADED");
        }

        if (projectInfo.getProjectType() == ProjectType.GRADLE) {
            // Get the jar of the downloaded repository
            System.out.println("GENERATING JAR FILE");
            String jarPath = RepositoryUtil.getRepositoryJAR(projectInfo);
            System.out.println("FINAL JAR PATH IS " + jarPath);
            System.out.println("SUCCESSFULLY GENERATED JAR FILE WITH JAR PATH " + jarPath);

            List<MavenCoordinate> coordList = new ArrayList<>();
            for (Dependency dep : dependencyList) {
                MavenCoordinate depcoord = new MavenCoordinate(dep.groupId, dep.artifactId, dep.version, "jar");
                coordList.add(depcoord);
            }

            MavenCoordinate[] toBeFilled = new MavenCoordinate[coordList.size()];
            new VulnerabilityTracerAllocationSiteBased().traceProjectVulnerabilities(new File(jarPath), coordList.toArray(toBeFilled), repositoryName, link, defaultBranch, projectInfo);
            new VulnerabilityTracer().traceProjectVulnerabilities(new File(jarPath), coordList.toArray(toBeFilled), repositoryName, link, defaultBranch, projectInfo);
        } else {
            String rootPomXMLPath = repositoryPath + "/pom.xml";

            File alreadyBuiltFile = new File(repositoryPath + "/built.txt");
            Boolean alreadyBuiltFileExists = alreadyBuiltFile.exists();
            // Only build project if not already built!
            if (!alreadyBuiltFileExists) {
                var res = RepositoryUtil.buildMavenProject(rootPomXMLPath);
                if (!res) {
                    Writer output = new FileWriter(Const.LOG_FILE_PATH, true);
                    output.append("\n\n-----------------\nBUILD FAILED FOR POM XML PATH " + rootPomXMLPath + "\n");
                    output.close();
                    return;
                }
                // Create empty file
                alreadyBuiltFile.createNewFile();
            }

            List<File> targetDirs = new ArrayList<>();
            listOfFiles(new File(repositoryPath), targetDirs);
            if (!alreadyBuiltFileExists) {
                Writer output = new FileWriter(Const.LOG_FILE_PATH, true);
                String result = "\n\n-----------------\nFOUND A TOTAL OF " + targetDirs.size() + " TARGET FOLDER FOR PROJECT " + repositoryName + "\n";
                output.append(result);
                output.close();
            }

            for (File f : targetDirs) {
                String pomXMLPath = getInnerPomXMLPath(f);
                String targetPomXMLAbsPath = new File(repositoryPath + "/" + projectInfo.getRelativeDepFilePath()).getAbsolutePath();
                if (!targetPomXMLAbsPath.equals(pomXMLPath)) continue;

                String innerJarPath = getInnerJarPath(f);
                System.out.println("INNER JAR PATH IS " + innerJarPath);
                if (innerJarPath != null) {
                    // Get dependencies from project dependency file.
                    try {
                        System.out.println("GETTING DEPENDENCIES FROM ROOT pom.xml file");
                        System.out.println("GETTING pom.xml DEPENDENCIES FROM path " + rootPomXMLPath);
                        dependencyList = PomAnalyzer.getProjectDependencies(rootPomXMLPath);
                        System.out.println("SUCCESSFULLY RETRIEVED DEPENDENCIES FROM pom.xml file");

                        List<MavenCoordinate> coordList = new ArrayList<>();
                        for (Dependency dep : dependencyList) {
                            MavenCoordinate depcoord = new MavenCoordinate(dep.groupId, dep.artifactId, dep.version, "jar");
                            coordList.add(depcoord);
                        }
                        MavenCoordinate[] toBeFilled = new MavenCoordinate[coordList.size()];
                        new VulnerabilityTracerAllocationSiteBased().traceProjectVulnerabilities(new File(innerJarPath), coordList.toArray(toBeFilled), repositoryName, link, defaultBranch, projectInfo);
                        new VulnerabilityTracer().traceProjectVulnerabilities(new File(innerJarPath), coordList.toArray(toBeFilled), repositoryName, link, defaultBranch, projectInfo);
                        String result = "SUCCESSFULLY ANALYZED JAR PATH "
                                + new File(innerJarPath).getPath()
                                + "\nUSING ROOT POM FILE AT" + repositoryPath + "/pom.xml"
                                + "\nAND DOWNLOADED LOCAL POM PATH" + projectInfo.getDownloadedDepFilePath() + "\n";
                        Writer output = new FileWriter(Const.LOG_FILE_PATH, true);
                        output.append(result);
                        output.close();
                    } catch (VulnsNotFoundException e) {
                        String result = "NO VULNERABILITIES FOUND USING ROOT POM FILE AT "
                                + repositoryPath + "/pom.xml"
                                + "\nFOR JAR WITH PATH " + new File(innerJarPath).getPath()
                                + "\nAND DOWNLOADED LOCAL PATH "
                                + projectInfo.getDownloadedDepFilePath() + "\n";
                        Writer output = new FileWriter(Const.LOG_FILE_PATH, true);
                        output.append(result);
                        output.close();
                    }

                    if (pomXMLPath != null) {
                        try {
                            // Get dependencies from project dependency file.
                            System.out.println("GETTING DEPENDENCIES FROM pom.xml file");
                            System.out.println("GETTING pom.xml DEPENDENCIES FROM path " + pomXMLPath);
                            dependencyList = PomAnalyzer.getProjectDependencies(pomXMLPath);
                            System.out.println("SUCCESSFULLY RETRIEVED DEPENDENCIES FROM pom.xml file");
                            List<MavenCoordinate> coordList = new ArrayList<>();
                            for (Dependency dep : dependencyList) {
                                MavenCoordinate depcoord = new MavenCoordinate(dep.groupId, dep.artifactId, dep.version, "jar");
                                coordList.add(depcoord);
                            }
                            MavenCoordinate[] toBeFilled = new MavenCoordinate[coordList.size()];
                            new VulnerabilityTracerAllocationSiteBased().traceProjectVulnerabilities(new File(innerJarPath), coordList.toArray(toBeFilled), repositoryName, link, defaultBranch, projectInfo);
                            new VulnerabilityTracer().traceProjectVulnerabilities(new File(innerJarPath), coordList.toArray(toBeFilled), repositoryName, link, defaultBranch, projectInfo);
                            String result = "SUCCESSFULLY ANALYZED JAR PATH "
                                    + innerJarPath + "\nUSING NON-ROOT POM FILE AT"
                                    + projectInfo.getRelativeDepFilePath()
                                    + "\nAND DOWNLOADED LOCAL POM PATH "
                                    + projectInfo.getDownloadedDepFilePath() + "\n";
                            Writer output = new FileWriter(Const.LOG_FILE_PATH, true);
                            output.append(result);
                            output.close();

                        } catch (VulnsNotFoundException e) {
                            String result = "NO VULNERABILITIES FOUND USING NON-ROOT POM FILE AT "
                                    + projectInfo.getRelativeDepFilePath()
                                    + " \nFOR JAR WITH PATH " + innerJarPath
                                    + "\nAND DOWNLOADED LOCAL PATH "
                                    + projectInfo.getDownloadedDepFilePath() + "\n";
                            Writer output = new FileWriter(Const.LOG_FILE_PATH, true);
                            output.append(result);
                            output.close();
                        }

                    }
                }
            }
        }
//        } finally {
//            FileUtils.deleteDirectory(new File(repositoryName));
//        }
    }
}
