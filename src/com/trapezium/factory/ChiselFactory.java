/*
 * @(#)ChiselFactory.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.factory;

import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.util.GlobalProgressIndicator;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.edit.TokenEditor;
import com.trapezium.chisel.Optimizer;
import com.trapezium.chisel.RangeReplacer;
import com.trapezium.chisel.NodeLocatorVisitor;
import com.trapezium.chisel.ProcessedFile;
import com.trapezium.chisel.ChiselDescriptor;
import com.trapezium.chisel.ChiselSet;
import com.trapezium.util.ProgressIndicator;
import com.trapezium.chisel.RowState;
import com.trapezium.chisel.Chisel;
import com.trapezium.util.StringUtil;
import com.trapezium.edit.Document;
import java.io.*;
import java.util.Vector;


//
//  The ChiselFactory collects a set of chisel class names, and provides an
//  interface for creating the set of Chisel objects associated with those
//  chisel class names.
//
public class ChiselFactory extends QueuedRequestFactory {
    // when factory made by Strings, this is either a String or a Vector of Stings
	Object chiselNames;
	
	// when factory is made by Optimizers, this is either an Optimizer or a Vector of Optimizers
	Object chiselOptimizers;
	
	String factoryName = null;
	String factoryTitle = null;
	String baseFilePath = null;
	String baseFileName = null;
	int chiselType;
	ProgressIndicator progressListener;
	RowState rowState;

	public void wipeout() {
	    super.wipeout();
	    chiselNames = null;
	    chiselOptimizers = null;
	    progressListener = null;
	    rowState = null;
	}
	
	public ChiselFactory( int chiselType, ProgressIndicator pl, RowState rowState ) {
		super();
		progressListener = pl;
		chiselNames = null;
		this.chiselType = chiselType;
		this.rowState = rowState;
	}
	
	/** This no longer used, but should probably be re-introduced.
	 *  It allows compatible chisels to all run within the same ChiselFactory.
	 *  Compatible means that the sections of the VRML text that they affect
	 *  is guaranteed to be non-overlapping.
	 *  This was abandoned because the type categories weren't defined very
	 *  well, and it made the GUI a bit more confusing.
	 *  When re-introduced, the compatibility types need to be redesigned,
	 *  and the feedback markings done a bit differently (probably still
	 *  keep one 'phase' for each check box, just mark the phase as done
	 *  if it is included with a compatible chisel category).
	 */
	public boolean isCompatible( int type ) {
	    return( false );
//	    if ( type == chiselType ) {
//	        return( false );
//	    } else if (( chiselType == ChiselDescriptor.ALLNODE_CHISEL ) && ( type > ChiselDescriptor.ALLNODE_CHISEL )) {
//	        return( false );
//	    } else {
//	        return( true );
//	    }
	}
	
	public String getChiselName() {
	    if ( chiselNames instanceof String ) {
	        return( (String)chiselNames );
	    } else if ( chiselOptimizers instanceof Optimizer ) {
	        return( chiselOptimizers.getClass().getName() );
        } else {
	        return( null );
	    }
	}

    public void setBaseFilePath( String baseFilePath ) {
        this.baseFilePath = baseFilePath;
    }
    
    public void setBaseFileName( String baseFileName ) {
        this.baseFileName = baseFileName;
    }
    
    /** add a chisel by name to the list of optimizers to create */
	public void addChisel( String chiselName ) {
	    if ( chiselNames == null ) {
	        chiselNames = chiselName;
	    } else if ( chiselNames instanceof String ) {
	        String save = (String)chiselNames;
	        chiselNames = new Vector();
	        ((Vector)chiselNames).addElement( save );
	    }
        if ( chiselNames instanceof Vector ) {
    		((Vector)chiselNames).addElement( chiselName );
    	}
	}

	/** Add an optimizer to the list of optimizers */
	public void addOptimizer( Optimizer chisel ) {
	    if ( chiselOptimizers == null ) {
	        chiselOptimizers = chisel;
	    } else if ( chiselOptimizers instanceof Optimizer ) {
	        Optimizer o = (Optimizer)chiselOptimizers;
	        chiselOptimizers = new Vector();
	        ((Vector)chiselOptimizers).addElement( o );
	    }
	    if ( chiselOptimizers instanceof Vector ) {
	        ((Vector)chiselOptimizers).addElement( chisel );
	    }
	}
	
	public void setFactoryName( String factoryName ) {
	    this.factoryName = factoryName;
	}
	
	public void setFactoryTitle( String factoryTitle ) {
	    this.factoryTitle = factoryTitle;
	}

    public void dump( int indentLevel ) {
        String spacer = StringUtil.spacer( indentLevel );
        System.out.println( spacer + "Factory name: " + factoryName );
        System.out.println( spacer + "Factory title: " + factoryTitle );
        if ( chiselNames instanceof Vector ) {
            Vector cn = (Vector)chiselNames;
            for ( int i = 0; i < cn.size(); i++ ) {
                System.out.println( spacer + "chisel " + i + ": " + (String)cn.elementAt( i ));
            }
        } else if ( chiselNames instanceof String ) {
            String cn = (String)chiselNames;
            System.out.println( spacer + "chisel 0: " + cn );
        }
    }

	/** Run the set of chisels indicated for this factory.
     *
	 *  First creates the set of chisels as indicated by the class names
	 *  collected via the "addChisel" method above.  Then sets up
	 *  the information in the RangeReplacer object by traversing the
	 *  scene graph with the NodeLocatorVisitor (which is initialized 
	 *  by the chisels as they are created).  Then a new token stream
	 *  is created using that RangeReplacer object, and that new token
	 *  stream is placed in the FactoryData object.
     */
	public void handleRequest( FactoryData factoryData ) {
		try {
		    if ( rowState != null ) {
    		    rowState.rowRunning();
    		}
			RangeReplacer rr = new RangeReplacer();
			Scene vrmlScene = (Scene)factoryData.getScene();
                        if (vrmlScene == null) {
                            System.out.println("Chisel: Failed to handle request (probably a queued request that is no longer valid)");
                            return;
                        }
			TokenEnumerator sceneTokenEnumerator = vrmlScene.getTokenEnumerator();
			NodeLocatorVisitor nlv = new NodeLocatorVisitor( sceneTokenEnumerator );
			createChisels( rr, nlv, sceneTokenEnumerator, factoryData );
			GlobalProgressIndicator.setProgressIndicator( progressListener, "Traversing scene...", 0 );
			sceneTokenEnumerator.notifyLineNumbers( progressListener );
			vrmlScene.traverse( nlv );
			if ( progressListener != null ) {
    		    progressListener.reset();
    			progressListener.setTitle( factoryTitle );
    		} else {
    		    System.out.println( factoryTitle );
    		}
			sceneTokenEnumerator.notifyLineNumbers( progressListener );
			if ( rr.replacementsRegistered() ) {
    			TokenEditor te = rr.recreateTokenStream( factoryTitle, (TokenEditor)sceneTokenEnumerator, vrmlScene.getFirstTokenOffset(), vrmlScene.getLastTokenOffset(), progressListener );
    			rr.wipeout();
    			if ( !GlobalProgressIndicator.abortCurrentProcess ) {
                    Document doc = factoryData.getDocument();
                    if ( doc != null ) {
                        doc.setLines( te );
                        doc.setDocumentLoader( factoryData );
                    }
        			factoryData.setTokenEditor( te );
        			factoryData.setScene( vrmlScene );
        			vrmlScene.setTokenEnumerator( te );
        			if ( factoryData instanceof ProcessedFile ) {
        			    ((ProcessedFile)factoryData).markPercent();
        			}
        			sceneTokenEnumerator.wipeout();
        		} else if ( te != null ) {
        		    te.wipeout();
        		    te = null;
        		}
        	} else {
        	    factoryData.disableParse();
        	}
        	if ( progressListener != null ) {
       			progressListener.reset();
       		}
   			if ( rowState != null ) {
    			rowState.rowDone();
    		}
    		//Runtime.getRuntime().gc(); MLo
    	} catch ( Exception e ) {
			System.out.println( "ChiselFactory got an exception: " + e.toString() );
			e.printStackTrace();
			factoryData.setError( e );
		}
	}

	public String getFactoryName() {
	    if ( factoryName != null ) {
	        return( factoryName );
	    } else {
    		return( getFixedFactoryName() );
    	}
	}
	
	static public String getFixedFactoryName() {
		return( "Chisel Factory" );
	}

    /** Get the chisel singleton */
	Optimizer createChisel( String name, FactoryData factoryData ) {
	    Optimizer factoryCreated  = ChiselDescriptor.createChisel( name );
	    if ( factoryData instanceof ProcessedFile ) {
    	    ((ProcessedFile)factoryData).chiselWith( factoryCreated );
    	}
	    if ( factoryCreated != null ) {
	        factoryCreated.setBaseFilePath( baseFilePath );
	        factoryCreated.setBaseFileName( baseFileName );
	        return( factoryCreated );
	    }
		Class nodeClass = null;
		try {
			if (( name.indexOf( "DEF" ) == 0 ) && ( name.indexOf( "DEFremover" ) == -1 )) {
				nodeClass = Class.forName( "com.trapezium.chisel.DEFmaker" );
			} else {
				nodeClass = Class.forName("com.trapezium.chisel." + name);
			}
			if (nodeClass != null) {
				Optimizer o =  (Optimizer) (nodeClass.newInstance());
				if (( name.indexOf( "DEF" ) == 0 ) && ( name.indexOf( "DEFremover" ) == -1 ) && ( name.indexOf( "DEFmaker" ) == -1 )) {
					o.setNodeName( name.substring( 3 ));
				}
				o.setBaseFilePath( baseFilePath );
				o.setBaseFileName( baseFileName );
				return o;
			}
		} catch ( Exception e ) {
			return( null );
		}
		return( null );
	}

    /** create chisel objects based on names, ChiselFactory runs a set of these */
	void createChisels( RangeReplacer rr, NodeLocatorVisitor nlv, TokenEnumerator v, FactoryData factoryData ) {
        if ( chiselNames instanceof Vector ) {
            Vector cn = (Vector)chiselNames;
    		for ( int i = 0; i < cn.size(); i++ ) {
    			createOneChisel( (String)cn.elementAt( i ), rr, nlv, v, factoryData );
    		}
    	} else if ( chiselNames instanceof String ) {
    	    createOneChisel( (String)chiselNames, rr, nlv, v, factoryData );
    	} else if ( chiselOptimizers instanceof Vector ) {
    	    Vector co = (Vector)chiselOptimizers;
    	    for ( int i = 0; i < co.size(); i++ ) {
    	        createOneChisel( (Optimizer)co.elementAt( i ), rr, nlv, v );
    	    }
    	} else if ( chiselOptimizers instanceof Optimizer ) {
    	    createOneChisel( (Optimizer)chiselOptimizers, rr, nlv, v );
    	}
    }
    
    void createOneChisel( String chiselName, RangeReplacer rr, NodeLocatorVisitor nlv, TokenEnumerator v, FactoryData factoryData ) {
		Optimizer o = createChisel( chiselName, factoryData );
		createOneChisel( o, rr, nlv, v );
	}
	
	void createOneChisel( Optimizer o, RangeReplacer rr, NodeLocatorVisitor nlv, TokenEnumerator v ) {
		if ( o != null ) {
			o.setRangeReplacer( rr );
			o.setDataSource( v );
			nlv.addNodeLocatorListener( o );
			System.out.println( o.getActionMessage() );
//		} else {
//			System.out.println( "failed creating chisel " + chiselName );
		}
	}
}
