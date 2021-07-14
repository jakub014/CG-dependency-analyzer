import DependencyAnalyzer.*;
import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import eu.fasten.core.maven.data.Revision;
import java.sql.SQLOutput;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.jooq.tools.csv.CSVReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RepositoryAnalyzer {

    //TODO remove once inputs clarified
    // Booleans
    private static final boolean scanMaven = true;
    private static final boolean scanGradle = false;


    public static void main(String[] args) throws ParseException, IOException {

        // Open logging file
        File file = new File(Const.LOG_FILE_PATH);
        file.createNewFile();

        // Get & read vulnerable packages from lima statements
        File vulnerablePackagesFile = new File("PATH TO VULNERABLE PACKAGES");
        Set<Revision> vulnerablePackages = readVulnerablePackages(vulnerablePackagesFile);

        // Provide path to manifests downloaded by Ruby script
        String pathToManifests = "H:/dependency-analyzer-fasten/CG-dependency-analyzer/scripts/depbot-script/dependencyManifests";
        File manifestDir = new File(pathToManifests);

        File[] repos = manifestDir.listFiles();

        // For every repository where manifests were retrieved
        for (File repo : repos) {
            var tmp = repo.getName().split("#");
            String user = tmp[0];
            String repositoryName = tmp[1];
            System.out.println("User: " + user + " Repo: " + repositoryName);

            Set<RevisionExt> revisions = MavenResolver.resolveDependencySet(repo.getAbsolutePath());

            //Checking package-level vulnerability
            Set<RevisionExt> vulnerableRevisions = getVulnerableDependencies(revisions, vulnerablePackages);

            if (vulnerableRevisions.size() > 0) {
                // Package-level vulnerable

                //download Project
                String link = ""; //TODO based on $user & $repositoryname obtain the link of the repo
                String repositoryPath = "downloaded-repos/" + repositoryName;
                if (!new File(repositoryPath).exists()) {
                    System.out.println("DOWNLOADING REPOSITORY FROM LINK " + link);
                    try {
                        repositoryPath = RepositoryUtil.downloadMavenRepository(link, user+"#"+repositoryName,  new String[]{"master", "main", "develop", "dev"});
                    } catch (IOException e) {
                        // Repo or branches don't exist
                        //TODO Add logging here
                        continue;
                    }
                    System.out.println("SUCCESSFULLY DOWNLOADED REPOSITORY " + repositoryPath);
                } else {
                    System.out.println("REPOSITORY ALREADY DOWNLOADED");
                }


                String rootPomXMLPath = repositoryPath + "/pom.xml";

                //build Project
                if (!RepositoryUtil.buildMavenProject(rootPomXMLPath)) {
                    continue; //TODO discuss: Skip over failed builds (failed builds could still output some jar files)
                }

                //Create Maven coordinates out of revisions
                List<MavenCoordinate> coordList = new ArrayList<>();
                for (RevisionExt rev : revisions) {
                    MavenCoordinate depcoord = new MavenCoordinate(rev.groupId, rev.artifactId, rev.version.toString(), "jar");
                    coordList.add(depcoord);
                }

                //Search for target directories
                List<File> targetDirs = new ArrayList<>();
                listOfTargetFiles(new File(repositoryPath), targetDirs);
                //For every target directory
                for (File f : targetDirs) {
                    //TODO put this scope in a separate method
                    String pomXMLPath = getInnerPomXMLPath(f);

                    //Search for Jar
                    String innerJarPath = getInnerJarPath(f); //TODO Search all Jars & add all to the call graph analysis
                    System.out.println("INNER JAR PATH IS " + innerJarPath);
                    if (innerJarPath != null) {
                        try {
                            MavenCoordinate[] toBeFilled = new MavenCoordinate[coordList.size()];

                            //run call graph analysis
                            new VulnerabilityTracer().traceProjectVulnerabilities(new File(innerJarPath), coordList.toArray(toBeFilled), repositoryName, link);
                            String result = "SUCCESSFULLY ANALYZED JAR PATH "
                                + new File(innerJarPath).getPath();
                            Writer output = new FileWriter(Const.LOG_FILE_PATH, true);
                            output.append(result);
                            output.close();
                        } catch (OPALException e) {
                            //Call graph generation exception
                        }
                    }
                }
            }
        }
    }

    /**
     * Reads in the vulnerable packages from a file.
     * @param vulnerablePackages the file
     * @return a set of revisions
     */
    public static Set<Revision> readVulnerablePackages(File vulnerablePackages) {
        try {
            Set<Revision> vulnerableRevisions = new HashSet<>();
            Scanner sc = new Scanner(vulnerablePackages);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                //TODO convert line to revision
                vulnerableRevisions.add(null);
            }
            return vulnerableRevisions;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + vulnerablePackages.getAbsolutePath());
        }
        return null;
    }

    /**
     * Returns the dependencies that are considered vulnerable according to the vulnerable packages provided.
     * @param dependencies the dependencies
     * @param vulnerableRevisions the packages that are considered vulnerable.
     * @return the dependencies that are considered vulnerable according to the vulnerable packages provided
     */
    public static Set<RevisionExt> getVulnerableDependencies(Set<RevisionExt> dependencies, Set<Revision> vulnerableRevisions) {
        Set<RevisionExt> vulnerabledependencies = new HashSet<>();
        for (RevisionExt r : dependencies) {
            for(Revision vR : vulnerableRevisions) {
                if (r.groupId.equals(vR.groupId) && r.artifactId.equals(vR.artifactId) && r.version.equals(vR.version)) {
                    vulnerabledependencies.add(r);
                }
            }
        }
        return vulnerabledependencies;
    }

    private static List<String> getProjectsToInspect() throws IOException {
        return Arrays.asList(FileUtils.readFileToString(
                new File(Const.INSPECTING_PROJECTS_PATH), StandardCharsets.UTF_8
        ).split("\n"));
    }


    //TODO remove once inputs clarified
    /**
     * Read CSV file and intersects with FILTER_DATA_PATH, creating a ProjectInfo class for each entry.
     *
     * @return The list of projects to be analyzed.
     */
    private static List<ProjectInfo> getProjectInfoList(String csvPath) throws IOException, ParseException {
        System.out.println("LOADING PROJECTS FROM " + csvPath);
        List<String> projectsToInspect = getProjectsToInspect();
        for (String s : projectsToInspect) System.out.println(s);

        JSONParser parser = new JSONParser();
        JSONArray data = (JSONArray) parser.parse(new FileReader(Const.FILTER_DATA_PATH));
        CSVReader reader = new CSVReader(new FileReader(csvPath));

        List<ProjectInfo> projectInfoList = new ArrayList<>();
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            String downloadedDepFilePath = nextLine[0];
            String currentUserAndRepo = nextLine[1];
            String relativeDepFilePath = nextLine[2];
            String projectTypeStr = nextLine[3];
            ProjectType projectType;
            if (projectTypeStr.equals("maven")) {
                projectType = ProjectType.MAVEN;
            } else if (projectTypeStr.equals("gradle")) {
                projectType = ProjectType.GRADLE;
            } else {
                continue;
            }

            var found = false;
            for (String p : projectsToInspect) {
                if (p.trim().equals(currentUserAndRepo.trim())) {
                    found = true;
                    break;
                }
            }
            if (!found) continue;

            for (Object o : data) {
                JSONObject jsonProject = (JSONObject) o;
                String repository = (String) jsonProject.get("repository");
                String user = (String) jsonProject.get("user");
                String userAndRepo = user + "/" + repository;

                if (((projectType == ProjectType.GRADLE && scanGradle)
                        || (projectType == ProjectType.MAVEN && scanMaven))
                        && (userAndRepo.trim().equals(currentUserAndRepo.trim()))) {
                    projectInfoList.add(new ProjectInfo(projectType, downloadedDepFilePath, relativeDepFilePath, jsonProject));
                    break;
                }
            }
        }
        System.out.println("SUCCESSFULLY LOADED " + projectInfoList.size() + " " + projectInfoList.get(0).getProjectType() + " PROJECTS");
        return projectInfoList;
    }

    public static void listOfTargetFiles(File dirPath, List<File> targetDirs) {
        File filesList[] = dirPath.listFiles();
        for (File file : filesList) {
            if (file.isDirectory() && file.getName().equals("target")) {
                targetDirs.add(file);
            } else {
                if (file.isDirectory()) {
                    listOfTargetFiles(file, targetDirs);
                }
            }
        }
    }

    public static void listOfBuildLibsFiles(File dirPath, List<File> buildLibsDirs) {
        File filesList[] = dirPath.listFiles();
        for (File file : filesList) {
            if (file.isDirectory() && file.getName().equals("build")) {
                for (File file2 : file.listFiles()) {
                    if (file2.isDirectory() && file2.getName().equals("libs")) {
                        buildLibsDirs.add(file2);
                    }
                }
            } else {
                if (file.isDirectory()) {
                    listOfBuildLibsFiles(file, buildLibsDirs);
                }
            }
        }
    }

    private static String getInnerJarPath(File f) {
        //TODO always finds the first jar (what to do if there are many?)
        var res = f.listFiles();
        if (res != null) {
            for (File f2 : res) {
                if (f2.getName().endsWith(".jar")) {
                    return f2.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static String getInnerPomXMLPath(File f) {
        String pomXMLPath = null;
        var res2 = f.getParentFile().listFiles();
        if (res2 != null) {
            for (File f3 : res2) {
                if (f3.isFile() && f3.getName().equals("pom.xml")) {
                    pomXMLPath = f3.getAbsolutePath();
                }
            }
        }
        return pomXMLPath;
    }

    private static String getInnerBuildGradlePath(File f) {
        String buildGradlePath = null;
        var res2 = f.getParentFile().getParentFile().listFiles();
        if (res2 != null) {
            for (File f2 : res2) {
                if (f2.isFile() && (f2.getName().equals("build.gradle") || f2.getName().equals("build.gradle.kts"))) {
                    buildGradlePath = f2.getAbsolutePath();
                }

            }
        }
        return buildGradlePath;
    }
}
