package org.regis.jlisp;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Interpreter {

    private HashMap<String, Object> symbolTable = new HashMap<>();

    private ThreadLocal<Process> current = new ThreadLocal<>();

    public Interpreter() {
        register("+", args -> (Integer) args.get(0) + (Integer) args.get(1));
        register("-", args -> (Integer) args.get(0) - (Integer) args.get(1));
        register("*", args -> (Integer) args.get(0) * (Integer) args.get(1));
        register("/", args -> (Integer) args.get(0) / (Integer) args.get(1));
        register("println", args -> {
            System.out.println(args.get(0));
            return null;
        });
        Process p = new Process();
        current.set(p);
    }

    public SExpression defun(SExpression sexp) {
        SExpression fun = new SExpression(sexp.list.removeFirst());
        symbolTable.put((String) fun.list.getFirst(), fun);
        return fun;
    }

    public void register(String name, Function<List<Object>, Object> f) {
        SExpression sfun = new SExpression(new Symbol("apply"), f);
        symbolTable.put(name, new SExpression(name, new SExpression("..."), sfun));
    }

    public void evalSExpression(SExpression expression) {
        Frame f = new Frame((SExpression) resolveName(((Symbol) expression.list.get(0)).name), expression.list.subList(
                1, expression.list.size()));
        current.get().stack.addFirst(f);
    }

    private Object resolveName(String name) {
        Object v = findValue(name);
        while (v instanceof Symbol) {
            Symbol s = (Symbol) v;
            v = findValue(s.name);
        }
        return v;
    }

    private Object findValue(String name) {
        Object v = null;
        if (current.get().stack.size() > 0) {
            for (Variable var : current.get().stack.getFirst().variables) {
                if (var.name.equals(name)) {
                    v = var.value;
                }
            }
        }
        if (v == null) {
            v = symbolTable.get(name);
        }
        return v;
    }

    private Object eval(SExpression obj) {
        switch (((Symbol) obj.list.getFirst()).name) {
        case "apply":
            return ((Function<List<Object>, Object>) obj.list.get(1)).<List<Variable>> compose(
                    t -> t.stream().map(v -> v.value).collect(Collectors.toList())).apply(
                    current.get().stack.getFirst().variables);
        case "bind":
            Frame f = current.get().stack.getFirst();
            String name = (String) obj.list.get(1);
            Object value = current.get().value;
            f.variables.add(new Variable(name, value));
            return value;
        default:
            evalSExpression(obj);
        }
        return null;
    }

    public void run() {
        Parser parser = new Parser(System.in);
        List<SExpression> sexps = null;
        try {
            sexps = parser.parse();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        for (SExpression sexp : sexps) {
            current.get().value = eval(sexp);
            while ((sexp = next()) != null) {
                Object value = eval(sexp);
                current.get().value = value;
            }
        }
    }

    private SExpression next() {
        return current.get().next();
    }

    public static void main(String[] args) {
        Interpreter interpreter = new Interpreter();
        interpreter.run();
        System.out.println(interpreter.current.get().value);
    }
}
