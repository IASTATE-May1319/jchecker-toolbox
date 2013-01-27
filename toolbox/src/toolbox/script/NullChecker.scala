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

  /**
   * Get the dataflow edges going to the given node
   */
  def flow(sink:Q) = {
    edges(Edge.DATA_FLOW).reverse(edges(Edge.DECLARES).forward(sink))
  }

  def showSubgraph(sink:Q) = {
    var t = edges(Edge.DATA_FLOW).reverse(edges(Edge.DECLARES).forward(sink))
    var h = new Highlighter()

    DisplayUtil.displayGraph(t.eval(), h)
  }
  
  
  def showHighlightedSubgraph(sink:Q) = {
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
  def check(sink:Q) = {
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
  def recursiveCheck(graph:Graph, g:GraphElement, indent:String):GraphElement = {
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
}
