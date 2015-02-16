package org.regis.jlisp;

import java.util.LinkedList;


public class SExpression {
    public SExpression(Object... objs) {
        list = new LinkedList<>();
        for (Object obj : objs) {
            list.add(obj);
        }
    }

    LinkedList<Object> list;
    
    public String toString() {
        return list == null ? null : list.toString();
    }
}
