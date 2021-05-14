import java.util.ArrayList;

public class ImpactPoint {
    Long affectedMethod;
    Long dependencyMethod;
    String affectedMethodName;
    String dependencyMethodName;

    public ImpactPoint(Long affectedMethod, Long dependencyMethod){
        this.affectedMethod = affectedMethod;
        this.dependencyMethod = dependencyMethod;
    }

}
