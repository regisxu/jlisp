

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Frame {
    String name;
    List<Object> local = new LinkedList<>();
    Iterator<Object> code;
    Iterator<Object> current;
    boolean isCode;
    Map<String, Object> variables = new HashMap<>();

    public Frame(SExpression expr, List<Object> args) {

        code = expr.list.iterator();
        name = (String) code.next();
        List<String> argNames = ((SExpression) code.next()).list.stream().map(entry -> {
            return (String) entry;
        }).collect(Collectors.toList());
        Iterator<Object> it = args.iterator();
        for (String argName : argNames) {
            Object arg = it.next();
            if (arg instanceof SExpression) {
                SExpression s = (SExpression) arg;
                local.add(s);
                SExpression bind = new SExpression(new Symbol("bind"), argName);
                local.add(bind);
            } else {
                variables.put(argName, arg);
            }
        }

        current = local.iterator();
        isCode = false;
    }
    
    public SExpression next() {
        if (current.hasNext()) {
            return (SExpression) current.next();
        }

        if (!isCode) {            
            current = code;
            isCode = true;
            return next();
        }
        return null;
    }
}
