/*
 * @(#)UrlLocalizer.java
 *
 * Copyright (c) 1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 */
package com.trapezium.util;

import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.File;

public class UrlLocalizer extends RemoteUrlGenerator {
    String localDirPath;

    static final String SpecialPath = "special/";
    // set to false by createLocalFileName, set to true by getLocalDir
    boolean specialMapping;
    Hashtable specialMap;
    UrlLocalizer newLocalizer;
    
    public UrlLocalizer( String remoteUrlPath, String localDirPath ) {
        super( remoteUrlPath );
        this.localDirPath = endInSlash( localDirPath );
        specialMapping = false;
        newLocalizer = this;
    }
    
    /** necessary for case where remote url path changes, set with getLocalDir */
    public UrlLocalizer getNewLocalizer() {
        return( newLocalizer );
    }

    void addSpecialMapping( String url, String specialMapping ) {
        if (( url == null ) || ( specialMapping == null )) {
            return;
        }
        if ( specialMap == null ) {
            specialMap = new Hashtable();
        }
        specialMap.put( url, specialMapping );
    }

    /** Get the local directory used as a base for this UrlLocalizer */
    public String getLocalDirPath() {
        return( localDirPath );
    }


    public String removeComment( String urlFile ) {
        if ( urlFile.indexOf( "#" ) != -1 ) {
            urlFile = urlFile.substring( 0, urlFile.indexOf( "#" ));
        }
        return( urlFile );
    }
    
    /** Create a local file name from a relative url file name,
     *  creates in a subdirectory called "special" if url doesn't translate
     *  into a relative local name (this is done inside "getLocalDir"
     */
    public String createLocalFileName( String urlFile ) {
  		// get the local name of the file.  Assume
   		// paths are relative.  If not, just put
   		// with same name in local directory.
   		specialMapping = false;
   		urlFile = removeComment( urlFile );
   		boolean relative = (urlFile.indexOf( ':' ) == -1);
		String relativeDirName = getLocalDir( urlFile );
		String destFileName = urlFile;
	    if ( destFileName.indexOf( '/' ) != -1 ) {
	        if ( destFileName.lastIndexOf( '/' ) == ( destFileName.length() - 1 )) {
	            destFileName = "index.html";
	        } else {
    			destFileName = urlFile.substring(
   	    			urlFile.lastIndexOf( '/' ) + 1, urlFile.length() );
   	    	}
		}
		if ( relativeDirName != null ) {
			destFileName = relativeDirName + '/' + destFileName;
		} else if ( localDirPath != null ) {
            destFileName = localDirPath + destFileName;
        }
        String localName = collapsePath( destFileName );
        if (( localName != null ) && specialMapping ) {
            addSpecialMapping( urlFile, localName );
        }
//        System.out.println( "local name for '" + urlFile + "' is '" + localName + "'" );
//        System.out.println( "relativeDirName '" + relativeDirName + "', remoteSite '" + remoteSite + "'" );
//        System.out.println( "remoteUrlPath '" + remoteUrlPath + "', localDirPath '" + localDirPath + "'" );
        return( localName );
    }

    /** Get a local directory name based on a url or relative url,
     *  use a special directory if given a path that doesn't fit quite anywhere.
     */
    String getLocalDir( String completePath ) {
        // reset each call
        newLocalizer = this;
        if ( completePath.indexOf( "/" ) >= 0 ) {
	        String dirPath = completePath.substring( 0, completePath.lastIndexOf( "/" ) + 1 );
    	    // assume dirPath includes complete remoteUrlPath
	        if ( dirPath.indexOf( remoteUrlPath ) == 0 ) {
	            if ( dirPath.compareTo( remoteUrlPath ) == 0 ) {
	                return( null );
	            } else {
	                String path = dirPath.substring( remoteUrlPath.length() );
                    return( localDirPath + path );
        	    }
    	    } else if ( dirPath.indexOf( ":" ) >= 0 ) {
    	        // if the dirPath is a subset of the remoteUrlPath,
    	        // base the relativeDir on that
    	        StringTokenizer remoteTokenizer = new StringTokenizer( remoteUrlPath, "/\\" );
    	        StringTokenizer dirPathTokenizer = new StringTokenizer( dirPath, "/\\" );
    	        // first two must match
    	        int remoteTokenCount = remoteTokenizer.countTokens();
    	        int dirPathTokenCount = dirPathTokenizer.countTokens();
    	        boolean doSpecialMapping = false;
    	        if (( remoteTokenCount > 2 ) && ( dirPathTokenCount > 2 )) {
    	            for ( int i = 0; i < 2; i++ ) {
    	                String rt = remoteTokenizer.nextToken();
    	                String dt = dirPathTokenizer.nextToken();
    	                if ( rt.compareTo( dt ) != 0 ) {
    	                    doSpecialMapping = true;
    	                    break;
    	                }
    	            }
    	            if ( !doSpecialMapping ) {
    	                int sameCount = 2;
    	                String dt = null;
    	                while ( true ) {
    	                    if ( !remoteTokenizer.hasMoreTokens() ) {
    	                        break;
    	                    }
    	                    if ( !dirPathTokenizer.hasMoreTokens() ) {
    	                        dt = null;
    	                        break;
    	                    }
    	                    String rt = remoteTokenizer.nextToken();
    	                    dt = dirPathTokenizer.nextToken();
    	                    if (( rt == null ) || ( dt == null )) {
    	                        break;
    	                    }
    	                    if ( rt.compareTo( dt ) != 0 ) {
    	                        break;
    	                    }
    	                    sameCount++;
    	                }
    	                int dotCount = remoteTokenCount - sameCount;
    	                StringBuffer sb = new StringBuffer();
    	                sb.append( localDirPath );
    	                for ( int i = 0; i < dotCount; i++ ) {
    	                    sb.append( "../" );
    	                }
    	                if ( dt != null ) {
    	                    sb.append( dt );
    	                    sb.append( '/' );
    	                }
    	                while ( dirPathTokenizer.hasMoreTokens() ) {
    	                    dt = dirPathTokenizer.nextToken();
    	                    if ( dt == null ) {
    	                        break;
    	                    } else {
    	                        sb.append( dt );
    	                        sb.append( '/' );
    	                    }
    	                }
    	                String result = new String( sb );
    	                newLocalizer = new UrlLocalizer( dirPath, result );
    	                newLocalizer.setExtensionsToAccept( getExtensionsToAccept() );
    	                return( result );
    	            }
    	        }
    	        specialMapping = true;
    	        return( localDirPath + SpecialPath );
   	        } else {
   	            return( localDirPath + dirPath );
   	        }
   	    } else {
   	        return( null );
   	    }
   	}


    public Enumeration getMapList() {
        if ( specialMap != null ) {
            return( specialMap.keys() );
        } else {
            return( null );
        }
    }
    
    public String getSpecialMapping( String key ) {
        if ( specialMap != null ) {
            return( (String)specialMap.get( key ));
        } else {
            return( null );
        }
    }
}
