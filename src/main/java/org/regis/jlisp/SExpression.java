package org.regis.jlisp;

import java.util.LinkedList;


public class SExpression {
    public LinkedList<Object> list;

    public SExpression(Object... objs) {
        list = new LinkedList<>();
        for (Object obj : objs) {
            list.add(obj);
        }
    }

    
    public String toString() {
        return list == null ? null : list.toString();
    }
}
