import eu.fasten.analyzer.javacgopal.data.CallGraphConstructor;
import eu.fasten.analyzer.javacgopal.data.MavenCoordinate;
import eu.fasten.analyzer.javacgopal.data.PartialCallGraph;
import eu.fasten.analyzer.javacgopal.data.exceptions.MissingArtifactException;
import eu.fasten.analyzer.javacgopal.data.exceptions.OPALException;
import eu.fasten.core.data.ExtendedRevisionJavaCallGraph;
import eu.fasten.core.merge.LocalMerger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CallGraphGenerator {
    public static void main(String[] args) throws OPALException, MissingArtifactException {
        //final var file = new MavenCoordinate.MavenResolver().downloadArtifact(new MavenCoordinate("groupID","artifactID", "version", "packaging"), "jar");
        File file = new File("C:\\Users\\Jakub\\Desktop\\Playing round here\\helloGraphs.jar");

        MavenCoordinate dep = new MavenCoordinate("com.googlecode.json-simple","json-simple", "1.1.1", "jar");

        final var opalCG = new CallGraphConstructor(file, "", "CHA");
        final var cg = new PartialCallGraph(opalCG, true);
        final ExtendedRevisionJavaCallGraph rcg = ExtendedRevisionJavaCallGraph.extendedBuilder()
            .graph(cg.getGraph())
            .product(dep.getProduct())
            .version(dep.getVersionConstraint())
            .classHierarchy(cg.getClassHierarchy())
            .nodeCount(cg.getNodeCount())
            .build();



        List<ExtendedRevisionJavaCallGraph> depSet = new ArrayList<>();
        depSet.add(rcg);

        var merger = new LocalMerger(depSet);
        var mergedDirectedGraph = merger.mergeAllDeps();
        var allUris = merger.getAllUris();

        System.out.println(rcg.toJSON());
    }
}
