package DependencyAnalyzer;

import org.jooq.tools.csv.CSVReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class TextDepFileAnalyzer {

    private static final String VULNERABILITY_FILE_PATH = "src/main/resources/pkg_cves.json";
    static final List<Dependency> listOfVuln = new VulnParser(VULNERABILITY_FILE_PATH).readVuln();

    public static List<Dependency> getProjectDependencies(String depFilePath) throws VulnsNotFoundException {

        ArrayList<Dependency> listOfDeps = new ArrayList<>();

        try {
            // Open file and reader
            File depFile = new File(depFilePath);
            FileReader reader = new FileReader(depFile);
            BufferedReader br = new BufferedReader(reader);

            String nextLine;
            while ((nextLine = br.readLine()) != null) {
                String groupId;
                String artifactId;
                String version;

                if (nextLine.contains(":")) {
                    String[] colonSplit = nextLine.split(":");
                    groupId = colonSplit[0];
                    String[] dollarSplit = colonSplit[1].split("\\$");
                    artifactId = dollarSplit[0];
                    version = dollarSplit[1];
                } else {
//                    String[] dollarSplit = nextLine.split("\\$");
//                    artifactId = dollarSplit[0];
//                    version = dollarSplit[1];
                    continue;
                }
                listOfDeps.add(new Dependency(groupId, artifactId, version));
            }
        } catch (IOException e) {
            System.out.println("TEXT DEPFILE NOT FOUND");
        }

//        for (Dependency d : listOfDeps) System.out.println(d);

        //finds the intersection of both
        List<Dependency> result = listOfVuln.stream().distinct().filter(listOfDeps::contains).collect(Collectors.toList());

        if(result.size() > 0) {
            return result;
        } else {
            throw new VulnsNotFoundException("Vulnerabilities not found in path " + depFilePath);
        }
    }
}
