import com.google.common.collect.BiMap;
import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import eu.fasten.core.data.DirectedGraph;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public abstract class Tracer {
    static final int MAX_METHOD_TRACING = 30000;

    public abstract void traceProjectVulnerabilities(
            File project, MavenCoordinate[] dependencyCoordinates,
            String projectName, String link, String branch, ProjectInfo projectInfo
    ) throws OPALException, MissingArtifactException, IOException, ParseException;

    static void writeOutputOldFormat(
            String filePath, String projectName,
            String link, String vulnDep, Set<ImpactPoint> results)
    {
        try {
            File file = new File(filePath);
            file.createNewFile();
            Writer output = new FileWriter(filePath, true);
            String result = "";
            result += projectName + "\n";
            result += link + "\n";
            result += "Vulnerable dependency: " + vulnDep + "\n\n";
            for (ImpactPoint i : results) {
                result += ("___" + i.affectedMethodName + "\n");
                result += ("_______" + i.dependencyMethodName + "\n\n");
            }
            output.write(result);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeOutput(
            String filePath, String projectName,
            String link, String vulnDep, Set<ImpactPoint> results)
    {
        try {
            File file = new File(filePath);
            file.createNewFile();
            Writer output = new FileWriter(filePath, true);
            String result = "";
            result += projectName + "\n";
            result += link + "\n";
            result += "Vulnerable dependency: " + vulnDep + "\n\n";
            for (ImpactPoint i : results) {
                result += ("___" + i.affectedMethodName + "\n");
                result += ("_______" + i.dependencyMethodName + "\n");
                result += ("___________" + i.originVulnerableMethodName + "\n");
                result += "METHOD TRACES: \n";
                for (String traceMethod : i.trace) {
                    result += traceMethod + "\n";
                }
            }
            result += "\n\n\n";
            output.write(result);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void logResults(Boolean isVulnerable, ProjectInfo projectInfo, String projectName, String vulnDep) {
        try {
            String filePath;
            if (!isVulnerable) {
                filePath = "analysisResults/stats/negative.txt";
            } else {
                filePath = "analysisResults/stats/positive.txt";
            }
            Writer output = new FileWriter(filePath, true);
            String result = projectInfo.getUser() + ":" + projectName + projectInfo.getRelativeDirectoryPath("/") + " ~ " + vulnDep + "\n";
            output.append(result);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the ids corresponding to the method names given in the allUris map.
     *
     * @param allUris    allUris
     * @param methodUris the method names
     * @return the id
     */
    private static Set<Long> getMethodIDs(BiMap<Long, String> allUris, String[] methodUris) {
        Set<Long> result = new HashSet<>();

        for (int i = 0; i < methodUris.length; i++) {
            int counter = 0;
            for (int k = 0; k < methodUris[i].length(); k++) {
                if (methodUris[i].charAt(k) == '/') {
                    counter++;
                }
                if (counter == 3) {
                    methodUris[i] = methodUris[i].substring(k);
                    break;
                }
            }
        }

        for (String uri : methodUris) {
            for (Map.Entry<Long, String> x : allUris.entrySet()) {
                if (x.getValue() != null && x.getValue().contains(uri)) {
                    result.add(x.getKey());
                }
            }
        }
        return result;
    }

    /**
     * Returns a list of all affected methods in the project.
     *
     * @param mergedGraph          the merged graph of a RCG
     * @param vulnerableMethodUris the vulnerable method in the dependency
     * @return a list of all affected methods in the project
     */
    static Set<ImpactPoint> getAffectedMethods(DirectedGraph mergedGraph, BiMap<Long, String> allUris, String[] vulnerableMethodUris, String projectName) {
        Set<ImpactPoint> impacts = new HashSet<>();
        Set<Long> toBeVisited = getMethodIDs(allUris, vulnerableMethodUris);

        for (Long vulnerableMethod : toBeVisited) {
            Queue<Long> q = new LinkedList<>();
            Set<ImpactPoint> localImpacts = new HashSet<>();
            Set<Long> visited = new HashSet<>();
            Map<Long, Long> discoveredMap = new HashMap<>();

            q.add(vulnerableMethod);
            visited.add(vulnerableMethod);

            System.out.println("CURRENTLY STARTING VISITING METHOD " + allUris.get(vulnerableMethod));
            while (!q.isEmpty()) {
                Long visitedMethod = q.poll();
                var res = mergedGraph.incomingEdgesOf(visitedMethod);
                for (LongLongPair pair : res) {
                    Long callingMethod = pair.leftLong();
                    if (!visited.contains(callingMethod)) {
                        String callingMethodURI = allUris.get(callingMethod);
                        discoveredMap.put(callingMethod, visitedMethod);
                        if (callingMethodURI.contains(projectName)) {
                            localImpacts.add(new ImpactPoint(callingMethod, visitedMethod, vulnerableMethod));
                        } else {
                            visited.add(callingMethod);
                            q.add(callingMethod);
                        }
                    }
                }
            }

            System.out.println("METHOD HAS " + localImpacts.size() + " TOTAL IMPACTS:\n");
            for (ImpactPoint i : localImpacts) {
                List<String> trace = new ArrayList<>();
                Long traceMethod = discoveredMap.get(i.affectedMethod);
                while (discoveredMap.containsKey(traceMethod)) {
                    trace.add(allUris.get(traceMethod));
                    traceMethod = discoveredMap.get(traceMethod);
                }
                trace.add(allUris.get(vulnerableMethod));
                i.trace = trace;
                impacts.add(i);
                System.out.println(allUris.get(i.affectedMethod));
                System.out.println(allUris.get(i.dependencyMethod) + "\n\n");
            }
        }

        for (ImpactPoint i : impacts) {
            i.affectedMethodName = allUris.get(i.affectedMethod);
            i.dependencyMethodName = allUris.get(i.dependencyMethod);
            i.originVulnerableMethodName = allUris.get(i.originVulnerableMethod);
        }

        //TODO to improve performance change way of detecting if method is in correct repo
        return impacts;
    }
}