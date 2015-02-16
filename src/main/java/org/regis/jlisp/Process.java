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
        eval();
        return value;
    }

    private void eval() {
        Object o = null;
        while ((o = codeStack.pollFirst()) != null) {
            if (o instanceof SExpression) {
                SExpression exp = (SExpression) o;
                if (((Symbol) exp.list.getFirst()).name.equals("defun")) {
                    SExpression f = new SExpression();
                    f.list = new LinkedList<Object>(exp.list.subList(1, exp.list.size()));
                    symbolTable.put(((Symbol) exp.list.get(1)).name, f);
                } else {
                    codeStack.addFirst(new Call((Symbol) exp.list.getFirst(), exp.list.size() - 1));
                    for (Object obj : exp.list.subList(1, exp.list.size())) {
                        codeStack.addFirst(obj);
                    }
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
        List<Object> paras = new LinkedList<>();
        for (int i = 0; i < call.count; i++) {
            paras.add(value(varStack.removeFirst()));
        }
        if (fun instanceof Function) {
            Function f = (Function) fun;
            value = f.apply(paras);
            varStack.addFirst(value);
        } else {
            Frame f = new Frame((SExpression) fun, paras);
            stack.addFirst(f);
            codeStack.addAll(0, ((SExpression) fun).list.subList(2, ((SExpression) fun).list.size()));
        }
    }

    private Object value(Object obj) {
        if (obj instanceof Symbol) {
            Symbol s = (Symbol) obj;
            return resolveName(s.name);
        }
        return obj;
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
                ("(defun add (a b) (+ a b))\n"
                + "(add (add 2 3) 4)\n").getBytes()));
        List<SExpression> sexps = null;
        try {
            sexps = parser.parse();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Process p = new Process();
        LinkedList<Object> code = new LinkedList<Object>();
        code.addAll(sexps);
//        code.addFirst(new SExpression());
//        code.addFirst(new Symbol("main"));
//        SExpression main = new SExpression();
//        main.list = code;
//        Frame f = new Frame(main, Collections.emptyList());
//        p.stack.add(f);
        p.codeStack = code;
        System.out.println(p.run());
    }
}
