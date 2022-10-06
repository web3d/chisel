/*
 * @(#)ProcessedFile.java
 *
 * Copyright (c) 1998-2000 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 */
package com.trapezium.chisel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.*;

import com.trapezium.factory.FactoryData;
import com.trapezium.factory.FactoryChain;
import com.trapezium.factory.QueuedRequestFactory;
import com.trapezium.factory.TokenStreamFactory;
import com.trapezium.factory.FactoryResponseListener;
import com.trapezium.factory.ParserFactory;
import com.trapezium.util.GlobalProgressIndicator;
import com.trapezium.util.ProgressIndicator;
import com.trapezium.vrml.*;
import com.trapezium.vrml.grammar.*;
import com.trapezium.edit.TokenEditor;
import com.trapezium.edit.EditLintVisitor;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.chisel.gui.ChiselController;
import com.trapezium.html.HTMLgenerator;
import com.trapezium.vrml.visitor.X3dWriter;

import com.trapezium.edit.Document;

/** a ProcessedFile object is a FactoryData object that knows how to load
    itself, reload itself from its original source, and save itself. */
public class ProcessedFile extends FactoryData implements Serializable {

    /** URL for the source; null if source is a local file */
    //URL url;

    /** file to save to; if the source is a remote URL, this is a locally cached
        copy of the retrieved file.  If the source is a local URL, then this
        File is just the URL opened as a file */
    //File file;

    /** way to create the data given the source */
    FactoryChain loader;

	// for the moment, ParserFactory is not thread-safe, only have one for entire system
    QueuedRequestFactory parser;
    QueuedRequestFactory tokenFactory;
    Session session;

    public void wipeout() {
        super.wipeout();
        if ( parser != null ) {
            parser.wipeout();
            parser = null;
        }
        if ( tokenFactory != null ) {
            tokenFactory.wipeout();
            tokenFactory = null;
        }
        if ( loader != null ) {
            loader.wipeout();
            loader = null;
        }
        session = null;
    }
    
	public QueuedRequestFactory getParserFactory() {
		return( parser );
	}

	public QueuedRequestFactory getTokenFactory() {
	    return( tokenFactory );
	}

    public ProcessedFile() {
        this(null);
    }

	/*  create a ProcessedFile, load it and parse it */
	static int id = 1;
	int myId;

	boolean newlyOpened;
	public boolean isNewlyOpened() {
	    return( newlyOpened );
	}

	public void notNewlyOpened() {
	    newlyOpened = false;
	}


	int versionNumber;
    public ProcessedFile(String source) {
        myId = id;
        newlyOpened = true;
        id++;
        versionNumber = 1;
        //System.out.println( "Creating processedFile " + myId );
        setUrl(source);
        loader = new FactoryChain( ChiselSet.VALIDATORS );
        tokenFactory = new TokenStreamFactory();
        parser = new ParserFactory();
        loader.addFactory(tokenFactory);
        loader.addFactory(parser);
    }

    /** Get the version number for the file */
    public int getVersion() {
        return( versionNumber );
    }

    /** Get the version number as a String */
    public String getVersionString() {
        return( "v" + versionNumber + ":  " );
    }

    /** Set the version number for the file */
    public void setVersion( int versionNo ) {
        versionNumber = versionNo;
        markVersion( versionNumber );
        Scene scene = getScene();
        if ( scene != null ) {
            GlobalProgressIndicator.setProgressIndicator( ChiselSet.getProgressIndicator( ChiselSet.VALIDATORS ), "Serializing...", scene.getVrmlElementCount() );
            getDocument().setViewerVersion( versionNo, scene );
        }
    }

    public void initProgressIndicator( String title ) {
        Scene scene = getScene();
        GlobalProgressIndicator.setProgressIndicator( ChiselSet.getProgressIndicator( ChiselSet.VALIDATORS ), title, scene.getVrmlElementCount() );
    }


    /** Update a single listener text field, sets the text and checks box off.
     *
     *  @param count count used to update
     *  @param strheader  String identifying type of data
     *  @param frl FactoryResponseListener, where update is sent
     *  @param testProfile text is appended here for progress check
     *
     *  @return true if the file is not totally clean, otherwise false
     */
    boolean updateListener( int count, String strheader, FactoryResponseListener frl, StringBuffer testProfile ) {
        String txt = count + strheader;
	    frl.setText( txt );
	    boolean flag = (count > 0);
	    if ( frl instanceof ChiselRow ) {
	        ChiselRow cr = (ChiselRow)frl;
	        if ( cr.isAutomaticallyChecked() ) {
    	        setRowInfo(cr, flag);
    	    }
	    }
	    if ( testProfile != null ) {
    	    testProfile.append( txt );
    	}
	    return( flag );
	}

    /** Update the summary info in all the chisel rows.
     *
     *  @param cleanCount number of times file cleaned, some chisels automatically updated
     *     only on first pass through (where cleanCount==0)
     *
     *  @return true if the file is not totally cleaned and the info profile has
     *     changed.  The info profile is necessary to prevent useless repeated
     *     attempts to clean a file, which cannot be cleaned for whatever reason.
     */
    String infoProfile = null;
    int cleanCount = 0;
    public void setCleanCount( int ccval ) {
        cleanCount = ccval;
    }
    public int getCleanCount() {
        return( cleanCount );
    }
    public boolean updateInfo() {
        boolean needSignal = false;
        StringBuffer testProfile = new StringBuffer();
        EditLintVisitor lv = getLintInfo();
        if ( lv != null ) {
            FactoryResponseListener frl = ChiselSet.getSpecializedListener( ChiselSet.InlineCountListener );
    		if ( frl != null ) {
    		    frl.setText( lv.getInlineCount() + " inline" );
    		}

    		// unused DEF's don't set the signal
    		frl = ChiselSet.getSpecializedListener( ChiselSet.UnusedDEFCountListener );
    		if ( frl != null ) {
    		    frl.setText( lv.getUnusedDEFCount() + " unused" );
 /*   		    boolean flag = (lv.getUnusedDEFCount() > 0);
				// turn it off it was on
    		    if ( frl instanceof ChiselRow ) {
    		        setRowInfo((ChiselRow)frl, flag);
    		    }*/
    		}

    		frl = ChiselSet.getSpecializedListener( ChiselSet.DefaultFieldCountListener );
    		if ( frl != null ) {
    		    needSignal = updateListener( lv.getDefaultFieldCount(), " default", frl, testProfile ) || needSignal;
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.UnusedCoordCountListener );
    		FactoryResponseListener unusedListener = frl;
    		if ( frl != null ) {
    		    needSignal = updateListener( lv.getUnusedCoordCount(), " unused", frl, testProfile ) || needSignal;
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.UnusedPROTOinterfaceCountListener );
    		if ( frl != null ) {
    		    updateListener( lv.getUnusedPROTOinterfaceCount(), " unused", frl, testProfile );
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.DuplicateFieldCountListener );
    		if ( frl != null ) {
    		    needSignal = updateListener( lv.getDuplicateFieldCount(), " repeated", frl, testProfile ) || needSignal;
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.SingleColorIFScountListener );
    		if ( frl != null ) {
    		    needSignal = updateListener( lv.getSingleColorIFScount(), " single colored IFS", frl, testProfile ) || needSignal;
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.EmptyIndexedFaceSetCountListener );
    		if ( frl != null ) {
    		    needSignal = updateListener( lv.getEmptyIndexedFaceSetCount(), " empty", frl, testProfile ) || needSignal;
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.ElevationGridCountListener );
    		if ( frl != null ) {
    		    if ( lv.getElevationGridCount() == 1 ) {
    		        frl.setText( "1 grid" );
    		    } else {
        		    frl.setText( lv.getElevationGridCount() + " grids" );
        		}
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.TransformCountListener );
    		if ( frl != null ) {
    		    if ( lv.getTransformCount() == 1 ) {
    		        frl.setText( "1 transform" );
    		    } else {
        		    frl.setText( lv.getTransformCount() + " transforms" );
        		}
    		}
     		frl = ChiselSet.getSpecializedListener( ChiselSet.DEFUSECountListener );
    		if ( frl != null ) {
    		    int defcount = lv.getDEFcount();
    		    int usecount = lv.getUSEcount();
    		    frl.setText( defcount + " DEF, " + usecount + " USE" );
    		}
     		frl = ChiselSet.getSpecializedListener( ChiselSet.DEFUSECountListener2 );
    		if ( frl != null ) {
    		    int defcount = lv.getDEFcount();
    		    int usecount = lv.getUSEcount();
    		    frl.setText( defcount + " DEF, " + usecount + " USE" );
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.DupIndexCountListener );
    		if ( frl != null ) {
    		    needSignal = updateListener( lv.getDupIndexCount(), " repeated", frl, testProfile ) || needSignal;
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.BadFaceCountListener );
    		if ( frl != null ) {
    		    needSignal = updateListener( lv.getBadFaceCount(), " bad", frl, testProfile ) || needSignal;
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.RepeatedValueCountListener );
    		if ( frl != null ) {
    		    boolean flag = updateListener( lv.getRepeatedValueCount(), " repeated", frl, testProfile );
    		    if ( frl instanceof ChiselRow ) {
    		        if ( flag ) {
    		            setRowInfo((ChiselRow)unusedListener, flag );
    		        }
    		    }
   		        needSignal = needSignal || flag;
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.BadRouteListener );
    		if ( frl != null ) {
    		    updateListener( lv.getBadRouteCount(), " bad", frl, null );
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.NormalCountListener );
    		if ( frl != null ) {
    		    updateListener( lv.getNormalCount(), " normal", frl, null );
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.UnnecessaryKeyValueListener );
    		if ( frl != null ) {
    		    if ( cleanCount == 0 ) {
        		    needSignal = updateListener( lv.getUnnecessaryKeyValueCount(), " unnecessary", frl, testProfile ) || needSignal;
        		} else {
        		    updateListener( lv.getUnnecessaryKeyValueCount(), " unnecessary", frl, testProfile );
        		    if ( frl instanceof ChiselRow ) {
        		        setRowInfo( (ChiselRow)frl, false );
        		    }
        		}
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.UnindexedValueListener );
    		if ( frl != null ) {
    		    updateListener( lv.getUnindexedValueCount(), " unindexed values", frl, null );
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.InterpolatorCountListener );
    		if ( frl != null ) {
    		    if ( lv.getInterpolatorCount() == 1 ) {
    		        frl.setText( "1 interpolator" );
    		    } else {
    		        frl.setText( lv.getInterpolatorCount() + " interpolators" );
    		    }
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.InterpolatorCountListener2 );
    		if ( frl != null ) {
    		    if ( lv.getInterpolatorCount() == 1 ) {
    		        frl.setText( "1 interpolator" );
    		    } else {
    		        frl.setText( lv.getInterpolatorCount() + " interpolators" );
    		    }
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.UselessNodeCountListener );
    		if ( frl != null ) {
    		    needSignal = updateListener( lv.getUselessNodeCount(), " useless", frl, testProfile ) || needSignal;
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.ValueNodeCountListener );
    		if ( frl != null ) {
    		    if ( lv.getValueNodeCount() == 1 ) {
    		        frl.setText( "1 value node" );
    		    } else {
    		        frl.setText( lv.getValueNodeCount() + " value nodes" );
    		    }
    		}
    		frl = ChiselSet.getSpecializedListener( ChiselSet.PROTOInstanceCountListener );
    		if ( frl != null ) {
    		    if ( lv.getPROTOInstanceCount() == 1 ) {
    		        frl.setText( "1 PROTO instance" );
    		    } else {
    		        frl.setText( lv.getPROTOInstanceCount() + " PROTO instances" );
    		    }
    		}
        }
        if ( infoProfile == null ) {
            infoProfile = new String( testProfile );
        } else {
            String infoTestProfile = new String( testProfile );
            if ( infoTestProfile.compareTo( infoProfile ) == 0 ) {
                needSignal = false;
            } else {
                infoProfile = infoTestProfile;
            }
        }
        return( needSignal );
	}

	void setRowInfo(ChiselRow cr, boolean flag) {
        cr.rowReady();
        ChiselController cc = cr.getChiselController();
        cr.setEnabled( flag );
        cc.setValue( String.valueOf( flag ));
        cc.repaint();
    }

    /** DocumentLoader interface */
    public void reload( Object scene ) {
        System.out.println( "Reloading scene..." );
        if ( scene instanceof Scene ) {
            Scene s = (Scene)scene;
            TokenEnumerator te = s.getTokenEnumerator();
            if ( te instanceof TokenEditor ) {
                getDocument().setLines( (TokenEditor)te );
            }
            setScene( s );
            cleanCount = 0;
            updateInfo();
        }
    }

    FactoryResponseListener parsingListener = null;
    public void setParsingListener( FactoryResponseListener f ) {
        parsingListener = f;
        tokenFactory.setListener( f );
        parser.addListener( f );
    }

    public void mergeErrors( BitSet errorMarks, BitSet warningMarks, BitSet nonconformanceMarks ) {
        mergeLintErrors( parsingListener, errorMarks, warningMarks, nonconformanceMarks );
    }

    /** Set who is going to be called when each stage of the loader is done */
    public void setDoneListener( FactoryResponseListener f ) {
        //loader.setListener( f );
        loader.addListener( f );    // should be safe, since it rejects duplicates
    }


    //
    //  The ProcessedFile FactoryChain consists of a large number of chisels, which
    //  may be independently activated/deactivated via the GUI, or may be deactivated
    //  if there are validation errors.  The latter case means that we do not allow
    //  chiselling of files which contain errors.
    //
    //  The "enableChisel" method turns on chisels by name, based on the
    //  name associated with the row visualization.
    //
    public void enableChisel( String chiselName, FactoryResponseListener theListener ) {
        loader.enableChisel( chiselName, theListener );
    }

    public void disableChisel( String chiselName ) {
        loader.disableChisel( chiselName );
    }

    public void removeFactory( String factoryName ) {
        loader.removeFactory( factoryName );
    }

    public int getId() {
        return( myId );
    }

    /** Generate local name for file */
    public String generateName() {
        return( generateName( false ));
    }

    /** Generate a name for file.
     *
     *  @param stripNumber strip off "_<N>" portion of name.
     */
    public String generateName( boolean stripNumber ) {
        File file = getFile();
        if ( file != null ) {
            String name = file.getName();
            if (( saveAsCounter > 1 ) && stripNumber ) {
                int underscoreIdx = name.lastIndexOf( "_" );
                int dotIdx = name.lastIndexOf( "." );
                if (( underscoreIdx != -1 ) && ( dotIdx != -1 ) && ( underscoreIdx < dotIdx )) {
                    String beforeUnderscore = name.substring( 0, underscoreIdx );
                    String dotAndAfter = name.substring( dotIdx );
                    return( beforeUnderscore + dotAndAfter );
                }
            }
            return( name );
        } else {
            return( "unknown.wrl" );
        }
    }
    
    public String generateX3dName() {
        File file = getFile();
        if ( file != null ) {
            String name = file.getName();
            int dotIdx = name.lastIndexOf( "." );
            if ( dotIdx != -1 ) {
                String beforeDot = name.substring( 0, dotIdx );
                return( beforeDot + ".x3d" );
            } else {
                return( name + ".x3d" );
            }
        }
        return( "unknown.x3d" );
    }

    public String generateChiseledName() {
        File file = getFile();
        if ( file != null ) {
            String name = file.getName();
            int dotIdx = name.lastIndexOf( "." );
            if ( dotIdx != -1 ) {
                String beforeDot = name.substring( 0, dotIdx );
                return( beforeDot + ".chiseled.wrl" );
            } else {
                return( name + ".chiseled.wrl" );
            }
        } else {
            return( "unknown.chiseled.wrl" );
        }
    }

    public String getLocalName() {
        File file = getFile();
        if (file != null) {
            return file.getName();
        } else {
            return null;
        }
    }

    /** generate a name for "save as", just the file name, with an appended
     *  save as counter (local to the processed file).
     */
    int saveAsCounter = 1;
    public String generateSaveAsName() {
        String name = generateName( true );
        if ( name.indexOf( "." ) == -1 ) {
            name = name + ".wrl";
        } else {
            if (!name.endsWith(".wrl")) {
            name = name + ".wrl";
        }
        }
        String beforeDot = name.substring( 0, name.lastIndexOf( "." ));
        String dotAndAfter = name.substring( name.lastIndexOf( "." ));
        saveAsCounter++;
        String newName = beforeDot + "_" + saveAsCounter + dotAndAfter;
        return( newName );
    }
    int X3DsaveAsCounter = 1;
    public String generateX3dSaveAsName() {
        String name = generateName( true );
        if (!name.endsWith(".wrl") && !name.endsWith(".x3d")) {
            name = name + ".x3d";
        } else { // replace suffix with .x3d
            name = name.substring( 0, name.lastIndexOf( "." )) + ".x3d";
        }
        String beforeDot = name.substring( 0, name.lastIndexOf( "." ));
        String dotAndAfter = name.substring( name.lastIndexOf( "." ));
        X3DsaveAsCounter++;
        String newName = beforeDot + "_" + X3DsaveAsCounter + dotAndAfter;
        return( newName );
    }

    /** generate a name for gzip, name.chiseled.wrz
     */
    public String generateGzipName() {
        return( generateGzipName( false ));
    }

    /** Generate a name for gzip, name.chiseled.wrz.
     *
     *  @param stripNumber true if the "_<N>" part of the name is to be stripped
     *     out.
     */
    public String generateGzipName( boolean stripNumber ) {
        String name = getLocalName();
        if ( name == null ) {
            name = "unknown.wrz";
        }
        if ( name.indexOf( "." ) == -1 ) {
            name = name + ".wrz";
        }
        String beforeDot = name.substring( 0, name.indexOf( "." ));
        if ( stripNumber && ( saveAsCounter > 1 )) {
            if ( beforeDot.indexOf( "_" ) > 0 ) {
                beforeDot = beforeDot.substring( 0, beforeDot.lastIndexOf( "_" ));
            }
        }
        String newName = beforeDot + ".chiseled.wrz";
        return( newName );
    }

    /** generate a name for gzip save as, name_<N>.chiseled.wrz
     */
    public String generateGzipSaveAsName() {
        String name = generateGzipName( true );
        String beforeChiseled = name.substring( 0, name.indexOf( ".chiseled" ));
        saveAsCounter++;
        String newName = beforeChiseled + "_" + saveAsCounter + ".chiseled.wrz";
        return( newName );
    }

    
    public void addFactory( QueuedRequestFactory f ) {
		loader.addFactory( f );
	}

	public void addOptimizer( Optimizer o ) {
	    loader.addOptimizer( o, parser, getBaseFilePath(), getNameWithoutPath() );
	}

    public void addFactory( QueuedRequestFactory f, QueuedRequestFactory prev ) {
		loader.insertFactory( f, prev );
		//load();
	}

	public void insertFactory( QueuedRequestFactory f ) {
	    loader.insertFactory( f, tokenFactory );
	}

    public String getBaseFilePath() {
        String name = getUrl();
        if ( name != null ) {
            if ( name.lastIndexOf( '/' ) != -1 ) {
                name = name.substring( 0, name.lastIndexOf( '/' ));
            } else if ( name.lastIndexOf( '\\' ) != -1 ) {
                name = name.substring( 0, name.lastIndexOf( '\\' ));
            } else {
                name = null;
            }
        }
        //System.out.println( "ProcessedFile base name is '" + name + "'" );
        return( name );

    }
	public String getNameWithoutPath() {
		String name = getUrl();
		if ( name != null ) {
			if ( name.lastIndexOf( '/' ) != -1 ) {
				return( name.substring( name.lastIndexOf( '/' ) + 1 ));
			} else if ( name.lastIndexOf( '\\' ) != -1 ) {
				return( name.substring( name.lastIndexOf( '\\' ) + 1 ));
			} else {
				return( name );
			}
		}
		return( null );
	}

    public String getNameWithoutVersion() {
        String name = getUrl();
        File file = getFile();
        if (file != null) {
            name = file.getName();
        }
        return name;
    }

    public String getName() {
        String name = getNameWithoutVersion();
        return getVersionString() + name;
    }

    Document doc;
    MenuItem undoItem;
    MenuItem redoItem;
    public void setUndoItem( MenuItem undoItem ) {
        this.undoItem = undoItem;
        undoItem.setEnabled( false );
    }

    public void setRedoItem( MenuItem redoItem ) {
        this.redoItem = redoItem;
        redoItem.setEnabled( false );
    }
    public void clear_undo() {
        if ( doc != null ) {
            doc.clear_undo();
        }
    }

    /** Remove all resialized files */
    public void removeSerialFiles() {
        if ( doc != null ) {
            doc.removeSerialFiles( versionNumber );
        }
    }

    /** Get the Document associated with this ProcessedFile */
    public Document getDocument() {
        return( doc );
    }

    /** Set the Document associated with this ProcessedFile */
    public void setDocument( Document doc ) {
        this.doc = doc;
        if ( doc != null ) {
            doc.setUndoItem( undoItem );
            doc.setRedoItem( redoItem );
            doc.setNodeSelector( getScene() );
        }
    }

	/** kick off the loader chain of processing */
	Date startTime;
    public void load() {
        load( true );
    }

    /** Start running loader factories.
     *
     *  @param resetStartTime if true, "startTime" is set to current time
     */
    void load( boolean resetStartTime ) {
        if ( resetStartTime ) {
            startTime = new Date();
        }
        loader.submit(this);
    }

    public Date getStartTime() {
        return( startTime );
    }

    /** Save the file in text format
     */
	public void asciiSave() {
	    asciiSave( getFile(), false, false );
	}

    /** Save the file in gzip format
     */
	public void gzipSave() {
		asciiSave( getFile(), true, false );
	}
	
	public void x3dSave() {
	    asciiSave( getFile(), false, true );
	}


    class SaveThread extends Thread {
        boolean gzip;
        boolean x3d;
        int numberLines;
        PrintStream out;
        TokenEditor sceneTokenEditor;

        SaveThread( TokenEditor sceneTokenEditor, boolean gzip, boolean x3d, int numberLines, PrintStream out ) {
            this.sceneTokenEditor = sceneTokenEditor;
            this.gzip = gzip;
            this.x3d = x3d;
            this.numberLines = numberLines;
            this.out = out;
        }

        public void run() {
       		Properties props = ChiselProperties.getProperties();
            String unixFormatProperty = props.getProperty("workspace.saveInUnixFormat");
            boolean saveInUnixFormat = (unixFormatProperty == null) ? false : ("true".equalsIgnoreCase(unixFormatProperty));
            if ( x3d ) {
                Scene s = getScene();
                X3dWriter x3dw = new X3dWriter( out, sceneTokenEditor );
                s.twoPassTraverse( x3dw );
                x3dw.finished();
            } else if ( gzip ) {
                for ( int i = 0; i < numberLines; i++ ) {
                    GlobalProgressIndicator.markProgress();
                    if ( saveInUnixFormat ) {
                        out.print( sceneTokenEditor.getNospaceLineAt( i ));
                        out.print( '\n' );
                    } else {
                        out.println( sceneTokenEditor.getNospaceLineAt( i ));
                    }
                }
            } else {
                for ( int i = 0; i < numberLines; i++ ) {
                    GlobalProgressIndicator.markProgress();
                    if ( saveInUnixFormat ) {
                        out.print( sceneTokenEditor.getTabLineAt( i ));
                        out.print( '\n' );
                    } else {
                        out.println( sceneTokenEditor.getTabLineAt( i ));
                    }
                }
            }
            out.flush();
            out.close();
            System.out.println( "Finished saving file, saved " + numberLines + " lines" );
            GlobalProgressIndicator.reset();
            if ( Chisel.singletonChisel != null ) {
                Chisel.singletonChisel.updateHeaderLine();
            }
            saving = false;
        }
    }

    boolean saving;
    SaveThread saver;

    /** Save the file in text format
     *
     *  @param file file to save it to
     *  @param gzip if true, save in gzip format
     *  @param x3d if true, save in experimental X3d format 
     */
    public void asciiSave( File file, boolean gzip, boolean x3d ) {
        if ( saving ) {
            return;
        }
        saving = true;
        setGzip( gzip );
        Scene scene = getScene();
        System.out.println( "Saving file..." );
        try {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStream fo = fos;
            if ( gzip ) {
                fo = new GZIPOutputStream( fo );
            }
            PrintStream out = new PrintStream( fo );
            TokenEditor sceneTokenEditor = (TokenEditor) scene.getTokenEnumerator();
            int numberLines = sceneTokenEditor.getNumberLines();
            String progressStr = null;
            if ( gzip ) {
                progressStr = "GZIP saving " + file.getName() + " ";
            } else {
                progressStr = "Saving " + file.getName() + " ";
            }
            GlobalProgressIndicator.setProgressIndicator( ChiselSet.getProgressIndicator( ChiselSet.VALIDATORS ), progressStr, numberLines );
            SaveThread saveThread = new SaveThread( sceneTokenEditor, gzip, x3d, numberLines, out );
            saveThread.start();
            saver = saveThread;
        } catch( Exception e ) {
            e.printStackTrace();
            saving = false;
        }
    }

    public void waitForSave() {
        if ( saver != null ) {
            try {
                saver.join();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }

    public void synchTokenEditor() {
        Scene scene = getScene();
        TokenEditor sceneTokenEditor = (TokenEditor)scene.getTokenEnumerator();
        setTokenEditor( sceneTokenEditor );
        if ( sceneTokenEditor.isDirty() ) {
            sceneTokenEditor.retokenize();
            Scene newScene = new Scene( scene.getUrl(), sceneTokenEditor );
            VRML97parser parser = new VRML97parser();
            parser.Build( sceneTokenEditor, newScene );
            setScene( newScene );
        }
    }

    void dump( String loc ) {
        Scene s = getScene();
        if ( s != null ) {
            TokenEnumerator te = s.getTokenEnumerator();
            if ( te != null ) {
                System.out.println( loc + ", have " + te.getNumberLines() + " lines" );
            } else {
                System.out.println( loc + ", token enumerator is null" );
            }
        } else {
            System.out.println( loc + ", scene is null" );
        }
    }

	/** store info about which chisel is operating on this file
	 *
	 *  @param chisel the chisel that is going to operate on the file
	 */
	public void chiselWith( Optimizer chisel )  {
	    if ( session == null ) {
	        session = new Session();
	    }
	    session.chiselWith( chisel );
	}

	/** Save the current version in the session object */
	void markVersion( int version ) {
	    if ( session == null ) {
	        session = new Session();
	    }
	    session.markVersion( version );
	}

	/** Save the current percent reduction in the session object */
	public void markPercent() {
	    if ( session == null ) {
	        session = new Session();
	    }
	    session.markPercent( getPercentX100() );
	}

	/** Generate an HTML report for the chisel session.
	 *
	 *  @param htmlFileName destination of the HTML report
	 *  @param wrlFileName name of the file that was chiseled
	 *  @param originalName original name of the file
	 */
	public void generateHtml( String htmlFileName, String wrlFileName, String originalName ) {
	    if ( session == null ) {
	        session = new Session();
	    }
	    session.generateHtml( htmlFileName, wrlFileName, originalName );
	}

    /**
     *  The Session class tracks all the operations done on the file being
     *  processed.
     */
    class Session {
        /** the list of operations */
        Vector entries;
        ChiselSessionEntry lastChisel;
        public Session() {
            entries = new Vector();
        }

        /** store info about the chisel operating on the file */
        void chiselWith( Optimizer chisel ) {
            lastChisel = new ChiselSessionEntry( chisel );
            entries.addElement( lastChisel );
            if ( chisel.getNumberOptions() > 0 ) {
                entries.addElement( new ChiselOptionSettings( chisel.getNumberOptions(), chisel ));
            }
//            dump();
        }

        /** store info when a new version is made */
        void markVersion( int version ) {
            entries.addElement( new VersionSessionEntry( version ));
//            dump();
        }
        void markPercent( int percentX100 ) {
            if ( lastChisel != null ) {
                lastChisel.markPercent( percentX100 );
            }
//            dump();
        }

        /** Generate an HTML report to a specific file.
         *
         *  @param htmlFileName the name of the file to contain the html
         *  @param wrlFileName current name of the chiseled file
         *  @param originalName original name of the chiseled file, only
         *    used if different from the current name.
         */
        void generateHtml( String htmlFileName, String wrlFileName, String originalName ) {
            try {
                OutputStream os = new FileOutputStream( htmlFileName );
                HTMLgenerator htmlGen = new HTMLgenerator( os );
                htmlGen.startBody();
                htmlGen.header( "Chisel " + Chisel.version + ": " + wrlFileName );
                htmlGen.pText( "<P>" );
                if (( originalName != null ) && ( originalName.compareTo( wrlFileName ) != 0 )) {
                    htmlGen.locateText( "Original file: <A HREF=\"" + originalName + "\">" + originalName + "</A>", "EM" );
                    htmlGen.pText( "<BR>" );
                }
                
                htmlGen.locateText( "Chiseled file: <A HREF=\"" + wrlFileName + "\">" + wrlFileName + "</A>", "EM" );
                htmlGen.pText( "</P>" );
                htmlGen.startTable( null, "STRONG", 1, 4, 4 );
                htmlGen.startRow();
                htmlGen.columnHeader( "Chisel", "CENTER" );
                htmlGen.columnHeader( "% Reduction", "CENTER" );
                htmlGen.columnHeader( "Total Reduced", "CENTER" );
                htmlGen.endRow();
                ChiselSessionEntry prevCSE = null;

                int numberEntries = entries.size();
                for ( int i = 0; i < numberEntries; i++ ) {
                    SessionEntry s = (SessionEntry)entries.elementAt( i );
                    if ( s instanceof ChiselSessionEntry ) {
                        ChiselSessionEntry cse = (ChiselSessionEntry)s;
                        SessionEntry nextEntry = null;
                        if ( i < (numberEntries - 1 )) {
                            nextEntry = (SessionEntry)entries.elementAt( i + 1 );
                        }
                        cse.genRow( htmlGen, prevCSE, nextEntry );
                        prevCSE = cse;
                    }
                }
                htmlGen.terminate();
                System.out.println( "HTML report in " + htmlFileName );
            } catch ( Exception e ) {
                System.out.println( "Could not generate HTML" );
                e.printStackTrace();
            }
        }

        void dump() {
            for ( int i = 0; i < entries.size(); i++ ) {
                SessionEntry s = (SessionEntry)entries.elementAt( i );
                System.out.println( s.getName() );
                if ( s instanceof ChiselSessionEntry ) {
                    ChiselSessionEntry cs = (ChiselSessionEntry)s;
                    System.out.println( "reduction " + cs.getTotalReduction() );
                }
            }
        }
    }

    /** ChiselOptionSettings saves the settings for a specific chisel */
    class ChiselOptionSettings extends SessionEntry {
        Vector optionValues;
        Optimizer chisel;

        ChiselOptionSettings( int numberOptions, Optimizer chisel ) {
            super( "settings" );
            this.chisel = chisel;
            optionValues = new Vector( numberOptions );
            for ( int i = 0; i < numberOptions; i++ ) {
                optionValues.addElement( chisel.getOptionValue( i ));
            }
        }

        /** Generate a row entry, which is a table-within-a-table */
        void genRow( HTMLgenerator htmlGen ) {
            htmlGen.startTable( null, "STRONG", 1, 0, 0 );
            for ( int i = 0; i < optionValues.size(); i++ ) {
                htmlGen.startRow( "RIGHT" );
                htmlGen.genColumn( (String)chisel.getOptionLabel( i ) );
                Object x = optionValues.elementAt( i );
                if ( x instanceof String ) {
                    htmlGen.genColumn( (String)x );
                } else {
                    htmlGen.genColumn( "unknown" );
                }
                htmlGen.endRow();
            }
            htmlGen.endTable();
        }
    }

    /** SessionEntry is the base class for all entries */
    class SessionEntry {
        String name;
        public SessionEntry( String name ) {
            this.name = name;
        }
        public String getName()  {
            return( name );
        }
    }

    class ChiselSessionEntry extends SessionEntry{
        int numberOptions;
        Object[] options;
        int percentX100;
        Optimizer chisel;

        public ChiselSessionEntry( Optimizer chisel ) {
            super( chisel.getClass().getName() );
            this.chisel = chisel;
            numberOptions = chisel.getNumberOptions();
            percentX100 = 10000;
            if ( numberOptions > 0 ) {
                options = new Object[ numberOptions ];
                for ( int i = 0; i < numberOptions; i++ ) {
                    options[i] = chisel.getOptionValue( i );
                }
            }
        }

        /** Generate a row of text for a particular chisel */
        public void genRow( HTMLgenerator htmlGen, ChiselSessionEntry prevEntry, SessionEntry nextEntry ) {
            htmlGen.startRow( "LEFT" );
            if ( nextEntry instanceof ChiselOptionSettings ) {
                ChiselOptionSettings cos = (ChiselOptionSettings)nextEntry;
                htmlGen.genColumnStart( chisel.getActionMessage() );
                cos.genRow( htmlGen );
                htmlGen.genColumnEnd();
            } else {
                htmlGen.genColumn( chisel.getActionMessage() );
            }
            htmlGen.genColumn( getDelta( prevEntry ));
            htmlGen.genColumn( getTotalReduction() );
            htmlGen.endRow();
        }

        void markPercent( int percentX100 ) {
//            System.out.println( "markPercent " + percentX100 );
            this.percentX100 = percentX100;
        }
        String redStr;

        /** Get the string representing the percent reduction */
        String getTotalReduction()  {
//            if ( redStr != null ) {
//                System.out.println( "Reusing redStr, percentX100 is " + percentX100 );
//                return( redStr );
//            }
            System.out.println( "percentX100 is " + percentX100 );
            int px = 10000 - percentX100;
            int percent = px/100;
            int hundredths = px%100;
            if ( hundredths < 10 ) {
                redStr = percent + ".0" + hundredths + "%";
            } else {
                redStr = percent + "." + hundredths + "%";
            }
            return( redStr );
        }

        public int getX100() {
            return( percentX100 );
        }

        String getDelta( ChiselSessionEntry prevEntry ) {
            int prevX100 = 10000;
            if ( prevEntry != null ) {
                prevX100 = prevEntry.getX100();
            }
            int diff = prevX100 - percentX100;
            if ( diff < 0 ) {
                return( "*" );
            } else {
                int percent = diff/100;
                int hundredths = diff%100;
                if ( hundredths < 10 ) {
                    return( new String( percent + ".0" + hundredths + "%" ));
                } else {
                    return( new String( percent + "." + hundredths + "%" ));
                }
            }
        }
    }

    class VersionSessionEntry extends SessionEntry {
        int version;
        public VersionSessionEntry( int version ) {
            super( "Version " + version );
            this.version = version;
        }
    }
}


