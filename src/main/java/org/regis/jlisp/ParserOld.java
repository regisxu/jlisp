package org.regis.jlisp;

import java.util.LinkedList;
import java.util.List;

public class ParserOld {

    public LinkedList<SExpression> parse(String code) {
        LinkedList<SExpression> list = new LinkedList<>();
        Tokenizer tokenizer = new Tokenizer(code);
        while (tokenizer.hasNext()) {
            list.add(buildSexp(tokenizer));
        }
        return list;
    }

    private SExpression buildSexp(Tokenizer tokenizer) {
        tokenizer.readLeftParenthesis();
        LinkedList<Object> list = buildList(tokenizer);
        tokenizer.readRightParenthesis();
        SExpression sexp = new SExpression();
        sexp.list = list;
        return sexp;
    }

    private LinkedList<Object> buildList(Tokenizer tokenizer) {
        LinkedList<Object> list = new LinkedList<>();
        boolean done = false;
        do {
            char c = tokenizer.nextChar();
            switch (c) {
            case '(':
                list.add(buildSexp(tokenizer));
                break;
            case ')':
                done = true;
                break;
            case ' ':
                tokenizer.readSpace();
                break;
            case '"':
                String value = tokenizer.readString();
                list.add(value);
                break;
            default:
                list.add(tokenizer.readToken());
                break;
            }
        } while (!done);
        return list;
    }

    public static void main(String[] args) {
        ParserOld p = new ParserOld();
        List<SExpression> list = p.parse("(+ 1 \"1\")");
        System.out.println();
    }
}
