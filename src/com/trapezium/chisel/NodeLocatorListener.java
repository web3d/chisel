package com.trapezium.chisel;

import java.util.EventListener;

public interface NodeLocatorListener extends EventListener {
	public String getNodeName();
	public int getNumberAdditionalNames();
	public String getAdditionalName( int offset );
	public void nodeFound( NodeFoundEvent nfe );
	public void routeFound( RouteFoundEvent rfe );
	public boolean isDEFUSElistener();
	public boolean isDEFlistener();
	public boolean isROUTElistener();
	public boolean isPROTOlistener();
}
