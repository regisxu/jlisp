package org.regis.jlisp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Process {

    private Object value;
    private LinkedList<Object> codeStack = new LinkedList<>();
    private LinkedList<Object> varStack = new LinkedList<>();

    private Context context;

    private long id;

    private static AtomicLong ider = new AtomicLong(0);

    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    private static Map<Long, Process> processes = new HashMap<>();

    public Process(List<SExpression> sexps, Map<String, Object> envs) {
        context = new Context(envs);
        Function<List<Object>, Object> f = args -> {
            context.popFrame();
            return varStack.pollFirst();
        };
        context.addEnv("#pop", f);
        codeStack.addAll(sexps);
        id = ider.getAndIncrement();
        processes.put(id, this);
    }

    public Process(List<SExpression> sexps) {
        this(sexps, Builtin.builtin);
    }

    public Object run() {
        eval();
        return value;
    }

    public void execute() {
        executor.execute(new Runnable() {
            public void run() {
                eval();
            }
        });
    }

    public static Process findProcess(long id) {
        return processes.get(id);
    }

    private void eval() {
        Object o = null;
        while ((o = codeStack.pollFirst()) != null) {
            if (o instanceof SExpression) {
                SExpression exp = (SExpression) o;
                if (((Symbol) exp.list.getFirst()).name.equals("defun")) {
                    defun(exp);
                } else if (((Symbol) exp.list.getFirst()).name.equals("spawn")) {
                    spawn(exp);
                } else if (((Symbol) exp.list.getFirst()).name.equals("suspend")) {
                    break;
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

    private void spawn(SExpression exp) {
        List<SExpression> code = exp.list.subList(1, exp.list.size()).stream().map(entry -> (SExpression) entry)
                .collect(Collectors.toList());
        Process p = new Process(code, context.getEnvs());
        p.execute();
        value = p.id;
        varStack.addFirst(p.id);
    }

    private void defun(SExpression exp) {
        if (exp.list.get(2) instanceof String) {
            defunJava(exp);
        } else {
            SExpression f = new SExpression();
            f.list = new LinkedList<Object>(exp.list.subList(1, exp.list.size()));
            f.list.add(new SExpression(new Symbol("#pop")));
            context.addLocal(((Symbol) exp.list.get(1)).name, f);
        }
    }

    private void defunJava(SExpression exp) {
        try {
            String signature = (String) exp.list.get(2);
            String name = signature.substring(signature.lastIndexOf(".") + 1, signature.length());
            String clsName = signature.substring(signature.indexOf(":") + 1, signature.lastIndexOf("."));
            Class cls = Thread.currentThread().getContextClassLoader().loadClass(clsName);
            JInvoker invoker = new JInvoker(cls, name);
            context.addLocal(((Symbol) exp.list.get(1)).name, new Function<List<Object>, Object>() {
                @Override
                public Object apply(List<Object> args) {
                    try {
                        return invoker.invoke(args.toArray());
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }

            });
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
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
            List<String> names = ((SExpression) ((SExpression) fun).list.get(1)).list.stream().map(entry -> {
                return ((Symbol) entry).name;
            }).collect(Collectors.toList());
            context.pushFrame(names, paras);
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
        return context.value(name);
    }
}
