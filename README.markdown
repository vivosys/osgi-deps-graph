Introduction
------------

Quick-and-dirty mechanism to produce a dependency graph for a set of bundles in an OSGi environment.

The graph is output in graphml, and includes the following features:

1. Bundles,

2. OSGi services registered by each bundle. The bundle and associated services are enclosed together,

3. Bundle package dependency relationships i.e. if bundle A imports any packages in bundle B, a "imports"
   directed edge is drawn from A to B.

4. Bundle service relationships i.e. if bundle A uses a service S, a "uses" directed edge (shown in a
   different color) is drawn from A to S.

It supports specifying regex-based includes and excludes for bundles and services. It also supports stripping
text from the beginning of bundle and service labels in order to remove repetitive information and make the graph
neater. It also supports ignoring all bundles below a given start level.

The code is loosely based on a similar project maven-graph-plugin that outputs a graphml graph based on a
project's maven dependencies. See https://github.com/janssk1/maven-graph-plugin.


Usage
-----

To use, simply install the bundle into your OSGi environment and start it. It will produce the graphml output
in the current working directory. Import the graphml into a supported editor (such as yEd), and format the
graph as desired. I find a hierarchical view generally works well.

The bundle can be immediately uninstalled after it generates the graph.


Future Improvements
-------------------

1. Currently the includes/excludes/strips are entered directly into the OsgiDepsActivator. These could be obtained
   from the OSGi ConfigAdmin.

2. The code in OsgiGraphBuilder could definitely use some love/refactoring. The tests should be improved before this
   refactoring is done.

3. Add the ability to display individual package import/exports in the graph. This will make the graph VERY large but
   may be useful in some cases.

4. Add more information to the graph, as needed. Use more color coding.