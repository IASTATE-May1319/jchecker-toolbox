package com.ensoftcorp.atlas.java.example.script;

import com.ensoftcorp.atlas.java.core.db.graph.Graph;
import com.ensoftcorp.atlas.java.core.query.Attr;
import com.ensoftcorp.atlas.java.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.AtlasScript;
import com.ensoftcorp.atlas.java.core.script.Script;

/**
 * 
 * Immediate neighbors of an entity.
 * 
 * @author jmathews
 *
 */
public class Neighbors extends Script implements AtlasScript {

	@Override
	public Graph eval() {
		
		/*
		 * Selects a method by the name of the type and the name of the method;
		 * note that this may return multiple methods as the type is not
		 * qualified by the package name, nor the method qualified by its
		 * signature.
		 */
		Q entity = methodSelect("Connection", "authenticatePassword");

		/*
		 * Along any edge in the universe...
		 */
		Q u = universe();
		
		Q neighbors = neighbors(u, entity);

		/*
		 * Given the neighbor graph, add in the parent structures along
		 * 'declares' edges; when displayed, these will appear as containers,
		 * giving additional context for the methods.
		 */
		
		Q declGraph = extend(neighbors, Attr.Edge.DECLARES);
				
		return declGraph.eval();
	}

	
}
