package com.vettiankal;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Profiler extends TimerTask {

    private long pollInterval;
    private long duration;
    private HashMap<ThreadInfo, ExecutionTree> trees;
    private int polls;

    private Timer scheduler;
    private long start;
    private ProfileCompleteEvent onComplete;

    // TODO allow threads to monitor to be specified
    public Profiler() {
        this(10);
    }

    public Profiler(long pollInterval) {
        this(pollInterval, -1);
    }

    public Profiler(long pollInterval, long duration) {
        this.pollInterval = pollInterval;
        this.duration = duration;
        this.trees = new HashMap<>();
        this.polls = 0;
    }

    public long getPollingDelay() {
        return pollInterval;
    }

    public long getDuration() {
        return duration;
    }

    public void run() {
        try {
            if (duration > 0 && System.currentTimeMillis() - start > duration) {
                scheduler.cancel();
                if (onComplete != null) onComplete.onComplete(trees);
                return;
            }

            Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
            for (Map.Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet()) {
                ThreadInfo threadInfo = new ThreadInfo(entry.getKey());
                if (trees.get(threadInfo) == null) {
                    trees.put(threadInfo, new ExecutionTree());
                }

                ExecutionTree threadTree = trees.get(threadInfo);
                threadTree.add(entry.getValue());
            }

            this.polls++;
        } catch (Throwable e) {
            // TODO set exception handler
            e.printStackTrace();
        }
    }

    public void start() {
        start(null);
    }

    public void start(ProfileCompleteEvent onComplete) throws ProfilerException {
        if(this.scheduler != null) throw new ProfilerException("Attempted to start already started profiler");

        this.scheduler = new Timer();
        this.scheduler.scheduleAtFixedRate(this, 0, pollInterval);
        this.start = System.currentTimeMillis();
        this.onComplete = onComplete;
    }

    public HashMap<ThreadInfo, ExecutionTree> stop() throws ProfilerException {
        if(scheduler == null) throw new ProfilerException("Attempted to stop non-started profiler");

        scheduler.cancel();
        return trees;
    }

}
