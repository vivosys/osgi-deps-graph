package com.vivosys.osgi.deps.builder.graph;

import java.awt.*;

public class Edge {

    public final Vertex from;
    public final Vertex to;
    public String label;
    public Color color;

    public Edge(Vertex from, Vertex to, String label) {
        this(from, to, label, Color.black);
    }

    public Edge(Vertex from, Vertex to, String label, Color color) {
        if(from == null || to == null) {
            throw new IllegalArgumentException("From/To cannot be null: " + from + "/" + to);
        }
        this.from = from;
        this.to = to;
        this.label = label;
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (!from.equals(edge.from)) return false;
        if (!to.equals(edge.to)) return false;
        if (!label.equals(edge.label)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + label.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Edge");
        sb.append("{from=").append(from);
        sb.append(", to=").append(to);
        sb.append('}');
        return sb.toString();
    }
}
