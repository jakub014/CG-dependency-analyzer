import DependencyAnalyzer.BuildGradleAnalyzer;
import DependencyAnalyzer.PomAnalyzer;
import DependencyAnalyzer.Dependency;

import DependencyAnalyzer.VulnsNotFoundException;
import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import org.apache.commons.io.FileUtils;
import org.jooq.tools.csv.CSVReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MainScript {

    private static final boolean filterEnabled = false;
    private static final Long TIMESTAMP_MAY_2020 = 1589454523000L;
    private static final Long TIMESTAMP_FEBRUARY_2021 = 1613308674000L;

    private static ProjectType getProjectType(String groupID, String packageName) {
        String basePath = "src/main/resources/depfiles/" + groupID + "__" + packageName;
        String pomPath = basePath + "_pom.xml";
        String gradlePath = basePath + "_build.gradle";

        if (new File(pomPath).exists()) {
            return ProjectType.MAVEN;
        } else if (new File(gradlePath).exists()) {
            return ProjectType.GRADLE;
        }
        return null;
    }

    public static void main(String[] args) throws ParseException, IOException {
        JSONParser parser = new JSONParser();
        JSONArray data = (JSONArray) parser.parse(new FileReader("src/main/resources/vulnerableProjectData.json"));

        CSVReader reader = new CSVReader(new FileReader("src/main/resources/depfile-info.csv"));
        String[] nextLine;

        List<ProjectInfo> projectInfoList = new ArrayList<>();
        String previousUserAndRepo = null;

        // Perform analysis for each entry in depfile-info.csv
        while ((nextLine = reader.readNext()) != null) {
            String downloadedDepFilePath = nextLine[0];
            String currentUserAndRepo = nextLine[1];
            String relativeDepFilePath = nextLine[2];
            String projectType = nextLine[3];

            // Add project info to list.
            for (Object o : data) {
                JSONObject jsonProject = (JSONObject) o;
                String repository = (String) jsonProject.get("repository");
                String user = (String) jsonProject.get("user");
                String userAndRepo = user + "/" + repository;

                    if (userAndRepo.equals(currentUserAndRepo)) {
                        //boolean innerProject = currentUserAndRepo.equals(previousUserAndRepo);
                        projectInfoList.add(new ProjectInfo(projectType, downloadedDepFilePath, relativeDepFilePath, jsonProject, true));
                        previousUserAndRepo = currentUserAndRepo;
                        break;
                    }
            }
        }

//        for (Object o : data) {
//            JSONObject jsonProject = (JSONObject) o;
//            String packageName = (String) jsonProject.get("repository");
//            String groupID = (String) jsonProject.get("user");
//            ProjectType projectType = getProjectType(groupID, packageName);
//            projectInfoList.add(new ProjectInfo(projectType, jsonProject));
//        }

        final int startFrom = 4204;
        int counter = 0;

        String filePath = "analysisResults/analysed-repos-gradle.txt";
        File file = new File(filePath);
        file.createNewFile();
        Writer output = new FileWriter(filePath, true);

        for (ProjectInfo projectInfo : projectInfoList) {
            counter++;
            if (counter > startFrom) {
                Long lastUpdated = (Long) projectInfo.getLastUpdated();
                if (!filterEnabled || TIMESTAMP_FEBRUARY_2021 > lastUpdated) {
                    System.out.println("START ANALYSIS ON PROJECT NO." + counter);

                    String packageName = projectInfo.getRepository();
                    String groupID = projectInfo.getUser();

                    String pomPath = "src/main/resources/" + projectInfo.getDownloadedDepFilePath();
                    File pom = new File(pomPath);

                    if (!pom.exists()) {
                        try {
                            String result = "POM LOCALLY NOT FOUND FOR " + packageName + " ~ " + groupID + "\n";
                            output.append(result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    /*if (projectInfo.isInnerProject()) {
                        System.out.println("RELATIVE PROJECT DIR DEP PATH " + projectInfo.getRelativeDirectoryPath());
                        System.out.println("RELATIVE DEP FILE PATH IS " + projectInfo.getRelativeDepFilePath());
                        System.out.println("PROJECT USER IS " + projectInfo.getUser());
                        System.out.println("PROJECT REPO IS " + projectInfo.getRepository());
                    }*/


                    try {
                        List<Dependency> dependencyList = PomAnalyzer.getProjectDependencies(pomPath);
                        analyzeRepository(projectInfo, dependencyList);
                        try {
                            String result = "SUCCESSFULLY ANALYZED " + packageName + " ~ " + groupID + "\n";
                            output.append(result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } catch (VulnsNotFoundException e) {
                        System.out.println("NO VULNERABLE DEPENDENCIES FOUND");
                        try {
                            String result = "NO VULNERABLE DEPENDENCIES FOUND FOR " + packageName + " ~ " + groupID + "\n";
                            output.append(result);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } catch (IOException | OPALException | MissingArtifactException | ParseException | RepositoryUtil.JarNotFoundException e) {
                        try {
                            String result = "EXCEPTION " + e.getClass() + " THROWN FOR " + packageName + " ~ " + groupID + "\n";
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

    public static void analyzeRepository(ProjectInfo projectInfo, List<Dependency> dependencyList) throws IOException, OPALException, MissingArtifactException, ParseException, RepositoryUtil.JarNotFoundException, VulnsNotFoundException {
        String link = projectInfo.getLink();
        String defaultBranch = projectInfo.getDefaultBranch();
        ProjectType projectType = projectInfo.getProjectType();

//        for (Dependency d : dependencyList) System.out.println(d);

        RepositoryUtil.Pair pair = RepositoryUtil.getRepoAndLink(link);
        String repositoryName = pair.getLeft();
        link = pair.getRight();

        // Download repository from GitHub.
        String repositoryPath = "downloaded-repos/" + repositoryName + "/" + repositoryName + "-" + defaultBranch;
        if (!new File(repositoryPath).exists()) {
            System.out.println("DOWNLOADING REPOSITORY FROM LINK " + link);
            repositoryPath = RepositoryUtil.downloadRepository(link, repositoryName, defaultBranch);
            System.out.println("SUCCESSFULLY DOWNLOADED REPOSITORY " + repositoryPath);
        } else {
            System.out.println("REPOSITORY ALREADY DOWNLOADED");
        }

        // Get the jar of the downloaded repository.
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
