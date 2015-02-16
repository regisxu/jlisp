package org.regis.jlisp;

public class Call {
    Symbol name;
    int count;
    
    public Call(Symbol name, int count) {
        this.name = name;
        this.count = count;
    }
}
