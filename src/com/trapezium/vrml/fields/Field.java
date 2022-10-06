/*
 * @(#)Field.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.fields;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.MultipleTokenElement;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.FieldId;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.NodeType;
import com.trapezium.vrml.grammar.VRML97;
import com.trapezium.pattern.Visitor;

/**
 *  Base class for all field instances.
 *
 *  A Field is the range of tokens from the field id to the last token 
 *  in the field value..
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 13 Jan 1998
 *
 *  @since           1.0
 */
public class Field extends MultipleTokenElement {
	/** used only for indicating if PROTO field declarations are used by IS */
	// need to move this over into PROTO interface vector
	boolean inUse = false;
	public void markInUse() {
		inUse = true;
	}
	public boolean isInUse() {
		return( inUse );
	}
	
	/** check if this field is an ISField */
	public boolean isISfield() {
	    if ( fieldValue != null ) {
	        return( fieldValue.getChildAt( 0 ) instanceof ISField );
	    } else {
	        return( false );
	    }
	}

	
	/** base constructor */
	public Field( int tokenOffset ) {
		super( tokenOffset );
	}

    FieldValue fieldValue;
    
	/** Get a FieldValue for this field */
	public FieldValue getFieldValue() {
	    return( fieldValue );
	}
	
	public void setFieldValue( FieldValue fv ) {
	    fieldValue = fv;
	    if ( fv != null ) {
    	    fieldValue.setParent( this );
    	}
	}
	
	public void fieldValueTraverse( Visitor v ) {
	    if ( fieldValue != null ) {
	        if ( v.isTwoPassVisitor() ) {
	            fieldValue.twoPassTraverse( v );
	        } else {
       	        fieldValue.traverse( v );
       	    }
	    }
	}

	/**
	 *  Get a string identifying the type of this field.
	 */
	int fieldType;
	public int getFieldType() {
		return( fieldType );
	}
	public void setFieldType( String fieldType ) {
	    setFieldType( VRML97.typeStrToInt( fieldType ));
	}
	public void setFieldType( int ft ) {
		fieldType = ft;
	}

    /** Template, get the interface type
     *
     *  @returns one of the following: VRML97.exposedField, VRML97.field,
     *     VRML97.eventIn, VRML97.eventOut
     */
    public int getInterfaceType() {
        return( -1 );
    }
    
	String fieldId;
	
	/** Get the name identifying this field */
	public String getFieldId() {
	    return( fieldId );
	}
	
	/** Set the name identifying this field */
	public void setFieldId( String id ) {
	    fieldId = id;
	}

    /** Get the Node that is the parent of this Field */
	public Node getNodeParent() {
		VrmlElement p = getParent();
		while (( p != null ) && ( !( p instanceof Node ))) {
			p = p.getParent();
		}
		if ( p != null ) {
			return( (Node)p );
		} else {
			return( null );
		}
	}
	
	/** Get the Node that is the field value of this field, bypassing DEF/USE */
	public Node getNodeValue() {
	    // this is convenience routine for Fields representing Nodes
	    if ( fieldValue instanceof SFNodeValue ) {
	        VrmlElement node = fieldValue.getChildAt( 0 );
	        if ( node instanceof Node ) {
	            if ( node instanceof DEFUSENode ) {
	                node = ((DEFUSENode)node).getNode();
	            }
	            return( (Node)node );
	        }
	    }
	    return( null );
	}
	
	/** Get the Node that is the field value, returning DEF/USE if present */
	public Node getNode() {
	    if ( fieldValue instanceof SFNodeValue ) {
	        VrmlElement node = fieldValue.getChildAt( 0 );
	        if ( node instanceof Node ) {
	            return( (Node)node );
	        }
	    }
	    return( null );
	}

    /** Get a float value at a particular offset in a list for this Field.
     *
     *  @param dataSource TokenEnumerator containing data
     *  @param offset offset of the NumberToken to get the value for
     *  @return the float value at the offset
     *  @throws IndexOutOfBoundsException if the offset is outside token range
     */
     class GetFloatOptimizer {
        final int SaveSize = 20;
        int[] offsets;
        int[] scannerValues;
        int firstOffset;
        int lastOffset;
        int idx;
        
        public GetFloatOptimizer( TokenEnumerator dataSource, int firstOffset, int lastOffset ) {
            this.firstOffset = firstOffset;
            this.lastOffset = lastOffset;
            idx = 0;
            offsets = new int[SaveSize];
            scannerValues = new int[SaveSize];
            idx = 0;
            setup( dataSource );
        }
        
        void setup( TokenEnumerator dataSource ) {
            int scanSize = ( lastOffset - firstOffset )/( SaveSize + 1 );
            if ( scanSize <= 0 ) {
                return;
            }
            
            int counter = 1;
            int scanner = firstOffset;
            dataSource.setState( scanner );
            while ( scanner < lastOffset ) {
                scanner = dataSource.skipToNumber( scanSize );
                if ( scanner == -1 ) {
                    break;
                }
                offsets[ idx ] = scanSize*counter;
                scannerValues[ idx ] = scanner;
                idx++;
                counter++;
                if ( idx >= SaveSize ) {
                    break;
                }
            }
        }
                
        
        int skipToNumber( TokenEnumerator dataSource, int offset ) {
            int originalOffset = offset;
            int foundIdx = -1;
            int foundDistance = -1;
            for ( int i = 0; i < idx; i++ ) {
                if ( offsets[i] < offset ) {
                    if ( foundIdx == -1 ) {
                        foundIdx = i;
                        foundDistance = offsets[i];
                    } else if ( offsets[i] > foundDistance ) {
                        foundIdx = i;
                        foundDistance = offsets[i];
                    }
                } else {
                    break;
                }
            }
            if ( foundIdx != -1 ) {
                offset -= offsets[foundIdx];
                dataSource.setState( scannerValues[foundIdx] );
            } else {
                dataSource.setState( firstOffset );
            }
            return( dataSource.skipToNumber( offset ));
        }
     }
     transient GetFloatOptimizer getFloatOptimizer;
	public float getFloat( TokenEnumerator dataSource, int offset ) throws IndexOutOfBoundsException {
	    if ( getFloatOptimizer == null ) {
	        getFloatOptimizer = new GetFloatOptimizer( dataSource, getFirstTokenOffset(), getLastTokenOffset() );
	    }
	    int oldState = dataSource.getState();
	    int scanner = getFloatOptimizer.skipToNumber( dataSource, offset );
//        dataSource.setState( firstToken );
//	    int scanner = dataSource.skipToNumber( offset );
//        if (( scanner >= lastToken ) || ( scanner == -1 )) {
        if ( scanner == -1 ) {
            dataSource.setState( oldState );
            throw new IndexOutOfBoundsException( "Bad offset " + offset );
        }
        float result = dataSource.getFloat( scanner );
        dataSource.setState( oldState );
        return( result );
	}

	/** Clone a field value into a new Field */
	protected void cloneFieldValue( Field result, VrmlElement protoInstance ) {
	    result.setFieldId( getFieldId() );
	    FieldValue fv = getFieldValue();
	    if ( fv != null ) {
	        FieldValue fvClone = (FieldValue)fv.vrmlClone( protoInstance );
	        VrmlElement preservedParent = null;
	        if ( fvClone == fv ) {
	            preservedParent = fv.getParent();
	        }
	        result.setFieldValue( fvClone );
	        if ( preservedParent != null ) {
	            fv.setParent( preservedParent );
	        }
	    }
	}
}


