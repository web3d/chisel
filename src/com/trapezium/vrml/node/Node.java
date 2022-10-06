/*
 * @(#)Node.java
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
import com.trapezium.parse.TokenTypes;
import com.trapezium.vrml.DEFResolver;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.SFBoolValue;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.MFFloatValue;
import com.trapezium.vrml.fields.MFNodeValue;
import com.trapezium.vrml.fields.SFInt32Value;
import com.trapezium.vrml.fields.SFFloatValue;

import com.trapezium.vrml.grammar.Spelling;
import com.trapezium.vrml.grammar.VRML97;
import com.trapezium.vrml.grammar.FieldDescriptor;
import com.trapezium.vrml.grammar.DEFNameFactory;
import com.trapezium.util.ReturnInteger;
import com.trapezium.vrml.visitor.ChildCloner;
import com.trapezium.vrml.visitor.AdjustmentVisitor;
import com.trapezium.vrml.VrmlElementNotFoundException;
import java.io.File;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *  Base class for all nodes.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 21 May 1998, added DEFResolver
 *                   1.12, 29 March 1998
 *                   added methods for moving VrmlElements between scene graphs
 *  @version         1.1, 14 Jan 1998
 *
 *  @since           1.0
 */
public class Node extends Field {
	/**
	 *  Base class for all nodes.
	 */
	public Node( int tokenOffset ) {
		super( tokenOffset );
	}

    /** Get the node value for a particular field.
     *
     *  @param fieldName the name of the node containing field.
     *
     *  @return the Node contained in the field with the given name, or null if that
     *     field is not explicitly specified in the vrml file.
     */
    public Node getNodeValue( String fieldName ) {
        Field f = getField( fieldName );
        if ( f != null ) {
            return( f.getNodeValue() );
        } else {
            return( null );
        }
    }

	/** Template method, PROTO overrides this, in some cases we need to know if
	 *  scene graph component is a built in node, or part of a PROTO.
	 */
	public boolean isPROTOnode() {
	    return( false );
	}

	/** Template method, DEFUSENode overrides */
	public boolean isDEF() {
	    return( false );
	}

	/** Template method, DEFUSENode overrides */
	public boolean isUSE() {
	    return( false );
	}

	/** Template method, DEFUSENode overrides */
	public boolean isDEForUSE() {
	    return( false );
	}

	/** Template method, overridden by bindableNodes */
	public boolean isBindable() {
	    return( false );
	}

	/** Template method, overridden by Sensor */
	public boolean isSensor() {
	    return( false );
	}


	/** Is this node included in a generated portion of a PROTO instance */
	public boolean generatedParent() {
	    VrmlElement scanner = getParent();
	    while ( scanner != null ) {
	        if ( scanner.getFirstTokenOffset() == -1 ) {
	            return( true );
	        }
	        scanner = scanner.getParent();
	    }
	    return( false );
	}

    /** Get the DEF node that is the parent of this node.
     */
	public DEFUSENode getDEFparent() {
		VrmlElement scanner = getParent();
		while ( scanner != null ) {
			if ( scanner instanceof DEFUSENode ) {
				DEFUSENode dun = (DEFUSENode)scanner;
				return( dun );
			}
			scanner = scanner.getParent();
		}
		return( null );
	}

	/** Get a parent of a particular node type.
	 *
	 *  @param nodeName the type of parent to search for
	 */
	public Node getParent( String nodeName ) {
	    VrmlElement scanner = getParent();
	    while ( scanner != null ) {
	        if ( scanner instanceof Node ) {
	            Node n = (Node)scanner;
	            if ( nodeName.compareTo( n.getBaseName() ) == 0 ) {
	                return( n );
	            }
	        }
	        scanner = scanner.getParent();
	    }
	    return( null );
	}


	/** Overrides base class setError, used for PROTO node errors that
	 *  get moved up hierarchy to get PROTO instance declaration.
	 */
	public void setError( String s ) {
	    if ( getFirstTokenOffset() == -1 ) {
	        VrmlElement p = getParent();
	        if ( p != null ) {
    	        p.setError( s );
    	    }
	    } else {
	        super.setError( s );
	    }
	}

    /** Template for language extension syntax of PROTO and Script nodes */
    public void addInterface( Field f ) {
    }

    /** Template for interface count */
    public int getInterfaceCount( int type ) {
        return( 0 );
    }

	/** base constructor used by generated built in nodes. */
	public Node() {
		super( -1 );
	}

    /** clone object */
    public VrmlElement vrmlClone( VrmlElement pi ) {
        Node nonDEFnode = getNode();
        if ( nonDEFnode == null ) {
            return( null );
        }
        String nodeName = nonDEFnode.getNodeName();
        try {
            Node n = VRML97.NodeFactory( nodeName );
            ChildCloner cc = new ChildCloner( n, pi );
            nonDEFnode.traverse( cc );
            return( n );
        } catch( Exception e ) {
            return( null );
        }
    }

    /** Get the boolean value of a field, uses default if field not in file.
     *
     *  This is only called for valid boolean fields, no check done.
     */
	public boolean getBoolValue( String fieldName ) {
		Field f = getField( fieldName );
		if ( f == null ) {
		    return( VRML97.getDefaultBoolValue( getNodeName(), fieldName ));
		} else {
    		SFBoolValue bf = (SFBoolValue)f.getFieldValue();
    		return( bf.getValue() );
    	}
	}
	
	
	/** Get the MFFieldValue for a node field.
	 *
	 *  @param node the name of the node containing the MFFieldValue, e.g.
	 *     "coord" of IndexedFaceSet contains a Coordinate node
	 *  @param nodeField the name of the field containing the MFFieldValue,
	 *     e.g. "point" of the Coordinate node
	 *
	 *  @return the MFFieldValue of the node/nodeField, or null if not found in the
	 *     text
	 */
	public MFFieldValue getMFfield( String node, String nodeField ) {
        Field sfnode = getField( node );
        if ( sfnode != null ) {
            FieldValue fv = sfnode.getFieldValue();
            if ( fv instanceof SFNodeValue ) {
                SFNodeValue sfn = (SFNodeValue)fv;
                Node n = sfn.getNode();
                if ( n != null ) {
                    Field mffield = n.getField( nodeField );
                    if ( mffield != null ) {
                        FieldValue mfcfv = mffield.getFieldValue();
                        if ( mfcfv instanceof MFFieldValue ) {
                            return( (MFFieldValue)mfcfv );
                        }
                    }
                }
            }
        }
        return( null );
    }

    /** Get the integer value contained in a particular field.
     *
     *  @param fieldName the name of the field
     *
     *  @return an int value of the named field, or 0 if the field wasn't specified
     *     in the text or is invalid for the node.
     */
	public int getIntValue( String fieldName ) {
		Field f = getField( fieldName );
		if ( f != null ) {
		    FieldValue fv = f.getFieldValue();
		    if ( fv instanceof SFInt32Value ) {
		        return( ((SFInt32Value)fv).getIntValue() );
		    }
		}
		return( 0 );
	}
    
    
    /** Get the float value contained in a particular field.
     *
     *  @param fieldName the name of the field
     *
     *  @return a float value of the named field, or 0 if the field wasn't specified
     *     in the text or is invalid for the node.
     */
	public float getFloatValue( TokenEnumerator dataSource, String fieldName ) {
		Field f = getField( fieldName );
	    if ( f != null ) {
	        FieldValue fv = f.getFieldValue();
	        if ( fv instanceof SFFloatValue ) {
	            return( dataSource.getFloat( fv.getFirstTokenOffset() ));
	        }
	    }
	    return( 0f );
	}
	
	/** Get the float array contained in a particular field.
	 *
	 *  @param fieldName the name of the field
	 *
	 *  @return a float array of the named field, or null if the field wasn't specified
	 *     in the text of the file, or is not valid for the node.
	 */
	public float[] getFloatArray( String fieldName ) {
	    Field f = getField( fieldName );
	    if ( f != null ) {
	        FieldValue fv = f.getFieldValue();
	        if ( fv instanceof MFFloatValue ) {
	            return( ((MFFloatValue)fv).getFloatArray() );
	        }
	    }
	    return( null );
	}
 
        
	/** get the node name, template method, overridden by generated code */
	public String getNodeName() {
	    return( null );
	}

	/**
	 *  Find a field given its name, only finds explicitly defined fields.
	 *
	 *  @return  Field object of the given name, or null if the field
	 *     is not explicitly mentioned in the VRML 2.0 file.
	 */
	public Field getField( String fieldName ) {
		if ( fieldName == null ) {
			return( null );
		} else {
		    int nChildren = numberChildren();
			for ( int i = 0; i < nChildren; i++ ) {
				VrmlElement e = getChildAt( i );
				if ( e instanceof Field ) {
					Field f = (Field)e;
					String id = f.getFieldId();
					if ( id == null ) {
//						System.out.println( "null name for field " + i );
					} else if ( fieldName.compareTo( id ) == 0 ) {
						return( f );
					}
				}
			}
		}
		return( null );
	}

    /** Get the number of explicitly declared fields for this Node. */
	public int getNumberFields() {
		int fieldCount = 0;
	    int nChildren = numberChildren();
		for ( int i = 0; i < nChildren; i++ ) {
			VrmlElement e = getChildAt( i );
			if ( e instanceof Field ) {
				fieldCount++;
			}
		}
		return( fieldCount );
	}

    /** Get the field at a particular offset */
	public Field getFieldAt( int offset ) {
		int fieldCount = 0;
	    int nChildren = numberChildren();
		for ( int i = 0; i < nChildren; i++ ) {
			VrmlElement e = getChildAt( i );
			if ( e instanceof Field ) {
				if ( fieldCount == offset ) {
					return( (Field)e );
				}
				fieldCount++;
			}
		}
		return( null );
	}

    /** Get the IS declaration associated with a field */
	public Field getISfield( String fieldName ) {
		if ( fieldName == null ) {
			return( null );
		} else {
		    int nChildren = numberChildren();
			for ( int i = 0; i < nChildren; i++ ) {
				VrmlElement e = getChildAt( i );
				if ( e instanceof ISField ) {
					Field f = (Field)e;
					if ( f.getName() == null ) {
					} else if ( fieldName.compareTo( f.getName() ) == 0 ) {
						return( f );
					}
				}
			}
		}
		return( null );
	}

    /** Get the last field in the Node
     */
    public Field getLastField() {
        int nChildren = numberChildren();
        for ( int i = nChildren-1; i >= 0; i-- ) {
            VrmlElement e = getChildAt( i );
            if ( e instanceof Field ) {
                return( (Field)e );
            }
        }
        return( null );
    }

    /** Get a particular child node from the "children" field.
     */
    public Node getChildNode( int childNodeOffset ) {
        Field children = getField( "children" );
        if ( children != null ) {
            FieldValue fv = children.getFieldValue();
            if ( fv instanceof MFNodeValue ) {
                MFNodeValue nodeList = (MFNodeValue)fv;
                return( nodeList.getChildNode( childNodeOffset ));
            }
        }
        return( null );
    }

	/**
	 *  Find a field that contains most of the letters of the input name.
	 */
	public String getClosestFieldId( String fieldName ) {
	    ReturnInteger score = new ReturnInteger();
	    String closest = VRML97.getClosestFieldId( getNodeName(), fieldName, score );
	    if (( closest == null ) || ( score.getValue() < Spelling.Threshhold( closest ))) {
	        return( null );
	    } else {
	        return( closest );
	    }
	}


	/**
	 * Get a boolean field value, returns false if object structure not what is
	 * expected.
	 *
	 *  @param name of the field to check
	 *  @return true if the boolean field is explicitly true, false if the
	 *     boolean field is explicitly false, otherwise returns the default
	 *     value of the boolean field.  Returns false if the field doesn't
	 *     exist in this node, or exists but is not a boolean type.
	 */
	public boolean getBoolean( String fieldName ) {
		FieldValue fv = getFieldValue( fieldName );
		if ( fv == null ) {
		    return( VRML97.getDefaultBoolValue( getNodeName(), fieldName ));
		} else if ( fv instanceof SFBoolValue ) {
			SFBoolValue sfbv = (SFBoolValue)fv;
			return( sfbv.getValue() );
		} else {
    		return( false );
    	}
	}

    /** Get the FieldValue for a field of the given name.
     *
     *  @param  fieldName  indicates the field to access
     *  @return the FieldValue for that field, or null if the field was
     *     not explicitly declared in the file.
     */
	public FieldValue getFieldValue( String fieldName ) {
		Field f = getField( fieldName );
		if ( f != null ) {
			return( f.getFieldValue() );
		}
		return( null );
	}

	/**
	 *  Base class getNode just returns self.  DEFUSENode overrides this to get associated Node
	 */
	public Node getNode() {
		return( this );
	}

	/** Check if field is valid accoring to VRML97 specification.
	 *
	 * @return  true if the field is valid, otherwise false
	 */
	public boolean isValidFieldId( String fieldId ) {
	    return( VRML97.isValidFieldId( getNodeName(), fieldId ));
	}


	/** Is this field id one of the implicitly defined eventIn or
     *  eventOut fields associated with an exposedField?
     */
	public boolean isImplicitFieldId( String fieldId ) {
	    if ( fieldId.indexOf( "set_" ) == 0 ) {
	        String unset = fieldId.substring( 4 );
	        int unsetType = getInterfaceType( unset );
	        if ( unsetType == VRML97.exposedField ) {
	            return( true );
	        }
	    } else if ( fieldId.indexOf( "_changed" ) > 0 ) {
	        int cidx = fieldId.indexOf( "_changed" );
	        String unchanged = fieldId.substring( 0, cidx );
	        int unchangedType = getInterfaceType( unchanged );
	        if ( unchangedType == VRML97.exposedField ) {
	            return( true );
	        }
	    }
	    return( false );
	}

    /** Get the interface type of a field.
     *
     *  @return  one of the built-in VRML97 interface types:
     *    VRML97.field, VRML97.exposedField, VRML97.eventIn, VRML97.eventOut,
     *    returns VRML97.UnknownInterfaceType if field id is unknown
     */
	public int getInterfaceType( String fieldId ) {
	    return( VRML97.getInterfaceType( getNodeName(), fieldId ));
	}

    /** Get the data type of the field in String form.
     *
     *  @return VRML97.SFxxxx or VRML97.MFxxxx if known,
     *     otherwise returns VRML97.UnknownType.
     */
	public String getFieldType( String fieldId ) {
	    return( VRML97.getFieldTypeString( getNodeName(), fieldId ));
	}

    /** Add a node to a <B>children</B> field of a grouping node.
     *
     *  @param sourceNode node to add to children node, can exist in the same or a different Scene graph
     *  @throws NotAGroupingNodeException if the node has no <B>children</B> field
     *  @throws InvalidChildNodeException if the node is not an acceptable child node according to VRML spec
     *  @throws InvalidFieldException if we fail creating a <B>children</B> field when necessary
     *
     *  @return new Node added
     */
    public Node addChildNode( Node sourceNode ) throws InvalidChildNodeException, NotAGroupingNodeException, InvalidFieldException {
        if ( !NodeType.isGroupingNode( getNodeName() )) {
            throw new NotAGroupingNodeException();
        }
        if (( sourceNode == null ) || NodeType.isBadChild( sourceNode.getNodeName() )) {
            throw new InvalidChildNodeException();
        }
        // possibly rename conflicting DEFs, but do this before the copy
        Scene scene = (Scene)getScene();
        DEFResolver defResolver = null;
        if ( scene != null ) {
            defResolver = scene.createDEFNames( sourceNode );
        }
        Field children = getField( "children" );

        // if there is no children field, create it
        if ( children == null ) {
            setField( new String( "children []" ));
            children = getField( "children" );
        } else {
            TokenEnumerator te = scene.getTokenEnumerator();
            // if children field exists, make sure it has brackets
            te.enableCommentSkipping();
            te.setState( children.getFirstTokenOffset() );
            int leftBracket = te.getNextToken();
            int rightBracket = children.getLastTokenOffset();
            if ( !te.isLeftBracket( leftBracket )) {
                te.insert( leftBracket, "[", TokenTypes.LeftBracket );
                AdjustmentVisitor av = new AdjustmentVisitor( te, leftBracket, 1 );
                scene.traverse( av );
                // move rightBracket loc 1 to right due to left bracket insert
                rightBracket++;
                // more rightBracket loc 1 more to right because insertion point
                // is past end of children
                rightBracket++;
                te.insert( rightBracket, "]", TokenTypes.RightBracket );
                av = new AdjustmentVisitor( te, rightBracket, 1 );
                scene.traverse( av );
            }
            te.disableCommentSkipping();
        }

        // always insert right after left bracket
        TokenData tokenData = new TokenData( children );
        tokenData.setInsertionToken( children.getFirstTokenOffset(), TokenTypes.LeftBracket );
        TokenData newTokenData = new TokenData( sourceNode, TokenData.ReCreate );
        tokenData.insert( newTokenData );

        // add the new vrmlElement as a child
        FieldValue fv = children.getFieldValue();
        fv.addChild( newTokenData.getNode() );
        if ( defResolver != null ) {
            defResolver.resolve( newTokenData.getNode() );
        }
        return( newTokenData.getNode() );
    }

    /** Add a node to a <B>children</B> field of a grouping node.
     *
     *  @param sourceNode string form of VRML text describing a node
     *  @throws NotAGroupingNodeException if the node has no <B>children</B> field
     *  @throws InvalidChildNodeException if the node is not an acceptable child node according to VRML spec
     *  @throws InvalidFieldException if we fail creating a <B>children</B> field when necessary
     *
     *  @return new Node added
     */
    public Node addChildNode( String sourceNode ) throws InvalidChildNodeException, NotAGroupingNodeException, InvalidFieldException {
        Scene s = (Scene)getScene();
        DEFNameFactory dfn = null;
        if ( s != null ) {
            dfn = s.getDEFNameFactory();
        }
        TokenData newTokenData = new TokenData( sourceNode, dfn );
        return( addChildNode( newTokenData.getNode() ));
    }

    /** Add a node to a <B>children</B> field of a grouping node.
     *
     *  @param sourceNode File containing VRML text describing a node, may be gzipped,
     *     File must exist, this does nothing if file doesn't exist
     *  @throws NotAGroupingNodeException if the node has no <B>children</B> field
     *  @throws InvalidChildNodeException if the node is not an acceptable child node according to VRML spec
     *  @throws InvalidFieldException if we fail creating a <B>children</B> field when necessary
     *
     *  @return new Node added
     */
    public Node addChildNode( File sourceNode ) throws InvalidChildNodeException, NotAGroupingNodeException, InvalidFieldException {
        TokenData newTokenData = new TokenData( sourceNode );
        return( addChildNode( newTokenData.getNode() ));
    }

    /** Set a field value of a node by copying an existing Field into the
     *  node.  The existing Field may be in the same scene graph, or in
     *  a different scene graph.  The scene graph and the Scene's
     *  tokenEnumerator are updated, so that the scene graph may continue
     *  to be used, and the text of the file may be regenerated from the
     *  tokenEnumerator.
     *
     *  @param sourceField data field with values to set or replace in node
     *  @throws InvalidFieldException if the node has no such field,
     *  @throws InvalidNodeException if this is called for a USE node
	 *  @return the newly added Field
     */
    public Field setField( Field sourceField ) throws InvalidFieldException, InvalidNodeException {
        String fieldId = sourceField.getFieldId();
        if ( !isValidFieldId( fieldId )) {
            // if this is a ScriptInstance, there is no such thing as an invalid id
            if ( this instanceof ScriptInstance ) {
                ScriptInstance si = (ScriptInstance)this;
                return( si.addInterface( sourceField.getText() ));
            } else {
                throw new InvalidFieldException();
            }
        }
        try {
            Node tempParent = null;
            if ( this instanceof PROTOInstance ) {
                PROTOInstance pi = (PROTOInstance)this;
                Scene s = (Scene)getScene();
                tempParent = s.PROTOFactory( pi.getPROTOname() );
            } else {
                tempParent = VRML97.NodeFactory( getNodeName() );
            }
            tempParent.setParent( getScene() );
            TokenData newTokenData = new TokenData( sourceField, TokenData.ReCreateField, tempParent );
            Field existingField = getField( fieldId );
            if ( existingField != null ) {
                removeField( existingField );
            }
            // create TokenData for current node, insert field right after
            // the "{"
            TokenData tokenData = new TokenData( this );
            tokenData.setInsertionToken( getFirstTokenOffset(), TokenTypes.LeftBrace );
            tokenData.insert( newTokenData );
            addChild( newTokenData.getField() );
			return( newTokenData.getField() );
        } catch ( Exception e ) {
            System.out.println( "Exception: " + e );
            e.printStackTrace();
			return( null );
        }
    }

    /** Set a Field from TokenData, used when Field not in a Scene */
    Field setField( TokenData newTokenData ) throws InvalidFieldException, InvalidNodeException {
        Field sourceField = newTokenData.getField();
        if ( sourceField == null ) {
            throw new InvalidFieldException();
        }
        String fieldId = sourceField.getFieldId();
        if ( !isValidFieldId( fieldId )) {
            throw new InvalidFieldException();
        }
        try {
            Field existingField = getField( fieldId );
            if ( existingField != null ) {
                removeField( existingField );
            }
            TokenData tokenData = new TokenData( this );
            tokenData.setInsertionToken( getFirstTokenOffset(), TokenTypes.LeftBrace );
            tokenData.insert( newTokenData );
            addChild( newTokenData.getField() );
            return( newTokenData.getField() );
        } catch ( Exception e ) {
            System.out.println( "Exception: " + e );
            e.printStackTrace();
            return( null );
        }
    }

    /** Check if a field is USEd by a ROUTE.
     *
     *  @param fieldName the name of the field to check
     *  @return true if the field is a Node field that is USEd by a ROUTE, otherwise false
     */
    public boolean isFieldUsedByROUTE( String fieldName ) {
        Field f = getField( fieldName );
        if ( f != null ) {
            FieldValue fv = f.getFieldValue();
            if ( fv instanceof SFNodeValue ) {
                Node fn = ((SFNodeValue)fv).getNode();
                if ( fn instanceof DEFUSENode ) {
                    DEFUSENode dun = (DEFUSENode)fn;
                    return( dun.isUsedByROUTE() );
                }
            }
        }
        return( false );
    }


    /** Debugging detail dump of token enumerator to a file */
    void ddump( String s ) {
        Scene sc = (Scene)getScene();
        TokenEnumerator te = sc.getTokenEnumerator();
        te.detailDump( s );
    }

    /** Remove a Field from the TokenEnumerator and scene graph */
    public void removeField( Field f ) throws VrmlElementNotFoundException {
        removeVrmlElement( f );
    }

    /** Remove a Node from the "children" field, removed from TokenEnumerator and Scene graph */
    public void removeChildNode( Node n ) throws VrmlElementNotFoundException {
        removeVrmlElement( n );
    }


    /** Set a field value of a node.
     *
     *  @param sourceField String form of field
     *  @throws InvalidFieldException if the node has no such field
     *  @return resulting Field added, or null if an unexpected exception encountered
     */
    public Field setField( String sourceField ) throws InvalidFieldException {
        try {
            Node tempParent = VRML97.NodeFactory( getNodeName() );
            tempParent.setParent( getScene() );
            TokenData newTokenData = new TokenData( sourceField, tempParent, null );
            return( setField( newTokenData ));
        } catch ( InvalidFieldException e ) {
            throw e;
        } catch ( Exception e ) {
            e.printStackTrace();
            return( null );
        }
    }

    /** Set a field value of a node from a file
     *
     *  @param sourceField File containing Field text, only first field used if
     *    more than one exists in the file
     *  @throws InvalidFieldException if the node has no such field
     */
    public Field setField( File sourceField ) throws InvalidFieldException {
        try {
            Node tempParent = VRML97.NodeFactory( getNodeName() );
            tempParent.setParent( getScene() );
            TokenData newTokenData = new TokenData( sourceField, tempParent );
            return( setField( newTokenData ));
        } catch ( InvalidFieldException e ) {
            throw e;
        } catch ( Exception e ) {
            e.printStackTrace();
            return( null );
        }
    }

    /** Get a list of field names associated with a Node.
     *  ScriptInstance and PROTOInstance override this to include user
     *  defined fields.
     *
     *  To get the FieldDescriptor associated with a particular field,
     *  use:
     *    <PRE>
     *      FieldDescriptor fd = VRML97.getFieldDescriptor( getNodeName(), fieldName )
     *    </PRE>
     */
    String[] fieldNameList;
    public String[] getFieldNames() {
        if ( fieldNameList != null ) {
            return( fieldNameList );
        }
        Hashtable hashTable = VRML97.getFieldTable( getNodeName() );
        if ( hashTable == null ) {
            return( null );
        } else {
            int size = hashTable.size();
            fieldNameList = new String[ size ];
            Enumeration keys = hashTable.keys();
            int resultIdx = 0;
            while ( keys.hasMoreElements() ) {
                String x = (String)keys.nextElement();
                if ( resultIdx < size ) {
                    fieldNameList[ resultIdx++ ] = x;
                }
            }
            return( fieldNameList );
        }
    }

    /** Get the default field value as a string
     *
     *  @param fieldName the name of the field to get default value for
     *  @throws InvalidFieldException if there is no such field
     */
    public String getDefaultFieldValue( String fieldName ) throws InvalidFieldException {
        FieldDescriptor fd = VRML97.getFieldDescriptor( getNodeName(), fieldName );
        if ( fd == null ) {
            throw new InvalidFieldException();
        } else {
            return( fd.getDefaultValue() );
        }
    }
}


