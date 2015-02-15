package org.regis.jlisp;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Frame {
    String name;
    List<Object> local = new LinkedList<>();
    Iterator<Object> code;
    Iterator<Object> current;
    boolean isCode;
    List<Variable> variables = new LinkedList<>();

    public Frame(SExpression expr, List<Object> args) {

        code = expr.list.iterator();
        name = (String) code.next();
        List<String> argNames = ((SExpression) code.next()).list.stream().map(entry -> {
            return (String) entry;
        }).collect(Collectors.toList());
        if (argNames.size() == 1 && argNames.get(0).equals("...")) {
            for (int i = 0; i < args.size(); ++i) {
                if (args.get(i) instanceof SExpression) {
                    SExpression s = (SExpression) args.get(i);
                    local.add(s);
                    SExpression bind = new SExpression(new Symbol("bind"), "arg" + i);
                    local.add(bind);
                } else {
                    variables.add(new Variable("arg" + i, args.get(i)));
                }
            }
        } else {
            Iterator<Object> it = args.iterator();
            for (String argName : argNames) {
                Object arg = it.next();
                if (arg instanceof SExpression) {
                    SExpression s = (SExpression) arg;
                    local.add(s);
                    SExpression bind = new SExpression(new Symbol("bind"), argName);
                    local.add(bind);
                } else {
                    variables.add(new Variable(argName, arg));
                }
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
