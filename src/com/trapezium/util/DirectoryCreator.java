package com.trapezium.util;

import java.util.Vector;
import java.io.File;

public class DirectoryCreator {
    Vector createdDirectories;

    public DirectoryCreator() {
        createdDirectories = new Vector();
    }

    public String getDirName( String fileName ) {
	    int forwardSlashIndex = fileName.lastIndexOf( "/" );
	    int backSlashIndex = fileName.lastIndexOf( "\\" );
	    if (( forwardSlashIndex == -1 ) && ( backSlashIndex == -1 )) {
	        return null;
	    }
	    int terminatorIndex = forwardSlashIndex;
	    if ( backSlashIndex > terminatorIndex ) {
	        terminatorIndex = backSlashIndex;
	    }
	    return( fileName.substring( 0, terminatorIndex ));
    }
    
	public void createDestinationDirectory( String fileName ) {
	    String dirName = getDirName( fileName );
	    if ( dirName == null ) {
	        return;
	    }
	    if ( dirAlreadyCreated( dirName )) {
	        return;
	    } else if ( dirExists( dirName )) {
	        addDir( dirName );
	    } else {
	        createDir( dirName );
	        addDir( dirName );
	    }
	}

	boolean dirAlreadyCreated( String dirName ) {
	    if ( createdDirectories == null ) {
	        return( false );
	    } else {
	        int dsize = createdDirectories.size();
	        for ( int i = 0; i < dsize; i++ ) {
	            String dtest = (String)createdDirectories.elementAt( i );
	            if ( dtest.compareTo( dirName ) == 0 ) {
	                return( true );
	            }
	        }
	        return( false );
	    }
	}

	void addDir( String dirName ) {
	    if ( createdDirectories == null ) {
	        createdDirectories = new Vector();
	    }
	    createdDirectories.addElement( dirName );
	}

	void createDir( String dirName ) {
	    // break dir into component paths, create each path that doesn't exist
	    StringBuffer componentPaths = new StringBuffer();
	    int len = dirName.length();
	    for ( int i = 0; i < len; i++ ) {
	        char x = dirName.charAt( i );
	        componentPaths.append( x );
	        if (( x == '/' ) || ( x == '\\' )) {
	            String dir = new String( componentPaths );
	            if ( !dirExists( dir )) {
	                createDirPath( dir );
	            }
	        } else if ( i == ( len - 1 )) {
	            String dir = new String( componentPaths );
	            if ( !dirExists( dir )) {
	                createDirPath( dir );
	            }
	        }
   		}
	}

	void createDirPath( String dirName ) {
		File f = new File( dirName );
		f.mkdir();
	}

	public boolean dirExists( String dirName ) {
		File f = new File( dirName );
		if ( f.exists() ) {
			return( true );
		}
		return( false );
	}
}
