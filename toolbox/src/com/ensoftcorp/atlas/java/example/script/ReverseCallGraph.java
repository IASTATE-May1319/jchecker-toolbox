package com.ensoftcorp.atlas.java.example.script;

import java.awt.Color;

import com.ensoftcorp.atlas.java.core.db.graph.Graph;
import com.ensoftcorp.atlas.java.core.highlight.H;
import com.ensoftcorp.atlas.java.core.query.Attr;
import com.ensoftcorp.atlas.java.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.AtlasScript;
import com.ensoftcorp.atlas.java.core.script.Script;

/**
 * Basic reverse call graph, extended with declaration structure.
 * 
 * @author jmathews
 *
 */
public class ReverseCallGraph extends Script implements AtlasScript {

	@Override
	public Graph eval() {

		/*
		 * Selects a method by the name of the type and the name of the method;
		 * note that this may return multiple methods as the type is not
		 * qualified by the package name, nor the method qualified by its
		 * signature.
		 */
		Q entity = methodSelect("Encryptor", "encrypt");
		

		/* From the set of call edges... */
		Q callg = edges(Attr.Edge.CALL);
		/*
		 * ...calculate what is reachable starting from the given entity along
		 * the call edges.
		 */
		Q rev = callg.reverse(entity);
		
		/*
		 * Make the origin node orange, and the rest of the call graph green.
		 * The origin is part of the reverse call graph, but the highlighter conflict
		 * strategy will use the first color specified.
		 */
		H h = getHighlighter();
		h.highlight(entity, Color.ORANGE);
		h.highlight(rev, Color.GREEN);
		

		/*
		 * Given the reverse call graph, add in the parent structures along
		 * 'declares' edges; when displayed, these will appear as containers,
		 * giving additional context for the methods.
		 */
		Q declCallg = extend(rev, Attr.Edge.DECLARES);

		return declCallg.eval();
	}

}
