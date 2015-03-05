package org.regis.jlisp;

import java.io.InputStreamReader;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InterpreterTest {

    @Test
    public void test_eval() throws Exception {
        Assert.assertEquals(Interpreter.eval("(+ 2 3)"), 5);
        Assert.assertEquals(Interpreter.eval("(+ 2 3)\n"), 5);
        Assert.assertEquals(Interpreter.eval("(+ 2 3)\r"), 5);
    }

    @Test
    public void test_eval_file() throws Exception {
        Interpreter.eval(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("test.lisp")));
    }

    @Test
    public void test_defun_java() throws Exception {
        Assert.assertEquals(
                Interpreter
                        .eval("(defun getProperty \"java:java.lang.System.getProperty\")\n(getProperty \"java.home\")"),
                System.getProperty("java.home"));
    }
}
