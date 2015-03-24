package org.regis.jlisp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Context {

    private Map<String, Object> envs = new HashMap<>();

    private LinkedList<HashMap<String, Object>> frames = new LinkedList<>();

    public Context(Map<String, Object> envs) {
        // TODO: switch to CopyOnWrite Map
        this.envs = new HashMap<>(envs);
    }

    public Object value(String name) {
        HashMap<String, Object> local = frames.peekFirst();
        if (local != null && local.containsKey(name)) {
            return frames.getFirst().get(name);
        }
        if (envs.containsKey(name)) {
            return envs.get(name);
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

    public void addEnv(String name, Object value) {
        envs.put(name, value);
    }
    
    public void addLocal(String name, Object value) {
        if (frames.peekFirst() == null) {
            envs.put(name, value);
        } else {
            frames.getFirst().put(name, value);
        }
    }

    public Map<String, Object> getEnvs() {
        return envs;
    }
}
