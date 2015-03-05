package org.regis.jlisp;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class JInvoker {

    private List<MethodHandle> mhs = new LinkedList<>();

    public JInvoker(Class<?> cls, String name) throws NoSuchMethodException, IllegalAccessException {
        Method[] mds = cls.getDeclaredMethods();
        for (Method method : mds) {
            if (method.getName().equals(name)) {
                mhs.add(toMethodHandle(method));
            }
        }
        if (mhs.size() == 0) {
            throw new RuntimeException("Can't find method " + name + " in class " + cls.getName());
        }
    }

    private MethodHandle toMethodHandle(Method method) throws NoSuchMethodException, IllegalAccessException {
        if (Modifier.isStatic(method.getModifiers())) {
            return MethodHandles.lookup().findStatic(method.getDeclaringClass(), method.getName(),
                    MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
        } else {
            return MethodHandles.lookup().findVirtual(method.getDeclaringClass(), method.getName(),
                    MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
        }
    }

    public Object invoke(Object... args) throws Throwable {
        Class[] argTypes = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            argTypes[i] = args[i].getClass();
        }
        for (MethodHandle mh : mhs) {
            try {
                MethodType mt = MethodType.methodType(Object.class, argTypes);
                MethodHandle m = mh.asType(mt);
                return m.invokeWithArguments(args);
            } catch (WrongMethodTypeException e) {
                continue;
            }
        }

        throw new WrongMethodTypeException("Wrong arguments");
    }
}
