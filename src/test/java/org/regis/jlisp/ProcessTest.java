package org.regis.jlisp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.regis.jlisp.parser.Parser;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProcessTest {

    @Test
    public void testRun() throws Exception {
        Parser parser = new Parser(new ByteArrayInputStream(("(defun add (a b c) (+ a (+ c b)))\n"
                + "(add (add 2 3 4) 5 6)\n").getBytes()));
        List<SExpression> sexps = null;
        sexps = parser.parse();
        Process p = new Process(sexps);
        Assert.assertEquals(p.run(), 20);
    }

    @Test
    public void testPrint() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Parser parser = new Parser(new ByteArrayInputStream(("(println \"test\")\n").getBytes()));
        List<SExpression> sexps = null;
        sexps = parser.parse();
        Process p = new Process(sexps);
        p.run();
        Assert.assertEquals(new String(out.toByteArray()), "test" + System.lineSeparator());
    }

    @Test
    public void testSpawn() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Parser parser = new Parser(new ByteArrayInputStream(("(println (spawn (println \"test\")))\n").getBytes()));
        List<SExpression> sexps = null;
        sexps = parser.parse();
        Process p = new Process(sexps);
        p.run();
        Thread.sleep(1000);
        Field field = Process.class.getDeclaredField("ider");
        field.setAccessible(true);
        long id = ((AtomicLong) field.get(null)).get() - 1;
        Assert.assertEquals(new String(out.toByteArray()),
                id + System.lineSeparator() + "test" + System.lineSeparator());
    }

    @Test
    public void testSuspend() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Parser parser = new Parser(new ByteArrayInputStream(("(spawn (suspend) (println \"test\"))\n").getBytes()));
        List<SExpression> sexps = null;
        sexps = parser.parse();
        Process p = new Process(sexps);
        p.run();
        Thread.sleep(1000);
        Assert.assertEquals(new String(out.toByteArray()), "");
    }

    @Test
    public void testResume() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Parser parser = new Parser(new ByteArrayInputStream(
                ("(resume (spawn (suspend) (println \"test\")))\n").getBytes()));
        List<SExpression> sexps = null;
        sexps = parser.parse();
        Process p = new Process(sexps);
        p.run();
        Thread.sleep(1000);
        Assert.assertEquals(new String(out.toByteArray()), "test" + System.lineSeparator());
    }
}
