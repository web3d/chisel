/*
 * @(#)InlineCreator.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.reorganizers;

import com.trapezium.chisel.*;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFVec3fValue;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.util.Vector;

/**  The InlineCreator chisel either converts Transform nodes into Inline nodes */
public class InlineCreator extends Optimizer {
    int inlineNo;

    /** Internal class for handline Inline info */
    class InlineInfo {
        String fileName;
        Node node;
        int minToken;
        int maxToken;
        String bboxSize;
        String bboxCenter;
        boolean generatedBboxInfo;
        TokenEnumerator nodeDataSource;

        /** Class constructor for internal class */
        public InlineInfo( String fileName, Node node, TokenEnumerator nodeDataSource ) {
            this.fileName = fileName;
            this.nodeDataSource = nodeDataSource;
            this.node = node;
            generatedBboxInfo = false;
            bboxSize = null;
            bboxCenter = null;
        }

        /** Generate an Inline file for the node contained in this object.
         *
         *  @param dataSource the source of the text for the generated file
         */
	    public void genFile( TokenEnumerator dataSource ) {
	        try {
    	        FileOutputStream fos = new FileOutputStream( fileName );
    	        PrintStream ps = new PrintStream( fos );
    	        TokenPrinter tpOut = new TokenPrinter( ps, dataSource );
    	        tpOut.print( "#VRML V2.0 utf8" );
    	        tpOut.flush();
    	        minToken = node.getFirstTokenOffset();
    	        maxToken = node.getLastTokenOffset();
    	        genNode( node, tpOut, dataSource );
    	        tpOut.flush();
    	        ps.flush();
    	        ps.close();
    	        fos.close();
    	    } catch ( Exception e ) {
    	        e.printStackTrace();
    	    }
	    }

        /** Get a bboxSize String if the node being Inlined is a Shape node
         *  with an IndexedFaceSet, IndexedLineSet, or PointSet.  Need to
         *  include this also for other geometry nodes (bug 171)
         */
        String getBboxSize() {
            genBboxInfo();
            return( bboxSize );
        }
        
        String getBboxCenter() {
            genBboxInfo();
            return( bboxCenter );
        }
        
        void genBboxInfo() {
            if ( !generatedBboxInfo ) {
                generatedBboxInfo = true;
                if ( node.getBaseName().compareTo( "Shape" ) == 0 ) {
                    Field geometry = node.getField( "geometry" );
                    if ( geometry != null ) {
                        Node geometryNode = geometry.getNodeValue();
                        if ( geometryNode != null ) {
                            Field coord = geometryNode.getField( "coord" );
                            if ( coord != null ) {
                                Node coordNode = coord.getNodeValue();
                                if ( coordNode != null ) {
                                    Field point = coordNode.getField( "point" );
                                    if ( point != null ) {
                                        FieldValue pointList = point.getFieldValue();
                                        if ( pointList instanceof MFVec3fValue ) {
                                            MFVec3fValue values = (MFVec3fValue)pointList;
                                            values.genExtremes( nodeDataSource );
                                            if ( values.hasExtremes() ) {
                                                bboxSize = new String( "bboxSize " + values.getXsize() + " " + values.getYsize() + " " + values.getZsize() );
                                                bboxCenter = new String( "bboxCenter " + values.getXcenter() + " " + values.getYcenter() + " " + values.getZcenter() );
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
        
	    /** Generate text for a node that is being Inlined, the generated
	     *  text is placed in the inline file.
	     *
	     *  @param nn the node being converted into an inline
	     *  @param tpOut the print destination for the text of the node
	     *     being converted to an inline (i.e. the inline file being
	     *     created)
	     *  @param dataSource the source of the text to place in the new file
	     */
	    void genNode( Node nn, TokenPrinter tpOut, TokenEnumerator dataSource ) {
	        int scanner = nn.getFirstTokenOffset();
	        int end = nn.getLastTokenOffset();
	        dataSource.setState( scanner );
	        while ( scanner <= end ) {
	            int type = dataSource.getType( scanner );
	            boolean handledToken = false;
	            
	            // USE nodes require special handling.  If the DEF for the 
	            // USE is within the node being inlined, then the USE can
	            // remain unchanged.  Otherwise, the USE gets completely
	            // replaced with the text of the original DEF.
	            //
	            // An even better solution would be to DEF the first instance
	            // of the USE.
	            if ( dataSource.sameAs( scanner, "USE" )) {
	                int nscanner =  dataSource.getNextToken();
	                Scene s = (Scene)node.getScene();
	                if ( s != null ) {
	                    DEFUSENode dun = s.getDEF( dataSource.toString( nscanner ));
	                    if ( dun != null ) {
	                        Node referencedNode = dun.getNode();
	                        int rnfirst = referencedNode.getFirstTokenOffset();
	                        int rnlast = referencedNode.getLastTokenOffset();
	                        if (( rnfirst < minToken ) || ( rnlast > maxToken )) {
    	                        genNode( dun.getNode(), tpOut, dataSource );
    	                        dataSource.setState( nscanner );
    	                        handledToken = true;
    	                        scanner = nscanner;
    	                    }
	                    }
	                }
	            }
	            if ( !handledToken ) {
	                tpOut.print( dataSource, scanner, type );
	            }
	            scanner = dataSource.getNextToken();
	            if ( scanner == -1 ) {
	                break;
	            }
	        }
	    }

	    public String getFileName() {
	        return( fileName );
	    }
	    
	    public String getRelativeFileName() {
	        if ( fileName.indexOf( "/" ) > 0 ) {
	            return( fileName.substring( fileName.lastIndexOf( "/" ) + 1, fileName.length() ));
	        } else {
	            return( fileName );
	        }
	    }
	}

    /** Class constructor, convert a specific node type into a Inlines */
	public InlineCreator( String whichNode ) {
		super( whichNode, "Converting " + whichNode + " to Inline..." );
		inlineNo = 0;
	}

	public void attemptOptimization( Node n ) {
	    inlineNo++;
	    String fileName = null;
	    if ( baseFileName != null ) {
	        if ( baseFileName.indexOf( "." ) > 0 ) {
    	        String nameWithoutExtension = baseFileName.substring( 0, baseFileName.indexOf( "." ));
    	        fileName = nameWithoutExtension + "_" + inlineNo + ".wrl";
    	    } else {
    	        fileName = baseFileName + "_" + inlineNo + ".wrl";
    	    }
    	} else {
    	    fileName = "Inline_" + inlineNo + ".wrl";
    	}
    	if ( baseFilePath != null ) {
    	    fileName = baseFilePath + "/" + fileName;
    	}
	    replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), new InlineInfo( fileName, n, dataSource ));
	}

	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    if ( param instanceof InlineInfo ) {
	        InlineInfo ii = (InlineInfo)param;
	        ii.genFile( tp.getDataSource() );
	        tp.print( "Inline { url \"" + ((InlineInfo)param).getRelativeFileName() + "\"" );
	        String bboxSize = ii.getBboxSize();
	        String bboxCenter = ii.getBboxCenter();
	        if ( bboxSize != null ) {
	            tp.print( bboxSize );
	        }
	        if ( bboxCenter != null ) {
	            tp.print( bboxCenter );
	        }
	        tp.print( "}" );
	    }
	}
}


