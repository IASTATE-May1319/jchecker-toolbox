package com.ensoftcorp.atlas.java.example.handlers;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.java.core.db.graph.Graph;
import com.ensoftcorp.atlas.java.core.highlight.H;
import com.ensoftcorp.atlas.java.core.script.AtlasScript;
import com.ensoftcorp.atlas.java.example.script.SubclassesOf;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;

public class YourHandler extends AbstractQueryHandler {

	protected void runQuery(IProgressMonitor monitor) {
		AtlasScript s = new SubclassesOf();
		Graph g = s.eval();
		H h = s.getHighlighter();

		DisplayUtil.displayGraph(g, h);
	}
	
}
