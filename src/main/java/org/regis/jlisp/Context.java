package org.regis.jlisp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Context {

    private HashMap<String, Object> global = new HashMap<>();

    private LinkedList<HashMap<String, Object>> frames = new LinkedList<>();

    public Object value(String name) {
        HashMap<String, Object> local = frames.peekFirst();
        if (local != null && local.containsKey(name)) {
            return frames.getFirst().get(name);
        }
        if (global.containsKey(name)) {
            return global.get(name);
        }
        throw new RuntimeException("Can't resolve symbol '" + name + "'");
    }

    public void pushFrame(List<String> names, List<Object> values) {
        HashMap<String, Object> map = new HashMap<>();
        Iterator<String> itName = names.iterator();
        Iterator<Object> itValue = values.iterator();
        while (itName.hasNext()) {
            if (itValue.hasNext()) {
                map.put(itName.next(), itValue.next());
            } else {
                map.put(itName.next(), null);
            }
        }
        frames.addFirst(map);
    }

    public void popFrame() {
        frames.pollFirst();
    }

    public void addGlobal(String name, Object value) {
        global.put(name, value);
    }
    
    public void addLocal(String name, Object value) {
        if (frames.peekFirst() == null) {
            global.put(name, value);
        } else {
            frames.getFirst().put(name, value);
        }
    }
}
