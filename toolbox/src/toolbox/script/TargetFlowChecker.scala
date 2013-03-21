package toolbox.script
import java.awt.Color
import java.util.HashMap

object TargetFlowChecker extends App {

  import com.ensoftcorp.atlas.java.core.highlight.Highlighter
  import com.ensoftcorp.atlas.java.core.query.Attr.Edge
  import com.ensoftcorp.atlas.java.core.query.Attr.Node
  import com.ensoftcorp.atlas.java.core.query.Q
  import com.ensoftcorp.atlas.java.core.script.Common.edges
  import com.ensoftcorp.atlas.java.core.script.Common.universe
  import com.ensoftcorp.atlas.java.core.script.Common
  import com.ensoftcorp.atlas.java.core.script.Common._
  import com.ensoftcorp.atlas.java.interpreter.lib.Common._
  import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil

  import edu.iastate.jchecker.gui.views.ViolationWrapper;

  import java.util.ArrayList
  import java.lang.Boolean
  import scala.collection.mutable.ListBuffer
  import toolbox.script.Util
  import toolbox.script.util._

  def annotPkg = "annotations"; // Package that contains annotations (could be requested from user in the future)

  /*
   * This query pulls out all nodes that have been annotated with @SuppressWarnings. It will remove these nodes from target flow analysis.
   */
  def suppressedSections = edges(Edge.DECLARES).forward(extend(universe.selectNode("name", "SuppressWarnings", "subkind", "type.annotation"), Edge.ANNOTATION));

  /*
   *  Instead of using the entire universe, we combine the dataflow, annotation, and declare graphs and base our analysis off of their combination.
   */
  def galaxy = (extend(universe.edgesTaggedWithAny(Edge.DATA_FLOW, Edge.CONTROL_FLOW))) difference suppressedSections;

  /**
   * Tests for a target flow between a "Nullable" annotation and a"NonNull" annotation
   *
   * @param envelope    - The envelope to display. When envelope is null, the display envelope will be minimized
   *                      Ex. To display the entire universe with the target flow highlighted, pass in universe
   *                      as the envelope.
   * @param saveAfter   - Boolean determining whether or not to save the graphs after analysis. True will save
   *                      the graph as <timestamp>.png and add it to the toolbox project. False will not save the
   *                      graphs.
   */
  def nullTest(envelope: Q, saveAfter: Boolean) = {
    getTargetFlows(envelope, "Nullable", "NonNull", saveAfter);
  }

  /**
   * Performs a test to find places where null-literals meet a @NonNull annotation
   *
   * @param envelope    - The envelope to display. When envelope is null, the display envelope will be minimized
   *                      Ex. To display the entire universe with the target flow highlighted, pass in universe
   *                      as the envelope.
   * @param saveAfter   - Boolean determining whether or not to save the graphs after analysis. True will save
   *                      the graph as <timestamp>.png and add it to the toolbox project. False will not save the
   *                      graphs.
   */
  def nullLiteralTest(envelope: Q, saveAfter: Boolean) = {
    var sourceNodes = TargetFlowChecker.galaxy.selectNode(Node.NAME, "null") intersection TargetFlowChecker.galaxy.selectNode(Node.IS_LITERAL, new Boolean(true)); // Pull out nodes with source annotation
    var destNodes = extend(typeSelect(TargetFlowChecker.annotPkg, "NonNull"), Edge.ANNOTATION); // Pull out nodes with destination annotation

    getTargetFlowsQ(envelope, sourceNodes, destNodes, saveAfter);
  }

  /**
   * Highlights target flows between a source annotation and a destination annotation
   *
   * @param envelope    - The envelope to display. When envelope is null, the display envelope will be minimized
   *                      Ex. To display the entire universe with the target flow highlighted, pass in universe
   *                      as the envelope.
   * @param src         - The source annotation of the target flow
   * @param dest        - The destination annotation of the target flow
   * @param saveAfter   - Boolean determining whether or not to save the graphs after analysis. True will save
   *                      the graph as <timestamp>.png and add it to the toolbox project. False will not save the
   *                      graphs.
   */
  def getTargetFlows(envelope: Q, src: String, dest: String, saveAfter: Boolean) = {
    var sourceNodes = extend(typeSelect(annotPkg, src), Edge.ANNOTATION); // Pull out nodes with source annotation
    var destNodes = extend(typeSelect(annotPkg, dest), Edge.ANNOTATION); // Pull out nodes with destination annotation

    getTargetFlowsQ(envelope, sourceNodes, destNodes, saveAfter);
  }

  /**
   * Highlights target flows between a source query and a destination query
   *
   * @param envelope    - The envelope to display. When envelope is null, the display envelope will be minimized
   *                      Ex. To display the entire universe with the target flow highlighted, pass in universe
   *                      as the envelope.
   * @param src         - The source query of the target flow
   * @param dest        - The destination query of the target flow
   * @param saveAfter   - Boolean determining whether or not to save the graphs after analysis. True will save
   *                      the graph as <timestamp>.png and add it to the toolbox project. False will not save the
   *                      graphs.
   */
  def getTargetFlowsQ(envelope: Q, sourceNodes: Q, destNodes: Q, saveAfter: Boolean) = {
    var start = System.currentTimeMillis; // Mark the beginning of target flow analysis
    var targetFlows = new ListBuffer[ViolationWrapper]();
    var count = 1;

    var srcIter = sourceNodes.roots().eval().nodes().iterator(); // An iterator over nodes with the source annotation
    while (srcIter.hasNext()) {
      var srcNode = srcIter.next();
      var srcQuery = toQ(toGraph(srcNode)); // Convert the source node to it's own query

      var destIter = destNodes.roots().eval().nodes().iterator(); // An iterator over nodes with the destination annotation
      while (destIter.hasNext()) {
        var destNode = destIter.next();
        var destQuery = toQ(toGraph(destNode)); // Convert the destination node to it's own query
        //var targetFlow = galaxy.between(srcQuery, destQuery); // Search for a flow from the source node to the destination node
        var forwardFlow = SensitivityUtils.sensitiveFlow(galaxy.eval(), srcQuery.eval(), true);
        var targetFlow = toQ(forwardFlow).between(srcQuery, destQuery); ;

        if (!targetFlow.eval().edges().isEmpty()) { // If the target flow is no-empty, we've found a rule infringement

          // The following step adds the source annotation back to the subgraph
          targetFlow = targetFlow union (sourceNodes difference (sourceNodes.roots() difference srcQuery) union (destNodes difference (destNodes.roots() difference destQuery)));

          // This is a valid target flow, so tack on some metadata for display purposes
          var project = edges(Edge.DECLARES).reverse(destQuery).roots().nodesTaggedWithAll("project").eval().nodes().getFirst().attr().get("name");
          var sourceName = srcNode.attr().get("name");
          var destName = destNode.attr().get("name");
          var targetMetaData = new HashMap[String, Object]();
          targetMetaData.put("project", project);
          targetMetaData.put("source", sourceName);
          targetMetaData.put("dest", destName);
          targetMetaData.put("id", new Integer(count));
          count = count + 1;

          // Pull out annotation edges and nodes for special highlighting in the highlightSubgraph method
          var annotEdges = (sourceNodes difference (sourceNodes.roots() difference srcQuery)) union (destNodes intersection targetFlow);
          var annotNodes = annotEdges.leaves();

          var specialNodes = new ArrayList[QColor]();
          specialNodes.add(new QColor(annotEdges.roots(), Color.RED));
          specialNodes.add(new QColor(annotNodes, Color.GREEN));
          specialNodes.add(new QColor(targetFlow difference annotEdges, Color.ORANGE));

          var specialEdges = new ArrayList[QColor]();
          specialEdges.add(new QColor(annotEdges, Color.GREEN));
          specialEdges.add(new QColor(targetFlow difference annotNodes, Color.ORANGE));

          // Use the given envelope if it isn't null, make the subgraph it's own envelope when the given envelope is null
          var env = envelope;
          if (env == null) {
            env = targetFlow;
          }

          targetFlows += new ViolationWrapper(env, targetFlow, specialNodes, specialEdges, targetMetaData);
        }
      }
    }

    var elapsedTime = System.currentTimeMillis - start; // Calculate the duration of the flow analysis for display
    var seconds = elapsedTime / 1000;
    println("Operation took " + ((seconds - seconds % 60) / 60) + " minutes, " + seconds % 60 + " seconds, and " + (elapsedTime % 1000) + " milliseconds.");

    targetFlows;
  }

  /**
   * Highlights a subgraph within the full graph (can be the same) with special nodes and edges highlighted differently
   *
   * @param fullGraph           - Full graph to display when completed
   * @param highlightedSubgraph - Highlighted portion of the full graph (can be the same as the full graph)
   * @param specialNodes        - A list containing pairs. Each pair contains a subgraph and a color. The subgraph's nodes
   *                              will be highlighted in the given color.
   * @param specialEdges        - A list containing pairs. Each pair contains a subgraph and a color. The subgraph's edges
   *                              will be highlighted in the given color.
   * @param saveAfter           - Boolean determining whether or not to save the graphs after analysis. True will save
   *                              the graph as <timestamp>.png and add it to the toolbox project. False will not save the
   *                              graphs.
   */
  def highlightSubgraph(fullGraph: Q, highlightedSubgraph: Q, specialNodes: java.util.List[QColor], specialEdges: java.util.List[QColor], saveAfter: Boolean) = {
    var h = new Highlighter();

    var edgeIter = specialEdges.iterator; // Iterate through the special edge list highlighting each with the given color
    while (edgeIter.hasNext) {
      var pair = edgeIter.next();

      // Highlight special edges with their given colors
      h.highlightEdges(pair.getQuery(), pair.getColor());
    }

    var nodeIter = specialNodes.iterator; // Iterate through the special node list highlighting each with the given color
    while (nodeIter.hasNext) {
      var pair = nodeIter.next();

      // Highlight special nodes with their given colors
      h.highlightNodes(pair.getQuery(), pair.getColor());
    }

    // Display the highlighted subgraph in the given envelope
    DisplayUtil.displayGraph(fullGraph.eval(), h);

    // Save the graph as an image when called for
    if (saveAfter) {
      Util.saveGraph(System.currentTimeMillis() + ".png", fullGraph, h);
    }
  }
}
