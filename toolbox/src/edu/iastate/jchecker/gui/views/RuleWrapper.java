package edu.iastate.jchecker.gui.views;

import org.eclipse.jface.viewers.TableViewer;

import scala.collection.Iterator;
import scala.collection.mutable.ListBuffer;
import toolbox.script.Checker;

public class RuleWrapper {
	private String checker;
	private String source;
	private String dest;

	ListBuffer<ViolationWrapper> results;

	public RuleWrapper(String checker, String source, String dest) {
		this.setChecker(checker);
		this.setSource(source);
		this.setDest(dest);
	}

	public void run() {
		if (checker.equals(View.NULL_LITERAL)) {
			results = Checker.nullLiteralTest(null, false);
		} else {
			results = Checker.getTargetFlows(null, source, dest, false);
		}
	}

	public void postRun(TableViewer viewer) {
		Iterator<ViolationWrapper> iter = results.iterator();
		while (iter.hasNext()) {
			ViolationWrapper violation = iter.next();
			violation.setSourceAnnot(source);
			violation.setDestAnnot(dest);
			violation.setChecker(checker);
			violation.createTableItem(viewer.getTable());
		}
	}

	public String getChecker() {
		return checker;
	}

	public void setChecker(String checker) {
		this.checker = checker;
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
			if (source != null && dest != null) {
				return source.equals(r.getSource()) && dest.equals(r.getDest());
			} else {
				return checker.equals(r.getChecker());
			}
		} else {
			return false;
		}
	}
}
