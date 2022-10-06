/*
 * @(#)ChiselTableStack.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.util.GlobalProgressIndicator;
import com.trapezium.util.ProgressIndicator;
import com.trapezium.vrml.node.humanoid.HumanoidVisitor;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.factory.ParserFactory;
import com.trapezium.factory.FactoryResponseListener;
import com.trapezium.factory.FactoryChain;
import com.trapezium.factory.FactoryData;
import com.trapezium.edit.TokenEditor;
import com.trapezium.edit.EditLintVisitor;
import com.trapezium.edit.Document;
import com.trapezium.chisel.gui.TextLabel;
import com.trapezium.chisel.gui.TableRowComponent;
import com.trapezium.chisel.gui.WorkspaceListener;
import com.trapezium.chisel.gui.ScrollablePane;
import com.trapezium.chisel.gui.ComponentFactory;
import com.trapezium.chisel.gui.LabelledImageButton;
import com.trapezium.chisel.gui.ProgressBar;
import com.trapezium.chisel.gui.ChiselController;
import com.trapezium.chisel.gui.Slider;
import com.trapezium.chisel.gui.FontPool;
import com.trapezium.chisel.gui.SpinBox;
import com.trapezium.vrml.NodeSelection;

/** The ChiselTableStack is a main component of the Chisel front end.  It
 *  contains five components: file info window, validation control window,
 *  cleanup control window, optimizer control window, and save window.
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.1, 29 Jan 1998
 *
 *  @since           1.0
 */
public class ChiselTableStack extends ChiselPane implements ActionListener, ChiselRowListener {

    Vector files;           // ProcessedFile objects
    int selectedFile = -1;

    /** flag determining whether just the file name or the whole path is displayed */
    boolean showFullPath = true;

    /** set only when reformatting */
    boolean reformatting = false;
    boolean stripComments = false;

    /** Chisel program owner */
    Chisel chiselOwner;

	/** the chisels available to the program */
    ChiselSet sets[];

	ChiselSet validatorSet;

	/** the stacked tables */
	TopTable fileTable;
    ChiselTable tables[];

    /** the pane holding the tables */
    ChiselTablePane tablePane;

    /** this defaults to entry 0 in the table list */
	ChiselTable validationTable;

    /** this is entry 1 in the table list */
    ChiselTable formatTable;

    /** this defaults to entry 2 in the table list */
	ChiselTable cleanerTable;

    public ChiselTableStack(ChiselSet chiselSets[], Chisel chisel) {
        super();
        chiselOwner = chisel;

        files = new Vector();

        Container container = getContainer();
        ComponentFactory.setPaneBorder(container, NO_BORDER);
        container.setBackground(DEFAULT_TABLECOLOR.darker());

        fileTable = new TopTable(4, 1);
        fileTable.setExpandible(false);
        fileTable.setCollapsed(false);
        fileTable.getComponent().setBackground(DEFAULT_TOPTABLECOLOR);
        container.add(fileTable.getComponent());

        sets = chiselSets;
        int numsets = sets.length;

        tables = new ChiselTable[numsets];

        tablePane = new ChiselTablePane();

        for (int i = 0; i < numsets; i++) {
            int num = sets[i].getNumberChisels();
            tables[i] = new ChiselTable(2, num, this);
            setupTable(tables[i], sets[i].getCommand(), sets[i].getTooltip(), sets[i].getInitialCollapsedValue(), sets[i].getGreenable());
            setChiselSet(tables[i], sets[i]);
            ChiselSet.setProgressIndicator( i, tables[i] );

            // set specialized listener if there is one
            for (int j = 0; j < num; j++) {
                int listener = sets[i].getEntry(j).getSpecializedListener();
                if (listener != -1) {
                    ChiselSet.setSpecializedListener( listener, (ChiselRow)tables[i].getRow( j ) );
                }
            }
            tablePane.add(tables[i].getComponent());
        }
        container.add(tablePane);

        // validation, format and cleanup are special
        validatorSet = sets[0];
        validationTable = tables[0];
        formatTable = tables[1];
        cleanerTable = tables[2];
        resetAllTables( false );
        resetAllTables( true );

        empty();
    }

    /** Set up a chisel group.
     *
     *  @param table gui element
     *  @param command
     *  @param tip tip that appears when mouse is over
     *  @param collapsed true if initially collapsed, otherwise false
     *  @param greenable true if turns green, otherwise false
     */
    private void setupTable(ChiselTable table, String command, String tip, boolean collapsed, boolean greenable) {
        table.setGreenable(greenable);
        LabelledImageButton runButton = new LabelledImageButton(command, null, LabelledImageButton.TEXT_ALIGN_CENTER | LabelledImageButton.TEXT_ALIGN_VCENTER); //ASCENT);
        runButton.setAccess(true);
        runButton.addActionListener(this);
        runButton.setTip(tip);

        table.setExpandible(true);
        table.setCollapsed(collapsed);

        table.getHeader().setColumn(0, runButton);
        table.getHeader().setColumn(1, new ProgressBar());
    }

    /** Load visual properties */
    public void loadProperties(Properties props) {
        for (int i = 0; i < tables.length; i++) {
            boolean collapsed = false;
            String str = props.getProperty("table." + i + ".expanded");
            tables[i].setCollapsed( !("true".equalsIgnoreCase(str)) );
        }
        int num = formatTable.getNumberRows();
        for (int i = 0; i < num; i++) {
   			ChiselRow row = (ChiselRow) formatTable.getRow( i );
   			if (i == 0) {
       			String str = props.getProperty( "format.stripComments" );
       			if ( str != null ) {
           			row.setEnabled( "true".equalsIgnoreCase( str ));
                    ChiselController cc = row.getChiselController();
                    cc.setValue( str );
           		}
       	    } else {
       			FormatOptions option = (FormatOptions) row.getChisel().getOptionHolder();
       	        String str = props.getProperty( option.getPropertyName() + "Enabled" );
       	        if ( str != null ) {
                    ChiselController cc = row.getChiselController();
                    cc.setValue( str );
           	        row.setEnabled( "true".equalsIgnoreCase( str ));
                    str = props.getProperty( option.getPropertyName() );
                    if ( str != null ) {
                        option.setOptionValue( 0, str );
                        ChiselDescriptor cd = row.getChisel();
                        Object xxx = cd.getOption( 0 );
                        if ( xxx instanceof ChiselController ) {
                            cc = (ChiselController)xxx;
                            Component ccc = cc.getComponent( 0 );
                            if ( ccc instanceof SpinBox ) {
                                ((SpinBox)ccc).setValue( str );
                            }
                        }
                    }
                }
       	    }
            formatTable.updateState( i, row.isEnabled() );              
            formatTable.setBaseState();
        }
        LabelledImageButton button = (LabelledImageButton) formatTable.getHeader().getColumn(0);
        button.setSignal( false );
        num = validationTable.getNumberRows();
        for (int i = 0; i < num; i++) {
   			ChiselRow row = (ChiselRow) validationTable.getRow( i );
   			String str = props.getProperty( "validate.v" + i );
   			if ( str != null ) {
      			row.setEnabled( "true".equalsIgnoreCase( str ));
                ChiselController cc = row.getChiselController();
                cc.setValue( str );
          	}
            validationTable.updateState( i, row.isEnabled() );              
            validationTable.setBaseState();
        }
        button = (LabelledImageButton) validationTable.getHeader().getColumn(0);
        button.setSignal( false );
        Component comp = getComponent();
        Dimension size = comp.getSize();
        String wstr = props.getProperty("tables.width");
        if (wstr != null) {
            try {
                size.width = Integer.parseInt(wstr);
            } catch (Exception e) {
            }
        }
        String hstr = props.getProperty("tables.height");
        if (hstr != null) {
            try {
                size.height = Integer.parseInt(hstr);
            } catch (Exception e) {
            }
        }
        comp.setSize(size);
    }

    /** Save visual properties */
    public void saveProperties(Properties props) {
        for (int i = 0; i < tables.length; i++) {
            props.put( "table." + i + ".expanded",  String.valueOf(!tables[i].isCollapsed()) );
        }
        Component comp = getComponent();
        Dimension size = comp.getSize();
        props.put( "tables.width",  String.valueOf(size.width) );
        props.put( "tables.height",  String.valueOf(size.height) );
        int num = formatTable.getNumberRows();
        for (int i = 0; i < num; i++) {
   			ChiselRow row = (ChiselRow) formatTable.getRow( i );
   			if (i == 0) {
       			props.put( "format.stripComments", String.valueOf( row.isEnabled()));
       	    } else {
       			FormatOptions option = (FormatOptions) row.getChisel().getOptionHolder();
       	        props.put( option.getPropertyName() + "Enabled", String.valueOf( row.isEnabled() ));
       			props.put( option.getPropertyName(), (String)option.getOptionValue(0) );
       	    }
        }
        num = validationTable.getNumberRows();
        for ( int i = 0; i < num; i++ ) {
            ChiselRow row = (ChiselRow)validationTable.getRow( i );
            props.put( "validate.v" + i, String.valueOf( row.isEnabled() ));
        }
    }

    /** How many consecutive times the CLEANERS have automatically been called */
    static final int MAX_AUTO_CLEAN = 5;

    /** Called when a chisel is enabled or disabled (ChiselRowListener interface) */
    public void rowStateChanged(ChiselRow row) {
        ChiselTable table = row.getOwner();
        if ( table.isGreenable() ) {
            boolean sig = false;
            LabelledImageButton button = (LabelledImageButton) table.getHeader().getColumn(0);
            int numrows = table.getNumberRows();
            for (int i = 0; i < numrows; i++) {
                ChiselRow r = (ChiselRow) table.getRow(i);
                table.updateState( i, r.isEnabled() );              
//        	    if (r.isEnabled()) {
//                    sig = true;
//                    break;
//                }
            }
            button.setSignal( table.stateChanged() );
//            button.setSignal(sig);
        }
    }

    /** Called when someone clicks on one of the Chisel categories */
    boolean validateOnly = false;  // prevents unnecessary serializing
   	public void actionPerformed( ActionEvent e ) {
        String command = e.getActionCommand();
        doCommand( command );
    }

    void doCommand( String command ) {
   	    if ( !ChiselSet.chiselsEnabled() ) {
   	        System.out.println( "Processing underway, unable to " + command );
   	        return;
   	    }
   	    GlobalProgressIndicator.abortCurrentProcess = false;
        ProcessedFile theFile = getProcessedFile();
        if (theFile == null) {
            return;     // no action if there isn't a file
        }
        ChiselSet.disableChisels();
        Chisel.singletonChisel.refresh();

        int category = 0;
        for (int i = 0; i < sets.length; i++) {
            if (sets[i].getCommand().equalsIgnoreCase(command)) {
               category = i;
               break;
            }
        }
        theFile.enableParse();
        theFile.setPreviousNumberErrors();
        switch (category) {
            case ChiselSet.VALIDATORS:
                // validation only
                validationTable.setBaseState();
                System.out.println("Validating " + theFile.getName() + "...");
                removeFactories( theFile );
                validateOnly = true;
                queueValidators(true);
                processFile();
                break;

            case ChiselSet.FORMATTER:
                formatTable.setBaseState();
                System.out.println( "Formatting " + theFile.getName() + "..." );
                removeFactories( theFile );
                theFile.disableChisel( "TokenStreamFactory" );
                ParserFactory.hackTest = true;
//                theFile.disableChisel( "ParserFactory" );
                reformatting = true;
                int num = formatTable.getNumberRows();
                for (int i = 0; i < num; i++) {
        			ChiselRow row = (ChiselRow) formatTable.getRow( i );
        			if (i == 0) {
            			stripComments = row.isEnabled();
            	    } else {
            			FormatOptions option = (FormatOptions) row.getChisel().getOptionHolder();
            			option.runOption( row.isEnabled() );
            	    }
                }
                processFile();
                break;

            default:
                validateOnly = false;
                // validate, optimize, validate
                runChisels( category );
                break;
		}
	}


    void runChisels( int category ) {
        runChisels( category, true );
    }
    
    void runChisels( int category, boolean resetStartTime ) {
        ProcessedFile theFile = getProcessedFile();
        theFile.setCleanCount( 1 );
        theFile.disableChisel( "TokenStreamFactory" );
        if ( queueChisels(category) ) {
            System.out.println("Processing " + theFile.getName() + "...");
            processFile( resetStartTime );
        } else {
            ChiselSet.enableChisels();
        }
    }

    void setChiselSet(ChiselTable table, ChiselSet chiselSet) {
        int numchisels = (chiselSet == null) ? 0 : chiselSet.getNumberChisels();
        if (numchisels > table.getNumberRows()) {
            table.setNumberRows(numchisels, this);
        }
        int numrows = table.getNumberRows();
        boolean autocheck = chiselSet.isAutomaticallyChecked();
        for (int r = 0; r < numrows; r++) {
			ChiselRow row = (ChiselRow) table.getRow( r );
            if (r < numchisels) {
    			row.setChisel(chiselSet.getEntry( r ), autocheck);
    	    } else {
    			row.setChisel(null, false);
            }
            table.updateState( r, row.isEnabled() );
	    }
	    table.setBaseState();
    }


    /** display an entry in the file list */
    public void open(int index) {
        ProcessedFile data = (index >= 0 && index < files.size()) ? (ProcessedFile) files.elementAt(index) : null;
        if (data != null) {
            open(data);
        }
    }

	/** display an ProcessedFile in the ChiselTable.  Add it to the open file list
	 *  if it isn't there already.
	 */
    public void open(ProcessedFile data) {
        if (!files.contains(data)) {
            files.addElement(data);
			ChiselRow row = (ChiselRow) validationTable.getRow( 0 );
			data.setParsingListener( row );
        }
        setProcessedFile(data);
    }


    /** private method called by queueXxx methods to wipe the slate clean
     *  before adding new factories
     */
	private void removeFactories(ProcessedFile theFile) {
        // skip the first entry (validators)
        int numsets = sets.length;
        for (int i = 1;	i < numsets; i++)  {
            theFile.removeFactory( sets[i].getCommand() );
        }
    }

    /** Queue the validator chisels for the ProcessedFile. */
    void queueValidators( boolean enable ) {
        ProcessedFile theFile = getProcessedFile();
        theFile.synchTokenEditor();
        int numvalidators = validatorSet.getNumberChisels();

		for ( int i = 0; i < numvalidators; i++ ) {
			ChiselRow row = (ChiselRow) validationTable.getRow( i );
			if ( !row.isEnabled() ) {
			    if ( enable ) {
			        if ( i == 0 ) {
    			        Chisel.unloadHumanoid();
    			    } else if ( i == 1 ) {
    			        VrmlElement.disableBaseProfile = false;
    			    } else if ( i == 2 ) {
    			        VrmlElement.nowarning = false;
    			    } else if ( i == 3 ) {
    			        VrmlElement.noUnusedDEFwarning = false;
    			    }
			    }
			}
			if (row.isEnabled() ) {
			    if ( enable ) {
			        if ( i == 0 ) {
    			        Chisel.loadHumanoid();
    			    } else if ( i == 1 ) {
    			        VrmlElement.disableBaseProfile = true;
    			    } else if ( i == 2 ) {
    			        VrmlElement.nowarning = true;
    			    } else if ( i == 3 ) {
    			        VrmlElement.noUnusedDEFwarning = true;
    			    }
			    }
//	            removeFactories(theFile);
///   			    theFile.enableChisel( "TokenStreamFactory", row );
//           		theFile.enableChisel( row.getChisel().getClassName(), row );
           	// never disable the ParserFactory, have to run it after any other category
           	// so that scene gets re-created, as well as verify token stream
	        } else if ( row.getChisel().getClassName().compareTo( "ParserFactory" ) != 0 ) {
   	            theFile.disableChisel( row.getChisel().getClassName() );
	        } else if ( !theFile.isDirty() ) {
	            theFile.disableChisel( "TokenStreamFactory" );
	        }
	    }
    }

    /** Queue a category of chisels for running.
     *
     *  @param whichSet possible values ChiselSet.VALIDATORS=0, etc
     */
    boolean queueChisels(int whichSet) {
        ProcessedFile theFile = getProcessedFile();

        ChiselSet set = sets[whichSet];
        ChiselTable table = tables[whichSet];
        table.setBaseState();

        if ( theFile.hasErrors() ) {
            System.out.println( "Warning, file has errors!" );
        } else {
            System.out.println( "queueChisels(" + whichSet + ") for id " + theFile.getId() );
        }
        int numchisels = set.getNumberChisels();
        FactoryChain factory = null;
  		for ( int i = 0; i < numchisels; i++ ) {
   			ChiselRow row = (ChiselRow) table.getRow( i );
   			if (row.isEnabled()) {
   			    if ( factory == null ) {
   			        //
   			        // This is an attempt to handle overlapping chisels, create a
   			        // FactoryChain for the ChiselRow, with a single ChiselFactory
   			        // element.  Whenever an activated chisel requires a new factory
   			        // another chisel factory gets created (this part not done yet)
   			        factory = new FactoryChain( whichSet );
   			        factory.setFactoryName( set.getCommand() );
   			        factory.setFactoryTitle( "Processing... " );
                    removeFactories(theFile);
   			        theFile.insertFactory( factory );
                    ChiselRow headerRow = (ChiselRow)tables[whichSet].getRow( 0 );
                    factory.setListener( headerRow );
   			    }
   			    factory.addChisel( row.getChisel().getClassName(),
   			        row.getChisel().getChiselType(), theFile.getParserFactory(),
   			        theFile.getBaseFilePath(), theFile.getNameWithoutPath(), row );
   			}
        }
        return( factory != null );
    }

    void processFile() {
        processFile( true );
    }
    
    void processFile( boolean resetStartTime ) {
        ProcessedFile theFile = getProcessedFile();
   		theFile.load( resetStartTime );
    }


	/** remove the current ProcessedFile from the ChiselTable.
	 */
    public void close() {
        if (selectedFile < 0) {
            return;
        }

        ProcessedFile data = (ProcessedFile) files.elementAt(selectedFile);
        close(data);
    }

    /* remove a particular ProcessedFile */
    public void close(ProcessedFile data) {
//        clearHeaderLine();
//        System.out.println( "Before close " + files.size() + " files" );
        files.removeElement(data);
        if (selectedFile >= files.size()) {
            selectedFile = files.size() - 1;
        }
        if (selectedFile >= 0) {
            open(selectedFile);
        } else {
            clearHeaderLine();//empty();
        }
//        System.out.println( "After close " + files.size() + " files" );
        getComponent().repaint();
    }

    /** update the display if the object belongs to this viewer or is null */
    //public void fileUpdated(ProcessedFile data) {
    //    System.out.println("CTS!!!.fileUpdated");
    //}

    /** load an object into the viewer */
    //public void fileDone(ProcessedFile data) {
    //    System.out.println("CTS!!!.fileDone");
    //}

    /** Called when a file has been processed, keeps repeating CLEANERS
     *  until there is nothing checked (or has hit repeat limit)
     */
    public void done(FactoryData result) {
        boolean allDone = false;
        int finalVersion = 0;
        ProcessedFile processedFile = null;
        boolean flagSet = false;
        if (result instanceof ProcessedFile) {
            processedFile = (ProcessedFile)result;
            Date endTime = new Date();
            Date startTime = processedFile.getStartTime();
            if ( reformatting ) {
                System.out.println( "" );
            } else {
                if ( result.errorsCreated() ) {
                    System.out.println( "Warning: " + result.getNumberErrors() + " errors created" );
                } else {
                    System.out.println( "Elapsed time: " + ((endTime.getTime() - startTime.getTime())/1000 ) + " seconds" );
                }
            }
            flagSet = processedFile.updateInfo();
            int cleanCount = processedFile.getCleanCount();
            updateHeaderLine( processedFile );
            if ( NodeSelection.getSingleton().hasSelection() ) {
                NodeSelection.getSingleton().reset();
                flagSet = false;
            }
            if ( !processedFile.isNewlyOpened() ) {
//                System.out.println( "GlobalProgressIndicator.abortCurrentProcess " + GlobalProgressIndicator.abortCurrentProcess );
//                System.out.println( "!validateOnly " + !validateOnly );
//                System.out.println( "flagset " + flagSet );
//                System.out.println( "Chisel.autoClean " + Chisel.autoClean );
//                System.out.println( "!cleanerTable.anyRowDisabled() " + !cleanerTable.anyRowDisabled() ) ;

                 if ( !GlobalProgressIndicator.abortCurrentProcess && !validateOnly && flagSet && Chisel.autoClean && !cleanerTable.anyRowDisabled() ) {
                    cleanCount++;
                    processedFile.setCleanCount( cleanCount );
                    if ( cleanCount < MAX_AUTO_CLEAN ) {
                        runChisels( ChiselSet.CLEANERS, false );
                    } else {
                        allDone = true;
                    }
                } else {
                    TokenEnumerator tokenEnumerator = result.getLineSource();
//                    System.out.println( "Finished sequence, size: " + tokenEnumerator.getFileDataIdx() + " bytes" );
                    allDone = true;
                    finalVersion = processedFile.getVersion() + 1;
                }
            } else {
                processedFile.notNewlyOpened();
                allDone = true;
                finalVersion = 1;
            }
        }
        if ( allDone ) {
            resetAllTables( reformatting );
            cleanerTable.resetRowDisabled();
            Scene s = processedFile.getScene();
    		if ( reformatting ) {
    		    if ( processedFile.getTokenEditor() != s.getTokenEnumerator() ) {
    		        System.out.println( "processedFile and scene have different token enumerators" );
    		    }
//        	    s.setTokenEnumerator( processedFile.getTokenEditor()  );
        	    if ( !GlobalProgressIndicator.abortCurrentProcess ) {
            	    TokenEditor newDataSource = remakeDataSource( s, processedFile.getDocument(), processedFile );
            	    s.setTokenEnumerator( newDataSource );
           	        processedFile.setTokenEditor( newDataSource );
                    Date endTime = new Date();
                    Date startTime = processedFile.getStartTime();
                    System.out.println( "Elapsed time: " + ((endTime.getTime() - startTime.getTime())/1000 ) + " seconds" );
               	}
            }
       		BitSet errorMarks = new BitSet( Slider.ErrorMarkCount );
       		BitSet warningMarks = new BitSet( Slider.ErrorMarkCount );
       		BitSet nonconformanceMarks = new BitSet( Slider.ErrorMarkCount );
       		processedFile.mergeErrors( errorMarks, warningMarks, nonconformanceMarks );
          	chiselOwner.setErrorMarks( processedFile, errorMarks, warningMarks, nonconformanceMarks );
            LabelledImageButton cleanButton = (LabelledImageButton) cleanerTable.getHeader().getColumn(0);
            //cleanButton.setSignal(flagSet);
            if ( !validateOnly && !GlobalProgressIndicator.abortCurrentProcess ) {
                processedFile.setVersion( finalVersion );
            }
            processedFile.setValidated( true );
            ChiselSet.enableChisels();
            NodeSelection.getSingleton().reset();
            // stripping comments changes token offsets, have to revalidate
            if ( reformatting && stripComments ) {
                reformatting = false;
                doCommand( "VALIDATE" );
            }
            reformatting = false;
            Chisel.singletonChisel.refresh();
    	    Runtime.getRuntime().gc();
        }
    }
    
    void resetAllTables() {
        resetAllTables( false );
    }
    
    void resetAllTables( boolean reformatting ) {
        if ( reformatting ) {
            LabelledImageButton button = (LabelledImageButton) formatTable.getHeader().getColumn(0);
            button.setSignal( formatTable.stateChanged() );
        } else {
            for ( int i = 3; i < tables.length; i++ ) {
                ChiselTable t = tables[i];
                t.resetAllRows();
            }
            for ( int i = 0; i < tables.length; i++ ) {
                ChiselTable t = tables[i];
                if ( t == cleanerTable ) {
                    t.clearBaseState();
                } else {
                    t.setBaseState();
                }
                LabelledImageButton bt = (LabelledImageButton)t.getHeader().getColumn( 0 );
                bt.setSignal( t.stateChanged() );
            }
            LabelledImageButton button = (LabelledImageButton) validationTable.getHeader().getColumn(0);
            button.setSignal( validationTable.stateChanged() );
        }
    }

    /** Remake TokenEnumerator as reformatted text */
    TokenEditor remakeDataSource( Scene scene, Document docInterface, ProcessedFile processedFile ) {
        TokenEnumerator sceneTokens = scene.getTokenEnumerator();
        TokenEditor dataSource = new TokenEditor( sceneTokens );
        if ( sceneTokens instanceof TokenEditor ) {
            TokenEditor sceneTokenEditor = (TokenEditor)sceneTokens;
            sceneTokenEditor.disableLineInfo();
        }
        if ( stripComments ) {
            sceneTokens.enableCommentSkipping();
        } else {
            sceneTokens.disableCommentSkipping();
        }
		if ( Chisel.humanoid != null ) {
			HumanoidVisitor hv = new HumanoidVisitor( Chisel.humanoid, sceneTokens );
			scene.traverse( hv );
		}
        ProgressIndicator pl = ChiselSet.getProgressIndicator( ChiselSet.FORMATTER );
        if ( pl != null ) {
            pl.reset();
            pl.setTitle( "Reformatting..." );
        }

//        Chisel.disableTextWindowPaint = true;
        sceneTokens.notifyLineNumbers( pl );
        TokenPrinter tp = new TokenPrinter( sceneTokens, dataSource );

        // the data sink gets formatted text
        tp.doPrettyPrint();

        // generate the new token enumerator
        tp.printRange( scene.getFirstTokenOffset(), scene.getLastTokenOffset(), true );
        if ( pl != null ) {
            pl.reset();
        }
        sceneTokens.enableCommentSkipping();

        // replace the scene token enumerator so errors can be located visually
        // first send lines over to editor, then make a new TokenEditor from that
        docInterface.setLines( dataSource );
        docInterface.setDocumentLoader( processedFile );

        EditLintVisitor lv = new EditLintVisitor( dataSource );
        scene.traverse( lv );
        processedFile.setLintInfo( lv );
        return( dataSource );
    }



	void setRowInfo(ChiselRow cr, boolean flag) {
        cr.rowReady();
        ChiselController cc = cr.getChiselController();
        cr.setEnabled( flag );
        cc.setValue( String.valueOf( flag ));
        cc.repaint();
    }

    /** save the current document.  Prompts for filename if the document was not loaded
        from disk or previously saved. */
    //public void save() {
    //}

    /** remove the current document from the viewer and display an empty document */
    public void empty() {
        TableRow ftrow = fileTable.getHeader();
        ftrow.setColumn( 0, new PathLabel(""));
		ftrow.setColumn( 1, new TextLabel(""));
		ftrow.setColumn( 2, new TextLabel(""));
		ftrow.setColumn( 3, new TextLabel(""));
    }

    public void updateHeaderLine() {
        ProcessedFile pf = getProcessedFile();
        if ( pf != null ) {
            //updateHeaderLine( pf );
            setProcessedFile( pf );
        } else {
            clearHeaderLine();
        }
    }
    
    /** get the ProcessedFile being viewed */
    public ProcessedFile getProcessedFile() {
        if (selectedFile >= 0) {
            return (ProcessedFile) files.elementAt(selectedFile);
        } else {
            return null;
        }
    }

    /** set the ProcessedFile being viewed */
    public void setProcessedFile(ProcessedFile data) {
        if (selectedFile >= 0) {
            ProcessedFile old = (ProcessedFile) files.elementAt(selectedFile);
            // if the file is currently selected, there's nothing to do
            
            // This has to be commented out, otherwise, switching windows
            // by closing files doesn't change display (don't know why yet)
            //if (old == data) {
              //  return;
            //}
        }
        if ( data == null ) {
            return;
        }
        selectedFile = files.indexOf(data);
        if (selectedFile < 0) {
            return;
        }

        File file = data.getFile();
        if (file == null) {
            file = new File(data.getNameWithoutVersion());
        }

        String name;
        if (showFullPath) {
            try {
                name = file.getCanonicalPath();
            } catch (Exception e) {
                name = file.getAbsolutePath();
            }
        } else {
            name = file.getName();
        }

        long flen = file.length();
        String length;
        if (flen >= 1024) {
            length = String.valueOf((flen + 512) / 1024) + "KB";
        } else if (flen == 1) {
            length = "1 byte";
        } else {
            length = flen + " bytes";
        }
        ChiselRow row = (ChiselRow)validationTable.getRow( 0 );
	    row.setFileLength( file.length() );

        String readonly = file.canWrite() ? "" : "R";
        String dir = file.isDirectory() ? "D" : "";

        DateFormat dateFormat = DateFormat.getInstance();
        String date = dateFormat.format(new Date(file.lastModified()));

        TableRow ftrow = fileTable.getHeader();
        Component c = ftrow.getColumn(0);
        if (c == null) {
            ftrow.setColumn( 0, new PathLabel(name) );
        } else {
            ((PathLabel)c).setText(name);
        }
        c = ftrow.getColumn(1);
        if (c == null) {
            ftrow.setColumn( 1, new TextLabel(readonly + dir) );
        } else {
            ((TextLabel)c).setText(readonly + dir);
        }
        c = ftrow.getColumn(2);
        if (c == null) {
            TextLabel lengthlabel = new TextLabel(length);
            lengthlabel.setAlign(TextLabel.TEXT_ALIGN_RIGHT | TextLabel.TEXT_ALIGN_VCENTER);
            ftrow.setColumn( 2, lengthlabel );
        } else {
            ((TextLabel)c).setText(length);
        }
        c = ftrow.getColumn(3);
        if (c == null) {
            TextLabel datelabel = new TextLabel(date);
            datelabel.setAlign(TextLabel.TEXT_ALIGN_RIGHT | TextLabel.TEXT_ALIGN_VCENTER);
            ftrow.setColumn( 3, datelabel );
        } else {
            ((TextLabel)c).setText(date);
        }
//        updateSizePercent( data );
        //ftrow.getComponent().repaint();

        // update the info in the rows
        boolean needSignal = data.updateInfo(); //updateInfo(data);
        updateHeaderLine( data );
        LabelledImageButton cleanButton = (LabelledImageButton) cleanerTable.getHeader().getColumn(0);
        cleanButton.setSignal(needSignal);

        // clear the status bar
        System.out.println("");
   }

   void clearHeaderLine() {
        TableRow ftrow = fileTable.getHeader();
        for ( int i = 0; i < 4; i++ ) {
            Component c = ftrow.getColumn(i);
            if ( c instanceof TextLabel ) {
                ((TextLabel)c).setText( "" );
            }
        }
        ftrow = fileTable.getRow( 0 );
        Component c = ftrow.getColumn( 1 );
        if ( c instanceof TextLabel ) {
            ((TextLabel)c).setText( "" );
        }
        c = ftrow.getColumn( 3 );
        if ( c instanceof TextLabel ) {
            ((TextLabel)c).setText( "" );
        }
        ProgressIndicator pi = ChiselSet.getProgressIndicator( ChiselSet.VALIDATORS );
        pi.setText( "" );
   }

   /** Update the header line for a file */
   void updateHeaderLine( ProcessedFile data ) {
        if ( data.isGzipped() ) {
            TableRow ftrow = fileTable.getHeader();
            Component c = ftrow.getColumn(2);
            if (c instanceof TextLabel) {
                String s = ((TextLabel)c).getText();
                if ( s.indexOf( "gzipped" ) == -1 ) {
                    ((TextLabel)c).setText(s + " gzipped");
                }
                c.invalidate();
            }
        } 
	    ProgressIndicator owner = ChiselSet.getProgressIndicator( ChiselSet.VALIDATORS );
	    owner.reset();
	    owner.setText( data.getErrorSummaryString() );
        TableRow ftrow = fileTable.getRow( 0 );
        String s = new String( data.getSizeReductionPercent() + "%" );
		Component c = ftrow.getColumn( 1 );
		if ( c == null ) {
		    ftrow.setColumn( 1, new TextLabel( s ));
		} else if ( c instanceof TextLabel ) {
		    ((TextLabel)c).setText( s );
	    } else {
		    System.out.println( "C is " + c.getClass().getName() );
		}
		c = ftrow.getColumn( 3 );
		if ( c == null ) {
		    ftrow.setColumn( 3, new TextLabel( data.getPolygonCount() + "" ));
		} else if ( c instanceof TextLabel ) {
	        ((TextLabel)c).setText( data.getPolygonCount() + "" );
		} else {
		}
   }

    /** dump the current document to the console */
    public void dump() {
    }

    /** determine the minimum size */
    public Dimension minimumLayoutSize(Container target) {
        Insets insets = target.getInsets();
        Dimension dim = new Dimension(insets.left + insets.right, insets.top + insets.bottom);
        int numkids = target.getComponentCount();
        for (int i = 0; i < numkids; i++) {
            Component kid = target.getComponent(i);
            Dimension min = kid.getMinimumSize();
            dim.height += min.height;
            int minw = min.width + insets.left + insets.right;
            if (dim.width < minw) {
                dim.width = minw;
            }
        }
        return dim;
    }

    /** determine the preferred size */
    public Dimension preferredLayoutSize(Container target) {
        Insets insets = target.getInsets();
        Dimension dim = new Dimension(insets.left + insets.right, insets.top + insets.bottom);
        int numkids = target.getComponentCount();
        for (int i = 0; i < numkids; i++) {
            Component kid = target.getComponent(i);
            Dimension pref = kid.getPreferredSize();
            dim.height += pref.height;
            int prefw = pref.width + insets.left + insets.right;
            if (dim.width < prefw) {
                dim.width = prefw;
            }
        }
        return dim;
    }

    /** determine the maximum size.  By default this is infinite. */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /** lay out the component in a vertical stack. */
    public void layoutContainer(Container target) {

        Insets insets = target.getInsets();
        Dimension size = target.getSize();

        int x = insets.left;
        int y = insets.top;
        int width = size.width - insets.left - insets.right;
        int height = size.height - insets.top - insets.bottom;

        Component ftComp = fileTable.getComponent();
        int ftHeight = ftComp.getPreferredSize().height;
        ftComp.setBounds(x, y, width, ftHeight);
        tablePane.setBounds(x, y + ftHeight, width, height - ftHeight);
    }

	/** the top table contains file information */
	class TopTable extends ChiselTable {

        public TopTable(int numcolumns, int numrows) {
            super(numcolumns, numrows, null);
            setExpandible(false);
        }
        public TableRow createHeader(int columns, Insets insets, Object data) {
            TableRow row = super.createHeader(columns, insets, data);

            // set a minimum width for the right two columns
            TableRowComponent rowcomp = (TableRowComponent) row.getComponent();
            FontMetrics fm = rowcomp.getFontMetrics(rowcomp.getFont());

            rowcomp.setColumnWidth(0, 32 * fm.charWidth('x'));
            rowcomp.setColumnVaries(0, true);

            rowcomp.setColumnWidth(1, fm.stringWidth("RD"));
            rowcomp.setColumnVaries(1, false);

            // the file size column
            rowcomp.setColumnWidth(2, fm.stringWidth("999999KB"));
            rowcomp.setColumnVaries(2, true);

            // the file date column
            rowcomp.setColumnWidth(3, fm.stringWidth("99/99/99 99:99 AM"));
            rowcomp.setColumnVaries(3, true);

            return row;
        }

        public TableRow createRow(int rownum, int columns, Insets insets, Object data) {

            // ignore passed columns; the row will have 4 columns, 2 of them labels
            TableRow row = super.createRow(rownum, 4, insets, data);

            // calculate minimum widths for labels
            TableRowComponent rowcomp = (TableRowComponent) row.getComponent();
            FontMetrics fm = rowcomp.getFontMetrics(rowcomp.getFont());

            row.setColumn( 0, new TextLabel("Data reduction: ", FontPool.getLabelFont(), TextLabel.TEXT_ALIGN_RIGHT | TextLabel.TEXT_ALIGN_VCENTER) );
            rowcomp.setColumnWidth( 0, fm.stringWidth("MMData reduction: ") );
            rowcomp.setColumnVaries( 0, false );

            row.setColumn( 2, new TextLabel("Polygon count: ", FontPool.getLabelFont(), TextLabel.TEXT_ALIGN_RIGHT | TextLabel.TEXT_ALIGN_VCENTER) );
            rowcomp.setColumnWidth( 2, fm.stringWidth("MMPolygon count: ") );
            rowcomp.setColumnVaries( 2, false );

            return row;
        }

    }

    class WorkspaceAdapter extends WindowAdapter implements WorkspaceListener {
        public void windowMaximized(WindowEvent evt) {
        }
        public void windowDemaximized(WindowEvent evt) {
        }
    }

    class ChiselTablePane extends ScrollablePane {

        int unitHt = 24;

        public ChiselTablePane() {
            super();
            getHScrollComponent().setVisible(false);
            setOpaque(false);
            ((Adjustable) getVScrollComponent()).setUnitIncrement(unitHt);
        }

        public void doLayout() {
            Dimension size = getSize();
            Component hs = getHScrollComponent();
            Component vs = getVScrollComponent();
            Dimension minh = hs.getMinimumSize();
            Dimension minv = vs.getMinimumSize();

            ((Adjustable) getVScrollComponent()).setBlockIncrement(size.height - unitHt);

            int ntables = tables.length;
            int y = 0;
            int width = size.width - minv.width + 9; // chop off some pixels from the scroll bar
            for (int i = 0; i < ntables; i++) {
                ChiselTable t = tables[i];
                Component comp = t.getComponent();
                int height = comp.getPreferredSize().height;
                comp.reshape(0, y, width, height);

                y += height;
                //if (y >= yvisbottom) {
                //    break;
                //}
            }


            //hs.setBounds(0, size.height - minh.height/2, size.width - minv.width /2, minh.height);
            vs.setBounds(width - 7, 0, minv.width, size.height);
            setScrollValues();
        }
    }
}


class PathLabel extends TextLabel {

    public PathLabel(String label) {
        super(label);
    }


    final static String ellipsis = "...";

    /** Shorten a path name as necessary to fit within a given width.  The
        returned string is for display purposes only. */
    static String fitPath(String path, int width, FontMetrics fm) {
        int pw = fm.stringWidth(path);
        if (pw > width) {
            int ellwidth = fm.stringWidth(ellipsis);
            int fs = path.lastIndexOf(File.separatorChar);
            if (fs > 0) {
                String name = (fs < path.length() - 1 ? path.substring(fs) : "");
                int nw = fm.stringWidth(name);
                if (ellwidth + nw < width) {
                    for (int cut = fs - 1; cut > 3; cut--) {
                        path = path.substring(0, cut) + ellipsis + name;
                        pw = fm.stringWidth(path);
                        if (pw <= width) {
                            break;
                        }
                    }

                } else {
                    path = name.substring(1);
                    while (path.length() > 0 && fm.stringWidth(path) > width) {
                        path = path.substring(0, path.length() - 1);
                    }
                }
            }
        }
        return path;
    }

    public void paint(Graphics g) {
        String oldlabel = label;
		Dimension size = getSize();
		Font font = getFont();
		if (font != null) {
    		label = fitPath(label, size.width - margin.left - margin.right, getFontMetrics(font));
		}

		super.paint(g);

		label = oldlabel;
    }
}
