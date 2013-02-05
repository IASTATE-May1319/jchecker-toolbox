package com.ensoftcorp.atlas.java.example.script;

import com.ensoftcorp.atlas.java.core.db.graph.Graph;
import com.ensoftcorp.atlas.java.core.query.Attr;
import com.ensoftcorp.atlas.java.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.AtlasScript;
import com.ensoftcorp.atlas.java.core.script.Script;

/**
 * Shows type hierarchy.  Note that (at present) this includes
 * both interfaces and classes without distinguishing between them.
 * 
 * @author jmathews
 *
 */
public class Subclasses extends Script implements AtlasScript {

	@Override
	public Graph eval() {

		Q allTypes = universe().selectNode(Attr.Node.KIND, Attr.Node.TYPE);
		Q supertypeEdges = edges(Attr.Edge.SUPERTYPE);
		
		Q typeHierarchy = supertypeEdges.between(allTypes, allTypes);

		return typeHierarchy.eval();
	}

}
