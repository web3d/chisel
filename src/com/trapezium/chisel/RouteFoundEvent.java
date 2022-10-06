package com.trapezium.chisel;

import com.trapezium.vrml.ROUTE;
import java.util.EventObject;

public class RouteFoundEvent extends EventObject {
	ROUTE route;
	String name;

	public RouteFoundEvent( Object source, ROUTE route, String name ) {
		super( source );
		this.route = route;
		this.name = name;
	}

	public ROUTE getRoute() {
		return( route );
	}

	public String getName() {
		return( name );
	}
}
