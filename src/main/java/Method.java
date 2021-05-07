import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Method {
    Long id;
    String name;
    List<List<Long>> traces;

    public Method(Long id){
        this.id = id;
        traces = new ArrayList<>();
    }

    public static boolean containsID(Set<Method> methods, Long id) {
        for (Method m : methods) {
            if(m.id.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
