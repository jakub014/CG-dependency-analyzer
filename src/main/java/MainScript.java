import PomAnalyzer.PomAnalyzer;
import PomAnalyzer.Dependency;

import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MainScript {

    private static final String VULNERABILITY_FILE_PATH = "src/main/resources/pkg_cves.json";


    public static void main(String[] args) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        JSONArray data = (JSONArray) parser.parse(new FileReader("src/main/resources/filteredProjects50StarsLast2Weeks.json"));

        System.out.println(data.size());

        final int startFrom = 0;

        JSONArray results = new JSONArray();
        int counter = 0;

        for (Object o : data) {
            counter++;
            if (counter > startFrom) {
                System.out.println("START ANALYSIS ON PROJECT NO." + counter);
                JSONObject obj = (JSONObject) o;

                JSONObject apiResponse = (JSONObject) obj.get("fullresponse");

                String defaultBranch = (String) apiResponse.get("default_branch");
                String link =(String) apiResponse.get("html_url");
                analyzeRepository(link, defaultBranch);
            }
        }

    }

    public static void analyzeRepository(String link, String defaultBranch) {
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
            List<Dependency> dependencyList = PomAnalyzer.getProjectDependencies(pomXMLPath, VULNERABILITY_FILE_PATH);
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
            VulnerabilityTracer.traceProjectVulnerabilities(new File(jarPath), coordList.toArray(toBeFilled), repositoryName);

        } catch (PomAnalyzer.VulnsNotFoundException e) {
            System.out.println("NO VULNERABLE DEPENDENCIES FOUND");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OPALException e) {
            e.printStackTrace();
        } catch (MissingArtifactException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } catch (RepositoryUtil.JarNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                FileUtils.deleteDirectory(new File(repositoryName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
