package org.regis.jlisp;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InterpreterTest {

    @Test
    public void testEval() throws Exception {
        Assert.assertEquals(Interpreter.eval("(+ 2 3)"), 5);
        Assert.assertEquals(Interpreter.eval("(+ 2 3)\n"), 5);
        Assert.assertEquals(Interpreter.eval("(+ 2 3)\r"), 5);
    }
}
