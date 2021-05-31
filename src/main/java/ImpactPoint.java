import java.util.ArrayList;
import java.util.Objects;

public class ImpactPoint {
    Long affectedMethod;
    Long dependencyMethod;
    Long originVulnerableMethod;
    String affectedMethodName;
    String dependencyMethodName;
    String originVulnerableMethodName;

    public ImpactPoint(Long affectedMethod, Long dependencyMethod, Long originVulnerableMethod){
        this.affectedMethod = affectedMethod;
        this.dependencyMethod = dependencyMethod;
        this.originVulnerableMethod = originVulnerableMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImpactPoint)) return false;
        ImpactPoint that = (ImpactPoint) o;
        return Objects.equals(affectedMethod, that.affectedMethod) && Objects.equals(dependencyMethod, that.dependencyMethod) && Objects.equals(originVulnerableMethod, that.originVulnerableMethod) && Objects.equals(affectedMethodName, that.affectedMethodName) && Objects.equals(dependencyMethodName, that.dependencyMethodName) && Objects.equals(originVulnerableMethodName, that.originVulnerableMethodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(affectedMethod, dependencyMethod, originVulnerableMethod, affectedMethodName, dependencyMethodName, originVulnerableMethodName);
    }
}
