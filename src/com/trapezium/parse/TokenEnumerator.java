/*
 * @(#)TokenEnumerator.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.parse;
import com.trapezium.util.ReturnInteger;
import com.trapezium.util.ByteString;
import com.trapezium.util.ProgressIndicator;

import java.util.Vector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import com.trapezium.util.GlobalProgressIndicator;

/**
 *  Converts an InputStream into a sequence of tokens used by parsing.
 *  <P>
 *  1.0 architecture had Token objects, this replaced in 1.1 with less elegant,
 *  but more memory efficient byte array with run-length-encoding of initial spaces
 *  to store VRML file in memory.
 *  <P>
 *  Each token still contains the same information as previously, except is
 *  now represented as an "int", and is accessed only through the TokenEnumerator.
 *  The information associated with each token is:
 *  <OL>
 *  <LI>line number</LI>
 *  <LI>offset in line of start of token</LI>
 *  <LI>size of token (number of characters it contains)</LI>
 *  <LI>type of token</LI>
 *  </OL>
 *  <P>
 *  Quoted strings are implemented with two token types:  QuotedString
 *  and QuotedStringContinuation.  This is done to avoid the complication
 *  of tokens existing on more than one line.
 *
 *  This class works with TokenFactory.  Arrays and primitive types rather than
 *  objects are used to increase the size of a file that can be handled before
 *  OutOfMemory exception occurs.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.11, 20 Jan 1998
 *
 *  @since           1.0
 */
public class TokenEnumerator implements TokenTypes, java.io.Serializable {
    static int tabIndentSize = 0;
    static public void setTabIndentSize( int n ) {
        tabIndentSize = n;
    }
    static int InitialByteArraySize = 10000;
    static int InitialTokenArraySize = 5000;
    static int InitialLineArraySize = 5000;

    static int SmallInitialByteArraySize = 100;
    static int SmallInitialTokenArraySize = 20;

    /** One array entry per token */
    transient protected int[] lineNumberArray;
    transient protected short[] lineOffsetArray;
    transient protected boolean[] lineBreakArray;
    transient protected byte[] tokenTypeArray;
    transient protected short[] tokenSizeArray;

    /** The number of entries possible in the above arrays */
    protected int tokenArrayBoundary;

    /** The number of valid token entries */
    protected int numberTokens;

    /** Internal cursor, optimization, offset into above arrays during scanning */
    protected int tokenScannerOffset;

    /** Set by any method which changes fileData */
    transient protected boolean dirtyFileData;

    /** One array entry per line */
    protected int[] lineIdx;

    /** The number of valid lines */
    protected int numberLines;

    /** The number of entries possible in the lineIdx array */
    protected int lineArrayBoundary;

    /** Get the number of tokens created */
    public int getNumberTokens() {
        return( numberTokens );
    }

    /********************* LINE METHODS **************************/
    /** Check if the TokenEnumerator has any lines */
    public boolean hasLines() {
        return( numberLines > 0 );
    }

    /** Get the number of lines */
    public int getNumberLines() {
        return( numberLines );
    }

    /** Set the number of lines */
    public void setNumberLines( int n ) {
        numberLines = n;
    }

    /**
     *  get the number of tokens allocated in token array +1000
     *  (not used by vorlon)
     */
    public int getTokenArraySize() {
        return( numberTokens + 1000 );
    }

    /** Get the lineNumberArray */
    public int[] getLineNumberArray() {
        return( lineNumberArray );
    }

    /** Get the lineOffsetArray */
    public short[] getLineOffsetArray() {
        return( lineOffsetArray );
    }

    /** Get the lineBreakArray */
    public boolean[] getLineBreakArray() {
        return( lineBreakArray );
    }

    /** Get the tokenTypeArray
     *
     *  @see TokenTypes
     */
    public byte[] getTokenTypeArray() {
        return( tokenTypeArray );
    }

    /** Get the tokenSizeArray */
    public short[] getTokenSizeArray() {
        return( tokenSizeArray );
    }

    /** Get the size of the token based arrays */
    public int getTokenArrayBoundary() {
        return( tokenArrayBoundary );
    }

    /** Get the lineIdx array */
    public int[] getLineIdx() {
        return( lineIdx );
    }

    /** Set the offset in the byte array of a line */
    public void setLineIdx( int lineNo, int idxVal ) {
        lineIdx[ lineNo ] = idxVal;
    }

    /** Get the size of the line based array */
    public int getLineArrayBoundary() {
        return( lineArrayBoundary );
    }

    /**
     *  Get the amount of the line based array that is in use + 500
     */
    public int getLineArraySize() {
        return( numberLines + 500 );
    }

    /** Check if TokenEnumerator needs retokenizing
     *
     *  @return true if the TokenEnumerator needs retokenizing, otherwise false
     */
    public boolean isDirty() {
        return( dirtyFileData );
    }

    /** Get the number of tokens on a line */
    int lastLineChecked = -1;
    int firstTokenOnLastLineChecked = -1;
    int startOffsetOfNextLine = -1;
    public int getNumberTokensOnLine( int lineNumber ) {
        int startOffset = 0;
        if ( lineNumber > lastLineChecked ) {
            if ( startOffsetOfNextLine != -1 ) {
                startOffset = startOffsetOfNextLine;
            }
        }
        boolean foundIt = false;
        int result = 0;
        for ( int i = startOffset; i < numberTokens; i++ ) {
            if ( !foundIt ) {
                if ( lineNumberArray[ i ] == lineNumber ) {
                    firstTokenOnLastLineChecked = i;
                    lastLineChecked = lineNumber;
                    result = 1;
                    foundIt = true;
                } else if ( lineNumberArray[i] > lineNumber ) {
                    break;
                }
            } else {
                if ( lineNumberArray[ i ] != lineNumber ) {
                    startOffsetOfNextLine = i + 1;
                    return( result );
                }
                result++;
            }
        }
        return( result );
    }

    public int getFirstTokenOnLine( int lineNumber ) {
        if ( lineNumber == lastLineChecked ) {
//   System.out.println( "ret1" + firstTokenOnLastLineChecked );
            return( firstTokenOnLastLineChecked );
        } else if ( lineNumber > lastLineChecked ) {
            for ( int i = startOffsetOfNextLine; i < numberTokens; i++ ) {
                if ( lineNumberArray[ i ] == lineNumber ) {
//                    System.out.println( "ret 2" );
                    return( i );
                } else if ( lineNumberArray[i] > lineNumber ) {
                    return( -1 );
                }
            }
            return( -1 );
        }
        for ( int i = 0; i < numberTokens; i++ ) {
            if ( lineNumberArray[i] == lineNumber ) {
  //              System.out.println( "ret3" );
                return( i );
            } else if ( lineNumberArray[i] > lineNumber ) {
                return( -1 );
            }
        }

        return( -1 );
    }

    /** Remove a set of tokens */
    public void removeTokens( int startToken, int nTokens ) {
        dirtyFileData = true;
        int end = numberTokens - nTokens;
        for ( int i = startToken; i < end; i++ ) {
            lineNumberArray[i] = lineNumberArray[ i + nTokens ];
            lineOffsetArray[i] = lineOffsetArray[ i + nTokens ];
            lineBreakArray[i] = lineBreakArray[ i + nTokens ];
            tokenTypeArray[i] = tokenTypeArray[ i + nTokens ];
            tokenSizeArray[i] = tokenSizeArray[ i + nTokens ];
        }
        numberTokens -= nTokens;
    }

    /** Create a new line at a specific offset, assumes
     *  fileData already set up.
     *
     *  @param newLineNumber the new line number
     *  @param byteOffset the offset into the fileData array of the
     *     text of the line
     */
    protected void insertLine( int newLineNumber, int byteOffset ) {
        dirtyFileData = true;
        addLineCapacity();
        for ( int i = numberLines; i > newLineNumber; i-- ) {
            lineIdx[ i ] = lineIdx[ i - 1 ];
        }
        numberLines++;
        lineIdx[ newLineNumber ] = byteOffset;
    }

    /** Expand the size of a line by shifting bytes in fileData, and shifting
     *  offset values in lineIdx
     */
    protected void expandSize( int lineNumber, int size ) {
        // size must be at least 20
        if ( size < 20 ) {
            size = 20;
        }

        // if we are on the last line number, the allocated size is the remainder of
        // the fileData size.
        int allocatedLineSize = 0;
        if ( lineNumber == ( numberLines - 1 )) {
            allocatedLineSize = byteArrayBoundary - lineIdx[ lineNumber ];
        // Otherwise, the allocated line size is the difference between this line and
        // the next non-error indicating line
        } else {
            int nextLine = lineNumber + 1;
            if ( nextLine >= numberLines ) {
                allocatedLineSize = byteArrayBoundary - lineIdx[ lineNumber ];
            } else {
                allocatedLineSize = lineIdx[ nextLine ] - lineIdx[ lineNumber ];
            }
        }

        int actualLineSize = 0;
        int scanner = lineIdx[ lineNumber ];
        while (( scanner < byteArrayBoundary ) && ( fileData[ scanner ] != 0 )) {
            actualLineSize++;
            scanner++;
        }
        if ( fileData[ scanner ] == 0 ) {
            actualLineSize++;
        }

        // If we don't have enough room for the expansion, make sure fileData
        // size can accommodate it, then shift data of all subsequent lines
        if (( allocatedLineSize - actualLineSize ) < size ) {
            ensureByteCapacity( size );
            if ( lineNumber != ( numberLines - 1 )) {
                int zboundary = lineIdx[ lineNumber ];
                while ( fileData[ zboundary ] != 0 ) {
                    zboundary++;
                }
                zboundary++;
                rightShiftBytes( zboundary, size );
                int boundary = lineIdx[ lineNumber ];
                for ( int i = 0; i < numberLines; i++ ) {
                    if ( lineIdx[i] > boundary ) {
                        lineIdx[ i ] += size;
                    }
                }
            }
        }
    }

    /** Remove a set of lines */
    public void removeLines( int startLine, int n ) {
        dirtyFileData = true;
        int end = numberLines - n;
        for ( int i = startLine; i < end; i++ ) {
            lineIdx[ i ] = lineIdx[ i + n ];
        }
        numberLines -= n;
        for ( int i = 0; i < numberTokens; i++ ) {
            int lineNo = lineNumberArray[i];
            if ( lineNo >= startLine ) {
                lineNo -= n;
                lineNumberArray[i] = lineNo;
            }
        }
    }

    /**
     *  Get the offset in the byte array of the first byte of a particular line
     */
    public int getLineIdx( int lineNo ) {
        return( lineIdx[ lineNo ] );
    }

    /** Get the number of bytes occupied by a line, including 0 bytes,
     *  and assuming that next line immediately follows.
     */
    public int getLineSize( int lineNo ) {
        if ( lineNo == ( numberLines - 1 )) {
            return( fileDataIdx - lineIdx[ lineNo ] );
        } else {
            return( lineIdx[ lineNo + 1 ] - lineIdx[ lineNo ] );
        }
    }

    // This version is going with the very simple form of one byte per character
    // with new line characters represented by 0 byte
    protected byte[] fileData;
    protected int fileDataIdx = 0;
    protected int byteArrayBoundary;

    /**
     *  Get the byte array used to store file text.
     */
    public byte[] getFileData() {
        return( fileData );
    }


    /** Get the number of bytes used in the byte array.  */
    public int getFileDataIdx() {
        return( fileDataIdx );
    }

    /** Set the next free location in the byte array */
    public void setFileDataIdx( int fileDataIdx ) {
        this.fileDataIdx = fileDataIdx;
    }

    /**
     *  set all array objects to null, so they can be garbage collected
     */
    public void wipeout() {
        fileData = null;
        lineIdx = null;
        lineNumberArray = null;
        lineOffsetArray = null;
        lineBreakArray = null;
        tokenSizeArray = null;
    }

    /** Make token be the first token in a line */
    public void startLineWith( int tokenOffset ) {
        if ( tokenOffset == 0 ) {
            return;
        }
        int tokenLine = lineNumberArray[ tokenOffset ];
        int prevTokenLine = lineNumberArray[ tokenOffset - 1 ];
        if ( prevTokenLine != tokenLine ) {
            return;
        }

        // split the lines
        int tokenByteOffset = getByteOffset( tokenOffset );
        int tokenLineOffset = lineOffsetArray[ tokenOffset ];
        boolean splitOnSpace = true;
        if ( tokenByteOffset == ( getByteOffset( tokenOffset - 1 ) + getSize( tokenOffset - 1 ))) {
            splitOnSpace = false;
        }
        split_line( tokenLine, tokenLineOffset, splitOnSpace );

        // now all the line numbers of all tokens have to be adjusted
        for ( int i = tokenOffset; i < numberTokens; i++ ) {
            lineNumberArray[ i ] += 1;
        }

        // all the tokens on the new line have to be adjusted so that first token is at offset 0
        int scanner = tokenOffset;
        while ( lineNumberArray[ scanner ] == ( tokenLine + 1 )) {
            lineOffsetArray[ scanner ] -= tokenLineOffset;
            scanner++;
        }
    }

    /** split a line at a particualr offset
     *
     *  @param tokenLine the line to split
     *  @param tokenLineOffset offset into the bytes where to split, takes into
     *                         account rle of spaces
     *  @param splitOnSpace
     */
    protected void split_line( int tokenLine, int tokenLineOffset, boolean splitOnSpace ) {
        dirtyFileData = true;
        int tokenByteOffset = lineIdx[ tokenLine ] + tokenLineOffset;
        insertLine( tokenLine + 1, tokenByteOffset );

        // at this point, lineIdx is set correctly.  However, if line is split
        // where there is no space between tokens, we have to shift fileData
        // by one byte, and adjust all lineIdx entries
        if ( !splitOnSpace ) {
            rightShiftBytes( tokenByteOffset, 1 );
            for ( int i = tokenLine + 1; i < numberLines; i++ ) {
                lineIdx[i] += 1;
            }
            fileData[ tokenByteOffset ] = 0;
        } else {
            fileData[ tokenByteOffset - 1 ] = 0;
        }
    }

    /** Insert one token of specific type */
    public void insert( int tokenOffset, String tok1, byte tokType ) {
        int len1 = tok1.length();
        int length = len1 + 1;
        ensureByteCapacity( length );
        ensureTokenCapacity( 1 );
        rightShiftTokens( tokenOffset, 1 );
        int tokenByteOffset = getByteOffset( tokenOffset );
        rightShiftBytes( tokenByteOffset, length );
        for ( int i = 0; i < len1; i++ ) {
            fileData[ tokenByteOffset++ ] = (byte)tok1.charAt( i );
        }
        fileData[ tokenByteOffset++ ] = (byte)' ';
        tokenTypeArray[ tokenOffset ] = tokType;
        tokenSizeArray[ tokenOffset ] = (short)len1;
        int scanner = tokenOffset + 1;
        int insertionLine = lineNumberArray[ tokenOffset ];
        while ( lineNumberArray[ scanner ] == insertionLine ) {
            lineOffsetArray[ scanner ] += length;
            scanner++;
            if ( scanner >= numberTokens ) {
                break;
            }
        }
        for ( int i = insertionLine + 1; i < numberLines; i++ ) {
            lineIdx[i] += length;
        }
    }

    /** Insert two tokens */
    public void insert( int tokenOffset, String tok1, String tok2 ) {
        int len1 = tok1.length();
        int len2 = tok2.length();
        int length = len1 + len2 + 2;
        ensureByteCapacity( length );
        ensureTokenCapacity( 2 );
        rightShiftTokens( tokenOffset, 2 );
        int tokenByteOffset = getByteOffset( tokenOffset );
        rightShiftBytes( tokenByteOffset, length );
        for ( int i = 0; i < len1; i++ ) {
            fileData[ tokenByteOffset++ ] = (byte)tok1.charAt( i );
        }
        fileData[ tokenByteOffset++ ] = (byte)' ';
        for ( int i = 0; i < len2; i++ ) {
            fileData[ tokenByteOffset++ ] = (byte)tok2.charAt( i );
        }
        fileData[ tokenByteOffset++ ] = (byte)' ';
        lineBreakArray[ tokenOffset ] = true;
        tokenTypeArray[ tokenOffset ] = NameToken;
        tokenSizeArray[ tokenOffset ] = (short)len1;
        int insertionLine = lineNumberArray[ tokenOffset ];
        lineNumberArray[ tokenOffset + 1 ] = insertionLine;
        lineOffsetArray[ tokenOffset + 1 ] = (short)(lineOffsetArray[ tokenOffset ] + len1 + 1);
        lineBreakArray[ tokenOffset + 1 ] = false;
        tokenTypeArray[ tokenOffset + 1 ] = NameToken;
        tokenSizeArray[ tokenOffset + 1 ] = (short)len2;
        int scanner = tokenOffset + 2;
        while ( lineNumberArray[ scanner ] == insertionLine ) {
            lineOffsetArray[ scanner ] += length;
            scanner++;
            if ( scanner >= numberTokens ) {
                break;
            }
        }
        for ( int i = insertionLine + 1; i < numberLines; i++ ) {
            lineIdx[i] += length;
        }
    }

    /** Replace a token with a specific String value */
    public void replace( int tokenOffset, String newToken ) {
        // new length - original length is the change
        int newTokenLength = newToken.length();
        int lengthChange = newTokenLength - tokenSizeArray[ tokenOffset ];
        ensureByteCapacity( lengthChange );
        int tokenByteOffset = getByteOffset( tokenOffset );
        if ( lengthChange > 0 ) {
            rightShiftBytes( tokenByteOffset, lengthChange );
        } else {
            for ( int i = tokenByteOffset; i < fileDataIdx; i++ ) {
                fileData[ i ] = fileData[ i - lengthChange ];
                if ( fileData[i] == 0 ) {
                    break;
                }
            }
        }
        for ( int i = 0; i < newTokenLength; i++ ) {
            fileData[ tokenByteOffset++ ] = (byte)newToken.charAt( i );
        }
        tokenSizeArray[ tokenOffset ] = (short)newTokenLength;
        int insertionLine = lineNumberArray[ tokenOffset ];
        int scanner = tokenOffset + 1;
        while ( lineNumberArray[ scanner ] == insertionLine ) {
            lineOffsetArray[ scanner ] += lengthChange;
            scanner++;
            if ( scanner >= numberTokens ) {
                break;
            }
        }
        if ( lengthChange > 0 ) {
            for ( int i = insertionLine + 1; i < numberLines; i++ ) {
                lineIdx[i] += lengthChange;
            }
        }
    }


    /** Shift all token arrays right by the specified amount starting at a specific boundary */
    void rightShiftTokens( int boundary, int amount ) {
        for ( int i = numberTokens - 1; i >= boundary; i-- ) {
            lineNumberArray[ i + amount ] = lineNumberArray[i];
            lineOffsetArray[ i + amount ] = lineOffsetArray[i];
            lineBreakArray[ i + amount ] = lineBreakArray[i];
            tokenTypeArray[ i + amount ] = tokenTypeArray[i];
            tokenSizeArray[ i + amount ] = tokenSizeArray[i];
        }
        numberTokens += amount;
    }


    /** Shift all data bytes right by the specified amount starting at a specific boundary */
    void rightShiftBytes( int boundary, int amount ) {
        // first right shift all the fileData
        for ( int i = fileDataIdx - 1; i >= boundary; i-- ) {
            fileData[ i + amount ] = fileData[ i ];
        }
        fileDataIdx += amount;
    }

    /**
     *  copy array objects from one TokenEnumerator to another
     */
    public void snarfArrays( TokenEnumerator source ) {
//        fileData = source.getFileData();
//        lineIdx = source.getLineIdx();
        lineNumberArray = source.getLineNumberArray();
        lineOffsetArray = source.getLineOffsetArray();
        lineBreakArray = source.getLineBreakArray();
        tokenTypeArray = source.getTokenTypeArray();
        tokenSizeArray = source.getTokenSizeArray();
        tokenArrayBoundary = source.getTokenArrayBoundary();
        lineArrayBoundary = source.getLineArrayBoundary();
//        byteArrayBoundary = source.getByteArrayBoundary();
    }


    /** get the number of bytes available in the byte array */
    public int getByteArrayBoundary() {
        return( byteArrayBoundary );
    }
    public int getByteArraySize() {
        return( fileDataIdx + 10000 );
    }

    String fileUrl;
    public String getFileUrl() {
        return( fileUrl );
    }
    public void setFileUrl( String fileUrl ) {
        this.fileUrl = fileUrl;
    }

    /** Class constructor
     *
     *  @param  is      InputStream containing the data
     *  @param  inFile  url used to create the InputStream
     */
	public TokenEnumerator( InputStream is, String inFile ) throws FileNotFoundException, IOException {
		this( is, inFile, null, null );
	}

    /** Class constructor
     *
     *  @param  is      InputStream containing the data
     *  @param  inFile  url used to create the InputStream
     *  @param  allowUnterminatedString if true, QuotedStringContinuation tokens are allowed,
     *   otherwise, QuotedStrings are automatically ended on the line they start on even if there is
     *   no matching quote
     */
	public TokenEnumerator( InputStream is, String inFile, boolean allowUnterminatedString ) throws FileNotFoundException, IOException {
		this( is, inFile, null, null, allowUnterminatedString );
	}

    static public int lastId = 0;
    int myId;

    /**  Class constructor
    *
    *  @param  is     InputStream containing the data
    *  @param  inFile url used to create the InputStream
    *  @param  frl    callback to give progress
    *  @param  fileSource  File object if a local file
    */
	public TokenEnumerator( InputStream is, String inFile, ProgressIndicator frl, File fileSource ) throws FileNotFoundException, IOException {
	    this( is, inFile, frl, fileSource, true );
	}

	public TokenEnumerator( InputStream is, String inFile, ProgressIndicator frl, File fileSource, boolean allowUnterminatedString ) throws FileNotFoundException, IOException {
	    lastId++;
	    myId = lastId;
	    fileUrl = inFile;
	    long fileLength = 0;
	    if ( fileSource != null ) {
	        fileLength = fileSource.length();
	        if ( fileLength < InitialByteArraySize ) {
	            fileLength += fileLength/2;
	        } else {
    	        fileLength = ( fileSource.length()/100000 ) * 100000 + 100000;
    	    }
	    }
	    if ( fileLength == 0 ) {
	        fileLength = InitialByteArraySize;
	    }
	    init( fileLength );
        loadLines( is, frl, allowUnterminatedString );
	}

	/** enumerate sequence of tokens from a string */
	public TokenEnumerator( String s ) {
	    init( SmallInitialByteArraySize, SmallInitialTokenArraySize, 3 );
	    TokenFactory t = new TokenFactory();
	    addLine( s, t );
	    tokenScannerOffset = -1;
	}

	/** used when recreating token enumerator based on old one */
	public TokenEnumerator() {
	    lastId++;
	    myId = lastId;
	    init( InitialByteArraySize, InitialTokenArraySize, InitialLineArraySize );
	}

	/** Create a token enumerator with built in arrays of a specific size */
	public TokenEnumerator( int byteArraySize, int tokenArraySize, int lineArraySize ) {
	    init( byteArraySize, tokenArraySize, lineArraySize );
	}

    /** Create one tokenEnumerator using the same arrays as another.
     *  Original intent was to allow arrays to be reused during reformatting,
     *  however, this only works if same comment skipping strategy in place,
     *  if not, there is risk that arrays wipe out each other.
     */
	public TokenEnumerator( TokenEnumerator source ) {
    	lastId++;
    	myId = lastId;
	    snarfArrays( source );
	    if (( source.getByteArrayBoundary() - source.getFileDataIdx() ) < InitialByteArraySize ) {
    	    fileData = new byte[ source.getByteArrayBoundary() + InitialByteArraySize ];
    	    byteArrayBoundary = source.getByteArrayBoundary();
    	} else {
    	    fileData = new byte[ source.getByteArrayBoundary() ];
    	    byteArrayBoundary = source.getByteArrayBoundary();
    	}
    	lineIdx = new int[ source.getLineArrayBoundary() ];
	fileUrl = source.getFileUrl();
    }

    /** Initialize all array sizes based on file length, initialize byte array also.
     *
     *  @param byteArraySize estimated size of the byte array
     */
    void init( long byteArraySize ) {
        init( byteArraySize, true );
    }

    /** Initialize all array sizes based on file length
     *
     *  @param byteArraySize estimated size of byte array
     *  @param initializeByteArray true if the byte array is initialized also,
     *     it is not initialized in the case of reading from serialized version.
     */
    void init( long byteArraySize, boolean initializeByteArray ) {
	    int tokLength = (int)byteArraySize/4;
	    if ( tokLength <= 0 ) {
	        tokLength = 100;
	    }
	    int lineLength = (int)byteArraySize/6;
	    if ( lineLength <= 0 ) {
	        lineLength = 50;
	    }
	    if ( !initializeByteArray ) {
	        lineLength = lineArrayBoundary;
	    }
        init( byteArraySize, tokLength, lineLength, initializeByteArray );
    }

    /**  Initialize all arrays
     *
     *  @param byteArraySize estimated size of byte array
     *  @param tokenArraySize estimated size of toke array
     *  @param lineArraySize estimated number of lines
     */
	void init( long byteArraySize, int tokenArraySize, int lineArraySize ) {
	    init( byteArraySize, tokenArraySize, lineArraySize, true );
	}

	/** Initialize all arrays, with possible exception of byte array.
	 *
	 *  @param byteArraySize estimated size of byte array
	 *  @param tokenArraySize estimated size of token array
	 *  @param lineArraySize estimated number of lines
	 *  @param initializeByteArray true if the byte array is to be initialized also.
	 */
	void init( long byteArraySize, int tokenArraySize, int lineArraySize, boolean initializeByteArray ) {
	    try {
    		tokenScannerOffset = 0;
    		numberTokens = 0;
    		lineNumberArray = new int[ tokenArraySize ];
    		lineOffsetArray = new short[ tokenArraySize ];
    		lineBreakArray = new boolean[ tokenArraySize ];
    		tokenTypeArray = new byte[ tokenArraySize ];
    		tokenSizeArray = new short[ tokenArraySize ];
    		tokenArrayBoundary = tokenArraySize;
    		lineArrayBoundary = lineArraySize;
    		if ( initializeByteArray ) {
        		fileData = new byte[(int)byteArraySize];
           		lineIdx = new int[ lineArraySize ];
        		numberLines = 0;
        	}
    		byteArrayBoundary = (int)byteArraySize;
    	} catch ( Exception e ) {
    	    System.out.println( "Exception " + e );
    	}
	}

	/** Read object form ObjectInputStream, and reconstruct transient fields.
	 *
	 */
	private void readObject( java.io.ObjectInputStream stream ) throws java.io.IOException {
	    try {
	        stream.defaultReadObject();
	        init( fileData.length, false );
            byteString = new ByteString();
            lineReporter = new LineReporter();
            retokenize();
	    } catch ( Exception e ) {
	        e.printStackTrace();
	    }
	}

    static public long presetLength = 0;
	void loadLines( InputStream inStream, ProgressIndicator frl, boolean allowUnterminatedString ) {
		TokenFactory t = new TokenFactory( allowUnterminatedString );
		long sizeInBytes = 0;
		try {
    		LineReader lr = new LineReader( inStream );
    		int counter = 0;
	   		while ( true ) {
    		    String s = lr.readLine();
    		    if ( s == null ) {
    		        break;
    		    } else if ( counter == 0 ) {
    		        // funny case where zero length file has bogus line result with single char 0xff
    		        if ( s.length() == 1 ) {
    		            break;
    		        }
    		    }
   		        addLine( s, t );
		        sizeInBytes += s.length() + 2;
		        if ( counter == 100 ) {
		            if (( frl != null ) && ( presetLength != 0 )) {
        		        frl.setPercent( (int)(sizeInBytes*100/presetLength));
        		    }
    		        counter = 0;
    		    }
    		    counter++;
    		    if ( GlobalProgressIndicator.abortCurrentProcess ) {
    		        return;
    		    }
		    }
		} catch( Exception e ) {
		    try {
		        e.printStackTrace();
    		    inStream.close();
    		    if ( frl != null ) {
    		        lineReporter.setLineCount( numberLines );
    		    }
    		} catch( Exception e2 ) {
    		}
		}
	}



	/** Retokenize the data in the "fileData" byte array. */
	public void retokenize() {
	    TokenFactory t = new TokenFactory();
	    int replacementLineNumber = 0;
	    numberTokens = 0;
	    for ( int i = 0; i < numberLines; i++ ) {
	        if ( processLine( t, lineIdx[ i ], replacementLineNumber )) {
	            lineIdx[ replacementLineNumber ] = lineIdx[ i ];
	            replacementLineNumber++;
	        }
	    }
	    numberLines = replacementLineNumber;
	    tokenScannerOffset = 0;
	    dirtyFileData = false;
	}

    public void addLineCapacity() {
        if ( numberLines == lineArrayBoundary ) {
            increaseLineCapacity();
        }
    }

    /** Increase the capacity of the lineIdx[] array */
    void increaseLineCapacity() {
        int newSize = lineArrayBoundary + InitialLineArraySize;
        int[] temp = new int[ newSize ];
        System.arraycopy( lineIdx, 0, temp, 0, lineArrayBoundary );
        lineIdx = temp;
        lineArrayBoundary = newSize;
    }

    /** Make sure the lineIdx[] array has enough room for <B>nLines</B> more lines.
     *
     *  @param nLines  the number of new lines to add
     */
    public void ensureLineCapacity( int nLines ) {
        if (( numberLines + nLines ) >= lineArrayBoundary ) {
            increaseLineCapacity();
        }
    }

    void ensureTokenCapacity( int nTokens ) {
        if (( numberTokens + nTokens ) >= tokenArrayBoundary ) {
            int newSize = tokenArrayBoundary + InitialTokenArraySize;
            int[] temp = new int[ newSize ];
            System.arraycopy( lineNumberArray, 0, temp, 0, tokenArrayBoundary );
            lineNumberArray = temp;
            temp = null;
            System.gc();
            short[] stemp = new short[ newSize ];
            System.arraycopy( lineOffsetArray, 0, stemp, 0, tokenArrayBoundary );
            lineOffsetArray = stemp;
            stemp = null;
            System.gc();
            byte[] bytetemp = new byte[ newSize ];
            System.arraycopy( tokenTypeArray, 0, bytetemp, 0, tokenArrayBoundary );
            tokenTypeArray = bytetemp;
            bytetemp = null;
            System.gc();
            stemp = new short[ newSize ];
            System.arraycopy( tokenSizeArray, 0, stemp, 0, tokenArrayBoundary );
            tokenSizeArray = stemp;
            stemp = null;
            System.gc();
            boolean[] btemp = new boolean[ newSize ];
            System.arraycopy( lineBreakArray, 0, btemp, 0, tokenArrayBoundary );
            lineBreakArray = btemp;
            tokenArrayBoundary = newSize;
            temp = null;
            btemp = null;
            System.gc();
        }
    }

    /** Add a line to the list of lines */
    public void addLine( String line, TokenFactory t ) {
        addLineCapacity();
        lineIdx[ numberLines ] = fileDataIdx;
        int len = line.length();
//        System.out.println( "ensure byte capacity " + len );
        ensureByteCapacity( len );
        // rle encode leading spaces
        int spaceCount = 0;
        for ( int i = 0; i < len; i++ ) {
            char x = line.charAt( i );
            if (( x == ' ' ) || ( x == '\t' )) {
                spaceCount++;
            } else {
                break;
            }
        }
        if ( spaceCount > 0 ) {
            if ( spaceCount > 100 ) {
                spaceCount = 100;
            }
            fileData[ fileDataIdx++ ] = (byte)(spaceCount | 128);
        }
//        System.out.println( "spaceCount is " + spaceCount + ", fileDataIdx " + fileDataIdx + ", line " + lno + ", byteArrayBoundary " + byteArrayBoundary );
        int internalCount = 0;
        int internalLine = 0;
        for ( int i = spaceCount; i < len; i++ ) {
            fileData[ fileDataIdx++ ] = (byte)line.charAt( i );
            internalCount++;
            if (( internalCount > 1000 ) && ( line.charAt( i ) == ' ' )) {
                internalLine++;
//                System.out.println( "internal line " + internalLine );
                fileData[ fileDataIdx++ ] = 0;
                byteString.setup( fileData, lineIdx[ numberLines ] );
                processLine( t, byteString, numberLines );
                numberLines++;
                addLineCapacity();
                lineIdx[ numberLines ] = fileDataIdx;
                internalCount = 0;
            }
        }
        fileData[ fileDataIdx++ ] = 0;
        byteString.setup( fileData, lineIdx[ numberLines ] );
        processLine( t, byteString, numberLines );
        numberLines++;
    }

    /** Add a line to the list of lines */
    public void addLine( StringBuffer line, TokenFactory t ) {
        addLineCapacity();
        lineIdx[ numberLines ] = fileDataIdx;
        int len = line.length();
        ensureByteCapacity( len );
        // rle encode leading spaces
        int spaceCount = 0;
        for ( int i = 0; i < len; i++ ) {
            char x = line.charAt( i );
            if (( x == ' ' ) || ( x == '\t' )) {
                spaceCount++;
            } else {
                break;
            }
        }
        if ( spaceCount > 0 ) {
            if ( spaceCount > 100 ) {
                spaceCount = 100;
            }
            fileData[ fileDataIdx++ ] = (byte)(spaceCount | 128);
        }
        for ( int i = spaceCount; i < len; i++ ) {
            fileData[ fileDataIdx++ ] = (byte)line.charAt( i );
        }
        fileData[ fileDataIdx++ ] = 0;
        byteString.setup( fileData, lineIdx[ numberLines ] );
        processLine( t, byteString, numberLines );
        numberLines++;
    }

    /** Optimized add line, takes bytes from source array.
     *
     *  @param offset offset into source array
     *  @param len number of bytes occupied by original line, including terminating byte
     *  @param sourceArray byte array containing text of original line
     *  @param t TokenFactory required by TokenEnumerator
     */
    public void addLine( int offset, int len, byte[] sourceArray, TokenFactory t ) {
        addLineCapacity();
        lineIdx[ numberLines ] = fileDataIdx;
        ensureByteCapacity( len );
        System.arraycopy( sourceArray, offset, fileData, fileDataIdx, len );
        fileDataIdx += len;
//        fileData[ fileDataIdx++ ] = 0;
        processLine( t, lineIdx[ numberLines ], numberLines );
        numberLines++;
    }

    public void addLine( int offset, int len, byte[] sourceArray,
        int numberSourceTokens, int sourceTokenOffset,
        short[] sourceLineOffsetArray, short[] sourceTokenSizeArray,
        boolean[] sourceLineBreakArray, byte[] sourceTokenTypeArray ) {
        addLineCapacity();
        lineIdx[ numberLines ] = fileDataIdx;
        ensureByteCapacity( len );
        System.arraycopy( sourceArray, offset, fileData, fileDataIdx, len );
        fileDataIdx += len;
        if ( numberSourceTokens > 0 ) {
            ensureTokenCapacity( numberSourceTokens );
            for ( int i = 0; i < numberSourceTokens; i++ ) {
                lineNumberArray[numberTokens + i] = numberLines;
            }
//            System.out.println( "addLine numberSourceTokens " + numberSourceTokens + ", sourceTokenOffset " + sourceTokenOffset );
//            System.out.println( "numberTokens " + numberTokens );
//            System.out.println( "sourceLineOffsetArray size is " + sourceLineOffsetArray.length );
//            System.out.println( "lineOffsetArray size is " + lineOffsetArray.length );
            System.arraycopy( sourceLineOffsetArray, sourceTokenOffset,
                lineOffsetArray, numberTokens, numberSourceTokens );
            System.arraycopy( sourceTokenSizeArray, sourceTokenOffset,
                tokenSizeArray, numberTokens, numberSourceTokens );
            System.arraycopy( sourceLineBreakArray, sourceTokenOffset,
                lineBreakArray, numberTokens, numberSourceTokens );
            System.arraycopy( sourceTokenTypeArray, sourceTokenOffset,
                tokenTypeArray, numberTokens, numberSourceTokens );
            numberTokens += numberSourceTokens;
        }
        numberLines++;
    }


    /** Make sure the fileData[] byte array has enough room for <B>len</B> more bytes.
     *
     *  @param len  number of bytes more that are to be added
     */
    public void ensureByteCapacity( int len ) {
        len++;
        while (( fileDataIdx + len ) >= byteArrayBoundary ) {
            int newSize = byteArrayBoundary;
            if ( byteArrayBoundary > 300000 ) {
                newSize += 300000;
            } else {
                newSize = newSize * 2;
            }
            byte[] btemp = new byte[ newSize ];
            System.arraycopy( fileData, 0, btemp, 0, byteArrayBoundary );
            fileData = btemp;
            btemp = null;
            Runtime.getRuntime().gc();
            byteArrayBoundary = newSize;
        }
    }

    public void lineDump() {
        System.out.println( numberLines + " lines." );
        for ( int i = 0; i < numberLines; i++ ) {
            System.out.println( (i+1) + " " + lineIdx[i] + ": " + getLineAt( i ));
        }
    }

    /** save lines to a PrintStream */
    public void saveLines( PrintStream ps ) {
        for ( int i = 0; i < numberLines; i++ ) {
            String s = getLineAt( i );
            ps.println( s );
        }
    }

    /** debugging dump of TokenEnumerator */
    public void dump() {
        System.out.println( "TokenEnumerator dump: FileDataIdx is " + fileDataIdx );
        int scanner = 0;
        StringBuffer buf = new StringBuffer();
        while ( scanner < fileDataIdx ) {
            if ( fileData[ scanner ] == 0 ) {
                String s = new String( buf );
                System.out.println( s );
                buf = new StringBuffer();
            } else if (( fileData[ scanner ] & 128 ) != 0 ) {
                int rleSpace = fileData[ scanner ] & 127;
                for ( int i = 0; i < rleSpace; i++ ) {
                    buf.append( ' ' );
                }
            } else {
                buf.append( (char)fileData[ scanner ] );
            }
            scanner++;
        }
        System.out.println( "Done with TokenEnumerator dump" );
    }

    public void detailDump( int tokenOffset ) {
        System.out.println( "token: " + tokenOffset + ", '" + toString( tokenOffset ) + "'" );
        System.out.println( "..isLineBreak: " + isLineBreak( tokenOffset ));
        System.out.println( "..line " + getLineNumber( tokenOffset ) + ", offset " + getLineOffset( tokenOffset ));
        System.out.println( "..line is '" + getLineAt( getLineNumber( tokenOffset )) + "'" );
        System.out.println( "..type is " + getTokenType( tokenOffset ));
    }

    public void detailDump() {
        detailDump( System.out, true );
    }

    public void detailDump( boolean details ) {
        detailDump( System.out, details );
    }

    /** Dump detailed TokenEnumerator data to a file.
     *
     *  @param fileName file to dump to
     */
    public void detailDump( String fileName ) {
        try {
            detailDump( new PrintStream( new FileOutputStream( fileName )), true );
        } catch ( Exception e ) {
        }
    }

    void detailDump( PrintStream ps, boolean allDetails ) {
        for ( int i = 0; i < numberLines; i++ ) {
            ps.println( (i+1) + ": " + getLineAt( i ));
        }
        if ( allDetails ) {
/*            for ( int i = 0; i < fileDataIdx; i++ ) {
                if ( fileData[i] != 0 ) {
                    if (( fileData[i] & 128 ) != 0 ) {
                        ps.println( i + ": rle " + (fileData[i] & 127) + " spaces" );
                    } else {
                        char c = (char)fileData[i];
                        ps.println( i + ": '" + c + "'" );
                    }
                } else {
                    ps.println( "zero" );
                }
            }*/
            for ( int i = 0; i < numberLines; i++ ) {
                ps.println( "line " + i + " at " + lineIdx[i] );
            }
            for ( int i = 0; i < numberTokens; i++ ) {
                ps.println( "token " + i + ", line " + getLineNumber( i ) + ", lineoffset " + getLineOffset( i ) + ", type " + getTokenType( i ) + ", value '" + toString( i ) + "'" );
//                ps.println( "token " + i + ", line " + getLineNumber( i ) + ", lineoffset " + getLineOffset( i ) + ", type " + getTokenType( i ));
            }
        }
    }

    //
    //  add a token to the token enumerator, which updates the lineNumberArray,
    //  lineOffsetArray, tokenTypeArray, and tokenSizeArray.  The lineBreakArray
    //  is for formatting only, and is updated in the specific VrmlElement subclasses
    //  through a call to "breakLineAt".
    //
    void addToken( int lineNumber, byte type, int offset, int size ) {
        if ( type == QuotedStringContinuation ) {
            offset = 0;
        }
        ensureTokenCapacity( 1 );
        lineNumberArray[ numberTokens ] = lineNumber;
        lineOffsetArray[ numberTokens ] = (short)offset;
        tokenSizeArray[ numberTokens ] = (short)size;
        lineBreakArray[ numberTokens ] = false;
        tokenTypeArray[ numberTokens ] = type;
        numberTokens++;
    }

    /** mark the token as one that is associated with a line break */
    public void breakLineAt( int offset ) {
        lineBreakArray[ offset ] = true;
    }

    /** is the token associated with a line break? */
    public boolean isLineBreak( int offset ) {
        return( lineBreakArray[ offset ] );
    }

    /** this class is not used by Vorlon, part of callback progress reporting */
	class LineReporter {
	    ProgressIndicator frl = null;
	    int lastReportedLine;
	    int lineCount;
	    int lastPercent;

	    public void setProgressIndicator( ProgressIndicator l ) {
	        frl = l;
	        lastReportedLine = 0;
	        lastPercent = 0;
	    }

	    public void setLineCount( int lineCount ) {
	        this.lineCount = lineCount;
	    }

	    public void report( int lineNumber ) {
	        if (( frl != null ) && ( lineCount != 0 )) {
	            int currentPercent = lineNumber*100/lineCount;
	            if ( currentPercent != lastPercent ) {
    	            frl.setPercent( lastPercent );
    	            lastPercent = currentPercent;
    	        }
	        }
	    }
	}

	transient LineReporter lineReporter = new LineReporter();

    /** get the String object for a line at a particular offset */
	public String getLineAt( int offset ) {
	    if ( offset >= numberLines ) {
	        return( null );
	    }
	    return( getString( lineIdx[ offset ], false ));
	}

	/** get the String object for a line at a particular offset, but limit size */
	public String getLineAt( int offset, int sizeLimit ) {
	    if ( offset >= numberLines ) {
	        return( null );
	    }
//	    System.out.println( "getLineAt " + offset + " with sizeLimit " + sizeLimit );
	    return( getString( lineIdx[ offset ], sizeLimit ));
	}

	public String getNospaceLineAt( int offset ) {
	    if ( offset >= numberLines ) {
	        return( null );
	    }
	    return( getNospaceString( lineIdx[ offset ] ));
	}

	public String getTabLineAt( int offset ) {
	    if ( offset >= numberLines ) {
	        return( null );
	    }
	    return( getString( lineIdx[ offset ], true ));
	}

	String getNospaceString( int lineByteOffset ) {
	    StringBuffer sb = new StringBuffer();
	    while ( fileData[ lineByteOffset ] != 0 ) {
	        if (( fileData[ lineByteOffset ] & 128 ) != 0 ) {
	            lineByteOffset++;
	            continue;
	        } else if ( fileData[ lineByteOffset ] == (byte)'\t' ) {
                sb.append( ' ' );
	        } else {
    	        sb.append( (char)fileData[ lineByteOffset ] );
    	    }
	        lineByteOffset++;
	    }
	    return( new String( sb ));
	}

    /** Get the String at a particular offset in the byte array */
	String getString( int lineByteOffset, boolean spaceToTab ) {
	    StringBuffer sb = new StringBuffer();
	    int firstByteOffset = lineByteOffset;
	    while ( fileData[ lineByteOffset ] != 0 ) {
	        if (( firstByteOffset == lineByteOffset ) && (( fileData[ lineByteOffset ] & 128 ) != 0 )) {
	            int rleSpace = fileData[ lineByteOffset ] & 127;
	            if (( tabIndentSize == 0 ) || !spaceToTab ) {
    	            for ( int i = 0; i < rleSpace; i++ ) {
    	                sb.append( ' ' );
    	            }
    	        } else {
    	            int ntabs = rleSpace/tabIndentSize;
    	            int remainderSpaces = rleSpace%tabIndentSize;
    	            for ( int i = 0; i < ntabs; i++ ) {
    	                sb.append( '\t' );
    	            }
    	            for ( int i = 0; i < remainderSpaces; i++ ) {
    	                sb.append( ' ' );
    	            }
    	        }
	        } else if ( fileData[ lineByteOffset ] == (byte)'\t' ) {
                sb.append( ' ' );
	        } else {
	            if (( fileData[ lineByteOffset ] & 128 ) != 0 ) {
	                int ival = fileData[ lineByteOffset ] & 127;
	                ival += 128;
	                System.out.println( "byte is " + ival );
	            }
    	        sb.append( (char)fileData[ lineByteOffset ] );
    	    }
	        lineByteOffset++;
	    }
	    return( new String( sb ));
	}

	String getString( int lineByteOffset, int sizeLimit ) {
	    StringBuffer sb = new StringBuffer();
	    int currentSize = 0;
	    while ( fileData[ lineByteOffset ] != 0 ) {
	        if (( fileData[ lineByteOffset ] & 128 ) != 0 ) {
	            int rleSpace = fileData[ lineByteOffset ] & 127;
   	            for ( int i = 0; i < rleSpace; i++ ) {
   	                sb.append( ' ' );
   	            }
   	            currentSize += rleSpace;
	        } else if ( fileData[ lineByteOffset ] == (byte)'\t' ) {
                sb.append( ' ' );
                currentSize++;
	        } else {
    	        sb.append( (char)fileData[ lineByteOffset ] );
    	        currentSize++;
    	    }
	        lineByteOffset++;
	        if ( currentSize >= sizeLimit ) {
	            break;
	        }
	    }
	    return( new String( sb ));
	}

    /** compare the current token to one in another TokenEnumerator
     *
     *  @return  true if the two tokens are identical, otherwise false.
     */
    public boolean sameAs( TokenEnumerator other ) {
        int l1 = length();
        int l2 = other.length();
        if ( l1 != l2 ) {
            return( false );
        }
        for ( int i = 0; i < l1; i++ ) {
            if ( charAt( i ) != other.charAt( i )) {
                return( false );
            }
        }
        return( true );
    }

    /**  Get the line number of a token */
    public int getLineNumber( int offset ) {
        return( lineNumberArray[ offset ] );
    }

    public void incLineNumbers( int line ) {
//        for ( int i = 0; i < numberTokens; i++ ) {
//            System.out.println( "before " + i + " line is " + getLineNumber( i ));
//        }
        for ( int i = numberTokens - 1; i >= 0; i-- ) {
            int lno = lineNumberArray[ i ];
            if ( lno >= line ) {
                lno++;
                lineNumberArray[i] = lno;
            } else {
                break;
            }
        }
//        for ( int i = 0; i < numberTokens; i++ ) {
//            System.out.println( "after " + i + " line is " + getLineNumber( i ));
//        }
    }

    /**  Get the offset within a line of a particular token, does not include leading white space
     *
     *  @param  offset  token offset
     */
    public int getLineOffset( int offset ) {
        int lineNumber = lineNumberArray[ offset ];
        int lineOffset = lineOffsetArray[ offset ];
        if ( lineOffset == 0 ) {
            return( 0 );
        }
        if ( lineOffset < 0 ) {
            lineOffset += 65536;
        }
        int byteOffset = lineIdx[ lineNumber ];
        if (( fileData[ byteOffset ] & 128 ) != 0 ) {
            int rleCount = fileData[ byteOffset ] & 127;
            return( lineOffset + rleCount - 1 );
        } else {
            return( lineOffset );
        }
    }

    /** get the character at a particular offset in the current token */
    public char charAt( int charOffset ) {
        int line = lineNumberArray[ tokenScannerOffset ];
        int lineOffset = lineOffsetArray[ tokenScannerOffset ];
        if ( lineOffset < 0 ) {
            lineOffset += 65536;
        }
        int byteOffset = lineIdx[ line ];
        return( (char)fileData[ byteOffset + lineOffset + charOffset ] );
    }

    /** get the character at a particular offset in a line */
    public char getLineChar( int lineNumber, int charOffset ) {
        int lineOffset = lineIdx[ lineNumber ];
        int rleCount = 0;
        if (( fileData[ lineOffset ] & 128 ) != 0 ) {
            rleCount = fileData[ lineOffset ] & 127;
            if ( charOffset < rleCount ) {
                return( ' ' );
            }
        }
        return( (char)fileData[ lineOffset + charOffset - rleCount ] );
    }

    /** Get the character at a particular offset for a token */
    public char charAt( int charOffset, int tokenOffset ) {
        int line = lineNumberArray[ tokenOffset ];
        int lineOffset = lineOffsetArray[ tokenOffset ];
        int byteOffset = lineIdx[ line ];
        if ( lineOffset < 0 ) {
            lineOffset += 65536;
        }
        return( (char)fileData[ byteOffset + lineOffset + charOffset ] );
    }

    /** Get the byte offset of a token in fileData byte array */
    public int getByteOffset( int tokenOffset ) {
        int line = lineNumberArray[ tokenOffset ];
        int lineOffset = lineOffsetArray[ tokenOffset ];
        if ( lineOffset < 0 ) {
            lineOffset += 65536;
        }
        int byteOffset = lineIdx[ line ];
        return( byteOffset + lineOffset );
    }

    /** get the length of the current token */
    public int length() {
        return( length( tokenScannerOffset ));
    }

    /** Get the length of a particular token */
    public int length( int offset ) {
        return( tokenSizeArray[ offset ] );
    }

    /**  Get the integer value of the token at a particular offset.
     *  @param offset offset of the token to check
     *  @return the integer value of the token at the offset, 0 if token isn't an int
     */
    public int getIntValue( int offset ) {
		int result = 0;
		int sign = 1;
		int len = length( offset );
		for ( int i = 0; i < len; i++ ) {
			if (( i == 0 ) && ( charAt( i, offset ) == '-' )) {
				sign = -1;
			} else {
				result = result*10 + ( charAt( i, offset ) - '0');
			}
		}
		return( sign*result );
    }

    public boolean isNegativeOne( int offset ) {
        if ( tokenSizeArray[ offset ] == 2 ) {
            if ( charAt( 0, offset ) == '-' ) {
                if ( charAt( 1, offset ) == '1' ) {
                    return( true );
                }
            }
        }
        return( false );
    }


    public int valueBeforeDot( int offset ) {
        int len = length( offset );
        int result = 0;
        int sign = 1;
        for ( int i = 0; i < len; i++ ) {
            if (( i == 0 ) && ( charAt( i, offset ) == '-' )) {
                sign = -1;
            } else {
                char cval = charAt( i, offset );
                if (( cval == '.' ) || ( cval == 'e' ) || ( cval == 'E' )) {
                    break;
                }
                result = result * 10 + ( charAt( i, offset ) - '0' );
            }
        }
        return( result );
    }

    public int zerosAfterDot( int offset ) {
        int len = length( offset );
        int zcount = 0;
        boolean foundDot = false;
        for ( int i = 0; i < len; i++ ) {
            if ( charAt( i, offset ) == '.' ) {
                foundDot = true;
            } else if ( foundDot ) {
                int val = charAt( i, offset ) - '0';
                if ( val == 0 ) {
                    zcount++;
                } else {
                    return( zcount );
                }
            }
        }
        return( zcount );
    }

    public int valueAfterDot( int offset ) {
        int len = length( offset );
        boolean foundDot = false;
        int zcount = 0;
        int result = 0;
        int afterDotResolution = 0;
        for ( int i = 0; ( i < len ) && ( afterDotResolution < 6 ); i++ ) {
            char cval = charAt( i, offset );
            if ( cval == '.' ) {
                foundDot = true;
            } else if (( cval == 'e' ) || ( cval == 'E' )) {
                break;
            } else if ( foundDot ) {
                int val = cval - '0';
                if ( val == 0 ) {
                    if ( zcount == 0 ) {
                        zcount = 10;
                    } else {
                        zcount = zcount * 10;
                    }
                } else {
                    zcount = 0;
                }
                result = result * 10 + val;
                afterDotResolution++;
            }
        }
        if ( zcount > 0 ) {
            result = result/zcount;
        }
        return( result );
    }

    /** is the token at a particular offset a right bracket or a right brace */
    public boolean isRightBracketOrBrace( int offset ) {
        return(( tokenTypeArray[ offset ] == RightBracket ) || ( tokenTypeArray[ offset ] == RightBrace ));
    }

    /** is the token a quoted string? */
    public boolean isQuotedString( int offset ) {
        return( tokenTypeArray[ offset ] == QuotedString );
    }

    /** is the token a right bracket? */
    public boolean isRightBracket( int offset ) {
        return( tokenTypeArray[ offset ] == RightBracket );
    }

    /** is the token a left bracket? */
    public boolean isLeftBracket( int offset ) {
        return( tokenTypeArray[ offset ] == LeftBracket );
    }

    /** is the token a right brace? */
    public boolean isRightBrace( int offset ) {
        return( tokenTypeArray[ offset ] == RightBrace );
    }

    /** is the token a left brace? */
    public boolean isLeftBrace( int offset ) {
        return( tokenTypeArray[ offset ] == LeftBrace );
    }

    /** is the token a bracket or brace? */
    public boolean isSpecialCharacter( int offset ) {
        int tokenType = tokenTypeArray[ offset ];
        return(( tokenType == RightBracket ) || ( tokenType == RightBrace ) ||
               ( tokenType == LeftBracket ) || ( tokenType == LeftBrace ));
    }

    /** is the token a quoted string continuation? */
    public boolean isContinuationString( int offset ) {
        return( tokenTypeArray[ offset ] == QuotedStringContinuation );
    }

    /** is the token a number? */
    public boolean isNumber( int offset ) {
        return( tokenTypeArray[ offset ] == NumberToken );
    }

    /** is the token a name? */
    public boolean isName( int offset ) {
        int type = tokenTypeArray[ offset ];
        return(( type == NameToken ) || ( type == Keyword1Token ) || ( type == Keyword2Token ));
    }

    /** skip to the next token of a particular type */
    public void skipTo( int tokenType ) {
        int prevToken = tokenScannerOffset;
        int scanner = tokenScannerOffset;
        while (( scanner != -1 ) && ( tokenTypeArray[ scanner ] != tokenType )) {
            prevToken = scanner;
            scanner = getNextToken();
        }
        tokenScannerOffset = prevToken;
    }


    /** Skip to the first number token, or end of stream, whichever comes first.
     *
     *  @return  the offset of the next number token (current token if the
     *           current one is a number), or -1 if no number tokens found
     */
    public int skipNonNumbers() {
        while ( tokenScannerOffset < numberTokens ) {
            if ( isNumber( tokenScannerOffset )) {
                break;
            } else {
                tokenScannerOffset = getNextToken();
                if ( tokenScannerOffset == -1 ) {
                    break;
                }
            }
        }
        return( tokenScannerOffset );
    }

    /** Skip to the a specific number token */
    public int skipToNumber( int offset ) {
        int currentOffset = 0;
        while ( tokenScannerOffset < numberTokens ) {
            if ( tokenTypeArray[ tokenScannerOffset ] == NumberToken ) {
                if ( currentOffset == offset ) {
                    return( tokenScannerOffset );
                } else {
                    currentOffset++;
                }
            }
            tokenScannerOffset++;
        }
        return( -1 );
    }

    /** is this a comment token? */
    public boolean isComment( int offset ) {
        return( tokenTypeArray[ offset ] == CommentToken );
    }

    /** compare current token to a string */
    public boolean sameAs( String s ) {
        return( sameAs( tokenScannerOffset, s ));
    }

    /** compare token to a string */
    public boolean sameAs( int tokenOffset, String s ) {
        if ( tokenOffset == -1 ) {
            return( false );
        } else if ( tokenSizeArray[ tokenOffset ] != s.length() ) {
            return( false );
        }
        int size = tokenSizeArray[ tokenOffset ];
        for ( int i = 0; i < size; i++ ) {
            if ( s.charAt( i ) != charAt( i, tokenOffset )) {
                return( false );
            }
        }
        return( true );
    }

    /** compare token to a string, case insensitive */
    public boolean nearlySameAs( int tokenOffset, String s ) {
        if ( tokenOffset == -1 ) {
            return( false );
        } else if ( tokenSizeArray[ tokenOffset ] != s.length() ) {
            return( false );
        }
        int size = tokenSizeArray[ tokenOffset ];
        for ( int i = 0; i < size; i++ ) {
            if ( Character.toLowerCase( s.charAt( i )) != Character.toLowerCase( charAt( i, tokenOffset ))) {
                return( false );
            }
        }
        return( true );
    }

    /** compare beginning of token to a string */
    public boolean matches( int tokenOffset, String s ) {
        int size = tokenSizeArray[ tokenOffset ];
        if ( size >= s.length() ) {
            for ( int i = 0; i < size; i++ ) {
                if ( s.charAt( i ) != charAt( i, tokenOffset )) {
                    return( false );
                }
            }
            return( true );
        }
        return( false );
    }

    /** compare two tokens */
    public boolean sameAs( int token1offset, int token2offset ) {
        if (( token1offset == -1 ) || ( token2offset == -2 )) {
            return( false );
        }
        if ( tokenSizeArray[ token1offset ] != tokenSizeArray[ token2offset ] ) {
            return( false );
        }
        int size = tokenSizeArray[ token1offset ];
        for ( int i = 0; i < size; i++ ) {
            if ( charAt( i, token1offset ) != charAt( i, token2offset )) {
                return( false );
            }
        }
        return( true );
    }

    /** Get size of a token, does not include leading white space */
    public int getSize( int tokenOffset ) {
        return( tokenSizeArray[ tokenOffset ] );
    }

    /** Convert a token to a string, does not include leading white space */
    public String toString( int tokenOffset ) {
        int size = tokenSizeArray[ tokenOffset ];
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < size; i++ ) {
            char c = charAt( i, tokenOffset );
            if (( c & 128 ) == 0 ) {
                sb.append( c );
            } else {
                size -= (int)( c & 127 );
                size++;
            }
        }
        return( new String( sb ));
    }


    /** Get the float value of a token */
    public float getFloat( int offset ) {
        int size = getSize( offset );
        int line = lineNumberArray[ offset ];
        int lineOffset = lineOffsetArray[ offset ];
        if ( lineOffset < 0 ) {
            lineOffset += 65536;
        }
        int scanner = lineIdx[ line ] + lineOffset;
        int resultBeforeDot = 0;
        int resultAfterDot = 0;
        boolean gotDot = false;
        int divisor =  1;
        boolean negate = false;
        int afterDotCount = 0;  // limit to 8
        for ( int i = 0; i < size; i++, scanner++ ) {
            char x = (char)fileData[ scanner ];
            if (( x >= '0' ) && ( x <= '9' )) {
                int n = (int)(x - '0');
                if ( gotDot ) {
                    afterDotCount++;
                    if ( afterDotCount > 8 ) continue;
                    resultAfterDot = resultAfterDot*10 + n;
                    divisor = divisor*10;
                } else {
                    resultBeforeDot = resultBeforeDot * 10 + n;
                }
            } else if ( x == '.' ) {
                gotDot = true;
            } else if ( x == 'e' ) {
                return( eGetFloat( offset ));
            } else if ( x == 'E' ) {
                return( eGetFloat( offset ));
            } else if ( x == '-' ) {
                negate = true;
            }
        }
        float result  = resultBeforeDot + (float)((float)resultAfterDot/(float)divisor);

        if ( negate ) {
            result = result*-1;
        }
        return( result );
    }

    float eGetFloat( int tokenOffset ) {
        String s = toString( tokenOffset );
        return( Float.valueOf( s ).floatValue() );
    }

    public boolean isFloat( int offset ) {
        if ( isNumber( offset )) {
            int size = getSize( offset );
            int line = lineNumberArray[ offset ];
            int lineOffset = lineOffsetArray[ offset ];
            if ( lineOffset < 0 ) {
                lineOffset += 65536;
            }
            int scanner = lineIdx[ line ] + lineOffset;
            for ( int i = 0; i < size; i++, scanner++ ) {
                if ( fileData[ scanner ] == (byte)'.' ) {
                    return( true );
                }
            }
        }
        return( false );
    }

    public boolean hasChar( int offset, char cval ) {
        int size = getSize( offset );
        int line = lineNumberArray[ offset ];
        int lineOffset = lineOffsetArray[ offset ];
        if ( lineOffset < 0 ) {
            lineOffset += 65536;
        }
        int scanner = lineIdx[ line ] + lineOffset;
        for ( int i = 0; i < size; i++, scanner++ ) {
            if ( fileData[ scanner ] == (byte)cval ) {
                return( true );
            }
        }
        return( false );
    }

    public void append( int tokenOffset, StringBuffer accumulator ) {
        int line = lineNumberArray[ tokenOffset ];
        int lineOffset = lineOffsetArray[ tokenOffset ];
        if ( lineOffset < 0 ) {
            lineOffset += 65536;
        }
        int byteOffset = lineIdx[ line ] + lineOffset;
        int size = tokenSizeArray[ tokenOffset ];
        for ( int i = 0; i < size; i++, byteOffset++ ) {
            if (( fileData[byteOffset] & 128 ) != 0 ) {
                int rleSize = fileData[ byteOffset ] & 127;
                size -= rleSize;
                size++;
                accumulator.append( ' ' );
            } else {
                accumulator.append( (char)fileData[ byteOffset ] );
            }
        }
    }


static int counter = 0;

    /** create tokens for a line of text
     *
     *  @param t     TokenFactory used to create tokens
     *  @param line  text containing tokens
     *  @param lineNumber  line number of the text line
     */
    ReturnInteger tokenOffsetReturn = new ReturnInteger();
    ReturnInteger tokenTypeReturn = new ReturnInteger();
    ReturnInteger tokenSizeReturn = new ReturnInteger();
    transient ByteString byteString = new ByteString();
	void processLine( TokenFactory t, ByteString line, int lineNumber ) {
		int charsProcessed = 0;
//		System.out.println( "Process line " + lineNumber + ": '" + line + "'" );
//		ReturnInteger tokenOffsetReturn = new ReturnInteger();
//		ReturnInteger tokenTypeReturn = new ReturnInteger();
//		ReturnInteger tokenSizeReturn = new ReturnInteger();
		int rleCount = 0;
		if (( fileData[ lineIdx[ lineNumber ]] & 128 ) != 0 ) {
		    rleCount = ( fileData[ lineIdx[ lineNumber ]] & 127 ) - 1;
		}
		int len = line.length();
		while ( charsProcessed < len ) {
			t.tokenize( byteString, charsProcessed,
			    tokenOffsetReturn, tokenTypeReturn, tokenSizeReturn );
			if ( tokenOffsetReturn.getValue() == -1 ) {
			    return;
			} else {
				charsProcessed = tokenOffsetReturn.getValue() + tokenSizeReturn.getValue();
                addToken( lineNumber, (byte)tokenTypeReturn.getValue(), tokenOffsetReturn.getValue() - rleCount, tokenSizeReturn.getValue() );
			}
		}
		counter++;
	}


	/**  Create tokens for a line
	 *
	 *  @param t     TokenFactory used to create tokens
	 *  @param fileDataOffset  offset into byte buffer for start of line
	 *  @param lineNumber      line number currently being processed
	 *
	 *  @return  true if line was processed, otherwise false
	 */
	protected boolean processLine( TokenFactory t, int fileDataOffset, int lineNumber ) {
        processLine( t, getByteString( fileDataOffset ), lineNumber );
        return( true );
	}

    protected ByteString getByteString( int fileDataOffset ) {
        byteString.setup( fileData, fileDataOffset );
        return( byteString );
    }

    /**  are there more tokens available */
	public boolean hasMoreElements() {
	    int state = tokenScannerOffset;
	    int nextToken = getNextToken();
	    tokenScannerOffset = state;
	    return( nextToken != -1 );
	}

	transient ProgressIndicator factoryResponseListener;

	/** set up call back to report token scanning progress.
	 *  Not used by Vorlon.
	 */
	public void notifyLineNumbers( ProgressIndicator frl ) {
		factoryResponseListener = frl;
		lineReporter.setProgressIndicator( factoryResponseListener );
		lineReporter.setLineCount( numberLines );
	}

	/** report back based on token */
	public void notifyByToken( int tokenOffset ) {
	    if ( lineReporter != null ) {
    	    lineReporter.report( lineNumberArray[ tokenOffset ] );
    	}
	}

	/** report back based on line */
	public void notifyByLine( int lineNumber ) {
	    if ( lineReporter != null ) {
    	    lineReporter.report( lineNumber );
    	}
	}

	int lastLineReported = -1;

	/** get the next token */
	public int getNextToken() {
	    if ( GlobalProgressIndicator.abortCurrentProcess ) {
	        return( -1 );
	    }
	    tokenScannerOffset++;
		skipCommentTokens();
		if ( tokenScannerOffset >= numberTokens ) {
		    return( -1 );
		} else {
		    if ( factoryResponseListener != null ) {
		        lineReporter.report( lineNumberArray[ tokenScannerOffset ] );
		    }
		    return( tokenScannerOffset );
		}
	}

    /**  get the token following a particular token */
	public int getNextToken( int prevToken ) {
	    if ( prevToken != -1 ) {
    	    tokenScannerOffset = prevToken;
    	    return( getNextToken() );
    	} else {
    	    return( -1 );
    	}
	}

	public int getNextNumber( int tokenOffset ) {
	    while ( tokenOffset > 0 ) {
	        if ( isNumber( tokenOffset )) {
	            return( tokenOffset );
	        } else {
	            tokenOffset = getNextToken( tokenOffset );
	        }
	    }
	    return( tokenOffset );
	}


	/** default is to skip comment tokens */
	boolean skipComments = true;

	/** getNextToken() will return comment tokens */
	public void disableCommentSkipping() {
	    skipComments = false;
	}

    /** getNextToken() will not return comment tokens */
	public void enableCommentSkipping() {
	    skipComments = true;
	}

    /**  skip comment tokens */
	void skipCommentTokens() {
		while ( tokenScannerOffset < numberTokens ) {
		    if (( tokenTypeArray[ tokenScannerOffset ] == CommentToken ) && skipComments ) {
		        tokenScannerOffset++;
		    } else if ( tokenTypeArray[ tokenScannerOffset ] == WhiteToken ) {
		        tokenScannerOffset++;
		    } else {
		        return;
		    }
		}
	}

	/** get an int form of the type of the token at a particular offset
	 *
	 *  @return  the type of token, one of TokenTypes
	 *  @see TokenTypes
	 */
	public int getType( int offset ) {
	    return( tokenTypeArray[ offset ] );
	}

	/** get a string form of the type of the token at a particular offset */
	public String getTokenType( int offset ) {
	    int type = tokenTypeArray[ offset ];
	    if ( type == WhiteToken ) {
	        return( "WhiteToken" );
	    } else if ( type == LeftBracket ) {
	        return( "LeftBracket" );
	    } else if ( type == RightBracket ) {
	        return( "RightBracket" );
	    } else if ( type == LeftBrace ) {
	        return( "LeftBrace" );
	    } else if ( type == RightBrace ) {
	        return( "RightBrace" );
	    } else if ( type == NameToken ) {
	        return( "NameToken" );
	    } else if ( type == Keyword1Token ) {
	        return( "Keyword1Token" );
	    } else if ( type == Keyword2Token ) {
	        return( "Keyword2Token" );
	    } else if ( type == NumberToken ) {
	        return( "NumberToken" );
	    } else if ( type == BadNumber ) {
	        return( "BadNumber" );
	    } else if ( type == QuotedString ) {
	        return( "QuotedString" );
	    } else if ( type == EmptyLine ) {
	        return( "EmptyLine" );
	    } else if ( type == QuotedStringContinuation ) {
	        return( "QuotedStringContinuation" );
	    } else if ( type == CommentToken ) {
	        return( "CommentToken" );
	    } else {
	        return( "Unknown type " + type );
	    }
	}


	/** state is just the current offset */
	public int getState() {
		return( tokenScannerOffset );
	}

    /** what is the current token offset */
	public int getCurrentTokenOffset() {
	    return( tokenScannerOffset );
	}

	/** restore previous state, used for lookahead */
	public void setState( int state ) {
		tokenScannerOffset = state;
	}

    /** create a byte array for a sequence of tokens */
    public byte[] getCharArray( int t1, int t2 ) {
        int l1 = tokenSizeArray[t1];
        int l2 = tokenSizeArray[t2];
        byte[] result = new byte[ l1 + l2 + 1 ];
        System.arraycopy( fileData, getByteOffset(t1), result, 0, l1 );
        System.arraycopy( fileData, getByteOffset(t2), result, l1+1, l2 );
        result[l1] = (byte)' ';
        return( result );
    }
    public byte[] getCharArray( int t1, int t2, int t3 ) {
        int l1 = tokenSizeArray[t1];
        int l2 = tokenSizeArray[t2];
        int l3 = tokenSizeArray[t3];
        byte[] result = new byte[ l1 + l2 + l3 + 2 ];
        System.arraycopy( fileData, getByteOffset(t1), result, 0, l1 );
        System.arraycopy( fileData, getByteOffset(t2), result, l1+1, l2 );
        System.arraycopy( fileData, getByteOffset(t3), result, l1+l2+2, l3 );
        result[l1] = (byte)' ';
        result[l1+l2+1] = (byte)' ';
        return( result );
    }

    public boolean copy3f( float[] result, int last ) {
        skipTo( NumberToken );
        if ( tokenScannerOffset >= last ) {
            return( false );
        }
        result[0] = getFloat( tokenScannerOffset );
        getNextToken();
        skipTo( NumberToken );
        if ( tokenScannerOffset >= last ) {
            return( false );
        }
        result[1] = getFloat( tokenScannerOffset );
        getNextToken();
        skipTo( NumberToken );
        if ( tokenScannerOffset >= last ) {
            return( false );
        }
        result[2] = getFloat( tokenScannerOffset );
        getNextToken();
        return( true );
    }

    public boolean copy2f( float[] result, int last ) {
        skipTo( NumberToken );
        if ( tokenScannerOffset >= last ) {
            return( false );
        }
        result[0] = getFloat( tokenScannerOffset );
        getNextToken();
        skipTo( NumberToken );
        if ( tokenScannerOffset >= last ) {
            return( false );
        }
        result[1] = getFloat( tokenScannerOffset );
        getNextToken();
        return( true );
    }
}


