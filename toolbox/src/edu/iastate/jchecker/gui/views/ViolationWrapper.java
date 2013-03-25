package edu.iastate.jchecker.gui.views;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import toolbox.script.util.QColor;

import com.ensoftcorp.atlas.java.core.query.Q;

public class ViolationWrapper {

	private Q fullGraph;
	private Q highlightedSubgraph;
	private List<QColor> specialNodes;
	private List<QColor> specialEdges;
	private Map<String, Object> metaData;

	/**
	 * @param fullGraph
	 * @param highlightedSubgraph
	 * @param specialNodes
	 * @param specialEdges
	 * @param metaData
	 */
	public ViolationWrapper(Q fullGraph, Q highlightedSubgraph, List<QColor> specialNodes, List<QColor> specialEdges,
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

	public Map<String, Object> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}

	public String getSourceAnnot() {
		return (String) metaData.get("sourceAnnot");
	}

	public void setSourceAnnot(String source) {
		metaData.put("sourceAnnot", source);
	}

	public String getDestAnnot() {
		return (String) metaData.get("destAnnot");
	}

	public void setDestAnnot(String dest) {
		metaData.put("destAnnot", dest);
	}

	public void setChecker(String checker) {
		metaData.put("checker", checker);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(metaData.get("sourceAnnot"));
		if (metaData.get("destAnnot") != null) {
			sb.append('/');
			sb.append(metaData.get("destAnnot"));
		}
		sb.append(") ");
		sb.append(metaData.get("project"));
		sb.append(": ");
		sb.append(metaData.get("source"));
		sb.append(" --> ");
		sb.append(metaData.get("dest"));
		return sb.toString();
	}

	public TableItem createTableItem(Table parent) {
		TableItem item = new TableItem(parent, SWT.NONE);
		if (metaData.get("checker") != null) {
			item.setText(0, (String) metaData.get("checker"));
		}
		if (metaData.get("sourceAnnot") != null) {
			item.setText(1, (String) metaData.get("sourceAnnot"));
		}
		if (metaData.get("destAnnot") != null) {
			item.setText(2, (String) metaData.get("destAnnot"));
		}
		item.setText(3, (String) metaData.get("project"));
		item.setText(4, (String) metaData.get("source"));
		item.setText(5, (String) metaData.get("dest"));
		item.setData(this);
		return item;
	}
}
