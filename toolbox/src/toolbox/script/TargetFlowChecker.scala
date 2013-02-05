import java.awt.Color

object TargetFlowChecker extends App {

  import com.ensoftcorp.atlas.java.core.highlight.Highlighter
  import com.ensoftcorp.atlas.java.core.query.Attr.Edge
  import com.ensoftcorp.atlas.java.core.query.Q
  import com.ensoftcorp.atlas.java.core.script.Common.edges
  import com.ensoftcorp.atlas.java.core.script.Common.universe
  import com.ensoftcorp.atlas.java.core.script.Common._
  import com.ensoftcorp.atlas.java.interpreter.lib.Common._
  import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil

  //def annotQuery = universe.selectNode("name", dest) intersection universe.selectNode("subkind", "type.annotation");
  def annotPkg = "annotations";

  /**
   * Tests for a target flow between a "Nullable" annotation and a"NonNull" annotation
   *
   * @param envelope -  The envelope to display. When envelope is null, the display envelope will be minimized
   *                    Ex. To display the entire universe with the target flow highlighted, pass in universe
   *                    as the envelope.
   */
  def nullTest(envelope: Q) = {
    highlightTargetFlow(envelope, "Nullable", "NonNull");
  }

  /**
   * Highlights target flows between a src annotation and a dest annotation
   *
   * @param envelope -  The envelope to display. When envelope is null, the display envelope will be minimized
   *                    Ex. To display the entire universe with the target flow highlighted, pass in universe
   *                    as the envelope.
   * @param src      -  The source annotation of the target flow
   * @param dest     -  The destination annotation of the target flow
   */
  def highlightTargetFlow(envelope: Q, src: String, dest: String) = {
    var start = System.currentTimeMillis;
    var sourceNodes = extend(typeSelect(annotPkg, src), Edge.ANNOTATION);
    var destNodes = extend(typeSelect(annotPkg, dest), Edge.ANNOTATION);
    var targetFlow = universe.between(sourceNodes, destNodes);

    if (!targetFlow.eval().edges().isEmpty()) {
      targetFlow = targetFlow union sourceNodes;

      var annotNodes = universe.selectNode("subkind", "type.annotation") intersection targetFlow;

      var annotEdges = edges(Edge.ANNOTATION).reverseStep(universe.selectNode("subkind", "type.annotation")) intersection targetFlow;

      if (envelope == null) {
        highlightSubgraph(targetFlow, targetFlow, annotNodes, annotEdges);
      } else {
        highlightSubgraph(envelope, targetFlow, annotNodes, annotEdges);
      }

      println("FAILED: A target flow was found between " + src + " and " + dest + "!");
    } else {
      println("SUCCESS: No target flows were found between " + src + " and " + dest + ".");
    }
    var elapsedTime = System.currentTimeMillis - start;
    var seconds = elapsedTime / 1000;

    println("Operation took " + ((seconds - seconds % 60) / 60) + " minutes, " + seconds % 60 + " seconds, and " + (elapsedTime % 1000) + " milliseconds.");
  }

  /**
   * Highlights a subgraph within the full graph (can be the same) with special nodes and edges highlighted differently
   *
   * @param fullGraph - Full graph to display when completed
   * @param highlightedSubgraph - Highlighted portion of the full graph (can be the same as the full graph)
   * @param specialNodes - Special nodes within the highlighted subgraph to be highlighted differently
   * @param specialEdges - Special edges within the highlighted subgraph to be highlighted differently
   */
  def highlightSubgraph(fullGraph: Q, highlightedSubgraph: Q, specialNodes: Q, specialEdges: Q) {
    var h = new Highlighter()
    h.highlightEdges(highlightedSubgraph difference specialNodes, Color.YELLOW)
    h.highlightEdges(highlightedSubgraph intersection specialEdges, Color.RED)
    h.highlightNodes(highlightedSubgraph difference specialNodes, Color.ORANGE)
    h.highlightNodes(highlightedSubgraph intersection specialNodes, Color.RED)
    DisplayUtil.displayGraph(fullGraph.eval(), h)

    // PNG output file
    import org.eclipse.core.resources.ResourcesPlugin
    var projectName = "toolbox";
    var pngName = "out.png";
    var out = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile(pngName);
    var file = out.getRawLocation().toFile().getAbsoluteFile();

    var job = save(fullGraph, file, highlighter = h);

    // block until save is complete
    job.join();
  }
}
