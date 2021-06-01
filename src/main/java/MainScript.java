import DependencyAnalyzer.Dependency;
import DependencyAnalyzer.PomAnalyzer;
import DependencyAnalyzer.TextDepFileAnalyzer;
import DependencyAnalyzer.VulnsNotFoundException;
import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import org.jooq.tools.csv.CSVReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainScript {

    // Booleans
    private static final boolean filterEnabled = true;
    private static final boolean scanMaven = true;
    private static final boolean scanGradle = false;

    // Constants
    private static final int startFrom = 6015;
    private static final int upTo = 100000000;
    private static final int[] skip = {};
    private static final String FILTER_DATA_PATH = "src/main/resources/vulnerableProjectData.json";
    private static final String MAVEN_PROJECT_CSV_PATH = "src/main/resources/depfile-info-maven.csv";
    private static final String GRADLE_PROJECT_CSV_PATH = "src/main/resources/depfile-info-gradle.csv";
    private static final String LOG_FILE_PATH = "analysisResults/analysed-repos.txt";
    private static final Long TIMESTAMP_MAY_2020 = 1589454523000L;
    private static final Long TIMESTAMP_FEBRUARY_2021 = 1613308674000L;

    public static void main(String[] args) throws ParseException, IOException {

        // Load projects to be analyzed
        ArrayList<Integer> skipList = new ArrayList<>();
        for (int idx : skip) skipList.add(idx);
        List<ProjectInfo> projectInfoList = new ArrayList<>();
        if (scanMaven) projectInfoList.addAll(getProjectInfoList(MAVEN_PROJECT_CSV_PATH));
        if (scanGradle) projectInfoList.addAll(getProjectInfoList(GRADLE_PROJECT_CSV_PATH));

        // Open logging file
        File file = new File(LOG_FILE_PATH);
        file.createNewFile();
        Writer output = new FileWriter(LOG_FILE_PATH, true);

        // Analyze projects
        int counter = -1;
        for (ProjectInfo projectInfo : projectInfoList) {
            counter++;
            if (counter >= startFrom && counter <= upTo && !skipList.contains(counter)) {
                Long lastUpdated = projectInfo.getLastUpdated();
                if (!filterEnabled || TIMESTAMP_FEBRUARY_2021 < lastUpdated) {
                    String packageName = projectInfo.getRepository();
                    System.out.println("START ANALYSIS ON PROJECT NO." + counter + ": " + packageName);
                    String groupID = projectInfo.getUser();
                    String subdir = projectInfo.getRelativeDirectoryPath("/");

                    String depFilePath = "src/main/resources/" + projectInfo.getDownloadedDepFilePath();
                    File depFile = new File(depFilePath);

                    if (!depFile.exists()) {
                        try {
                            String result = "DEPENDENCY FILE LOCALLY NOT FOUND FOR " + groupID + "/" + packageName + subdir + "\n";
                            output.append(result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        counter++;
                        continue;
                    }

                    /*if (projectInfo.isInnerProject()) {
                        System.out.println("RELATIVE PROJECT DIR DEP PATH " + projectInfo.getRelativeDirectoryPath());
                        System.out.println("RELATIVE DEP FILE PATH IS " + projectInfo.getRelativeDepFilePath());
                        System.out.println("PROJECT USER IS " + projectInfo.getUser());
                        System.out.println("PROJECT REPO IS " + projectInfo.getRepository());
                    }*/

                    try {
                        List<Dependency> dependencyList;
                        if (projectInfo.getProjectType() == ProjectType.MAVEN) {
                            dependencyList = PomAnalyzer.getProjectDependencies(depFilePath);
                        } else {
                            dependencyList = TextDepFileAnalyzer.getProjectDependencies(depFilePath);
                        }
                        analyzeRepository(projectInfo, dependencyList);
                        try {
                            String result = "SUCCESSFULLY ANALYZED " + groupID + "/" + packageName + subdir + "\n";
                            output.append(result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } catch (VulnsNotFoundException e) {
                        System.out.println("NO VULNERABLE DEPENDENCIES FOUND");
                        try {
                            String result = "NO VULNERABLE DEPENDENCIES FOUND FOR " + groupID + "/" + packageName + subdir + "\n";
                            output.append(result);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } catch (IOException | OPALException | MissingArtifactException | ParseException | RepositoryUtil.JarNotFoundException e) {
                        try {
                            String result = "EXCEPTION " + e.getClass() + " THROWN FOR " + groupID + "/" + packageName + subdir + "\n";
                            output.append(result);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        output.close();
    }

    /**
     * Read CSV file and intersects with FILTER_DATA_PATH, creating a ProjectInfo class for each entry.
     * @return The list of projects to be analyzed.
     */
    private static List<ProjectInfo> getProjectInfoList(String csvPath) throws IOException, ParseException {
        System.out.println("LOADING PROJECTS FROM " + csvPath);
        JSONParser parser = new JSONParser();
        JSONArray data = (JSONArray) parser.parse(new FileReader(FILTER_DATA_PATH));
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

            for (Object o : data) {
                JSONObject jsonProject = (JSONObject) o;
                String repository = (String) jsonProject.get("repository");
                String user = (String) jsonProject.get("user");
                String userAndRepo = user + "/" + repository;

                if (((projectType == ProjectType.GRADLE && scanGradle)
                        || (projectType == ProjectType.MAVEN && scanMaven))
                        && (userAndRepo.equals(currentUserAndRepo))) {
                    projectInfoList.add(new ProjectInfo(projectType, downloadedDepFilePath, relativeDepFilePath, jsonProject, true));
                    break;
                }
            }
        }
        System.out.println("SUCCESSFULLY LOADED " + projectInfoList.size() + " " + projectInfoList.get(0).getProjectType() + " PROJECTS");
        return projectInfoList;
    }

    public static void analyzeRepository(ProjectInfo projectInfo, List<Dependency> dependencyList) throws IOException, OPALException, MissingArtifactException, ParseException, RepositoryUtil.JarNotFoundException, VulnsNotFoundException {
        // Get project link, name, and default branch
        String link = projectInfo.getLink();
        String defaultBranch = projectInfo.getDefaultBranch();
        RepositoryUtil.Pair pair = RepositoryUtil.getRepoAndLink(link);
        String repositoryName = pair.getLeft();
        link = pair.getRight();

        // Download repository from GitHub
        String repositoryPath = "downloaded-repos/" + repositoryName + "/" + repositoryName + "-" + defaultBranch;
        if (!new File(repositoryPath).exists()) {
            System.out.println("DOWNLOADING REPOSITORY FROM LINK " + link);
            repositoryPath = RepositoryUtil.downloadRepository(link, repositoryName, defaultBranch);
            System.out.println("SUCCESSFULLY DOWNLOADED REPOSITORY " + repositoryPath);
        } else {
            System.out.println("REPOSITORY ALREADY DOWNLOADED");
        }

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
        VulnerabilityTracer.traceProjectVulnerabilities(new File(jarPath), coordList.toArray(toBeFilled), repositoryName, link, defaultBranch, projectInfo);
    }
}
