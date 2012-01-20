package com.vivosys.osgi.deps.builder.graphml;

import com.vivosys.osgi.deps.builder.graph.Edge;
import com.vivosys.osgi.deps.builder.graph.Graph;
import com.vivosys.osgi.deps.builder.graph.Vertex;

import java.awt.*;
import java.io.*;

public class GraphMLGenerator {

    private final Color COLOR_CONTAINER = new Color(255, 255, 153);
    private final Color COLOR_NODE = Color.orange;

    private class Builder {

        private final static String TMPL_BASE = "com/vivosys/osgi/deps/builder/graphml";
        private final Writer out;

        public Builder(Writer out) {
            this.out = out;
        }

        private String fetch(BufferedReader in) throws IOException {
            StringBuilder sb = new StringBuilder();
            String str;
            try {
                while (true) {
                    str = in.readLine();
                    if (str == null) break;
                    sb.append(str);
                    sb.append("\n");
                }
            } finally {
                in.close();
            }

            str = sb.toString();

            return str;
        }

        private String fetchTemplate(String tmplName, String name) throws IOException {
            return fetchTemplate(tmplName, name, "#FFCC00", null);
        }

        private String fetchTemplate(String tmplName, String name, String color, String shape) throws IOException {
            InputStream is = getClass().getClassLoader().getResourceAsStream(TMPL_BASE + "/" + tmplName);

            String text = fetch(new BufferedReader(new InputStreamReader(is)));
            if (name != null) {
                text = text.replaceAll("@NAME@", name);
            }
            if (color != null) {
                text = text.replaceAll("@COLOR@", color);
            }
            if (shape != null) {
                text = text.replaceAll("@SHAPE@", shape);
            }

            return text;
        }

        private void begin() throws IOException {
            out.write(fetchTemplate("head.tmpl", null));
        }

        private void end() throws IOException {
            out.write("</graphml>\n");
        }

        private void beginGraph(String id) throws IOException {
            out.write("<graph id='" + id + "' edgedefault='undirected'>");
        }

        private void endGraph() throws IOException {
            out.write("</graph>\n");
        }

        private void beginLeafNode(String id, String name, String color, String shape) throws IOException {
            out.write("<node id='" + id + "'>");
            out.write(fetchTemplate("simplenode.tmpl", name, color, shape));
        }

        private void endLeafNode() throws IOException {
            out.write("</node>\n");
        }

        private void edge(String fromId, String toId, String name) throws IOException {
            edge(fromId, toId, name, null);
        }

        private void edge(String fromId, String toId, String name, String color) throws IOException {
            if(color == null) color = getColorString(Color.black);
            out.write("<edge source='" + fromId + "' target='" + toId + "'>");
            out.write(fetchTemplate("edgenode.tmpl", name, color, null));
            out.write("</edge>\n");
        }
    }

    private static String encode(String text) {
        if (text == null) {
            text = "";
        }
        return text.replaceAll("[<]", "&lt;").replaceAll("[>]", "&gt;").replaceAll("[:]", "\n:");
    }

    public void serialize(Graph graph, Writer writer) throws IOException {
        Builder gen = new Builder(writer);
        gen.begin();
        serializeGraph(graph, gen);
        gen.end();
    }

    private void serializeGraph(Graph graph, Builder gen) throws IOException {
        gen.beginGraph("graph" + (graph.getParent() == null ? "root" : graph.getParent().getVertexId()));

        for (Vertex vertex : graph.getVertices()) {
            gen.beginLeafNode(vertex.getVertexId().toString(), encode(vertex.getLabel()),
                getColorString(vertex.isContainer() ? COLOR_CONTAINER : COLOR_NODE),
                vertex.isSupporting() ? "ellipse" : "roundrectangle");
            if(vertex.getGraph() != null) serializeGraph(vertex.getGraph(), gen);
            gen.endLeafNode();
        }
        for (Edge edge : graph.getEdges()) {
            gen.edge(edge.from.getVertexId().toString(), edge.to.getVertexId().toString(), edge.label, getColorString(edge.color));
        }

        gen.endGraph();
    }

    private String getColorString(Color color) {
        return "#" + Integer.toHexString(color.getRGB() & 0x00ffffff);
    }
}
