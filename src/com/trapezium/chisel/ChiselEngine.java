/*
 * @(#)ChiselEngine.java
 *
 * Copyright (c) 1998-2000 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import com.trapezium.vrml.Scene;
import com.trapezium.edit.TokenEditor;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.factory.FactoryResponseAdapter;
import com.trapezium.factory.FactoryData;
import java.util.Vector;
import java.io.File;

/** Main class for command line version of Chisel.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 4 Dec 1998
 *
 *  @since           1.0
 */

public class ChiselEngine extends Thread {

    /** files being viewed or processed */
    Vector filesToProcess;
    ChiselSet[] sets;
    ChiselCommandList chiselCommandList;
    EngineRunner theDriver;
    boolean doGzip;
    boolean preserveNames;
    String outputDir;
    boolean stripComments;
    boolean reformat;

    /** Class constructor */
    public ChiselEngine( Vector filesToProcess, ChiselCommandList chiselCommandList, boolean doGzip, String outputDir, boolean preserveNames, boolean stripComments, boolean reformat ) {
        this.filesToProcess = filesToProcess;
        this.chiselCommandList = chiselCommandList;
        this.doGzip = doGzip;
        this.outputDir = outputDir;
        this.preserveNames = preserveNames;
        this.stripComments = stripComments;
        this.reformat = reformat;
        fixOutputDir();
        sets = ChiselSet.createChiselSets();
        theDriver = new EngineRunner( this );
    }
    
    /** if outputDir exists, make sure it ends with a '/' */
    void fixOutputDir() {
        if ( outputDir != null ) {
            int slashIndex = outputDir.lastIndexOf( '/' );
            if ( slashIndex == -1 ) {
                slashIndex = outputDir.lastIndexOf( '\\' );
            }
            if ( slashIndex != ( outputDir.length() - 1 )) {
                slashIndex = -1;
            }
            if ( slashIndex == -1 ) {
                outputDir = outputDir + "/";
            }
        }
    }
    
    
    /** Strip out all comments and/or reformat */
    void stripAndReformat( ProcessedFile p ) {
        Scene scene = p.getScene();
        TokenEnumerator sceneTokens = scene.getTokenEnumerator();
        TokenEditor dataSource = new TokenEditor( sceneTokens );
        if ( sceneTokens instanceof TokenEditor ) {
            TokenEditor sceneTokenEditor = (TokenEditor)sceneTokens;
            sceneTokenEditor.disableLineInfo();
        }
        if ( stripComments ) {
            sceneTokens.enableCommentSkipping();
        } else {
            sceneTokens.disableCommentSkipping();
        }
        TokenPrinter tp = new TokenPrinter( sceneTokens, dataSource );

        // the data sink gets formatted text
        if ( reformat ) {
            tp.doPrettyPrint();
        }

        // generate the new token enumerator
        tp.printRange( scene.getFirstTokenOffset(), scene.getLastTokenOffset(), true );

        p.setTokenEditor( dataSource );
        scene.setTokenEnumerator( dataSource );
    }

    /** running thread */
    public void run() {
        int numberFiles = filesToProcess.size();
        for ( int i = 0; i < numberFiles; i++ ) {
            String fileName = (String)filesToProcess.elementAt( i );
            System.out.println( "Processing file #" + (i+1) + ": " + fileName );
            processFile( fileName, chiselCommandList );
			Runtime.getRuntime().gc();
        }
    }

    /** result, exit with this */
    public int getResult() {
        return( 0 );
    }

    /** Process a single file */
    void processFile( String fileName, ChiselCommandList chiselCommandList ) {
        ProcessedFile p = new ProcessedFile( fileName );
        p.setDoneListener( theDriver );

        /* Create the ChiselFactory with all the factories to run */
        int numberChisels = chiselCommandList.getNumberCommands();
        for ( int i = 0; i < numberChisels; i++ ) {
            Optimizer chisel = chiselCommandList.elementAt( i );
            chisel.reset();
            p.addOptimizer( chisel );
        }

        /** Run all the factories */
        p.load();
        waitTilDone();
        if ( stripComments && reformat ) {
            System.out.println( "stripping out comments and reformatting..." );
            stripAndReformat( p );
        } else if ( stripComments ) {
            System.out.println( "stripping out comments..." );
            stripAndReformat( p );
        } else if ( reformat ) {
            System.out.println( "reformatting..." );
            stripAndReformat( p );
        }

        // set the saved file name, then start file saving thread
        if ( doGzip ) {
            String name = null;
            if ( preserveNames ) {
                name = p.getLocalName();
            } else {
                name = p.generateGzipName();
            }
            String saveFile = getOutdirName( name );
            System.out.println( "Saving file in gzip format to " + saveFile );
            p.setFile( new File( saveFile ));
            p.gzipSave();
        } else {
            String name = null;
            if ( preserveNames ) {
                name = p.getLocalName();
            } else {
                name = p.generateChiseledName();
            }
            String saveFile = getOutdirName( name );
            System.out.println( "Saving file to " + saveFile );
            p.setFile( new File( saveFile ));
            p.asciiSave();
        }

        // Wait for file saving thread to complete
        p.waitForSave();
        p.wipeout();
        p = null;
    }

    /** Get the name of a file by prepending output directory name, if any */
    String getOutdirName( String fileName ) {
        if ( outputDir == null ) {
            return( fileName );
        } else {
            return( outputDir + fileName );
        }
    }
            
    void waitTilDone() {
        suspend();
    }

    void doneCallback() {
        resume();
    }

    /** Process a file with a single chisel */
    void processUsing( ProcessedFile p, Optimizer chisel ) {
        RangeReplacer rr = new RangeReplacer();
        Scene vrmlScene = p.getScene();
        TokenEnumerator sceneTokenEnumerator = vrmlScene.getTokenEnumerator();
        NodeLocatorVisitor nlv = new NodeLocatorVisitor( sceneTokenEnumerator );
        chisel.setRangeReplacer( rr );
        chisel.setDataSource( sceneTokenEnumerator );
        nlv.addNodeLocatorListener( chisel );
        System.out.println( chisel.getActionMessage() );
        vrmlScene.traverse( nlv );
        TokenEditor te = rr.recreateTokenStream( "",
            (TokenEditor)sceneTokenEnumerator, vrmlScene.getFirstTokenOffset(),
            vrmlScene.getLastTokenOffset(), null );
        rr.wipeout();
        p.setTokenEditor( te );
        p.setScene( vrmlScene );
        sceneTokenEnumerator.wipeout();
    }
}

class EngineRunner extends FactoryResponseAdapter {
    ChiselEngine callback;
    EngineRunner( ChiselEngine callback ) {
        this.callback = callback;
    }

	public void done( FactoryData result ) {
	    callback.doneCallback();
	}
}
