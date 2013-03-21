package toolbox.script.util;
import static toolbox.script.util.LightTraversals.lDifference;
import static toolbox.script.util.LightTraversals.lForward;
import static toolbox.script.util.LightTraversals.lIntersection;
import static toolbox.script.util.LightTraversals.lReverse;
import static toolbox.script.util.LightTraversals.lStepFrom;
import static toolbox.script.util.LightTraversals.lStepTo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.ensoftcorp.atlas.java.core.db.graph.EdgeGraph;
import com.ensoftcorp.atlas.java.core.db.graph.Graph;
import com.ensoftcorp.atlas.java.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.java.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.java.core.db.graph.operation.InducedGraph;
import com.ensoftcorp.atlas.java.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.java.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.java.core.db.set.EmptyAtlasSet;
import com.ensoftcorp.atlas.java.core.query.Attr.Edge;
import com.ensoftcorp.atlas.java.core.query.Attr.Node;

public class SensitivityUtils {

  /**
   * Returns the call context sensitive forward or reverse data flow from the starting
   * point in the given context.
   * 
   * @param context
   * @param from
   * @param forward
   * @return
   */
  public static Graph sensitiveFlow(Graph context, Graph from, boolean forward){
    // Edge contexts for later use
    Graph decContext = new EdgeGraph(context.edges().taggedWithAny(Edge.DECLARES));
    Graph dfLocalContext = new EdgeGraph(context.edges().taggedWithAny(Edge.DF_LOCAL));
    Graph dfInterContext = new EdgeGraph(context.edges().taggedWithAny(Edge.DF_INTERPROCEDURAL));

    // The current state of the flow that is being constructed
    AtlasHashSet<GraphElement> edges = new AtlasHashSet<GraphElement>();
    AtlasHashSet<GraphElement> nodes = new AtlasHashSet<GraphElement>(from.nodes());
    
    // The state of the flow at the last iteration
    AtlasHashSet<GraphElement> oldEdges = new AtlasHashSet<GraphElement>();
    AtlasHashSet<GraphElement> oldNodes = new AtlasHashSet<GraphElement>();
  
    // A history of method call sites, built throughout the traversal.  Its keys 
    // are callsites, and the values are methods
    HashMap<GraphElement, HashSet<GraphElement>> contextHistory = new HashMap<GraphElement, HashSet<GraphElement>>();
    
    // Stack variables used inside the loops below, allocated here for efficiency
    Graph lcFlow, ipFlow;
    boolean destIsField, destIsParam, originIsField, originIsReturn, addEdge;
    GraphElement callsite = null, dfNode, otherNode, method;
    
    // Continue adding to the sensitive flow until nothing new is added
    do{
      // Set old state equal to current state
      oldEdges.addAll(edges);
      oldNodes.addAll(nodes);
      
      // From the current nodes, fully traverse the DF_LOCAL edges
      lcFlow = forward ? lForward(dfLocalContext, nodes, false, Integer.MAX_VALUE):
                         lReverse(dfLocalContext, nodes, false, Integer.MAX_VALUE);
      nodes.addAll(lcFlow.nodes());
      edges.addAll(lcFlow.edges());

      // From the current nodes, traverse the DF_INTERPROCEDURAL nodes 1 step
      ipFlow = forward ? lForward(dfInterContext, nodes, false, 1):
                         lReverse(dfInterContext, nodes, false, 1);
   
      // Scrutinize each new IP edge to see if we should add it
      AtlasSet<GraphElement> toExamine = lDifference(ipFlow.edges(), edges);
      for(Iterator<GraphElement> it = toExamine.iterator(); it.hasNext();){
        // The edge and the origin and destination nodes
        GraphElement edge = it.next();
        GraphElement origin = edge.getNode(EdgeDirection.FROM);
        GraphElement dest = edge.getNode(EdgeDirection.TO);
        
        // Test what the origin and destination are
        destIsField = dest.tags().contains(Node.FIELD);
        destIsParam = dest.tags().contains(Node.PARAMETER);
        originIsField = origin.tags().contains(Node.FIELD);
        originIsReturn = Boolean.TRUE.equals(origin.attr().get(Node.IS_MASTER_RETURN));
        dfNode = destIsField || destIsParam ? origin:dest;
        otherNode = dfNode==dest ? origin:dest;
        callsite = firstDeclarator(decContext, dfNode, Node.CONTROL_FLOW);
        
        // Don't add this edge until we can prove that we should
        addEdge = false;
        
        // If this is a read or write to a field
        if(destIsField || originIsField){
          addEdge = true;
        }
        // Else if the destination is a parameter or return value
        else if(destIsParam || originIsReturn){
          // Get the method and whether it's static
          method = firstDeclarator(decContext, otherNode, Node.METHOD);

          // If we're on the "forward" side of the traversal
          if((destIsParam && forward) ||
             (originIsReturn && !forward)){
            // Then we'll keep the edge
            addEdge = true;
            
            // Record the call context history
            addToHistory(contextHistory, callsite, method);
          }
          // Else if we're on the "trailing" side of the traversal, and the object
          // instance passed is consistent with the object instance history, and
          // the call context is consistent with the call context history
          else if(isInHistory(contextHistory, callsite, method) || 
                  (nodes.contains(otherNode) && !isInAnyHistory(contextHistory, method))){
            // Then we'll keep the edge
            addEdge = true;
          }
        }
        
        // If we have decided to keep the edge, then do so
        if(addEdge){
          nodes.add(forward ? dest:origin);
          edges.add(edge);
        }
      }
    // Repeat this process until nothing new is added
    }while(oldNodes.size()!=nodes.size() || oldEdges.size() != edges.size());
    
    // Return our sensitively-constructed flow
    return new InducedGraph(nodes, edges);
  }
  
  /**
   * Given a control flow block (corresponds to a call site) and a method node,
   * adds the method node block to the CF block's history.
   * @param whitelist
   * @param cfBlock
   * @param other
   */
  private static void addToHistory(HashMap<GraphElement, HashSet<GraphElement>> whitelist, GraphElement cfBlock, GraphElement other){
    HashSet<GraphElement> listed = whitelist.get(cfBlock);
    if(listed == null){
      listed = new HashSet<GraphElement>();
      whitelist.put(cfBlock, listed);
    }
    listed.add(other);
  }
  
  /**
   * Checks to see if the other element exists in the given CF block's whitelist.
   * @param history
   * @param cfBlock
   * @param other
   * @return
   */
  private static boolean isInHistory(HashMap<GraphElement, HashSet<GraphElement>> history, GraphElement cfBlock, GraphElement other){
    HashSet<GraphElement> listed = history.get(cfBlock);
    return !(listed == null) && listed.contains(other);
  }
  
  /**
   * Checks whether the other element exists in any CF block's whitelist.
   * @param history
   * @param other
   * @return
   */
  private static boolean isInAnyHistory(HashMap<GraphElement, HashSet<GraphElement>> history, GraphElement other){
    // Go through each CF block's white list
    for(Iterator<HashSet<GraphElement>> it = history.values().iterator(); it.hasNext();){
      if(it.next().contains(other)) return true;
    }
    return false;
  }
  
  /**
   * Given a GraphElement in a View, walks declares backwards until the container
   * node of the given type is found. If none is found, returns null (this should never happen)
   * @param view
   * @param block
   * @return
   */
  private static GraphElement firstDeclarator(Graph declaresContext, GraphElement block, String type){
    if(block.tags().contains(type)) return block;

    AtlasSet<GraphElement> blockSet = new AtlasHashSet<GraphElement>();
    blockSet.add(block);
    Graph blockGraph = new InducedGraph(blockSet, EmptyAtlasSet.<GraphElement>instance());
    
    while(true){
      // Walk the declares edges back one step
      blockGraph = lDifference(lReverse(declaresContext, blockGraph, true, 1),blockGraph);
      
      // If no nodes are in the result, then we reached the end of the road without finding
      // a method. (This should never happen)
      if(blockGraph.nodes().size() == 0) return null;
      
      // Should definitely only be one node in this set. Can't be declared by multiple
      // things.
      GraphElement nextBlock = blockGraph.nodes().getFirst();
      
      if(nextBlock.tags().contains(type)) return nextBlock;
    }
  }
}