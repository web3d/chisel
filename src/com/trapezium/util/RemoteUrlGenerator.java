/*
 * @(#)RemoteUrlGenerator.java
 *
 * Copyright (c) 1998-2000 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.util;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 *  This class is used to generate remote urls that may be specified relative
 *  to another url.
 */
public class RemoteUrlGenerator {
    // base starting point
    String remoteUrlPath;
    
    // site necessary for site level filtering
    String remoteSite;

    // filtering options:  directoy filtering & site filtering
    static public final int NO_FILTERING = 0;
    static public final int DIRECTORY_FILTERING = 1;
    static public final int SITE_FILTERING = 2;
    int filterType;
    
    Vector extensionsToAccept;
    
    public void addExtensionAcceptor( String s ) {
        if ( extensionsToAccept == null ) {
            extensionsToAccept = new Vector();
        }
        extensionsToAccept.addElement( s );
    }
    
    public Vector getExtensionsToAccept() {
        return( extensionsToAccept );
    }
    
    public void setExtensionsToAccept( Vector v ) {
        extensionsToAccept = v;
    }

    public RemoteUrlGenerator( String remoteUrlPath ) {
        this.remoteUrlPath = endInSlash( remoteUrlPath );
        filterType = DIRECTORY_FILTERING;
        if ( remoteUrlPath != null ) {
            StringTokenizer st = new StringTokenizer( remoteUrlPath, "/" );
            if ( st.countTokens() > 1 ) {
                st.nextToken();
                remoteSite = st.nextToken();
            }
        }
    }

    /** make sure a path ends in a slash */
    protected String endInSlash( String path ) {
        if ( path != null ) {
            if ( path.lastIndexOf( '/' ) == ( path.length() - 1 )) {
                return( path );
            } else if ( path.lastIndexOf( '\\' ) == ( path.length() - 1 )) {
                return( path );
            } else {
                path = path + "/";
            }
        } else { 
            path = new String( "./" );
        }
        return( path );
    }

    public String getRemoteUrlPath() {
        return( remoteUrlPath );
    }

    /** Create a remote url to fetch given a url that may be relative to
     *  the remoteUrlPath (it may also be a completely specified url).
     *  Filters out urls that are not part of the remoteUrlPath.
     *
     *  @param remoteRelativeUrl a url extracted from an html or wrl file,
     *     which may be relative to the remoteUrlPath
     */
    public String createUrlToFetch( String remoteRelativeUrl ) {
        if ( remoteRelativeUrl.lastIndexOf( '/' ) == ( remoteRelativeUrl.length() - 1 )) {
            remoteRelativeUrl = remoteRelativeUrl + "index.html";
        }
        if ( remoteRelativeUrl.indexOf( "http:" ) >= 0 ) {
            return( validatePath( remoteRelativeUrl ));
        } else if ( remoteRelativeUrl.indexOf( ":" ) >= 0 ) {
            return( null );
        } else {
//            System.out.println( "generate remote url for " + remoteRelativeUrl );
            String src = createSourceFileName( remoteRelativeUrl );
//            System.out.println( "result is " + src );
            if ( src != null ) {
                src = collapsePath( src );
//                System.out.println( "collapsed is " + src );
                if ( src != null ) {
                    src = validatePath( src );
//                    System.out.println( "valid is " + src );
                }
            }
            return( src );
        }
    }

    /** Creates a source file name */
    String createSourceFileName( String src ) {
   	    if ( src.indexOf( ":" ) == -1 ) {
   	        src = remoteUrlPath + src;
   	    }
   	    if ( src.lastIndexOf( '/' ) == ( src.length() - 1 )) {
   	        src = src + "index.html";
   	    }
   	    return( src );
   	}

    /** Get rid of ".." and "//" in paths, "//" OK only after http: */
    protected String collapsePath( String src ) {
        StringTokenizer st = new StringTokenizer( src, "/\\" );
        int numberTokens = st.countTokens();
        if ( numberTokens > 0 ) {
            boolean[] keep = new boolean[ numberTokens ];
            int idx = 0;
            boolean modifyPath = false;
            while ( st.hasMoreTokens() ) {
                String tok = st.nextToken();
                if ( tok.compareTo( ".." ) == 0 ) {
                    keep[ idx ] = false;
                    modifyPath = true;
                } else {
                    keep[ idx ] = true;
                }
                idx++;
            }
            if ( modifyPath ) {
                int kcount = 0;
                int firstToSkip = -1;
                for ( int i = 0; i < numberTokens; i++ ) {
                    if ( !keep[i] ) {
                        if ( firstToSkip == -1 ) {
                            firstToSkip = i;
                        }
                        kcount++;
                        // <2 means we are wiping out http or site
                        if (( firstToSkip - kcount ) < 2 ) {
                            return( null );
                        }
                        keep[ firstToSkip - kcount ] = false;
                    } else if ( firstToSkip != -1 ) {
                        break;
                    }
               }

               StringTokenizer st2 = new StringTokenizer( src, "/\\" );
               idx = 0;
               StringBuffer result = new StringBuffer();
               boolean doSlash = false;
               boolean secondSlash = false;
               while ( st2.hasMoreTokens() ) {
                   String tok = st2.nextToken();
                   if ( keep[ idx ] ) {
                       if ( doSlash ) {
                           result.append( "/" );
                           if ( secondSlash ) {
                            result.append( "/" );
                           }
                           secondSlash = false;
                       }
                       result.append( tok );
                       doSlash = true;
                       if ( tok.compareTo( "http:" ) == 0 ) {
                        secondSlash = true;
                       }
                   }
                   idx++;
               }
               String s = collapseSlash( new String( result ));
               return( s );
           }
       }
       return( collapseSlash( src ));
   }

   protected String collapseSlash( String s ) {
    int startIndex = 0;
    if ( s.indexOf( "http:" ) >= 0 ) {
        startIndex = 6;
    }
    StringBuffer sb = new StringBuffer();
    boolean slash = false;
    for ( int i = 0; i < s.length(); i++ ) {
        if ( i < startIndex ) {
            sb.append( s.charAt( i ));
        } else {
            char x = s.charAt( i );
            if (( x == '/' ) || ( x == '\\' )) {
                if ( !slash ) {
                    sb.append( x );
                }
                slash = true;
            } else {
                slash = false;
                sb.append( x );
            }
        }
    }
    return( new String( sb ));
   }

    /** Set filtering to either DIRECTORY_FILTERING or SITE_FILTERING */
    public void setFilterType( int filterType ) {
        this.filterType = filterType;
    }

    String makeFwdSlash( String s ) {
        if ( s.indexOf( '\\' ) != -1 ) {
            StringBuffer sb = new StringBuffer();
            int len = s.length();
            for ( int i = 0; i < len; i++ ) {
                if ( s.charAt( i ) == '\\' ) {
                    sb.append( '/' );
                } else {
                    sb.append( s.charAt( i ));
                }
            }
            return( new String( sb ));
        }
        return( s );
    }
    
    boolean sourceStartsWithRemotePath( String src, String remotePath ) {
        while ( src.indexOf( remotePath ) == -1 ) {
//            System.out.println( "failed, remotePath is '" + remotePath + "'" );
            int firstSlash = remotePath.indexOf( '/' );
            int lastSlash = remotePath.lastIndexOf( '/' );
            if ( firstSlash == lastSlash ) {
                return( false );
            }
            if ( firstSlash == -1 ) {
                return( false );
            }
            remotePath = remotePath.substring( lastSlash );
        }
        return( true );
    }
            
    /** Get acceptable path, depending on <B>filterType</B>.
     *  If we are doing SITE_FILTERING, any path with the same site is acceptable.
     *  If we are doing DIRECTORY_FILTERING, any path with the same path as
     *  passed to this object's constructor is acceptable.
     *  If there are any specific extensions to avoid, these are filtered out.
     *  If there are any specific extensions to accept, these are allowed.
     *
     *  @param  srcPath the path to validate
     *  @return srcPath if it is acceptable, null if it is not acceptable.
     */
     boolean dbg = false;
    String validatePath( String src ) {
        if ( src == null ) {
            return( null );
        }

        boolean acceptable = true;
        if ( extensionsToAccept != null ) {
            String lower = src.toLowerCase();
            int numberExtensionsToAccept = extensionsToAccept.size();
            if ( numberExtensionsToAccept > 0 ) {
                acceptable = false;
            }
            for ( int i = 0; i < numberExtensionsToAccept; i++ ) {
                String ext = (String)extensionsToAccept.elementAt( i );
                if ( lower.indexOf( ext ) > 0 ) {
                    acceptable = true;
                    break;
                }
            }
        }
        if ( !acceptable ) {
            if ( dbg ) System.out.println( "Failure 1" );
            return( null );
        }
        if ( filterType == NO_FILTERING ) {
            return( src );
        } else if ( filterType == SITE_FILTERING ) {
            if ( remoteSite != null ) {
                StringTokenizer st = new StringTokenizer( src, "/" );
                if ( st.countTokens() > 1 ) {
                    st.nextToken();
                    String srcSite = st.nextToken();
                    if ( srcSite.compareTo( remoteSite ) == 0 ) {
                        return( src );
                    }
                }
            }
            if ( dbg ) System.out.println( "Failure 2" );
            return( null );
        } else if ( filterType == DIRECTORY_FILTERING ) {
            if ( src == null ) {
                if ( dbg ) System.out.println( "Failure 3" );
                return( null );
            } else if ( remoteUrlPath.compareTo( "./" ) == 0 ) {
                // this means we are at the current unspecified directory
                // so we disallow a path with ".." or an explicit path with ":"
                if ( src.indexOf( ".." ) >= 0 ) {
                    if ( dbg ) System.out.println( "Failure 4" );
                    return( null );
                } else if ( src.indexOf( ":" ) >= 0 ) {
                    if ( dbg ) System.out.println( "Failure 5" );
                    return( null );
                } else {
                    return( src );
                }
            }
            String fwdSlashSrc = makeFwdSlash( src );
            String fwdSlashRemoteUrlPath = makeFwdSlash( remoteUrlPath );
            if ( !sourceStartsWithRemotePath( fwdSlashSrc, fwdSlashRemoteUrlPath )) {          
                if ( dbg ) System.out.println( "Failure 6, src '" + fwdSlashSrc + "', remoteUrlPath '" + fwdSlashRemoteUrlPath + "'" );
                return( null );
            } else {
                return( src );
            }
        } else {
            if ( dbg ) System.out.println( "Failure 7" );
            return( null );
        }
    }
}
