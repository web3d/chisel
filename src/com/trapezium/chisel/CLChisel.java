/*
 * @(#)CLChisel.java
 *
 * Copyright (c) 1998-2000 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import com.trapezium.chisel.gui.AboutPanel;
import com.trapezium.util.WildCardFilter;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;


/** Main class for command line version of Chisel.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 4 Dec 1998
 *
 *  @since           1.0
 */

public class CLChisel {

    /** application directory */
    public static String appDirectory;

    //private static ChiselAccess access;

    private static final String VERSION = "$id: $)";

    public static void main(String[] args) {
		System.out.println( "Chisel " + VERSION );
		System.out.println( Chisel.trapeziumHeader );

        String javaversion = System.getProperty("java.version");
        if (javaversion.compareTo("1.1.2") < 0) {
            System.out.println("Warning: outdated Java VM; some features will not be available.");
            System.out.println("To enable all features run on a version 1.1.2 or higher VM.");
        }

        /////
        System.out.println("Java version " + javaversion);
        String classpath = System.getProperty("java.class.path");
        System.out.println("Classpath = " + classpath);

        appDirectory = "";
        StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String path = st.nextToken();
            if (path.toLowerCase().endsWith("chisel.jar")) {
                if ( path.length() > 10 ) {
                    appDirectory = path.substring( 0, path.length() - 10 );
                } else {
                    appDirectory = "";
                }
                break;
            }
        }
        System.out.println("Chisel directory: " + appDirectory);
        /////

        ChiselSet[] sets = ChiselSet.createChiselSets();

        Vector filesToProcess = new Vector();
        ChiselCommandList chiselCommandList = null;
        boolean doGzip = false;
        boolean preserveNames = false;
        boolean stripComments = false;
        boolean reformat = false;
        String outputDir = null;
		if ( args.length != 0 ) {
			for ( int i = 0; i < args.length; i++ ) {
                if ( WildCardFilter.isWild( args[i] )) {
                    File thisDir = new File( "." );
                    String[] files = thisDir.list( new WildCardFilter( args[i] ));
                    for ( int j = 0; j < files.length; j++ ) {
                        filesToProcess.addElement( files[j] );
                    }
                } else if ( isHtml( args[i] )) {
                    chiselCommandList = new ChiselCommandList( args[i], sets );
                } else if ( args[i].compareTo( "-gzip" ) == 0 ) {
                    doGzip = true;
                } else if ( args[i].compareTo( "-preserve" ) == 0 ) {
                    preserveNames = true;
                } else if ( args[i].compareTo( "-nocomment" ) == 0 ) {
                    stripComments = true;
                } else if ( args[i].compareTo( "-format" ) == 0 ) {
                    reformat = true;
                } else if ( args[i].compareTo( "-output" ) == 0 ) {
                    if (( i + 1 ) < args.length ) {
                        i++;
                        outputDir = args[i];
                    }
                } else {
                    filesToProcess.addElement( args[i] );
                }
			}
	    }

	    // If there is no HTML template file, print error message
        boolean exitFlag = false;
        if ( chiselCommandList == null ) {
            System.out.println( "No HTML report file found." );
            exitFlag = true;
        }
		if (( chiselCommandList == null ) || ( chiselCommandList.getNumberCommands() == 0 )) {
			System.out.println( "No chisel requests found in HTML report file." );
			exitFlag = true;
		}
        if ( filesToProcess.size() == 0 ) {
            System.out.println( "No files to process." );
            exitFlag = true;
        }
        if ( exitFlag ) {
            System.exit( 0 );
        }

        // Create the ChiselEngine and start it running
        ChiselEngine theEngine = new ChiselEngine( filesToProcess, chiselCommandList, doGzip, outputDir, preserveNames, stripComments, reformat );
        theEngine.start();
        try {
            theEngine.join();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
        System.exit( theEngine.getResult() );
    }


    static boolean isHtml( String fileName ) {
        String lowerFileName = fileName.toLowerCase();
        return( (lowerFileName.indexOf( ".htm" ) > 0) || (lowerFileName.indexOf( ".html" ) > 0) );
    }
 }

