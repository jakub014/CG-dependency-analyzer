package DependencyAnalyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BuildGradleAnalyzer {

    private static final String VULNERABILITY_FILE_PATH = "src/main/resources/pkg_cves.json";
    static final List<Dependency> listOfVuln = new VulnParser(VULNERABILITY_FILE_PATH).readVuln();

    public static List<Dependency> getProjectDependencies(String baseRepositoryPath) throws VulnsNotFoundException {
        ArrayList<Dependency> listOfDeps = new ArrayList<>();
        File repoDir = new File(baseRepositoryPath);
        File getDependenciesSH = new File(baseRepositoryPath + "/get_dependencies.sh");
        try {
            getDependenciesSH.createNewFile();

            Writer writer = new FileWriter(getDependenciesSH.getAbsolutePath(), true);
            String result = "bash gradlew dependencies | sed -n 's/.*--- \\([^ ]*\\).*/\\1/p' | sort | uniq";
            writer.write(result);
            writer.close();

            Process process = Runtime.getRuntime().exec("bash get_dependencies.sh", null, repoDir.getAbsoluteFile());

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] depDetails = line.split(":");
                System.out.println(Arrays.toString(depDetails));
                if (depDetails.length == 3) {
                    listOfDeps.add(new Dependency(depDetails[0], depDetails[1], depDetails[2]));
                }
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("DEPENDENCY EXTRACTION SUCCESS!");
            } else {
                //abnormal...
                System.out.println("DEPENDENCY EXTRACTION FAILED!");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            getDependenciesSH.delete();
        }

        //finds the intersection of both
        List<Dependency> result = listOfVuln.stream().distinct().filter(listOfDeps::contains).collect(Collectors.toList());
        if (result.size() > 0) {
            return result;
        } else {
            throw new VulnsNotFoundException("Vulnerabilities not found in path " + baseRepositoryPath + "/build.gradle");
        }
    }

    public static void main(String[] args) {
        try {
            getProjectDependencies("rewrite/rewrite-main");
        } catch (VulnsNotFoundException e) {
            e.printStackTrace();
        }
    }
}
