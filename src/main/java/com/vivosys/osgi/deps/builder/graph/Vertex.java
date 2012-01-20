package com.vivosys.osgi.deps.builder.graph;

public class Vertex {

    private final Object vertexId;
    private String label;
    private Graph graph;
    private boolean container;
    private boolean supporting;

    Vertex(Object vertexId) {
        this.vertexId = vertexId;
    }

    public Object getVertexId() {
        return vertexId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public boolean isContainer() {
        return container;
    }

    public void setContainer(boolean container) {
        this.container = container;
    }

    public boolean isSupporting() {
        return supporting;
    }

    public void setSupporting(boolean supporting) {
        this.supporting = supporting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vertex vertex = (Vertex) o;

        if (graph != null ? !graph.equals(vertex.graph) : vertex.graph != null) return false;
        if (vertexId != null ? !vertexId.equals(vertex.vertexId) : vertex.vertexId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = vertexId != null ? vertexId.hashCode() : 0;
        result = 31 * result + (graph != null ? graph.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        toString(buffer, 0);
        return buffer.toString();
    }

    public void toString(StringBuilder buffer, int indent) {
        addIndent(indent, buffer);
        buffer.append(vertexId.toString()).append("\n");
        if(graph != null) buffer.append("Subgraph:\n").append(graph.toString()).append("\n");
    }

    private void addIndent(int indent, StringBuilder res) {
        for (int i =0;i < indent;i++) {
            res.append("\t");
        }
    }
}
