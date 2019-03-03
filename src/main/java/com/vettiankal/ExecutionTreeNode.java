package com.vettiankal;

import java.util.Collection;
import java.util.HashMap;

public class ExecutionTreeNode {

    private String stackTrace;
    private int visits;
    private HashMap<String, ExecutionTreeNode> children;

    public ExecutionTreeNode(StackTraceElement element) {
        this(element.getClassName() + "::" + element.getMethodName());
    }

    protected ExecutionTreeNode(String element) {
        this.stackTrace = element;
        this.children = new HashMap<>();
    }

    public int getVisits() {
        return visits;
    }

    public void visit() {
        this.visits++;
    }

    public ExecutionTreeNode getChild(StackTraceElement trace) {
        ExecutionTreeNode node = new ExecutionTreeNode(trace);
        ExecutionTreeNode prev = children.putIfAbsent(node.toString(), node);
        return prev != null ? prev : node;
    }

    public ExecutionTreeNode[] getChildren() {
        Collection<ExecutionTreeNode> values = children.values();
        return values.toArray(new ExecutionTreeNode[values.size()]);
    }

    @Override
    public int hashCode() {
        return stackTrace.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other.toString().equals(stackTrace);
    }

    @Override
    public String toString() {
        return stackTrace;
    }

}
