/*
 * @(#)ComplexityData.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import java.io.PrintStream;
import java.util.Vector;

/**
 *  Information related to complexity of a scene graph.
 *  <P>
 *  This is used to accumulate error and polygon count information during
 *  the processing of files.  The ComplexityVisitor contains a ComplexityData
 *  element where it stores its information.  
 *  <P>
 *  If several files are being processed, a single global ComplexityData
 *  object is used to contain the sum of their information.  Each
 *  time a ComplexityVisitor completes its traversal of the scene graph,
 *  it updates the global ComplexityData with the data from that single
 *  traversal (see the "addInfo" method).
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 14 Jan 1998
 *
 *  @since           1.0
 */
public class ComplexityData {
	int polygonCount;
	int coneCount;
	int sphereCount;
	int cylinderCount;
	int fileCount;    // used when this is also a summary for alot of files
	int warningCount;
	int errorCount;
	int nonconformanceCount;
	Vector inlineFileList;

    /** class constructor */
	public ComplexityData() {
		polygonCount = 0;
		coneCount = 0;
		sphereCount = 0;
		cylinderCount = 0;
		nonconformanceCount = 0;
		warningCount = 0;
		errorCount = 0;
		fileCount = 0;
	}

    /** add an inline file if not already in the list */
    public void incInline( String inlineUrl ) {
        if ( inlineFileList == null ) {
            inlineFileList = new Vector();
        }
        int inlineFileListSize = inlineFileList.size();
        for ( int i = 0; i < inlineFileListSize; i++ ) {
            String test = (String)inlineFileList.elementAt( i );
            if ( test.compareTo( inlineUrl ) == 0 ) {
                return;
            }
        }
        inlineFileList.addElement( inlineUrl );
    }
    
    /** how many inline files were found? */
    public int getInlineCount() {
        if ( inlineFileList == null ) {
            return( 0 );
        } else {
            return( inlineFileList.size() );
        }
    }

    /** how many files were processed */
	public int getFileCount() {
		return( fileCount );
	}

    /** increment polygon count */
	public void incPolygonCount( int inc ) {
		polygonCount += inc;
	}

    /** increment cone count */
	public void incConeCount() {
		coneCount++;
	}

    /** increment sphere count */
	public void incSphereCount() {
		sphereCount++;
	}

    /** increment cylinder count */
	public void incCylinderCount() {
		cylinderCount++;
	}

    /** set the warning count, comes from LintVisitor */
	public void setWarningCount( int wc ) {
		warningCount = wc;
	}

    /** set the error count, comes from LintVisitor */
	public void setErrorCount( int ec ) {
		errorCount = ec;
	}
	
	/** Set the nonconformance count */
	public void setNonconformanceCount( int ncc ) {
	    nonconformanceCount = ncc;
	}

    /** how many polygons were found? */
	public int getPolygonCount() {
		return( polygonCount );
	}

    /** how many cones were found? */
	public int getConeCount() {
		return( coneCount );
	}

    /** how many spheres were found? */
	public int getSphereCount() {
		return( sphereCount );
	}

    /** how many cylinders were found? */
	public int getCylinderCount() {
		return( cylinderCount );
	}

    /** how many warnings were encountered? */
	public int getWarningCount() {
		return( warningCount );
	}

    /** how many errors were encountered? */
	public int getErrorCount() {
		return( errorCount );
	}
	
	/** get nonconformance count */
	public int getNonconformanceCount() {
	    return( nonconformanceCount );
	}

    /** add information from another complexity data object */
	public void addInfo( ComplexityData cd ) {
		fileCount++;
		polygonCount += cd.getPolygonCount();
		coneCount += cd.getConeCount();
		sphereCount += cd.getSphereCount();
		cylinderCount += cd.getCylinderCount();
		warningCount += cd.getWarningCount();
		nonconformanceCount += cd.getNonconformanceCount();
		errorCount += cd.getErrorCount();
	}

    /** print summary information to a PrintStream */
	public void summary( PrintStream ps ) {
		if ( fileCount > 1 ) {
			ps.println( "Summary for " + fileCount + " files:" );
			if (( warningCount == 1 ) && ( errorCount == 1 )) {
				ps.println( "1 warning, 1 error." );
			} else if (( warningCount == 1 ) && ( errorCount > 1 )) {
				ps.println( "1 warning, " + errorCount + " errors." );
			} else if (( warningCount > 1 ) && ( errorCount == 1 )) {
				ps.println( warningCount + " warnings, 1 error." );
			} else if (( warningCount > 1 ) && ( errorCount > 1 )) {
				ps.println( warningCount + " warnings, " + errorCount + " errors." );
			} else if (( warningCount == 0 ) && ( errorCount == 1 )) {
				ps.println( "1 error." );
			} else if (( warningCount == 0 ) && ( errorCount > 1 )) {
				ps.println( errorCount + " errors." );
			} else if (( warningCount == 1 ) && ( errorCount == 0 )) {
				ps.println( "1 warning." );
			} else if (( warningCount > 1 ) && ( errorCount == 0 )) {
				ps.println( warningCount + " warnings." );
			}
			if ( nonconformanceCount == 1 ) {
			    ps.println( "1 nonconformance." );
			} else if ( nonconformanceCount > 1 ) {
			    ps.println( nonconformanceCount + " nonconformances." );
			}
		}
		StringBuffer sb = new StringBuffer();
		boolean doComma = false;
		int totalCount = polygonCount;
		boolean approx = false;
		if ( sphereCount > 0 ) {
			totalCount += sphereCount*40;
			approx = true;
		}
		if ( coneCount > 0 ) {
			totalCount += coneCount*12;
			approx = true;
		}
		if ( cylinderCount > 0 ) {
			totalCount += cylinderCount*12;
			approx = true;
		}
		if ( totalCount > 0 ) {
			if ( totalCount == 1 ) {
				sb.append( "1 polygon" );
			} else {
				if ( approx ) {
					sb.append( "Approximately " );
				}
				sb.append( totalCount + " polygons" );
			}
			doComma = true;
		}
/*		if ( sphereCount > 0 ) {
			if ( doComma ) {
				sb.append( ", " );
			}
			if ( sphereCount == 1 ) {
				sb.append( "1 sphere" );
			} else {
				sb.append( sphereCount + " spheres" );
			}
			doComma = true;
		}
		if ( coneCount > 0 ) {
			if ( doComma ) {
				sb.append( ", " );
			}
			if ( coneCount == 1 ) {
				sb.append( "1 cone" );
			} else {
				sb.append( coneCount + " cones" );
			}
			doComma = true;
		}
		if ( cylinderCount > 0 ) {
			if ( doComma ) {
				sb.append( ", " );
			}
			if ( cylinderCount == 1 ) {
				sb.append( "1 cylinder" );
			} else {
				sb.append( cylinderCount + " cylinders" );
			}
			doComma = true;
		}*/
		if ( doComma ) {
			sb.append( "." );
		}
		String s = new String( sb );
		if ( s.length() > 0 ) {
			ps.println( s );
		} else if ( fileCount > 1 ) {
			ps.println( "No polygons." );
		}
	}
}

