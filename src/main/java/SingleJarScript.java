import DependencyAnalyzer.BuildGradleAnalyzer;
import DependencyAnalyzer.Dependency;
import DependencyAnalyzer.PomAnalyzer;
import DependencyAnalyzer.VulnsNotFoundException;
import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SingleJarScript {

    public static void main(String[] args) {
        String link = "https://github.com/alibaba/termd";
        String defaultBranch = "master";

        ProjectType projectType = ProjectType.MAVEN;

        // Get repository name and standardize link.
        RepositoryUtil.Pair pair = RepositoryUtil.getRepoAndLink(link);
        String repositoryName = pair.getLeft();
        link = pair.getRight();

        // Download repository from GitHub.
//        System.out.println("DOWNLOADING REPOSITORY FROM LINK " + link);
//        String repositoryPath = RepositoryUtil.downloadRepository(link, repositoryName, defaultBranch);
//        System.out.println("SUCCESSFULLY DOWNLOADED REPOSITORY " + repositoryPath);

        String baseRepoPath = repositoryName + "/" + repositoryName + "-" + defaultBranch;
        //String jarPath = baseRepoPath + "/build/lib/appmap-1.1.0.jar";
        String gradleBaseDirPath = "";

        String pomXMLPath = baseRepoPath + "/pom.xml";
        String jarPath = baseRepoPath + "/target/termd-core-1.1.7.12-SNAPSHOT.jar";

        try {
            // Get dependencies from project dependency file.
            List<Dependency> dependencyList;
            if (projectType == projectType.MAVEN) {
                System.out.println("GETTING DEPENDENCIES FROM pom.xml file");
                System.out.println("GETTING pom.xml DEPENDENCIES FROM path " + pomXMLPath);
                dependencyList = PomAnalyzer.getProjectDependencies(pomXMLPath);
                System.out.println("SUCCESSFULLY RETRIEVED DEPENDENCIES FROM pom.xml file");
            } else {
                System.out.println("GETTING DEPENDENCIES FROM build.gradle file");
                System.out.println("GETTING build.gradle DEPENDENCIES FROM base path " + gradleBaseDirPath);
                dependencyList = BuildGradleAnalyzer.getProjectDependencies(gradleBaseDirPath);
                System.out.println("SUCCESSFULLY RETRIEVED DEPENDENCIES FROM build.gradle file");
            }

            List<MavenCoordinate> coordList = new ArrayList<>();
            for (Dependency dep : dependencyList) {
                MavenCoordinate depcoord = new MavenCoordinate(dep.groupId, dep.artifactId, dep.version, "jar");
                coordList.add(depcoord);
            }

            MavenCoordinate[] toBeFilled = new MavenCoordinate[coordList.size()];
            VulnerabilityTracer.traceProjectVulnerabilities(new File(jarPath), coordList.toArray(toBeFilled), repositoryName, link, defaultBranch);
        } catch (OPALException | IOException | ParseException | MissingArtifactException | VulnsNotFoundException e) {
            e.printStackTrace();
        }
    }
}
