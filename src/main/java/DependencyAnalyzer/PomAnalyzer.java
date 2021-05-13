package DependencyAnalyzer;

import java.util.stream.Collectors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PomAnalyzer {

    private static final String VULNERABILITY_FILE_PATH = "src/main/resources/pkg_cves.json";
    static final List<Dependency> listOfVuln = new VulnParser(VULNERABILITY_FILE_PATH).readVuln();

    public static List<Dependency> getProjectDependencies(String pomXMLPath) throws VulnsNotFoundException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        ArrayList<Dependency> listOfDeps = new ArrayList<>();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(pomXMLPath));

            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("dependency");
            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    try {
                        String groupId = element.getElementsByTagName("groupId").item(0).getTextContent();
                        String artifactId = element.getElementsByTagName("artifactId").item(0).getTextContent();
                        String version = element.getElementsByTagName("version").item(0).getTextContent();

                        if (version.startsWith("${") && version.endsWith("}")) {
                            String trimmedVersion = version.substring(2, version.length()-1);
                            NodeList propertiesList = doc.getElementsByTagName("properties");

                            Element propertyElement = (Element) propertiesList.item(0);
                            version = propertyElement.getElementsByTagName(trimmedVersion).item(0).getTextContent();
                        }
                        listOfDeps.add(new Dependency(groupId, artifactId, version));
                    } catch (NullPointerException e) {
                        //INCOMPLETE DEPENDENCY DEFINITION IN POM

                    }

                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("POM NOT FOUND");
        }

        //finds the intersection of both
        List<Dependency> result = listOfVuln.stream().distinct().filter(listOfDeps::contains).collect(Collectors.toList());

        if(result.size() > 0) {
            return result;
        } else {
            throw new VulnsNotFoundException("Vulnerabilities not found in path " + pomXMLPath);
        }
    }
}
