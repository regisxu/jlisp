
import java.util.LinkedList;


public class SExpression {
    public SExpression(Object... objs) {
        list = new LinkedList<>();
        for (Object obj : objs) {
            list.add(obj);
        }
    }

    LinkedList<Object> list;
}
