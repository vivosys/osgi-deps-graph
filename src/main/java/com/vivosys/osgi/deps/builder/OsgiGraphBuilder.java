package com.vivosys.osgi.deps.builder;

import com.vivosys.osgi.deps.builder.graph.Edge;
import com.vivosys.osgi.deps.builder.graph.Graph;
import com.vivosys.osgi.deps.builder.graph.Vertex;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class OsgiGraphBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OsgiGraphBuilder.class);

    private static final Color COLOR_WEAK_EDGE = Color.lightGray;

    private static final String SERVICE_PACKAGE_ADMIN = "org.osgi.service.packageadmin.PackageAdmin";
    private static final String SERVICE_STARTLEVEL = "org.osgi.service.startlevel.StartLevel";
    private static final String PREFIX_VERTEX_BUNDLE_CONTAINER = "container_";
    private static final String PREFIX_VERTEX_BUNDLE = "bundle_";
    private static final String PREFIX_SERVICEREF = "serviceref_";

    private List<Pattern> ignoreServicesMatching = new ArrayList<Pattern>();
    private List<Pattern> acceptServicesMatching = new ArrayList<Pattern>();
    private List<Pattern> ignoreBundlesMatching = new ArrayList<Pattern>();
    private List<Pattern> acceptBundlesMatching = new ArrayList<Pattern>();
    private List<String> stripFromServiceName = new ArrayList<String>();
    private List<String> stripFromBundleName = new ArrayList<String>();

    public OsgiGraphBuilder() {
        // ignore self always
        addIgnoreBundlesMatching("com.vivosys.osgi-deps");
    }

    public void addIgnoreServicesMatching(String ignoreServicesMatching) {
        this.ignoreServicesMatching.add(Pattern.compile(ignoreServicesMatching));
    }

    public void addAcceptServicesMatching(String acceptServicesMatching) {
        this.acceptServicesMatching.add(Pattern.compile(acceptServicesMatching));
    }

    public void addIgnoreBundlesMatching(String ignoreBundlesMatching) {
        this.ignoreBundlesMatching.add(Pattern.compile(ignoreBundlesMatching));
    }

    public void addAcceptBundlesMatching(String acceptBundlesMatching) {
        this.acceptBundlesMatching.add(Pattern.compile(acceptBundlesMatching));
    }

    public void addStripFromServiceName(String stripFromServiceName) {
        this.stripFromServiceName.add(stripFromServiceName);
    }

    public void addStripFromBundleName(String stripFromBundleName) {
        this.stripFromBundleName.add(stripFromBundleName);
    }

    private String getName(Bundle b){
        return b.getSymbolicName() == null ? String.valueOf(b.getBundleId()) : b.getSymbolicName();
    }

    public Graph buildGraph(BundleContext bundleContext, int minStartLevel) {
        final ServiceReference packageAdminRef = bundleContext.getServiceReference(SERVICE_PACKAGE_ADMIN);
        final ServiceReference startLevelRef = bundleContext.getServiceReference(SERVICE_STARTLEVEL);
        final PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(packageAdminRef);
        final StartLevel startLevel = (StartLevel) bundleContext.getService(startLevelRef);

        Graph graph = new Graph();
        Map<Bundle, Vertex> bundleVertices = new HashMap<Bundle, Vertex>();
        Map<BundleService, Vertex> serviceVertices = new HashMap<BundleService, Vertex>();
        Map<ServiceReference, List<BundleService>> bundlesExposingService = new HashMap<ServiceReference, List<BundleService>>();
        Map<Bundle, List<ServiceReference>> servicesInUse = new HashMap<Bundle, List<ServiceReference>>();
        Map<Bundle, List<Bundle>> packagesImported = new HashMap<Bundle, List<Bundle>>();

        Bundle[] bundles = bundleContext.getBundles();
        if(bundles != null) for(Bundle b : bundles) {
            if(startLevel.getBundleStartLevel(b) < minStartLevel ||
                ! accept(getName(b), acceptBundlesMatching, ignoreBundlesMatching)) continue;
            final ServiceReference[] registeredServices = b.getRegisteredServices();
            Vertex bundleVertex;
            Graph bundleGraph = graph;
            // remove services that are not in our accept/ignore lists
            List<ServiceReference> acceptedServices = new ArrayList<ServiceReference>();
            if(registeredServices != null) for(ServiceReference ref : registeredServices) {
                if(accept(getServiceLabel(ref), acceptServicesMatching, ignoreServicesMatching)) {
                    acceptedServices.add(ref);
                }
            }
            if(acceptedServices.size() == 0) {
                bundleVertex = graph.findOrCreate(PREFIX_VERTEX_BUNDLE + b.getBundleId());
            } else {
                Vertex bundleGraphVertex = graph.findOrCreate(PREFIX_VERTEX_BUNDLE_CONTAINER + b.getBundleId());
                bundleGraphVertex.setContainer(true);
                bundleGraph = new Graph(bundleGraphVertex);
                bundleVertex = bundleGraph.findOrCreate(PREFIX_VERTEX_BUNDLE + b.getBundleId());
            }
            bundleVertex.setLabel(strip(getName(b), stripFromBundleName));
            bundleVertices.put(b, bundleVertex);
            for(ServiceReference ref : acceptedServices) {
                Vertex serviceVertex = bundleGraph.findOrCreate(PREFIX_SERVICEREF + ref.getProperty(Constants.SERVICE_ID));
                serviceVertex.setSupporting(true);
                String label = strip(getServiceLabel(ref), stripFromServiceName);
                serviceVertex.setLabel(label);
                BundleService bundleService = new BundleService(b, ref);
                serviceVertices.put(bundleService, serviceVertex);
                if(bundlesExposingService.containsKey(ref)) {
                    bundlesExposingService.get(ref).add(bundleService);
                } else {
                    final List<BundleService> bundleServiceList = new ArrayList<BundleService>();
                    bundleServiceList.add(bundleService);
                    bundlesExposingService.put(ref, bundleServiceList);
                }
                bundleGraph.addEdge(new Edge(bundleVertex, serviceVertex, "provides", COLOR_WEAK_EDGE));
            }
            final ServiceReference[] inUseServices = b.getServicesInUse();
            if(inUseServices != null) for(ServiceReference ref : inUseServices) {
                List<ServiceReference> bundleServiceList;
                if (servicesInUse.containsKey(b)) {
                    bundleServiceList = servicesInUse.get(b);
                } else {
                    bundleServiceList = new ArrayList<ServiceReference>();
                    servicesInUse.put(b, bundleServiceList);
                }
                bundleServiceList.add(ref);
            }

            ExportedPackage[] packages = packageAdmin.getExportedPackages(b);
            if(packages != null) for(ExportedPackage p : packages) {
                final Bundle[] importingBundles = p.getImportingBundles();
                if(importingBundles != null) for(Bundle importingBundle : importingBundles) {
                    List<Bundle> bundleList;
                    if (packagesImported.containsKey(b)) {
                        bundleList = packagesImported.get(b);
                    } else {
                        bundleList = new ArrayList<Bundle>();
                        packagesImported.put(b, bundleList);
                    }
                    bundleList.add(importingBundle);
                }
            }
        }

        for(Bundle b : servicesInUse.keySet()) {
            List<ServiceReference> bundleServiceList = servicesInUse.get(b);
            for(ServiceReference ref : bundleServiceList) {
                final List<BundleService> bundleServices = bundlesExposingService.get(ref);
                if(bundleServices != null) for(BundleService bundleService : bundleServices) {
                    Vertex from = bundleVertices.get(b);
                    Vertex to = serviceVertices.get(bundleService);
                    graph.addEdge(new Edge(from, to, "uses", Color.orange));
                }
            }
        }

        for(Bundle b : packagesImported.keySet()) {
            List<Bundle> bundleList = packagesImported.get(b);
            if(bundleList != null) for(Bundle importingBundle : bundleList) {
                Vertex from = bundleVertices.get(importingBundle);
                Vertex to = bundleVertices.get(b);
                if(from != null && to != null) graph.addEdge(new Edge(from, to, "imports"));
            }
        }

        return graph;
    }

    private String getServiceLabel(ServiceReference ref) {
        String label;
        if(ref.getProperty(Constants.SERVICE_PID) != null) {
            label = ref.getProperty(Constants.SERVICE_PID).toString();
        } else {
            label = ((String[]) ref.getProperty("objectClass"))[0];
            final Object beanName = ref.getProperty("org.springframework.osgi.bean.name");
            if(beanName != null) {
                label = label + " (" + beanName.toString() + ")";
            }
        }
        return label;
    }

    private boolean accept(String str, List<Pattern> acceptList, List<Pattern> ignoreList) {
        if(ignoreList.size() > 0) for(Pattern p : ignoreList) {
            if(p.matcher(str).matches()) {
                return false;
            }
        }
        // default is to accept if no acceptList is provided
        if(acceptList.size() == 0) return true;
        if(acceptList.size() > 0) for(Pattern p : acceptList) {
            if(p.matcher(str).matches()) {
                return true;
            }
        }
        return false;
    }

    private String strip(String str, List<String> stripList) {
        for(String s : stripList) {
            str = str.replace(s, "");
        }
        return str;
    }

}
