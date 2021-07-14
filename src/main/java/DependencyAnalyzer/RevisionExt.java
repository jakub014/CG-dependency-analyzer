package DependencyAnalyzer;

import eu.fasten.core.maven.data.Revision;
import java.sql.Timestamp;

public class RevisionExt extends Revision {

    boolean isDirectDependency;

    public RevisionExt(String groupId, String artifactId, String version, Timestamp createdAt, boolean directDependency) {
        super(groupId, artifactId, version, createdAt);
        this.isDirectDependency = directDependency;
    }

}
