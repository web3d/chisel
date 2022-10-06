/*
 * @(#)PROTOMaker.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.condensers;

import com.trapezium.pattern.Visitor;
import com.trapezium.vrml.grammar.VRML97;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.ROUTE;
import com.trapezium.util.NameGenerator;
import com.trapezium.util.ReturnInteger;
import com.trapezium.chisel.*;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *  Creates PROTOs for interpolators if they have the same keys
 */
public class PROTOMaker extends Optimizer {

    class ROUTEReplacer {
        boolean srcIsInterpolator;
        boolean destIsInterpolator;
        ROUTE route;
        
        ROUTEReplacer( ROUTE route, boolean srcIsInterpolator, boolean destIsInterpolator ) {
            this.route = route;
            this.srcIsInterpolator = srcIsInterpolator;
            this.destIsInterpolator = destIsInterpolator;
        }
        
        ROUTE getRoute() {
            return( route );
        }
        
        boolean isSrcInterpolator() {
            return( srcIsInterpolator );
        }
        
        boolean isDestInterpolator() {
            return( destIsInterpolator );
        }
    }
    
    boolean attemptedFirstNode;
    Hashtable interpolatorLists;
    Scene theScene;
    NameGenerator nameGenerator;
    boolean printedPROTOs;
    
	public PROTOMaker() {
		super( "Interpolator", "Creating PROTOs for interpolators..." );
		reset();
	}
	
	public void reset() {
		interpolatorLists = new Hashtable();
		theScene = null;
		nameGenerator = new NameGenerator();
		attemptedFirstNode = false;
		printedPROTOs = false;
	}
	
	public boolean isROUTElistener() {
	    return( true );
	}

    /** Add an interpolator node to the corresponding list */
    void addInterpolator( String interpolatorType, Node n ) {
        InterpolatorEntry ie = (InterpolatorEntry)interpolatorLists.get( interpolatorType );
        if ( ie == null ) {
            ie = new InterpolatorEntry( interpolatorType, nameGenerator );
            interpolatorLists.put( interpolatorType, ie );
        }
        ie.add( n );
    }
    
    /** Is a specific node in the replacement interpolator lists? */
    boolean isInterpolatorReplaced( String interpolatorType, Node n ) {
        InterpolatorEntry ie = (InterpolatorEntry)interpolatorLists.get( interpolatorType );
        if ( ie == null ) {
            return( false );
        } else {
            return( ie.find( n ));
        }
    }

    /** Check if a node name is an interpolator */
    boolean isInterpolator( String nodeName ) {
 	    if ( nodeName.compareTo( "ColorInterpolator" ) == 0 ) {
 	        return( true );
 	    } else if ( nodeName.compareTo( "CoordinateInterpolator" ) == 0 ) {
 	        return( true );
 	    } else if ( nodeName.compareTo( "NormalInterpolator" ) == 0 ) {
 	        return( true );
 	    } else if ( nodeName.compareTo( "OrientationInterpolator" ) == 0 ) {
 	        return( true );
 	    } else if ( nodeName.compareTo( "PositionInterpolator" ) == 0 ) {
 	        return( true );
 	    } else if ( nodeName.compareTo( "ScalarInterpolator" ) == 0 ) {
 	        return( true );
 	    } else {
 	        return( false );
 	    }
    }
    
 	public void attemptOptimization( Node n ) {
 	    // don't mess with interpolators that are inside PROTOs
 	    Scene s = (Scene)n.getScene();
 	    if ( s.getPROTOparent() != null ) {
 	        return;
 	    }
 	    // don't mess with interpolators that may have been copied due
 	    // to being inside a PROTO
 	    if ( n.getParent() instanceof PROTOInstance ) {
 	        return;
 	    }
 	    if ( !attemptedFirstNode ) {
 	        attemptedFirstNode = true;
 	        replaceRange( 1, 1, null );
 	    }
 	    boolean foundInterpolator = false;
 	    if ( theScene == null ) {
 	        theScene = (Scene)n.getScene();
 	    }
 	    String nodeName = n.getBaseName();
 	    if ( !VRML97.isValidFieldId( nodeName, "key" )) {
 	        return;
 	    }
 	    if ( n.getField( "key" ) == null ) {
 	        return;
 	    }
 	    if ( isInterpolator( nodeName )) {
 	        foundInterpolator = true;
 	        addInterpolator( nodeName, n );
 	    } 
 	    if ( foundInterpolator ) {
 	        replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), n );
 	    }
 	}
 	
 	public void attemptOptimization( ROUTE route ) {
 	    String sourceObject = route.getSourceDEFname();
 	    String destObject = route.getDestDEFname();
 	    Scene s = (Scene)route.getScene();
 	    if (( sourceObject != null ) && ( destObject != null ) && ( s != null )) {
 	        DEFUSENode src  = s.getDEF( sourceObject );
 	        DEFUSENode dest = s.getDEF( destObject );
 	        Node srcNode = src.getNode();
 	        Node destNode = dest.getNode();
 	        if (( srcNode != null ) && ( destNode != null )) {
 	            String srcName = srcNode.getBaseName();
 	            String destName = destNode.getBaseName();
 	            boolean srcIsInterpolator = isInterpolator( srcName );
 	            boolean destIsInterpolator = isInterpolator( destName );
                if ( srcIsInterpolator && !isInterpolatorReplaced( srcName, srcNode )) {
                    return;
                }
                if ( destIsInterpolator && !isInterpolatorReplaced( destName, destNode )) {
                    return;
                }
 	            if ( srcIsInterpolator || destIsInterpolator ) {
 	                replaceRange( route.getFirstTokenOffset(), route.getLastTokenOffset(), new ROUTEReplacer( route, srcIsInterpolator, destIsInterpolator ));
 	            }
 	        }
	    }
 	}

    void printPROTOs( TokenPrinter tp ) {
        Enumeration keys = interpolatorLists.keys();
        while ( keys.hasMoreElements() ) {
            InterpolatorEntry ie = (InterpolatorEntry)interpolatorLists.get( keys.nextElement() );
            ie.printPROTO( tp );
        }
    }
    
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    if (( param == null ) && !printedPROTOs ) {
	        printPROTOs( tp );
	        printedPROTOs = true;
	        tp.print( dataSource, startTokenOffset );
	    } else if ( param instanceof Node ) {
	        // in case interpolator was first node
	        if ( !printedPROTOs ) {
	            printPROTOs( tp );
	            printedPROTOs = true;
	        }
	        String nodeName = ((Node)param).getBaseName();
	        InterpolatorEntry ie = (InterpolatorEntry)interpolatorLists.get( nodeName );
	        if ( ie != null ) {
	            ie.print( tp, (Node)param );
	        }
	    } else if ( param instanceof ROUTEReplacer ) {
	        ROUTEReplacer rr = (ROUTEReplacer)param;
	        ROUTE route = rr.getRoute();
	        boolean replaceSource = rr.isSrcInterpolator();
	        boolean replaceDest = rr.isDestInterpolator();
	        tp.flush();
	        tp.print( "ROUTE" );
   	        String src = route.getSourceDEFname();
   	        String srcField = route.getSourceFieldName();
   	        String s = src + "." + remap( srcField, replaceSource );
   	        tp.print( s );
   	        tp.print( "TO" );
   	        String dest = route.getDestDEFname();
   	        String destField = route.getDestFieldName();
   	        s = dest + "." + remap( destField, replaceDest );
   	        tp.print( s );
   	        tp.flush();
   	    }
	}
	
	String remap( String interpolatorField, boolean replaceit ) {
	    if ( replaceit ) {
	        if ( interpolatorField.compareTo( "set_fraction" ) == 0 ) {
	            return( "i" );
	        } else if ( interpolatorField.compareTo( "value_changed" ) == 0 ) {
	            return( "o" );
	        } else if ( interpolatorField.compareTo( "key" ) == 0 ) {
	            return( "k" );
	        } else if ( interpolatorField.compareTo( "keyValue" ) == 0 ) {
	            return( "v" );
	        } else {
	            return( "??" );
	        }
	    } else {
	        return( interpolatorField );
	    }
	}
	
	class KeyFieldEntry {
	    Field keyField;
	    ReturnInteger count;
	    
	    KeyFieldEntry( Field keyField ) {
	        this.keyField = keyField;
	        count = new ReturnInteger( 1 );
	    }
	    
	    public Field getKeyField() {
	        return( keyField );
	    }
	    
	    public int getCount() {
	        return( count.getValue() );
	    }
	    
	    public void incCount() {
	        count.incValue();
	    }
	}
	
	class InterpolatorEntry {
	    // name of VRML interpolator
	    String interpolatorType;
	    
	    // generated PROTO name
	    String PROTOname;
	    
	    // vector of KeyFieldEntry, which just contain "key" field and 
	    // a count of how many identical "key" fields found
	    Vector keyFields;
	    
	    // mapping from a Node to an offset in the "keyFields" vector
	    Hashtable nodeToKeyEntry;
	    
	    NameGenerator nameGenerator;
	    
	    // offset of the most popular "key" field, which gets used
	    // as the default
	    int mostPopularKeyOffset;
	    
	    InterpolatorEntry( String interpolatorType, NameGenerator nameGenerator ) {
	        this.interpolatorType = interpolatorType;
	        this.nameGenerator = nameGenerator;
	        keyFields = new Vector();
	        nodeToKeyEntry = new Hashtable();
	        PROTOname = null;
	    }
	    
	    /** Keep count of most popular key fields, put in mapping node to key entry */
        void add( Node n ) {
            Field key = n.getField( "key" );
            int keyStart = key.getFirstTokenOffset();
            int keyEnd = key.getLastTokenOffset();
            int keyFieldsSize = keyFields.size();
            for ( int i = 0; i < keyFieldsSize; i++ ) {
                KeyFieldEntry kfe = (KeyFieldEntry)keyFields.elementAt( i );
                Field testField = kfe.getKeyField();
                if ( fieldsIdentical( keyStart, keyEnd, testField.getFirstTokenOffset(), testField.getLastTokenOffset() )) {
                    kfe.incCount();
                    nodeToKeyEntry.put( n, new Integer( i ));
                    return;
                }
            }
            KeyFieldEntry kfe = new KeyFieldEntry( key );
            keyFields.addElement( kfe );
            nodeToKeyEntry.put( n, new Integer( keyFieldsSize ));
        }
        
        /** Check if a node is registered in the list */
        boolean find( Node n ) {
            return( nodeToKeyEntry.get( n ) != null );
        }
        
        void calculateMostPopularKeyOffset() {
            int keyFieldsSize = keyFields.size();
            int winner = 0;
            int winnerCount = -1;
            for ( int i = 0; i < keyFieldsSize; i++ ) {
                KeyFieldEntry kfe = (KeyFieldEntry)keyFields.elementAt( i );
                if ( kfe.getCount() > winnerCount ) {
                    winner = i;
                    winnerCount = kfe.getCount();
                }
            }
            mostPopularKeyOffset = winner;
        }
        
        boolean fieldsIdentical( int f1start, int f1end, int f2start, int f2end ) {
            int f1size = f1end - f1start + 1;
            int f2size = f2end - f2start + 1;
            if ( f1size != f2size ) {
                return( false );
            }
            for ( int i = f1start; i <= f1end; i++, f2start++ ) {
                if ( !dataSource.sameAs( i, f2start )) {
                    return( false );
                }
            }
            return( true );
        }
                    
        void printKeyField( TokenPrinter tp ) {
            calculateMostPopularKeyOffset();
            KeyFieldEntry kfe = (KeyFieldEntry)keyFields.elementAt( mostPopularKeyOffset );
            tp.print( "exposedField MFFloat k" );
            printField( tp, kfe.getKeyField(), true );
        }
        
        void printField( TokenPrinter tp, Field f ) {
            printField( tp, f, false );
        }
        
        void printField( TokenPrinter tp, Field f, String nm ) {
            tp.print( nm );
            printField( tp, f, true );
        }
        
        void printField( TokenPrinter tp, Field f, boolean skipFirst ) {
            if ( f != null ) {
                int fStart = f.getFirstTokenOffset();
                if ( skipFirst ) {
                    fStart++;
                }
                int fEnd = f.getLastTokenOffset();
                for ( int i = fStart; i <= fEnd; i++ ) {
                    tp.print( dataSource, i );
                }
            }
        }
        
        /** Get the key entry offset for a node */
        int getKeyEntryOffset( Node n ) {
            Integer offset = (Integer)nodeToKeyEntry.get( n );
            return( offset.intValue() );
        }
        
        /** Print a node, but as a PROTO */
	    void print( TokenPrinter tp, Node n ) {
	        tp.print( PROTOname );
	        tp.print( "{" );
	        int keyEntryOffset = getKeyEntryOffset( n );
	        KeyFieldEntry kfe = (KeyFieldEntry)keyFields.elementAt( keyEntryOffset );
	        if ( keyEntryOffset != mostPopularKeyOffset ) {
	            printField( tp, n.getField( "key" ), "k" );
	        }
	        printField( tp, n.getField( "keyValue" ), "v" );
	        tp.print( "}" );
	    }
	    
	    void createPROTOname() {
	        while ( true ) {
	            String testName = nameGenerator.generateName();
	            if ( theScene.getPROTO( testName ) == null ) {
	                PROTOname = testName;
	                return;
	            }
	        }
	    }
	    
	    void printEventIn( TokenPrinter tp ) {
	        tp.flush();
	        tp.print( "eventIn SFFloat i" );
	        tp.flush();
	    }

        void printEventOut( TokenPrinter tp ) {
            tp.flush();
            tp.print( "eventOut" );
            tp.print( VRML97.getFieldTypeString( interpolatorType, "value_changed" ));
            tp.print( "o" );
            tp.flush();
        }
	    
	    void printPROTO( TokenPrinter tp ) {
	        createPROTOname();
	        tp.flush();
	        tp.print( "PROTO" );
	        tp.print( PROTOname );
	        tp.print( "[" );
	        printEventIn( tp );
	        printKeyField( tp );
	        tp.print( "exposedField" );
	        tp.print( VRML97.getFieldTypeString( interpolatorType, "keyValue" ));
	        tp.print( "v" );
	        tp.print( "[]" );
	        printEventOut( tp );
	        tp.print( "] {" );
	        tp.flush();
	        tp.print( interpolatorType );
	        tp.print( "{" );
	        tp.flush();
	        tp.print( "set_fraction IS i" );
	        tp.flush();
	        tp.print( "key IS k" );
	        tp.flush();
	        tp.print( "keyValue IS v" );
	        tp.flush();
	        tp.print( "value_changed IS o" );
	        tp.print( "} }" );
	    }
	}
}
