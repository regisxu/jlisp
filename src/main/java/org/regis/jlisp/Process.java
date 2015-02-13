package org.regis.jlisp;


import java.util.LinkedList;

public class Process {

    LinkedList<Frame> stack = new LinkedList<>();
    Object value;

    public Process() {

    }

    public SExpression next() {
        while (stack.size() > 0) {
            Frame f = stack.getFirst();
            SExpression expr = f.next();
            if (expr == null) {
                stack.removeFirst();
            } else {
                return expr;
            }
        }
        return null;
    }
}
