package com.vivosys.osgi.deps.builder;

import com.vivosys.osgi.deps.builder.graph.Vertex;
import org.osgi.framework.Bundle;

public class BundleVertex {

    private Vertex vertex;
    private Bundle bundle;

    public BundleVertex(Vertex vertex, Bundle bundle) {
        this.vertex = vertex;
        this.bundle = bundle;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public Bundle getBundle() {
        return bundle;
    }
}
