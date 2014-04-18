

import java.util.regex.Pattern;

public class Tokenizer {

    private String str;
    private int current;
    private Pattern pattern = Pattern.compile("-?[1-9][0-9]*");

    public Tokenizer(String str) {
        this.str = str;
        current = 0;
    }

    public boolean hasNext() {
        return current < str.length();
    }

    public String readString() {
        int index = str.indexOf('"', current + 1);
        String value = str.substring(current + 1, index);
        current = index + 1;
        return value;
    }

    public char nextChar() {
        return str.charAt(current);
    }

    public char readLeftParenthesis() {
        return str.charAt(current++);
    }

    public char readRightParenthesis() {
        return str.charAt(current++);
    }

    public char readSpace() {
        return str.charAt(current++);
    }

    public Object readToken() {
        int i1 = str.indexOf(" ", current);
        int i2 = str.indexOf(")", current);
        int index = i1 < i2 ? i1 : i2;
        if (i1 == -1) {
            index = i2;
        } else if (i2 == -1) {
            index = i1;
        }
        String value = str.substring(current, index);
        current = index;
        if (pattern.matcher(value).matches()) {
            return Integer.parseInt(value);
        } else {
            Symbol s = new Symbol();
            s.name = value;
            return s;
        }
    }

}
