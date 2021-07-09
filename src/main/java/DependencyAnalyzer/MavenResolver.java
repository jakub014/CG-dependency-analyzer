package DependencyAnalyzer;

import eu.fasten.core.data.Constants;
import eu.fasten.core.maven.data.Revision;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenResolver {

    private static final Logger logger = LoggerFactory.getLogger(MavenResolver.class);

    //small test execution, if it works the main method can be removed
    public static void main(String[] args) {
        String pathToPom = "C:/exampleProject/master/"; //TODO edit this line
        Set<Revision> revisions = resolveFullDependencySetOnline(pathToPom);

        System.out.println("Found dependencies: ");
        for(Revision r : revisions) {
            System.out.println(r.artifactId);
            System.out.println(r.version);
            System.out.println(r.groupId);
            System.out.println("");
        }
    }

    /**
     * Resolves full dependency set online.
     *
     * @return A dependency set (including all transitive dependencies) of given Maven coordinate
     */
    public static Set<Revision> resolveFullDependencySetOnline(String pathToPomXML) {
        Set<Revision> dependencySet = new HashSet<>();
        File dir = new File(pathToPomXML);
        try {
            String extractDependencies = "deps=$(mvn dependency:tree -DoutputType=tgf 2>&1| grep -v WARN | grep -Po \"[0-9]+ (([A-Za-z0-9.\\-_])*:){1,6}\"| cut -f2 -d ' '| cut -f1,2,4 -d ':' | sort | uniq | grep -v 'depresolver') && echo $deps | tr ' ' '\\n'";
            logger.debug("Running 'mvn dependency:list'");
            var cmd = new String[]{
                "bash",
                "-c",
                extractDependencies};

            ProcessBuilder builder = new ProcessBuilder(cmd);
            var process = builder.directory(dir).start();
            var completed = process.waitFor(1, TimeUnit.MINUTES);

            if (!completed) {
                throw new RuntimeException("Maven resolution process timed out after 60 seconds");
            }
            var exitValue = process.exitValue();

            var stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            var stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            var bufferStr = "";

            logger.debug("Maven resolution finished with exit code " + exitValue);
            if (exitValue != 0) {
                while ((bufferStr = stdInput.readLine()) != null) {
                    logger.debug(bufferStr);
                }
                while ((bufferStr = stdError.readLine()) != null) {
                    logger.error("ERROR: " + bufferStr);
                }
                throw new RuntimeException("Maven resolution failed with exit code " + exitValue);
            }
            while ((bufferStr = stdInput.readLine()) != null) {
                var coordinates = bufferStr.split(Constants.mvnCoordinateSeparator);
                try {
                    dependencySet.add(new Revision(coordinates[0], coordinates[1], coordinates[2], new Timestamp(-1)));
                } catch (ArrayIndexOutOfBoundsException e) {
                    logger.error("Error parsing {} to a Maven coordinate", bufferStr, e);
                }
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error resolving Maven artifact: ", e);
        }
        //dependencySet.remove(new Revision(group, artifact, version, new Timestamp(timestamp))); //TODO not sure whether this line is needed
        return dependencySet;
    }
}
