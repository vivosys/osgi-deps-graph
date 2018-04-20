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

/**
 *  Create dependency graph for all the bundles with start level specified by 
 *  the system property "com.vivosys.min.startlevel.depends.graph".
 *  
 *  The default level is 1.
 */
public class OsgiDepsActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(OsgiDepsActivator.class);

    public static final String MIN_START_LEVEL_PROPERTY = "com.vivosys.min.startlevel.depends.graph";
    private static final int DEFAULT_MIN_START_LEVEL = 1;

    public void start(BundleContext bundleContext) throws Exception {
        Graph graph;
        try {
            OsgiGraphBuilder builder = new OsgiGraphBuilder();
            builder.addIgnoreBundlesMatching("org.apache.servicemix.bundles.cglib");
            builder.addIgnoreServicesMatching("org.springframework.osgi.*");
            // to make names shorter, given prefix text can be stripped from bundle names
            builder.addStripFromBundleName("com.vivosys.");
            // to make names shorter, given prefix text can be stripped from service names
            builder.addStripFromServiceName("org.springframework.data.mongodb.");
            builder.addStripFromServiceName("org.springframework.integration.");
            
            int minStartLevel =  loadMinStartLevel();
            graph = builder.buildGraph(bundleContext, minStartLevel);
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

    private static int loadMinStartLevel() {
        String minStartLevel = System.getProperty(MIN_START_LEVEL_PROPERTY);
        Integer retVal = DEFAULT_MIN_START_LEVEL;
        
        logger.info("Loading property {} to specify minimal relevant start level for OSGi deps graph", MIN_START_LEVEL_PROPERTY);
        
        if (minStartLevel != null) {
            try {
                retVal = Integer.parseInt(minStartLevel);
            } catch (NumberFormatException  e) {
                logger.error("Property {} not in numeric format: {}", MIN_START_LEVEL_PROPERTY, minStartLevel);
            }
        }
        logger.info("Building deps graph for start levels {} or above", retVal);
        return retVal;      
    }
}
