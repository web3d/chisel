package com.trapezium.vrml.node.generated;

import com.trapezium.vrml.fields.*;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.NodeType;

//
// WARNING:  generated code, do not edit
// Copyright 1997, Trapezium Development
//


public class IndexedFaceSet extends Geometry {

    int numberFaces;
    
	/** constructor */
	public IndexedFaceSet() {
		super();
	}
	public String getNodeName() {
	    return( "IndexedFaceSet" );
	}
	
	public void setNumberFaces( int numberFaces ) {
	    this.numberFaces = numberFaces;
	}
	
	public int getNumberFaces() {
	    return( numberFaces );
	}
}
