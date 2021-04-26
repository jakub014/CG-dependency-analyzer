import eu.fasten.analyzer.javacgopal.data.CallGraphConstructor;
import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.PartialCallGraph;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import eu.fasten.core.data.ExtendedRevisionJavaCallGraph;
import eu.fasten.core.maven.utils.MavenUtilities;
import eu.fasten.core.merge.LocalMerger;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import scala.reflect.internal.TreeInfo;

public class CallGraphGenerator {
    public static void main(String[] args) throws OPALException, MissingArtifactException {

        MavenCoordinate depcoord = new MavenCoordinate("com.googlecode.json-simple", "json-simple", "1.1.1", "jar");
        final var depfile = new MavenCoordinate.MavenResolver().downloadArtifact(depcoord, MavenUtilities.MAVEN_CENTRAL_REPO);

        File file = new File("C:\\Users\\Jakub\\Desktop\\Playing round here\\helloGraphs.jar");

        final var opalCG = new CallGraphConstructor(file, "", "CHA");
        final var cg = new PartialCallGraph(opalCG, true);
        final ExtendedRevisionJavaCallGraph rcg = ExtendedRevisionJavaCallGraph.extendedBuilder()
            .graph(cg.getGraph())
            .product("helloGraphs")
            .version("develop-snapshot")
            .classHierarchy(cg.getClassHierarchy())
            .nodeCount(cg.getNodeCount())
            .build();

        final var opalCGDep = new CallGraphConstructor(depfile, "", "CHA");
        final var cgDep = new PartialCallGraph(opalCGDep, true);
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

        var merger = new LocalMerger(depSet);
        var mergedDirectedGraph = merger.mergeAllDeps();
        var allUris = merger.getAllUris();

        System.out.println(rcgDep.mapOfAllMethods().size());
        System.out.println(rcg.mapOfAllMethods().size());

        System.out.println(mergedDirectedGraph.numNodes());


        //TODO hardcoded
        Long vulnerableMethodID = 12l;


        Set<Long> visited = new HashSet<>();
        Set<Long> toBeVisited = new HashSet<>();

        toBeVisited.add(vulnerableMethodID);

        while(!toBeVisited.isEmpty()) {
            Set<Long> toBeVisitedNext = new HashSet<>();
            for(Long method : toBeVisited) {
                //toBeVisited.remove(method);

                if(!visited.contains(method)) {
                    visited.add(method);
                }
                mergedDirectedGraph.edgesOf(method).stream().map(x -> x.leftLong()).forEach(x -> {
                    if(!visited.contains(x)) {
                        toBeVisitedNext.add(x);
                    }
                });
            }
            toBeVisited = toBeVisitedNext;
        }

        //TODO to improve performance change way of detecting if method is in correct repo
        var hey = visited.stream().map(x -> allUris.get(x)).filter(x -> x.contains("helloGraphs")).toArray();

    }
}
