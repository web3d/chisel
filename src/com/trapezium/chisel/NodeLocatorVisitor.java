package com.trapezium.chisel;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.pattern.Visitor;
import com.trapezium.vrml.ROUTE;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.Scene;

import java.util.Vector;
import java.util.Hashtable;

//
//  The NodeLocatorVisitor is the event source for NodeFoundEvents.  A NodeFoundEvent
//  is generated whenever a node of one of the registered types is located.  This event
//  is set to all registered listeners.
//
//  For example, the IFS_ColorOptimizer only cares about IndexedFaceSet nodes.  The
//  IFS_ColorOptimizer implements the NodeLocatorListener interface which has the
//  "nodeFound" method.

public class NodeLocatorVisitor extends Visitor {
	Vector nodeNames;
	Vector listeners;
	boolean all = false;
	Hashtable nodesFound;

	public NodeLocatorVisitor( TokenEnumerator v ) {
		super( v );
	}

    /** PROTOs can result in duplicate notifications, this prevents this */
    boolean notified( Node n ) {
        if ( nodesFound == null ) {
            nodesFound = new Hashtable();
            nodesFound.put( n, this );
            return( false );
        }
        if ( nodesFound.get( n ) == null ) {
            nodesFound.put( n, this );
            return( false );
        } else {
            return( true );
        }
    }

	public void addNodeLocatorListener( NodeLocatorListener nll ) {
		if ( listeners == null ) {
			listeners = new Vector();
		}
		listeners.addElement( nll );
		registerNodeName( nll.getNodeName() );
		int additionalNames = nll.getNumberAdditionalNames();
		for ( int i = 0; i < additionalNames; i++ ) {
		    registerNodeName( nll.getAdditionalName( i ));
		}
	}

	public void removeNodeLocatorListener( NodeLocatorListener nll ) {
		if ( listeners != null ) {
			listeners.removeElement( nll );
		}
	}

	public void registerNodeName( String nodeType ) {
	    if ( nodeType == null ) {
	        return;
	    }
		if ( nodeType.compareTo( "All" ) == 0 ) {
			all = true;
			return;
		}
		if ( nodeType.compareTo( "CoordinateOwner" ) == 0 ) {
		    registerNodeName( "IndexedFaceSet" );
		    registerNodeName( "IndexedLineSet" );
		    return;
		}
		if ( nodeType.compareTo( "Interpolator" ) == 0 ) {
		    registerNodeName( "PositionInterpolator" );
		    registerNodeName( "ScalarInterpolator" );
		    registerNodeName( "OrientationInterpolator" );
		    registerNodeName( "ColorInterpolator" );
		    registerNodeName( "CoordinateInterpolator" );
		    registerNodeName( "NormalInterpolator" );
		}
		if ( nodeNames == null ) {
			nodeNames = new Vector();
		}
		int count = nodeNames.size();
		for ( int i = 0; i < count; i++ ) {
			String s = (String)nodeNames.elementAt( i );
			if ( s.compareTo( nodeType ) == 0 ) {
				return;
			}
		}
		nodeNames.addElement( nodeType );
	}

	public boolean visitObject( Object a ) {
	    //////
	    //  System.out.print(" ->" + a.getClass().getName());
        //////

		if (( a instanceof ROUTE ) && ( listeners != null )) {
			ROUTE route = (ROUTE)a;
			int count = listeners.size();
			boolean routeListenerFound = false;
			for ( int i = 0; i < count; i++ ) {
				NodeLocatorListener nl = (NodeLocatorListener)listeners.elementAt( i );
				if ( nl.isROUTElistener() ) {
					routeListenerFound = true;
					break;
				}
			}
			if ( routeListenerFound ) {
				RouteFoundEvent rfe = new RouteFoundEvent( this, route, "ROUTE" );
				for ( int i = 0; i < count; i++ ) {
					NodeLocatorListener nl = (NodeLocatorListener)listeners.elementAt( i );
					if ( nl.isROUTElistener() ) {
						nl.routeFound( rfe );
					}
				}
			}
		} else if (( a instanceof Node ) && ( listeners != null )) {
			//  We only find DEFed nodes, USEd nodes have already been DEFed somewhere
			//  else, so any optimization of the node takes place in its DEFed version
			Node n = (Node)a;
			Scene xxxx = (Scene)n.getScene();
			if ( n instanceof DEFUSENode ) {
				DEFUSENode dun = (DEFUSENode)n;
				int count = listeners.size();

				// check if there are any DEF listeners.  This is separate from the
				// notification because in most cases there won't be any.  If there are,
				// we then create the NodeFoundEvent and notify them
				boolean DEFlistenerFound = false;
				for ( int i = 0; i < count; i++ ) {
					NodeLocatorListener nl = (NodeLocatorListener)listeners.elementAt( i );
					if ( nl.isDEFUSElistener() || nl.isDEFlistener() ) {
						DEFlistenerFound = true;
						break;
					}
				}

				// If there are any DEF listeners, notify them
				if ( DEFlistenerFound ) {
					NodeFoundEvent nfe = new NodeFoundEvent( this, n, "DEFUSENode" );
					for ( int i = 0; i < count; i++ ) {
						NodeLocatorListener nl = (NodeLocatorListener)listeners.elementAt( i );
						if ( nl.isDEFUSElistener() || nl.isDEFlistener() ) {
							nl.nodeFound( nfe );
						}
					}
				}

				if ( !dun.isDEF() ) {
					return( false );
				} else {
					return( true );
				}
			} else if ( n instanceof PROTO ) {
			    boolean protoListenerFound = false;
			    int count = listeners.size();
			    for ( int i = 0; i < count; i++ ) {
			        NodeLocatorListener nl = (NodeLocatorListener)listeners.elementAt( i );
			        if ( nl.isPROTOlistener() ) {
    					NodeFoundEvent nfe = new NodeFoundEvent( this, n, "PROTO" );
			            nl.nodeFound( nfe );
			        }
			    }
			}
			if ( !notified( n )) {
    			if ( all ) {
    				NodeFoundEvent nfe = new NodeFoundEvent( this, n, n.getBaseName() );
    				notifyListeners( nfe );
    			} else if ( nodeNames != null ) {
    				int count = nodeNames.size();
    				for ( int i = 0; i < count; i++ ) {
    					String s = (String)nodeNames.elementAt( i );
    					if ( n.getBaseName().compareTo( s ) == 0 ) {
    						NodeFoundEvent nfe = new NodeFoundEvent( this, n, s );
    						notifyListeners( nfe );
    						break;
    					}
    				}
    			}
			}
		}
		return( true );
	}


	public void notifyListeners( NodeFoundEvent nfe ) {
		if ( listeners != null ) {
			int listenerCount = listeners.size();
			for ( int i = 0; i < listenerCount; i++ ) {
				NodeLocatorListener nl = (NodeLocatorListener)listeners.elementAt( i );
				nl.nodeFound( nfe );
			}
		}
	}
}
