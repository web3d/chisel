package com.trapezium.chisel;

import com.trapezium.vrml.node.Node;
import java.util.EventObject;

public class NodeFoundEvent extends EventObject {
	Node node;
	String name;

	public NodeFoundEvent( Object source, Node n, String name ) {
		super( source );
		this.node = n;
		this.name = name;
	}

	public Node getNode() {
		return( node );
	}

	public String getName() {
		return( name );
	}
}
