package com.vivosys.osgi.deps.builder;

import com.vivosys.osgi.deps.builder.graph.Edge;
import com.vivosys.osgi.deps.builder.graph.Graph;
import com.vivosys.osgi.deps.builder.graph.Vertex;
import com.vivosys.osgi.deps.builder.graphml.GraphMLGenerator;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OsgiGraphBuilderTest {

    private final static String SERVICE_PACKAGE_ADMIN = "org.osgi.service.packageadmin.PackageAdmin";
    private final static String SERVICE_STARTLEVEL = "org.osgi.service.startlevel.StartLevel";

    private Bundle b0;
    private Bundle b1;
    private Bundle b2;
    private Bundle b3;

    private ServiceReference servARefExportedByBundle1;
    private ServiceReference servARefExportedByBundle3;
    private ServiceReference servBRef;

    private ServiceReference startLevelServiceRef;
    private StartLevel startLevelService;

    private ServiceReference pkgAdminServiceRef;
    private PackageAdmin pkgAdminService;

    @Before
    public void setupStubs() {
        servARefExportedByBundle1 = getMockServiceRef(0, "serv-a");
        servARefExportedByBundle3 = getMockServiceRef(1, "serv-a");
        servBRef = getMockServiceRef(2, "serv-b");

        startLevelServiceRef = mock(ServiceReference.class);
        startLevelService = mock(StartLevel.class);

        pkgAdminServiceRef = mock(ServiceReference.class);
        pkgAdminService = mock(PackageAdmin.class);

        b0 = getMock("mock-b0", 0, null, null);
        b1 = getMock("mock-b1", 1, new ServiceReference[] {servARefExportedByBundle3}, new ServiceReference[] {servARefExportedByBundle1});
        b2 = getMock("mock-b2", 2, new ServiceReference[] {servBRef}, null);
        b3 = getMock("mock-b3", 3, null, new ServiceReference[] {servARefExportedByBundle3, servBRef});
    }

    @Test
    public void testBuildGraph() throws Exception {
        BundleContext context = mock(BundleContext.class, RETURNS_SMART_NULLS);
        when(context.getServiceReference(SERVICE_STARTLEVEL)).thenReturn(startLevelServiceRef);
        when(context.getService(startLevelServiceRef)).thenReturn(startLevelService);
        when(startLevelService.getBundleStartLevel(b0)).thenReturn(5);
        when(startLevelService.getBundleStartLevel(b1)).thenReturn(50);
        when(startLevelService.getBundleStartLevel(b2)).thenReturn(50);
        when(startLevelService.getBundleStartLevel(b3)).thenReturn(60);

        when(context.getService(servARefExportedByBundle1)).thenReturn(new ServiceImpl("serv-a"));
        when(context.getService(servARefExportedByBundle3)).thenReturn(new ServiceImpl("serv-a"));
        when(context.getService(servBRef)).thenReturn(new ServiceImpl("serv-b"));
        when(context.getBundles()).thenReturn(new Bundle[] {b0, b1, b2, b3});

        when(context.getServiceReference(SERVICE_PACKAGE_ADMIN)).thenReturn(pkgAdminServiceRef);
        when(context.getService(pkgAdminServiceRef)).thenReturn(pkgAdminService);

        ExportedPackage[] exportedPackagesB1 = new ExportedPackage[] {
            getMockExpPkg("b1.export1", new Bundle[] {b3}),
            getMockExpPkg("b1.export2", new Bundle[0]),
            getMockExpPkg("b1.export3", new Bundle[0])
        };
        when(pkgAdminService.getExportedPackages(b1)).thenReturn(exportedPackagesB1);

        ExportedPackage[] exportedPackagesB2 = new ExportedPackage[] {
            getMockExpPkg("b2.export1", new Bundle[0]),
            getMockExpPkg("b2.export2", new Bundle[] {b1}),
            getMockExpPkg("b2.export3", new Bundle[0])
        };
        when(pkgAdminService.getExportedPackages(b2)).thenReturn(exportedPackagesB2);

        when(pkgAdminService.getExportedPackages(b3)).thenReturn(null);

        OsgiGraphBuilder builder = new OsgiGraphBuilder();
        Graph g = builder.buildGraph(context, 50);
        System.out.println(g);

        verifyZeroInteractions(b0);

        assertNull(g.getParent());
        assertEquals(4, g.getEdges().size());
        assertTrue(hasEdge(g, "mock-b1", "serv-a", "uses"));
        assertTrue(hasEdge(g, "mock-b1", "serv-a", "provides"));
        assertTrue(hasEdge(g, "mock-b1", "mock-b2", "imports"));
        assertTrue(hasEdge(g, "mock-b2", "serv-b", "uses"));
        assertTrue(hasEdge(g, "mock-b3", "mock-b1", "imports"));
        assertTrue(hasEdge(g, "mock-b3", "serv-a", "provides"));
        assertTrue(hasEdge(g, "mock-b3", "serv-b", "provides"));

        assertEquals(3, g.getVertices().size());
        assertTrue(hasVertex(g, "mock-b1"));
        assertTrue(hasVertex(g, "mock-b2"));
        assertTrue(hasVertex(g, "mock-b3"));
        assertTrue(hasVertex(g, "serv-a"));
        assertTrue(hasVertex(g, "serv-b"));

/*
        GraphMLGenerator gen = new GraphMLGenerator();
        //Writer out = new PrintWriter(System.out);
        Writer out = new FileWriter("/var/tmp/test.graphml");
        gen.serialize(g, out);
        out.flush();
        out.close();
*/
    }

    private Bundle getMock(String toString, long id, ServiceReference[] servicesInUse, ServiceReference[] registeredServices) {
        final Bundle mock = mock(Bundle.class, RETURNS_SMART_NULLS);
        when(mock.getServicesInUse()).thenReturn(servicesInUse);
        when(mock.getRegisteredServices()).thenReturn(registeredServices);
        when(mock.toString()).thenReturn(toString);
        when(mock.getBundleId()).thenReturn(id);
        when(mock.getSymbolicName()).thenReturn(toString);
        return mock;
    }

    private ServiceReference getMockServiceRef(long id, String serviceName) {
        final ServiceReference mock = mock(ServiceReference.class, RETURNS_SMART_NULLS);
        when(mock.getProperty(Constants.SERVICE_ID)).thenReturn(id);
        when(mock.getProperty(Constants.SERVICE_PID)).thenReturn(serviceName);
        when(mock.toString()).thenReturn("service-ref-" + serviceName);
        return mock;
    }

    private ExportedPackage getMockExpPkg(String pkgName, Bundle[] importingBundles) {
        final ExportedPackage mock = mock(ExportedPackage.class, RETURNS_SMART_NULLS);
        when(mock.getName()).thenReturn(pkgName);
        when(mock.toString()).thenReturn("pkg-" + pkgName);
        when(mock.getImportingBundles()).thenReturn(importingBundles);
        return mock;
    }

    private boolean hasEdge(Graph g, Object fromId, Object toId, String label) {
        boolean ret = false;
        for(Edge e : g.getEdges()) {
            ret = fromId.equals(e.from.getLabel()) && toId.equals(e.to.getLabel()) && e.label.equals(label);
            if(! ret) for(Vertex v : g.getVertices()) {
                if(v.getGraph() != null) {
                    ret = hasEdge(v.getGraph(), fromId, toId, label);
                    if(ret) break;
                }
            }
            if(ret) break;
        }
        return ret;
    }

    private boolean hasVertex(Graph graph, String vertexLabel) {
        boolean ret = false;
        for(Vertex v : graph.getVertices()) {
            ret = vertexLabel.equals(v.getLabel());
            if(! ret && v.getGraph() != null) {
                ret = hasVertex(v.getGraph(), vertexLabel);
            }
            if(ret) break;
        }
        return ret;
    }

    private class ServiceImpl {
        String name;
        private ServiceImpl(String name) {
            this.name = name;
        }
    }
}
