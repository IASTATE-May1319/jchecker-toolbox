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
public class SubclassesOf extends Script implements AtlasScript {

	@Override
	public Graph eval() {

		// assuming that the Android SDK source is in the workspace so it will be indexed
		Q entity = typeSelect("android.app", "Activity");
		
		Q typeHierarchy = subclassesOf(entity);
		
		// add packages for clustering
		Q ext = extend(typeHierarchy, Attr.Edge.DECLARES);

		
		return ext.eval();
	}

	private Q subclassesOf(Q types) {
		Q supertypeEdges = edges(Attr.Edge.SUPERTYPE);
		Q typeHierarchy = supertypeEdges.reverse(types);
		return typeHierarchy;
	}
	
}
