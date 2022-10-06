/*
 * @(#)Optimizer.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.ROUTE;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.node.DEFUSENode;
import java.util.Vector;

import java.io.PrintStream;

/** The Optimizer is the base class for all chisels.  It handles replacing a range
 *  of tokens through the <B>replaceRange</B> method. 
 */
abstract public class Optimizer implements NodeLocatorListener, OptionHolder {
	RangeReplacer rangeReplacer;
	
	// the type of node to replace
	String nodeName;
	Vector additionalNodes;
	
    // flags for non-node name specific controls
    boolean specificNodeType;
    boolean allNodes;
    boolean coordinateOwnerNode;
    boolean interpolatorNode;
    boolean defOnly;
	
	protected String baseFilePath;
	protected String baseFileName;
	public TokenEnumerator dataSource;

	public Optimizer( String nodeName, String actionMessage ) {
		this.nodeName = nodeName;
		this.actionMessage = actionMessage;
		allNodes = false;
		specificNodeType = true;
		allNodes = false;
		coordinateOwnerNode = false;
		interpolatorNode = false;
		setFlags();
	}

    /** Not available ... */
	private Optimizer() {
	}
	
	public String getNodeName() {
		return( nodeName );
	}

	public void setNodeName( String nodeName ) {
		this.nodeName = nodeName;
		setFlags();
	}
	
	public int getNumberAdditionalNames() {
	    if ( additionalNodes == null ) {
	        return( 0 );
	    } else {
	        return( additionalNodes.size() );
	    }
	}
	
	public String getAdditionalName( int offset ) {
	    return( (String)additionalNodes.elementAt( offset ));
	}
	
	public void addAdditionalNode( String nodeName ) {
	    if ( additionalNodes == null ) {
	        additionalNodes = new Vector();
	    }
	    additionalNodes.addElement( nodeName );
	}

	void setFlags() {
	    if ( nodeName != null ) {
	        allNodes = ( nodeName.compareTo( "All" ) == 0 );
            coordinateOwnerNode = ( nodeName.compareTo( "CoordinateOwner" ) == 0 );
            interpolatorNode = ( nodeName.compareTo( "Interpolator" ) == 0 );
            defOnly = ( nodeName.compareTo( "DEF" ) == 0 );
        }
	    specificNodeType = !(allNodes || coordinateOwnerNode || interpolatorNode || defOnly);
	}

    /** reset optimizer, subclasses may need this */
    public void reset() {
        rangeReplacer = null;
        dataSource = null;
    }
    
    /** Get a message used for display on status line */
    String actionMessage;
    public String getActionMessage() {
        return( actionMessage );
    }
    
    /** Get the number of control options available for this chisel. */
    public int getNumberOptions() {
        return( 0 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int optionOffset ) {
        return( null );
    }

    /** Get a specific control option label */
    public String getOptionLabel( int optionOffset ) {
        return( null );
    }

    /** Get current option value */
    public Object getOptionValue( int optionOffset ) {
        return( null );
    }

    /** Set option value */
    public void setOptionValue( int optionOffset, Object value ) {
    }

    /** Get current option value */
    public Object getOptionConstraints( int optionOffset ) {
        return( null );
    }

    /** Set option value */
    public void setOptionConstraints( int optionOffset, Object constraints ) {
    }

    /** Convert an option value to a boolean */
    public boolean optionValueToBoolean( Object value ) {
        return( "true".equalsIgnoreCase(value.toString()) );
    }
    /** Convert an integer to an option value */
    public Object booleanToOptionValue( boolean value ) {
        return( value ? "true" : "false" );
    }

    /** Convert an option value to an integer */
    public int optionValueToInt( Object value ) {
        int n;
        try {
            n = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            System.out.println("Couldn't convert option " + value + " to integer.");
            n = 0;
        }
        return n;
    }
    
    /** Convert an integer to an option value */
    public Object intToOptionValue( int value ) {
        return String.valueOf(value);
    }


    /** Set the base file path for the source file being processed */
    public void setBaseFilePath( String baseFilePath ) {
        this.baseFilePath = baseFilePath;
    }

    /** Set the base file name for the source file being processed */
    public void setBaseFileName( String baseFileName ) {
        this.baseFileName = baseFileName;
    }

	public void setRangeReplacer( RangeReplacer rr ) {
		this.rangeReplacer = rr;
	}

	public void setDataSource( TokenEnumerator v ) {
	    dataSource = v;
	}

	public void replaceRange( int firstTokenOffset, int lastTokenOFfset, Object param ) {
		rangeReplacer.replaceRange( this, firstTokenOffset, lastTokenOFfset, param );
	}

	public void replaceStartEnd( int oldStartOffset, int oldEndOffset, int newStartOffset, int newEndOffset ) {
	    rangeReplacer.replaceStartEnd( oldStartOffset, oldEndOffset, newStartOffset, newEndOffset );
	}

	public void eofTokens( int firstTokenOffset, int lastTokenOffset ) {
		rangeReplacer.eofTokens( firstTokenOffset, lastTokenOffset );
	}

    void tryAdditional( NodeFoundEvent nfe ) {
        if ( additionalNodes != null ) {
    	    String nfeName = nfe.getName();
    	    int numberAdditional = additionalNodes.size();
    	    for ( int i = 0; i < numberAdditional; i++ ) {
    	        String s = (String)additionalNodes.elementAt( i );
    	        if ( nfeName.compareTo( s ) == 0 ) {
    	            attemptOptimization( nfe.getNode() );
    	            break;
    	        }
    	    }
    	}
    }

	//
	//  "nodeFound" and "getNodeName" are the NodeLocatorListener interface.
	//  Optimizers listen for NodeFoundEvents then attempt optimizations on those
	//  nodes.  The "attemptOptimization" is an abstract method implemented in the
	//  specific Optimizer subclasses.
	//
	public void nodeFound( NodeFoundEvent nfe ) {
	    if ( specificNodeType ) {
            if ( nodeName.compareTo( nfe.getName() ) == 0 ) {
    			attemptOptimization( nfe.getNode() );
    		} else {
    		    tryAdditional( nfe );
    		}
		} else if ( coordinateOwnerNode ) {
	        String nfeName = nfe.getName();
	        if ( nfeName.compareTo( "IndexedFaceSet" ) == 0 ) {
	            attemptOptimization( nfe.getNode() );
	        } else if ( nfeName.compareTo( "IndexedLineSet" ) == 0 ) {
	            attemptOptimization( nfe.getNode() );
	        }
	    } else if ( interpolatorNode ) {
	        String nfeName = nfe.getName();
	        if ( nfeName.indexOf( "Interpolator" ) > 0 ) {
	            attemptOptimization( nfe.getNode() );
	        } else {
	            tryAdditional(nfe );
	        }
    	} else if ( defOnly ) {
    	    Node n = nfe.getNode();
    	    if ( n instanceof DEFUSENode ) {
    	        DEFUSENode dun = (DEFUSENode)n;
    	        if ( dun.isDEF() ) {
    	            attemptOptimization( n );
    	        }
    	    }
   	    } else if ( allNodes ) {
			attemptOptimization( nfe.getNode() );
		}
	}

	public void routeFound( RouteFoundEvent rfe ) {
		attemptOptimization( rfe.getRoute() );
	}


	// some subclasses may override this, determines whether or not optimize is called
	public boolean optimizePossible( Object param ) {
		return( true );
	}

    /** NodeLocatorListener interface, subclasses override this to return 
     *  true to force "attemptOptimization" calls on DEF/USE nodes.
     */
	public boolean isDEFUSElistener() {
		return( false );
	}
	
	/** NodeLocatorListener interface, subclasses override this to return 
	*   true to force "attemptOptimization" calls on DEF nodes.
	 */
	public boolean isDEFlistener() {
	    return( false );
	}

    /** template, subclasses override this to return true if they want to define
     *  the "attemptOptimization( ROUTE )" method.
     */
	public boolean isROUTElistener() {
		return( false );
	}

    /** template, subclasses override tihs to return true if they want access
     *  to the interior of PROTOs.
     */
	public boolean isPROTOlistener() {
	    return( false );
	}

    /** template, subclasses override this if they modify ROUTEs */
	public void attemptOptimization( ROUTE route ) {
	}

    /** template, subclasses override this if they modify Nodes */
	public void attemptOptimization( Node node ) {
	}


    /** Print a sequence of numbers
     *
     *  @param tp  where to print
     *  @param scanner  first token to print
     *  @param endTokenOffset  last token to print, printing stops early if this found
     *  @param n  number of tokens to print
     *
     *  @return  the token after the last token printed, or endTokenOffset if encountered
     */
    protected int printNumbers( TokenPrinter tp, int scanner, int endTokenOffset, int n ) {
        dataSource.setState( scanner );
        for ( int i = 0; i < n; i++ ) {
            tp.print( dataSource, scanner );
            scanner = dataSource.getNextToken();
            if ( scanner >= endTokenOffset ) {
                break;
            }
            if ( i < ( n - 1 )) {
                scanner = tp.printNonNumbers( scanner, endTokenOffset );
                if ( scanner >= endTokenOffset ) {
                    break;
                }
            }
        }
        return( scanner );
    }

    /** Replace an index field when there is one index entry per face,
     *  and that one face has been converted into several faces.
     *
     *  @param tp print destination
     *  @param indexField the index field to replace
     *  @param faceMultiple array containing factor multiple for each face
     *  @param faceCount number of faces
     */
	protected void replaceIndexFaceMultiple( TokenPrinter tp, Field indexField, int[] faceMultiple, int faceCount ) {
	    int scanner = indexField.getFirstTokenOffset();
		int endTokenOffset = indexField.getLastTokenOffset();
	    dataSource.setState( scanner );
	    int faceNo = 0;
	    while ( true ) {
	        scanner = tp.printNonNumbers( scanner, endTokenOffset );
	        if ( scanner >= endTokenOffset ) {
	            break;
	        }
	        if (( faceNo < faceCount ) && ( faceMultiple[ faceNo ] > 0 )) {
	            for ( int i = 0; i < faceMultiple[ faceNo ]; i++ ) {
	                tp.print( dataSource, scanner );
	            }
	        } else {
	            tp.print( dataSource, scanner );
	        }
	        faceNo++;
	        scanner = dataSource.getNextToken();
	    }
	}

	protected void replaceValueNodeFaceMultiple( TokenPrinter tp, Field valueNode, int[] faceMultiple, int faceCount ) {
	    int scanner = valueNode.getFirstTokenOffset();
	    int endTokenOffset = valueNode.getLastTokenOffset();
	    dataSource.setState( scanner );
	    int faceNo = 0;
	    while ( true ) {
	        scanner = tp.printNonNumbers( scanner, endTokenOffset );
	        if ( scanner >= endTokenOffset ) {
	            break;
	        }
	        if (( faceNo < faceCount ) && ( faceMultiple[ faceNo ] > 0 ))  {
	            int tscanner = printNumbers( tp, scanner, endTokenOffset, 3 );
	            for ( int i = 1; i < faceMultiple[ faceNo ]; i++ ) {
	                tscanner = printNumbers( tp, scanner, endTokenOffset, 3 );
	            }
	            scanner = tscanner;
	        } else {
	            tp.print( dataSource, scanner );
	            scanner = dataSource.getNextToken();
	        }
	        faceNo++;
	    }
	}

	// RangeReplacer calls this when it has a range of tokens to replace
	abstract public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset );
	
	/** default summary (none), used by command line version */
	public void summarize( PrintStream ps ) {
	}
	
	/** default, chisel has no final code generation */
	public boolean hasFinalCode() {
	    return( false );
	}
	
	/** method which prints final code, subclasses may override this */
	public void printFinalCode( TokenPrinter tp ) {
	}

}
