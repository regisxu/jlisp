package org.regis.jlisp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Builtin {

    public static final Map<String, Object> builtin = new HashMap<>();

    static {
        init();
    }

    public static void init() {
        register("+", args -> (Integer) args.get(0) + (Integer) args.get(1));
        register("-", args -> (Integer) args.get(0) - (Integer) args.get(1));
        register("*", args -> (Integer) args.get(0) * (Integer) args.get(1));
        register("/", args -> (Integer) args.get(0) / (Integer) args.get(1));
        register("println", args -> {
            System.out.println(args.get(0));
            return args.get(0);
        });
        register("invoke", args -> {
            String signature = (String) args.get(0);
            String name = signature.substring(signature.lastIndexOf(".") + 1, signature.length());
            String clsName = signature.substring(signature.indexOf(":") + 1, signature.lastIndexOf("."));

            try {
                Class cls = Thread.currentThread().getContextClassLoader().loadClass(clsName);
                JInvoker invoker = new JInvoker(cls, name);
                return invoker.invoke(args.subList(1, args.size()).toArray());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        register("resume", args -> {
            Long id = (Long) args.get(0);
            Process p = Process.findProcess(id);
            if (p != null) {
                p.execute();
                return id;
            } else {
                return null;
            }
        });
    }

    public static void register(String name, Function<List<Object>, Object> f) {
        builtin.put(name, f);
    }
}
