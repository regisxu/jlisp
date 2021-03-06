package org.regis.jlisp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scheduler {

    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    private static Map<Long, Process> processes = new ConcurrentHashMap<>();

    private Scheduler() {
        // prevent initialize instance
    }

    public static void execute(Process p) {
        if (p != null) {
            executor.execute(p::eval);
        }
    }

    public static Long resume(Long id) {
        Process p = processes.get(id);
        if (p != null) {
            executor.execute(p::eval);
            return id;
        } else {
            return null;
        }
    }

    public static void register(Process p) {
        if (p != null) {
            processes.put(p.getId(), p);
        }
    }

    public static void unregister(long id) {
        processes.remove(id);
    }

    public static Process findProcess(long id) {
        return processes.get(id);
    }
}
