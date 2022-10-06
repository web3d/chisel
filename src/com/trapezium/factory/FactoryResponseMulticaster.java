/*
 * FactoryResponseMulticaster.java
 *
 * A cool way to transparently convert a single listener into multiple listeners.
 *
 * When a FactoryResponseListener is being added, it is just added with:
 *
 *    listener = FactoryResponseMulticaster.add( listener, newListener );
 *
 * This effectively sets "listener" to "newListener" if there wasn't one previously.
 * Or it creates a FactoryResponseMulticaster with two listeners if there already
 * was a previous listener.
 *
 * The only complication here is that now callers have to distinguish between setting
 * an initial listener (with "setListener"), and either setting or adding
 * a listener with "addListener".
 */

package com.trapezium.factory;

public class FactoryResponseMulticaster implements FactoryResponseListener {

    // symantec 2.0 "Final" required me to remove word "final"..."Debug" 
    // accepted it no problem
    protected FactoryResponseListener a,b;
    
    private FactoryResponseMulticaster(FactoryResponseListener a, FactoryResponseListener b) {
    	this.a = a;
    	this.b = b;
    }

    protected static FactoryResponseListener add(FactoryResponseListener a, FactoryResponseListener b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
        	return new FactoryResponseMulticaster(a, b);
        }
    }

    protected static FactoryResponseListener remove(FactoryResponseListener listener, FactoryResponseListener listenerToRemove) {
        if (listener == listenerToRemove || listener == null) {
            return null;
        } else if (listener instanceof FactoryResponseMulticaster) {
            return ((FactoryResponseMulticaster) listener).remove(listenerToRemove);
        } else {
            return listener;
        }
    }

    private FactoryResponseListener remove(FactoryResponseListener listener) {
	    if (listener == a) {
	        return b;
	    } else if (listener == b) {
	        return a;
	    } else {
    	    FactoryResponseListener amod = remove(a, listener);
	        FactoryResponseListener bmod = remove(b, listener);
        	if (amod == a && bmod == b) {
        	    return this;
	        } else {
	            return add(amod, bmod);
	        }
	    }
    }

    //
    //  Check if this FactoryResponseMulticaster already contains a particular listener
    //
    public boolean contains( FactoryResponseListener listener ) {
        if ( listener == a ) {
            return( true );
        } else if ( listener == b ) {
            return( true );
        }
        if ( a instanceof FactoryResponseMulticaster ) {
            FactoryResponseMulticaster frma = (FactoryResponseMulticaster)a;
            if ( frma.contains( listener )) {
                return( true );
            }
        }
        if ( b instanceof FactoryResponseMulticaster ) {
            FactoryResponseMulticaster frmb = (FactoryResponseMulticaster)b;
            if ( frmb.contains( listener )) {
                return( true );
            }
        }
        return( false );
    }
    
	// The following are the FactoryResponseListener interface
	public void done( FactoryData result ) {
	    a.done(result);
	    b.done(result);
	}


	public void update( FactoryData result ) {
	    a.update(result);
	    b.update(result);
	}

	public void setNumberOfLines( int n ) {
	    a.setNumberOfLines(n);
	    b.setNumberOfLines(n);
	}
	
	public void setLinePercent( int n ) {
	    a.setLinePercent( n );
	    b.setLinePercent( n );
	}

	public void setPolygonCount( int n ) {
	    a.setPolygonCount(n);
	    b.setPolygonCount(n);
	}

	public void setAction( String action ) {
	    a.setAction(action);
	    b.setAction(action);
	}
	
	public void setText( String text ) {
	    a.setText( text );
	    b.setText( text );
	}

	public void setFactoryData( FactoryData fd ) {
	    a.setFactoryData(fd);
	    b.setFactoryData(fd);
	}
}
