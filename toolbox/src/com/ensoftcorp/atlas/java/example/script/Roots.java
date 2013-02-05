package com.ensoftcorp.atlas.java.example.script;

import com.ensoftcorp.atlas.java.core.db.graph.Graph;
import com.ensoftcorp.atlas.java.core.query.Attr;
import com.ensoftcorp.atlas.java.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.AtlasScript;
import com.ensoftcorp.atlas.java.core.script.Script;

/**
 * Finds potentially interesting roots of the call graph.
 * 
 * @author jmathews
 *
 */
public class Roots extends Script implements AtlasScript {

	@Override
	public Graph eval() {

		
		/*
		 * Roots of the call graph with at least one child.
		 */
		Q callg = edges(Attr.Edge.CALL);
		Q roots = callg.roots().difference(callg.leaves());
		
		Q g = roots;
		
		// add packages for clustering
		Q ext = extend(g, Attr.Edge.DECLARES);

		
		return ext.eval();
	}

	
}
