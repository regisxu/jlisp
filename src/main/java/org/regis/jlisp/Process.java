package org.regis.jlisp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Process {

    private Object value;
    private LinkedList<Object> codeStack = new LinkedList<>();
    private LinkedList<Object> varStack = new LinkedList<>();

    private Context context;

    private LinkedList<Object> mb = new LinkedList<>();

    private boolean isRunning = true;

    private final long id;

    private static AtomicLong ider = new AtomicLong(0);

    public Process(List<SExpression> sexps, Map<String, Object> envs) {
        context = new Context(envs);
        context.addEnv("#pop", (Function<List<Object>, Object>) (args -> pop()));
        context.addEnv("send", (Function<List<Object>, Object>) (args -> {
            return send(args.get(0), (Long) args.get(1));
        }));
        context.addEnv("receive", (Function<List<Object>, Object>) (args -> receive()));
        codeStack.addAll(sexps);
        id = ider.getAndIncrement();
        Scheduler.register(this);
    }

    public Process(List<SExpression> sexps) {
        this(sexps, Builtin.builtin);
    }

    public Object run() {
        eval();
        Scheduler.unregister(id);
        return value;
    }

    public long getId() {
        return id;
    }

    public boolean send(Object msg, long id) {
        Process p = Scheduler.findProcess(id);
        if (p != null) {
            p.putMsg(msg);
            return true;
        }
        return false;
    }

    public Object receive() {
        synchronized (mb) {
            if (mb.size() > 0) {
                return mb.removeFirst();
            } else {
                isRunning = false;
                codeStack.addFirst(new SExpression(new Symbol("receive")));
                return null;
            }
        }
    }

    public void putMsg(Object msg) {
        synchronized (mb) {
            mb.addLast(msg);
            if (!isRunning) {
                isRunning = true;
                Scheduler.resume(id);
            }
        }
    }

    void eval() {
        Object o = null;
        while (isRunning && (o = codeStack.pollFirst()) != null) {
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

    private Object pop() {
        context.popFrame();
        return varStack.pollFirst();
    }

    private void spawn(SExpression exp) {
        List<SExpression> code = exp.list.subList(1, exp.list.size()).stream().map(entry -> (SExpression) entry)
                .collect(Collectors.toList());
        Process p = new Process(code, context.getEnvs());
        value = p.id;
        varStack.addFirst(p.id);
        Scheduler.execute(p);
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
            Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(clsName);
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
        Object fun = context.value(call.name.name);
        List<Object> paras = new LinkedList<>();
        for (int i = 0; i < call.count; i++) {
            paras.add(value(varStack.removeFirst()));
        }
        if (fun instanceof Function) {
            @SuppressWarnings("unchecked")
            Function<List<Object>, Object> f = (Function<List<Object>, Object>) fun;
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
            return context.value(s.name);
        }
        return obj;
    }
}
