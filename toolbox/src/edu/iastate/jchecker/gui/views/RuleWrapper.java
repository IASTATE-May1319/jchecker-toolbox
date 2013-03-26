package edu.iastate.jchecker.gui.views;

import scala.collection.Iterator;
import scala.collection.mutable.ListBuffer;
import toolbox.script.Checker;

public class RuleWrapper {
	private String checker;
	private String source;
	private String destination;
	private ListBuffer<ViolationWrapper> results;

	/**
	 * Create a new rule wrapper
	 * 
	 * @param checker
	 *            - The checker for this rule
	 * @param source
	 *            - The source annotation for this rule
	 * @param destination
	 *            - The destination annotation for this rule
	 */
	public RuleWrapper(String checker, String source, String destination) {
		this.setChecker(checker);
		this.setSourceAnnotation(source);
		this.setDestinationAnnotation(destination);
	}

	/**
	 * Execute this rule and return a list of violations (evidence of rule
	 * infringement)
	 * 
	 * @return The list of violations of this rule
	 */
	public ListBuffer<ViolationWrapper> run() {
		if (checker.equals(View.NULL_LITERAL)) {
			results = Checker.nullLiteralTest(null, false);
		} else {
			results = Checker.getTargetFlows(null, source, destination, false);
		}
		Iterator<ViolationWrapper> iter = results.iterator();
		while (iter.hasNext()) {
			ViolationWrapper violation = iter.next();
			violation.setSourceAnnotation(source);
			violation.setDestinationAnnotation(destination);
			violation.setChecker(checker);
		}
		return results;
	}

	/**
	 * @return The checker for this rule
	 */
	public String getChecker() {
		return checker;
	}

	/**
	 * Set the checker for this rule
	 * 
	 * @param checker
	 */
	public void setChecker(String checker) {
		this.checker = checker;
	}

	/**
	 * @return The source annotation for this rule
	 */
	public String getSourceAnnotation() {
		return source;
	}

	/**
	 * Set the source annotation for this rule
	 * 
	 * @param source
	 */
	public void setSourceAnnotation(String source) {
		this.source = source;
	}

	/**
	 * @return The destination annotation for this rule
	 */
	public String getDestinationAnnotation() {
		return destination;
	}

	/**
	 * Set the destination annotation for this rule
	 * 
	 * @param destination
	 */
	public void setDestinationAnnotation(String destination) {
		this.destination = destination;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(checker);
		sb.append(": ");
		sb.append(source);
		sb.append(" --> ");
		sb.append(destination);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		if (destination == null && source == null) {
			return checker.hashCode();
		} else {
			return (checker + '/' + source + '/' + destination).hashCode();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o.getClass() == this.getClass()) {
			RuleWrapper r = (RuleWrapper) o;
			if (source != null && destination != null) {
				return checker.equals(r.getChecker()) && source.equals(r.getSourceAnnotation())
						&& destination.equals(r.getDestinationAnnotation());
			} else {
				return checker.equals(r.getChecker());
			}
		} else {
			return false;
		}
	}
}
