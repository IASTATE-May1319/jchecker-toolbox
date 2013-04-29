package edu.iastate.jchecker.gui.views;

import java.util.List;


import com.ensoftcorp.atlas.java.core.query.Q;

import edu.iastate.jchecker.toolbox.util.QColor;

/**
 * @author Jay
 * 
 */
public class ViolationWrapper {

	private Q fullGraph;
	private Q highlightedSubgraph;
	private List<QColor> specialNodes;
	private List<QColor> specialEdges;
	private String project;
	private String source;
	private String destination;
	private String sourceAnnotation;
	private String destinationAnnotation;
	private String checker;

	/**
	 * Create a new ViolationWrapper
	 * 
	 * @param fullGraph
	 *            - The entire envelope containing this violation
	 * @param highlightedSubgraph
	 *            - The highlighted subgraph of this violation
	 * @param specialNodes
	 *            - Nodes that should be highlighted in unique ways
	 * @param specialEdges
	 *            - Edges that should be highlighted in unique ways
	 * @param project
	 *            - The project containing this violation
	 * @param source
	 *            - The source node for this violation
	 * @param destination
	 *            - The destination node for this violation
	 */
	public ViolationWrapper(Q fullGraph, Q highlightedSubgraph, List<QColor> specialNodes, List<QColor> specialEdges,
			Object project, Object source, Object destination) {
		this.fullGraph = fullGraph;
		this.highlightedSubgraph = highlightedSubgraph;
		this.specialNodes = specialNodes;
		this.specialEdges = specialEdges;
		this.project = (String) project;
		this.source = (String) source;
		this.destination = (String) destination;
	}

	/**
	 * @return The entire envelope containing this violation
	 */
	public Q getFullGraph() {
		return fullGraph;
	}

	/**
	 * Set the envelope for this violation
	 * 
	 * @param fullGraph
	 */
	public void setFullGraph(Q fullGraph) {
		this.fullGraph = fullGraph;
	}

	/**
	 * @return The highlighted subgraph of this violation
	 */
	public Q getHighlightedSubgraph() {
		return highlightedSubgraph;
	}

	/**
	 * Set the highlighted subgraph of this violation
	 * 
	 * @param highlightedSubgraph
	 */
	public void setHighlightedSubgraph(Q highlightedSubgraph) {
		this.highlightedSubgraph = highlightedSubgraph;
	}

	/**
	 * @return The nodes that should be highlighted in unique ways
	 */
	public List<QColor> getSpecialNodes() {
		return specialNodes;
	}

	/**
	 * Set the nodes to be highlighted in unique ways
	 * 
	 * @param specialNodes
	 */
	public void setSpecialNodes(List<QColor> specialNodes) {
		this.specialNodes = specialNodes;
	}

	/**
	 * @return The edges that should be highlighted in unique ways
	 */
	public List<QColor> getSpecialEdges() {
		return specialEdges;
	}

	/**
	 * Set the edges that should be highlighted in unique ways
	 * 
	 * @param specialEdges
	 */
	public void setSpecialEdges(List<QColor> specialEdges) {
		this.specialEdges = specialEdges;
	}

	/**
	 * @return The project containing this violation
	 */
	public String getProject() {
		return project;
	}

	/**
	 * Set the project containing this violation
	 * 
	 * @param project
	 */
	public void setProject(String project) {
		this.project = project;
	}

	/**
	 * @return The source node for this violation
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Set the source node for this violation
	 * 
	 * @param source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return The destination node for this violation
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * Set the destination node for this violation
	 * 
	 * @param destination
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * @return The source annotation for this violation
	 */
	public String getSourceAnnotation() {
		return sourceAnnotation;
	}

	/**
	 * Set the source annotation for this violation
	 * 
	 * @param sourceAnnotation
	 */
	public void setSourceAnnotation(String sourceAnnotation) {
		this.sourceAnnotation = sourceAnnotation;
	}

	/**
	 * @return The destination annotation for this violation
	 */
	public String getDestinationAnnotation() {
		return destinationAnnotation;
	}

	/**
	 * Set the destination annotation for this violation
	 * 
	 * @param destinationAnnotation
	 */
	public void setDestinationAnnotation(String destinationAnnotation) {
		this.destinationAnnotation = destinationAnnotation;
	}

	/**
	 * @return The checker for this violation
	 */
	public String getChecker() {
		return checker;
	}

	/**
	 * Set the checker for this violation
	 * 
	 * @param checker
	 */
	public void setChecker(String checker) {
		this.checker = checker;
	}
}
