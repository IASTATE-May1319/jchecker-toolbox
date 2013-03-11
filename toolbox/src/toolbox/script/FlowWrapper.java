package toolbox.script;

import java.util.List;
import java.util.Map;

import com.ensoftcorp.atlas.java.core.query.Q;

public class FlowWrapper {

	private Q fullGraph;
	private Q highlightedSubgraph;
	private List<QColor> specialNodes;
	private List<QColor> specialEdges;
	private Map metaData;

	/**
	 * @param fullGraph
	 * @param highlightedSubgraph
	 * @param specialNodes
	 * @param specialEdges
	 * @param metaData
	 */
	public FlowWrapper(Q fullGraph, Q highlightedSubgraph, List<QColor> specialNodes, List<QColor> specialEdges,
			Map<String, Object> metaData) {
		this.fullGraph = fullGraph;
		this.highlightedSubgraph = highlightedSubgraph;
		this.specialNodes = specialNodes;
		this.specialEdges = specialEdges;
		this.metaData = metaData;
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

	public Map getMetaData() {
		return metaData;
	}

	public void setMetaData(Map metaData) {
		this.metaData = metaData;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(metaData.get("id"));
		sb.append(") ");
		sb.append(metaData.get("project"));
		sb.append(": ");
		sb.append(metaData.get("source"));
		sb.append(" --> ");
		sb.append(metaData.get("dest"));
		return sb.toString();
	}
}
