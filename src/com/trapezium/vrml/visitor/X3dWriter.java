/*
 * @(#)X3dWriter.java
 *
 * Copyright (c) 1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import com.trapezium.pattern.Visitor;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFNodeValue;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.util.StringUtil;
import java.util.Vector;
import java.io.PrintStream;

/**
 *  The X3dWriter handles VRML to X3D only for the case where the node fields are ordered
 *  in a particular way:  the non-node fields have to be traversed before the node fields.
 *  This has required the addition of a new "traverse" method which traverses in this
 *  particular order.
 */
 
public class X3dWriter extends Visitor {
    PrintStream out;

    class LevelRecorder {
        Vector levels = new Vector();
        Vector nodes = new Vector();
        Vector terminated = new Vector();
        Vector childrenAdded = new Vector();
        
        void terminate( int offset ) {
            Boolean terminatedBool = (Boolean)terminated.elementAt( offset );
            if ( !terminatedBool.booleanValue() ) {
                Boolean childrenAddedBool = (Boolean)childrenAdded.elementAt( offset );
                if ( childrenAddedBool.booleanValue() ) {
                    out.println( ">" );
                } else {
                    out.println( "/>" );
                }
                terminated.removeElementAt( offset );
                terminated.insertElementAt( new Boolean( true ), offset );
            }
        }
      
        void xmlClose( int offset ) {
            String levelString = (String)nodes.elementAt( offset );
            Integer level = (Integer)levels.elementAt( offset );
            int levelInt = level.intValue();
            if ( levelInt > 0 ) {
                out.print( StringUtil.spacer( levelInt ));
            }
           out.println( "</" + levelString + ">" );
        }
      
        void recordLevel( int visitLevel ) {
            // if we are going down a level, make sure last guy is terminated,
            // and we mark it as a node where children are added
            if ( levels.size() > 0 ) {
                int lastElement = levels.size() - 1;
                Integer level = (Integer)levels.elementAt( lastElement );
                if ( visitLevel > level.intValue() ) {
                    childrenAdded.removeElementAt( lastElement );
                    childrenAdded.insertElementAt( new Boolean( true ), lastElement );
                    terminate( lastElement );
                } 
            }
            // if we are going up a level or repeating same level, terminate everything
            while ( levels.size() > 0 ) {
                int lastElement = levels.size() - 1;
                Integer level = (Integer)levels.elementAt( lastElement );
                if ( level.intValue() >= visitLevel ) {
                    terminate( lastElement );
                    Boolean childrenAddedBool = (Boolean)childrenAdded.elementAt( lastElement );
                    if ( childrenAddedBool.booleanValue() ) {
                        xmlClose( lastElement );
                    }
                    levels.removeElementAt( lastElement );
                    nodes.removeElementAt( lastElement );
                    terminated.removeElementAt( lastElement );
                    childrenAdded.removeElementAt( lastElement );
                } else {
                    break;
                }
            }
        }

        void finished() {
            int lsize = levels.size();
            if ( lsize > 0 ) {
                for ( int i = lsize - 1; i >= 0; i-- ) {
                    terminate( i );
                    Boolean childrenAddedBool = (Boolean)childrenAdded.elementAt( i );
                    if ( childrenAddedBool.booleanValue() ) {
                        xmlClose( i );
                    }
                }
            }
        }
        
        void storeLevel( String levelString, int visitLevel ) {
            Integer level = new Integer( visitLevel );
            levels.addElement( level );
            nodes.addElement( levelString );
            terminated.addElement( new Boolean( false ));
            childrenAdded.addElement( new Boolean( false ));
        }
    }
          
    LevelRecorder lr;
    
    public X3dWriter( PrintStream out, TokenEnumerator dSource ) {
        super( dSource );
        this.out = out;
        lr = new LevelRecorder();
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        out.println( "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.1//EN\" \"http://www.web3d.org/specifications/x3d-3.1.dtd\">");
        out.println( "<X3D version=\'3.1\' profile=\'Immersive\' xmlns:xsd=\"http://www.w3.org/2001/XMLSchema-instance\" xsd:noNamespaceSchemaLocation=\"http://www.web3d.org/specifications/x3d-3.1.xsd\">" );
        out.println( " <Scene>" );
    }
    
    public void finished() {
        lr.finished();
        out.println( " </Scene>" );
        out.println( "</X3D>" );
    }

    String defString;
    String useString;

    void writeNode( Node n ) {
        lr.recordLevel( visitLevel );
        VrmlElement parent = n.getParent();
        System.out.println( n + " parent is " + parent );
        int spaceCount = visitLevel;
        if ( parent instanceof DEFUSENode ) {
            spaceCount--;
        }
        if ( spaceCount > 0 ) {
            out.print( StringUtil.spacer( spaceCount ));
        }
        out.print( "<" + n.getBaseName() );
        if ( defString != null ) {
            out.print( " DEF=\'" + defString + "\'" );
        } else if ( useString != null ) {
            out.print( " USE=\'" + useString + "\'" );
        }
        defString = null;
        useString = null;
        lr.storeLevel( n.getBaseName(), visitLevel );
    }

    void writeField( Field f ) {
        FieldValue fv = f.getFieldValue();

        if (fv == null) {
            System.err.println("Error: Attempting to write value for field with no value:" + f);
            return;
        }
        
        if (( fv instanceof MFNodeValue ) || ( fv instanceof SFNodeValue )) {
            return;
        }
        out.print( "\n" + spacer() + f.getFieldId() + "=\'" );
        int first = fv.getFirstTokenOffset();
        if ( dataSource.isLeftBracket( first )) {
            first++;
        }
        int last = fv.getLastTokenOffset();
        if ( dataSource.isRightBracket( last )) {
            last--;
        }
        int counter = 0;
        for ( int i = first; i < last; i++ ) {
            out.print( dataSource.toString( i ));
            if ( counter == 20 ) {
                out.println( "" );
                out.print( spacer() );
                counter = 0;
            } else {
                out.print( " " );
                counter++;
            }
        }
        out.print( dataSource.toString( last ) + "\'" );
    }

    public boolean acceptsPassOne( Object a ) {
        if ( a instanceof Field ) {
            Field f = (Field)a;
            FieldValue fv = f.getFieldValue();
            if ( fv instanceof SFNodeValue ) {
                return( false );
            } else if ( fv instanceof MFNodeValue ) {
                return( false );
            }
        }
        return( true );
    }
    
    public boolean acceptsPassTwo( Object a ) {
        if ( a instanceof Field ) {
            Field f = (Field)a;
            FieldValue fv = f.getFieldValue();
            if ( fv instanceof SFNodeValue ) {
                return( true );
            } else if ( fv instanceof MFNodeValue ) {
                return( true );
            }
        }
        return( false );
    }
    
    public boolean visitObject( Object a ) {
        System.out.println( spacer() + a );
        if ( a instanceof DEFUSENode ) {
            DEFUSENode dun = (DEFUSENode)a;
            if ( dun.isDEF() ) {
                defString = dun.getId();
            } else {
                useString = dun.getId();
            }
        } else if ( a instanceof Node ) {
            writeNode( (Node)a );
        } else if ( a instanceof Field ) {
            writeField( (Field)a );
        }
        return( true );
    }
    
    public boolean isTwoPassVisitor() {
        return( true );
    }
}
