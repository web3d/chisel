package com.trapezium.vrml.grammar;

import com.trapezium.vrml.VrmlElementContainer;

/**
 *  The scene graph component for a Spec node.
 *
 *  Spec nodes were used to generate source files in package
 *  com.trapezium.vrml.node.generated.  Re-architecture due to OutOfMemory
 *  problem made generated nodes much less complex, probably should 
 *  eliminate these eventually. 
 *
 *  Note:  spec nodes & code generation no longer works.  Probably need
 *  to drop this altogether.
 *           
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 25 Nov 1997
 *
 *  @since           1.0
 */
public class Spec extends VrmlElementContainer {
	public Spec() {
		super( -1 );
	}
}

