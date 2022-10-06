/*
 * @(#)vorlon.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium;

import com.trapezium.vorlon.LintVisitor;
import com.trapezium.vrml.visitor.UrlVisitor;
import com.trapezium.vrml.visitor.DumpVisitor;
import com.trapezium.vrml.visitor.ComplexityData;
import com.trapezium.vrml.visitor.ComplexityVisitor;
import com.trapezium.vrml.node.NodeType;
import com.trapezium.vrml.grammar.*;
import com.trapezium.parse.TextLineParser;
import com.trapezium.parse.TextLineEnumerator;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.parse.InputStreamFactory;
import com.trapezium.vrml.*;
import com.trapezium.vrml.node.PROTO;
import java.util.Vector;
import java.io.*;
import com.trapezium.vrml.node.humanoid.SpecHumanoid;
import com.trapezium.vrml.node.humanoid.HumanoidVisitor;
import com.trapezium.chisel.RangeReplacer;
import com.trapezium.chisel.NodeLocatorVisitor;
import com.trapezium.chisel.Optimizer;
import com.trapezium.chisel.TokenPrinter;
import com.trapezium.util.WildCardFilter;


/**
 *  The main class for the Vorlon 1.2 validator.
 *
 *  Note:  The VRML 2.0 header is validated separately.  This is because
 *  the "-url" option checks nested urls.  However, nested url files may
 *  in some cases be HTML files, which are handled in a different way.
 *
 *  What this means, is at the moment, the VRML97parser does not validate
 *  the VRML 2.0 header comment.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.22, 4 Nov 1998, minor grammar bug fixes and wild card handling
 *  @version         1.2, 22 June 1998
 *  @version         1.12, 3 March 1998
 *
 *  @since           1.0
 */
public class clvorlon {
	static final String VRML2header = "#VRML V2.0 utf8";
	static final String VRML1header = "#VRML V1.0 ascii";
	static final String VRMLAnyHeader = "#VRML";
	static final String version = "Vorlon 97 v1.52";
	static final String trapeziumHeader = "Copyright (c) 1998-1999 by Trapezium Development LLC.  All rights reserved.";
	static Vector processedFiles = new Vector();
	static boolean import_file = false;
	static boolean binary_import_file = false;
	static boolean showTimes = false;
	static boolean displaySceneGraph = false;
	static SpecHumanoid humanoid = null;
	static int vrml1count = 0;
	static int vrml2count = 0;
	static ComplexityData globalCD = new ComplexityData();
	static Vector additionalFiles = new Vector();   // mks and possibly unix expand wild cards for us
	static boolean didUsage = false;
	static Vector chiselNames = new Vector();
	static Vector chisels = new Vector();
	static public boolean chisel = false;
	static public boolean prettyPrint = false;
	static public boolean printHeader = true;
	static public boolean ifsInfo = false;
	static public boolean urlReading = false;
	static public boolean outputToFile = false;
	// have to set this to false when vorlon delivered
	static public boolean enableChisel = true;


   /**
    *  Main program for Vorlon VRML 2.0 syntax checker.
    *
    *  Command line parameters are:
    *  <P>filename [filename*] -- one or more file names
    *  <P>-url -- checks all nested urls
    *  <P>-nowarning -- do not show warning messages
    *  <P>-noDEFwarning -- do not show unused DEF warning messages
    *  <P>-out -- output info from <file>.wrl to <file>.out
    *  <P>-humanoid -- validate against humanoid animation spec
    *
    *  @param args      command line parameters
    */
	public static void main(String[] args) {

        String listfile = null;
		// initialize default field values
		if ( args.length == 0 ) {
			usage();
			System.exit( 1 );
		} else {
			try {
			    int starti = 0;
				if ( args[0].indexOf( "=" ) > 0 ) {
//               		System.out.println( version );
//               		System.out.println( trapeziumHeader );
					listfile = args[0].substring( args[0].indexOf( "=" ) + 1 );
//					printHeader = false;
					starti = 1;
				} 
				urlReading = false;
				for ( int i = starti; i < args.length; i++ ) {
					if ( args[i].compareTo( "-url" ) == 0 ) {
						urlReading = true;
					} else if ( args[i].compareTo( "-nurbs" ) == 0 ) {
					    VRML97.enableNurbs();
					    NodeType.enableNurbs();
					} else if ( args[i].compareTo( "-out" ) == 0 ) {
					    outputToFile = true;
					} else if ( args[i].compareTo( "-time" ) == 0 ) {
					    showTimes = true;
					} else if ( args[i].compareTo( "-nowarning" ) == 0 ) {
						VrmlElement.nowarning = true;
					} else if ( args[i].compareTo( "-nowarnings" ) == 0 ) {
						VrmlElement.nowarning = true;
					} else if ( args[i].compareTo( "-noDEFwarning" ) == 0 ) {
					    VrmlElement.noUnusedDEFwarning = true;
					} else if ( args[i].compareTo( "-noDEFwarnings" ) == 0 ) {
					    VrmlElement.noUnusedDEFwarning = true;
					} else if ( args[i].compareTo( "-ifs" ) == 0 ) {
					    ifsInfo = true;
					} else if ( args[i].compareTo( "-humanoid" ) == 0 ) {
						humanoid = new SpecHumanoid( "humanoid1.1");
						if ( humanoid.failedLoading() ) {
							humanoid = null;
						} else {
							PROTO.suppressISwarning = true;
						}
					} else if ( args[i].compareTo( "-humanoid10" ) == 0 ) {
						humanoid = new SpecHumanoid( "humanoid1.0");
						if ( humanoid.failedLoading() ) {
							humanoid = null;
						} else {
							PROTO.suppressISwarning = true;
						}
					} else if ( args[i].compareTo( "-pp" ) == 0 ) {
						prettyPrint = true;
						printHeader = false;
					} else if ( args[i].compareTo( "-fast" ) == 0 ) {
					    NodeType.verifyDisabled = true;
					} else if ( args[i].compareTo( "-conformance" ) == 0 ) {
					    VrmlElement.baseProfile = true;
					} else if (( args[i].compareTo( "-chisel" ) == 0 ) && enableChisel ) {
						i++;
							// by default white-space chisel always available
						chisel = true;
						for ( ; i < args.length; i++ ) {
							if ( args[i].indexOf( "-" ) == 0 ) {
								i--;
								break;
							}
							addChisel( args[i] );
						}
					} else if ( args[i].compareTo( "-noheader" ) == 0 ) {
					    printHeader = false;
					} else if ( args[i].compareTo( "-import" ) == 0 ) {
						import_file = true;
					} else if ( args[i].compareTo( "-fetch" ) == 0 ) {
						binary_import_file = true;
					} else if ( args[i].compareTo( "-graph" ) == 0 ) {
					    displaySceneGraph = true;
					} else if ( args[i].compareTo( "-vorlon" ) == 0 ) {
					    // ignore this, flag used by chisel to call vorlon directly
					} else if ( args[i].indexOf( "-" ) == 0 ) {
						System.out.println( "Unknown flag: " + args[i] );
						if ( !didUsage ) {
							usage();
							didUsage = true;
						}
					} else {
						additionalFiles.addElement( args[i] );
					}
				}
       			if ( printHeader ) {
               		System.out.println( version );
               		System.out.println( trapeziumHeader );
               	}
  				for ( int i = 0; i < additionalFiles.size(); i++ ) {
   					String fileName = (String)additionalFiles.elementAt( i );
   					if ( urlReading ) {
       					processFile( fileName, new UrlVisitor( fileName ));
   	   				} else {
   	    				processFile( fileName, null );
   		    		}
   			    }
   				if ( globalCD.getFileCount() > 1 ) {
   					globalCD.summary( System.out );
   				}
				//System.out.println( "Saw references to  " + vrml1count + " VRML 1.0 files" );
				//System.out.println( "Checked " + vrml2count + " VRML 2.0 files" );
				//CategoryVisitor cv = new CategoryVisitor( vrmlTokenEnumerator.getLines() );
				//vrmlScene.traverse( cv );
				//cv.summary();
			} catch ( FileNotFoundException e ) {
				System.out.println( "Unable to open file '" + args[0] + "'" );
			} catch ( ClassNotFoundException e ) {
				System.out.println( "Java version 1.1 or later required to read gzip encoded files" );
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		if ( listfile != null ) {
    		processFileList( listfile );
   		}
		if ( globalCD.getErrorCount() > 0 ) {
		    System.exit( 1 );
		} else {
		    System.exit( 0 );
		}
	}

    /**
     *  Process a list of file names contained in a file.
     *
     *  @param fileName   the local disk file containing a list of file names
     */
	static void processFileList( String fileName ) {
		try {
			Runtime rt = Runtime.getRuntime();
			TextLineParser tlp = new TextLineParser( new File( fileName ));
			Vector lines = tlp.getLines();
			int filesChecked = 0;
			int errorTotal = 0;
			long totalStartTime = 0;
			for ( int i = 0; i < lines.size(); i++ ) {
				TokenEnumerator tp = new TokenEnumerator( (String)lines.elementAt( i ));
				int t1offset = tp.getNextToken();
				String s1 = null;
				if ( t1offset != -1 ) {
					s1 = tp.toString( t1offset );
				}
				if ( s1 != null ) {
					long fileTime = 0;
					try {
						filesChecked++;
						long startTime = System.currentTimeMillis();
						if ( totalStartTime == 0 ) {
							totalStartTime = startTime;
						}
						if ( urlReading ) {
    						errorTotal += processFile( s1, new UrlVisitor( s1 ));
    					} else {
    						errorTotal += processFile( s1, null );
    					}
						long endTime = System.currentTimeMillis();
						long totalTime = ( endTime - totalStartTime )/1000;
						fileTime = ( endTime - startTime )/1000;
						if ( showTimes ) {
							System.out.println( "Time: " + ( endTime - startTime )/1000 + " seconds, total " + totalTime );
						}
					} catch ( FileNotFoundException e ) {
						System.out.println( "File not found" );
					} catch ( ClassNotFoundException e ) {
						System.out.println( "Java version 1.1 or later required to read gzip encoded files" );
					} catch ( Exception e ) {
						System.out.println( "Unexpected error: " + e.toString() );
					}
				}
			}
			globalCD.summary( System.out );
		} catch ( FileNotFoundException e ) {
			System.out.println( "Could not find file '" + fileName + "'" );
		} catch( Exception e ) {
			System.out.println( "Unknown exception: " + e.toString() );
		}
	}


    /**
     *  Import a url to the local name "imported".
     *
     *  Nearly obsolete, "fetchFile" does the same preserving file names.  Only difference
     *  is that this unzips gzips files as it fetches them.
     *
     *  @param inFile  url to import
     */
	static void importFile( String inFile ) throws FileNotFoundException, IOException, ClassNotFoundException {
		System.out.println( "Importing '" + inFile + "'" );
		InputStream inputStream = InputStreamFactory.getInputStream( inFile );
		TextLineParser tlp = new TextLineParser( inputStream );
		TextLineEnumerator tle = new TextLineEnumerator( tlp );
		FileOutputStream f = new FileOutputStream( "imported" );
		PrintStream ps = new PrintStream( f );
		while ( tle.hasMoreElements() ) {
			String s = (String)tle.nextElement();
			ps.println( s );
		}
		ps.close();
		f.close();
	}

    /**
     *  Create a local copy of a url
     *
     *  The copy is placed in the current directory with the same name as the file
     *  has at its remote location.
     *
     *  @param inFile  url to fetch
     */
	static void fetchFile( String inFile ) throws FileNotFoundException, IOException, ClassNotFoundException {
		if ( inFile.indexOf( "http:" ) != 0 ) {
			System.out.println( "Fetch requires a url" );
			return;
		}
		System.out.println( "Fetching '" + inFile + "'" );
		InputStream inputStream = InputStreamFactory.getRawInputStream( inFile );
		if ( inputStream == null ) {
		    System.out.println( "Cannot fetch file." );
		    return;
		}
		String outFileName = inFile.substring( inFile.lastIndexOf( '/' ) + 1, inFile.length() );
		FileOutputStream outputStream = null;
		try {
			if ( outFileName.length() == 0 ) {
				outFileName = "fetched";
			}
			outputStream = new FileOutputStream( outFileName );
		} catch ( Exception e ) {
			System.out.println( "Failed to open output file '" + outFileName + "'" );
			return;
		}
		byte[] buf = new byte[4096];
		int count;
		while (( count = inputStream.read( buf )) >= 0 ) {
			if ( count == 4096 ) {
				outputStream.write( buf );
			} else if ( count > 0 ) {
				outputStream.write( buf, 0, count );
			}
		}
		outputStream.close();
	}

    //
    //  The following code is related to an undocumented vorlon feature
    //  of checking "wrl" files referenced by html files.
    //
	static Vector hrefFiles = new Vector();
	static Vector processedHrefFiles = new Vector();
	static String firstFile = null;
	static String firstFileFilter = null;

    /**
     *  Does an href refer to the same domain name as the first file?
     *
     *  To prevent walking all over the internet, only hrefs in the same domain are checked.
     *
     *  @param   hrefFile   the url referred to in an HREF statement
     *  @return             true if the hrefFile is in same domain as first file,
     *                      otherwise false
     */
	static boolean isAcceptable( String hrefFile ) {
		if ( firstFileFilter == null ) {
			return( false );
		}
		return( hrefFile.indexOf( firstFileFilter ) > 0 );
	}

	/**
	 *  Locate all the HREF statements in a file and extract url references.
	 *
	 *  @param   inFile     the html file to check
	 *  @param   v          a TokenEnumerator containing all the tokens of the file
	 */
	static void locateHREFs( String inFile, TokenEnumerator v ) {
		if ( firstFile == null ) {
			firstFile = inFile;
			if ( inFile.indexOf( "." ) > 0 ) {
				if ( inFile.indexOf( "." ) != inFile.lastIndexOf( "." )) {
					firstFileFilter = inFile.substring( inFile.indexOf( "." ) + 1, inFile.lastIndexOf( "." ));
				}
			}
		}
		for ( int i = 0; i < v.getNumberLines(); i++ ) {
			String line = v.getLineAt( i );
			if ( line.indexOf( "href" ) >= 0 ) {
				extractFile( inFile, line, hrefFiles );
			} else if ( line.indexOf( "HREF" ) >= 0 ) {
				extractFile( inFile, line, hrefFiles );
			}
		}
	}

    /**
     *  Extract a url reference from a line in an html file.
     *
     *  If a url reference is found, it is added to a vector of file names.
     *
     *  @param   inFile   the url containing the lines
     *  @param   line     the line from the url that we are checking
     *  @param   files    vector of Strings, any url reference found is placed here
     */
	static void extractFile( String inFile, String line, Vector files ) {
		int hrefIdx = line.indexOf( "href" );
		if ( hrefIdx == -1 ) {
			hrefIdx = line.indexOf( "HREF" );
		}
		if ( hrefIdx == -1 ) {
			return;
		}
		StringBuffer sb = new StringBuffer();

		// skip to first quote
		int i = 0;
		int len = line.length();
		for ( i = hrefIdx; i < len; i++ ) {
			if ( line.charAt( i ) == '"' ) {
				break;
			}
		}
		if ( i == len ) return;
		i++;
		if ( i == len ) return;

		// copy chars up to next quote
		for ( ; i < len; i++ ) {
			if ( line.charAt( i ) == '"' ) {
				break;
			}
			sb.append( line.charAt( i ));
		}
		String result = new String( sb );
		if ( result.indexOf( '#' ) != -1 ) {
			result = result.substring( 0, result.indexOf( '#' ));
			if ( result.length() == 0 ) {
				return;
			}
		}

		// only add it if its not already there
		for ( i = 0; i < files.size(); i++ ) {
			String test = (String)files.elementAt( i );
			if ( result.compareTo( test ) == 0 ) {
				return;
			}
		}
		for ( i = 0; i < processedHrefFiles.size(); i++ ) {
			String test = (String)processedHrefFiles.elementAt( i );
			if ( result.compareTo( test ) == 0 ) {
				return;
			}
		}
		if ( result.indexOf( "mailto" ) == 0 ) {
			return;
		}
		if ( result.indexOf( "MAILTO" ) == 0 ) {
			return;
		}
		if ( result.indexOf( ":" ) == -1 ) {
			if ( inFile.indexOf( ".com" ) == ( inFile.length() - 4 )) {
				if ( inFile.lastIndexOf( "/" ) == ( inFile.length() - 1 )) {
					result = inFile + result;
				} else if ( result.charAt( 0 ) == '/' ) {
					result = inFile + result;
				} else {
					result = inFile + "/" + result;
				}
			} else {
				if ( result.charAt( 0 ) == '/' ) {
					result = inFile.substring( 0, inFile.lastIndexOf( '/' )) + result;
				} else {
					result = inFile.substring( 0, inFile.lastIndexOf( '/' ) + 1 ) + result;
				}
			}
		}
		if (( result.indexOf( ".htm" ) == -1 ) && ( result.indexOf( ".HTM" ) == -1 ) &&
			( result.indexOf( ".wrl" ) == -1 ) && ( result.indexOf( ".WRL" ) == -1 ) &&
			( result.lastIndexOf( "/" ) != ( result.length() - 1 ))) {
			return;
		}
		files.addElement( result );
	}

    /**
     *  Process a url, actual processing depends on command line.
     *
     *  Options are:
     *  <P>
     *  <B>-import</B> creates a local file from the url called <B>imported</B>
     *  <P>
     *  <B>-fetch</B> creates a local file from the url with the same name as it has remotely.
     *  <P>
     *  <B><filename></B> any number of file names can be specified on the command line,
     *  here they are processed one by one.
     *  <P>
     *  <B><wild card filename></B> limited wildcard support.  In some cases the operating
     *  system itself converts the wild cards into a list of file names, in which case we
     *  never see the wild cards.  In other cases, we support only "*" in a limited way for
     *  the local disk.
     *
     *  @param   inFile   String indicating the file or files to process
     *  @param   urlVisitor  when the <B>-url</B> flag is given on the command line, this
     *                       visitor is created to traverse the scene graph of any parsed file,
     *                       and locate any urls in Anchor or Inline nodes.  These are then
     *                       added to the list of files left to be processed.
     */
	static int processFile( String inFile, UrlVisitor urlVisitor ) throws FileNotFoundException, IOException, ClassNotFoundException {
		for ( int i = 0; i < processedFiles.size(); i++ ) {
			String testName = (String)processedFiles.elementAt( i );
			if ( testName.compareTo( inFile ) == 0 ) {
				return( 0 );
			}
		}
		if ( import_file ) {
			importFile( inFile );
			return( 0 );
		}
		if ( binary_import_file ) {
			fetchFile( inFile );
			return( 0 );
		}

        if ( !prettyPrint ) {
    		System.out.println( "Checking '" + inFile + "'" );
    	}
		if ( WildCardFilter.isWild( inFile )) {
			File thisDir = new File( "." );
	        String[] files = thisDir.list( new WildCardFilter( inFile ));
			int total = 0;
			for ( int i = 0; i < files.length; i++ ) {
				total += processFile( files[i], urlVisitor );
			}
			return( total );
		} else {
			try {
				InputStream is = InputStreamFactory.getInputStream( inFile );
				return( processInputStream( inFile, urlVisitor, is ));
			} catch ( NoClassDefFoundError eee ) {
			    eee.printStackTrace();
				return( 0 );
			} catch( FileNotFoundException e ) {
			    throw( e );
			} catch( Exception e ) {
			    e.printStackTrace();
				return( 0 );
			}
		}
	}

    /**
     *  Process a single input stream, either a vrml or html file.
     *
     *  @param   inFile  the url used to create the input stream
     *  @param   urlVisitor  used to add Anchor and Inline url references to list of files
     *                       to be processed, if null url references ignored
     *  @param   is      the InputStream created from the url
     */
	static int processInputStream( String inFile, UrlVisitor urlVisitor, InputStream is ) throws FileNotFoundException, IOException, ClassNotFoundException {
		Scene vrmlScene = null;
		boolean isHTML = false;
		TokenEnumerator vrmlTokenEnumerator = new TokenEnumerator( is, inFile );
		if ( urlVisitor != null ) {
		    urlVisitor.setDataSource( vrmlTokenEnumerator );
		}
		LintVisitor lv = new LintVisitor( vrmlTokenEnumerator );
		String line0 = vrmlTokenEnumerator.getLineAt( 0 );
		ComplexityData cd = new ComplexityData();
		ComplexityVisitor cv = new ComplexityVisitor( cd, vrmlTokenEnumerator );
		if ( line0.indexOf( VRML2header ) != 0 ) {
			if (( line0.indexOf( "HTML" ) == -1 ) && ( line0.indexOf( "html" ) == -1 ) &&
			    ( inFile.indexOf( ".htm" ) == -1 ) && ( inFile.indexOf( ".HTM" ) == -1 )) {
				lv.showError( "Header must be '" + VRML2header + "'", 0, 0 );
			} else {
				isHTML = true;
			}
			if ( line0.indexOf( VRML1header ) == 0 ) {
				vrml1count++;
			} else if ( line0.indexOf( VRMLAnyHeader ) != -1 ) {
//				System.out.println( "Looks like I got a VRML file" );
			} else if ( isHTML ) {
				locateHREFs( inFile, vrmlTokenEnumerator );
				processedFiles.addElement( inFile );
				if ( urlVisitor == null ) {
					urlVisitor = new UrlVisitor( inFile );
				}
			}
		} else {
			vrml2count++;
			vrmlScene = new Scene( inFile, vrmlTokenEnumerator );
			VRML97parser parser = new VRML97parser();
			parser.Build( vrmlTokenEnumerator, vrmlScene );
			if ( urlVisitor != null ) {
				urlVisitor = new UrlVisitor( inFile );
				urlVisitor.setDataSource( vrmlTokenEnumerator );
				vrmlScene.traverse( urlVisitor );
				processedFiles.addElement( inFile );
			}
			if ( !prettyPrint ) {
    			NodeType.verifyUsage( vrmlScene.getUsageTable(), vrmlScene.getTokenEnumerator(), vrmlScene.getErrorSummary() );
    			if ( humanoid != null ) {
    				HumanoidVisitor hv = new HumanoidVisitor( humanoid, vrmlTokenEnumerator );
    				vrmlScene.traverse( hv );
    				hv.summarize( System.out );
    			}
    			vrmlScene.traverse( lv );
	    		vrmlScene.traverse( cv );
	    	} else {
				TokenPrinter tp = new TokenPrinter( System.out, vrmlTokenEnumerator );
				tp.doPrettyPrint();
				Vector v = new Vector();
				tp.printRange( vrmlScene.getFirstTokenOffset(), vrmlScene.getLastTokenOffset(), true );
				tp.flush();
				for ( int i = 0; i < v.size(); i++ ) {
					String s = (String)v.elementAt( i );
					System.out.println( s );
				}
				return( 0 );
			}
			if ( displaySceneGraph ) {
    			DumpVisitor dv = new DumpVisitor( System.out, vrmlTokenEnumerator );
    			vrmlScene.traverse( dv );
    		}
		}
		if ( !isHTML ) {
		    if ( outputToFile ) {
		        try {
    		        String outFile = getOutFile( inFile );
    		        PrintStream fos = new PrintStream( new FileOutputStream( new File( outFile )));
    		        lv.summary( fos );
    		        fos.flush();
    		        fos.close();
    		    } catch ( Exception e ) {
    		        System.out.println( "** Exception: " + e );
    		    }
		    } else {
    			lv.summary( System.out );
    		}
			cv.setWarningCount( lv.getWarningCount() );
			cv.setErrorCount( lv.getErrorCount() );
			cv.setNonconformanceCount( lv.getNonconformanceCount() );
			cv.summary( System.out );
			globalCD.addInfo( cv.getComplexityData() );
		}
		if ( chisel ) {
			RangeReplacer rr = new RangeReplacer();
			NodeLocatorVisitor nlv = new NodeLocatorVisitor( vrmlScene.getTokenEnumerator() );
//			Sweeper sweeper = new Sweeper( vrmlScene );
			createChisels( rr, nlv, vrmlScene.getTokenEnumerator() );
			vrmlScene.traverse( nlv );
			rr.writeFile( inFile + ".chiseled", vrmlScene.getTokenEnumerator() ); //sweeper );
			summarizeChisels( System.out );
			return( 0 );
		}
		int errorSum = 0;
		if ( urlVisitor != null ) {
			// Check each of the files found at this level
			for ( int i = 0; i < urlVisitor.getNumberFiles(); i++ ) {
				String uFile = urlVisitor.getFileAt( i );
				errorSum += processFile( uFile, urlVisitor );
			}
		}
		while ( hrefFiles.size() > 0 ) {
			String hrefFile = (String)hrefFiles.elementAt( 0 );
			if ( isAcceptable( hrefFile )) {
				try {
					errorSum += processFile( hrefFile, urlVisitor );
				} catch ( FileNotFoundException e ) {
					System.out.println( "Unable to open file." );
					errorSum++;
				} catch ( Exception e ) {
					System.out.println( "Unexpected error: " + e.toString() );
				}
			}
			processedHrefFiles.addElement( hrefFile );
			if ( hrefFiles.size() > 0 ) {
				hrefFiles.removeElementAt( 0 );
			}
		}
		vrmlTokenEnumerator.wipeout();
		Runtime.getRuntime().gc();

		return( lv.getErrorCount() );
	}

    static String getOutFile( String inFile ) {
        int dotIdx = inFile.lastIndexOf( "." );
        if ( dotIdx != -1 ) {
            String name = inFile.substring( 0, dotIdx );
            return( new String( name + ".out" ));
        } else {
            return( new String( inFile + ".out" ));
        }
    }
    
    /**
     *  print Vorlon command line instructions.
     */
	public static void usage() {
		System.out.println( "There are four ways to use the Vorlon v1.5 VRML 97 verifier:" );
		System.out.println( "" );
		System.out.println( "1. To check a single file, local or remote:" );
		System.out.println( "" );
		System.out.println( "   vorlon <url>" );
		System.out.println( "" );
		System.out.println( "2. To check several files:" );
		System.out.println( "" );
		System.out.println( "   vorlon *.wrl" );
		System.out.println( " or" );
		System.out.println( "   vorlon <file1.wrl> <file2.wrl> ...." );
		System.out.println( "" );
		System.out.println( "3. To check a specific list of files:" );
		System.out.println( "" );
		System.out.println( "   vorlon list=filelist" );
		System.out.println( "" );
		System.out.println( "   where 'filelist' contains one url per line." );
		System.out.println( "" );
		System.out.println( "4. To check a file, and all url references:" );
		System.out.println( "" );
		System.out.println( "   vorlon <file.wrl> -url" );
		System.out.println( "" );
		System.out.println( "Additional flags:" );
		System.out.println( "-fetch        make a local copy of url without checking" );
		System.out.println( "-humanoid     verify conformance to h-anim spec 1.1" );
		System.out.println( "-humanoid10   verify conformance to h-anim spec 1.0" );
		System.out.println( "-nowarning    suppress warning messages" );
		System.out.println( "-noDEFwarning suppress unused DEF warning messages" );
		System.out.println( "-out          output file check info to <fileName>.out" );
		System.out.println( "-conformance  verify VRML 97 base profile conformance only" );
		System.out.println( "-graph        show scene graph" );
		System.out.println( "-nurbs        check blaxxun nurbs nodes" );
	}

	/** add a chisel name and/or option to list of chisel names */
	static void addChisel( String name ) {
		chiselNames.addElement( name );
	}

	/** create the chisels, with options */
	static void createChisels( RangeReplacer rr, NodeLocatorVisitor nlv, 
		TokenEnumerator v ) {
		Optimizer lastChisel = null;
		for ( int i = 0; i < chiselNames.size(); i++ ) {
			String chiselName = (String)chiselNames.elementAt( i );
			String chiselOption = null;
			if ( lastChisel != null ) {
				if ( chiselName.charAt( 0 ) == 'o' ) {
					chiselOption = chiselName;
					chiselName = null;
				}
			}
			if ( chiselName != null ) {
				Optimizer o = createChisel( chiselName );
				if ( o != null ) {
					lastChisel = o;
					o.setRangeReplacer( rr );
					o.setDataSource( v );
					nlv.addNodeLocatorListener( o );
					chisels.addElement( o );
					System.out.println( "created chisel " + chiselName );
				} else {
					System.out.println( "chisel " + chiselName + " not found" );
				}
			}
			if ( chiselOption != null ) {
				i++;
				if ( i < chiselNames.size() ) {
					String optionNumber = (String)chiselNames.elementAt( i );
					i++;
					if ( i < chiselNames.size() ) {
						String optionValue = (String)chiselNames.elementAt( i );
						lastChisel.setOptionValue( 
							Integer.parseInt( optionNumber ), optionValue );
					}
				}
			}
		}
	}

	static void summarizeChisels( PrintStream ps ) {
		for ( int i = 0; i < chisels.size(); i++ ) {
			Optimizer o = (Optimizer)chisels.elementAt( i );
			o.summarize( ps );
		}
	}

	static Optimizer createChisel( String name ) {
		Class nodeClass = null;
		try {
			if (( name.indexOf( "DEF" ) >= 0 ) && ( name.indexOf( "DEFremover" ) == -1 )) {
				nodeClass = Class.forName( "com.trapezium.chisel.condensers.DEFmaker" );
			} else {
				nodeClass = Class.forName("com.trapezium.chisel." + name);
			}
			if (nodeClass != null) {
				Optimizer o =  (Optimizer) (nodeClass.newInstance());
				if (( name.indexOf( "DEF" ) == 0 ) && ( name.indexOf( "DEFremover" ) == -1 ) && ( name.indexOf( "DEFmaker" ) == -1 )) {
					o.setNodeName( name.substring( 3 ));
				}
				return o;
			}
		} catch ( Exception e ) {
		    e.printStackTrace();
			return( null );
		}
		return( null );
	}
}

