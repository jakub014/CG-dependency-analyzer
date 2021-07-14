package DependencyAnalyzer;

import eu.fasten.core.data.Constants;
import eu.fasten.core.maven.data.Revision;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenResolver {

    private static final Logger logger = LoggerFactory.getLogger(MavenResolver.class);

    //small test execution, if it works the main method can be removed
    public static void main(String[] args) throws IOException {
        //String dir = "C:\\dependency-analyzer-fasten\\spider-flow-master"; <-- example path
        String dir = "<Path to project>"; //TODO edit this line

        var revisions = resolveDependencySet(dir);

        System.out.println("Found dependencies: ");
        for(RevisionExt r : revisions) {
            System.out.println(r.groupId + " "+ r.artifactId + " " + r.version + " Direct dependency: " + r.isDirectDependency);
        }
    }


    public static Set<RevisionExt> resolveDependencySet(String pathToPomXML) {
        try {

            String outputfilePath = pathToPomXML + "\\mvn_dependency_tree.txt";
            File outputFile = new File(outputfilePath);
            String writeDependencies = "mvn dependency:tree -DoutputFile="+outputfilePath+" -DappendOutput=true";


            var commands = new String[]{writeDependencies}; //TODO Check what needs to be added here for linux

            //Some extra stuff for windows
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                commands = new String[]{"cmd.exe", "/c", writeDependencies};
            }


            ProcessBuilder builder = new ProcessBuilder(commands);
            Process p = builder.directory(new File(pathToPomXML)).start();
            var completed = p.waitFor(5, TimeUnit.MINUTES);
            if (completed) {
                var result = parseDependencyTreeOutput(outputFile);
                outputFile.delete();
                return result;
            } else {
                System.out.println("Process timed out after 5 minutes");
                outputFile.delete();
                return null;
            }
        } catch (Exception ex) {}
        return null;
    }

    /**
     * Parses the output of 'mvn dependency:tree'
     * @param dependencyTree
     * @return
     * @throws FileNotFoundException
     */
    private static Set<RevisionExt> parseDependencyTreeOutput(File dependencyTree) throws FileNotFoundException {
        Set<RevisionExt> dependencies = new HashSet<>();
        Scanner sc = new Scanner(dependencyTree);

        String[] dependencyTreeStructureElements = {"+-", "|", "\\-"};

        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            char firstChar = line.charAt(0);
            if(!Character.isLetter(firstChar) && !Character.isDigit(firstChar)) {
                if (line.startsWith("+- ") || line.startsWith("\\- ")) {
                    //Direct dependency
                    String dependencyString = line.substring(3).trim();
                    dependencies.add(parseDependencyStringToRevision(dependencyString, true));
                } else {
                    line = line.trim();
                    String dependencyString = multipleRemoves(dependencyTreeStructureElements, line);
                    dependencies.add(parseDependencyStringToRevision(dependencyString, false));
                }
            }
        }
        sc.close();
        return dependencies;
    }

    /**
     * Parses a dependency string of the format: <GroupID>:<artifactID>:<packaging>:<Version>:<compile> to a revision
     * @param dependency a dependency string of the format: <GroupID>:<artifactID>:<packaging>:<Version>:<compile>
     * @return the revision
     */
    private static RevisionExt parseDependencyStringToRevision(String dependency, boolean directDependency) {
        String[] tmp = dependency.split(":");
        var groupID = tmp[0];
        var artifactID = tmp[1];
        var version = tmp[3];
        return new RevisionExt(groupID, artifactID, version, new Timestamp(-1), directDependency);
    }

    private static String multipleRemoves(String[] charsToRemove, String targetString) {
        String tmp = targetString;
        for (String s : charsToRemove) {
            tmp = tmp.replace(s, "");
        }
        return tmp.trim();
    }
}
