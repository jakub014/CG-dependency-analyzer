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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MainScript {

    private static ProjectType getProjectType(String groupID, String packageName) {
        String pomName = groupID + "__" + packageName + "_pom.xml";
        String pomPath = "src/main/resources/poms/" + pomName;

        String gradleName = groupID + "__" + packageName + "_build.gradle";
        String gradlePath = "src/main/resources/gradle/" + gradleName;

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

        System.out.println(data.size());

        final int startFrom = 184;
        int counter = 0;

        String filePath = "analysisResults/analysed-repos-gradle.txt";
        File file = new File(filePath);
        file.createNewFile();

        for (Object o : data) {
            counter++;
            if (counter > startFrom) {
                JSONObject obj = (JSONObject) o;

                if (10 <= (Long) obj.get("stars")) {
                    System.out.println("START ANALYSIS ON PROJECT NO." + counter);

                    JSONObject apiResponse = (JSONObject) obj.get("fullresponse");

                    String packageName = (String) obj.get("repository");
                    String groupID = (String) obj.get("user");
                    ProjectType projectType = getProjectType(groupID, packageName);


                    if (projectType == null) {
                        try {
                            Writer output = new FileWriter(filePath, true);
                            String result = "POM/GRADLE FILE LOCALLY NOT FOUND FOR " + packageName + " ~ " + groupID + "\n";
                            output.append(result);
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        String defaultBranch = (String) apiResponse.get("default_branch");
                        String link = (String) apiResponse.get("html_url");
                        analyzeRepository(link, defaultBranch, projectType);

                        try {
                            Writer output = new FileWriter(filePath, true);
                            String result = "SUCCESSFULLY ANALYZED " + packageName + " ~ " + groupID + "\n";
                            output.append(result);
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } catch (VulnsNotFoundException e) {
                        System.out.println("NO VULNERABLE DEPENDENCIES FOUND");
                        try {
                            Writer output = new FileWriter(filePath, true);
                            String result = "NO VULNERABLE DEPENDENCIES FOUND FOR " + packageName + " ~ " + groupID + "\n";
                            output.append(result);
                            output.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } catch (IOException | OPALException | MissingArtifactException | ParseException | RepositoryUtil.JarNotFoundException e) {
                        try {
                            Writer output = new FileWriter(filePath, true);
                            String result = "EXCEPTION " + e.getClass() + " THROWN FOR " + packageName + " ~ " + groupID + "\n";
                            output.append(result);
                            output.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

            }
        }

    }

    public static void analyzeRepository(String link, String defaultBranch, ProjectType projectType) throws IOException, OPALException, MissingArtifactException, ParseException, RepositoryUtil.JarNotFoundException, VulnsNotFoundException {
        // Get repository name and standardize link.
        RepositoryUtil.Pair pair = RepositoryUtil.getRepoAndLink(link);
        String repositoryName = pair.getLeft();
        link = pair.getRight();

        try {

            // Download repository from GitHub.
            System.out.println("DOWNLOADING REPOSITORY FROM LINK " + link);
            String repositoryPath = RepositoryUtil.downloadRepository(link, repositoryName, defaultBranch);
            System.out.println("SUCCESSFULLY DOWNLOADED REPOSITORY " + repositoryPath);

            // Get dependencies from project dependency file.
            List<Dependency> dependencyList;
            if (projectType == ProjectType.MAVEN) {
                System.out.println("GETTING DEPENDENCIES FROM pom.xml file");
                String pomXMLPath = repositoryPath + "/pom.xml";
                dependencyList = PomAnalyzer.getProjectDependencies(pomXMLPath);
                System.out.println("SUCCESSFULLY RETRIEVED DEPENDENCIES FROM pom.xml file");
            } else {
                System.out.println("GETTING DEPENDENCIES FROM build.gradle file");
                dependencyList = BuildGradleAnalyzer.getProjectDependencies(repositoryPath);
                System.out.println("SUCCESSFULLY RETRIEVED DEPENDENCIES FROM build.gradle file");
            }

            // If no exception thrown, vulnerabilities have been found in the dependency file of project. Continue.
            // Get the jar of the downloaded repository.
            System.out.println("GENERATING JAR FILE");
            String jarPath = RepositoryUtil.getRepositoryJAR(repositoryName, defaultBranch, projectType);
            System.out.println("SUCCESSFULLY GENERATED JAR FILE WITH JAR PATH " + jarPath);

            List<MavenCoordinate> coordList = new ArrayList<MavenCoordinate>();
            for (Dependency dep : dependencyList) {
                MavenCoordinate depcoord = new MavenCoordinate(dep.groupId, dep.artifactId, dep.version, "jar");
                coordList.add(depcoord);
            }

            MavenCoordinate[] toBeFilled = new MavenCoordinate[coordList.size()];
            VulnerabilityTracer.traceProjectVulnerabilities(new File(jarPath), coordList.toArray(toBeFilled), repositoryName, link);
        } finally {
            FileUtils.deleteDirectory(new File(repositoryName));
        }
    }
}
