/*
 * @(#)TokenPrinter.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import java.io.*;
import java.util.Vector;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.parse.TokenFactory;
import com.trapezium.parse.TokenTypes;
import com.trapezium.factory.FactoryResponseListener;

/** The TokenPrinter is the main class for regenerating VRML text with
 *  specific sections replaced.
 */

public class TokenPrinter {
	PrintStream ps;
	protected TokenEnumerator dataSink;
	protected TokenEnumerator dataSource;
	int lastLineNumber = -1;
	StringBuffer accumulator;
	int lineCount = 0;
	boolean prettyPrint = false;
	int indentLevel = 0;

    TokenFactory dataSinkTokenFactory;
    void setDataSink( TokenEnumerator e ) {
        dataSink = e;
        if ( dataSink != null ) {
            if ( dataSinkTokenFactory == null ) {
                dataSinkTokenFactory = new TokenFactory();
            }
        }
    }

    public TokenEnumerator getDataSink() {
        return( dataSink );
    }

    /** Save the dataSink to a PrintStream */
    public void saveDataSink( PrintStream ps ) {
        dataSink.saveLines( ps );
    }
    
    /** Get the data source for tokens being printed */
    public TokenEnumerator getDataSource() {
        return( dataSource );
    }
    
    /** Set the data source for tokens being printed */
    public void setDataSource( TokenEnumerator dataSource ) {
        this.dataSource = dataSource;
        setupDataSourceFields();
    }

    /** Limit the range of tokens accessed in preparation for replacing a
      * section.  Limits are used by some of the scanning and printing methods.
      */
    int startLimit;
    int endLimit;
    public void limitRange( int first, int last ) {
        dataSource.setState( first );
        startLimit = first;
        endLimit = last;
    }

    /** Print up to and including a token of a specific type, without going
     *  past end limit.
     *  
     *  @param tokenType the type of token to print to, printing stops
     *     after this one is printed
     */
    public void printTo( int tokenType ) {
        int scanner = dataSource.getState();
        while ( scanner <= endLimit ) {
            print( dataSource, scanner );
            if ( dataSource.getType( scanner ) == tokenType ) {
                break;
            }
            scanner = dataSource.getNextToken();
        }
    }

    /** Skip up to a specific token type, without going past end limit
     *
     *  @param tokenType the type of token which ends the skipping
     */
    public void skipTo( int tokenType ) {
        int scanner = dataSource.getState();
        while ( scanner < endLimit ) {
            if ( dataSource.getType( scanner ) == tokenType ) {
                break;
            }
            scanner = dataSource.getNextToken();
        }
    }

    /** Utility routines */
    /** Print 3 floats, mirroring SFVec3f in VRML */
    public void print3f( float[] f ) {
        print( f[0] );
        print( f[1] );
        print( f[2] );
    }
    
    public void print3i( int[] i ) {
        print( i[0] );
        print( i[1] );
        print( i[2] );
    }


	public TokenPrinter( PrintStream ps, TokenEnumerator dataSource ) {
		init();
		this.ps = ps;
		setDataSource( dataSource );
	}

    /** Class constructor, for TokenPrinter that transfers from one TokenEnumerator to another */
	public TokenPrinter( TokenEnumerator dataSource, TokenEnumerator dataSink ) {
		init();
		setDataSource( dataSource );
		setDataSink( dataSink );
	}

	public void doPrettyPrint() {
		prettyPrint = true;
	}

	Vector colorStrings;
	int colorStringsSize;
	Vector indexStrings;
	int indexStringsSize;
	Vector coord3dStrings;
	int coord3dStringsSize;
	Vector coord2dStrings;
	int coord2dStringsSize;
	static final int NoBreakRule = 0;
	static final int BreakAfterUSE1 = 1;
	static final int BreakAfterUSE2 = 2;
	static final int BreakOnNegativeOne = 3;
	static final int BreakEverySecondNumber = 4;
	static final int BreakEveryThirdNumber = 5;
	static final int FirstNumberBreakRule = BreakOnNegativeOne;
    /**  to save space, we can print a comma instead of a newline when
         we break after a coordinate etc.
    */
	boolean commaForNumberBreak = true;
	int currentBreakRule = NoBreakRule;
	int numberCount = 0;
	void setBreakRule( int scannerOffset, int type ) {
		if (( type == TokenTypes.RightBracket ) || ( type == TokenTypes.RightBrace )) {
			currentBreakRule = NoBreakRule;
			return;
		} else if ( type == TokenTypes.NumberToken ) {
			return;
		} else if (( type == TokenTypes.LeftBracket ) || ( type == TokenTypes.LeftBrace )) {
			numberCount = 0;
			return;
		} else if ( dataSource.sameAs( scannerOffset, "USE" )) {
		    currentBreakRule = BreakAfterUSE1;
		    return;
		} else if ( currentBreakRule == BreakAfterUSE1 ) {
		    currentBreakRule = BreakAfterUSE2;
		    return;
		}
		
		if ( type == TokenTypes.Keyword1Token ) {
    		if ( currentBreakRule != BreakEverySecondNumber ) {
    			for ( int i = 0; i < coord3dStringsSize; i++ ) {
    				String test = (String)coord3dStrings.elementAt( i );
    				if ( dataSource.sameAs( scannerOffset, test )) {
    					currentBreakRule = BreakEveryThirdNumber;
    					return;
    				}
    			}
    		}
    		for ( int i = 0; i < coord2dStringsSize; i++ ) {
    			String test = (String)coord2dStrings.elementAt( i );
    			if ( dataSource.sameAs( scannerOffset, test )) {
    				currentBreakRule = BreakEverySecondNumber;
    				return;
    			}
	    	}
    		for ( int i = 0; i < indexStringsSize; i++ ) {
    			String test = (String)indexStrings.elementAt( i );
    			if ( dataSource.sameAs( scannerOffset, test )) {
    				currentBreakRule = BreakOnNegativeOne;
	    			return;
    			}
    		}
    		for ( int i = 0; i < colorStringsSize; i++ ) {
    			String test = (String)colorStrings.elementAt( i );
    			if ( dataSource.sameAs( scannerOffset, test )) {
    				currentBreakRule = BreakEveryThirdNumber;
    				return;
    			}
    		}
    	}
	}

	boolean applyBreakRule( int scannerOffset, int type ) {
		if ( type == TokenTypes.CommentToken || type == TokenTypes.QuotedString || type == TokenTypes.QuotedStringContinuation ) {
		    return( true );
	    } else if ( currentBreakRule == NoBreakRule ) {
			return( false );
		} else if ( currentBreakRule == BreakEverySecondNumber ) {
			if ( type == TokenTypes.NumberToken ) {
				numberCount++;
				if ( numberCount == 2 ) {
					numberCount = 0;
					return( true );
				} else {
					return( false );
				}
			} else {
				return( false );
			}
		} else if ( currentBreakRule == BreakEveryThirdNumber ) {
			if ( type == TokenTypes.NumberToken ) {
				numberCount++;
				if ( numberCount == 3 ) {
					numberCount = 0;
					return( true );
				} else {
					return( false );
				}
			} else {
				return( false );
			}
		} else if ( currentBreakRule == BreakOnNegativeOne ) {
			if ( type == TokenTypes.NumberToken ) {
				int intval = dataSource.getIntValue( scannerOffset );
				return( intval == -1 );
			} else {
				return( false );
			}
		} else if ( currentBreakRule == BreakAfterUSE2 ) {
		    currentBreakRule = NoBreakRule;
		    return( true );
		} else {
			return( false );
		}
	}

	void init() {
		ps = null;
		dataSink = null;
		accumulator = new StringBuffer();
		colorStrings = new Vector();
		indexStrings = new Vector();
		coord3dStrings = new Vector();
		coord2dStrings = new Vector();
		colorStrings.addElement( new String( "color" ));
		indexStrings.addElement( new String( "coordIndex" ));
		indexStrings.addElement( new String( "texCoordIndex" ));
		indexStrings.addElement( new String( "colorIndex" ));
		indexStrings.addElement( new String( "normalIndex" ));
		coord3dStrings.addElement( new String( "point" ));
		coord2dStrings.addElement( new String( "texCoord" ));
		colorStringsSize = colorStrings.size();
		indexStringsSize = indexStrings.size();
		coord3dStringsSize = coord3dStrings.size();
		coord2dStringsSize = coord2dStrings.size();
	}

	void nextIndent() {
		flush();
		indentLevel++;
	}

	void prevIndent() {
		flush();
		indentLevel--;
		indent();
	}

	void nextLine() {
		flush();
		indent();
	}
	
	/** Set up dataSource fields that are constant for the life of this TokenPrinter.
	 *  This is an optimization to prevent calls to TokenEnumerator methods,
	 *  by providing those arrays directly in this object.
	 */
	int[] dataSourceLineNumber;
	byte[] dataSourceByteArray;
	int dataSourceNumberTokens;
	short[] dataSourceTokenSizeArray;
	short[] dataSourceLineOffsetArray;
	int[] dataSourceLineIdx;
	boolean[] dataSourceLineBreakArray;
	byte[] dataSourceTokenTypeArray;
	void setupDataSourceFields() {
	    dataSourceLineNumber = dataSource.getLineNumberArray();
	    dataSourceByteArray = dataSource.getFileData();
	    dataSourceNumberTokens = dataSource.getNumberTokens();
	    dataSourceTokenSizeArray = dataSource.getTokenSizeArray();
	    dataSourceLineOffsetArray = dataSource.getLineOffsetArray();
	    dataSourceLineIdx = dataSource.getLineIdx();
	    dataSourceLineBreakArray = dataSource.getLineBreakArray();
	    dataSourceTokenTypeArray = dataSource.getTokenTypeArray();
	}

    /** Print a range of tokens.
     *
     *  @param firstTokenOffset first token in the range to print
     *  @param lastTokenOffset last token in the range to print
     *  @param reformat indicates whether reformatting occurs during the printing
     */
	public void printRange( int firstTokenOffset, int lastTokenOffset, boolean reformat ) {
	    if ( firstTokenOffset > lastTokenOffset ) {
	        return;
	    }
	    if ( reformat || ( ps != null ) || ( dataSink == null )) {
	        filterPrint( firstTokenOffset, lastTokenOffset );
	    } else {
    	    // Two cases to handle:
    	    // 1. token range on a single line
    	    // 2. token range on more than one line
    	    //    - first line may or may not be complete
    	    //    - last line may or may not be complete
    	    int firstLineNumber = dataSourceLineNumber[ firstTokenOffset ];
    	    int lastLineNumber = dataSourceLineNumber[ lastTokenOffset ];
    	    if ( firstLineNumber == lastLineNumber ) {
    	        filterPrint( firstTokenOffset, lastTokenOffset );
    	    } else {
    	        flush();
    	        boolean firstLineComplete = false;
    	        boolean lastLineComplete = false;
    	        if ( firstTokenOffset == 0 ) {
    	            firstLineComplete = true;
    	        } else if ( dataSourceLineNumber[ firstTokenOffset - 1 ] != firstLineNumber ) {
    	            firstLineComplete = true;
    	        }
    	        if ( lastTokenOffset >= ( dataSourceNumberTokens - 1 )) {
    	            lastLineComplete = true;
    	        } else if ( dataSourceLineNumber[ lastTokenOffset + 1 ] != lastLineNumber ) {
    	            lastLineComplete = true;
    	        }
    	        int firstLineOffset = 0;
    	        if ( !firstLineComplete ) {
    	            firstLineOffset = dataSource.getLineOffset( firstTokenOffset );
    	        }
    	        int lastLineOffset = 0;
    	        if ( !lastLineComplete ) {
    	            lastLineOffset = dataSource.getLineOffset( lastTokenOffset ) + dataSourceTokenSizeArray[ lastTokenOffset ];
    	        }
    	        if ( firstLineComplete ) {
    	            dataSink.addLine( dataSourceLineIdx[ firstLineNumber ], 
    	                dataSource.getLineSize( firstLineNumber ), dataSourceByteArray,
    	                dataSinkTokenFactory );
//    	            dataSink.addLine( dataSourceLineIdx[ firstLineNumber ], 
//    	                dataSource.getLineSize( firstLineNumber ), dataSourceByteArray,
//    	                dataSource.getNumberTokensOnLine( firstLineNumber ), firstTokenOffset,
//    	                dataSourceLineOffsetArray, dataSourceTokenSizeArray,
//    	                dataSourceLineBreakArray, dataSourceTokenTypeArray );
    	        } else {
    	            String firstLine = dataSource.getLineAt( firstLineNumber );
    	            printLine( firstLine.substring( firstLineOffset ));
    	        }
    	        for ( int i = firstLineNumber + 1; i < lastLineNumber; i++ ) {
    	            dataSink.addLine( dataSourceLineIdx[ i ], 
    	                dataSource.getLineSize( i ), dataSourceByteArray,
    	                dataSinkTokenFactory );
//    	            dataSink.addLine( dataSourceLineIdx[ i ], 
//    	                dataSource.getLineSize( i ), dataSourceByteArray,
//    	                dataSource.getNumberTokensOnLine( i ), dataSource.getFirstTokenOnLine( i ),
//    	                dataSourceLineOffsetArray, dataSourceTokenSizeArray,
//    	                dataSourceLineBreakArray, dataSourceTokenTypeArray );
    	            dataSource.notifyByLine( i );
    	        }
    	        if ( lastLineComplete ) {
    	            dataSink.addLine( dataSourceLineIdx[ lastLineNumber ], 
    	                dataSource.getLineSize( lastLineNumber ), dataSourceByteArray,
    	                dataSinkTokenFactory );
//    	            dataSink.addLine( dataSourceLineIdx[ lastLineNumber ], 
//    	                dataSource.getLineSize( lastLineNumber ), dataSourceByteArray,
//    	                dataSource.getNumberTokensOnLine( lastLineNumber ), dataSource.getFirstTokenOnLine( lastLineNumber ),
//    	                dataSourceLineOffsetArray, dataSourceTokenSizeArray,
//    	                dataSourceLineBreakArray, dataSourceTokenTypeArray );
    	        } else {
    	            String lastLine = dataSource.getLineAt( lastLineNumber );
    	            printLine( lastLine.substring( 0, lastLineOffset ));
    	        }
    	    }
    	}
    }

    void filterPrint( int firstTokenOffset, int lastTokenOffset ) {
//        dataSource.detailDump();
        
		int scannerOffset = firstTokenOffset;
		dataSource.setState( scannerOffset );
		boolean needIndent = false;
		boolean brokeLine = false;
		boolean lastWasContinuation = false;
		while ( true ) {
			if ( scannerOffset == -1 ) {
				break;
			}
       	    int type = dataSource.getType( scannerOffset );
			if ( prettyPrint ) {
			    if ( !( type == TokenTypes.QuotedStringContinuation )) {
			        lastWasContinuation = false;
			    }
				if (( type == TokenTypes.RightBracket ) ||
			        ( type == TokenTypes.RightBrace )) {
				    prevIndent();
				    brokeLine = true;
				} else if ( type == TokenTypes.QuotedStringContinuation ) {
   				    nextLine();
    				lastWasContinuation = true;
					brokeLine = true;
				} else if ( needIndent ) {
					indent();
				} else if ( dataSource.isLineBreak( scannerOffset )) {
					if ( !brokeLine ) {
						nextLine();
						brokeLine = true;
					}
				} else {
					brokeLine = false;
				}
				needIndent = false;
			}
			print( dataSource, scannerOffset, type );
			if ( prettyPrint ) {
    			setBreakRule( scannerOffset, type );
				if (( type == TokenTypes.LeftBracket ) || ( type == TokenTypes.LeftBrace )) {
					nextIndent();
					brokeLine = true;
					needIndent = true;
				} else if (( type == TokenTypes.CommentToken ) || ( type == TokenTypes.QuotedString ) || ( type == TokenTypes.QuotedStringContinuation )) {
				    flush();
				    needIndent = true;
				    brokeLine = true;
				} else if (( type == TokenTypes.RightBracket ) || ( type == TokenTypes.RightBrace )) {
					flush();
					needIndent = true;
				} else if ( applyBreakRule( scannerOffset, type )) {
				    if ( commaForNumberBreak && currentBreakRule >= FirstNumberBreakRule ) {
        		        accumulator.append( ',' );
	                    int limit = (currentBreakRule - FirstNumberBreakRule + 1) * 8;
        		        if (flushIfWithin(limit)) {
        					//needIndent = true;
        					brokeLine = true;
        			    }
				    } else {
    					flush();  //nextLine();
    					needIndent = true;
    					brokeLine = true;
    			    }
				}
			}
			if ( scannerOffset >= lastTokenOffset ) {
				break;
			}
			scannerOffset = dataSource.getNextToken( scannerOffset );
			if ( scannerOffset > lastTokenOffset ) {
			    break;
			}
		}
		flush();
	}


	boolean printedComment = false;
	public void print( TokenEnumerator v, int t ) {
	    print( v, t, v.getType( t ));
	}
	
	public void print( TokenEnumerator v, int t, int type ) {
//	    System.out.println( "Print token " + t + ", which is '" + v.toString( t ) + "'" );
		if (!flushIfWithin(8)) {
       		attemptSpace(); 
       	} else {
       	    justIndented = false;
       	}
		if (( type == TokenTypes.NumberToken ) && v.isFloat( t )) {
		    boolean hasE = ( v.hasChar( t, 'e' ) || v.hasChar( t, 'E' ));
		    int sign = 1;
		    if ( v.charAt( 0, t ) == '-' ) {
		        sign = -1;
		    }
		    int vbefore = v.valueBeforeDot( t );
		    int zcount = v.zerosAfterDot( t );
		    int vafter = v.valueAfterDot( t );
		    if ( vbefore > 0 ) {
		        accumulator.append( sign*vbefore );
		    }

		    if ( vafter > 0 ) {
		        if (( vbefore == 0 ) && ( sign < 0 )) {
		            accumulator.append( '-' );
		        }
    		    accumulator.append( '.' );
    		}
		    if (( zcount > 0 ) && ( vafter > 0 )) {
		        for ( int i = 0; i < zcount; i++ ) {
		            accumulator.append( '0' );
		        }
		    }
		    if ( vafter > 0 ) {
		        accumulator.append( vafter );
		    }
		    if (( vbefore == 0 ) && ( vafter == 0 )) {
		        accumulator.append( '0' );
		    }
		    if ( hasE ) {
		        int size = v.getSize( t );
		        boolean founde = false;
		        for ( int i = 0; i < size; i++ ) {
		            if ( v.charAt( i, t ) == 'e' ) {
		                accumulator.append( 'e' );
		                founde = true;
		            } else if ( v.charAt( i, t ) == 'E' ) {
		                accumulator.append( 'e' );
		                founde = true;
		            } else if ( founde ) {
		                accumulator.append( v.charAt( i, t ));
		            }
		        }
		    }
		} else {
		    v.append( t, accumulator );
		}
//		System.out.println( "accumulator is '" + accumulator + "'" );
		if (( type == TokenTypes.CommentToken ) || ( type == TokenTypes.QuotedString ) || ( type == TokenTypes.QuotedStringContinuation )) {
			printedComment = true;
			flush();
		}
	}

	/** Print a float value */
	public void print( float f ) {
		if ( f == 0f ) {
    		flushIfWithin(2);
    		attemptSpace();
			accumulator.append( '0' );
		} else if ( f == 1f ) {
    		flushIfWithin(2);
    		attemptSpace();
			accumulator.append( '1' );
		} else {
    		attemptFlush();
	    	attemptSpace();
			accumulator.append( f );
		}
	}

	public void print( double d ) {
	    attemptFlush();
	    attemptSpace();
	    Double dd = new Double( d );
	    accumulator.append( String.valueOf(dd) );
	}

	public void print( int i ) {
		flushIfWithin(6);
		attemptSpace();
		accumulator.append( i );
	}

    /** Print a String to the token stream */
	public void print( String s ) {
		flushIfWithin(s.length() + 1);
		attemptSpace();
		accumulator.append( s );
	}

    /** Print an int as a float, with a "." separating the value at a
     *  particular resolution.
     */
    public void printAtResolution( int value, int resolution ) {
        flushIfWithin( 2 );
        attemptSpace();
        if ( value == 0 ) {
            accumulator.append( '0' );
            return;
        }
        if ( value < 0 ) {
            accumulator.append( '-' );
            value = value*-1;
        }
        String s = String.valueOf( value );
        int len = s.length();
        int beforePoint = len - resolution;
        if ( beforePoint > 0 ) {
            for ( int i = 0; i < beforePoint; i++ ) {
                accumulator.append( s.charAt( i ));
            }
        }
        boolean doAfter = false;
        if ( beforePoint < 0 ) {
            doAfter = true;
        } else {
            for ( int i = 0; i < resolution; i++ ) {
                if ( s.charAt( i + beforePoint ) != '0' ) {
                    doAfter = true;
                    break;
                }
            }
        }
        if ( doAfter ) {
            accumulator.append( '.' );
            if ( beforePoint < 0 ) {
                beforePoint = -1*beforePoint;
                for ( int i = 0; i < beforePoint; i++ ) {
                    accumulator.append( '0' );
                }
                beforePoint = 0;
            }
            int zeroCount = 0;
            for ( int i = 0; i < resolution; i++ ) {
                if (( i + beforePoint ) >= len ) {
                    break;
                } else if ( s.charAt( i + beforePoint ) == '0' ) {
                    zeroCount++;
                } else {
                    for ( int j = 0; j < zeroCount; j++ ) {
                        accumulator.append( '0' );
                    }
                    zeroCount = 0;
                    accumulator.append( s.charAt( i + beforePoint ));
                }
            }
        }
    }

    /** Print a float with a particular resolution.
     *
     *  @param f float value to print
     *  @param resolution number of digits after the decimal point
     */
	public void print( float f, int resolution ) {
		if ( f == 0f ) {
            flushIfWithin(2);
            attemptSpace();
			accumulator.append( "0" );
		} else if ( f == 1f ) {
            flushIfWithin(2);
            attemptSpace();
			accumulator.append( "1" );
		} else {
			String s = String.valueOf( f );
			int minLen = s.length();
			int strlen = minLen;
			if ( resolution < minLen ) {
				minLen = resolution;
			}

            flushIfWithin(minLen + 1);
            attemptSpace();

			// "0" before "." is unnecessary
			if ( s.indexOf( "0." ) == 0 ) {
				if (( minLen == resolution ) && ( minLen < strlen )) {
					minLen++;
				}
				for (  int i = 1; i < minLen; i++ ) {
					accumulator.append( s.charAt( i ));
				}
			} else {
				for ( int i = 0; i < minLen; i++ ) {
					accumulator.append( s.charAt( i ));
				}
			}
		}
	}

    static int maxLineLength = 80;
    static public void setMaxLineLength( int len ) {
        maxLineLength = len;
    }
    
	void attemptFlush() {
	    flushIfWithin(8);
	}

	boolean flushIfWithin(int limit) {
		if (( accumulator.length() - indentLevel*indentSize ) > maxLineLength - limit ) {
			flush();
			if ( prettyPrint ) {
				indent();
			}
			return true;
		} else {
    		return false;
        }
	}

    public String getString() {
        return( new String( accumulator ));
    }

	public void flush() {
		if ( accumulator != null ) {
			if ( accumulator.length() > 0 ) {
				if ( dataSink != null ) {
					dataSink.addLine( accumulator, dataSinkTokenFactory );
//					System.out.println( "adding accumulator to dataSink!" );
				}
				if ( ps != null ) {
					ps.println( accumulator );
				}
				accumulator.setLength(0);  // = new StringBuffer();
			}
		}
	}
	
	public void printLine( String s ) {
	    if ( dataSink != null ) {
	        dataSink.addLine( s, dataSinkTokenFactory );
	    }
	    if ( ps != null ) {
	        ps.println( s );
	    }
	}

	public void println( String s ) {
		flush();
		ps.println( s );
		lineCount++;
		if (( lineCount % 5000 ) == 0 ) {
//		    System.out.print("TP calling gc()...");
			Runtime.getRuntime().gc();
//		    System.out.println("gc() done.");
		}
	}

	void attemptFlush( int lineNumber ) {
		if ( lastLineNumber != lineNumber ) {
			flush();
			lastLineNumber = lineNumber;
		}
	}

    boolean justIndented = false;
	void attemptSpace() {
	    if ( justIndented ) {
	        justIndented = false;
	        return;
	    }
	    justIndented = false;
		if ( accumulator.length() > indentLevel*indentSize ) {
			accumulator.append( ' ' );
		}
	}

    /** Indentation controls */
    static int indentSize = 3;
    public static int getIndentSize() {
        return indentSize;
    }
    public static void setIndentSize(int size) {
        indentSize = size;
//        System.out.println( "set indent size to " + indentSize );
    }


	void indent() {
		for ( int i = 0; i < indentLevel; i++ ) {
		    for ( int j = 0; j < indentSize; j++ ) {
    			accumulator.append( ' ' );
    		}
		}
		justIndented = true;
	}
	
	public int printNonNumbers( int scanner, int endTokenOffset ) {
	    return( printUntilToken( scanner, endTokenOffset, TokenTypes.NumberToken ));
	}
	
	/** Print tokens  until one of a particular type is encountered.
	 *
	 *  @param scanner where to start printing from
	 *  @param endTokenOffset limit to printing
	 *  @param tokenType tye type of token that terminates the printing,
	 *     (it is not printed, but its offset is returned)
	 *
	 *  @return the offset of the token that terminated the printing, or
	 *     one past the endTokenOffset, or -1
	 */
	public int printUntilToken( int scanner, int endTokenOffset, int tokenType ) {
		while (( scanner != -1 ) && ( scanner <= endTokenOffset )) {
		    int type = dataSource.getType( scanner );
		    if ( type == tokenType ) {
		        return( scanner );
	        } else {
    			print( dataSource, scanner, type );
    			scanner = dataSource.getNextToken();
    		}
    	}
		return( scanner );
	}
}
