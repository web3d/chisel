package com.trapezium.factory;

import java.util.Vector;

//
//  The FactoryDataUpdater updates FactoryData information as it gets
//  FactoryResponseListener interface calls.
//

public class FactoryDataUpdater implements FactoryResponseListener {
	//
	//  Only one request is handled at a time, this is all we need to know about it.
	//
	protected FactoryData data;
	protected FactoryResponseListener listener;

	public FactoryDataUpdater() {
	}

    // wipe out any previous listener
	public void setListener( FactoryResponseListener frl ) {
		listener = frl;
	}
	
	public FactoryResponseListener getListener() {
	    return( listener );
	}
	
    public void addListener( FactoryResponseListener frl ) {
        if ( frl == null ) {
            return;
        }
        if ( listener instanceof FactoryResponseMulticaster ) {
            FactoryResponseMulticaster frm = (FactoryResponseMulticaster)listener;
            if ( frm.contains( frl )) {
                return;
            }
        }
        listener = FactoryResponseMulticaster.add(listener, frl);
    }

	public void setFactoryData( FactoryData fd ) {
		data = fd;
	}


	//
	//  FactoryResponseListener interface:  update, done, requestCompleted, setXXXXX
	//
	public void update( FactoryData data ) {
		if ( listener != null ) {
			listener.update( data );
		}
	}

	public void done( FactoryData data ) {
		if ( listener != null ) {
			listener.done( data );
		}
	}

	public void setNumberOfLines( int n ) {
		data.setNumberOfLines( n );
		update( data );
	}

	public void setPolygonCount( int n ) {
		data.setPolygonCount( n );
		update( data );
	}
	
	public void setLinePercent( int n ) {
	}

	public void setAction( String s ) {
		if ( data != null ) {
			data.setAction( s );
			update( data );
		}
	}
	
	public void setText( String text ) {
	}
}

