/*
 * @(#)UrlVisitor.java
 *
 * Copyright (c) 1998-2000 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import java.util.Vector;
import java.io.InputStream;
import com.trapezium.parse.InputStreamFactory;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.parse.TextLineParser;
import com.trapezium.parse.TextLineEnumerator;
import com.trapezium.pattern.Visitor;
import com.trapezium.util.RemoteUrlGenerator;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.ROUTE;
import com.trapezium.vrml.RouteDestination;
import com.trapezium.vrml.grammar.VRML97;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.node.PROTObase;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.ScriptFileParsed;
import com.trapezium.vrml.ScriptFunction;
import com.trapezium.util.StringUtil;
import java.util.Enumeration;

/**
 *  Finds all fields indicating a url so they can be validated.
 *           
 *  @author          Johannes N. Johannsen
 *  @version         1.21 22 August 1998, updated for url access via PROTOs
 *                   1.1, 30 Dec 1997
 *
 *  @since           1.0
 */
public class UrlVisitor extends Visitor {
    // for creating urls
    RemoteUrlGenerator remoteUrlGenerator;

	String baseFile;
	
	/** files that should be checked */
	Vector fileNames;
	
	/** files that could not be found */
	Vector filesNotFound;
	
	/** files that we aren't checking */
	Vector nocheckFiles;
	Vector protoListByFile;
	String[] bgurls;
	
	boolean originalNamesOnly;
	static public final int USE_ORIGINAL_NAMES = 0;
	static public final int CHANGE_NAMES = 1;

    
    /** class constructor */
	public UrlVisitor( String baseFile ) {
		super( null );
		init( baseFile, CHANGE_NAMES );
	}
	
	public UrlVisitor( String baseFile, int nameHandling ) {
	    super( null );
	    init( baseFile, nameHandling );
	}
	
	void init( String baseFile, int nameHandling ) {
	    if ( nameHandling == USE_ORIGINAL_NAMES ) {
	        originalNamesOnly = true;
	    } else {
    		originalNamesOnly = false;
    	}
		this.baseFile = baseFile;
		fileNames = new Vector();
		filesNotFound = new Vector();
		nocheckFiles = new Vector();
		String basePath = null;
		if ( baseFile.lastIndexOf( '/' ) > 0 ) {
			basePath = baseFile.substring( 0, baseFile.lastIndexOf( '/' ) + 1 );
		} else if ( baseFile.lastIndexOf( '\\' ) > 0 ) {
		    basePath = baseFile.substring( 0, baseFile.lastIndexOf( '\\' ) + 1 );
        }
		remoteUrlGenerator = new RemoteUrlGenerator( basePath );
		bgurls = new String[6];
		bgurls[0] = "backUrl";
		bgurls[1] = "bottomUrl";
		bgurls[2] = "frontUrl";
		bgurls[3] = "leftUrl";
		bgurls[4] = "rightUrl";
		bgurls[5] = "topUrl";
	}

    /** Get the total number of url file references found. */
	public int getNumberFiles() {
		return( fileNames.size() );
	}

    /** Get the url String at a particular offset. */
	public String getFileAt( int offset ) {
		return( (String)fileNames.elementAt( offset ));
	}
	
	/** Get the number of url references that are not wrl files */
	public int getNumberOtherFiles() {
	    return( nocheckFiles.size() );
	}
	
	/** Get the url String of an other file */
	public String getOtherFileAt( int offset ) {
	    return( (String)nocheckFiles.elementAt( offset ));
	}

    /** Visitor pattern, look for url fields, add files to fileNames list */
	public boolean visitObject( Object a ) {
		if ( a instanceof ROUTE ) {
			ROUTE r = (ROUTE)a;
			RouteDestination rd = r.getRouteDestination();
			String destObjectName = r.getDestDEFname();
			if ( destObjectName != null ) {
				Scene scene = (Scene)r.getScene();
				if ( scene != null ) {
					DEFUSENode dun = scene.getDEF( destObjectName );
					if ( dun != null ) {
						Node n = dun.getNode();
						if ( n.getBaseName().compareTo( "Script" ) == 0 ) {
							Field url = n.getField( "url" );
							if ( url != null ) {
								if ( VRML97.scriptFileParsed( n )) {
									VRML97.checkScript( n, r, rd, dataSource );
								}
							}
						}
					}
				}
			}
		} else if ( a instanceof Node ) {
			Node n = (Node)a;
			if ( n instanceof DEFUSENode ) {
				return( true );
			}
			if ( n instanceof PROTOInstance ) {
			    return( true );
			}
			FieldValue urlFieldValue = null;
			boolean externPROTO = false;
			if ( n.getBaseName().compareTo( "EXTERNPROTO" ) == 0 ) {
				urlFieldValue = n.getFieldValue();
				externPROTO = true;
			} 
			Field urlField = n.getField( "url" );
			if ( urlField != null ) {
				urlFieldValue = urlField.getFieldValue();
			}
			boolean scriptUrl = ( n.getBaseName().compareTo( "Script" ) == 0 );
			if ( scriptUrl ) {
				if ( urlFieldValue != null ) {
				    Scene s = (Scene)urlField.getScene();
				    TokenEnumerator v = s.getTokenEnumerator();
					int tokenOffset = urlField.getFirstTokenOffset();
					v.setState( tokenOffset );
					tokenOffset = v.getNextToken();
					if ( v.isLeftBracket( tokenOffset )) {
						tokenOffset = v.getNextToken();
					}
					if ( tokenOffset != -1 ) {
						String javascriptCheck = v.toString( tokenOffset );
						if ( javascriptCheck.indexOf( "javascript:" ) > 0 ) {
							return( true );
						} else if ( javascriptCheck.indexOf( "vrmlscript:" ) > 0 ) {
						    return( true );
						}
					}
				}
			}
			if ( urlFieldValue != null ) {
				checkUrl( n, urlFieldValue, scriptUrl, externPROTO );
			}
			// background is exception, has 6 url fields, none named "url"
			if ( n.getBaseName().compareTo( "Background" ) == 0 ) {
				for ( int i = 0; i < bgurls.length; i++ ) {
					urlField = n.getField( bgurls[i] );
					if ( urlField != null ) {
						urlFieldValue = urlField.getFieldValue();
						if ( urlFieldValue != null ) {
							checkUrl( n, urlFieldValue, false, false );
						}
					}
				}
			}
		}
		return( true );
	}

    
    /** Check all urls specified by a url field.
     *
     *  @param n the Node containing the url field
     *  @param urlFieldValue the MFString list of urls
     *  @param scriptUrlFlag true if the urlFieldValue is from a Script node
     *  @param externPROTO true if the urlFieldValue is from an EXTERNPROTO
     */
	public void checkUrl( Node n, FieldValue urlFieldValue, boolean scriptUrlFlag, boolean externPROTO ) {
		if (!( urlFieldValue instanceof MFFieldValue )) {
			return;
		}
		MFFieldValue fv = (MFFieldValue)urlFieldValue;
		String protoName = null;
		int numberValues = fv.numberValues();
		Scene scn = (Scene)fv.getScene();
		for ( int i = 0; i < numberValues; i++ ) {
			FieldValue sfv = fv.getFieldValueAt( i );
			
			// this can occur if the children are ISFields, which means url check
			// has to be done as part of proto instance checking
			if ( sfv == null ) {
			    break;
			}
			TokenEnumerator v = scn.getTokenEnumerator();
			int tokenOffset = sfv.getFirstTokenOffset();
			String fileName = StringUtil.stripQuotes( v.toString( tokenOffset ));
			String originalFileName = fileName;
			if (( n.getBaseName().compareTo( "Anchor" ) == 0 ) ||
				( n.getBaseName().compareTo( "EXTERNPROTO" ) == 0 )) {
				if ( externPROTO ) {
					int poundIdx = fileName.indexOf( "#" );
					if (( poundIdx > 0 ) && ( poundIdx < ( fileName.length() - 1 ))) {
						protoName = fileName.substring( poundIdx + 1, fileName.length() );
					}
				}
				fileName = StringUtil.stripPound( fileName );
				originalFileName = fileName;
				if ( fileName.length() == 0 ) {
					continue;
				}
			}
			fileName = remoteUrlGenerator.createUrlToFetch( fileName );
			if ( fileName == null ) {
			    continue;
			}
			if (( fileName.compareTo( "vrmlscript" ) == 0 ) || ( fileName.compareTo( "javascript" ) == 0 )) {
			    continue;
			}

            // originalNamesOnly control confusing... it really means
            // we are using this for remote url reference extraction
            // so we don't want to mess with error messages
			boolean fileFound = originalNamesOnly;
			try {
				if ( !originalNamesOnly && !fileNotFound( fileName ) && !fileFound( fileName ) && !nocheckFileFound( fileName )) {
					System.out.println( "checking existence of '" + fileName + "'" );
					InputStream is = InputStreamFactory.getInputStream( fileName );
					fileFound = true;
					PROTOlist pl = null;
					if (( fileName.lastIndexOf( ".class" ) > 0 ) && scriptUrlFlag ) {
						Class nodeClass = null;
						try {
							nodeClass = Class.forName( fileName.substring( 0, fileName.lastIndexOf( ".class" )));
							n.addChild( new ScriptFunction( nodeClass ));
							n.addChild( new ScriptFileParsed( -1 ));
						} catch ( Exception e ) {
						}
					} else if ( scriptUrlFlag || externPROTO ) {
						TextLineEnumerator tle = new TextLineEnumerator( new TextLineParser( is ));
						int linecount = tle.size();
						for ( int j = 0; j < linecount; j++ ) {
							String s = tle.getLineAt( j );
							if ( scriptUrlFlag ) {
								VRML97.addFunction( s, n );
							} else if ( externPROTO ) {
								if (( s.indexOf( "PROTO" ) != -1 ) && ( s.indexOf( "EXTERNPROTO" ) == -1 )) {
									if ( pl == null ) {
										pl = new PROTOlist( fileName );
										if ( protoListByFile == null ) {
											protoListByFile = new Vector();
										}
										protoListByFile.addElement( pl );
									}
									pl.addPROTOline( s );
								}
							}
						}
						if ( scriptUrlFlag ) {
							n.addChild( new ScriptFileParsed( -1 ));
						}
					}						
					is.close();
				}
			} catch ( NoClassDefFoundError xx ) {
			    if ( originalNamesOnly ) {
			        addNocheckFile( originalFileName );
			    } else {
    				addNocheckFile( fileName );
    			}
			} catch ( Exception e ) {
				if (( fileName.lastIndexOf( ".class" ) > 0 ) && scriptUrlFlag ) {
					Class nodeClass = null;
					try {
						nodeClass = Class.forName( fileName.substring( 0, fileName.lastIndexOf( ".class" )));
						n.addChild( new ScriptFunction( nodeClass ));
						n.addChild( new ScriptFileParsed( -1 ));
						fileFound = true;
					} catch ( Exception ee ) {
						filesNotFound.addElement( fileName );
					}
					if ( !fileFound ) {
						System.out.println( "unable to check '" + fileName + "'" );
					}
					fileFound = true;
				} else {
					filesNotFound.addElement( fileName );
				}
			}
			if ( fileFound ) {
				if (( n.getBaseName().compareTo( "Anchor" ) == 0 ) ||
					( n.getBaseName().compareTo( "Inline" ) == 0 ) ||
					( n.getBaseName().compareTo( "EXTERNPROTO" ) == 0 ) ||
					( n.getBaseName().compareTo( "Background" ) == 0 ) ||
					( n.getBaseName().compareTo( "ImageTexture" ) == 0 )) {
					if ( originalNamesOnly ) {
					    addFile( originalFileName );
					} else {
    					addFile( fileName );
    				}
				} else {
				    if ( originalNamesOnly ) {
				        addNocheckFile( originalFileName );
				    } else {
    					addNocheckFile( fileName );
    				}
				}
			} else if ( !fileFound( fileName ) && !nocheckFileFound( fileName )) {		
				sfv.setError( "File '" + fileName + "' not found" );
				if ( originalNamesOnly ) {
    				addNocheckFile( originalFileName );
    			} else {
    			    addNocheckFile( fileName );
    			}
			}
			if ( externPROTO && !originalNamesOnly ) {
				PROTOlist pl = getProtoList( fileName );
				if ( pl == null ) {
					n.setError( "Referenced file has no PROTOs." );
				} else {
					if ( protoName != null ) {
						if ( !pl.hasPROTO( protoName )) {
							n.setError( "Referenced file has no PROTO named \"" + protoName + "\"" );
						}
					}
				}
			}
		}
	}

	public PROTOlist getProtoList( String fileName ) {
		if ( protoListByFile == null ) {
			return( null );
		}
		int plcount = protoListByFile.size();
		for ( int i = 0; i < plcount; i++ ) {
			PROTOlist test = (PROTOlist)protoListByFile.elementAt( i );
			if ( test.getFileName().compareTo( fileName ) == 0 ) {
				return( test );		
			}
		}
		return( null );
	}

    /** add the file name to the list of file names if it isn't already there */
	public void addFile( String fileName ) {
		if ( !fileFound( fileName )) {
			fileNames.addElement( fileName );
		}
	}


    /** is the file name found in the list of fileNames? */
	boolean fileFound( String fileName ) {
		return( veccheck( fileName, fileNames ));
	}

    /** is the file found in the vector of Strings? */
	boolean veccheck( String fileName, Vector v ) {
		for ( int i = 0; i < v.size(); i++ ) {
			String test = (String)v.elementAt( i );
			if ( test.compareTo( fileName ) == 0 ) {
				return( true );
			}
		}
		return( false );
	}

    /** is the file in the list of files that were referenced but not found? */
	boolean fileNotFound( String fileName ) {
		return( veccheck( fileName, filesNotFound ));
	}

    /** is the file in the list of files that were referenced, but should not be checked? */
	boolean nocheckFileFound( String fileName ) {
		return( veccheck( fileName, nocheckFiles ));
	}

    /** Add a file to the list of files that should not be checked */
	void addNocheckFile( String fileName ) {
		if ( !nocheckFileFound( fileName )) {
			nocheckFiles.addElement( fileName );
		}
	}
}



class PROTOlist {
	String fileName;
	Vector protoLines;

	public PROTOlist( String fileName ) {
		this.fileName = fileName;
		protoLines = new Vector();
	}

	public String getFileName() {
		return( fileName );
	}

	public int getNumberPROTOlines() {
		return( protoLines.size() );
	}

	public String getPROTOlineAt( int offset ) {
		return( (String)protoLines.elementAt( offset ));
	}

	public void addPROTOline( String line ) {
		protoLines.addElement( line );
	}

	public boolean hasPROTO( String name ) {
		int n = protoLines.size();
		for ( int i = 0; i < n; i++ ) {
			String s = getPROTOlineAt( i );
			if ( s.indexOf( name ) > 0 ) {
				return( true );
			}
		}
		return( false );
	}
}
