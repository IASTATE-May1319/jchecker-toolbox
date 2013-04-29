package edu.iastate.jchecker.toolbox.util;

import static com.ensoftcorp.atlas.java.core.script.Common.*;
import com.ensoftcorp.atlas.java.core.db.graph.Graph;
import com.ensoftcorp.atlas.java.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.java.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.java.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.java.core.db.graph.operation.InducedGraph;
import com.ensoftcorp.atlas.java.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.java.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.java.core.db.set.EmptyAtlasSet;
import com.ensoftcorp.atlas.java.core.query.Q;

public class LightTraversals {
  /**
   * Returns the set difference a - b on both nodes and edges
   * @param a
   * @param b
   * @return
   */
  public static Q lDifference(Q a, Q b){
    return toQ(lDifference(a.eval(), b.eval()));
  }
  
  /**
   * Returns the set difference a - b on both nodes and edges
   * @param a
   * @param b
   * @return
   */
  public static Graph lDifference(Graph a, Graph b){
    return new InducedGraph(lDifference(a.nodes(), b.nodes()),
                            lDifference(a.edges(), b.edges())); 
  }
  
  /**
   * Returns the set difference a - b on both nodes and edges
   * @param a
   * @param b
   * @return
   */
  public static Graph lDifference(Graph a, GraphElement b){
    AtlasSet<GraphElement> nodes = new AtlasHashSet<GraphElement>(a.nodes());
    AtlasSet<GraphElement> edges = new AtlasHashSet<GraphElement>(a.edges());
    nodes.remove(b);
    edges.remove(b);
    
    return new InducedGraph(nodes, edges); 
  }
  
  /**
   * Returns the set difference a - b
   * @param a
   * @param b
   * @return
   */
  public static AtlasSet<GraphElement> lDifference(AtlasSet<GraphElement> a, AtlasSet<GraphElement> b) {
    AtlasHashSet<GraphElement> result = new AtlasHashSet<GraphElement>(); 
    for(GraphElement ge : a){
      if(!b.contains(ge)) result.add(ge);
    }
    return result;
  }
  
  /**
   * Returns the set difference a - b
   * @param a
   * @param b
   * @return
   */
  public static AtlasSet<GraphElement> lDifference(AtlasSet<GraphElement> a, GraphElement b) {
    AtlasHashSet<GraphElement> result = new AtlasHashSet<GraphElement>(a);
    result.remove(b);
    return result;
  }
  
  /**
   * Returns the set intersection a AND b for both nodes and edges
   * @param a
   * @param b
   * @return
   */
  public static Q lIntersection(Q a, Q b){
    return toQ(lIntersection(a.eval(),b.eval()));
  }
  
  /**
   * Returns the set intersection a AND b for both nodes and edges
   * @param a
   * @param b
   * @return
   */
  public static Graph lIntersection(Graph a, Graph b){
    return new InducedGraph(lIntersection(a.nodes(), b.nodes()),
                            lIntersection(a.edges(), b.edges())); 
  }
  
  /**
   * Returns the set intersection a AND b
   * @param a
   * @param b
   * @return
   */
  public static AtlasSet<GraphElement> lIntersection(AtlasSet<GraphElement> a, AtlasSet<GraphElement> b) {
    AtlasHashSet<GraphElement> result = new AtlasHashSet<GraphElement>();
    for(GraphElement ge : a){
      if(b.contains(ge)) result.add(ge);
    }
    return result;
  }
  
  /**
   * Returns the union of nodes and edges in a and b
   * @param a
   * @param b
   * @return
   */
  public static Q lUnion(Q a, Q b){
    return toQ(lUnion(a.eval(), b.eval()));
  }
  
  /**
   * Returns the union of nodes and edges in a and b
   * @param a
   * @param b
   * @return
   */
  public static Graph lUnion(Graph a, Graph b){
    return new InducedGraph(lUnion(a.nodes(), b.nodes()),
                            lUnion(a.edges(), b.edges())); 
  }
  
  /**
   * Returns the union of a and b
   * @param a
   * @param b
   * @return
   */
  public static AtlasSet<GraphElement> lUnion(AtlasSet<GraphElement> a, AtlasSet<GraphElement> b) {
    AtlasHashSet<GraphElement> result = new AtlasHashSet<GraphElement>();
    result.addAll(a);
    result.addAll(b);
    return result;
  }
  
  /**
   * Returns the reverse traversal from the given nodes in the given context.
   * @param context
   * @param fromNodes
   * @return
   */
  public static Q lReverse(Q context, Q fromNodes){
    return toQ(lReverse(context.eval(), context.eval().nodes(), false, Integer.MAX_VALUE));
  }
  
  /**
   * Returns the reverse traversal from the given nodes in the given context.
   * Optionally will return nodes only, and can be limited to traversals of a 
   * maximum length.
   * @param context
   * @param fromNodes
   * @param nodesOnly
   * @param steps
   * @return
   */
  public static Q lReverse(Q context, Q fromNodes, boolean nodesOnly, int steps){
    return toQ(lReverse(context.eval(), context.eval().nodes(), nodesOnly, steps));
  }
  
  /**
   * Returns the reverse traversal from the given nodes in the given context.
   * Optionally will return nodes only, and can be limited to traversals of a 
   * maximum length.
   * @param context
   * @param fromNodes
   * @param nodesOnly
   * @param steps
   * @return
   */
  public static Graph lReverse(Graph context, Graph fromNodes, boolean nodesOnly, int steps) {
    return lReverse(context, fromNodes.nodes(), nodesOnly, steps);
  }
  
  /**
   * Returns the reverse traversal from the given nodes in the given context.
   * Optionally will return nodes only, and can be limited to traversals of a 
   * maximum length.
   * @param context
   * @param fromNodes
   * @param nodesOnly
   * @param steps
   * @return
   */
  public static Graph lReverse(Graph context, AtlasSet<GraphElement> fromNodes, boolean nodesOnly, int steps) {
    AtlasHashSet<GraphElement> nodes = new AtlasHashSet<GraphElement>();
    AtlasHashSet<GraphElement> edges = new AtlasHashSet<GraphElement>();
    nodeWalk(context, fromNodes, nodesOnly, steps, nodes, edges, NodeDirection.IN, EdgeDirection.FROM);
    return new InducedGraph(nodes, edges);
  }
  
  /**
   * Returns the reverse traversal from the given nodes in the given context.
   * Optionally will return nodes only, and can be limited to traversals of a 
   * maximum length.
   * @param context
   * @param fromNodes
   * @param nodesOnly
   * @param steps
   * @return
   */
  public static Graph lReverse(Graph context, GraphElement fromNode, boolean nodesOnly, int steps) {
    AtlasHashSet<GraphElement> fromNodes = new AtlasHashSet<GraphElement>();
    fromNodes.add(fromNode);
    return lReverse(context, fromNodes, nodesOnly, steps);
  }
  
  /**
   * Returns the forward traversal from the given nodes in the given context.
   * Optionally will return nodes only, and can be limited to traversals of a 
   * maximum length.
   * @param context
   * @param fromNodes
   * @return
   */
  public static Q lForward(Q context, Q from){
    return toQ(lForward(context.eval(), from.eval().nodes(), false, Integer.MAX_VALUE));
  }
  
  /**
   * Returns the forward traversal from the given nodes in the given context.
   * Optionally will return nodes only, and can be limited to traversals of a 
   * maximum length.
   * @param context
   * @param fromNodes
   * @param nodesOnly
   * @param steps
   * @return
   */
  public static Q lForward(Q context, Q from, boolean nodesOnly, int steps){
    return toQ(lForward(context.eval(), from.eval().nodes(), nodesOnly, steps));
  }
  
  /**
   * Returns the forward traversal from the given nodes in the given context.
   * Optionally will return nodes only, and can be limited to traversals of a 
   * maximum length.
   * @param context
   * @param fromNode
   * @param steps
   * @return
   */
  public static Graph lForward(Graph context, Graph fromNodes, boolean nodesOnly, int steps) {
    return lForward(context, fromNodes.nodes(), nodesOnly, steps);
  }
  
  /**
   * Returns the forward traversal from the given nodes in the given context.
   * Optionally will return nodes only, and can be limited to traversals of a 
   * maximum length.
   * @param context
   * @param fromNode
   * @param steps
   * @return
   */
  public static Graph lForward(Graph context, AtlasSet<GraphElement> fromNodes, boolean nodesOnly, int steps) {
    AtlasHashSet<GraphElement> nodes = new AtlasHashSet<GraphElement>();
    AtlasHashSet<GraphElement> edges = new AtlasHashSet<GraphElement>();
    nodeWalk(context, fromNodes, nodesOnly, steps, nodes, edges, NodeDirection.OUT, EdgeDirection.TO);
    return new InducedGraph(nodes, edges);
  }
  
  /**
   * Returns the forward traversal from the given nodes in the given context.
   * Optionally will return nodes only, and can be limited to traversals of a 
   * maximum length.
   * @param context
   * @param fromNode
   * @param nodesOnly
   * @param steps
   * @return
   */
  public static Graph lForward(Graph context, GraphElement fromNode, boolean nodesOnly, int steps) {
    AtlasHashSet<GraphElement> fromNodes = new AtlasHashSet<GraphElement>();
    fromNodes.add(fromNode);
    return lForward(context, fromNodes, nodesOnly, steps);
  }
  
  /**
   * Non-recursive wrapper for recursive walk. Output is through reachedNodes and
   * reachedEdges parameters.
   * 
   * @param context
   * @param fromNode
   * @param steps
   * @param nodeDir
   * @param edgeDir
   * @return
   */
  private static void nodeWalk(Graph context, AtlasSet<GraphElement> fromNodes, boolean nodesOnly, int steps, AtlasSet<GraphElement> reachedNodes, 
                               AtlasSet<GraphElement> reachedEdges, NodeDirection nodeDir, EdgeDirection edgeDir) {
    // Recursively walk the context, building up the set of nodes reached.
    for(GraphElement fromNode : fromNodes){
      // If the origin is not in the context, the walk is empty
      if (!context.nodes().contains(fromNode)) continue; 
      nodeWalk(context, fromNode, nodesOnly, steps, reachedNodes, reachedEdges, nodeDir, edgeDir);
    }
  }
  
  /**
   * Recursive traversal. Output is through reachedNodes and reachedEdges parameters.
   * 
   * @param context
   * @param fromNode
   * @param steps
   * @param reached
   * @param nodeDir
   * @param edgeDir
   */
  private static void nodeWalk(Graph context, GraphElement fromNode, boolean nodesOnly, int steps, AtlasSet<GraphElement> reachedNodes, 
                               AtlasSet<GraphElement> reachedEdges, NodeDirection nodeDir, EdgeDirection edgeDir) {
    // Add this node to those nodes that were reached
    reachedNodes.add(fromNode);
    
    // Don't bother looking at the edges- we're out of steps
    if(steps == 0) return;
    
    // For each edge from this node
    AtlasSet<GraphElement> nextEdges = context.edges(fromNode,nodeDir);
    for (GraphElement edge : nextEdges) {
      // If we're keeping track of edges too, add it
      if(!nodesOnly) reachedEdges.add(edge);
      // Get the predecessor or successor node
      GraphElement n = edge.getNode(edgeDir);
      // If this node is not already walked, then walk it too
      if(!reachedNodes.contains(n)){
        nodeWalk(context, n, nodesOnly, steps-1, reachedNodes, reachedEdges, nodeDir, edgeDir);
      }
    }
  }
  
  /**
   * Performs a "step to" operation in the given edge context from the given node.
   * Returns those nodes reached, excluding the original node.
   * @param edgeContext
   * @param node
   * @return
   */
  public static Graph lStepTo(Graph edgeContext, GraphElement node){
    return lDifference(lForward(edgeContext, node, true, 1),node);
  }
  
  /**
   * Performs a "step to" operation in the given edge context from the given node.
   * Returns those nodes reached, excluding the original node.
   * @param edgeContext
   * @param node
   * @return
   */
  public static Graph lStepTo(Graph edgeContext, AtlasSet<GraphElement> nodes){
    AtlasSet<GraphElement> reachedNodes = lDifference(lForward(edgeContext, nodes, true, 1).nodes(),nodes);
    return new InducedGraph(reachedNodes, EmptyAtlasSet.<GraphElement>instance());
  }
  
  /**
   * Performs a "step from" operation in the given edge context from the given node.
   * Returns those nodes reached, excluding the original node.
   * @param edgeContext
   * @param node
   * @return
   */
  public static Graph lStepFrom(Graph edgeContext, GraphElement node){
    return lDifference(lReverse(edgeContext, node, true, 1),node);
  }
  
  /**
   * Performs a "step from" operation in the given edge context from the given node.
   * Returns those nodes reached, excluding the original node.
   * @param edgeContext
   * @param node
   * @return
   */
  public static Graph lStepFrom(Graph edgeContext, AtlasSet<GraphElement> nodes){
    AtlasSet<GraphElement> reachedNodes = lDifference(lReverse(edgeContext, nodes, true, 1).nodes(),nodes);
    return new InducedGraph(reachedNodes, EmptyAtlasSet.<GraphElement>instance());
  }
  
  /**
   * Performs a reverse then forward from the given nodes to find everything these nodes
   * are bi-directionally connected to in the edge context.
   * 
   * @param edgeContext
   * @param nodes
   * @return
   */
  public static Graph lBidirectionallyConnected(Graph edgeContext, AtlasSet<GraphElement> nodes){
    return lForward(edgeContext, lReverse(edgeContext, nodes, false, Integer.MAX_VALUE), false, Integer.MAX_VALUE);
  }
  
  /**
   * Similar to a normal step-to operation, except only the first reached node
   * is returned.
   * @param edgeContext
   * @param node
   * @return
   */
  public static GraphElement lFirstStepTo(Graph edgeContext, GraphElement node){
    GraphElement outEdge = edgeContext.edges(node, NodeDirection.OUT).getFirst();
    return outEdge.getNode(EdgeDirection.TO);
  }
  
  /**
   * Similar to a normal step-from operation, except only the first reached node
   * is returned.
   * @param edgeContext
   * @param node
   * @return
   */
  public static GraphElement lFirstStepFrom(Graph edgeContext, GraphElement node){
    GraphElement outEdge = edgeContext.edges(node, NodeDirection.IN).getFirst();
    return outEdge.getNode(EdgeDirection.FROM);
  }
}