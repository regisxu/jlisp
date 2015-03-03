package org.regis.jlisp;

import java.io.Reader;
import java.io.StringReader;

public class Interpreter {

    public static Object eval(String code) throws ParseException {
        return eval(new StringReader(code));
    }

    public static Object eval(Reader reader) throws ParseException {
        Parser parser = new Parser(reader);
        return new Process(parser.parse()).run();
    }
}
