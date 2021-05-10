import PomAnalyzer.PomAnalyzer;
import PomAnalyzer.Dependency;

import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jooq.tools.json.ParseException;

public class MainScript {

    private static final String VULNERABILITY_FILE_PATH = "src/main/java/PomAnalyzer/data/NewFormat.json";


    public static void main(String[] args) {
        // TODO: Get set of projects to use from input file and execute the code below for each project.

        try {
            String link = "https://github.com/liquibase/liquibase-hibernate";
            String defaultBranch = "main";

            // Get repository name and standardize link.
            RepositoryUtil.Pair pair = RepositoryUtil.getRepoAndLink(link);
            String repositoryName = pair.getLeft();
            link = pair.getRight();

            // Download repository from GitHub.
            System.out.println("DOWNLOADING REPOSITORY FROM LINK " + link);
            String repositoryPath = RepositoryUtil.downloadRepository(link, repositoryName, defaultBranch);
            System.out.println("SUCCESSFULLY DOWNLOADED REPOSITORY " + repositoryPath);

            // Get dependencies from project dependency file.
            System.out.println("GETTING DEPENDENCIES FROM pom.xml file");
            String pomXMLPath = repositoryPath + "/pom.xml";
            List<Dependency> dependencyList = PomAnalyzer.getProjectDependencies(pomXMLPath, VULNERABILITY_FILE_PATH);
            System.out.println("SUCCESSFULLY RETRIEVED DEPENDENCIES FROM pom.xml file");
            System.out.println("DEPENDENCIES ARE: ");
            for (Dependency dependency : dependencyList) {
                System.out.println(dependency);
            }

            // If no exception thrown, vulnerabilities have been found in the dependency file of project. Continue.
            // Get the jar of the downloaded repository.
            System.out.println("GENERATING JAR FILE");
            String jarPath = RepositoryUtil.getRepositoryJAR(repositoryName, defaultBranch);
            System.out.println("SUCCESSFULLY GENERATED JAR FILE WITH JAR PATH " + jarPath);

            // TODO: Continue with call graph generation for jar + dependencies and track affected methods

            List<MavenCoordinate> coordList = new ArrayList<MavenCoordinate>();
            for (Dependency dep : dependencyList) {
                MavenCoordinate depcoord = new MavenCoordinate(dep.groupId, dep.artifactId, dep.version, "jar");
                coordList.add(depcoord);
            }

            MavenCoordinate[] toBeFilled = new MavenCoordinate[coordList.size()];
            VulnerabilityTracer.traceProjectVulnerabilities(new File(jarPath), coordList.toArray(toBeFilled), repositoryName);



        } catch (PomAnalyzer.VulnsNotFoundException | RepositoryUtil.JarNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } catch (OPALException e) {
            e.printStackTrace();
        } catch (MissingArtifactException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

    }
}
