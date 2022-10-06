/*
 * @(#)QueuedRequestFactory.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.factory;

import java.util.Vector;

//
//  The QueuedRequestFactory is the abstract base class for factory objects which accept
//  requestQueue which might take a while to process.  requestQueue are queued and handled one
//  at a time.
//
//  Subclasses only implement a "handleRequest" method.
//
//  The source of the Event passed to "queueRequest" must be a FactoryResponseListener,
//  so that it can track the progress of the request as it is processed by the factory.
//

public abstract class QueuedRequestFactory extends FactoryDataUpdater implements FactoryRequestListener {
	//
	//  All requests that are queued for this factory
	//
	Vector requestQueue = new Vector();

	//
	//  The thread that runs all queued requests
	//
	QueueRequestThread queueRequestThread;

    public void wipeout() {
        if ( queueRequestThread != null ) {
            queueRequestThread.stop();
            queueRequestThread.wipeout();
            queueRequestThread = null;
        }
        requestQueue = null;
    }
    
	//
	//  Only one request is processed at a time
	public QueuedRequestFactory() {
		queueRequestThread = new QueueRequestThread( this );
	}

	public void update() {
		update( data );
	}

	public void done() {
		done( data );
	}

	//
	//  FactoryRequestListener interface:  two forms of submit
	//
	//  Not all that sure we need to do it this way, but it makes it a bean, so it may
	//  be useful.
	//
	//  queue up a request in the TokenStreamFactory
	//
	public void submit( FactoryData factoryData ) {
		synchronized( requestQueue ) {
			requestQueue.addElement( factoryData );
		}
		queueRequestThread.resume();
	}


	public String getFactoryName() {
		return( "unnamed" );
	}

	public Vector cloneQueue() {
		Vector tempQueue;
		synchronized( requestQueue ) {
			tempQueue = (Vector)requestQueue.clone();
			requestQueue.removeAllElements();
		}
		return( tempQueue );
	}

	public int getQueueSize() {
		int qsize = 0;
		synchronized ( requestQueue ) {
			qsize = requestQueue.size();
		}
		return( qsize );
	}

	public void processRequest( FactoryData request ) {
		data = request;
		if ( listener != null ) {
			listener.setFactoryData( data );
		}
		request.setFactoryName( getFactoryName() );
		handleRequest( request );
		done();
	}

	//
	//
	//  process a request
	//
	abstract public void handleRequest( FactoryData factoryData );
}


class QueueRequestThread extends Thread {

	QueuedRequestFactory owner;

	public QueueRequestThread( QueuedRequestFactory owner ) {
		super();
		this.owner = owner;
		start();
		waitUntilStarted();
	}

    public void wipeout() {
        owner = null;
    }
    
	boolean started = false;
	void waitUntilStarted() {
		while ( true ) {
			if ( started ) {
				return;
			}
			try {
				//Thread.sleep( 1000 );
				Thread.sleep( 10 );
			} catch( Exception e ) {
                            System.err.println("QueueRequestThread: " + e); // MLo
			}
		}
	}

	public void run() {
		while ( true ) {
			started = true;

			int qsize = owner.getQueueSize();
			if ( qsize == 0 ) {
				suspend();
			}
			Vector tempQueue = owner.cloneQueue();
			while ( !tempQueue.isEmpty() ) {
				FactoryData request = (FactoryData)tempQueue.elementAt( 0 );
				tempQueue.removeElementAt( 0 );
				owner.processRequest( request );
			}
		}
	}
}
