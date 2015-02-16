package org.regis.jlisp;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Frame {
    String name;
    List<Variable> variables = new LinkedList<>();

    public Frame(SExpression expr, List<Object> args) {

        Iterator<Object> code = expr.list.iterator();
        name = ((Symbol) code.next()).name;
        List<String> argNames = ((SExpression) code.next()).list.stream().map(entry -> {
            return ((Symbol) entry).name;
        }).collect(Collectors.toList());
        Iterator<Object> it = args.iterator();
        for (String argName : argNames) {
            Object arg = it.next();
            variables.add(new Variable(argName, arg));
        }
    }
}
