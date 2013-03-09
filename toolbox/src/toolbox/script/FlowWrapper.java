package toolbox.script;

import java.util.List;

import com.ensoftcorp.atlas.java.core.query.Q;

public class FlowWrapper {

	private Q fullGraph;
	private Q highlightedSubgraph;
	private List<QColor> specialNodes;
	private List<QColor> specialEdges;

	/**
	 * @param fullGraph
	 * @param highlightedSubgraph
	 * @param specialNodes
	 * @param specialEdges
	 */
	public FlowWrapper(Q fullGraph, Q highlightedSubgraph, List<QColor> specialNodes, List<QColor> specialEdges) {
		this.fullGraph = fullGraph;
		this.highlightedSubgraph = highlightedSubgraph;
		this.specialNodes = specialNodes;
		this.specialEdges = specialEdges;
	}

	public Q getFullGraph() {
		return fullGraph;
	}

	public void setFullGraph(Q fullGraph) {
		this.fullGraph = fullGraph;
	}

	public Q getHighlightedSubgraph() {
		return highlightedSubgraph;
	}

	public void setHighlightedSubgraph(Q highlightedSubgraph) {
		this.highlightedSubgraph = highlightedSubgraph;
	}

	public List<QColor> getSpecialNodes() {
		return specialNodes;
	}

	public void setSpecialNodes(List<QColor> specialNodes) {
		this.specialNodes = specialNodes;
	}

	public List<QColor> getSpecialEdges() {
		return specialEdges;
	}

	public void setSpecialEdges(List<QColor> specialEdges) {
		this.specialEdges = specialEdges;
	}

	@Override
	public String toString() {
		// TODO: make this useful
		return super.toString();
	}
}
