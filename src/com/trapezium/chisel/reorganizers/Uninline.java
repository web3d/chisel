/*
 * @(#)Uninline.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.reorganizers;

import com.trapezium.chisel.*;

//
//  The Uninline chisel takes all Inlined files and merges them with
//  the current file.  It will only un-inline the first url listed in
//  the url field.  If no urls are listed, does nothing.
//

import java.io.PrintStream;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.ExposedField;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.visitor.PROTOcollector;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.ROUTE;
import com.trapezium.vrml.node.PROTObase;
import com.trapezium.util.StringUtil;

import java.util.Vector;

public class Uninline extends Optimizer {
    class SceneProtoCollector {
        PROTOcollector pc;
        SceneProtoCollector( PROTOcollector pc ) {
            this.pc = pc;
        }
        PROTOcollector getPROTOcollector() {
            return( pc );
        }
        Scene getScene() {
            return( pc.getScene() );
        }
    }
	int filesMerged = 0;
	Vector pcList;

	public Uninline() {
		super( "Inline", "Moving inline files into main file..." );
		reset();
	}
	
	public void reset() {
	    pcList = new Vector();
	}
	
    /** Called for each Inline encountered */
	public void attemptOptimization( Node n ) {
		Field urlField = n.getField( "url" );
		if ( urlField instanceof ExposedField ) {
		    MFFieldValue urls = (MFFieldValue)urlField.getFieldValue();
		    if ( urls != null ) {
		        FieldValue f = urls.getFieldValueAt( 0 );
		        if ( f != null ) {
		            String url = dataSource.toString( f.getFirstTokenOffset() );
		            if ( url != null ) {
            	        if (( baseFilePath != null ) && ( url.indexOf( ":" ) < 0)) {
            	            url = StringUtil.stripQuotes( url );
            	            if ( url.charAt( 0 ) != '/' ) {
            	                url = baseFilePath + "/" + url;
            	            } else {
                	            url = baseFilePath + url;
                	        }
            	        } else {
                	        url = StringUtil.stripQuotes( url );
                	    }
                	    if ( url != null ) {
                	        // here we need to create a Scene,
                	        // if it has PROTOs, print them at start of
                	        // optimize, renaming to avoid namespace
                	        // collisions
                	        Scene s = new Scene( url );
                	        if ( s.getTokenEnumerator() != null ) {
                    	        PROTOcollector protoCollector = new PROTOcollector( s, (Scene)n.getScene() );
                    	        s.traverse( protoCollector );
                    	        if ( protoCollector.hasPROTOs() ) {
                    	            replaceRange( 1, 1, protoCollector );
                    	        }
                    	        if ( s.getTokenEnumerator() != null ) {
            		                replaceRange( n.getFirstTokenOffset(), 
                    				    n.getLastTokenOffset(), new SceneProtoCollector( protoCollector ));
                    			}
                    			if ( protoCollector.hasROUTEs() ) {
                    			    pcList.addElement( protoCollector );
                    			}
                			}
            			}
        			}
               	}
        	}
    	}
	}

	// Replace an Inline node with the actual file
	// First gets a prefix that is does not exist in the file.  This prefix
	// is placed in front of every DEF/USE in the file being merged, to
	// prevent name space conflicts.
	// 
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    if ( param instanceof SceneProtoCollector ) {
    	    SceneProtoCollector spc = (SceneProtoCollector)param;
    	    printScene( tp, spc.getScene(), spc.getPROTOcollector() );
    	} else if ( param instanceof PROTOcollector ) {
    	    PROTOcollector pc = (PROTOcollector)param;
    	    int numberPROTOs = pc.getNumberPROTOs();
    	    for ( int i = 0; i < numberPROTOs; i++ ) {
    	        printPROTO( tp, pc.getPROTO( i ));
    	    }
    	    tp.printRange( startTokenOffset, endTokenOffset, false );
    	}
	}
	
	/** Print a Scene into the current Scene */
	void printScene( TokenPrinter tp, Scene s, PROTOcollector pc ) {
	    tp.flush();
	    TokenEnumerator save = tp.getDataSource();
	    if ( s != null ) {
	        TokenEnumerator newDataSource = s.getTokenEnumerator();
	        if ( newDataSource != null ) {
                    //MLo was if >1 but should always use a Group as
                    //Chisel has problems with uninlining a DEF-ed Inline
                    //if no Group is created here.
	            if ( s.numberChildren() > 0 ) {
    	            tp.print( "Group { children [" );
    	        }
	            tp.setDataSource( newDataSource );
	            pc.scanTokens();
	            while ( pc.hasMoreTokens() ) {
	                int token = pc.getNextToken();
	                if ( token != -1 ) {
    	                if ( pc.protoIsRemapped( token )) {
    	                    tp.print( pc.remapProto( token ));
    	                } else if ( pc.useIsRemapped( token )) {
    	                    tp.print( pc.remapUse( token ));
    	                } else {
    	                    tp.print( newDataSource, token );
    	                }
    	            }
	            }
	            if ( s.numberChildren() > 0 ) {
	                tp.print( "] }" );
	            }
	        }
	        tp.setDataSource( save );
	    }
	}
	
	/** Print a PROTO from another Scene */
	void printPROTO( TokenPrinter tp, PROTObase proto ) {
	    tp.flush();
	    TokenEnumerator save = tp.getDataSource();
	    Scene s = (Scene)proto.getRoot();
	    if ( s != null ) {
	        TokenEnumerator newDataSource = s.getTokenEnumerator();
	        if ( newDataSource != null ) {
	            tp.setDataSource( newDataSource );
	            tp.printRange( proto.getFirstTokenOffset(), 
	                proto.getLastTokenOffset(), false );
	            tp.setDataSource( save );
	        }
	    }
	}

    /** summary used by command line version */
	public void summarize( PrintStream ps ) {
	    System.out.println( filesMerged + " files merged" );
	}
	
	public boolean hasFinalCode() {
	    return( pcList.size() > 0 );
	}
	
	public void printFinalCode( TokenPrinter tp ) {
	    tp.flush();
	    int pcCount = pcList.size();
	    TokenEnumerator save = tp.getDataSource();
	    for ( int i = 0; i < pcCount; i++ ) {
	        PROTOcollector pc = (PROTOcollector)pcList.elementAt( i );
	        int numberROUTEs = pc.getNumberROUTEs();
	        tp.setDataSource( pc.getDataSource() );
	        for ( int j = 0; j < numberROUTEs; j++ ) {
	            ROUTE r = pc.getROUTE( j );
	            tp.printRange( r.getFirstTokenOffset(), r.getLastTokenOffset(), false );
	            tp.flush();
	        }
        }
	    tp.setDataSource( save );
	}
}


