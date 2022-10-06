/*
 * @(#)Chisel.java
 *
 * Copyright (c) 1998-2001 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import com.trapezium.clvorlon;
import com.trapezium.util.GlobalProgressIndicator;
import com.trapezium.util.ProgressIndicator;
import com.trapezium.util.StringUtil;
import com.trapezium.util.RemoteUrlGenerator;
import com.trapezium.util.UrlLocalizer;
import com.trapezium.util.DirectoryCreator;
import com.trapezium.vrml.*;
import com.trapezium.vrml.grammar.*;
import com.trapezium.vrml.visitor.*;
import com.trapezium.factory.FactoryData;
import com.trapezium.factory.FactoryResponseAdapter;
import com.trapezium.parse.InputStreamFactory;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.parse.TokenTypes;
import com.trapezium.chisel.gui.DisplayConstants;
import com.trapezium.chisel.gui.WorkspaceListener;
import com.trapezium.chisel.gui.FetchDialog;
import com.trapezium.chisel.gui.OptionsDialog;
import com.trapezium.chisel.gui.AboutDialog;
import com.trapezium.chisel.gui.AboutPanel;
import com.trapezium.chisel.gui.GlyphButton;
import com.trapezium.chisel.gui.ComponentFactory;
import com.trapezium.chisel.gui.DividerBarComponent;
import com.trapezium.chisel.gui.ChiselAWTViewer;
import com.trapezium.chisel.gui.PopupViewer;
import com.trapezium.vrml.node.humanoid.SpecHumanoid;

import adc.parser.*;


/** Main class for Chisel.
 *
 *  The Chisel class manages the data, the processing of the data, and the user
 *  interface.  Data is contained in an array of ProcessedFile objects.
 *  Processing is controlled by an array of chisel descriptors.  The user
 *  interface is implemented in an interior class called FrontEnd, which owns
 *  gui components created by ComponentFactory.
 *
 *  Chisel also is the central hub for event traffic.  Commands from any source
 *  are dispatched by Chisel.  The ChiselFileTable handles all events from file
 *  processing, and forwards some of these to Chisel.
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.1, 29 Jan 1998
 *
 *  @since           1.0
 */


public class Chisel extends FactoryResponseAdapter implements DisplayConstants {

    // metadata
    public final static String version = "2.1.4";
    public final static String appTitle = "Chisel";
    public final static String fullTitle = appTitle + " " + version;
    public final static String trapeziumHeader = "Copyright (c) 1998-2001 by Trapezium Development LLC.  All rights reserved.";
    public final static String contextFile = "chisel.context";

    /** files being viewed or processed */
    public Vector files;   // ProcessedFile objects

    /** user interface class library flag */
    public static boolean tryJFC = false;

    /** optimization, disable window painting while running through chisel chains */
    public static boolean disableTextWindowPaint = false;

    /** if true, keep running through clean sequence until nothing left to do */
    public static boolean autoClean = true;

    /** default workspace reload on launch option setting*/
    static boolean default_reloadWorkspace = false;
    
    static public boolean includeWarningsInSearch = false;
    static public boolean includeNonconformancesInSearch = false;
    

    /** property name */
    static final String name_reloadWorkspace = "workspace.reload";

    static public Chisel singletonChisel = null;

    /** application directory */
    public static String appDirectory;

    // this remembers the proper visible location while we're hiding the
    // front end window during validation
    transient int startX = 0;
    transient int startY = 0;

    protected FileDialog fileDialog;

    FrontEnd frontEnd;

    public void setErrorMarks( ProcessedFile processedFile, BitSet errorMarks, BitSet warningMarks, BitSet nonconformanceMarks ) {
        frontEnd.setErrorMarks( processedFile, errorMarks, warningMarks, nonconformanceMarks );
    }
    
    public void updateHeaderLine() {
        frontEnd.updateHeaderLine();
    }

    /** Used only for humanoid verification */
	static public SpecHumanoid humanoid = null;
	static SpecHumanoid savedHumanoid = null;
	static boolean attemptedHumanoidLoading = false;
	static public void loadHumanoid() {
	    if ( attemptedHumanoidLoading ) {
	        humanoid = savedHumanoid;
	    } else if (( humanoid == null ) && !attemptedHumanoidLoading ) {
	        attemptedHumanoidLoading = true;
    		savedHumanoid = new SpecHumanoid( "humanoid1.1" );
    		if ( !savedHumanoid.failedLoading() ) {
    			humanoid = savedHumanoid;
    		}
    	}
	}
	static public void unloadHumanoid() {
	    humanoid = null;
	}

    public Chisel() {
        files = new Vector();
        singletonChisel = this;
        initCommands();
        frontEnd = new FrontEnd(this);
    }
    
    public void refresh() {
        frontEnd.refresh();
    }

    public static void showSystemInfo() {
        System.out.println("System Properties\n---------------------------");
        System.out.println(System.getProperties());
        System.out.println("Fonts\n---------------------------");
        String fontlist[] = Toolkit.getDefaultToolkit().getFontList();
        for (int i = 0; i < fontlist.length; i++) System.out.println(" ...font " + i + ": " + fontlist[i]);
    }

    static boolean launchGUI = true;
    public static void main(String[] args) {
		System.out.println( fullTitle );
		System.out.println( trapeziumHeader );

        String javaversion = System.getProperty("java.version");
        if (javaversion.compareTo("1.1.2") < 0) {
            System.out.println("Warning: outdated Java VM; some features will not be available.");
            System.out.println("To enable all features run on a version 1.1.2 or higher VM.");
        }
        
        /////
        System.out.println("Java version " + javaversion);
        String classpath = System.getProperty("java.class.path");
        System.out.println("Classpath = " + classpath);
        
        appDirectory = "";
        StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator); 
        while (st.hasMoreTokens()) {
            String path = st.nextToken();
//            System.out.println( "path is '" + path + "'" );
            if (path.toLowerCase().endsWith("chisel.jar")) {
          //      File file = new File(path);
//                System.out.println( "Found chisel.jar in '" + path + "'" );
//                appDirectory = file.getParent() + File.separatorChar;
                if ( path.length() > 10 ) {
                    appDirectory = path.substring( 0, path.length() - 10 );
                } else {
                    appDirectory = "";
                }
//                System.out.println( "set appDirectory to '" + appDirectory + "'" );
                break;
            }
        }
        System.out.println("Chisel directory: " + appDirectory);
        
int delay = 0;
		if ( args.length != 0 ) {
			for ( int i = 0; i < args.length; i++ ) {
				if ( args[i].compareTo( "-forceawt" ) == 0 ) {
					tryJFC = false;
				} else if ( args[i].compareTo( "-forcejfc" ) == 0 ) {
					tryJFC = true;
			    } else if ( args[i].compareTo( "-sysinfo" ) == 0 ) {
                    showSystemInfo();

                // There are two places resources such as images can come
                // from: chisel.jar or separate files.  The default is
                // defined in ChiselResources.java and can be overriden
                // by the following two flags
                } else if ( args[i].compareTo( "-loadfromjar" ) == 0 ) {
                    ChiselResources.setLoadFromJar(true);
                } else if ( args[i].compareTo( "-loadfromfile" ) == 0 ) {
                    ChiselResources.setLoadFromJar(false);
        		} else if ( args[i].compareTo( "-chisel" ) == 0 ) {
        		    launchGUI = false;
        		} else if ( args[i].compareTo( "-vorlon" ) == 0 ) {
        		    launchGUI = false;
        		} else if ( args[i].compareTo( "-d1" ) == 0 ) {
        		    delay = 1000;
        		} else if ( args[i].compareTo( "-d2" ) == 0 ) {
        		    delay = 2000;
        		} else if ( args[i].compareTo( "-d3" ) == 0 ) {
        		    delay = 3000;
        		} else if ( args[i].compareTo( "-d4" ) == 0 ) {
        		    delay = 4000;
        		} else if ( args[i].compareTo( "-d5" ) == 0 ) {
        		    delay = 5000;
        		}
			}
	    }


        boolean refusedAndCancelled = false;
        if ( launchGUI ) {
            Image icon = ChiselResources.getImage("chisel");
            ComponentFactory.preloadImages();
            if ( delay > 0 ) {
                try {
                    Thread.sleep( delay );
                } catch ( Exception ee ) {
                }
            }

            Chisel chisel = new Chisel();
            chisel.openContext( contextFile );

            chisel.frontEnd.show();

            String filename = null;
            for (int i = 0; i < args.length; i++) {
                if (!args[i].startsWith("-")) {
                    filename = args[i];
                    break;
                }
            }
            chisel.openWorkspace();
            if (filename != null) {
                chisel.open(filename);
            }                    

        } else {
            clvorlon.enableChisel = true;
            clvorlon.main( args );
        }
    }

    public ProcessedFileViewer getViewer() {
        return frontEnd.getViewer();
    }

    private void initCommands() {
        ActionConnector.addActions(getActionList());
    }


    public ChiselAction[] getActionList() {
        return defaultActions;
    }

    public void done(FactoryData result) {
        if ( !result.getAborted() ) {
            // probably a better way to handle this, but if the file wasn't
            // found, get rid of the viewer.  Still have to reset title line
            if ( result.getFile() == null ) {
                ProcessedFileViewer pfv = getViewer();
                Component pfvComponent = pfv.getComponent();
                if ( pfvComponent instanceof ChiselAWTViewer ) {
                    ((ChiselAWTViewer)pfvComponent).close();
                }
            } else {
                //System.out.println("Chisel.done " + result.getFile().getName());
                frontEnd.done(result);
            }
        } else {
            GlobalProgressIndicator.abortCurrentProcess = false;
            closeFile((ProcessedFile)result );
            ProcessedFileViewer pfv = getViewer();
            Component pfvComponent = pfv.getComponent();
            if ( pfvComponent instanceof ChiselAWTViewer ) {
                ((ChiselAWTViewer)pfvComponent).close();
            }
        //    closeFile((ProcessedFile)result);
        }
    }

    public void open(String filename) {
        if (filename == null || filename.length() == 0) {
            ChiselSet.enableChisels();
            return;
        }
        String ext = "";
        int extStart = filename.lastIndexOf('.');
        if (extStart >= 0) {
            ext = filename.substring(extStart);
        }
        if (ext.equals(".context")) {
            openContext(filename);
        } else {
//            System.out.println("Opening file " + filename);
            openFile(filename);
//            System.out.println("Done.");
        }
    }

    public void openContext(String filename) {

        System.out.println("Opening context file " + filename);
		ChiselProperties.setProperties(filename);
		Properties props = ChiselProperties.getProperties();

        frontEnd.loadProperties(props);

        String autocleanProp = props.getProperty("workspace.automaticClean");
        if ( autocleanProp != null ) {
            autoClean = "true".equalsIgnoreCase( autocleanProp );
        }
    }
    
    public void openWorkspace() {
		Properties props = ChiselProperties.getProperties();
        String reloadProp = props.getProperty(name_reloadWorkspace);
        boolean reload = (reloadProp == null) ? default_reloadWorkspace : ("true".equalsIgnoreCase(reloadProp));
        if (reload) {
    		for (int i = 0; ; i++) {
    			String name = props.getProperty("file." + i);
    			if (name == null || name.length() == 0) {
    				break;
    			}
    			openFile(name);
    		}

    		String active = props.getProperty("viewer.active");
    		if (active != null) {
    		    frontEnd.selectFile(active);
    		    boolean maximized = "true".equalsIgnoreCase(props.getProperty("viewer.maximized"));
    		    if (maximized) {
    		        frontEnd.maximizeViewer(true);
    		    }
    		}
        }
     }


    public void saveContext(String filename) {
        if (filename == null || filename.length() == 0) {
            filename = contextFile;

            System.out.println("Saving context to " + filename + "...");

			Properties props = ChiselProperties.getProperties();

            frontEnd.saveProperties(props);

            //props.put(name_reloadWorkspace, String.valueOf(reloadWorkspace));

			// clear out any files that are there
			for (int i = 0; ; i++) {
				String name = "file." + i;
				if (props.getProperty(name) == null) {
					break;
				}
				props.remove(name);
			}

			int n = 0;
			Enumeration elements = files.elements();
            while (elements.hasMoreElements()) {
                ProcessedFile file = (ProcessedFile) elements.nextElement();
                if (file != null) {
                    System.out.println("..." + file.getUrl() ); //was getLocalName());
					String name = "file." + n++;
                    props.put(name,  file.getUrl() );  //was getLocalName());
                }
            }
            ProcessedFileViewer viewer = getViewer();
            if (viewer != null) {
                ProcessedFile file = viewer.getProcessedFile();
                if (file != null) {
                    props.put("viewer.active", file.getUrl());
                }
                props.put("viewer.maximized", String.valueOf(frontEnd.isViewerMaximized()));
            }
			ChiselProperties.saveProperties(contextFile);
            System.out.println("Done.");
        }
    }


    public void openFile(String filename) {
        if (filename == null || filename.length() < 1) {
           return;
        }

        System.out.println("Opening " + filename + "...");
        ProcessedFile file = new ProcessedFile(filename);
        file.setUndoItem( ComponentFactory.getUndoMenuItem() );
        file.setRedoItem( ComponentFactory.getRedoMenuItem() );
        files.addElement(file);
        file.setDoneListener(this);
        frontEnd.open(file);
        file.load();
//        System.out.println("Done.");
    }

    public void newFile(String filename) {
        if (filename == null || filename.length() < 1) {
           return;
        }

        System.out.println("Creating " + filename + "...");
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream( filename ));
		} catch ( Exception e ) {
			System.out.println( "Failed to open output file '" + filename + "'" );
			return;
		}
        ps.println("#VRML V2.0 utf8");
		ps.close();
		openFile(filename);
    }

    /** Close a processed file, and remove any serialized versions of it */
    public void closeFile(ProcessedFile file) {
        if (files.contains(file)) {
//            System.out.println("Chisel closing file " + file.getName());
            files.removeElement(file);
            frontEnd.close(file);
            
            if (file.isTemporary()) {
                file.getFile().delete();
            }
            file.removeSerialFiles();
        }
    }

    // NOTE:  these actions have to be identical to the ones in ChiselResources.java
    // maybe one should ref the other
    public static final String openAction = "open";
    public static final String newAction  = "new";
    public static final String saveAction = "save";
    public static final String saveAsAction = "saveAs";
    public static final String gzipSaveAction = "gzipSave";
    public static final String gzipSaveAsAction = "gzipSaveAs";
    //public static final String x3dSaveAction = "x3dSave";
    public static final String x3dSaveAsAction = "x3dSaveAs";
    public static final String exitAction = "exit";
    public static final String pasteAction = "paste-from-clipboard";
    public static final String cutAction = "cut-to-clipboard";
    public static final String copyAction = "copy-to-clipboard";
    public static final String undoAction = "undo-last-command";
    public static final String redoAction = "redo-last-command";
    public static final String nextErrAction = "next-error";
    public static final String prevErrAction = "prev-error";
    public static final String aboutAction = "about";
    public static final String unlockAction = "unlock";
    public static final String htmlAction = "genhtml";
    public static final String stopAction = "stop";
    public static final String validateAction = "validate";
    public static final String cleanAction = "clean";
    public static final String condenseAction = "condense";
    public static final String reduceAction = "reduce";
    public static final String mutateAction = "mutate";
    public static final String reorganizeAction = "reorganize";


    /**
     * Actions
     */
    private ChiselAction[] defaultActions = {
        new NewAction(),
        new OpenAction(),
        new FetchAction(),
        new SaveAction(),
        new SaveAsAction(),
        new GzipSaveAction(),
        new GzipSaveAsAction(),
        //new X3dSaveAction(),
        new X3dSaveAsAction(),
        new GenhtmlAction(),
        new ExitAction(),
        new PasteAction(),
        new CutAction(),
        new CopyAction(),
        new UndoAction(),
        new RedoAction(),
        new NexterrorAction(),
        new PreverrorAction(),
        new AboutAction(),
        //new UnlockAction(),
        new StopAction(),
        new ValidateAction(),
        new FormatAction(),
        new CleanAction(),
        new CondenseAction(),
        new ReduceAction(),
        new MutateAction(),
        new ReorganizeAction(),
        new OptionsAction()
    };

         static final boolean dbgfetch=false;

    class FetchAction extends ChiselAction implements Runnable {
        // files to possibly get references for
        Vector workFiles;
        
        // urlLocalizer is really specific to file
        Vector urlLocalizers;
        
        // files we've already got references for
        Vector doneFiles;
        
        // list of ".wrl" files to open
        Vector filesToOpen;
        
        // list of local fetched files
        Hashtable fetchedFiles;
        // for converting remote names to local names
        UrlLocalizer urlLocalizer;
        
        // for creating local directories
        DirectoryCreator directoryCreator;
     
        FetchAction() {
            super();
        }

        boolean alreadyDone( String fileName ) {
            if ( doneFiles == null ) {
//                System.out.println( "doneFiles is null" );
                return( true );
            } else if ( fileName == null ) {
//                System.out.println( "fileName is null" );
                return( true );
            } else {
                int doneListSize = doneFiles.size();
                for ( int i = 0; i < doneListSize; i++ ) {
                    String test = (String)doneFiles.elementAt( i );
                    if ( test.compareTo( fileName ) == 0 ) {
                        return( true );
                    }
                }
                return( false );
            }
        }
        
        boolean alreadyInWorklist( String fileName ) {
            if ( workFiles == null ) {
                return( false );
            } else if ( fileName == null ) {
                return( true );
            } else {
                int workListSize = workFiles.size();
                for ( int i = 0; i < workListSize; i++ ) {
                    String test = (String)workFiles.elementAt( i );
                    if ( test.compareTo( fileName ) == 0 ) {
                        return( true );
                    }
                }
                return( false );
            }
        }

		String localFileName;
		String urlToFetch;
		boolean recurseState;
		boolean otherRecurseState;

        public void actionPerformed(ActionEvent e) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            recurseState = false;
            otherRecurseState = false;
            String url = "";
            ProcessedFileViewer viewer = getViewer();
            if (viewer != null) {
                ProcessedFile file = viewer.getProcessedFile();
                if (file != null) {
                    url = file.getUrl();
                }
            }
            FetchDialog fetcher = new FetchDialog(frontEnd, url);
            fetcher.setVisible(true);

            urlToFetch = fetcher.getUrl();
    		if ( urlToFetch.indexOf( "http:" ) != 0 ) {
    		    if ( fetcher.wasCancelled() ) {
    		        System.out.println( "" );
    		    } else {
        			System.out.println( "Fetch requires a URL" );
        		}
    			return;
    		}
    		String sourceUrlPath = null;
    		if ( urlToFetch.lastIndexOf( '/' ) > 0 ) {
			    sourceUrlPath = urlToFetch.substring( 0, urlToFetch.lastIndexOf( '/' ) + 1 );
    		} else if ( urlToFetch.lastIndexOf( '\\' ) > 0 ) {
		        sourceUrlPath = urlToFetch.substring( 0, urlToFetch.lastIndexOf( '\\' ) + 1 );
            }
    		localFileName = urlToFetch.substring( urlToFetch.lastIndexOf( '/' ) + 1, urlToFetch.length() );
       		if ( localFileName == null || localFileName.length() == 0 ) {
   				localFileName = "index.html";
    	    }

            Frame frame = frontEnd.getFrame();
            if (fileDialog == null) {
                fileDialog = new FileDialog(frame);
            }
            fileDialog.setFile(localFileName);
            fileDialog.setMode(FileDialog.SAVE);
            fileDialog.show();

            localFileName = fileDialog.getFile();
            if (localFileName == null) {
                return;
            }

            String destDir = fileDialog.getDirectory();
            if ( destDir != null ) {
                localFileName = destDir + localFileName;
            }
            urlLocalizer = new UrlLocalizer( sourceUrlPath, destDir );
            recurseState = fetcher.getRecurseState();
            otherRecurseState = fetcher.getOtherRecurseState();
            if ( fetcher.siteRetrieval() ) {
                urlLocalizer.setFilterType( RemoteUrlGenerator.SITE_FILTERING );
            }
            if ( fetcher.wrlRetrieval() ) {
                urlLocalizer.addExtensionAcceptor( ".wrl" );
                urlLocalizer.addExtensionAcceptor( ".wrz" );
                urlLocalizer.addExtensionAcceptor( ".x3d" );
                urlLocalizer.addExtensionAcceptor( ".x3dv" );
            }
            if ( fetcher.pictureRetrieval() ) {
                urlLocalizer.addExtensionAcceptor( ".gif" );
                urlLocalizer.addExtensionAcceptor( ".jpg" );
                urlLocalizer.addExtensionAcceptor( ".png" );
            }
            if ( fetcher.soundRetrieval() ) {
                urlLocalizer.addExtensionAcceptor( ".wav" );
                urlLocalizer.addExtensionAcceptor( ".mid" );
                urlLocalizer.addExtensionAcceptor( ".aif" );
                urlLocalizer.addExtensionAcceptor( ".aiff" );
                urlLocalizer.addExtensionAcceptor( ".au" );
                urlLocalizer.addExtensionAcceptor( ".mp3" );
            }
            if ( fetcher.movieRetrieval() ) {
                urlLocalizer.addExtensionAcceptor( ".mpg" );
                urlLocalizer.addExtensionAcceptor( ".mpeg" );
                urlLocalizer.addExtensionAcceptor( ".avi" );
                urlLocalizer.addExtensionAcceptor( ".mov" );
            }
            if ( fetcher.htmlRetrieval() ) {
                urlLocalizer.addExtensionAcceptor( ".htm" );
                urlLocalizer.addExtensionAcceptor( ".html" );
            }
            if ( fetcher.hasAdditionalAcceptors() ) {
                int n = fetcher.getNumberAdditionalAcceptors();
                for ( int i = 0; i < n; i++ ) {
                    urlLocalizer.addExtensionAcceptor( fetcher.getAdditionalAcceptor( i ));
                }
            }
            new Thread( this ).start();
        }
    
        void addWorkFile( String workFileName, UrlLocalizer urlLocalizer, String source ) {
            if ( alreadyInWorklist( workFileName )) {
                return;
            } else if ( alreadyDone( workFileName )) {
                return;
            } else {
                workFiles.addElement( workFileName );
                urlLocalizers.addElement( urlLocalizer );
            }
        }

        PrintStream fetchLog;

        public void run() {
            try {
                workFiles = new Vector();
                doneFiles = new Vector();
                filesToOpen = new Vector();
                urlLocalizers = new Vector();
                fetchedFiles = new Hashtable();
                fetchLog = null;
                try {
                    File f = new File( "fetch.log" );
                    FileOutputStream fos = new FileOutputStream( f );
                    fetchLog = new PrintStream( fos );
                } catch ( Exception e ) {
                }

                if (!fetchFile(urlToFetch, localFileName)) {
                    System.out.println( "Failed fetching " + urlToFetch );
                    return;
                }
                addWorkFile( localFileName, urlLocalizer, "Main" );
                if ( recurseState || otherRecurseState ) {
					while (( workFiles.size() > 0 ) && ( !GlobalProgressIndicator.abortCurrentProcess )) {
if (dbgfetch)
    System.out.println( "workFiles.size is " + workFiles.size() );
					    localFileName = (String)workFiles.elementAt( 0 );
					    urlLocalizer = (UrlLocalizer)urlLocalizers.elementAt( 0 );
if (dbgfetch)
    System.out.println( "Now processing " + localFileName );
					    workFiles.removeElementAt( 0 );
					    urlLocalizers.removeElementAt( 0 );
					    if ( alreadyDone( localFileName )) {
if (dbgfetch)
    System.out.println( "ALREADY DONE!" );
					        continue;
					    }
					    doneFiles.addElement( localFileName );
					    String lowerCaseLocalFileName = localFileName.toLowerCase();
					    if (( lowerCaseLocalFileName.indexOf( ".wrl" ) > 0 ) ||
					        ( lowerCaseLocalFileName.indexOf( ".wrz" ) > 0 )) {
					        filesToOpen.addElement( localFileName );
					        getWrlReferences( localFileName, recurseState, otherRecurseState );
					    } else if ( lowerCaseLocalFileName.indexOf( ".htm" ) > 0 ) {
					        getHtmlReferences( localFileName, recurseState, otherRecurseState );
					    }
                    }
                } else {
                    filesToOpen.addElement( localFileName );
                }
            } catch (Exception ex) {
if (dbgfetch)
    System.out.println( ex.toString() );
                ex.printStackTrace();
                System.out.println("Unable to fetch '" + urlToFetch + "'" );
                fetchLog.close();
                fetchLog = null;
                return;
            }
            fetchLog.close();
            fetchLog = null;
            System.out.println( "Fetch complete: " + filesToOpen.size() + " files to open" );
            if ( !GlobalProgressIndicator.abortCurrentProcess ) {
                for ( int i = 0; i < filesToOpen.size(); i++ ) {
            		openFile( (String)filesToOpen.elementAt(i) );
            	}
            }
            if ( fetchedFiles.size() > 0 ) {
                if ( fetchedFiles.size() == 1 ) {
                    System.out.println( "Fetched 1 file." );
                } else {
                    System.out.println( "Fetched " + fetchedFiles.size() + " files." );
                }
            }
        }
        
        /** Get all the files referenced by a "wrl" file, add additional file
         *  references to the "workFiles" list.
         *
         *  @param saveFile file containing the references
         *  @param recurse flag enables recursive file fetches of ".wrl" files
         *  @param otherRecurse flag enables fetches of non ".wrl" files
         */
        void getWrlReferences( String saveFile, boolean recurse, boolean otherRecurse ) {
if (dbgfetch)            System.out.println( "Get wrl references from '" + saveFile + "'" );
            try {
                File f = new File( saveFile );
                TokenEnumerator te = new TokenEnumerator(
					InputStreamFactory.getInputStream( f ),	null, null, f );
                VRML97parser parser = new VRML97parser();
                Scene s = new Scene( saveFile, te );
                parser.Build( te, s );
                UrlVisitor urlv = new UrlVisitor( saveFile, UrlVisitor.USE_ORIGINAL_NAMES );
                s.traverse( urlv );

                if ( recurse ) {
                    int numberFiles = urlv.getNumberFiles();
if (dbgfetch)
    System.out.println( "urlv has " + numberFiles + " wrl files to fetch" );
                    for ( int i = 0; i < numberFiles; i++ ) {
                        if ( GlobalProgressIndicator.abortCurrentProcess ) {
                            break;
                        }
                        String urlFile = urlv.getFileAt( i );
    					String destFileName = urlLocalizer.createLocalFileName( urlFile );
    					String urlToFetch = urlLocalizer.createUrlToFetch( urlFile );
if (dbgfetch)
    System.out.println( "Calling fetchFile " + urlFile + " expands to " + urlToFetch + ", dest " + destFileName );
    					try {
                       		if ( fetchFile( urlToFetch, destFileName )) {
                       		    addWorkFile( destFileName, urlLocalizer.getNewLocalizer(), "WRL inline" );
                       		}
                       	} catch ( Exception eq ) {
                       	    eq.printStackTrace();
              	
                       	}
                    }
                } //else System.out.println( "recurse is false" );
                if ( otherRecurse ) {
                    int numberFiles = urlv.getNumberOtherFiles();
if (dbgfetch)
    System.out.println( "urlv has " + numberFiles + " non-wrl files to fetch" );
                    for ( int i = 0; i < numberFiles; i++ ) {
                        if ( GlobalProgressIndicator.abortCurrentProcess ) {
                            break;
                        }
                        String urlFile = urlv.getOtherFileAt( i );
                        String destFileName = urlLocalizer.createLocalFileName( urlFile );
                        String urlToFetch = urlLocalizer.createUrlToFetch( urlFile );
                        try {
                            fetchFile( urlToFetch, destFileName );
                        } catch ( Exception eq ) {
                            eq.printStackTrace();
                        }
                    }
                } //else if (dbgfetch) System.out.println( "otherRecurse is false" );
                // very stupid fetching here, any PROTO SFString or MFString
                // field with a string ending in ".html" or ".wrl" is
                // treated as a url
                // (not implemented yet, leno example needs this)
            } catch ( Exception eee ) {
                eee.printStackTrace();
                System.out.println( "exception is " + eee );
            }
        }

        
        /** Get files referenced by an HTML file, add any additional file 
         *  references to the "workFiles" list.
         */
        void getHtmlReferences( String saveFile, boolean recurse, boolean otherRecurse ) {
//            System.out.println( "..Get HTML references from '" + saveFile + "'" );
            try {
                StringBuffer sb = new StringBuffer();
                TokenEnumerator tokenEnumerator = new TokenEnumerator( InputStreamFactory.getInputStream( new File( saveFile )), saveFile, false );
                int tokenScanner = 0;
                tokenEnumerator.setState( tokenScanner );
                while ( tokenScanner != -1 ) {
                    if ( tokenEnumerator.getType( tokenScanner ) == TokenTypes.QuotedString ) {
                        sb.append( StringUtil.stripQuotes( tokenEnumerator.toString( tokenScanner )));
                    }
                    tokenScanner = tokenEnumerator.getNextToken();
                }
                if ( sb.length() > 0 ) {
if ( dbgfetch )
    System.out.println( "....Processing HTML tokenized stream for '" + sb + "'" );
                    HtmlStreamTokenizer quoteTokenizer = new HtmlStreamTokenizer( new StringBufferInputStream( new String( sb )));
                    processHtmlStream( quoteTokenizer );
                }
                System.out.println( "....Processing HTML in '" + saveFile + "'" );
    			HtmlStreamTokenizer tok = new HtmlStreamTokenizer( InputStreamFactory.getInputStream( new File( saveFile )));
    			processHtmlStream( tok );
			}
			catch (IOException e) {
			    e.printStackTrace();
			    System.out.println( e.toString() );
			}
        }

        /** Extract any files to fetch from HTML file, and fetch them */
        void processHtmlStream( HtmlStreamTokenizer tok ) {
       		HtmlTag tag = new HtmlTag();

            try {
		    	while (tok.nextToken() != HtmlStreamTokenizer.TT_EOF) {
            		int ttype = tok.getTokenType();

	    			if (ttype == HtmlStreamTokenizer.TT_TAG) {
		    			tok.parseTag(tok.getStringValue(), tag);
			        	int tagtype = tag.getTagType();
                        if ( !tag.isEndTag() ) {
                            String src = null;
                            if ( tagtype == tag.T_EMBED ) {
                                src = tag.getParam( "src" );
//                                System.out.println( "T_EMBED " + src );
                            } else if ( tagtype == tag.T_IMG ) {
                                src = tag.getParam( "src" );
//                                System.out.println( "T_IMG " + src );
                            } else if ( tagtype == tag.T_A ) {
                                src = tag.getParam( "href" );
//                                System.out.println( "T_A " + src );
                            } else if ( tagtype == tag.T_FRAME ) {
                                src = tag.getParam( "src" );
                                System.out.println( "T_FRAME " + src );
                            } else if ( tagtype == tag.T_BODY ) {
                                src = tag.getParam( "background" );
                            }
                            if ( src != null ) {
                                if (( src.length() > 0 ) && ( src.charAt( 0 ) != '#' ) && ( src.indexOf( "mailto" ) != 0 )) {
                                    if ( GlobalProgressIndicator.abortCurrentProcess ) {
                                        break;
                                    }
                                    String destFileName = urlLocalizer.createLocalFileName( src );
                                    System.out.println( "urlLocalizer local dir is '" + urlLocalizer.getLocalDirPath() + "'" );
                                    System.out.println( "Local file name of '" + src + "' is '" + destFileName + "'" );
                                    System.out.println( "original src '" + src + "'" );
                                    src = urlLocalizer.createUrlToFetch( src );
                                    System.out.println( "urlToFetch is '" + src + "'" );
                                    if ( src != null ) {
                                        try {
                                            if ( fetchFile( src, destFileName )) {
                                                addWorkFile( destFileName, urlLocalizer.getNewLocalizer(), "HTML" );
//                                            } else {
  //                                              System.out.println( "FETCH FAILED FOR " + destFileName );
                                            } 
                                        } catch( Exception eee ) {
                                            eee.printStackTrace();
                                        }
//                                    } else {
  //                                      System.out.println( "** DID NOT FETCH '" + destFileName );
                                    }

                                }
                            }
                        }
                    }
                }
	        } catch (HtmlException e)	{
	            e.printStackTrace();
			    System.out.println( e.toString() );
			} catch ( Exception ee ) {
			    ee.printStackTrace();
			    System.out.println( ee.toString() );
			}
		}
		
		/** Create a destination directory for a file. */
		void createDestinationDirectory( String fileName ) {
		    if ( directoryCreator == null ) {
		        directoryCreator = new DirectoryCreator();
		    }
		    directoryCreator.createDestinationDirectory( fileName );
		}
		
		
        /**
         *  Create a local copy of a url
         *
         *  This version is identical to the one in vorlon except that it takes
         *  the desired local name as an additional parameter.
         *
         *  @param inUrl     url to fetch
         *  @param saveFile  local name to save as
         *  @return  true if the file was fetched, otherwise false
         */
        boolean fetchFile( String inUrl, String saveFile ) throws FileNotFoundException, IOException, ClassNotFoundException {
            if ( saveFile == null ) {
                return( false );
            }
            if ( fetchedFiles.get( saveFile ) != null ) {
                System.out.println( "url " + inUrl + " (" + saveFile + ") already fetched." );
                return( true );
            }
    		if ( inUrl == null ) {
    		    return false;
    		}
            fetchedFiles.put( saveFile, saveFile );
    		System.out.println( "File #" + fetchedFiles.size() +  ", fetching '" + inUrl + "' to '" + saveFile + "'" );
    		if ( fetchLog != null ) {
        		fetchLog.println( "File #" + fetchedFiles.size() +  ", fetching '" + inUrl + "' to '" + saveFile + "'" );
        	}
    		createDestinationDirectory( saveFile );
    		InputStream inputStream = InputStreamFactory.getRawInputStream( inUrl );
    		if ( inputStream == null ) {
    		    System.out.println( "Cannot fetch file." );
    		    return false;
    		}
     		FileOutputStream outputStream = null;
    		try {
    			outputStream = new FileOutputStream( saveFile );
    		} catch ( Exception e ) {
    			System.out.println( "Failed to open output file '" + saveFile + "'" );
    			return false;
    		}
    		byte[] buf = new byte[4096];
    		int count;
    		while ((( count = inputStream.read( buf )) >= 0 ) && !GlobalProgressIndicator.abortCurrentProcess ) {
    			if ( count == 4096 ) {
    				outputStream.write( buf );
    			} else if ( count > 0 ) {
    				outputStream.write( buf, 0, count );
    			}
    		}
    		outputStream.close();
    		return true;
        }
    }

    class OpenAction extends ChiselAction {

        OpenAction() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            Frame frame = frontEnd.getFrame();
            if (fileDialog == null) {
                fileDialog = new FileDialog(frame);
            }
            fileDialog.setMode(FileDialog.LOAD);
            fileDialog.show();

            String file = fileDialog.getFile();
            if (file == null) {
                return;
            }
            if (file.indexOf(File.separatorChar) == -1) {
                String directory = fileDialog.getDirectory();
                if (directory.lastIndexOf(File.separatorChar) < directory.length() - 1) {
                    directory = directory + File.separatorChar;
                }
                file = directory + file;
            }
            open(file);
        }
    }

    static private int untitledSeq = 1;
    class NewAction extends ChiselAction {

        NewAction() {
            super();
        }


        public void actionPerformed(ActionEvent e) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            String name = "Untitled" + untitledSeq++ + ".wrl";
            newFile(name);
        }
    }

    class PasteAction extends ChiselAction {
        PasteAction() {
            super();
        }

        public void actionPerformed( ActionEvent e ) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            frontEnd.paste();
        }
    }

    class UndoAction extends ChiselAction {
        UndoAction() {
            super();
        }

        public void actionPerformed( ActionEvent e ) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            ProcessedFileViewer viewer = getViewer();
            if (viewer == null) {
                return;
            }

            ProcessedFile file = viewer.getProcessedFile();
            if (file == null) {
                return;
            }
            file.initProgressIndicator( "Undo..." );
            frontEnd.undo();
        }
    }

    class RedoAction extends ChiselAction {
        RedoAction() {
            super();
        }

        public void actionPerformed( ActionEvent e ) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            ProcessedFileViewer viewer = getViewer();
            if (viewer == null) {
                return;
            }

            ProcessedFile file = viewer.getProcessedFile();
            if (file == null) {
                return;
            }
            file.initProgressIndicator( "Redo..." );
            frontEnd.redo();
        }
    }

    class CutAction extends ChiselAction {
        CutAction() {
            super();
        }

        public void actionPerformed( ActionEvent e ) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            frontEnd.cut();
        }
    }

    class CopyAction extends ChiselAction {
        CopyAction() {
            super();
        }

        public void actionPerformed( ActionEvent e ) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            frontEnd.copy();
        }
    }

    class SaveAction extends ChiselAction {

        SaveAction() {
            super();
        }

        public void actionPerformed(ActionEvent ev) {
            GlobalProgressIndicator.abortCurrentProcess = false;

            ProcessedFileViewer viewer = getViewer();
            /*null;
            for (Object source = ev.getSource(); source != null; ) {
                if (source instanceof ProcessedFileViewer) {
                    viewer = (ProcessedFileViewer) source;
                    break;
                }
                if ( source instanceof Component ) {
                    source = ((Component)source).getParent();
                } else if ( source instanceof MenuComponent ) {
                    source = ((MenuComponent)source).getParent();
                } else {
                    break;
                }
            }*/
            if (viewer == null) {
                return;
            }

            ProcessedFile file = viewer.getProcessedFile();
            if (file == null) {
                System.out.println("Unable to save; no data.");
                return;
            }
            String name = file.getLocalName();
            if (name == null) {
                Frame frame = frontEnd.getFrame();
                if (fileDialog == null) {
                    fileDialog = new FileDialog(frame);
                }
                fileDialog.setFile( name );
                fileDialog.setMode(FileDialog.SAVE);
                fileDialog.show();

                name = fileDialog.getFile();
                if (name == null) {
                    return;
                }
                file.setFile(new File(name));
        }
            file.asciiSave();
         }

//        public boolean getEnabled() {
//            return( files.size() > 0 );
//        }
    }

    class SaveAsAction extends ChiselAction {

        SaveAsAction() {
            super();
        }

        public void actionPerformed(ActionEvent ev) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            ProcessedFileViewer viewer = getViewer(); /*null;
            for (Object source = ev.getSource(); source != null; source = ((Component) source).getParent()) {
                if (source instanceof ProcessedFileViewer) {
                    viewer = (ProcessedFileViewer) source;
                    break;
                }
            }*/
            if (viewer == null) {
                return;
            }

            ProcessedFile file = viewer.getProcessedFile();
            if (file == null) {
                System.out.println("Unable to save; no data.");
                return;
            }
            Frame frame = frontEnd.getFrame();
            if (fileDialog == null) {
                fileDialog = new FileDialog(frame);
            }
            fileDialog.setFile( file.generateSaveAsName() );
            fileDialog.setMode(FileDialog.SAVE);
            fileDialog.show();

            String name = fileDialog.getFile();
            if (name == null) {
                return;
            }
            String dir = fileDialog.getDirectory();
            if ( dir != null ) {
                name = dir + name;
            }

            System.out.println("Saving data to " + name + "...");
            // set File field in ProcessedFile
            File newfile = new File(name);
            file.setFile(newfile);
            frontEnd.setFile(file);
            file.asciiSave(); //(ProgressIndicator) frontEnd.statusbar);
        }
//        public boolean getEnabled() {
//            return( files.size() > 0 );
//        }
    }

    class GenhtmlAction extends ChiselAction {

        GenhtmlAction() {
            super();
        }

        public void actionPerformed(ActionEvent ev) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            ProcessedFileViewer viewer = getViewer(); /*null;
            for (Object source = ev.getSource(); source != null; source = ((Component) source).getParent()) {
                if (source instanceof ProcessedFileViewer) {
                    viewer = (ProcessedFileViewer) source;
                    break;
                }
            }*/
            if (viewer == null) {
                return;
            }

            ProcessedFile file = viewer.getProcessedFile();
            if (file == null) {
                System.out.println("Unable to save; no data.");
                return;
            }
            Frame frame = frontEnd.getFrame();
            if (fileDialog == null) {
                fileDialog = new FileDialog(frame);
            }
            String name = file.getNameWithoutPath();
            if (name == null) {
                return;
            }
            if ( name.indexOf( "." ) > 0 ) {
                name = name.substring( 0, name.indexOf( "." ));
            }
            name = name + ".chiseled.html";
            fileDialog.setFile( name );
            fileDialog.setMode(FileDialog.SAVE);
            fileDialog.show();
            name = fileDialog.getFile();
 
            String dir = fileDialog.getDirectory();
            if ( dir != null ) {
                name = dir + name;
            }

            System.out.println("Saving HTML report to " + name + "...");
            file.generateHtml( name, file.getUrl(), file.getOriginalName() );
        }
//        public boolean getEnabled() {
//            return( files.size() > 0 );
//        }
    }


    class GzipSaveAction extends ChiselAction {

        GzipSaveAction() {
            super();
        }

        public void actionPerformed(ActionEvent ev) {
            GlobalProgressIndicator.abortCurrentProcess = false;

            ProcessedFileViewer viewer = getViewer();
            if (viewer == null) {
                return;
            }

            ProcessedFile file = viewer.getProcessedFile();
            if (file == null) {
                System.out.println("Unable to save; no data.");
                return;
            }
            String name = file.getLocalName();
            if (name == null) {
                Frame frame = frontEnd.getFrame();
                if (fileDialog == null) {
                    fileDialog = new FileDialog(frame);
                }
                fileDialog.setFile( file.generateGzipName() );
                fileDialog.setMode(FileDialog.SAVE);
                fileDialog.show();

                name = fileDialog.getFile();
                if (name == null) {
                    return;
                }
                file.setFile(new File(name));
            }
            file.gzipSave();
        }

//        public boolean getEnabled() {
//            return( files.size() > 0 );
//        }
    }

    class GzipSaveAsAction extends ChiselAction {

        GzipSaveAsAction() {
            super();
        }

        public void actionPerformed(ActionEvent ev) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            ProcessedFileViewer viewer = getViewer(); /*null;
            for (Object source = ev.getSource(); source != null; source = ((Component) source).getParent()) {
                if (source instanceof ProcessedFileViewer) {
                    viewer = (ProcessedFileViewer) source;
                    break;
                }
            }*/
            if (viewer == null) {
                return;
            }

            ProcessedFile file = viewer.getProcessedFile();
            if (file == null) {
                System.out.println("Unable to save; no data.");
                return;
            }
            Frame frame = frontEnd.getFrame();
            if (fileDialog == null) {
                fileDialog = new FileDialog(frame);
            }
            fileDialog.setFile( file.generateGzipSaveAsName() );
            fileDialog.setMode(FileDialog.SAVE);
            fileDialog.show();

            String name = fileDialog.getFile();
            if (name == null) {
                return;
            }
            String dir = fileDialog.getDirectory();
            if ( dir != null ) {
                name = dir + name;
            }

            System.out.println("Saving data to " + name + "...");
            // set File field in ProcessedFile
            file.setFile(new File(name));
            file.gzipSave();
        }
//        public boolean getEnabled() {
//            return( files.size() > 0 );
//        }
    }
/*
    class X3dSaveAction extends ChiselAction {

        X3dSaveAction() {
            super();
        }

        public void actionPerformed(ActionEvent ev) {
            GlobalProgressIndicator.abortCurrentProcess = false;

            ProcessedFileViewer viewer = getViewer();
            if (viewer == null) {
                return;
            }

            ProcessedFile file = viewer.getProcessedFile();
            if (file == null) {
                System.out.println("Unable to save; no data.");
                return;
            }
            String name = file.getLocalName();
            if ((name == null) || ( name.indexOf( ".x3d" ) == -1 )){
                Frame frame = frontEnd.getFrame();
                if (fileDialog == null) {
                    fileDialog = new FileDialog(frame);
                }
                fileDialog.setFile( file.generateX3dName() );
                fileDialog.setMode(FileDialog.SAVE);
                fileDialog.show();

                name = fileDialog.getFile();
                if (name == null) {
                    return;
                }
                file.setFile(new File(name));
            }
            file.x3dSave();
        }

//        public boolean getEnabled() {
//            return( files.size() > 0 );
//        }
    }
*/
    class X3dSaveAsAction extends ChiselAction {

        X3dSaveAsAction() {
            super();
        }

        public void actionPerformed(ActionEvent ev) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            ProcessedFileViewer viewer = getViewer(); /*null;
            for (Object source = ev.getSource(); source != null; source = ((Component) source).getParent()) {
                if (source instanceof ProcessedFileViewer) {
                    viewer = (ProcessedFileViewer) source;
                    break;
                }
            }*/
            if (viewer == null) {
                return;
            }

            ProcessedFile file = viewer.getProcessedFile();
            if (file == null) {
                System.out.println("Unable to save; no data.");
                return;
            }
            Frame frame = frontEnd.getFrame();
            if (fileDialog == null) {
                fileDialog = new FileDialog(frame);
            }
            fileDialog.setFile( file.generateX3dSaveAsName() );
            fileDialog.setMode(FileDialog.SAVE);
            fileDialog.show();

            String name = fileDialog.getFile();
            if (name == null) {
                return;
            }
            String dir = fileDialog.getDirectory();
            if ( dir != null ) {
                name = dir + name;
            }

            System.out.println("Saving X3D data to " + name + "...");
            // set File field in ProcessedFile
            file.setFile(new File(name));
            file.x3dSave(); // MLo (called wrong save routine so gzipped vrml97 saved instead of X3D...)
        }
//        public boolean getEnabled() {
//            return( files.size() > 0 );
//        }
    }

    class ExitAction extends ChiselAction {

        ExitAction() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            for ( int i = 0; i < files.size(); i++ ) {
                ProcessedFile file = (ProcessedFile)files.elementAt( i );
                file.removeSerialFiles();
            }
            frontEnd.closeDown();
        }
    }

    class NexterrorAction extends ChiselAction {
        NexterrorAction() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            frontEnd.nextError();
        }
    }

    class PreverrorAction extends ChiselAction {
        PreverrorAction() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            frontEnd.prevError();
        }
    }


    class AboutAction extends ChiselAction {
        AboutAction() {
            super();
        }
        public void actionPerformed(ActionEvent e) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            Dialog aboutDialog = new AboutDialog(frontEnd, "About Chisel");
            aboutDialog.show();
        }
    }


    class OptionsAction extends ChiselAction {
        OptionsAction() {
            super();
        }
        public void actionPerformed(ActionEvent e) {
            GlobalProgressIndicator.abortCurrentProcess = false;
            OptionsDialog optionsDialog = new OptionsDialog(frontEnd);
            optionsDialog.setVisible(true);
            if (optionsDialog.hasChanged()) {
                saveContext(null);
                frontEnd.refresh();
            }
        }
    }

    class StopAction extends ChiselAction {
        StopAction() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            GlobalProgressIndicator.abortCurrentProcess = true;
            System.out.println( "Process aborted." );
        }
    }

    class ChiselCommandAction extends ChiselAction {
        ChiselCommandAction() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            GlobalProgressIndicator.abortCurrentProcess = false;

            System.out.println("Chisel command: " + e.getActionCommand());
            frontEnd.tables.actionPerformed(e);
        }
    }

    class ValidateAction extends ChiselCommandAction {
    }
    class CleanAction extends ChiselCommandAction {
    }
    class FormatAction extends ChiselCommandAction {
    }
    class CondenseAction extends ChiselCommandAction {
    }
    class ReduceAction extends ChiselCommandAction {
    }
    class ReorganizeAction extends ChiselCommandAction {
    }
    class MutateAction extends ChiselCommandAction {
    }




    /** a tracking class so the application can tell when a file has
        been closed. */

    class ViewerTracker implements WorkspaceListener {
        Container container;
        public ViewerTracker(Container container) {
            this.container = container;
        }
        public void windowOpened(WindowEvent evt) {
            //System.out.println("windowOpened");
        }
        public void windowClosing(WindowEvent evt) {
            //System.out.println("windowClosing");
        }
        public void windowClosed(WindowEvent evt) {
            //System.out.println("windowClosed");
            Component comp = evt.getComponent();
            // if a viewer is being closed, check to see if it is the
            // only viewer for that file.  If so, close it.
            if (comp instanceof ProcessedFileViewer) {
                ProcessedFile data = ((ProcessedFileViewer) comp).getProcessedFile();
                if (container != null) {
                    int count = container.getComponentCount();
                    for (int i = 0; i < count; i++) {
                        Component viewer = container.getComponent(i);
                        if (viewer != comp && viewer instanceof ProcessedFileViewer && ((ProcessedFileViewer) viewer).getProcessedFile() == data) {
                            // another viewer still has the file, fuhgeddaboutit
                            //System.out.println("Another viewer still has file");
                            return;
                        }
                    }
                    // that was the only one, close it
                    closeFile(data);
                }
            }
        }
        public void windowIconified(WindowEvent evt) {
            //System.out.println("windowIconified");
        }
        public void windowDeiconified(WindowEvent evt) {
            //System.out.println("windowDeiconified");
        }
        public void windowActivated(WindowEvent evt) {
            //System.out.println("windowActivated");
            Component comp = evt.getComponent();
            // if a viewer is being closed, check to see if it is the
            // only viewer for that file.  If so, close it.
            if (comp instanceof ProcessedFileViewer) {
                ProcessedFile data = ((ProcessedFileViewer) comp).getProcessedFile();

                frontEnd.setFile(data);
            }

        }
        public void windowDeactivated(WindowEvent evt) {
            //System.out.println("windowDeactivated");
        }
        public void windowMaximized(WindowEvent evt) {
            //System.out.println("windowMaximized");
            frontEnd.setMaximizedLook(true);
        }
        public void windowDemaximized(WindowEvent evt) {
            //System.out.println("windowDemaximized");
            frontEnd.setMaximizedLook(false);
        }
    }


    /** the Chisel's visible gui implementation.  It is hardcoded to create and
        manage the following components: a menu, a toolbar, a filetable window,
        a workspace window, and a status bar, contained within a standard gui
        frame.  It supports two orientations, HORIZONTAL and VERTICAL, which
        determine the way the filetable window and the workspace window are
        aligned.  The other components are always aligned vertically, i.e.
        menu and toolbar on top, status bar on bottom.
      */
      class bogusx {
      }
      static boolean firstTime = true;
    class FrontEnd extends GuiAdapter implements DialogOwner, ActionListener, KeyListener {

        int align;

        Frame frame;
        int sizediff = 0;
        int getHeight() {
            Rectangle rect = frame.getBounds();
            return( rect.height );
        }
        Object menubar;
        Container main;

        Container toolbar;
        Container divpane;  // contains filetable and workspace components
        Container statusbar;

        ChiselTableStack tables;
        ChiselWorkspace workspace;

        PopupViewer helpViewer;

        // show these when the viewer is maximized
        GlyphButton minButton = null;
        GlyphButton restoreButton;
        GlyphButton closeButton;

        public FrontEnd(Chisel app) {
            frame = ComponentFactory.createFrame(tryJFC, fullTitle);
            frame.addWindowListener(new AppCloser());

/*            if ( !noIcon ) {
                Image icon = ChiselResources.getImage("chisel");
                if (icon != null) {
                    frame.setIconImage(icon);            
                }
            }*/

            Insets insets = frame.insets();
            frame.setSize(insets.left + insets.right + 600, insets.top + insets.bottom + 500);

			main = ComponentFactory.createMainPanel(frame);
            main.setLayout(this);

            statusbar = ComponentFactory.createStatusBar(app);
            toolbar = ComponentFactory.createToolBar(app);

            align = DividerBarComponent.HORIZONTAL;
            ChiselSet sets[] = ChiselSet.createChiselSets();
            tables = new ChiselTableStack(sets,app);
            workspace = new ChiselWorkspace(tables);
            workspace.addWorkspaceListener(new ViewerTracker(workspace.getContainer()));

            Component tcomp = tables.getComponent();
            tcomp.setSize(tcomp.getPreferredSize());
            divpane = ComponentFactory.createDividedPane(align, tcomp, workspace.getComponent());

            //minButton = new GlyphButton(GlyphButton.MINIMIZE);
            //minButton.setForeground(Color.blue);
            //minButton.addActionListener(this);
            //minButton.setVisible(false);
            //main.add(minButton);
            restoreButton = new GlyphButton(GlyphButton.RESTORE);
            restoreButton.setForeground(Color.black);
            restoreButton.addActionListener(this);
            restoreButton.setVisible(false);
            main.add(restoreButton);
            closeButton = new GlyphButton(GlyphButton.CLOSE);
            closeButton.setForeground(Color.red);
            closeButton.addActionListener(this);
            closeButton.setVisible(false);
            main.add(closeButton);
            main.add(toolbar);
            main.add(divpane);
            main.add(statusbar);

            helpViewer = new PopupViewer(frame);
            Dimension helpSize = helpViewer.getPreferredSize();
            helpViewer.setSize(helpSize);
            // this strange step triggers code which positions the viewer nicely
            helpViewer.setLocation(-1000, 0);
            main.addKeyListener(this);
        }

        public void setErrorMarks( ProcessedFile processedFile, BitSet errorMarks, BitSet warningMarks, BitSet nonconformanceMarks ) {
            workspace.setErrorMarks( processedFile, errorMarks, warningMarks, nonconformanceMarks );
        }
        
        public void updateHeaderLine() {
            tables.updateHeaderLine();
        }
        
        // add or remove the unlock item from the main menu bar
        //private Menu unlockMenu = null;
        //public void showUnlockMenu(boolean show) {
        //    // first see if it is present.  It will be the last item if it's there
        //    MenuBar mb = frame.getMenuBar();
        //    int n = mb.getMenuCount();
        //    Menu m = mb.getMenu(n - 1);
        //    boolean present = (m == unlockMenu);
        //    if (present != show) {
        //        if (show) {
        //            if (unlockMenu == null) {
        //                unlockMenu = ComponentFactory.createMenu("unlockmenu");
        //            }
        //            mb.add(unlockMenu);
        //        } else {
        //            mb.remove(unlockMenu);
        //        }
        //    } 
        //}

        public void showHelp(boolean show) {
            if (!helpViewer.isVisible()) {
                Point pt = frame.getLocationOnScreen();
                Dimension helpSize = helpViewer.getPreferredSize();
                helpViewer.setSize(helpSize);

                if (helpViewer.getLocation().x + helpSize.width <= 0) {
        			Dimension size = frame.getSize();
                    helpViewer.setLocation(pt.x + size.width - helpSize.width - 4, pt.y + 2*(size.height - helpSize.height)/3);
                }
            }
            helpViewer.setVisible(show);
        }

        public void loadProperties(Properties props) {

            tables.loadProperties(props);

            Rectangle rect = frame.getBounds();
            String xstr = props.getProperty("frame.x");
            if (xstr != null) {
                try {
                    rect.x = Integer.parseInt(xstr);
                } catch (Exception e) {
                }
            }
            String ystr = props.getProperty("frame.y");
            if (ystr != null) {
                try {
                    rect.y = Integer.parseInt(ystr);
                } catch (Exception e) {
                }
            }
            String wstr = props.getProperty("frame.width");
            if (wstr != null) {
                try {
                    rect.width = Integer.parseInt(wstr);
                } catch (Exception e) {
                }
            }
            String hstr = props.getProperty("frame.height");
            if (hstr != null) {
                try {
                    rect.height = Integer.parseInt(hstr);
                } catch (Exception e) {
                }
            }
            frame.setBounds(rect);
            frame.validate();

        }

        public void saveProperties(Properties props) {

            tables.saveProperties(props);

			// put the frame bounds
			Rectangle rect = frame.getBounds();
            props.put( "frame.x",  String.valueOf(rect.x) );
            props.put( "frame.y",  String.valueOf(rect.y) );
            props.put( "frame.width",  String.valueOf(rect.width) );
            rect.height -= sizediff;
            props.put( "frame.height",  String.valueOf(rect.height) );
        }


        public void keyTyped(KeyEvent e) {}
    	public void keyReleased(KeyEvent e) {}
        public void keyPressed(KeyEvent e) {
            System.out.println("Chisel got keyPressed");
        }

        public void refresh() {
            main.invalidate();
            //toolbar.invalidate();
            workspace.getComponent().invalidate();
            //tables.getComponent().invalidate();
            main.validate();
        }


        /** start off the progress bar */
        public void startProgressBar(String text) {
            if (statusbar != null) {
                ProgressIndicator pi = (ProgressIndicator) statusbar;
                pi.setTitle(text);
                pi.reset();
                pi.setPercent(0);   // this gets the ball rolling
            }
        }

        public void setPercent(int n) {
            if (statusbar != null) {
                ProgressIndicator pi = (ProgressIndicator) statusbar;
                pi.setPercent(n);
            }
        }


        public void done(FactoryData result) {
            tables.done(result);
            workspace.done(result);
        }

        /** one of the window buttons fired */
    	public void actionPerformed( ActionEvent e ) {
    	    Object source = e.getSource();
    	    ProcessedFileViewer pfv = getViewer();
    	    if (pfv != null) {
        	    ChiselAWTViewer viewer = (ChiselAWTViewer) pfv.getComponent();
        	    if (source == minButton) {
        	        viewer.minimize(true);
        	    } else if (source == restoreButton) {
        	        viewer.maximize(false);
        	    } else if (source == closeButton) {
        	        viewer.close();
        	    }
        	}
    	}


        public void close(ProcessedFile file) {
            //tables.clearHeaderLine();
            if (workspace.contains(file)) {
                tables.close(file);
            }
                ProcessedFileViewer next = (ProcessedFileViewer) workspace.getSelection();
                if (next != null) {
                    tables.setProcessedFile(next.getProcessedFile());
                }
//            }
        }

        void show() {
            int origSize = getHeight();
            main.show(true);
//            frame.validate();
//System.out.println( "frame.show section.." );
            //frame.show(true);
            frame.validate();
/*            if ( firstTime ) {
                firstTime = false;
                try {
                    Thread.sleep( 3000 );
                } catch ( Exception e ) {
                }
            }*/
//if ( frame.getParent() != null ) System.out.println( "frame has parent" );
//if ( frame.getPeer() != null ) System.out.println( "frame has peer" );
//if ( frame.isVisible() ) System.out.println( "frame is visible" );
//else System.out.println( "frame is not visible" );
            frame.setVisible(true);
            int newSize = getHeight();
            sizediff = newSize - origSize;
//            System.out.println( "it returned..");
        }

        boolean viewerMaximized = false;
        boolean isViewerMaximized() {
            return viewerMaximized;
        }
        void maximizeViewer(boolean maximize) {
	        ProcessedFileViewer viewer = getViewer();
	        if (viewer instanceof ChiselAWTViewer) {
	            ((ChiselAWTViewer) viewer).maximize(maximize);
	        }
		}

        void setMaximizedLook(boolean maximized) {
            viewerMaximized = maximized;
            if (maximized) {
                String windowTitle = ((ChiselAWTViewer) getViewer().getComponent()).getTitle();
                frame.setTitle(fullTitle + " - " + windowTitle);
            } else {
                frame.setTitle(fullTitle);
            }

            //minButton.setVisible(maximized);
            restoreButton.setVisible(maximized);
            closeButton.setVisible(maximized);
        }

        // called when factory has finished its request
	    public void fileDone( ProcessedFile result ) {
//	        System.out.println("FE!.fileDone");
            workspace.fileDone(result);
	    }

        // called when data has changed
	    public void fileUpdated( ProcessedFile result ) {
//	        System.out.println("FE!.fileUpdate");
            workspace.fileUpdated(result);
	    }

	    public void paste() {
	        workspace.paste();
	    }

	    public void cut() {
	        workspace.cut();
	    }

	    public void copy() {
	        workspace.copy();
	    }

	    public void undo() {
	        workspace.undo();
	    }

	    public void redo() {
	        workspace.redo();
	    }

	    public void nextError() {
	        workspace.nextError();
	    }

	    public void prevError() {
	        workspace.prevError();
	    }

        public void open(ProcessedFile file) {
            tables.open(file);
            workspace.open(file);
            main.validate();
        }

        public void selectFile(String url) {
            workspace.setSelection(url);
            ProcessedFileViewer pfv = getViewer();
            if (pfv != null) {
                tables.setProcessedFile(pfv.getProcessedFile());
            } else {
                tables.setProcessedFile(null);
            }
            main.validate();
        }

        public void setFile(ProcessedFile file) {
            tables.setProcessedFile(file);
            workspace.setSelection(file);
            ProcessedFileViewer pfv = getViewer();
            if (pfv != null) {
                pfv.setProcessedFile(file);
            }
            setMaximizedLook(isViewerMaximized());

            main.validate();
        }

        protected Container getToolBar() {
            return toolbar;
        }

        public ProcessedFileViewer getViewer() {
            return workspace.getActiveViewer();
        }

        public ProcessedFile getFile() {
            ProcessedFileViewer pfv = getViewer();
            if (pfv != null) {
                return pfv.getProcessedFile();
            } else {
                return null;
            }
        }

        //protected Component getMenuBar() {
        //    return menubar;
        //}

        public ResourceBundle getResourceBundle() {
            return ChiselResources.getDefaultResourceBundle();
        }

        public Frame getFrame() {
            return frame;
        }

        protected ChiselWorkspace getWorkspace() {
            return workspace;
        }


        public void closeDown() {
            saveContext(null);
            frame.setVisible(false);
            frame.dispose();
            System.exit(0);
        }

        final class AppCloser extends WindowAdapter {
            public void windowClosing(WindowEvent e) {
                closeDown();
            }
        }

        /** determine the minimum size for the front end */
        public Dimension minimumLayoutSize(Container target) {
            Dimension dim = new Dimension(0, 0);

            if (menubar != null && menubar instanceof Component) {
                Component mb = (Component) menubar;
                if (mb.isVisible()) {
                    Dimension mbmin = mb.getMinimumSize();
                    dim.width = Math.max(mbmin.width, dim.width);
                    dim.height += mbmin.height;
                }
            }
            if (toolbar != null && toolbar.isVisible()) {
                Dimension tbmin = toolbar.getMinimumSize();
                dim.width = Math.max(tbmin.width, dim.width);
                dim.height += tbmin.height;
            }
            if (divpane != null && divpane.isVisible()) {
                Dimension divmin = divpane.getMinimumSize();
                dim.width = Math.max(divmin.width, dim.width);
                dim.height += divmin.height;
            }

            if (statusbar != null && statusbar.isVisible()) {
                Dimension sbmin = statusbar.getMinimumSize();
                dim.width = Math.max(sbmin.width, dim.width);
                dim.height += sbmin.height;
            }

            Insets insets = main.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;
            return dim;
        }

        /**
         * Determine the preferred size for the front end
         */
        public Dimension preferredLayoutSize(Container target) {
            Dimension dim = new Dimension(0, 0);

            if (menubar != null && menubar instanceof Component) {
                Component mb = (Component) menubar;
                if (mb.isVisible()) {
                    Dimension mbpref = mb.getPreferredSize();
                    dim.width = Math.max(mbpref.width, dim.width);
                    dim.height += mbpref.height;
                }
            }
            if (toolbar != null && toolbar.isVisible()) {
                Dimension tbpref = toolbar.getPreferredSize();
                dim.width = Math.max(tbpref.width, dim.width);
                dim.height += tbpref.height;
            }
            if (divpane != null && divpane.isVisible()) {
                Dimension divpref = divpane.getPreferredSize();
                dim.width = Math.max(divpref.width, dim.width);
                dim.height += divpref.height;
            }

            if (statusbar != null && statusbar.isVisible()) {
                Dimension sbpref = statusbar.getPreferredSize();
                dim.width = Math.max(sbpref.width, dim.width);
                dim.height += sbpref.height;
            }

            Insets insets = main.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;
            return dim;
        }

        /**
         * Return the maximum dimensions for the front end
         */
        public Dimension maximumLayoutSize(Container target) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        /**
         * Lays out the front end.  The components are fixed.
         */
        public void layoutContainer(Container target) {
            Rectangle rect = divpane.getBounds();
            if (target != main) {
                throw new IllegalArgumentException("FrontEnd cannot be called on arbitrary containers");
            }
            Insets insets = main.getInsets();
            Dimension maindim = main.getSize();
            int top = insets.top;
            int bottom = maindim.height - insets.bottom;
            int left = insets.left;
            int right = maindim.width - insets.right;

            if (menubar != null && menubar instanceof Component) {
                Component mb = (Component) menubar;
                if (mb.isVisible()) {
                    mb.setSize(right - left, mb.getSize().height);
                    Dimension d = mb.getPreferredSize();
                    mb.setBounds(left, top, right - left, d.height);
                    top += d.height;
                }
            }
            int tbheight = 0;
            if (toolbar != null && toolbar.isVisible()) {
                toolbar.setSize(right - left, toolbar.getSize().height);
                Dimension d = toolbar.getPreferredSize();
                toolbar.setBounds(left, top, right - left, d.height);
                top += d.height;
                tbheight = d.height;
            }

            // move buttons that appear when maximized
            // We have to lay them out even when they are not visible so they
            // will be in the right place when they are switched on.
            Dimension buttonsize;
            int hgap = 2;
            int vgap = 2;
            int x = right;
            int y = top - vgap - tbheight/2;
            if (closeButton != null) {
                buttonsize = closeButton.getPreferredSize();
                x -= hgap + buttonsize.width;
                closeButton.setBounds( x, y - buttonsize.height/2, buttonsize.width, buttonsize.height);
            }
            if (restoreButton != null) {
                buttonsize = restoreButton.getPreferredSize();
                x -= hgap + buttonsize.width;
                restoreButton.setBounds( x, y - buttonsize.height/2, buttonsize.width, buttonsize.height);
            }
            if (minButton != null) {
                buttonsize = minButton.getPreferredSize();
                x -= hgap + buttonsize.width;
                minButton.setBounds( x, y - buttonsize.height/2, buttonsize.width, buttonsize.height);
            }
            if (statusbar != null && statusbar.isVisible()) {
                statusbar.setSize(right - left, statusbar.getSize().height);
                Dimension d = statusbar.getPreferredSize();
                statusbar.setBounds(left, bottom - d.height, right - left, d.height);
                bottom -= d.height;
            }

            if (divpane != null && divpane.isVisible()) {
                divpane.setBounds(left, top, right - left, bottom - top);
            }
        }
    }

}

