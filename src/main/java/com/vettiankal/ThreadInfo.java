package com.vettiankal;

public class ThreadInfo {

    private String name;
    private long id;

    public ThreadInfo(Thread thread) {
        this(thread.getName(), thread.getId());
    }

    public ThreadInfo(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        return other.getClass() == getClass() && ((ThreadInfo)other).id == id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return name + " - " + id;
    }

}
