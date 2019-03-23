package com.vettiankal;

import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Profiler extends TimerTask implements NotificationListener {

    private long pollInterval;
    private long duration;
    private HashMap<ThreadInfo, ExecutionTree> trees;
    private Thread[] threads;
    private int polls;

    private Timer scheduler;
    private long start;
    private ProfileCompleteEvent onComplete;
    private Thread.UncaughtExceptionHandler exceptionHandler = (t, e) -> e.printStackTrace();

    public Profiler(Thread... threads) {
        this(10, threads);
    }

    public Profiler(long pollInterval, Thread... threads) {
        this(pollInterval, -1, threads);
    }

    public Profiler(long pollInterval, long duration, Thread... threads) {
        this.pollInterval = pollInterval;
        this.duration = duration;
        this.trees = new HashMap<>();
        this.polls = 0;
        this.threads = threads;
    }

    public long getPollingDelay() {
        return pollInterval;
    }

    public long getDuration() {
        return duration;
    }

    public long getTotalPolls() {
        return polls;
    }

    public Thread.UncaughtExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }


    @Override
    public void handleNotification(Notification notification, Object handback) {
        if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
            GarbageCollectionNotificationInfo gcInfo = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
            this.duration += gcInfo.getGcInfo().getDuration();
        }
    }

    @Override
    public void run() {
        try {
            if (duration > 0 && System.currentTimeMillis() - start > duration) {
                stop();
                if (onComplete != null) onComplete.onComplete(trees);
                return;
            }

            Map<Thread, StackTraceElement[]> stackTraces;
            if(threads.length > 0) {
                stackTraces = new HashMap<>();
                for(Thread thread : threads) {
                    stackTraces.put(thread, thread.getStackTrace());
                }
            } else {
                stackTraces = Thread.getAllStackTraces();
            }

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
            if(exceptionHandler != null) {
                exceptionHandler.uncaughtException(Thread.currentThread(), e);
            } else {
                e.printStackTrace();
                System.err.println("An error occurred while profiling. Use Profiler::setExceptionHandler to set the exception handler of the profiling thread.");
            }
        }
    }

    public void start() {
        start(null);
    }

    public void start(ProfileCompleteEvent onComplete) throws ProfilerException {
        if(this.scheduler != null) throw new ProfilerException("Attempted to start already started profiler");

        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (!(gcBean instanceof NotificationEmitter)) {
                continue;
            }

            NotificationEmitter emitter = (NotificationEmitter) gcBean;
            emitter.addNotificationListener(this,null,null);
        }

        this.scheduler = new Timer("Profiler", true);
        this.scheduler.scheduleAtFixedRate(this, 0, pollInterval);
        this.start = System.currentTimeMillis();
        this.onComplete = onComplete;
    }

    public HashMap<ThreadInfo, ExecutionTree> stop() throws ProfilerException {
        if(scheduler == null) throw new ProfilerException("Attempted to stop non-started profiler");
        scheduler.cancel();

        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (!(gcBean instanceof NotificationEmitter)) {
                continue;
            }

            NotificationEmitter emitter = (NotificationEmitter) gcBean;
            try {
                emitter.removeNotificationListener(this);
            } catch (ListenerNotFoundException ignored) {

            }
        }

        return trees;
    }

}
