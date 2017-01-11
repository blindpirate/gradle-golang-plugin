package com.github.blindpirate.gogradle.core.dependency.tree;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.util.ArrayList;
import java.util.List;

// TODO add root field
public class DependencyTreeNode {
    private String name;
    private boolean star;
    private ResolvedDependency originalDependency;
    private ResolvedDependency finalDependency;
    private List<DependencyTreeNode> children = new ArrayList<>();

    private DependencyTreeNode(ResolvedDependency originalDependency,
                               ResolvedDependency finalDependency,
                               boolean star) {
        this.originalDependency = originalDependency;
        this.finalDependency = finalDependency;
        this.name = originalDependency.getName();
        this.star = star;
    }

    public static DependencyTreeNode withOrignalAndFinal(ResolvedDependency original,
                                                         ResolvedDependency finalResult,
                                                         boolean star) {
        return new DependencyTreeNode(original, finalResult, star);
    }


    public DependencyTreeNode addChild(DependencyTreeNode child) {
        children.add(child);
        return this;
    }

    public String output() {
        return print("", true, true);
    }


    private String print(String prefix, boolean isTail, boolean isRoot) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix)
                .append(branch(isRoot, isTail))
                .append(format(isRoot))
                .append("\n");

        String prefixOfChildren = prefix + padding(isRoot, isTail);

        for (int i = 0; i < children.size() - 1; i++) {
            sb.append(children.get(i).print(prefixOfChildren, false, false));
        }
        if (children.size() > 0) {
            sb.append(children.get(children.size() - 1)
                    .print(prefixOfChildren, true, false));
        }
        return sb.toString();
    }

    private String padding(boolean isRoot, boolean isTail) {
        if (isRoot) {
            return "";
        }
        return isTail ? "    " : "│   ";
    }

    private String branch(boolean isRoot, boolean isTail) {
        if (isRoot) {
            return "";
        }
        return isTail ? "└── " : "├── ";
    }

    private String format(boolean isRoot) {
        if (isRoot) {
            return name;
        } else if (originalDependency == finalDependency) {
            return withCheckMark() + star();
        } else {
            return withArrow() + star();
        }
    }

    private String star() {
        return star ? " (*)" : "";
    }

    private String withArrow() {
        return finalDependency.getName() + ":"
                + originalDependency.formatVersion() + " -> " + finalDependency.formatVersion();
    }

    private String withCheckMark() {
        return finalDependency.getName() + ":" + finalDependency.formatVersion() + " √";
    }

    public GolangDependencySet flatten() {
        GolangDependencySet result = new GolangDependencySet();
        dfs(result);
        return result;
    }

    // TODO max depth
    private void dfs(GolangDependencySet result) {
        for (DependencyTreeNode child : children) {
            result.add(child.finalDependency);
            child.dfs(result);
        }
    }
}
