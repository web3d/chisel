package com.trapezium.vrml.grammar;

import com.trapezium.vrml.Scene;
import com.trapezium.vrml.LeftBrace;
import com.trapezium.vrml.RightBrace;
import com.trapezium.vrml.NodeTypeId;
import com.trapezium.parse.TokenEnumerator;

/**
 * Process the file we are using to generate built in node classes.  This is similar, but
 * not identical to ".wrl" file processing.  The "Spec" object is built as follows:
 *
 *            Spec s = new Spec();
 *            SpecRule.Build( v, s );
 *
 * The spec grammar is:
 *
 *  spec:
 *    specNode
 *
 *  specNode:
 *    Category categoryName { interfaceDeclarations* }
 *    nodeType { interfaceDeclarations* }
 *
 * When the "Spec" object has been created, we create a visitor to visit each node, and 
 * generate code based on the interfaceDeclarations and the embedded controls (tips, etc.)
 */

class SpecNodeRule {
	/** keep track of category based on where we are in the processing */
	String mostRecentCategory = null;
	InterfaceDeclarationRule interfaceDeclarationRule;

	public SpecNodeRule( NodeRule nodeRule ) {
	    interfaceDeclarationRule = new InterfaceDeclarationRule( nodeRule );
	}

	/**
	 *  Apply the SpecRule to update the Spec object. 
	 */
	public void Build( int tokenOffset, TokenEnumerator v, Spec spec ) {
		GrammarRule.Enter( "SpecNodeRule.Build" );

		SpecNode s = new SpecNode();
		spec.addChild( s );
		NodeTypeId nodeTypeId = new NodeTypeId( tokenOffset, v );
		s.addChild( nodeTypeId );
		if ( nodeTypeId.getName().compareTo( "Category" ) == 0 ) {
			tokenOffset = v.getNextToken();
			mostRecentCategory = v.toString( tokenOffset );
		}
		s.setCategory( mostRecentCategory );
		s.addChild( new LeftBrace( v ));
		while ( true ) {
			tokenOffset = v.getNextToken();
			if ( v.sameAs( tokenOffset, "}" )) {
				break;
			}
			interfaceDeclarationRule.Build( tokenOffset, v, null, s );
		}
		s.addChild( new RightBrace( tokenOffset, v ));
		GrammarRule.Exit( "SpecNodeRule.Build" );
	}
}

