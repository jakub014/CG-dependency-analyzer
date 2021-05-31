import DependencyAnalyzer.Dependency;
import DependencyAnalyzer.PomAnalyzer;
import DependencyAnalyzer.VulnsNotFoundException;
import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class IndependentScript {

    public static void listOfFiles(File dirPath, List<File> targetDirs) {
        File filesList[] = dirPath.listFiles();
        for (File file : filesList) {
            if (file.isDirectory() && file.getName().equals("target")) {
                System.out.println("Target dir path: " + file.getName());
                targetDirs.add(file);
            } else {
                if (file.isDirectory()) {
                    listOfFiles(file, targetDirs);
                }
            }
        }
    }

    public static void main(String[] args) {

        String filePath = "analysisResults/analysed-repos-maven-inner-projects.txt";
//        File file = new File(filePath);
//        try {
//            file.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        List<RepositoryUtil.Pair> data = new ArrayList<>();
      //  data.add(new RepositoryUtil.Pair("https://github.com/data-integrations/wrangler", "develop"));
        //data.add(new RepositoryUtil.Pair("https://github.com/database-rider/database-rider", "master"));
        data.add(new RepositoryUtil.Pair("https://github.com/aliyun/aliyun-apsaradb-hbase-demo", "master"));


        for (RepositoryUtil.Pair dataPair : data) {
            String link = dataPair.getLeft();
            // 1st part variables
            String defaultBranch =  dataPair.getRight();

            // 2nd part variables
            ProjectType projectType = ProjectType.MAVEN;

            // Get repository name and standardize link.
            RepositoryUtil.Pair pair = RepositoryUtil.getRepoAndLink(link);
            String repositoryName = pair.getLeft();
            link = pair.getRight();

            // Download repository from GitHub.
            System.out.println("DOWNLOADING REPOSITORY FROM LINK " + link);
            String repositoryPath = RepositoryUtil.downloadRepository(link, repositoryName, defaultBranch);
            System.out.println("SUCCESSFULLY DOWNLOADED REPOSITORY " + repositoryPath);

            String rootPomXMLPath = repositoryPath + "/pom.xml";
           // String rootPomXMLPath = "amazon-kinesis-client/amazon-kinesis-client-master" + "/pom.xml";

            RepositoryUtil.buildMavenProject(rootPomXMLPath);

            List<File> targetDirs = new ArrayList<>();
            File parent = new File(repositoryName);
            listOfFiles(parent, targetDirs);
            try {
                Writer output = new FileWriter(filePath, true);
                String result = "FOUND A TOTAL OF " + targetDirs.size() + " TARGET FOLDER FOR PROJECT " + repositoryName + "\n\n\n";
                output.append(result);
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (File f : targetDirs) {
                String innerJarPath = null;
                for (File f2 : f.listFiles()) {
                    if (f2.getName().endsWith(".jar")) {
                        innerJarPath = f2.getAbsolutePath();
                        break;
                    }
                }

                System.out.println("INNER JAR PATH IS " + innerJarPath);

                String pomXMLPath = null;
                for (File f3 : f.getParentFile().listFiles()) {
                    if (f3.isFile() && f3.getName().equals("pom.xml")) {
                        pomXMLPath = f3.getAbsolutePath();
                    }
                }

                System.out.println("INNER POM XML PATH IS " + pomXMLPath + "\n");
                try {
                    if (innerJarPath != null) {
                        // Get dependencies from project dependency file.
                        List<Dependency> dependencyList;
                        if (projectType == ProjectType.MAVEN) {
                            System.out.println("GETTING DEPENDENCIES FROM pom.xml file");
                            System.out.println("GETTING pom.xml DEPENDENCIES FROM path " + rootPomXMLPath);
                            dependencyList = PomAnalyzer.getProjectDependencies(rootPomXMLPath);
                            System.out.println("SUCCESSFULLY RETRIEVED DEPENDENCIES FROM pom.xml file");

                            List<MavenCoordinate> coordList = new ArrayList<>();
                            for (Dependency dep : dependencyList) {
                                MavenCoordinate depcoord = new MavenCoordinate(dep.groupId, dep.artifactId, dep.version, "jar");
                                coordList.add(depcoord);
                            }
                            if (dependencyList.size() > 0) {
                                MavenCoordinate[] toBeFilled = new MavenCoordinate[coordList.size()];
                                VulnerabilityTracer.traceProjectVulnerabilities(new File(innerJarPath), coordList.toArray(toBeFilled), repositoryName, link, defaultBranch);
                                Writer output = new FileWriter(filePath, true);
                                String result = "SUCCESSFULLY ANALYZED JAR PATH " + innerJarPath + " USING ROOT POM FILE AT " + rootPomXMLPath + "\n";
                                output.append(result);
                                output.close();
                            } else {
                                Writer output = new FileWriter(filePath, true);
                                String result = "NO VULNERABILITIES FOUND USING ROOT POM FILE AT " + rootPomXMLPath + " FOR JAR WITH PATH" + innerJarPath + "\n";
                                output.append(result);
                                output.close();
                            }

                        }

                        if (pomXMLPath != null) {
                            // Get dependencies from project dependency file.
                            if (projectType == ProjectType.MAVEN) {
                                System.out.println("GETTING DEPENDENCIES FROM pom.xml file");
                                System.out.println("GETTING pom.xml DEPENDENCIES FROM path " + pomXMLPath);
                                dependencyList = PomAnalyzer.getProjectDependencies(pomXMLPath);
                                System.out.println("SUCCESSFULLY RETRIEVED DEPENDENCIES FROM pom.xml file");

                                List<MavenCoordinate> coordList = new ArrayList<>();
                                for (Dependency dep : dependencyList) {
                                    MavenCoordinate depcoord = new MavenCoordinate(dep.groupId, dep.artifactId, dep.version, "jar");
                                    coordList.add(depcoord);
                                }
                                if (dependencyList.size() > 0) {
                                    MavenCoordinate[] toBeFilled = new MavenCoordinate[coordList.size()];
                                    VulnerabilityTracer.traceProjectVulnerabilities(new File(innerJarPath), coordList.toArray(toBeFilled), repositoryName, link, defaultBranch);
                                    Writer output = new FileWriter(filePath, true);
                                    String result = "SUCCESSFULLY ANALYZED JAR PATH " + innerJarPath + " USING NON-ROOT POM FILE AT " + pomXMLPath + "\n";
                                    output.append(result);
                                    output.close();
                                } else {
                                    Writer output = new FileWriter(filePath, true);
                                    String result = "NO VULNERABILITIES FOUND USING NON-ROOT POM FILE AT " + pomXMLPath + " FOR JAR WITH PATH" + innerJarPath + "\n";
                                    output.append(result);
                                    output.close();
                                }
                            }
                        }
                    }
                } catch (OPALException | IOException | ParseException | MissingArtifactException e) {
                    e.printStackTrace();
                    try {
                        Writer output = new FileWriter(filePath, true);
                        output.append("EXCEPTION " + e.getClass() + " THROWN FOR JAR WITH PATH " + innerJarPath + "\n");
                        output.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}