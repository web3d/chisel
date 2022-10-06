/*
 * @(#)TokenStreamFactory.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license
 * granted by Trapezium.
 */
package com.trapezium.factory;

import com.trapezium.edit.TokenEditor;
import java.io.*;
import java.util.Vector;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import com.trapezium.util.GlobalProgressIndicator;
import com.trapezium.edit.Document;
import com.trapezium.util.ProgressIndicator;
import com.trapezium.chisel.ChiselSet;

//
//  The TokenStreamFactory is a QueuedRequestFactory that creates TokenEnumerators
//  (which I think should be renamed to be TokenStreams).
//
//  An application should create a single TokenStreamFactory.  The Event source has
//  to be a FactoryResponseListener so that it can be notified of progress of the
//  factory.
//
public class TokenStreamFactory extends QueuedRequestFactory {

	public TokenStreamFactory() {
		super();
	}

	public String getFactoryName() {
		return( getFixedFactoryName() );
	}

	static public String getFixedFactoryName() {
		return( "Token Stream Factory" );
	}


    // ugly hack to communicate between createTokenStream and handleRequest
    private File fileSource;

	//
	//  create a TokenStream, when finished, generate an event back to requestor.
	//
	public void handleRequest( FactoryData request ) {
	    //System.out.println( "Creating tokens for " + request.getUrl() );
		try {
		    // check if the request includes a dirty token enumerator.
		    // If it does, this means we have a text-editted token enumerator,
		    // that needs to be re-tokenized.
		    TokenEditor tetest = request.getTokenEditor();
		    if (( tetest != null ) && ( tetest.isDirty() )) {
		        tetest.retokenize();
		    } else {
		        ProgressIndicator pl = ChiselSet.getProgressIndicator( ChiselSet.VALIDATORS );
		        if ( pl != null ) {
    		        pl.reset();
    		    }
        		Document doc = request.getDocument();
        		if ( pl != null ) {
          			pl.setTitle( "Loading file ... " );
          		}
       			System.out.println( "Loading '" + request.getUrl() + "'" );
                InputStream is = createTokenStream( request.getUrl(), request );
    			TokenEditor te = new TokenEditor( is, request.getUrl(), pl, fileSource );
    			if ( GlobalProgressIndicator.abortCurrentProcess ) {
    			    request.setAborted( true );
    			} else {
    			    if ( doc != null ) {
            			doc.setLines( te );
            			doc.setDocumentLoader( request );
            		}
        			request.setFile( fileSource );
        			request.setTokenEditor( te );
        		}
    		}
    	} catch ( java.io.FileNotFoundException fnf ) {
    	    System.out.println( "File '" + request.getUrl() + "' not found." );
    	    request.setError( fnf );
		} catch ( Exception e ) {
		    e.printStackTrace();
			request.setError( e );
		}
	}

	InputStream createTokenStream( String url, FactoryData data ) throws FileNotFoundException, IOException {

		fileSource = null;

		URL urlAttempt = null;
		data.setGzip( false );
		try {
			urlAttempt = new URL( url );
			PushbackInputStream test = new PushbackInputStream( urlAttempt.openStream() );
			int testchar = test.read();
			test.unread( testchar );
			if ( testchar == 0x1f ) {
			    data.setGzip( true );
				return( new BufferedInputStream( new GZIPInputStream( test )));
//			} else if ( testchar == 'P' ) {
			} else {
				return( new BufferedInputStream( test ));
			}
		} catch( Exception e ) {
			fileSource = new File( url );
			PushbackInputStream test = new PushbackInputStream( new FileInputStream( fileSource ));
			int testchar = test.read();
			test.unread( testchar );
			if ( testchar == 0x1f ) {
			    data.setGzip( true );
				return( new BufferedInputStream( new GZIPInputStream( test )));
//			} else if ( testchar == 'P' ) {
			} else {
				return( new BufferedInputStream( test ));
			}
		}
	}
}
