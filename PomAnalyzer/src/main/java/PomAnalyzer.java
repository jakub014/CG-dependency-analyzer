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
import java.io.InputStream;
import java.util.ArrayList;


public class PomAnalyzer {

    private static final String FILENAME = "D:\\Repos\\CG-dependency-analyzer\\pom.xml";

    public static void main(String[] args) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        ArrayList<Dependency> listOfDeps = new ArrayList<>();
        ArrayList<Dependency> listOfVuln = new VulnParser("D:\\Repos\\CG-dependency-analyzer\\PomAnalyzer\\vulnerable_pkg_vers.json").readVuln();

        try {

            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(FILENAME));

            doc.getDocumentElement().normalize();

            System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
            System.out.println("------");

            NodeList list = doc.getElementsByTagName("dependency");

            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    String groupId = element.getElementsByTagName("groupId").item(0).getTextContent();
                    String artifactId = element.getElementsByTagName("artifactId").item(0).getTextContent();
                    String version = element.getElementsByTagName("version").item(0).getTextContent();
                    listOfDeps.add(new Dependency(groupId, artifactId, version));
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }


        // retainAll finds the intersection of both
        System.out.print(listOfVuln.retainAll(listOfDeps));

    }
}
