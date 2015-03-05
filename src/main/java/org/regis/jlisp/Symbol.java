package org.regis.jlisp;


public class Symbol {
    public final String name;

    public Symbol(String name) {
        if (name == null) {
            throw new IllegalArgumentException("symbol name can't be null");
        }
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
