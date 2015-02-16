package org.regis.jlisp;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class Process {

    LinkedList<Frame> stack = new LinkedList<>();
    Object value;
    private LinkedList<Object> codeStack = new LinkedList<>();
    private LinkedList<Object> varStack = new LinkedList<>();

    private HashMap<String, Object> symbolTable = new HashMap<>();

    public Process() {
        register("+", args -> (Integer) args.get(0) + (Integer) args.get(1));
        register("-", args -> (Integer) args.get(0) - (Integer) args.get(1));
        register("*", args -> (Integer) args.get(0) * (Integer) args.get(1));
        register("/", args -> (Integer) args.get(0) / (Integer) args.get(1));
        register("println", args -> {
            System.out.println(args.get(0));
            return args.get(0);
        });
    }

    public void register(String name, Function<List<Object>, Object> f) {
        SExpression sfun = new SExpression(new Symbol("apply"), f);
        symbolTable.put(name, f);
    }

    public Process(HashMap<String, Object> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public SExpression next() {
        while (stack.size() > 0) {
            Frame f = stack.getFirst();
            SExpression expr = f.next();
            if (expr == null) {
                stack.removeFirst();
            } else {
                return expr;
            }
        }
        return null;
    }

    public Object run() {
        SExpression sexp = null;
        while ((sexp = next()) != null) {
            eval(sexp);
        }
        return value;
    }

    private void eval(SExpression sexp) {
        codeStack.addFirst(sexp);
        Object o = null;
        while ((o = codeStack.pollFirst()) != null) {
            if (o instanceof SExpression) {
                SExpression exp = (SExpression) o;
                codeStack.addFirst(new Call((Symbol) exp.list.getFirst(), exp.list.size() - 1));
                for (Object obj : exp.list.subList(1, exp.list.size())) {
                    codeStack.addFirst(obj);
                }
            } else if (o instanceof Call) {
                Call call = (Call) o;
                invoke(call);
            } else {
                varStack.addFirst(o);
            }
        }
    }

    private void invoke(Call call) {
        Object fun = resolveName(call.name.name);
        if (fun instanceof Function) {
            Function f = (Function) fun;
            List<Object> paras = new LinkedList<>();
            for (int i = 0; i < call.count; i++) {
                paras.add(varStack.removeFirst());
            }
            value = f.apply(paras);
            varStack.addFirst(value);
        }
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
        if (stack.size() > 0) {
            for (Variable var : stack.getFirst().variables) {
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

    public static void main(String[] args) {
        Parser parser = new Parser(new ByteArrayInputStream(
                ("(println (println \"test\"))\n"
                + "(* 2 3)\n").getBytes()));
        List<SExpression> sexps = null;
        try {
            sexps = parser.parse();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Process p = new Process();
        LinkedList<Object> code = new LinkedList<Object>();
        code.addAll(sexps);
        code.addFirst(new SExpression());
        code.addFirst("main");
        SExpression main = new SExpression();
        main.list = code;
        Frame f = new Frame(main, Collections.emptyList());
        p.stack.add(f);
        System.out.println(p.run());
    }
}
