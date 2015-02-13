import java.util.List;

public class Functions {

    public static int sum(List<Integer> args) {
        return args.stream().reduce(0, Integer::sum);
    }
}
