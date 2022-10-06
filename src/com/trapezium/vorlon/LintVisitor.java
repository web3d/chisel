/*
 * @(#)LintVisitor.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vorlon;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.pattern.Visitor;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.SFStringValue;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.ROUTE;
import com.trapezium.vrml.visitor.ISLocator;

import java.io.PrintStream;
import java.util.Hashtable;

/**
 *  Visits the SceneGraph, collects error information line by line.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 9 Jan 1998
 *
 *  @since           1.0
 */
public class LintVisitor extends Visitor {

	/** Total number of errors collected */
	int errorCount;

	/** Total number of warnings collected */
	int warningCount;

    /** Total number of nonconformances collected */
    int nonconformanceCount;

	/** Total number of lines with errors */
	int errorLines;

	/** unused DEF count */
	transient Hashtable unusedDEF;

	/** default field count */
	transient Hashtable defaultFields;

	/** number of unused coordinates */
	transient Hashtable nodeTable;
	int unusedCoordCount;

	/** duplicate fields */
	transient Hashtable duplicateFields;

	/** number of unused PROTO interface fields */
	int unusedPROTOinterfaceFields;

    /** number of empty IndexedFaceSets */
    int emptyIndexedFaceSetCount;

    /** number of ElevationGrids */
    int elevationGridCount;

    /** number of Transforms */
    int transformCount;
    
    /** number of Normals */
    int normalCount;
    
    /** number of unindexed values */
    int unindexedValueCount;

	/** Unordered linked list of errors */
	transient protected ErrorInfo errorList;

    /** number of DEF nodes found */
    int defCount;

    /** number of USE nodes found */
    int useCount;

    /** number of repeated index values */
    int dupIndexCount;

    /** number of degenerate faces */
    int badFaceCount;

    /** number of repeated values */
    int repeatedValueCount;

    /** number of bad ROUTEs */
    int badRouteCount;
    
    /** number of duplicate ROUTEs */
    int duplicateRouteCount;
    
    /** number of unnecessary keyValus */
    int unnecessaryInterpolatorValueCount;
    
    /** number of interpolators */
    int interpolatorCount;
    
    /** number of empty interpolators and TimeSensors without DEFs */
    int uselessNodeCount;
    
    /** number of value nodes (Coordinate, Color, Texture, Normal) */
    int valueNodeCount;

    /** number of single colored IFSes */
    int singleColorIFScount;
    
    /** number of PROTO instances */
    int protoInstanceCount;
    
    /** For detecting IS fields within a VrmlElement */
    ISLocator isLocator;
    
	/** class constructor */
	public LintVisitor( TokenEnumerator v ) {
		super( v );
		isLocator = new ISLocator();
		errorCount = 0;
		warningCount = 0;
		nonconformanceCount = 0;
		singleColorIFScount = 0;
		dupIndexCount = 0;
		badFaceCount = 0;
		errorLines = 0;
		repeatedValueCount = 0;
		badRouteCount = 0;
		duplicateRouteCount = 0;
		normalCount = 0;
		interpolatorCount = 0;
		valueNodeCount = 0;
		unnecessaryInterpolatorValueCount = 0;
		errorList = null;
		unusedDEF = new Hashtable();
		defaultFields = new Hashtable();
		nodeTable = new Hashtable();
		duplicateFields = new Hashtable();
	}

	public TokenEnumerator getLineSource() {
		return( dataSource );
	}

	/** Get the total number of warnings counted */
	public int getWarningCount() {
		return( warningCount );
	}

	/** Get the total number of errors counted. */
	public int getErrorCount() {
		return( errorCount );
	}
	
	public int getValueNodeCount() {
	    return( valueNodeCount );
	}
	
	public int getInterpolatorCount() {
	    return( interpolatorCount );
	}
	
	public int getSingleColorIFScount() {
	    return( singleColorIFScount );
	}

	/** Get the nonconformance count */
	public int getNonconformanceCount() {
	    return( nonconformanceCount );
	}

	/** Get the number of repeated index values in faces */
	public int getDupIndexCount() {
	    return( dupIndexCount );
	}

	/** Get the number of bad faces */
	public int getBadFaceCount() {
	    return( badFaceCount );
	}

	/** Get the number of repeated values */
	public int getRepeatedValueCount() {
	    return( repeatedValueCount );
	}
	
	/** Get the number of unnecessary key Values */
	public int getUnnecessaryKeyValueCount() {
	    return( unnecessaryInterpolatorValueCount );
	}

	/** Get the total number of DEF nodes */
	public int getDEFcount() {
	    return( defCount );
	}

	/** Get the total number of USE nodes */
	public int getUSEcount() {
	    return( useCount );
	}

	/** Get the number of unused DEFs */
	public int getUnusedDEFCount() {
	    return( unusedDEF.size() );
	}

	/** Get the number of default valued fields */
	public int getDefaultFieldCount() {
	    return( defaultFields.size() );
	}

	/** Get the number of empty IndexedFaceSets */
	public int getEmptyIndexedFaceSetCount() {
	    return( emptyIndexedFaceSetCount );
	}

	/** Get the number of elevation grids */
	public int getElevationGridCount() {
	    return( elevationGridCount );
	}

	public int getTransformCount() {
	    return( transformCount );
	}
	
	public int getUnindexedValueCount() {
	    return( unindexedValueCount );
	}
	
	public int getNormalCount() {
	    return( normalCount );
	}

	public int getBadRouteCount() {
	    return( badRouteCount );
	}
	
	public int getDuplicateRouteCount() {
	    return( duplicateRouteCount );
	}

	/** Get the number of duplicate fields */
	public int getDuplicateFieldCount() {
	    return( duplicateFields.size() );
	}

	/** Get the number of unused coordinates */
	public int getUnusedCoordCount() {
	    return( unusedCoordCount );
	}

	/** Get the number of unused PROTO interface fields */
	public int getUnusedPROTOinterfaceCount() {
	    return( unusedPROTOinterfaceFields );
	}
	
	public int getUselessNodeCount() {
	    return( uselessNodeCount );
	}
	
	public int getPROTOInstanceCount() {
	    return( protoInstanceCount );
	}

	/** Visitor interface, collects error information for each scene
	 *  graph component.
	 */
	boolean usingErrorSummary = false;
	public boolean visitObject( Object a ) {
	    if ( a instanceof PROTOInstance ) {
	        protoInstanceCount++;
	    }
	    // first time Scene with ErrorSummary encountered, get counts
	    // from the summary object
	    if (( a instanceof Scene ) && !usingErrorSummary ) {
	        ErrorSummary errorSummary = ((Scene)a).getErrorSummary();
	        if ( errorSummary != null ) {
	            usingErrorSummary = true;
	            int errorSummaryLimit = errorSummary.getWarningLimit();
	            int temp = errorSummary.getCount( "not in" );
	            unusedCoordCount += temp;
	            if ( temp > errorSummaryLimit ) {
	                warningCount += temp;
	                warningCount -= errorSummaryLimit;
	            }
	            temp = errorSummary.getCount( "repeated index in" );
	            dupIndexCount += temp;
	            if ( temp > errorSummaryLimit ) {
	                warningCount += temp;
	                warningCount -= errorSummaryLimit;
	            }
	            temp = errorSummary.getCount( "at least 3 edges" );
	            badFaceCount += temp;
	            if ( temp > errorSummaryLimit ) {
	                warningCount += temp;
	                warningCount -= errorSummaryLimit;
	            }
                temp = errorSummary.getCount( "repeated value" );
	            repeatedValueCount += temp;
	            if ( temp > errorSummaryLimit ) {
	                warningCount += temp;
	                warningCount -= errorSummaryLimit;
	            }
	            temp = errorSummary.getCount( "unnecessary keyValue" );
	            unnecessaryInterpolatorValueCount += temp;
	            if ( temp > errorSummaryLimit ) {
	                warningCount += temp;
	                warningCount -= errorSummaryLimit;
	            }
	            temp = errorSummary.getCount( "unused key" );
	            unnecessaryInterpolatorValueCount += temp;
	            if ( temp > errorSummaryLimit ) {
	                warningCount += temp;
	                warningCount -= errorSummaryLimit;
	            }
	        }
	    }
		if ( a instanceof VrmlElement ) {
			VrmlElement vle = (VrmlElement)a;
			boolean result = true;
			if ( vle instanceof Node ) {
			    Node n = (Node)vle;
			    if ( nodeTable.get( n ) == null ) {
			        nodeTable.put( n, n );
			    } else {
			        return( false );
			    }
			    String baseName = n.getBaseName();
			    String nodeError = n.getError();
			    if ( baseName.compareTo( "IndexedFaceSet" ) == 0 ) {
			        String ifserr = nodeError;
			        if ( ifserr != null ) {
			            if ( ifserr.indexOf( "in diffuseColor" ) > 0 ) {
			                singleColorIFScount++;
			            }
			        }
			        isLocator.reset();
			        n.traverse( isLocator );
			        if ( !isLocator.foundISField() )  {
    			        Field coordIndex = n.getField( "coordIndex" );
                		MFFieldValue coordIndexValue = null;
                		int idxCount = 0;
                		if ( coordIndex != null ) {
                		    coordIndexValue =(MFFieldValue)coordIndex.getFieldValue();
                		    if ( coordIndexValue != null ) {
                    		    idxCount = coordIndexValue.getRawValueCount();
                    		}
                   		}
                   		if ( idxCount == 0 ) {
                   		    emptyIndexedFaceSetCount++;
                   		}
                   	}
               	} else if ( baseName.compareTo( "ElevationGrid" ) == 0 ) {
               	    elevationGridCount++;
               	    valueNodeCount++;
               	} else if ( baseName.compareTo( "Extrusion" ) == 0 ) {
               	    valueNodeCount++;
               	} else if ( baseName.compareTo( "Transform" ) == 0 ) {
               	    transformCount++;
               	} else if ( baseName.compareTo( "Normal" ) == 0 ) {
               	    normalCount++;
               	    valueNodeCount++;
               	} else if ( baseName.indexOf( "Interpolator" ) > 0 ) {
               	    interpolatorCount++;
               	    Field key = n.getField( "key" );
               	    if ( key == null ) {
               	        uselessNodeCount++;
               	    } else if ( !( key.isISfield() )) {
               	        MFFieldValue keyFV = (MFFieldValue)key.getFieldValue();
               	        if ( keyFV == null ) {
               	            uselessNodeCount++;
               	        } else {
               	            if ( keyFV.getRawValueCount() < 2 ) {
               	                uselessNodeCount++;
               	            }
               	        }
               	    }
               	} else if ( baseName.compareTo( "Coordinate" ) == 0 ) {
               	    valueNodeCount++;
               	} else if ( baseName.compareTo( "Color" ) == 0 ) {
               	    valueNodeCount++;
               	} else if ( baseName.compareTo( "TextureCoordinate" ) == 0 ) {
               	    valueNodeCount++;
               	} else if ( nodeError != null ) {
               	    if ( baseName.compareTo( "TimeSensor" ) == 0 ) {
               	        if ( nodeError.indexOf( "unused TimeSensor" ) > 0 ) {
               	            uselessNodeCount++;
               	        }
               	    }
               	}
    	    } else if ( vle instanceof MFFieldValue ) {
    	        if (!((MFFieldValue)vle).isIndexed()) {
    	            unindexedValueCount += ((MFFieldValue)vle).getRawValueCount()/3;
    	        }
    	        String ferr = vle.getError();
    	        if ( ferr != null ) {
			        // too many extra index values
			        if ( ferr.indexOf( "only need" ) > 0 ) {
			            int foundCount = getInt1( ferr );
			            int needCount = getInt2( ferr );
			            unusedCoordCount += ( foundCount - needCount );
			        } else if ( ferr.indexOf( "additional unusable" ) > 0 ) {
			            unusedCoordCount += getInt1( ferr );
			        }
			    }
    	    }
		    if ( vle instanceof DEFUSENode ) {
				DEFUSENode dun = (DEFUSENode)vle;
				result = dun.isDEF();
				if ( result ) {
				    defCount++;
				} else {
				    useCount++;
				}
				if ( dun.isDEF() && !dun.isUsed() ) {
				    if ( unusedDEF.get( dun ) == null ) {
    					vle.setError( "Warning, DEF is not used" );
    					unusedDEF.put( dun, dun );
    				}
				}
			}  else if ( vle instanceof Field ) {
			    Field f = (Field)vle;
			    String ferr = f.getError();
			    if ( ferr != null ) {
			        if (( ferr.indexOf( "field value is default" ) > 0 ) || ( ferr.indexOf( "no values associated" ) > 0 )) {
			            if ( defaultFields.get( f ) == null ) {
			                defaultFields.put( f, f );
			            }
			        }
			        if ( ferr.indexOf( "not referenced" ) > 0 ) {
			            unusedPROTOinterfaceFields++;
			        }
			        if ( ferr.indexOf( "duplicate field" ) > 0 ) {
			            if ( duplicateFields.get( f ) == null ) {
			                duplicateFields.put( f, f );
			            }
			        }
			    }
			} else if ( !usingErrorSummary && ( vle instanceof Value )) {
			    Value v = (Value)vle;
			    String verr = v.getError();
			    if ( verr != null ) {
			        if ( verr.indexOf( "not in" ) > 0 ) {
			            unusedCoordCount++;
			        } else if ( verr.indexOf( "repeated index in" ) > 0 ) {
			            dupIndexCount++;
			        } else if ( verr.indexOf( "at least 3 edges" ) > 0 ) {
			            badFaceCount++;
			        } else if ( verr.indexOf( "repeated value" ) > 0 ) {
			            repeatedValueCount++;
			        } else if ( verr.indexOf( "unnecessary keyValue" ) > 0 ) {
			            unnecessaryInterpolatorValueCount++;
			        } else if ( verr.indexOf( "unused key" ) > 0 ) {
			            unnecessaryInterpolatorValueCount++;
			        }
			    }
			} else if ( vle instanceof ROUTE ) {
			    if ( vle.getError() != null ) {
			        String s = vle.getError();
			        if ( s.indexOf( "not allowed" ) > 0 ) {
    			        badRouteCount++;
    			    } else if ( s.indexOf( "repeated" ) > 0 ) {
    			        duplicateRouteCount++;
    			    }
			    }
			}

			if ( vle.getError() != null ) {
				int tokenOffset = vle.getFirstTokenOffset();

				// tokens use 1 based line numbering since this is most common
				// for text editors, so we report line numbers in a consistent way
				if ( tokenOffset != -1 ) {
					showError( vle.getError(), dataSource.getLineNumber( tokenOffset ), dataSource.getLineOffset( tokenOffset ));
				}
			}
			return( result );
		}
		return( true );
	}

    int getInt1( String ferr ) {
        int len = ferr.length();
        StringBuffer int1val = new StringBuffer();
        // skip initial non digits
        int scanner = 0;
        while ( scanner < len ) {
            char c = ferr.charAt( scanner );
            if (( c >= '0' ) && ( c <= '9' )) {
                break;
            }
            scanner++;
        }
        // load up StringBuffer with digits
        while ( scanner < len ) {
            char c = ferr.charAt( scanner );
            if (( c >= '0' ) && ( c <= '9' )) {
                int1val.append( c );
            } else {
                break;
            }
            scanner++;
        }
        String s = new String( int1val );
        return( Integer.parseInt( s ));
    }
    int getInt2( String ferr ) {
        int len = ferr.length();
        StringBuffer int2val = new StringBuffer();
        // skip initial non digits
        int scanner = 0;
        while ( scanner < len ) {
            char c = ferr.charAt( scanner );
            if (( c >= '0' ) && ( c <= '9' )) {
                break;
            }
            scanner++;
        }
        // skip first set of digits
        while ( scanner < len ) {
            char c = ferr.charAt( scanner );
            if ( !(( c >= '0' ) && ( c <= '9' ))) {
                break;
            }
            scanner++;
        }
        // skip second set of digits
        while ( scanner < len ) {
            char c = ferr.charAt( scanner );
            if (( c >= '0' ) && ( c <= '9' )) {
                break;
            }
            scanner++;
        }
        // load up StringBuffer with digits
        while ( scanner < len ) {
            char c = ferr.charAt( scanner );
            if (( c >= '0' ) && ( c <= '9' )) {
                int2val.append( c );
            } else {
                break;
            }
            scanner++;
        }
        String s = new String( int2val );
        return( Integer.parseInt( s ));
    }
    /** Optimization, hash table of errors, initialized with <B>setErrorKeys</B> */
	transient Hashtable errorKeys = null;

	/** Enable an optimization which puts errors in hash table for fast access.
	 *  Speeds up error reports for files with large number of errors.
	 */
	public void setErrorKeys() {
		if (( errorList != null ) && ( errorKeys == null )) {
			errorKeys = new Hashtable();
			ErrorInfo scanner = errorList;
			while ( scanner != null ) {
//				errorKeys.put( Integer.toString( scanner.getLineNumber() ), scanner );
                errorKeys.put( new Integer( scanner.getLineNumber() ), scanner );
				scanner = scanner.getNextLink();
			}
		}
	}

	/**  Get the errors associated with a particular line number.
	 *
	 *  @return  ErrorInfo object associated with the line number, null if none.
	 */
	public ErrorInfo getErrorInfo( int lineNumber ) {
		if ( errorList == null ) {
			return( null );
		} else if ( errorKeys != null ) {
			ErrorInfo result = (ErrorInfo)errorKeys.get( new Integer( lineNumber )); //Integer.toString( lineNumber ));
			return( result );
		} else {
			ErrorInfo scanner = errorList;
			while ( scanner != null ) {
				if ( scanner.getLineNumber() == lineNumber ) {
					return( scanner );
				} else {
					scanner = scanner.getNextLink();
				}
			}
			return( null );
		}
	}


	void addLintError( ErrorInfo e ) {
		if ( errorList == null ) {
			errorList = e;
		} else {
			errorList.addLink( e );
		}
	}

	void replaceLintError( ErrorInfo oldError, ErrorInfo newError ) {
		newError.setNextLink( oldError.getNextLink() );
		oldError.setNextLink( null );
		if ( oldError == errorList ) {
			errorList = newError;
		} else {
			errorList.replaceLintError( oldError, newError );
		}
	}

	/**
	 *  Add error to the list of errors for the file.
	 *
	 *  @param   error   description of the error
	 *  @param   lineNumber  line number of the error in the file
	 *  @param   offset	  offset of the error within the line
	 */
	public void showError( String error, int lineNumber, int offset ) {
		if ( lineNumber < 0 ) return;
		ErrorInfo le = new ErrorInfo( error, lineNumber, offset );

		// check if there are already errors associated with that line
		ErrorInfo baseLintError = getErrorInfo( lineNumber );

		// if no errors, add this
		if ( baseLintError == null ) {
			addLintError( le );
			if ( error.indexOf( "Warning" ) == 0 ) {
				warningCount++;
    		} else if ( error.indexOf( "Nonconformance" ) == 0 ) {
			    nonconformanceCount++;
			} else {
				errorCount++;
			}
			errorLines++;
		} else {
			// if there are errors, make sure one with smallest offset is first
			if ( le.offset < baseLintError.offset ) {
				if ( le.append( baseLintError )) {
					if ( error.indexOf( "Warning" ) == 0 ) {
						warningCount++;
					} else if ( error.indexOf( "Nonconformance" ) == 0 )  {
					    nonconformanceCount++;
				    } else {
						errorCount++;
					}
				}
				replaceLintError( baseLintError, le );
			} else {
				if ( baseLintError.append( le )) {
					if ( error.indexOf( "Warning" ) == 0 ) {
						warningCount++;
					} else if ( error.indexOf( "Nonconformance" ) == 0 ) {
					    nonconformanceCount++;
					} else {
						errorCount++;
					}
				}
			}
		}
	}

	/** print summary information to a PrintStream */
	public void summary( PrintStream ps ) {
		int numberLines = dataSource.getNumberLines();
		for ( int i = 0; i < numberLines; i++ ) {
			if ( getErrorInfo(i) != null ) {
				getErrorInfo(i).showError( dataSource, i, ps );
			}
		}
		if ( warningCount == 1 ) {
			ps.println( "1 warning." );
		} else if ( warningCount > 1 ) {
			ps.println( warningCount + " warnings." );
		}
		if ( nonconformanceCount == 1 ) {
		    ps.println( "1 nonconformance." );
		} else if ( nonconformanceCount > 1 ) {
		    ps.println( nonconformanceCount + " nonconformances." );
		}
		if ( errorCount == 0 ) {
			ps.println( "No errors found." );
		} else if ( errorCount == 1 ) {
			ps.println( "1 error found." );
		} else {
			ps.println( errorCount + " errors found." );
		}
	}

	/** Get the total number of lines in the file. */
	public int getNumberLines() {
		return( dataSource.getNumberLines() );
	}

	/** Get the number of lines containing errors */
	public int getNumberErrorLines() {
		return( errorLines );
	}
}

