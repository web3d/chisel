package com.trapezium.vrml.grammar;

import com.trapezium.vrml.NodeTypeId;
import com.trapezium.vrml.node.Node;

/**
 *  Creates the scene graph component for a Spec node.
 *
 *  Spec nodes contained information read from a file describing
 *  VRML built in nodes.  
 *
 *  Note:  No longer in use.
 *           
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 14 Jan 1998
 *
 *  @since           1.0
 */
public class SpecNode extends Node {
	public String category;
	public String vrmlConfig;
	boolean animatable;
	boolean triggerSource;
	boolean triggerDestination;
	boolean relocatable;
	String tip;

	public void setTip( String s ) {
		tip = s;
	}

	public String getTip() {
		return( tip );
	}

	public SpecNode() {
		super( -1 );
		animatable = false;
		triggerSource = false;
		triggerDestination = false;
		relocatable = false;
	}

	public void canBeRelocated() {
		relocatable = true;
	}

	public boolean isRelocatable() {
		return( relocatable );
	}

	public void canBeUsedAsTrigger() {
		triggerSource = true;
	}

	public boolean isPossibleTrigger() {
		return( triggerSource );
	}

	public void canBeAnimated() {
		animatable = true;
	}

	public boolean isAnimatable() {
		return( animatable );
	}

	public void canBeTriggered() {
		triggerDestination = true;
	}

	public boolean isTriggerable() {
		return( triggerDestination );
	}

	public void setCategory( String c ) {
		category = c;
	}

	public String getCategory() {
		return( category );
	}

	// VrmlConfig is a String for now.  What it really is, is a way to describe the 
	// configuration of existing PROTOs or built in VRML nodes in a way that implements
	// a specific PlanetFactory node type.  For now, I'm using this only for MouseOverTrigger,
	// MouseClickTrigger, a few other triggers, which only need to map their object name
	// into the corresponding VRML sensor.
	public void setVrmlConfig( String s ) {
		vrmlConfig = s;
		System.out.println( "SpecNode set vrmlconfig to '" + vrmlConfig + "'" );
	}

	public String getVrmlConfig() {
		return( vrmlConfig );
	}

	String fieldSource = null;
	public void setFieldSource( String s ) {
		fieldSource = s;
	}

	public String getFieldSource() {
		return( fieldSource );
	}

	public String getType() {
	    int nChildren = numberChildren();
		for ( int i = 0; i < nChildren; i++ ) {
			Object a = getChildAt( i );
			if ( a instanceof NodeTypeId ) {
				NodeTypeId n = (NodeTypeId)a;
				return( n.getName() );
			}
		}
		return( null );
	}

	public boolean isCategory() {
		return( getType().compareTo( "Category" ) == 0 );
	}

	public String classString() {
		if ( isCategory() ) {
			return( category );
		} else {
			return( getType() );
		}
	}

	public String superClassString() {
		if ( isCategory() ) {
			return( "Node" );
		} else {
			return( category );
		}
	}

	public String superClassGutsString() {
		if ( isCategory() ) {
			return( "File" );
		} else {
			return( category );
		}
	}
}
