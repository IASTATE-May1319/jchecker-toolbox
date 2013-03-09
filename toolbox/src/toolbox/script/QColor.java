package toolbox.script;

import java.awt.Color;

import com.ensoftcorp.atlas.java.core.query.Q;

public class QColor {

	private Q query;
	private Color color;

	/**
	 * @param query
	 * @param color
	 */
	public QColor(Q query, Color color) {
		this.query = query;
		this.color = color;
	}

	public Q getQuery() {
		return query;
	}

	public void setQuery(Q query) {
		this.query = query;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
