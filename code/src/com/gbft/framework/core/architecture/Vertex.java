package com.gbft.framework.core.architecture;

import java.util.HashSet;
import java.util.Set;

public class Vertex {
    public boolean valid;
    public int index;
    public int lowlink;
    public Set<Integer> outEdges;
    public Set<Integer> inEdges;
    public Set<Integer> subgraphEdges;

    public Vertex() {
        this.valid = true;
        this.index = -1;
        this.lowlink = -1;
        this.outEdges = new HashSet<>();
        this.inEdges = new HashSet<>();
        this.subgraphEdges = new HashSet<>();
    }
}
