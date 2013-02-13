package toolbox.script
import com.ensoftcorp.atlas.java.core.script.Common
import com.ensoftcorp.atlas.java.core.index.NameFactory
import com.ensoftcorp.atlas.java.core.db.Accuracy

object NullChecker {

  import com.ensoftcorp.atlas.java.core.query.Q
  import com.ensoftcorp.atlas.java.core.query.Attr
  import com.ensoftcorp.atlas.java.core.query.Attr.Edge
  import com.ensoftcorp.atlas.java.core.query.Attr.Node
  import com.ensoftcorp.atlas.java.core.script.Common._
  import com.ensoftcorp.atlas.java.core.highlight._
  import com.ensoftcorp.atlas.java.interpreter.lib.Common._
  import com.ensoftcorp.atlas.java.core.db.graph.Graph
  import com.ensoftcorp.atlas.java.core.db.graph.GraphElement
  import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil
  import java.awt.Color
  import scala.collection.mutable.ListBuffer
  import toolbox.script.TargetFlowChecker

  /**
   * Get the dataflow edges going to the given node
   */
  def flow(sink: Q) = {
    edges(Edge.DATA_FLOW).reverse(edges(Edge.DECLARES).forward(sink))
  }

  def showSubgraph(sink: Q) = {
    var t = edges(Edge.DATA_FLOW).reverse(edges(Edge.DECLARES).forward(sink))
    var h = new Highlighter()

    DisplayUtil.displayGraph(t.eval(), h)
  }

  def showHighlightedSubgraph(sink: Q) = {
    var t = edges(Edge.DATA_FLOW).reverse(edges(Edge.DECLARES).forward(sink))
    var y = check(sink)

    var h = new Highlighter()
    var iter = y.iterator
    while (iter.hasNext) {
      var next = iter.next()
      var sourceQ = toQ(toGraph(next))
      var destQ = t.leaves()
      var subgraph = t.between(sourceQ, destQ)
      h.highlightEdges(subgraph, Color.ORANGE)
      h.highlightNodes(subgraph.roots(), Color.RED)
    }

    DisplayUtil.displayGraph(t.eval(), h)
  }

  /**
   * Check whether a null value ever flows into the given node
   *
   * @returns a list of the offending node assignments
   */
  def check(sink: Q) = {
    var t = edges(Edge.DATA_FLOW).reverse(edges(Edge.DECLARES).forward(sink))
    val list = new ListBuffer[GraphElement]()
    var graph = t.eval()

    var nodes = graph.nodes()
    var iter = nodes.iterator()
    while (iter.hasNext()) {
      var result = recursiveCheck(graph, iter.next(), "")
      if (result != null) {
        println(result.attr())

        list += result
      }
    }

    list;
  }

  /**
   * Recursively check the given graph and graph element to see if
   * a null value is ever assigned to it.
   *
   * @return the GraphElement that is assigned to null (null if it 'null' isn't assigned)
   */
  def recursiveCheck(graph: Graph, g: GraphElement, indent: String): GraphElement = {
    var attrs = g.attr()
    var id = attrs.get("id")

    // TODO: not sure if this is a good check for being assigned null...
    if (id != null && id.toString().contains("org.eclipse.jdt.core.dom.NullLiteral")) {
      println("NULLABLE assigned to NONNULL")
      return g;
    }

    var edgeIter = graph.edges(g, GraphElement.NodeDirection.IN).iterator()
    while (edgeIter.hasNext()) {
      var tmp = recursiveCheck(graph, edgeIter.next(), indent + "\t")
      if (tmp != null) {
        return tmp;
      }
    }

    return null;
  }

  def highlightTargetFlow(envelope: Q, saveAfter: Boolean) = {
    var start = System.currentTimeMillis; // Mark the beginning of target flow analysis

    var sourceNodes = universe().selectNode(Node.NAME, "null"); // Pull out nodes with source annotation
    var destNodes = extend(typeSelect(TargetFlowChecker.annotPkg, "NonNull"), Edge.ANNOTATION); // Pull out nodes with destination annotation

    var srcIter = sourceNodes.roots().eval().nodes().iterator(); // An iterator over nodes with the source annotation
    while (srcIter.hasNext()) {
      var srcNode = srcIter.next();
      var srcQuery = toQ(Common.toGraph(srcNode)); // Convert the source node to it's own query

      var destIter = destNodes.roots().eval().nodes().iterator(); // An iterator over nodes with the destination annotation
      while (destIter.hasNext()) {
        var destNode = destIter.next();
        var destQuery = toQ(Common.toGraph(destNode)); // Convert the destination node to it's own query
        var targetFlow = TargetFlowChecker.galaxy.between(srcQuery, destQuery); // Search for a flow from the source node to the destination node

        if (!targetFlow.eval().edges().isEmpty()) { // If the target flow is no-empty, we've found a rule infringement

          // The following step adds the source annotation back to the subgraph
          targetFlow = targetFlow union (sourceNodes difference (sourceNodes.roots() difference srcQuery) union (destNodes difference (destNodes.roots() difference destQuery)));

          // Pull out annotation edges and nodes for special highlighting in the highlightSubgraph method
          var annotEdges = (sourceNodes difference (sourceNodes.roots() difference srcQuery)) union (destNodes intersection targetFlow);
          var annotNodes = annotEdges.leaves();

          var specialNodes = List(Pair(annotEdges.roots(), Color.RED), Pair(annotNodes, Color.GREEN), Pair(targetFlow difference annotEdges, Color.ORANGE));
          var specialEdges = List(Pair(annotEdges, Color.GREEN), Pair(targetFlow difference annotNodes, Color.ORANGE));

          // Use the given envelope if it isn't null, make the subgraph it's own envelope when the given envelope is null
          if (envelope == null) {
            TargetFlowChecker.highlightSubgraph(targetFlow, targetFlow, specialNodes, specialEdges, saveAfter);
          } else {
            TargetFlowChecker.highlightSubgraph(envelope, targetFlow, specialNodes, specialEdges, saveAfter);
          }
        }
      }
    }

    var elapsedTime = System.currentTimeMillis - start; // Calculate the duration of the flow analysis for display
    var seconds = elapsedTime / 1000;
    println("Operation took " + ((seconds - seconds % 60) / 60) + " minutes, " + seconds % 60 + " seconds, and " + (elapsedTime % 1000) + " milliseconds.");
  }
}
