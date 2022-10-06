/*
 * @(#)FactoryData.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.factory;

import java.io.File;
import java.util.Vector;
import java.util.BitSet;

import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.visitor.ParentClearer;
import com.trapezium.edit.TokenEditor;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.edit.EditLintVisitor;
import com.trapezium.edit.Document;

public class FactoryData implements DocumentLoader {
	FileDescription fileDescription;
	FileSizeData currentFileInfo;
	FileSizeData totalFileInfo;

	String action;
	Exception error;
	int factoryNumber;
	boolean chainDone;
	String currentFactoryName;
	boolean nodeVerifyChecksEnabled;
	boolean usageChecksEnabled;

    boolean aborted;
    boolean validated;
    
	/** if this object is marked as temporary then its file will be
	    deleted when it is closed. */
	boolean temporary = false;

	EditLintVisitor lintErrors;
	
	// used to detect when a Chisel creates errors (a bug)
	int previousNumberErrors;
	int currentNumberErrors;
	
	public void setPreviousNumberErrors() {
	    previousNumberErrors = getNumberErrors();
	}
	
	public void setCurrentNumberErrors() {
	    currentNumberErrors = getNumberErrors();
//	    System.out.println( "previous number errors " + previousNumberErrors + ", current number errors " + currentNumberErrors );
	}
	
	public boolean errorsCreated() {
	    return( currentNumberErrors > previousNumberErrors );
	}

    public Document getDocument() {
        return( null );
    }

	/** Get the TokenEnumerator for the file */
	public TokenEnumerator getLineSource() {
		if ( lintErrors != null ) {
			return( lintErrors.getLineSource() );
		} else {
			return( null );
		}
	}
	
	boolean parseDisabled;
	/** optimization to disable unnecessary parsing, one time settable */
	public void disableParse() {
	    parseDisabled = true;
	}
	public void enableParse() {
	    parseDisabled = false;
	}
	public boolean isParseDisabled() {
	    boolean result = parseDisabled;
	    parseDisabled = false;
	    return( result );
	}
	public boolean isParseEnabled() {
	    boolean result = !parseDisabled;
	    parseDisabled = false;
	    return( result );
	}
	
	/** DocumentLoader interface, ProcessedFile really implements this */
	public void reload( Object scene ) {
	}

    // set to true if we loaded a gzipped file
    boolean gzipFlag;
    public void setGzip( boolean val ) {
        gzipFlag = val;
    }
    
    public boolean isGzipped() {
        return( gzipFlag );
    }
    public int getSizeReductionPercent() {
        int r = fileDescription.getPercent();
        if (( r != -1 ) && ( r < 100 )) {
            return( 100 - r );
        } else {
            return( 0 );
        }
    }
    
    /** Get the current percent of original file times 100 */
    public int getPercentX100() {
        int r = fileDescription.getPercentX100();
        if ( r > 10000 ) {
            r = 10000;
        }
        if (( r != -1 ) && ( r <= 10000 )) {
            return( r );
        } else {
            return( 0 );
        }
    }
    
	public int getFirstErrorLine() {
		if ( lintErrors != null ) {
			return( lintErrors.getFirstErrorLine() );
		} else {
			return( 0 );
		}
	}


	/** Merge error categories with file text, places marks in Slider to
	 *  indicate type and location of error.
	 */
	public void mergeLintErrors( FactoryResponseListener frl, BitSet errorMarks, BitSet warningMarks, BitSet nonconformanceMarks ) {
		if ( lintErrors != null ) {
			lintErrors.mergeErrors( frl, errorMarks, warningMarks, nonconformanceMarks );
		}
	}


	FactoryChain currentChain;
	int pushedFactoryNumber;
	FactoryChain pushedFactoryChain;
	
	public void wipeout() {
	    currentChain = null;
	    pushedFactoryChain = null;
	}
	
	public void setFactoryChain( FactoryChain runningChain ) {
		currentChain = runningChain;
	}

	public void pushFactoryInfo() {
	    pushedFactoryChain = currentChain;
	    pushedFactoryNumber = factoryNumber;
	}

	public void setChainListener( FactoryResponseListener frl ) {
		if ( currentChain != null ) {
			currentChain.setListener( frl );
		}
	}

	public void popFactoryInfo() {
		// only do the pop if the pushed number is valid, which means it is a pushed
		// number from an outer FactoryChain
		if ( pushedFactoryNumber != -1 ) {
			factoryNumber = pushedFactoryNumber;
			currentChain = pushedFactoryChain;
		}
		pushedFactoryNumber = -1;
		pushedFactoryChain = null;
	}

	public void setLintInfo( EditLintVisitor lv ) {
		lintErrors = lv;
	}

	public EditLintVisitor getLintInfo() {
	    return( lintErrors );
	}

	public boolean hasLintInfo() {
		return( lintErrors != null );
	}

	String errorSummaryString = null;
	public String getErrorSummaryString() {
	    errorSummaryString = null;
        if ( errorSummaryString == null ) {
   		    StringBuffer sb = new StringBuffer();
   		    if ( getNumberErrors() == 0 ) {
   		        sb.append( "No errors" );
   		    } else if ( getNumberErrors() == 1 ) {
   		        sb.append( "1 error" );
   		    } else {
   		        sb.append( getNumberErrors() + " errors" );
   		    }
   		    if ( !VrmlElement.disableBaseProfile ) {
       		    if ( getNumberNonconformances() == 1 ) {
       		        sb.append( ", 1 nonconformance" );
       		    } else if ( getNumberNonconformances() > 1 ) {
       		        sb.append( ", " + getNumberNonconformances() + " nonconformances" );
       		    }
       		}
       		if ( !VrmlElement.nowarning ) {
       		    if ( getNumberWarnings() == 1 ) {
       		        sb.append( ", 1 warning" );
       		    } else if ( getNumberWarnings() > 1 ) {
       		        sb.append( ", " + getNumberWarnings() + " warnings" );
       		    }
       		}
   		    sb.append( "." );
   		    errorSummaryString = new String( sb );
   		}
   		return( errorSummaryString );
   	}
   	
	public boolean hasErrors() {
		return( getNumberErrors() > 0 );
	}

	public boolean hasWarnings() {
	    return( getNumberWarnings() > 0 );
	}

	public int getNumberErrors() {
		if ( lintErrors != null ) {
			return( lintErrors.getErrorCount() );
		} else {
			return( 0 );
		}
	}

	public int getNumberWarnings() {
		if ( lintErrors != null ) {
			return( lintErrors.getWarningCount() );
		} else {
			return( 0 );
		}
	}
	
	public int getNumberNonconformances() {
	    if ( lintErrors != null ) {
	        return( lintErrors.getNonconformanceCount() );
	    } else {
	        return( 0 );
	    }
	}

    static int idcounter = 1;
    int id;
    public int getId() {
        return( id );
    }
	public FactoryData() {
	    id = idcounter;
	    idcounter++;
		fileDescription = new FileDescription();
		currentFileInfo = new FileSizeData();
		totalFileInfo = new FileSizeData();

		action = null;
		error = null;
		factoryNumber = -1;
		chainDone = false;
		nodeVerifyChecksEnabled = true;
		usageChecksEnabled = false;
		aborted = false;
		validated = false;
	}
	
	/** Set the aborted flag, set to true if aborted during processing */
	public void setAborted( boolean val ) {
	    // only unvalidated entries can be marked as aborted
	    if ( !validated ) {
    	    aborted = val;
    	}
	}
	
	public boolean getAborted() {
	    return( aborted );
	}
	
	/** Set the validated flag, abort operation closes files if it 
	 *  occurs during the first validation.  Subsequent validations
	 *  can be aborted without closing files.
	 */
	public void setValidated( boolean val ) {
	    validated = val;
	}
	
	public boolean getValidated() {
	    return( validated );
	}

    public void setNodeVerifyChecksEnabled( boolean value) {
//        System.out.println( "setNodeVerifyChecksEnabled " + value );
        nodeVerifyChecksEnabled = value;
    }

    public boolean getNodeVerifyChecksEnabled() {
        return( nodeVerifyChecksEnabled );
    }
    
    public void setUsageChecksEnabled( boolean value ) {
        usageChecksEnabled = value;
    }
    
    public boolean getUsageChecksEnabled() {
        return( usageChecksEnabled );
    }
    
	public void setFactoryName( String s ) {
		currentFactoryName = s;
	}

	public String getFactoryName() {
		return( currentFactoryName );
	}

	public void setChainDone( boolean val ) {
		chainDone = val;
	}

	public boolean getChainDone() {
		return( chainDone );
	}

	public void setFactoryNumber( int n ) {
		factoryNumber = n;
	}

	public int getFactoryNumber() {
		return( factoryNumber );
	}

	public void setUrl( String url ) {
		fileDescription.setUrl( url );
	}

	public String getUrl() {
		return( fileDescription.getUrl() );
	}

	public String getPath() {
	    String s = fileDescription.getUrl();
	    if ( s.indexOf( "/" ) > 0 ) {
	        s = s.substring( 0, s.lastIndexOf( "/" ));
	        return( s );
	    } else {
	        return( null );
	    }
	}

	public void setFile( File file ) {
	    fileDescription.setFile( file );
	}

	public File getFile() {
	    return( fileDescription.getFile() );
	}
	
	/** Get the original name for the file. */
	public String getOriginalName() {
	    return( fileDescription.getOriginalName() );
	}

	public void setTokenEditor( TokenEditor tokenStream ) {
		fileDescription.setTokenEditor( tokenStream );
		currentFileInfo.setNumberOfLines( 0 );
	}

	public TokenEditor getTokenEditor() {
	    return( fileDescription.getTokenEditor() );
	}

	public boolean isDirty() {
	    if ( fileDescription != null ) {
	        TokenEditor te = getTokenEditor();
	        if ( te != null ) {
	            return( te.isDirty() );
	        }
	    }
	    return( false );
	}

    /** Save the Scene associated with this FactoryData.
     *
     *  @param scene current scene for this FactoryData
     */
	public void setScene( Scene scene ) {
		fileDescription.setScene( scene );
		currentFileInfo.setNumberOfLines( 0 );
	}

    /** Get the Scene associated with this FactoryData.
     *
     *  @return the Scene associated with this FactoryData.
     */
	public Scene getScene() {
		return( fileDescription.getScene() );
	}

	public void setAction( String action ) {
		this.action = action;
	}

	public String getAction() {
		return( action );
	}

	public void setNumberOfLines( int n ) {
		currentFileInfo.setNumberOfLines( n );
	}

	public int getNumberOfLines() {
		return( currentFileInfo.getNumberOfLines() );
	}

	public long getSizeInBytes() {
		return( currentFileInfo.getSizeInBytes() );
	}

	public void setPolygonCount( int n ) {
		currentFileInfo.setPolygonCount( n );
	}

	public int getPolygonCount() {
		return( currentFileInfo.getPolygonCount() );
	}

	public void setError( Exception e ) {
		error = e;
	}

	public Exception getError() {
		return( error );
	}

	// get percents to the resolution ".1"
	public int getSizePercent() {
		return( (int)( currentFileInfo.getSizeInBytes() * 1000/totalFileInfo.getSizeInBytes() ));
	}

	public boolean isTemporary() {
	    return temporary;
	}
	public void setTemporary(boolean temporary) {
	    this.temporary = temporary;
	}
}


class FileSizeData {
	int numberOfLines;
	long sizeInBytes;
	int polygonCount;

	public FileSizeData() {
		numberOfLines = 0;
		sizeInBytes = 0;
		polygonCount = 0;
	}

	public void setNumberOfLines( int n ) {
		numberOfLines = n;
	}

	public int getNumberOfLines() {
		return( numberOfLines );
	}

	public long getSizeInBytes() {
		return( sizeInBytes );
	}

	public void setPolygonCount( int n ) {
		polygonCount = n;
	}

	public int getPolygonCount() {
		return( polygonCount );
	}
}

class FileDescription {
	String url;
	String originalName;
    File file;
	TokenEditor tokenEditor;
	Scene scene;
	int originalSize;

	public FileDescription() {
		url = null;
		file = null;
		tokenEditor = null;
		scene = null;
	}

	public void setUrl( String url ) {
		this.url = url;
	}

	public String getUrl() {
		return( url );
	}

	public void setFile( File file ) {
	    this.file = file;
	    url = file.getAbsolutePath();
	    if ( originalName == null ) {
	        originalName = url;
	    }
	}
	
	/** Get original name for the file */
	public String getOriginalName() {
	    return( originalName );
	}

	public File getFile() {
	    return( file );
	}

	public void setTokenEditor( TokenEditor tokenEditor ) {
		this.tokenEditor = tokenEditor;
	}

	public TokenEditor getTokenEditor() {
		return( tokenEditor );
	}

	public void setScene( Scene scene ) {
   	    if ( this.scene != null ) {
   	        Scene s = this.scene;
   	        ParentClearer pc = new ParentClearer();
   	        s.traverse( pc );
       	    Runtime.getRuntime().gc();
   	    }
	    if (( this.scene == null ) && ( scene != null )) {
	        TokenEnumerator te = scene.getTokenEnumerator();
	        originalSize = te.getFileDataIdx();
//	        System.out.println( "base size set to " + originalSize );
	    }
		this.scene = scene;
    }
    
    public int getPercent() {
        if (( scene != null ) && ( originalSize > 0 )) {
            TokenEnumerator te = scene.getTokenEnumerator();
            int currentSize = te.getFileDataIdx();
            int p = currentSize*100/originalSize;
//            System.out.println( "originalSize " + originalSize + " currentSize is " + currentSize + ", percent " + p );
            return( p );
        } else {
            return( -1 );
        }
    }
    
    public int getPercentX100() {
        if (( scene != null ) && ( originalSize > 0 )) {
            TokenEnumerator te = scene.getTokenEnumerator();
            int currentSize = te.getFileDataIdx();
            int osize = originalSize/10000;
            int p = 0;
            if ( osize < 10 ) {
                p = currentSize*10000/originalSize;
            } else if ( osize < 100 ) {
                osize = originalSize/1000;
                p = currentSize*10/osize;
            } else {
                p = currentSize/osize;
            }
//            System.out.println( "getPercentX100: originalSize " + originalSize + " currentSize is " + currentSize + ", percent " + p + ", osize " + osize );
            return( p );
        } else {
            return( -1 );
        }
    }
        

	public Scene getScene() {
		return( scene );
	}
}
