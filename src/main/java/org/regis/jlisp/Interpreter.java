package org.regis.jlisp;

import java.io.Reader;
import java.io.StringReader;

import org.regis.jlisp.parser.ParseException;
import org.regis.jlisp.parser.Parser;

public class Interpreter {

    public static Object eval(String code) throws ParseException {
        return eval(new StringReader(code));
    }

    public static Object eval(Reader reader) throws ParseException {
        Parser parser = new Parser(reader);
        return new Process(parser.parse()).run();
    }
}
