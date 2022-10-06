/*
 * @(#)ScriptInstance.java
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
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElementNotFoundException;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.grammar.DEFNameFactory;
import com.trapezium.vrml.grammar.VRML97;
import com.trapezium.vrml.grammar.NodeRule;
import com.trapezium.vrml.grammar.ScriptRestrictedInterfaceDeclarationRule;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *  Scene graph component for a Script.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 7 April 1998, added "getFieldNames" method,
 *                         8 March 1998, added "getInterfaceCount" method,
 *  @version         1.1, 8 Jan 1998
 *
 *  @since           1.0
 */
public class ScriptInstance extends Node {
    Node scriptBase;
    Vector scriptInterface;

    /** Script node instance constructor */
    public ScriptInstance() {
        super();
        try {
            scriptBase = VRML97.NodeFactory( "Script" );
        } catch ( Exception e ) {
        }
    }

    /** Get the data type of the field in String form.
     *
     *  @return VRML97.SFxxxx or VRML97.MFxxxx if known,
     *     otherwise returns VRML97.UnknownType.
     */
	public String getFieldType( String fieldId ) {
	    String s = super.getFieldType( fieldId );
	    if ( s == null ) {
	        Field f = getInterfaceField( fieldId );
	        if ( f != null ) {
                return( VRML97.getFieldTypeString( f.getFieldType() ));
            }
	    }
	    return( s );
	}

    public String getNodeName() {
        return( scriptBase.getNodeName() );
    }
    
    public String getBaseName() {
        return( scriptBase.getBaseName() );
    }

	/** Spelling correction attempt, get a field id with close spelling */
	public String getClosestFieldId( String fieldName ) {
	    return( scriptBase.getClosestFieldId( fieldName ));
	}

    /** Remove a field from the interface if it is there */
    public void removeField( Field f ) throws VrmlElementNotFoundException {
        super.removeField( f );
        removeInterface( f );
    }
    
	public Field getInterfaceDeclaration( String fieldName ) {
	    return( null );
    }

	/** Check if the field is a built-in or user-defined Script field.
	 *
	 *  @return true if the field is a built-in Script field, or a user
	 *     defined Script field.  Otherwise, returns false.
	 */
	public boolean isValidFieldId( String fieldName ) {
	    // first check the built in field ids
	    if ( scriptBase.isValidFieldId( fieldName )) {
	        return( true );
	    }
	    // next check the interface fields
	    if ( scriptInterface != null ) {
	        int interfaceSize = scriptInterface.size();
	        for ( int i = 0; i < interfaceSize; i++ ) {
	            Field f = (Field)scriptInterface.elementAt( i );
	            if ( f.getFieldId().compareTo( fieldName ) == 0 ) {
	                return( true );
	            }
	        }
	    }
        return( false );
	}
	
	/** get the interface field of the given name */
	public Field getInterfaceField( String fieldId ) {
	    if ( scriptInterface != null ) {
	        int interfaceSize = scriptInterface.size();
	        for ( int i = 0; i < interfaceSize; i++ ) {
	            Field f = (Field)scriptInterface.elementAt( i );
	            if ( f.getFieldId().compareTo( fieldId ) == 0 ) {
	                return( f );
	            }
	        }
	    }
	    return( null );
	}

    /** add a user defined interface field to this script node */
	public void addInterface( Field f ) {
	    if ( scriptInterface == null ) {
	        scriptInterface = new Vector();
	    }
	    scriptInterface.addElement( f );
	}

	/** remove an interface element */
	public void removeInterface( Field f ) {
		if ( scriptInterface != null ) {
			scriptInterface.removeElement( f );
		}
	}

	ScriptRestrictedInterfaceDeclarationRule srid = null;
	void createScriptParsingRule() {
		Scene s = (Scene)getScene();
		DEFNameFactory defNameFactory = null;
		if ( s != null ) {
			defNameFactory = s.getDEFNameFactory();
		}
		NodeRule nodeRule = new NodeRule( defNameFactory );
		srid = new ScriptRestrictedInterfaceDeclarationRule( nodeRule );
	}

	/** add a user defined interface field to this script node */
	public Field addInterface( String s ) {
		if ( srid == null ) {
			createScriptParsingRule();
		}
		Scene scene = new Scene();
		TokenEnumerator tokenEnumerator = new TokenEnumerator( s );
		scene.setTokenEnumerator( tokenEnumerator );
		tokenEnumerator.setState( 0 );
		ScriptInstance sicopy = new ScriptInstance();
		sicopy.setParent( scene );
		Field f = srid.Build( 0, tokenEnumerator, scene, sicopy );
		if ( f != null ) {
			// temporarily addInterface
			addInterface( f );
			try {
    			Field newField = setField( f );
    			removeInterface( f );
    			if ( newField != null ) {
    				addInterface( newField );
    			}
    			return( newField );
    		} catch ( Exception e ) {
    		    e.printStackTrace();
    		}
		}
		return( null );
	}

    /** Get the number of user defined fields of a particular type */
	public int getInterfaceCount( int type ) {
	    int count = 0;
	    if ( scriptInterface != null ) {
	        int size = scriptInterface.size();
	        for ( int i = 0; i < size; i++ ) {
	            Field f = (Field)scriptInterface.elementAt( i );
	            if ( f.getInterfaceType() == type ) {
	                count++;
	            }
	        }
	    }
	    return( count );
	}

    /** get the VRML97 interface type for a field:  eventIn, eventOut,
     *  exposedField, or field.
     */
	public int getInterfaceType( String fieldId ) {
	    int interfaceType = VRML97.getInterfaceType( getNodeName(), fieldId );
	    if ( interfaceType == VRML97.UnknownInterfaceType ) {
	        Field f = getInterfaceField( fieldId );
	        if ( f != null ) {
	            return( f.getInterfaceType() );
	        }
	    }
	    return( interfaceType );
	}

    /** Get a list of field names associated with this instance,
     *  includes the built in script fields.
     *
     *  To get the FieldDescriptor associated with a particular field,
     *  use:  FieldDescriptor fd = VRML97.getFieldDescriptor( getNodeName(), fieldName )
     */
    public String[] getFieldNames() {
        Hashtable hashTable = VRML97.getFieldTable( "Script" );
        if ( hashTable == null ) {
            return( null );
        } else {
            int size = hashTable.size();
            int builtInSize = size;
            if ( scriptInterface != null ) {
                size += scriptInterface.size();
            }
            String[] result = new String[ size ];
            Enumeration keys = hashTable.keys();
            int resultIdx = 0;
            while ( keys.hasMoreElements() ) {
                String x = (String)keys.nextElement();
                if ( resultIdx < size ) {
                    result[ resultIdx++ ] = x;
                }
            }
            for ( int i = builtInSize; i < size; i++ ) {
	            Field f = (Field)scriptInterface.elementAt( i - builtInSize );
                result[ resultIdx++ ] = f.getFieldId();
            }
            return( result );
        }
    }
}
