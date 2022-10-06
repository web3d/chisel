package com.trapezium.vrml.grammar;

import com.trapezium.vrml.Scene;
import com.trapezium.parse.TokenEnumerator;

/**
 *  Creates the scene graph component for a Spec node.
 *
 *  No longer in use.  
 *
 *           
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 16 Dec 1997
 *
 *  @since           1.0
 */
public class SpecRule {
    
    SpecNodeRule specNodeRule;
    
    public SpecRule( NodeRule nodeRule ) {
        specNodeRule = new SpecNodeRule( nodeRule );
    }
    
	/**
	 *  Apply the SpecRule to update the Spec object. 
	 */
	public void Build( TokenEnumerator v, Spec spec ) {
		GrammarRule.Enter( "SpecRule.Build" );

		while ( true ) {
			int tokenOffset = v.getNextToken();
			if ( tokenOffset == -1 ) {
				break;
			}
			specNodeRule.Build( tokenOffset, v, spec );
		}

		GrammarRule.Exit( "SpecRule.Build" );
	}
}

