package com.ensoftcorp.atlas.java.example.script;

import com.ensoftcorp.atlas.java.core.db.graph.Graph;
import com.ensoftcorp.atlas.java.core.query.Attr;
import com.ensoftcorp.atlas.java.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.AtlasScript;
import com.ensoftcorp.atlas.java.core.script.Script;

/**
 * 
 * Immediate call relations of all methods in Connection.
 * 
 * @author jmathews
 *
 */
public class ImmediateCalls extends Script implements AtlasScript {

	@Override
	public Graph eval() {
		
		Q entity = typeSelect("com.trilead.ssh2", "Connection");
		entity = entity.union(methodsOf(entity));

		Q edges = edges(Attr.Edge.CALL);

		Q neighbors = neighbors(edges, entity);
		Q n = extend(neighbors, Attr.Edge.DECLARES);

				
		return n.eval();
	}

}
