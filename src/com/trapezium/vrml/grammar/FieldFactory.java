/*
 * @(#)FieldFactory.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.FieldId;
import com.trapezium.vrml.fields.MFField;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.SFStringValue;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.vrml.fields.MFNodeValue;
import com.trapezium.vrml.fields.ExposedField;
import com.trapezium.vrml.fields.NotExposedField;
import com.trapezium.vrml.fields.EventIn;
import com.trapezium.vrml.fields.EventOut;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.EXTERNPROTO;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.BadFieldId;
import com.trapezium.util.ReturnInteger;
import com.trapezium.vrml.node.NodeType;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.util.StringUtil;

/**
 *  Creates a VRML node field scene graph component.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 28 Feb 1998, added MFfield limit warnings
 *  @version         1.1, 19 Jan 1998
 *
 *  @since           1.1
 */
class FieldFactory {
    
    NodeRule nodeRule;
    NodeStatementRule nodeStatementRule;
    boolean valueCreationEnabled;
    
    /** Class constructor, calls main class constructor */
    FieldFactory( NodeRule nodeRule ) {
        this( nodeRule, true );
    }

    /** Main class constructor.
     *
     *  @param nodeRule NodeRule to be used in creating node instances
     *  @param valueCreation flag indicating whether a field declaration
     *     includes a value or not.  PROTO field declarations include
     *     initial values, EXTERNPROTO field declarations do not.
     */
    FieldFactory( NodeRule nodeRule, boolean valueCreation ) {
        this.nodeRule = nodeRule;
        this.valueCreationEnabled = valueCreation;
        nodeStatementRule = new NodeStatementRule( nodeRule );
    }
    
    /** create a field declaration
     *
     *  @param  vrmlFieldType  the type of field to create, see VRML97 for list of field types (SF..,MF.., etc.)
     *  @param  v  TokenEnumerator containing file text
     *  @param  scene  Scene containing the declaration
     *  @param  parent node containing the declaration
     *
     *  @return a Field scene graph component
     */
	Field CreateDeclaration( int vrmlFieldType, TokenEnumerator v, Scene scene, Node parent ) {
		try {
		    int tokenOffset = v.getCurrentTokenOffset();
		    v.breakLineAt( tokenOffset );
		    int fieldTypeOffset = v.getNextToken();
		    String fieldType = v.toString( fieldTypeOffset );
		    int fieldId = v.getNextToken();
		    		    
		    if ( !VRML97.isBuiltInType( fieldType )) {
		        ReturnInteger matchScore = new ReturnInteger();
		        String closestType = VRML97.getClosestType( fieldType, matchScore );
		        if (( closestType == null ) || ( Spelling.Threshhold( closestType )  > matchScore.getValue() )) {
		            BadFieldId bfi = new BadFieldId( fieldTypeOffset, "invalid field type" );
		            parent.addChild( bfi );
		            return( null );
    		    } else {
    		        BadFieldId bfi = new BadFieldId( fieldTypeOffset, "invalid field type, possibly '" + closestType + "'" );
    		        parent.addChild( bfi );
    		    }
    		    fieldType = closestType;
		    }
		    
	   
		    // First check if the field is valid within the context of the node
		    // If its not, search for a close match and go with that.
		    // If it isn't valid, and there is no close match, return a BadField object
		    String actualFieldType = null;
		    if ( fieldType.indexOf( "MF" ) == 0 ) {
		        actualFieldType = "MFField";
		    } else {
		        actualFieldType = fieldType;
		    }
			Class fieldClass = Class.forName("com.trapezium.vrml.fields." + actualFieldType );
	        if (fieldClass != null) {

				// create an instance
				Field f =  (Field) (fieldClass.newInstance());
    		    Field result = Create( vrmlFieldType, tokenOffset );
    		    result.setFieldType( vrmlFieldType );// jj added
    		    result.setFieldId( FieldId.getVRML2name( fieldId, v )); // jj added

				// set the first token for the field
				f.setFirstTokenOffset( tokenOffset );
				f.setFieldId( FieldId.getVRML2name( fieldId, v ));
				String fieldIdStr = f.getFieldId();
				if ( parent.getField( fieldIdStr ) != null ) {
				    f.setError( "Warning, duplicate declaration" );
				}
				if ( fieldIdStr != null ) {
				    if ( fieldIdStr.length() > Table7.NameLimit ) {
				        f.setError( "Nonconformance, field name length " + fieldIdStr.length() + " exceeds base profile limit " + Table7.NameLimit );
				    }
				}

				// initial value is required if we are creating an exposedField or field
				// unless this has been disabled for this factory (EXTERNPROTORule disables)
				if ( valueCreationEnabled ) {
    				if (( vrmlFieldType == VRML97.exposedField ) || ( vrmlFieldType == VRML97.field )) {
    				    boolean restoreState = false;
    				    int state = v.getState();
           				tokenOffset = v.getNextToken();
    				    if ( v.isRightBracketOrBrace( tokenOffset )) {
    				        restoreState = true;
    				    }
			            FieldValue fv = CreateFieldValue( result, fieldType, tokenOffset, v, scene, parent );
//        				FieldValue fv = CreateFieldValue( f, fieldType, tokenOffset, v, scene, parent );
        				f.setFieldValue( fv );
        				if ( restoreState ) {
        				    v.setState( state );
        				}
        			}
        		}

				// set the last token for the field
				f.setLastTokenOffset( v.getCurrentTokenOffset() );
				
    		    result.setLastTokenOffset( v.getCurrentTokenOffset() );
    		    result.addChild( f );
    		    result.setFieldId( f.getFieldId() );
    		    result.setFieldType( fieldType );

				return( result );
	        }

        } catch ( Exception e ) {
            System.out.println("Exception in Field.Factory for " + vrmlFieldType + ": " + e);
			e.printStackTrace();
        }
		System.out.println( "Couldn't find field class for " + vrmlFieldType );
        return null;
	}
	
	
	/** create an instance of a field
	 *
	 *  @param tokenOffset  the first token of the field, the field id
	 *  @param v   the TokenEnumerator containing the file text
	 *  @param s   the Scene containing the field
	 *  @param parent  the Node containing the field
	 *
	 *  @return  a Field corresponding to the field instance, or null if
	 *           the Node has no field with a spelling same as or close to
	 *           the field id.
	 */
	Field CreateInstance( int tokenOffset, TokenEnumerator v, Scene s, Node parent ) {
	    String fid = FieldId.getVRML2name( tokenOffset, v );
	    int instanceStart = tokenOffset;
	    v.breakLineAt( instanceStart );
	    
	    // if the field isn't found in the parent, mark the error,
	    // look for typo substitution
	    boolean implicitField = parent.isImplicitFieldId( fid );
	    
	    // if the parent node has the field, it is a duplicate
	    boolean duplicateField = false;
	    if ( parent.getField( fid ) != null ) {
	        duplicateField = true;
	    }
	    if ( !implicitField && !parent.isValidFieldId( fid )) {
	        String originalFid = fid;
	        fid = parent.getClosestFieldId( fid );
            if ( v.isSpecialCharacter( tokenOffset )) {
                BadFieldId bfi = new BadFieldId( tokenOffset );
                parent.addChild( bfi );
                bfi.setError( "expected field id here" );
                return( null );
            }
            BadFieldId bfi = new BadFieldId( tokenOffset );
            parent.addChild( bfi );
	        if ( fid == null ) {
	            if ( parent instanceof PROTOInstance ) {
	                PROTOInstance pi = (PROTOInstance)parent;
    	            bfi.setError( "not a valid field for " + pi.getPROTOname() );
    	        } else {
    	            bfi.setError( "not a valid field for " + parent.getBaseName() );
    	        }
    	        // if someone was putting a node or PROTO where a field should be,
    	        // build that, but add it as an error
    	        if ( VRML97.isBuiltInNode( originalFid )) {
    	            VrmlElement lastChild = parent.getLastChild();
    	            nodeRule.Build( tokenOffset, v, s, parent );
    	            VrmlElement lastChild2 = parent.getLastChild();
    	            if ( lastChild2 != lastChild ) {
    	                lastChild2.setError( "misplaced node" );
    	            }
    	        }
                return( null );
	        } else {
	            bfi.setError( "not a valid field, possibly '" + fid + "'" );
	        }
	    }
	    if ( implicitField ) {
	        fid = unImplicit( fid );
	    }
		tokenOffset = v.getNextToken();
		if ( tokenOffset == -1 ) {
			parent.setError( "unexpected end of file" );
		} else {
            Field result = null;
            FieldDescriptor fieldDescriptor = null;
            if ( parent instanceof PROTOInstance ) {
                PROTOInstance protoInstance = (PROTOInstance)parent;
                result = CreatePROTOFieldInstance( protoInstance, fid, instanceStart );
                fieldDescriptor = protoInstance.getFieldDescriptor( fid );
            } else {
                fieldDescriptor = VRML97.getFieldDescriptor( parent.getNodeName(), fid );
                result = CreateFieldInstance( fieldDescriptor, fid, instanceStart );
            }
			FieldValue f = CreateFieldValue( result, result.getFieldType(), tokenOffset, v, s, parent );
			if ( VRML97.fieldIsDefault( parent.getNodeName(), fid, f.getFirstTokenOffset(), f.getLastTokenOffset(), v )) {
				result.setError( "Warning, field value is default" );
			}
			if (( fieldDescriptor instanceof MFFieldDescriptor ) && ( f instanceof MFFieldValue )) {
			    MFFieldDescriptor mfFieldDescriptor = (MFFieldDescriptor)fieldDescriptor;
			    MFFieldValue mf = (MFFieldValue)f;
			    if ( mfFieldDescriptor.getLimit() > 0 ) {
			        if ( mf.getChildAt( 0 ) instanceof ISField ) {
			            ISField isf = (ISField)mf.getChildAt( 0 );
			            Field protoField = isf.getPROTOfield();
			            if ( protoField != null ) {
			                VrmlElement pf = protoField.getChildAt( 0 );
			                if ( pf instanceof MFField ) {
			                    FieldValue mff = ((MFField)pf).getFieldValue();
			                    if ( mff instanceof MFFieldValue ) {
			                        mf = (MFFieldValue)mff;
			                    }
			                }
			            }
			        }

			        int vcount = mf.getRawValueCount()/mfFieldDescriptor.getFactor();
			        if ( vcount == 0 ) {
			            vcount = mf.numberValues();
			        }
			        if ( vcount > mfFieldDescriptor.getLimit() ) {
    			        result.setError( "Nonconformance, value count " + vcount + " exceeds base profile limit " + mfFieldDescriptor.getLimit() );
   			        }
			    }
			}
			result.setLastTokenOffset( v.getCurrentTokenOffset() );
			result.setFieldValue( f );
			if ( duplicateField ) {
			    result.setError( "Warning, duplicate field" );
			}
//			result.addChild( f );
			
			if ( fieldDescriptor != null ) {
			    if ( fieldDescriptor.usesDEF() ) {
                    if ( f instanceof MFFieldValue ) {
                        MFFieldValue mfv = (MFFieldValue)f;
                        int numberValues = mfv.numberValues();
                        for ( int i = 0; i < numberValues; i++ ) {
                            FieldValue strValue = mfv.getFieldValueAt( i );
                            if ( strValue instanceof SFStringValue ) {
                                String quotedUrlString = v.toString( strValue.getFirstTokenOffset() );
                                if ( quotedUrlString.length() > 3 ) {
                                    String urlString = StringUtil.stripQuotes( quotedUrlString );
                                    String viewpointDEF = null;
                                    if ( urlString.length() > 1 ) {
                                        if ( urlString.charAt( 0 ) == '#' ) {
                                            viewpointDEF = urlString.substring( 1 );
                                        } else if ( urlString.indexOf( '#' ) > 0 ) {
                                            String sceneUrl = s.getUrl();
                                            if ( sceneUrl.lastIndexOf( '/' ) > 0 ) {
                                                sceneUrl = sceneUrl.substring( sceneUrl.lastIndexOf( '/' ));
                                                if ( sceneUrl.length() > 0 ) {
                                                    sceneUrl = sceneUrl.substring( 1 );
                                                }
                                            }
                                            String url = StringUtil.stripPound( urlString );
                                            if (( sceneUrl != null ) && ( url != null )) {
                                                if ( sceneUrl.compareTo( url ) == 0 ) {
                                                    viewpointDEF = urlString.substring( urlString.indexOf( '#' ) + 1 );
                                                }
                                            }
                                        }
                                       
                                        if ( viewpointDEF != null ) {
                                            DEFUSENode dun = s.getDEF( viewpointDEF );
                                            if ( dun == null ) {
                                                strValue.setError( "No DEF for '" + viewpointDEF + "'" );
                                            } else {
                                                Node n = dun.getNode();
                                                if ( n != null ) {
                                                    if ( n.getNodeName().compareTo( "Viewpoint" ) == 0 ) {
                                                        dun.markUsed();
                                                    } else {
                                                        strValue.setError( "not a Viewpoint" );
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
			
			// validate node type
			if ( f instanceof SFNodeValue ) {
				SFNodeValue sfn = (SFNodeValue)f;
				ReturnInteger fieldType = new ReturnInteger();
				if ( VRML97.fieldHasType( parent.getNodeName(), fid, fieldType )) {
				    Node n = sfn.getNode();
				    boolean setError = false;
				    boolean externPROTO = false;
				    if ( n instanceof PROTOInstance ) {
				        PROTOInstance pi = (PROTOInstance)n;
				        if ( pi.getPROTObase() instanceof EXTERNPROTO ) {
				            externPROTO = true;
				        }
				        n = pi.getPROTONodeType();
				    }
				    if ( n == null ) {
				        setError = true;
				    } else {
				        String nodeName = n.getNodeName();
				        if ( nodeName != null ) {
        				    if ( !NodeType.isCompatible( nodeName, fieldType.getValue() )) {
        				        setError = true;
        				    }
    				    }
				    }
				    // Have to load extern proto before checking
				    if ( setError && !externPROTO ) {
				        VrmlElement c0 = f.getChildAt( 0 );
				        if ( !( c0 instanceof ISField )) {
            		        f.setError( "node type must be " + NodeType.getFieldTypeList( fieldType.getValue() ));
            		    }
        		    }
				}
			} else if ( f instanceof MFNodeValue ) {  // MFNodeValue related blaxxun extensions
				ReturnInteger fieldType = new ReturnInteger();
				if ( VRML97.fieldHasType( parent.getNodeName(), fid, fieldType )) {
				    MFNodeValue mfnv = (MFNodeValue)f;
				    for ( int i = 0; i < mfnv.numberChildren(); i++ ) {
				        Object x = mfnv.getChildNode( i );
				        if ( x instanceof Node ) {
				            Node nnn = (Node)x;
				            if ( !NodeType.isCompatible( nnn.getNodeName(), fieldType.getValue() )) {
				                nnn.setError( "node type must be " + NodeType.getFieldTypeList( fieldType.getValue() ));
				            }
				        }
				    }
				}
			}
			return( result );
		}
		return( null );
	}

    String unImplicit( String fid ) {
        if ( fid.indexOf( "set_" ) == 0 ) {
            return( fid.substring( 4 ));
        } else if ( fid.indexOf( "_changed" ) > 0 ) {
            int cidx = fid.indexOf( "_changed" );
            return( fid.substring( 0, cidx ));
        } else {
            return( null );
        }
    }
    
    /** 
     *  create the field value of the node.
     */
    public FieldValue CreateFieldValue( Field f, int fieldType, int tokenOffset, TokenEnumerator v, Scene scene, Node parentNode ) {
        return( CreateFieldValue( f, VRML97.getFieldTypeString( fieldType ), tokenOffset, v, scene, parentNode ));
    }

    /** Create a FieldValue for a field.
     *
     *  @param f the Field to contain the FieldValue
     *  @param fieldType one of the built in VRML97 field types, e.g. SFBool, etc.
     *  @param tokenOffset token offset of the first token in the field value
     *  @param scene the scene containing the field, could be a scene contained in a PROTO
     *  @param parentNode the Node containing the field, used for IS field checks
     */
    public FieldValue CreateFieldValue( Field f, String fieldType, int tokenOffset, TokenEnumerator v, Scene scene, Node parentNode ) {
        try {
			// all primitive field types are in the "fields" package
            Class fieldValueClass = Class.forName("com.trapezium.vrml.fields." + fieldType + "Value" );
            if (fieldValueClass != null) {
				// create an instance
                FieldValue fv =  (FieldValue) (fieldValueClass.newInstance());
                fv.setParent( parentNode );
				fv.setFirstTokenOffset( tokenOffset );

				// check for IS here?
				if ( !v.isNumber( tokenOffset ) && v.nearlySameAs( tokenOffset, "IS" )) {
					ISField isField = new ISField( f, tokenOffset, v, scene.getPROTOparent() );
					if ( !v.sameAs( tokenOffset, "IS" )) {
					    isField.setError( "bad case, possibly 'IS'" );
					}
					fv.addChild( isField );
					VRML97.checkISFieldTypes( isField, parentNode );
				} else {					
					fv.init( nodeStatementRule, tokenOffset, v, scene );
				}
				fv.setLastTokenOffset( v.getCurrentTokenOffset() );
				return( fv );
			}

        } catch ( Exception e ) {
            System.out.println("Exception in FieldValue.Factory for " + fieldType + ": " + e);
			e.printStackTrace();
        }
        return null;
    }


    Field CreateFieldInstance( FieldDescriptor fieldDescriptor, String fieldId, int instanceStart ) {
        Field f = Create( fieldDescriptor.getDeclarationType(), instanceStart );
        f.setFieldId( fieldId );
        f.setFieldType( fieldDescriptor.getFieldType() );
        return( f );
    }
    
    Field CreatePROTOFieldInstance( PROTOInstance protoInstance, String fieldId, int instanceStart ) {
        Field protoDecl = protoInstance.getInterfaceDeclaration( fieldId );
        Field f = Create( protoDecl.getInterfaceType(), instanceStart );
        f.setFieldId( fieldId );
        f.setFieldType( protoDecl.getFieldType() );
        return( f );
    }
    
	Field Create( int type, int startOffset ) {
	    if ( type == VRML97.field ) {
	        return( new NotExposedField( startOffset ));
	    } else if ( type == VRML97.exposedField ) {
	        return( new ExposedField( startOffset ));
	    } else if ( type == VRML97.eventIn ) {
	        return( new EventIn( startOffset ));
	    } else if ( type == VRML97.eventOut ) {
	        return( new EventOut( startOffset ));
	    } else {
	        return( null );
	    }
	}
}
