package com.vettiankal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Profiler {

    private ScheduledExecutorService scheduler;
    private long pollDelay;
    private long duration;
    private HashMap<ThreadInfo, ExecutionTree> trees;
    private int polls;

    private ScheduledFuture task;
    private long start;
    private ProfileCompleteEvent onComplete;

    public Profiler() {
        //TODO adjust this value based on how long it takes to execute profiler
        this(10);
    }

    public Profiler(long pollDelay) {
        this(pollDelay, -1);
    }

    public Profiler(long pollDelay, long duration) {
        this.pollDelay = pollDelay;
        this.duration = duration;
        this.scheduler = Executors.newScheduledThreadPool(3);
        this.trees = new HashMap<>();
        this.polls = 0;
    }

    public long getPollingDelay() {
        return pollDelay;
    }

    public long getDuration() {
        return duration;
    }

    private void profile() {
        try {
            if (duration > 0 && System.currentTimeMillis() - start > duration) {
                task.cancel(false);
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
            e.printStackTrace();
        }
    }

    public void start() {
        start(null);
    }

    public void start(ProfileCompleteEvent onComplete) throws ProfilerException {
        if(this.task != null) throw new ProfilerException("Attempted to start already started profiler");

        this.task = scheduler.scheduleAtFixedRate(this::profile, 0, pollDelay, TimeUnit.MILLISECONDS);
        this.start = System.currentTimeMillis();
        this.onComplete = onComplete;
    }

    public ExecutionTree stop() throws ProfilerException {
        if(task == null) throw new ProfilerException("Attempted to stop non-started profiler");
        if(task.isCancelled()) throw new ProfilerException("Attempted to call stop more than once");

        task.cancel(true);
        scheduler.shutdown();
        return null;
    }

}
