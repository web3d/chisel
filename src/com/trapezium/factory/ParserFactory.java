/*
 * @(#)ParserFactory.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted 
 * by Trapezium.
 *
 */
package com.trapezium.factory;

import com.trapezium.edit.EditLintVisitor;
import com.trapezium.edit.TokenEditor;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.util.GlobalProgressIndicator;
import com.trapezium.vrml.visitor.ComplexityVisitor;
import com.trapezium.vrml.visitor.ComplexityData;
import com.trapezium.vrml.grammar.VRML97parser;
import com.trapezium.vrml.node.NodeType;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.chisel.ChiselSet;
import com.trapezium.util.ProgressIndicator;
import com.trapezium.vorlon.ErrorSummary;
import java.io.*;
import java.util.Vector;
import java.net.URL;

public class ParserFactory extends QueuedRequestFactory {
    static public boolean hackTest = false;

	public ParserFactory() {
		super();
	}

	//
	//  create a TokenStream, when finished, generate an event back to requestor.
	//
	public void handleRequest( FactoryData request ) {
	    System.out.println( "Checking syntax for " + request.getUrl() );
		if (( request.getTokenEditor() != null ) && ( request.isParseEnabled() || request.getNodeVerifyChecksEnabled() )) {
			try {
			    request.setPreviousNumberErrors();
			    if ( hackTest ) {
        			ProgressIndicator pl = ChiselSet.getProgressIndicator( ChiselSet.VALIDATORS );
    				pl.setTitle( "Checking syntax ... " );
    				pl.reset();
    				hackTest = false;
			    } else {
    				TokenEditor vrmlTokenEditor = request.getTokenEditor();
    				Scene vrmlScene = new Scene( request.getUrl(), vrmlTokenEditor );
    				ErrorSummary errorSummary = new ErrorSummary( 1000 );
    				vrmlScene.setErrorSummary( errorSummary );
        			vrmlScene.setTokenEnumerator( vrmlTokenEditor );
        			ProgressIndicator pl = ChiselSet.getProgressIndicator( ChiselSet.VALIDATORS );
    				vrmlTokenEditor.notifyLineNumbers( pl );
    				if ( pl != null ) {
        				pl.setTitle( "Checking syntax ... " );
        			}
    				VRML97parser parser = new VRML97parser();
    				if ( !request.getNodeVerifyChecksEnabled() ) {
    				    NodeType.verifyDisabled = true;
    				    if ( request.getUsageChecksEnabled() ) {
    				        NodeType.usageChecksEnabled = true;
    				    }
    				}

                    VrmlElement.createCount = 0;
    				parser.Build( vrmlTokenEditor, vrmlScene );
    				vrmlScene.setVrmlElementCount( VrmlElement.createCount );
    				NodeType.verifyDisabled = false;
    				NodeType.usageChecksEnabled = false;
    				if ( pl != null ) {
        				pl.reset();
        			}
    				if ( GlobalProgressIndicator.abortCurrentProcess ) {
    				    request.setAborted( true );
    				} else {
           				request.setScene( vrmlScene );
    
        				// These are only needed on the final pass
        				if ( request.getNodeVerifyChecksEnabled() ) {
        				    if ( pl != null ) {
                				pl.setTitle( "Checking scene graph..." );
                			}
                			NodeType.verifyUsage( vrmlScene.getUsageTable(), vrmlScene.getTokenEnumerator(), vrmlScene.getErrorSummary() );
            				vrmlTokenEditor.notifyLineNumbers( pl );
            				EditLintVisitor lv = new EditLintVisitor( vrmlTokenEditor );
            				if ( pl != null ) {
                				pl.reset();
                				pl.setTitle( "Collecting errors..." );
                			}
            				//System.out.println( "traversing with lint visitor..." );
            				vrmlScene.traverse( lv );
            				lv.setErrorKeys();
            				request.setLintInfo( lv );
            				if ( !GlobalProgressIndicator.abortCurrentProcess ) {
                        		ComplexityData cd = new ComplexityData();
                				ComplexityVisitor cv = new ComplexityVisitor( cd, vrmlTokenEditor );
                				if ( pl != null ) {
                    				pl.reset();
                    				pl.setTitle( "Counting polygons..." );
                    			}
                				//System.out.println( "traversing with complexity visitor..." );
                    			vrmlScene.traverse( cv );
                    			lv.setInlineCount( cv.getInlineCount() );
                    			request.setPolygonCount( cd.getPolygonCount() );
                    			vrmlTokenEditor.notifyLineNumbers( null );
//                    			System.out.println( "Done." );
                    			if ( pl != null ) {
                        			pl.reset();
                        		}
                    		}
                		}
                	}
            	}
//      			System.out.println( "Done." );
                request.setCurrentNumberErrors();

    		} catch ( Exception e ) {
System.out.println( "Got an exception: " + e.toString() );
				e.printStackTrace();
				request.setError( e );
			}
		}
	}

	public String getFactoryName() {
		return( getFixedFactoryName() );
	}

	static public String getFixedFactoryName() {
		return( "Parser Factory" );
	}
}
