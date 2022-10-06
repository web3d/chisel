/*
 * @(#)PROTOInstance.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.vrml.fields.MFNodeValue;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.grammar.VRML97;
import com.trapezium.vrml.grammar.FieldDescriptor;
import com.trapezium.vrml.visitor.ISLocator;
import com.trapezium.vrml.visitor.DEFVisitor;
import java.util.Vector;

/**
 *  Scene graph component for a PROTO instance.
 *
 *  The PROTOInstance has field values which are explicitly declared,
 *  and are part of the PROTO interface.  It has node values which are
 *  copied from the PROTO declaration, with actual values from the
 *  instance substituted where indicated by IS fields.
 *
 *  Note:  The above level of PROTOInstance construction is not complete.
 *
 *    USE nodeNameId
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.21, 16 July 1998, self ref PROTO USE bug
 *  @version         1.12, 7 April 1998, added "getFieldNames" method
 *  @version         1.1, 6 Jan 1998
 *
 *  @since           1.0
 */
public class PROTOInstance extends Node {
    PROTObase protoBase;

    /** Class constructor */
    public PROTOInstance( PROTObase pb ) {
        super();
        protoBase = pb;
    }

    /** template method, overrides Node.isPROTOnode() which returns false. */
    public boolean isPROTOnode() {
        return( true );
    }

    /** Get the first node in the PROTO declaration, this node defines the
     *  type of the PROTO.
     */
    public Node getPROTONodeType() {
        return( protoBase.getPROTONodeType() );
    }

    /** Get a reference to the declaration of the PROTO */
    public PROTObase getPROTObase() {
        return( protoBase );
    }
    
    /** Get the FieldDescriptor, defined by IS usage for a field */
    public FieldDescriptor getFieldDescriptor( String fieldId ) {
        if ( protoBase != null ) {
            return( protoBase.getFieldDescriptor( fieldId ));
        } else {
            return( null );
        }
    }

    /** Get the name of the PROTO this instance is based on */
    public String getPROTOname() {
        if ( protoBase != null ) {
            return( protoBase.getId() );
        } else {
            return( null );
        }
    }

    /** Get the name of the Node type compatible with this PROTO */
    public String getNodeName() {
        Node n = getPROTONodeType();
        if ( n != null ) {
            return( n.getNodeName() );
        } else {
            return( null );
        }
    }

	/** get the field id with the closest spelling */
	public String getClosestFieldId( String fieldName ) {
	    if ( protoBase != null ) {
	        return( protoBase.getClosestFieldId( fieldName ));
	    } else {
	        return( null );
	    }
	}

	/** get the VRML97 field type SF<type> or MF<type> for the field.
     *
     *  @return  type is returned as a String, "UnknownType" returned
     *     if field is unknown.
     */
	public String getFieldType( String fieldName ) {
	    if ( protoBase != null ) {
	        Field f = protoBase.getInterfaceDeclaration( fieldName );
	        if ( f != null ) {
	            return( VRML97.getFieldTypeString( f.getFieldType() ));
	        }
	    }
        return( "UnknownType" );
	}

	/** get the interface declaration of the field
	 *
	 *  @return  Field declaring the interface, null if field unknown
	 */
	public Field getInterfaceDeclaration( String fieldName ) {
	    if ( protoBase != null ) {
	        return( protoBase.getInterfaceDeclaration( fieldName ));
	    } else {
	        return( null );
	    }
	}

	/** Check if field is part of the PROTO interface declaration.
	 *
	 *  @return true if the <B>fieldName</B> is part of the PROTO 
	 *    interface declaration, otherwise false. 
	 */
	public boolean isValidFieldId( String fieldName ) {
	    if ( protoBase != null ) {
	        return( protoBase.isValidFieldId( fieldName ));
	    } else {
	        return( false );
	    }
	}

	/** Copy all nodes containing IS references from PROTO declaration body.
	 *
	 *  IS references are resolved as the copy takes place.
	 */
	public void copyBaseNodeInfo() {
	    if ( protoBase != null ) {
	        Scene s = protoBase.getPROTObody();
	        if ( s != null ) {
	            int nChildren = s.numberChildren();
//                ISLocator il = new ISLocator();
	            for ( int i = 0; i < nChildren; i++ ) {
	                VrmlElement vle = s.getChildAt( i );
	                if ( vle instanceof Node ) {
//	                    il.reset();
//	                    vle.traverse( il );

	                    // create copies of nodes that have IS field references
//	                    if ( il.foundISField() ) {
                            Node n = (Node)vle;
                            Node newNode = (Node)n.vrmlClone( this );
                            addChild( newNode );
//                        }
	                }
	            }
	        }
	    }
	}

	/**
	 *  Validate the nodes contained in a PROTO instance.
	 */
	public void verify( TokenEnumerator v ) {
   		Scene scene = (Scene)getScene();
   		if ( scene != null ) {
    	    int nChildren = numberChildren();
   	    	int vstate = v.getState();
    	    for ( int i = 0; i < nChildren; i++ ) {
    	        VrmlElement ve = getChildAt( i );
    	        if ( ve instanceof Node ) {
    	            Node node = (Node)ve;
           	    	NodeType.verify( node, node.getNodeName(), scene );
               	}
            }
       		v.setState( vstate );
        }
   	}

    /** Get the interface type of a particular field.
     *
     *  @param fieldId the name of the field to check
     *  @return VRML97.UnknownInterfaceType if the field doesn't exist in
     *    the PROTO, otherwise one of VRML97.exposedField, VRML97.field,
     *    VRML97.eventIn, VRML97.eventOut.
     */
   	public int getInterfaceType( String fieldId ) {
   	    Field f = getInterfaceDeclaration( fieldId );
   	    if ( f != null ) {
   	        return( f.getInterfaceType() );
   	    } else {
   	        return( VRML97.UnknownInterfaceType );
   	    }
   	}

    /** Get a list of field names defined for the PROTO.
     */
    public String[] getFieldNames() {
        return( protoBase.getFieldNames() );
    }
    
    /** Check if the instance fields are in use, handles case
     *  where the field is not referenced by IS in the PROTO declaration,
     *  but the field is DEFed in an instance, and USEd somewhere else.
     *  Note, at the time this is called (during parsing, we don't know if
     *  the DEF is USEd, so we just assume it is).
     *  In this case, the "not referenced" IS warning has to be removed.
     */
    public void checkInUse() {
        if ( protoBase != null ) {
            if ( protoBase.hasUnusedInterfaces() ) {
        	    int nChildren = numberChildren();
        	    for ( int i = 0; i < nChildren; i++ ) {
        	        VrmlElement ve = getChildAt( i );
        	        if ( ve instanceof Field ) {
        	            Field f = (Field)ve;
        	            FieldValue fv = f.getFieldValue();
                	    if (( fv instanceof SFNodeValue) || ( fv instanceof MFNodeValue )) {
                	        DEFVisitor dv = new DEFVisitor();
                	        fv.traverse( dv );
                	        if ( dv.getNumberDEFs() > 0 ) {
                	            Field interfaceDeclaration = protoBase.getInterfaceDeclaration( f.getFieldId() );
                	            if ( interfaceDeclaration != null ) {
                	                String err = interfaceDeclaration.getError();
                	                if ( err != null ) {
                	                    if ( err.indexOf( "not referenced" ) > 0 ) {
                	                        interfaceDeclaration.setError( null );
                	                    }
                	                }
                	            }
                	        }
                	    }
                	}
                }
            }
        }
    }
}

