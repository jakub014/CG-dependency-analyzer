import DependencyAnalyzer.PomAnalyzer;
import DependencyAnalyzer.Dependency;

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

    public static void main(String[] args) throws ParseException, IOException {
        JSONParser parser = new JSONParser();
        JSONArray data = (JSONArray) parser.parse(new FileReader("src/main/resources/vulnerableProjectData.json"));

        System.out.println(data.size());

        final int startFrom = 0;
        int counter = 0;

        String filePath = "analysisResults/analysed-repos.txt";
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

                    String pomName = groupID + "__" + packageName + "_pom.xml";
                    String pomPath = "src/main/resources/poms/" + pomName;
                    File pom = new File(pomPath);

                    if (!pom.exists()) {
                        try {
                            Writer output = new FileWriter(filePath, true);
                            String result = "POM LOCALLY NOT FOUND FOR " + packageName + " ~ " + groupID + "\n";
                            output.append(result);
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    try {
                        PomAnalyzer.getProjectDependencies(pomPath);
                        String defaultBranch = (String) apiResponse.get("default_branch");
                        String link = (String) apiResponse.get("html_url");
                        analyzeRepository(link, defaultBranch);

                        try {
                            Writer output = new FileWriter(filePath, true);
                            String result = "SUCCESSFULLY ANALYZED " + packageName + " ~ " + groupID + "\n";
                            output.append(result);
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } catch (PomAnalyzer.VulnsNotFoundException e) {
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

    public static void analyzeRepository(String link, String defaultBranch) throws IOException, OPALException, MissingArtifactException, ParseException, RepositoryUtil.JarNotFoundException, PomAnalyzer.VulnsNotFoundException {
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
            System.out.println("GETTING DEPENDENCIES FROM pom.xml file");
            String pomXMLPath = repositoryPath + "/pom.xml";
            List<Dependency> dependencyList = PomAnalyzer.getProjectDependencies(pomXMLPath);
            System.out.println("SUCCESSFULLY RETRIEVED DEPENDENCIES FROM pom.xml file");

            // If no exception thrown, vulnerabilities have been found in the dependency file of project. Continue.
            // Get the jar of the downloaded repository.
            System.out.println("GENERATING JAR FILE");
            String jarPath = RepositoryUtil.getRepositoryJAR(repositoryName, defaultBranch);
            System.out.println("SUCCESSFULLY GENERATED JAR FILE WITH JAR PATH " + jarPath);

            List<MavenCoordinate> coordList = new ArrayList<MavenCoordinate>();
            for (Dependency dep : dependencyList) {
                MavenCoordinate depcoord = new MavenCoordinate(dep.groupId, dep.artifactId, dep.version, "jar");
                coordList.add(depcoord);
            }

            MavenCoordinate[] toBeFilled = new MavenCoordinate[coordList.size()];
            VulnerabilityTracer.traceProjectVulnerabilities(new File(jarPath), coordList.toArray(toBeFilled), repositoryName, link);
        } catch (PomAnalyzer.VulnsNotFoundException e) {
            throw new PomAnalyzer.VulnsNotFoundException(e.getMessage());
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        } catch (OPALException e) {
            throw new OPALException(e.getMessage());
        } catch (MissingArtifactException e) {
            throw new MissingArtifactException(e.getMessage(), e.getCause());
        } catch (ParseException e) {
            throw new ParseException(e.getErrorType());
        } catch (RepositoryUtil.JarNotFoundException e) {
            throw new RepositoryUtil.JarNotFoundException(e.getMessage());
        } finally {
            try {
                FileUtils.deleteDirectory(new File(repositoryName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
