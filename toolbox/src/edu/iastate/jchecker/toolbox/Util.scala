package edu.iastate.jchecker.toolbox

import com.ensoftcorp.atlas.java.core.highlight.Highlighter
import com.ensoftcorp.atlas.java.core.query.Q
import com.ensoftcorp.atlas.java.core.script.Common.universe
import com.ensoftcorp.atlas.java.interpreter.lib.Common.save
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil
import org.eclipse.core.resources.ResourcesPlugin

object Util extends App {

  import com.ensoftcorp.atlas.java.core.query.Q
  import com.ensoftcorp.atlas.java.core.query.Attr.Edge
  import com.ensoftcorp.atlas.java.core.query.Attr.Node
  import com.ensoftcorp.atlas.java.core.script.Common._
  import com.ensoftcorp.atlas.java.core.highlight._
  import com.ensoftcorp.atlas.java.interpreter.lib.Common._
  import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil
  import com.ensoftcorp.atlas.java.core.highlight.Highlighter
  import java.lang.Boolean

  /**
   * @param filename	- Name of the output png file
   * @param graph	- Graph to be converted to an image
   * @param h		- Highlighter for the graph
   */
  def saveGraph(filename: String, graph: Q, h: Highlighter) = {

    // PNG output file
    import org.eclipse.core.resources.ResourcesPlugin
    var projectName = "toolbox";
    var pngName = filename;
    var out = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile(pngName);
    var file = out.getRawLocation().toFile().getAbsoluteFile();

    var job = save(graph, file, highlighter = h);

    // block until save is complete
    job.join();
  }

  def test() = {
    var query = universe.selectNode("isControlFlowRoot", new java.lang.Boolean(true), "isControlFlowExitPoint", new java.lang.Boolean(true), "dataFlowVisited", new java.lang.Boolean(true));
    DisplayUtil.displayGraph(query.eval(), new Highlighter());
  }
}