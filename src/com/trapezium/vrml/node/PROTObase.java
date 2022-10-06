/*
 * @(#)PROTObase.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import java.util.Vector;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.NodeTypeId;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.grammar.Spelling;
import com.trapezium.vrml.grammar.FieldDescriptor;
import com.trapezium.vrml.grammar.VRML97;
import com.trapezium.parse.TokenEnumerator;
import java.util.Enumeration;

/**
 *  Base class for PROTO and EXTERNPROTO scene graph components.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.21 22 August 1998, added url via IS enumeration access
 *                   1.12, 7 April 1998, added "getFieldNames"
 *                        28 March 1998, added "getInterfaceCount" method
 *  @version         1.1, 14 Jan 1998
 *
 *  @since           1.0
 */
abstract public class PROTObase extends Node {
    /** a list of the fields in the PROTO interface */
	Vector protoInterface;
	
	/** a list of interface fields mapped to node fields via IS syntax */
	Vector isList;
	
	/** flag to indicate whether or not unused interface existence check done */
	boolean unusedInterfaceCheckComplete;
	boolean unusedInterfacesExist;

	/** Create a PROTO or EXTERNPROTO starting at a token */
	public PROTObase( int tokenOffset ) {
		super( tokenOffset );
		unusedInterfaceCheckComplete = false;
		unusedInterfacesExist = false;
	}
    
    /** Check for unused interfaces */
    public boolean hasUnusedInterfaces() {
        checkForUnusedInterfaces();
        return( unusedInterfacesExist );
    }
    
    void checkForUnusedInterfaces() {
        if ( !unusedInterfaceCheckComplete && ( protoInterface != null )) {
    	    int interfaceSize = protoInterface.size();
    	    for ( int i = 0; i < interfaceSize; i++ ) {
    	        Field testField = (Field)protoInterface.elementAt( i );
    	        String error = testField.getError();
    	        if ( error != null ) {
    	            if ( error.indexOf( "not referenced" ) > 0 ) {
    	                unusedInterfacesExist = true;
    	                break;
    	            }
    	        }
    	    }
    	    unusedInterfaceCheckComplete = true;
    	}
    }
    
    /** Add a mapping from a proto interface field to a specific node field,
     *  mapping is defined in the <B>ISField</B>.
     */
    public void addISField( ISField is ) {
        if ( isList == null ) {
            isList = new Vector();
        }
        isList.addElement( is );
    }
    
    
    /** Get the number of IS fields in the PROTOBase
     */
    public int getNumberISfields() {
        if ( isList == null ) {
            return( 0 );
        }
        return( isList.size() );
    }
    
    /** Get a specific IS field in the PROTOBase 
     */
    public ISField getISfield( int offset ) {
        return( (ISField)isList.elementAt( offset ));
    }
    
    /** Get the list of ISfields */
    public Vector getISfields() {
        return( isList );
    }
    
    public FieldDescriptor getFieldDescriptor( String fieldName ) {
        if ( isList == null ) {
            return( null );
        }
        int n = isList.size();
		for ( int i = 0; i < n; i++ ) {
			ISField is = (ISField)isList.elementAt( i );
			String isFieldId = is.getFieldId();
			if ( isFieldId != null ) {
			    if ( isFieldId.compareTo( fieldName ) == 0 ) {
			        Field nodeField = is.getNodeField();
			        String nodeParentBaseName = null;
			        String nodeFieldId = null;
			        if ( nodeField != null ) {
    			        VrmlElement nodeFieldParent = nodeField.getParent();
    			        if ( nodeFieldParent instanceof Node ) {
    			            nodeParentBaseName = ((Node)nodeFieldParent).getBaseName();
    			        }
    			        nodeFieldId = nodeField.getFieldId();
    			    }
    			    if (( nodeParentBaseName != null ) && ( nodeFieldId != null )) {
    			        return( VRML97.getFieldDescriptor( nodeParentBaseName, nodeFieldId ));
    			    }
			    }
			}
		}
		return( null );
	}
	
	/** Get the vector of interface fields.
	 *
	 *  @return  null if there is no interface for the PROTO, otherwise
	 *     a vector of interface fields.
	 */
	public Vector getInterfaceVector() {
	    return( protoInterface );
	}

    /** Get a list of field names defined for the PROTO.
     */
    public String[] getFieldNames() {
        if ( protoInterface == null ) {
            return( null );
        } else {
            int size = protoInterface.size();
            if ( size == 0 ) {
                return( null );
            }
            String[] results = new String[ size ];
            for ( int i = 0; i < size; i++ ) {
	            Field testField = (Field)protoInterface.elementAt( i );
	            results[ i ] = testField.getFieldId();
            }
            return( results );
        }
    }

    /** Get the Scene representing the body of the PROTO declaration */
	public Scene getPROTObody() {
	    int nChildren = numberChildren();
	    for ( int i = 0; i < nChildren; i++ ) {
	        VrmlElement vle = getChildAt( i );
	        if ( vle instanceof Scene ) {
	            return( (Scene)vle );
	        }
	    }
	    return( null );
	}
	
	/** Get the first node of the PROTO, indicate type for the PROTO */
	public Node getPROTONodeType() {
	    return( getNodeAt( 0 ));
	}
	
	/** Get a specific Node in the PROTO */
	public Node getNodeAt( int offset ) {
	    Scene s = getPROTObody();
	    if ( s != null ) {
			VrmlElement firstNode = s.getChildAt( offset );
			if ( firstNode instanceof Node ) {
				return( (Node)firstNode );
			}
		}
		return( null );
	}
	

        
	/**
	 *  Get the interface declaration based on the name of the field.  "IS" fields refer
	 *  to the Field returned by this call.
	 */
	public Field getInterfaceDeclaration( String fieldName ) {
	    if ( protoInterface == null ) {
	        return( null );
	    }
	    int interfaceSize = protoInterface.size();
	    for ( int i = 0; i < interfaceSize; i++ ) {
	        Field testField = (Field)protoInterface.elementAt( i );
	        if ( testField.getFieldId().compareTo( fieldName ) == 0 ) {
	            return( testField );
	        }
	    }
	    return( null );
    }

    /**  Is the field name included in the PROTO interface */
    public boolean isValidFieldId( String fieldName ) {
        if ( protoInterface == null ) {
            return( false );
        }
        int interfaceSize = protoInterface.size();
        for ( int i = 0; i < interfaceSize; i++ ) {
            Field testField = (Field)protoInterface.elementAt( i );
            if ( testField.getFieldId().compareTo( fieldName ) == 0 ) {
                return( true );
            }
        }
        return( false );
    }
	
	/** Get string identifying PROTO */
	public String getId() {
		VrmlElement v = getChildAt( 0 );
		if ( v instanceof NodeTypeId ) {
			NodeTypeId nodeId = (NodeTypeId)v;
			return( nodeId.getName() );
		} else {
			return( null );
		}
	}
	
	/** Set the id of a PROTO, effectively renaming it.  Note this
	 *  does not rename instances.
	 */
	public void setId( String newId ) {
	    VrmlElement v = getChildAt( 0 );
	    if ( v instanceof NodeTypeId ) {
	        VrmlElement root = getRoot();
	        if ( root instanceof Scene ) {
	            Scene sroot = (Scene)root;
	            TokenEnumerator te = sroot.getTokenEnumerator();
	            te.replace( v.getFirstTokenOffset(), newId );
	        }
	    }
	}
	
	public String getNodeName() {
	    return( "PROTO " + getId() );
	}

    /** Get the built in node type for the PROTO or EXTERNPROTO */
	abstract public String getBuiltInNodeType();
	
	/** Add interface declaration to PROTO/EXTERNPROTO interface list */
	public void addInterface( Field f ) {
	    if ( protoInterface == null ) {
	        protoInterface = new Vector();
	    }
	    protoInterface.addElement( f );
	}

	/** Get the number of interface fields of a particular type */
	public int getInterfaceCount( int type ) {
	    int count = 0;
	    if ( protoInterface != null ) {
	        int size = protoInterface.size();
	        for ( int i = 0; i < size; i++ ) {
	            Field f = (Field)protoInterface.elementAt( i );
	            if ( f.getInterfaceType() == type ) {
	                count++;
	            }
	        }
	    }
	    return( count );
	}

	/** Get the field id closest to the unknown field id parameter */
	public String getClosestFieldId( String test ) {
	    if ( protoInterface == null ) {
	        return( null );
	    }
	    int interfaceSize = protoInterface.size();
	    int testScore = 0;
	    String result = null;
	    for ( int i = 0; i < interfaceSize; i++ ) {
	        Field f = (Field)protoInterface.elementAt( i );
	        String testId = f.getFieldId();
	        int score = Spelling.getMatchScore( testId, test );
	        if ( score > testScore ) {
	            result = testId;
	            testScore = score;
	        }
	    }
	    return( result );
	}
}
