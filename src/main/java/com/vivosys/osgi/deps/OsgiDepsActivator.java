package com.vivosys.osgi.deps;

import com.vivosys.osgi.deps.builder.OsgiGraphBuilder;
import com.vivosys.osgi.deps.builder.graph.Graph;
import com.vivosys.osgi.deps.builder.graphml.GraphMLGenerator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.Writer;

public class OsgiDepsActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(OsgiDepsActivator.class);

    public void start(BundleContext bundleContext) throws Exception {
        Graph graph;
        try {
            OsgiGraphBuilder builder = new OsgiGraphBuilder();
            builder.addIgnoreBundlesMatching("org.apache.servicemix.bundles.cglib");
            builder.addIgnoreBundlesMatching("com.citi.edelng.edel-common");
            builder.addIgnoreBundlesMatching("com.citi.edelng.edel-bl-.*");
            builder.addIgnoreServicesMatching("org.springframework.osgi.*");
            builder.addStripFromBundleName("com.citi.edelng.");
            builder.addStripFromServiceName("com.citi.edelng.engine.procman.api.module.");
            builder.addStripFromServiceName("com.citi.edelng.");
            builder.addStripFromServiceName("org.springframework.data.mongodb.");
            builder.addStripFromServiceName("org.springframework.integration.");
            graph = builder.buildGraph(bundleContext, 50);
        } catch (Exception e) {
            logger.error("Error building graph.", e);
            throw e;
        }

        GraphMLGenerator gen = new GraphMLGenerator();
        //Writer out = new PrintWriter(System.out);
        Writer out = new FileWriter("osgi-deps.graphml");
        try {
            gen.serialize(graph, out);
            System.out.println("Wrote relationship graph to osgi-deps.graphml.");
        } catch (Exception e) {
            logger.error("Error generating relationship graph.", e);
            throw e;
        } finally {
            out.close();
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }

}
