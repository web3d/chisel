package com.trapezium.vrml.node.generated;

import com.trapezium.vrml.fields.*;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.NodeType;

//
// WARNING:  generated code, do not edit
// Copyright 1997, Trapezium Development
//


public class Transform extends GroupingNode {

	/** constructor */
	public Transform() {
		super();
	}
	// override default false return in Node
	public boolean isRelocatable() {
		return( true );
	}
	// override default false return in Node
	public boolean acceptsAnimation() {
		return( true );
	}
	// override default false return in Node
	public boolean acceptsTrigger() {
		return( true );
	}
	// override default false return in Node
	public boolean isTriggerable() {
		return( true );
	}
	public String getNodeName() {
	    return( "Transform" );
	}
}
