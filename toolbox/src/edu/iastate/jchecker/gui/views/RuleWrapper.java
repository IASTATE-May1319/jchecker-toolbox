package edu.iastate.jchecker.gui.views;

import org.eclipse.jface.viewers.TableViewer;

import scala.collection.Iterator;
import scala.collection.mutable.ListBuffer;
import toolbox.script.FlowWrapper;
import toolbox.script.TargetFlowChecker;

public class RuleWrapper {
	private String source;
	private String dest;

	ListBuffer<FlowWrapper> results;

	public RuleWrapper(String source, String dest) {
		this.setSource(source);
		this.setDest(dest);
	}

	public void run(TableViewer viewer) {
		if (source == SampleView.NULL_LITERAL) {
			results = TargetFlowChecker.nullLiteralTest(null, false);
		} else {
			results = TargetFlowChecker.getTargetFlows(null, source, dest, false);
		}
	}

	public void postRun(TableViewer viewer) {
		Iterator<FlowWrapper> iter = results.iterator();
		while (iter.hasNext()) {
			FlowWrapper flow = iter.next();
			flow.setSourceAnnot(source);
			if (dest != null) {
				flow.setDestAnnot(dest);
			}
			viewer.add(flow);
		}
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the dest
	 */
	public String getDest() {
		return dest;
	}

	/**
	 * @param dest
	 *            the dest to set
	 */
	public void setDest(String dest) {
		this.dest = dest;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(source);
		if (dest != null) {
			sb.append(" --> ");
			sb.append(dest);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		if (dest == null && source != null) {
			return (source + "/").hashCode();
		} else {
			return (source + '/' + dest).hashCode();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o.getClass() == this.getClass()) {
			RuleWrapper r = (RuleWrapper) o;
			if (dest != null) {
				return source.equals(r.getSource()) && dest.equals(r.getDest());
			} else {
				return source.equals(r.getSource());
			}
		} else {
			return false;
		}
	}
}
