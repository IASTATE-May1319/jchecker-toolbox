package com.ensoftcorp.atlas.java.example.handlers;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.java.core.db.graph.Graph;
import com.ensoftcorp.atlas.java.core.highlight.H;
import com.ensoftcorp.atlas.java.core.script.AtlasScript;
import com.ensoftcorp.atlas.java.example.script.ReverseCallGraph;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;

public class ReverseCallGraphHandler extends AbstractQueryHandler {

	protected void runQuery(IProgressMonitor monitor) {
		AtlasScript rc = new ReverseCallGraph();
		Graph g =rc.eval();
		H h = rc.getHighlighter();
		DisplayUtil.displayGraph(g, h);
	}
	
}
