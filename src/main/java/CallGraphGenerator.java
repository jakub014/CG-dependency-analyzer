import com.google.common.collect.BiMap;
import eu.fasten.analyzer.javacgopal.data.CallGraphConstructor;
import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.PartialCallGraph;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import eu.fasten.core.data.DirectedGraph;
import eu.fasten.core.data.ExtendedRevisionJavaCallGraph;
import eu.fasten.core.maven.utils.MavenUtilities;
import eu.fasten.core.merge.LocalMerger;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;
import org.json.JSONArray;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CallGraphGenerator {

    public static void main(String[] args) throws OPALException, MissingArtifactException, IOException, ParseException {

        MavenCoordinate depcoord = new MavenCoordinate("com.googlecode.json-simple", "json-simple", "1.1.1", "jar");
        final File depfile = new MavenCoordinate.MavenResolver().downloadArtifact(depcoord, MavenUtilities.MAVEN_CENTRAL_REPO);

        //Pass the path as an argument
        File file = new File(args[0]);

        final CallGraphConstructor opalCG = new CallGraphConstructor(file, "", "CHA");
        final PartialCallGraph cg = new PartialCallGraph(opalCG, true);
        final ExtendedRevisionJavaCallGraph rcg = ExtendedRevisionJavaCallGraph.extendedBuilder()
            .graph(cg.getGraph())
            .product("helloGraphs")
            .version("develop-snapshot")
            .classHierarchy(cg.getClassHierarchy())
            .nodeCount(cg.getNodeCount())
            .build();

        final CallGraphConstructor opalCGDep = new CallGraphConstructor(depfile, "", "CHA");
        final PartialCallGraph cgDep = new PartialCallGraph(opalCGDep, true);
        final ExtendedRevisionJavaCallGraph rcgDep = ExtendedRevisionJavaCallGraph.extendedBuilder()
            .graph(cgDep.getGraph())
            .product(depcoord.getProduct())
            .version(depcoord.getVersionConstraint())
            .classHierarchy(cgDep.getClassHierarchy())
            .nodeCount(cgDep.getNodeCount())
            .build();


        List<ExtendedRevisionJavaCallGraph> depSet = new ArrayList<>();
        depSet.add(rcg);
        depSet.add(rcgDep);

        LocalMerger merger = new LocalMerger(depSet);
        DirectedGraph mergedDirectedGraph = merger.mergeAllDeps();
        BiMap<Long, String> allUris = merger.getAllUris();

        //TODO understand why amount of methods changes between runs
        System.out.println(rcgDep.mapOfAllMethods().size());
        System.out.println(rcg.mapOfAllMethods().size());
        //TODO understand why there are less nodes in the merged Graph
        System.out.println(mergedDirectedGraph.numNodes());

        JSONParser jsonParser = new JSONParser();
        JSONArray vulnUris_json = (JSONArray) jsonParser.parse(
                new FileReader("../resources/vulnerable_uris.json"));
        HashSet<String> vulnUris = (HashSet<String>) getVulnerableUris(vulnUris_json, allUris);

        //TODO do something with lists of affected methods
        List<List<String>> temp = new ArrayList<>();
        for (String vulnUri : vulnUris) {
            temp.add(getAffectedMethods(mergedDirectedGraph, allUris, vulnUri, rcg.product));
        }
    }

    /**
     * Takes the intersection of available URIs and the set of known vulnerable URIs in vulnerable_uris.json.
     * The set of methods in projects is assumed to be smaller than the set of known vulnerable methods (6000+).
     * @param vulnUris_json known vulnerable URIs
     * @param allUris available URIs in the analyzed project
     * @return the intersection of URIs
     */
    public static Set<String> getVulnerableUris(JSONArray vulnUris_json, BiMap<Long, String> allUris) {
        Iterator<Object> iterator = vulnUris_json.iterator();
        Set<String> vulnUris = new HashSet<>();
        String[] allUris_val = (String[]) allUris.values().toArray();
        while (iterator.hasNext()) {
            String currentUri = (String) iterator.next();
            if (Arrays.binarySearch(allUris_val, currentUri) >= 0) {
                vulnUris.add(currentUri);
            }
        }
        return vulnUris;
    }

    /**
     * Returns the id corresponding to the method name given in the allUris map.
     * @param allUris allUris
     * @param methodUri the methodname
     * @return the id
     */
    public static Long getMethodID(BiMap<Long, String> allUris, String methodUri) {
        for (Map.Entry<Long, String> x : allUris.entrySet()) {
            //TODO there might be better ways of getting the local ID
            if(x.getValue().contains(methodUri)) {
                return x.getKey();
            }
        }
        return -1l;
    }

    /**
     *
     * Returns a list of all affected methods in the project.
     * @param mergedGraph the merged graph of a RCG
     * @param vulnerableMethodUri the vulnerable method in the dependency
     * @return a list of all affected methods in the project
     */
    public static List<String> getAffectedMethods(DirectedGraph mergedGraph, BiMap<Long, String> allUris,  String vulnerableMethodUri, String projectName) {
        Set<Long> visited = new HashSet<>();
        Set<Long> toBeVisited = new HashSet<>();

        Long vulnerableMethodID = getMethodID(allUris, vulnerableMethodUri);

        toBeVisited.add(vulnerableMethodID);

        //TODO add method tracing
        while(!toBeVisited.isEmpty()) {
            Set<Long> toBeVisitedNext = new HashSet<>();
            for(Long method : toBeVisited) {
                //toBeVisited.remove(method);

                if(!visited.contains(method)) {
                    visited.add(method);
                }
                mergedGraph.edgesOf(method).stream().map(x -> x.leftLong()).forEach(x -> {
                    if(!visited.contains(x)) {
                        toBeVisitedNext.add(x);
                    }
                });
            }
            toBeVisited = toBeVisitedNext;
        }

        //TODO to improve performance change way of detecting if method is in correct repo
        return  visited.stream().map(x -> allUris.get(x)).filter(x -> x.contains(projectName)).collect(Collectors.toList());
    }
}
