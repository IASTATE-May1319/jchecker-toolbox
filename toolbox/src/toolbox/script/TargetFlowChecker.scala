import java.awt.Color

object TargetFlowChecker extends App {

  import com.ensoftcorp.atlas.java.core.highlight.Highlighter
  import com.ensoftcorp.atlas.java.core.query.Attr.Edge
  import com.ensoftcorp.atlas.java.core.query.Q
  import com.ensoftcorp.atlas.java.core.script.Common.edges
  import com.ensoftcorp.atlas.java.core.script.Common.universe
  import com.ensoftcorp.atlas.java.core.script.Common
  import com.ensoftcorp.atlas.java.core.script.Common._
  import com.ensoftcorp.atlas.java.interpreter.lib.Common._
  import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil
  import toolbox.script.Util;

  //def annotQuery = universe.selectNode("name", dest) intersection universe.selectNode("subkind", "type.annotation");
  def annotPkg = "annotations";

  def dfg = extend(edges(Edge.DATA_FLOW)) union extend(edges(Edge.ANNOTATION)) union extend(edges(Edge.DECLARES));

  /**
   * Tests for a target flow between a "Nullable" annotation and a"NonNull" annotation
   *
   * @param envelope -  The envelope to display. When envelope is null, the display envelope will be minimized
   *                    Ex. To display the entire universe with the target flow highlighted, pass in universe
   *                    as the envelope.
   */
  def nullTest(envelope: Q) = {
    highlightTargetFlow(envelope, "Nullable", "NonNull", false);
  }

  /**
   * Highlights target flows between a source annotation and a destination annotation
   *
   * @param envelope 	-  The envelope to display. When envelope is null, the display envelope will be minimized
   *                       Ex. To display the entire universe with the target flow highlighted, pass in universe
   *                       as the envelope.
   * @param src      	-  The source annotation of the target flow
   * @param dest     	-  The destination annotation of the target flow
   * @param saveAfter	-  Boolean determining whether or not to save the graphs after analysis. True will save
   * 			   the graph as <timestamp>.png and add it to the toolbox project. False will not save the
   * 			   graphs.
   */
  def highlightTargetFlow(envelope: Q, src: String, dest: String, saveAfter: Boolean) = {
    var start = System.currentTimeMillis; // Mark the beginning of target flow analysis

    var sourceNodes = extend(typeSelect(annotPkg, src), Edge.ANNOTATION); // Pull out nodes with source annotation
    var destNodes = extend(typeSelect(annotPkg, dest), Edge.ANNOTATION); // Pull out nodes with destination annotation

    var srcIter = sourceNodes.roots().eval().nodes().iterator(); // An iterator over nodes with the source annotation
    while (srcIter.hasNext()) {
      var srcNode = srcIter.next();
      var srcQuery = toQ(Common.toGraph(srcNode)); // Covert the source node to it's own query
      var targetFlow = dfg.between(srcQuery, destNodes); // Search for a flow from this source to any destination node

      if (!targetFlow.eval().edges().isEmpty()) { // If the target flow is no-empty, we've found a rule infringement

        // The following step adds the source annotation back to the subgraph
        targetFlow = targetFlow union (sourceNodes difference (sourceNodes.roots() difference srcQuery));

        // Pull out annotation edges and nodes for special highlighting in the highlightSubgraph method
        var annotEdges = (sourceNodes difference (sourceNodes.roots() difference srcQuery)) union (destNodes intersection targetFlow);
        var annotNodes = annotEdges.leaves();

        // Use the given envelope if it isn't null, make the subgraph it's own envelope when the given envelope is null
        if (envelope == null) {
          highlightSubgraph(targetFlow, targetFlow, annotNodes, annotEdges, saveAfter);
        } else {
          highlightSubgraph(envelope, targetFlow, annotNodes, annotEdges, saveAfter);
        }
      }
    }

    var elapsedTime = System.currentTimeMillis - start; // Calculate the duration of the flow analysis for display
    var seconds = elapsedTime / 1000;
    println("Operation took " + ((seconds - seconds % 60) / 60) + " minutes, " + seconds % 60 + " seconds, and " + (elapsedTime % 1000) + " milliseconds.");
  }

  /**
   * Highlights a subgraph within the full graph (can be the same) with special nodes and edges highlighted differently
   *
   * @param fullGraph	 	- Full graph to display when completed
   * @param highlightedSubgraph - Highlighted portion of the full graph (can be the same as the full graph)
   * @param specialNodes 	- Special nodes within the highlighted subgraph to be highlighted differently
   * @param specialEdges 	- Special edges within the highlighted subgraph to be highlighted differently
   * @param saveAfter	 	- Boolean determining whether or not to save the graphs after analysis. True will save
   * 			 	  the graph as <timestamp>.png and add it to the toolbox project. False will not save the
   * 			 	  graphs.
   */
  def highlightSubgraph(fullGraph: Q, highlightedSubgraph: Q, specialNodes: Q, specialEdges: Q, saveAfter: Boolean) = {

    var h = new Highlighter()

    // Highlight special edges in red
    h.highlightEdges(specialEdges, Color.RED)

    // Highlight roots of special edges in orange
    h.highlightNodes(specialEdges.roots(), Color.ORANGE)

    // Highlight special nodes in red
    h.highlightNodes(specialNodes, Color.RED)

    // Display the highlighted subgraph in the given envelope
    DisplayUtil.displayGraph(fullGraph.eval(), h)

    // Save the graph as an image when called for
    if (saveAfter) {
      Util.saveGraph(System.currentTimeMillis() + ".png", fullGraph, h);
    }
  }
}
