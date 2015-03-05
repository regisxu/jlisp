package org.regis.jlisp;


public class Symbol {
    public String name;

    public Symbol() {

    }

    public Symbol(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
