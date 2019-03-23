package com.vettiankal;

public class ExecutionTree {

    private ExecutionTreeNode root;

    public ExecutionTree() {
        root = new ExecutionTreeNode("root");
    }

    public void add(StackTraceElement[] stackTrace) {
        if(stackTrace.length == 0) return;

        ExecutionTreeNode node = root.getChild(stackTrace[stackTrace.length - 1]);
        node.visit();
        for(int i = stackTrace.length - 2; i >= 0; i--) {
            node = node.getChild(stackTrace[i]);
            node.visit();
        }
    }

    private void nodeString(ExecutionTreeNode node, StringBuilder builder, String indent) {
        builder.append(indent);
        builder.append(node);
        builder.append("\n");
        for(ExecutionTreeNode child : node.getChildren()) {
            nodeString(child, builder, indent + " ");
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        nodeString(root, builder, "");
        return builder.toString();
    }

}
