/*
 * @(#)ChiselSet.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;
import com.trapezium.util.ProgressIndicator;
import com.trapezium.util.ZipClassLoader;
import com.trapezium.factory.FactoryResponseListener;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.*;


/** This class keeps track of a set of chisels.  There are some standard
 *  sets:  VALIDATORS, FORMATTER, CLEANERS, CONDENSORS, REDUCERS, REORGANIZERS,
 *  and any number of extension sets.
 *
 *  Each set exists in its own package, and each package comes with a text
 *  file that is used to create the GUI for that set.
 */
public class ChiselSet {
    final public static int VALIDATORS = 0;
    static ProgressIndicator validatorProgress;
    final public static int FORMATTER = 1;
    static ProgressIndicator formatterProgress;
    final public static int CLEANERS = 2;
    static ProgressIndicator cleanerProgress;
    final public static int CONDENSERS = 3;
    static ProgressIndicator condenserProgress;
    final public static int REDUCERS = 4;
    static ProgressIndicator reducerProgress;
    final public static int REORGANIZERS = 5;
    static ProgressIndicator reorganizerProgress;
    final public static int MUTATORS = 6;
    static ProgressIndicator mutatorProgress;

    final public static int numSets = 7;  // exclude MUTATORS
    final public static int maxPlugins = 10;
    final public static String pluginDirName = "plugins";

    static Hashtable classMapper = new Hashtable();
    
//static public PrintStream dbgStr = null;
    /** create array of all chisel categories */
    /** create array of all chisel categories */
    static public ChiselSet[] createChiselSets() {
        ChiselSet[] sets = new ChiselSet[numSets];
        for (int i = 0; i < numSets; i++) {
            sets[i] = new ChiselSet(i);
            // force loading of class definitions in case plugins need them
            // (except VALIDATE and FORMAT classes, which aren't really separate classes)
            if (i > 1) {
                loadClassDefinitions(sets[i]);
            }
        }

        // look in plugins directory for plugins
        File pluginDir = new File(Chisel.appDirectory + pluginDirName);
        try {
            if (pluginDir.exists() && pluginDir.isDirectory()) {
                
                String[] pluginList = pluginDir.list();
                int count = pluginList.length;
                if (count > 0) {
                    ChiselSet[] pluginSets = new ChiselSet[count];

                    int additionalSets = 0;
                    for (int i = 0; i < count; i++) {
                        String name = pluginList[i];
                        if (name.endsWith(".zip") || name.endsWith(".ZIP") || name.endsWith(".jar") || name.endsWith(".JAR")) {
//                            if ( dbgStr == null ) {
//                                dbgStr = new PrintStream( new FileOutputStream( new File( "debug" )));
//                            }
//                            dbgStr.println( "Found a plugin '" + name + "'" );
                            ChiselPluginDescriptor cpd = getPlugin( name );
//                            if ( cpd == null ) dbgStr.println( "Hey, cpd is null!" );
                            if ( cpd != null && classesAvailable(cpd) ) {
//                                dbgStr.println( "adding it to additional sets" );
                                pluginSets[ additionalSets++ ] = new ChiselSet( cpd );
                            }
                        }
                    }
                    if ( additionalSets > 0 ) {
                        ChiselSet[] newResult = new ChiselSet[ numSets + additionalSets ];
                        for ( int i = 0; i < numSets; i++ ) {
                            newResult[i] = sets[i];
                        }
                        for ( int i = 0; i < additionalSets; i++ ) {
                            newResult[ numSets + i ] = pluginSets[i];
                        }
                        sets = newResult;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Unable to load plugins: " + e);
        }
        
        return sets;
    }

    static void loadClassDefinitions(ChiselSet set) {
        for (int i = 0; i < set.getNumberChisels(); i++) {
            ChiselDescriptor cd = set.getEntry(i);
            try {
                Class c = Class.forName(cd.getClassName());
            } catch (Exception x) {
                System.out.println("Chisel class " + cd.getClassName() + " not found.");
            }
        }            
    }
    
    static boolean classesAvailable(ChiselPluginDescriptor cpd) {
//        dbgStr.println( "Checking classes available\n" );
        for (int i = 0; i < cpd.getNumberChisels(); i++) {
            String name = cpd.getClassName(i);
            if ( classMapper.get( name ) == null ) {
                try {
                    Class c = Class.forName(name);
                } catch (Exception x) {
                    System.out.println("Plugin class " + name + " not found.");
//                    dbgStr.println( "plugin class " + name + " not found." );
                    return false;
                }
            }
        }
//        dbgStr.println( "Found classes\n" );
        return true;
    }

    /** Get the ChiselSetDescriptor for a plugin */
    static ChiselPluginDescriptor getPlugin( String name ) {
        ChiselPluginDescriptor plugin = null;
        String dir = Chisel.appDirectory +  pluginDirName;
//        dbgStr.println( "getPlugin " + name );
        try {

            ZipFile pluginFile = new ZipFile(dir + File.separatorChar + name);
            ZipClassLoader loader = new ZipClassLoader(pluginFile);
            Enumeration zippedFiles;

            // load all the classes, unless this is an evaluation copy
            //if (Chisel.accessAllowed()) {
                zippedFiles = pluginFile.entries();
                while (zippedFiles.hasMoreElements()) {
                    ZipEntry ze = (ZipEntry) zippedFiles.nextElement();
                    String fname = ze.getName();
                    if (fname.endsWith(".class")) {
                        // load the class
                        Class c = loader.loadClass(ze);
                        if ( c != null ) {
                            classMapper.put( c.getName(), c );
//                            dbgStr.println( "Putting class " + c.getName() + " into class mapper" );
                        }
                    }
                }
            //} 

            // go through again and look for a serialized chisel set
            zippedFiles = pluginFile.entries();
            while (zippedFiles.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) zippedFiles.nextElement();
                String fname = ze.getName();
                if (fname.endsWith(ChiselPluginDescriptor.extension)) {
                    ObjectInputStream ois = new ObjectInputStream( pluginFile.getInputStream(ze) );
                    Object o = ois.readObject();
                    if ( o instanceof ChiselPluginDescriptor ) {
                        plugin = (ChiselPluginDescriptor) o;
                    }
                }
            }

        } catch ( FileNotFoundException e ) {
//            dbgStr.println( "Didn't find file " + e.toString() );
            return( null );

        } catch ( Exception e ) {
            System.out.println("Error loading plugin: " + e.toString());
//            dbgStr.println( "Error loading plugin: " + e.toString() );
            return( null );
        }
               
//               dbgStr.println( "plugin " + name + " found" );
        return plugin;
    }
    
    /** Set the progress bar listener for a chisel group */
    static public void setProgressIndicator( int chiselGroup, ProgressIndicator pl ) {
        if ( chiselGroup == VALIDATORS ) {
            validatorProgress = pl;
        } else if ( chiselGroup == FORMATTER ) {
            formatterProgress = pl;
        } else if ( chiselGroup == CLEANERS ) {
            cleanerProgress = pl;
        } else if ( chiselGroup == CONDENSERS ) {
            condenserProgress = pl;
        } else if ( chiselGroup == REDUCERS ) {
            reducerProgress = pl;
        } else if ( chiselGroup == MUTATORS ) {
            mutatorProgress = pl;
        } else if ( chiselGroup == REORGANIZERS ) {
            reorganizerProgress = pl;
        }
    }

    static public int NoListener = -1;
    static public int InlineCountListener = 0;
    static public int UnusedDEFCountListener = 1;
    static public int DefaultFieldCountListener = 2;
    static public int UnusedCoordCountListener = 3;
    static public int UnusedPROTOinterfaceCountListener = 4;
    static public int ErrorSummaryListener = 5;
    static public int DuplicateFieldCountListener = 6;
    static public int EmptyIndexedFaceSetCountListener = 7;
    static public int ElevationGridCountListener = 8;
    static public int TransformCountListener = 9;
    static public int DEFUSECountListener = 10;
    static public int DupIndexCountListener = 11;
    static public int BadFaceCountListener = 12;
    static public int RepeatedValueCountListener = 13;
    static public int BadRouteListener = 14;
    static public int UnnecessaryKeyValueListener = 15;
    static public int NormalCountListener = 16;
    static public int UnindexedValueListener = 17;
    static public int InterpolatorCountListener = 18;
    static public int ValueNodeCountListener = 19;
    static public int UselessNodeCountListener = 20;
    static public int DEFUSECountListener2 = 21;
    static public int InterpolatorCountListener2 = 22;
    static public int SingleColorIFScountListener = 23;
    static public int PROTOInstanceCountListener = 24;
    static public int DEFUSECountListener3 = 25;
    // when another listener added above, update lastListener below
    static public int LastListener = 26;
    
    static FactoryResponseListener[] specializedListener = new FactoryResponseListener[LastListener];

    static public void setSpecializedListener( int listenerGroup, FactoryResponseListener frl ) {
        specializedListener[ listenerGroup ] = frl;
    }
    static public FactoryResponseListener getSpecializedListener( int listenerGroup ) {
        return( specializedListener[ listenerGroup ] );
    }

    static public ProgressIndicator getProgressIndicator( int chiselGroup ) {
        if ( chiselGroup == VALIDATORS ) {
            return( validatorProgress );
        } else if ( chiselGroup == FORMATTER ) {
            return( formatterProgress );
        } else if ( chiselGroup == CLEANERS ) {
            return( cleanerProgress );
        } else if ( chiselGroup == CONDENSERS ) {
            return( condenserProgress );
        } else if ( chiselGroup == REDUCERS ) {
            return( reducerProgress );
        } else if ( chiselGroup == MUTATORS ) {
            return( mutatorProgress );
        } else if ( chiselGroup == REORGANIZERS ) {
            return( reorganizerProgress );
        } else {
            return( null );
        }
    }

    /** Global state indicating whether or not chisels are running */
    static boolean chiselsAvailable = false;
    static public void enableChisels() {
        chiselsAvailable = true;
    }
    static public void disableChisels() {
        chiselsAvailable = false;
    }
    static public boolean chiselsEnabled() {
        return( chiselsAvailable );
    }

	ChiselDescriptor[] chisels;
	int numberChisels;
	String command;
	String tooltip;
	boolean initialCollapsedValue;
	boolean greenable;
	boolean automaticallyChecked;
	public boolean getGreenable() {
	    return( greenable );
	}
	public boolean isAutomaticallyChecked() {
	    return( automaticallyChecked );
	}
	
	//boolean accessAllowed;
	//public boolean fullAccessAllowed() {
	//    return( accessAllowed );
	//}


	/** Create a Chisel category based on a description contained in an
	 *  external object.  This constructor used for plugins.
	 */
    public ChiselSet( ChiselPluginDescriptor cpd ) {
        //accessAllowed = Chisel.fullAccessAllowed();
        String key = null;
        greenable = cpd.getGreenable();
        key = cpd.getKey();
        initialCollapsedValue = cpd.getInitialCollapsedValue();
        numberChisels = cpd.getNumberChisels();
        chisels = new ChiselDescriptor[ numberChisels ];
        for ( int i = 0; i < numberChisels; i++ ) {
            chisels[i] = new ChiselDescriptor( cpd.getClassName( i ),
                cpd.getPrompt( i ), cpd.getCheckVal( i ), 
                ChiselDescriptor.NOTYPE_CHISEL, cpd.getListener( i ));
        }
        command = cpd.getCommand();
        tooltip = cpd.getTip();
        automaticallyChecked = false;
    }

    /** Create a Chisel category for one of the built-in (predefined)
     *  categories.  This could be replaced by plug-in approach, to allow
     *  for language specific Chisel descriptions.
     *
     *  @param category one of the predefined Chisel categories
     */
	public ChiselSet(int category) {
        String key = null;
        automaticallyChecked = false;
        //accessAllowed = true;
		switch (category) {
		    case VALIDATORS:
		        greenable = true; //false;
		        key = "validate";
		        initialCollapsedValue = true;
		        numberChisels = 4;
        		chisels = new ChiselDescriptor[ numberChisels ];
        		chisels[0] = new ChiselDescriptor( "ParserFactory", "Humanoid Animation 1.1", false );
        		chisels[1] = new ChiselDescriptor( "ParserFactory", "Disable nonconformance checks", false );
        		chisels[2] = new ChiselDescriptor( "ParserFactory", "Disable warnings", false );
        		chisels[3] = new ChiselDescriptor( "ParserFactory", "Disable unused DEF warnings", false );
		        break;
		    case FORMATTER:
		        greenable = true; //false;
		        key = "format";
		        initialCollapsedValue = true;
		        numberChisels = 4;
		        chisels = new ChiselDescriptor[ numberChisels ];
                // NOTE: if strip comments is not at position 0 then ChiselTableStack.java must be updated
		        chisels[0] = new ChiselDescriptor( "#stripcomments", "Remove comments", false );
		        chisels[1] = new ChiselDescriptor( "#maxlinelen", "Wrap lines", true );
		        chisels[2] = new ChiselDescriptor( "#indent", "Indent", true );
		        chisels[3] = new ChiselDescriptor( "#tab", "Tab every N spaces", false );
		        break;
		    case CLEANERS:
		        automaticallyChecked = true;
		        greenable = true;
		        initialCollapsedValue = false;
		        key = "clean";
		        numberChisels = 10;
        		chisels = new ChiselDescriptor[ numberChisels ];
        		chisels[0] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.DefaultFieldValueRemover", "Remove default fields", false, ChiselDescriptor.NOTYPE_CHISEL, DefaultFieldCountListener );
        		chisels[1] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.IFS_DupCoordDetector", "Remove repeated value refs", false, ChiselDescriptor.IndexedFaceSet_CHISEL, RepeatedValueCountListener );
        		chisels[2] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.IFS_CoordRemover", "Remove unused values", false, ChiselDescriptor.IndexedFaceSet_CHISEL, UnusedCoordCountListener );
        		chisels[3] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.IFS_DupIndexRemover", "Remove repeated index values", false, ChiselDescriptor.IndexedFaceSet_CHISEL, DupIndexCountListener );
        		chisels[4] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.DuplicateFieldRemover", "Remove repeated fields", false, ChiselDescriptor.FIELD_CHISEL, DuplicateFieldCountListener );
        		chisels[5] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.IFS_BadFaceRemover", "Remove bad faces", false, ChiselDescriptor.IndexedFaceSet_CHISEL, BadFaceCountListener );
        		chisels[6] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.NodeRemover", "Remove useless nodes", false, ChiselDescriptor.NOTYPE_CHISEL, UselessNodeCountListener );
        		chisels[7] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.ROUTEMover", "Move ROUTEs to end of file", false, ChiselDescriptor.NOTYPE_CHISEL, BadRouteListener );
		        chisels[8] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.KeyValueRemover", "Remove unnecessary interpolator values", false, ChiselDescriptor.FIELD_CHISEL, UnnecessaryKeyValueListener );
		        chisels[9] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.IFS_ColorToDiffuseColor", "Generate diffuseColor field for IFS", false, ChiselDescriptor.NOTYPE_CHISEL, SingleColorIFScountListener );
		        break;
		    case CONDENSERS:
		        greenable = true;
		        initialCollapsedValue = true;
		        key = "condense";
		        numberChisels = 13;
        		chisels = new ChiselDescriptor[ numberChisels ];
		        chisels[0] = new ChiselDescriptor( "com.trapezium.chisel.condensers.ResolutionMaker", "Adjust numeric resolution", false, ChiselDescriptor.IndexedFaceSet_CHISEL, ValueNodeCountListener );
		        chisels[1] = new ChiselDescriptor( "com.trapezium.chisel.condensers.InterpolatorResolution", "Adjust Interpolator resolution", false, ChiselDescriptor.NOTYPE_CHISEL, InterpolatorCountListener );
        		chisels[2] = new ChiselDescriptor( "com.trapezium.chisel.condensers.DEFmaker", "Create DEF/USE", false, ChiselDescriptor.ALLNODE_CHISEL, DEFUSECountListener );
        		chisels[3] = new ChiselDescriptor( "com.trapezium.chisel.condensers.PROTOMaker", "Create PROTOs for interpolators", false, ChiselDescriptor.NOTYPE_CHISEL, InterpolatorCountListener2 );
		        chisels[4] = new ChiselDescriptor( "com.trapezium.chisel.condensers.IFS_IndexOptimizer", "Create index fields", false, ChiselDescriptor.IndexedFaceSet_CHISEL, UnindexedValueListener );
        		chisels[5] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.IFS_NoFaceRemover", "Remove empty IndexedFaceSets", false, ChiselDescriptor.IndexedFaceSet_CHISEL, EmptyIndexedFaceSetCountListener );
		        chisels[6] = new ChiselDescriptor( "com.trapezium.chisel.condensers.NormalRemover", "Remove normals", false, ChiselDescriptor.IndexedFaceSet_CHISEL, NormalCountListener );
           		chisels[7] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.DEFremover", "Remove unused DEFs", false, ChiselDescriptor.NOTYPE_CHISEL, UnusedDEFCountListener );
        		chisels[8] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.UnusedPROTOInterfaceRemover", "Remove unused PROTO interface fields", false, ChiselDescriptor.FIELD_CHISEL, UnusedPROTOinterfaceCountListener );
		        chisels[9] = new ChiselDescriptor( "com.trapezium.chisel.condensers.NameShortener", "Shorten DEF names", false, ChiselDescriptor.ALLNODE_CHISEL, DEFUSECountListener2 );
                        chisels[10] = new ChiselDescriptor( "com.trapezium.chisel.condensers.NameFixer", "Simplify DEF names", false, ChiselDescriptor.ALLNODE_CHISEL, DEFUSECountListener3 );
		        // a bit of history weirdness, InterpolatorMinimizer in cleaners...
		        chisels[11] = new ChiselDescriptor( "com.trapezium.chisel.cleaners.InterpolatorMinimizer", "Single value interpolator keys", false, ChiselDescriptor.FIELD_CHISEL );
		        chisels[12] = new ChiselDescriptor( "com.trapezium.chisel.condensers.Remove_material_node", "Remove material node", false, ChiselDescriptor.NOTYPE_CHISEL );
		        break;
		    case REDUCERS:
		        greenable = true;
		        initialCollapsedValue = true;
		        key = "reduce";
		        numberChisels = 4;
        		chisels = new ChiselDescriptor[ numberChisels ];
		        chisels[0] = new ChiselDescriptor( "com.trapezium.chisel.reducers.IFS_CoplanarTriToQuad", "Coplanar triangle to quad", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
		        chisels[1] = new ChiselDescriptor( "com.trapezium.chisel.reducers.IFS_Simplifier", "Remove smallest edges", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
		        chisels[2] = new ChiselDescriptor( "com.trapezium.chisel.reducers.IFS_PolygonRemover", "Remove smallest triangles", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
		        chisels[3] = new ChiselDescriptor( "com.trapezium.chisel.reducers.IFS_MeshRemover", "Merge parallel edges", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
		        break;
		    case REORGANIZERS:
		        greenable = true;
		        initialCollapsedValue = true;
		        key = "reorganize";
		        numberChisels = 8;
        		chisels = new ChiselDescriptor[ numberChisels ];
		        chisels[0] = new ChiselDescriptor( "com.trapezium.chisel.reorganizers.Uninline", "Uninline files", false, ChiselDescriptor.UnInline_CHISEL, InlineCountListener );
		        chisels[1] = new ChiselDescriptor( "com.trapezium.chisel.reorganizers.UnPROTO", "Un-PROTO", false, ChiselDescriptor.ALLNODE_CHISEL, PROTOInstanceCountListener );
        		chisels[2] = new ChiselDescriptor( "com.trapezium.chisel.reorganizers.ShapeToInline", "Turn Shapes into Inlines", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
        		chisels[3] = new ChiselDescriptor( "com.trapezium.chisel.reorganizers.ShapeColorSplitter", "Split IFS by color", false, ChiselDescriptor.ALLNODE_CHISEL );
        		chisels[4] = new ChiselDescriptor( "com.trapezium.chisel.reorganizers.ShapeColorJoiner", "Join Shapes by color", false, ChiselDescriptor.ALLNODE_CHISEL );
        		chisels[5] = new ChiselDescriptor( "com.trapezium.chisel.reorganizers.ShapeConnectivitySplitter", "Split IFS by connectivity", false, ChiselDescriptor.ALLNODE_CHISEL );
        		chisels[6] = new ChiselDescriptor( "com.trapezium.chisel.reorganizers.ElevationGridSplitter", "Split ElevationGrid", false, ChiselDescriptor.ALLNODE_CHISEL, ElevationGridCountListener );
                chisels[7] = new ChiselDescriptor( "com.trapezium.chisel.reorganizers.DEFToInline", "Turn top level DEFs to Inlines", false, ChiselDescriptor.ALLNODE_CHISEL );
		        break;
		    case MUTATORS:
                greenable = false;
                initialCollapsedValue = true;
                key = "mutate";                
		        numberChisels = 2;
        		chisels = new ChiselDescriptor[ numberChisels ];
                        // MLo (Commented out unreliable mutators)
                        // MLo Reasons for commenting out:
                        // - Origami and Cubist don't seem useful
                        // - Triangulator often ruins the geometry
                        // - Smasher generates errors if user choses two planes rather than just one
		        //chisels[0] = new ChiselDescriptor( "com.trapezium.chisel.mutators.Cubist", "Cubist", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
		        //chisels[1] = new ChiselDescriptor( "com.trapezium.chisel.mutators.Origami", "Origami", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
        		//chisels[2] = new ChiselDescriptor( "com.trapezium.chisel.mutators.IFS_Triangulator", "Triangulate", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
        		//chisels[3] = new ChiselDescriptor( "com.trapezium.chisel.mutators.IFS_Smasher", "Flatten", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
        		//chisels[4] = new ChiselDescriptor( "com.trapezium.chisel.mutators.IFS_FaceToLineSet", "Wireframe", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
        		//chisels[5] = new ChiselDescriptor( "com.trapezium.chisel.mutators.IFS_FaceToPointSet", "Point cloud", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
		        chisels[0] = new ChiselDescriptor( "com.trapezium.chisel.mutators.IFS_FaceToLineSet", "Wireframe", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
        		chisels[1] = new ChiselDescriptor( "com.trapezium.chisel.mutators.IFS_FaceToPointSet", "Point cloud", false, ChiselDescriptor.IndexedFaceSet_CHISEL );
		        break;
		    default:
		        chisels = null;
		        numberChisels = 0;
                break;
        }
        if (key == null) {
            command = tooltip = "";
        } else {
            command = ChiselResources.getLabel(key).toUpperCase();
            tooltip = ChiselResources.getTip(key);
        }
    }

    /** Get the number of chisels in the set */
	public int getNumberChisels() {
		return( numberChisels );
	}

    /** Get a particular chisel in the set */
	public ChiselDescriptor getEntry( int offset ) {
		return( chisels[ offset ] );
	}

	public String getCommand() {
	    return command;
	}

    /** Get the mouse-over tip associated with the set */
	public String getTooltip() {
	    return tooltip;
	}

    /** Is the chisel set initially collapsed or expanded */
	public boolean getInitialCollapsedValue() {
	    return( initialCollapsedValue );
	}
}
