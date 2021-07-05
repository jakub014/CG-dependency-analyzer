import java.util.List;

public class ImpactPoint {
    Long affectedMethod;
    Long dependencyMethod;
    Long originVulnerableMethod;
    String affectedMethodName;
    String dependencyMethodName;
    String originVulnerableMethodName;
    List<String> trace;

    public ImpactPoint(Long affectedMethod, Long dependencyMethod, Long originVulnerableMethod){
        this.affectedMethod = affectedMethod;
        this.dependencyMethod = dependencyMethod;
        this.originVulnerableMethod = originVulnerableMethod;
    }

}
