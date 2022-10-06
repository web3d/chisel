package com.trapezium.vrml.node.space;

import java.util.Vector;

public class DiagonalizeInfo {
	boolean faceDiagonalized;
	Vector vertexList;

	public DiagonalizeInfo() {
		faceDiagonalized = false;
		vertexList = new Vector();
	}

	public boolean diagonalized() {
		return( faceDiagonalized );
	}

	public void markAsDiagonalized() {
		faceDiagonalized = true;
	}

	public void saveVertex( SpacePrimitive v ) {
		vertexList.addElement( v );
	}

	public boolean hasVertex( SpacePrimitive v ) {
		return( vertexList.indexOf( v ) >= 0 );
	}
}
