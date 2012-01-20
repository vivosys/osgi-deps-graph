package com.vivosys.osgi.deps.builder.graph;

import java.util.*;

public class Graph {

    private Vertex parent;
    private final Map<Object, Vertex> vertices = new HashMap<Object, Vertex>();
    private Set<Edge> edges = new HashSet<Edge>();

    public Graph() {
        this.parent = null;
    }

    public Graph(Vertex parent) {
        this.parent = parent;
        parent.setGraph(this);
    }

    public Vertex getParent() {
        return parent;
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Vertex findOrCreate(Object vertexId) {
        Vertex vertex = vertices.get(vertexId);
        if (vertex == null) {
            vertex = new Vertex(vertexId);
            vertices.put(vertexId, vertex);
        }
        return vertex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Graph graph = (Graph) o;

        if (parent != null ? !parent.equals(graph.parent) : graph.parent != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return parent != null ? parent.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Vertices: ").append(vertices.values()).append("\n");
        buffer.append("Edges: ").append(edges);
        return buffer.toString();
    }
}
