import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Interpreter {

    private HashMap<String, Object> symbolTable = new HashMap<>();

    private ThreadLocal<Process> current = new ThreadLocal<>();

    public Interpreter() {
        register("+", new Function<List<Object>, Object>() {

            @Override
            public Object apply(List<Object> t) {
                return (Integer) t.get(0) + (Integer) t.get(1);
            }
        });
        Process p = new Process();
        current.set(p);
    }
  
    public void register(String name, Function<List<Object>, Object> f) {
        SExpression sum = new SExpression(new Symbol("apply"), f);
        symbolTable.put(name, new SExpression(name, new SExpression("a", "b"), sum));
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
            v = current.get().stack.getFirst().variables.get(name);
        }
        if (v == null) {
            v = symbolTable.get(name);
        }
        return v;
    }

    private Object eval(SExpression obj) {
        switch (((Symbol) obj.list.getFirst()).name) {
        case "apply":
            return ((Function) obj.list.get(1)).compose(new Function<Map<String, Object>, List<Object>>() {

                @Override
                public List<Object> apply(Map<String, Object> t) {
                    return t.values().stream().map(v -> (Integer) v).collect(Collectors.toList());
                }
            }).apply(current.get().stack.getFirst().variables);
        case "bind":
            Frame f = current.get().stack.getFirst();
            String name = (String) obj.list.get(1);
            Object value = current.get().value;
            f.variables.put(name, value);
            return value;
        default:
            evalSExpression(obj);
        }
        return null;
    }

    public void run() {
        Parser parser = new Parser();
        SExpression expr = parser.parse("(+ (+ 2 3) (+ 5 (+ 2 2)))").getFirst();
        current.get().value = eval(expr);
        while ((expr = next()) != null) {
            Object value = eval(expr);
            current.get().value = value;
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
