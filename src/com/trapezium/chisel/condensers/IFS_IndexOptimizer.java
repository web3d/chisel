/*
 * @(#)IFS_IndexOptimizer.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.condensers;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.SFBoolValue;
import com.trapezium.chisel.*;

import java.util.Vector;
import java.util.Hashtable;
import java.io.PrintStream;

//
//  The IFS_IndexOptimizer creates texCoordIndex/colorIndex/normalIndex fields if those
//  fields do not exist, and the one-for-one mapping in the texCoord/color/normal nodes
//  contain alot of repeated values.  In this creation, there are two possibilities.  If
//  these are perVertex, which is the only possibility for texCoord, or dependent on
//  colorPerVertex/normalPerVertex, then each index value has to correspond to an index
//  value in the coordIndex.  Otherwise if colorPerVertex/normalPerVertex is false, each
//  index value in colorIndex/normalIndex is associated with a face.
//
public class IFS_IndexOptimizer extends Optimizer {
	int valueRemovedCount = 0;

	//
	//  embedded class, contains object parameter for "optimize" method
	//
	class IFS_IndexInfo {
		Node node;
		Vector nonRepeatingValues;
		int[] indexArray;
		String indexFieldName;

		public IFS_IndexInfo( Node node, Vector nonRepeatingValues, int[] indexArray, String indexFieldName ) {
			this.node = node;
			this.nonRepeatingValues = nonRepeatingValues;
			this.indexArray = indexArray;
			this.indexFieldName = indexFieldName;
		}

		public Vector getNonRepeatingValues() {
			return( nonRepeatingValues );
		}

		public int[] getIndexArray() {
			return( indexArray );
		}

		public Node getNode() {
			return( node );
		}

		public String getIndexFieldName() {
			return( indexFieldName );
		}
	}

/*
	public IFS_IndexOptimizer( RangeReplacer rangeReplacer ) {
		super( rangeReplacer, "IndexedFaceSet" );
	}
*/
	public IFS_IndexOptimizer() {
		super( "IndexedFaceSet", "Creating index fields..." );
	}


	//
	//  Check the texCoordIndex/colorIndex/normalIndex.  If these do not exist, and the
	//  corresponding texCoord/color/normal nodes exist and have alot of repeated values,
	//  then we choose to do the optimization.  The savings depend on the size of the
	//  removed repeated values compared to the increased size due to the index field.
	//  In the case where colorPerVertex/normalPerVertex is false, this number is:
	//
	//    indexSize( number faces ) - average size color * number repeated colors
	//
	//  In the case where we deal with texCoord/texCoordIndex, or where colorPerVertex/
	//  normalPerVertex are true, the size is approximately
	//
	//    coordIndexSize - average size color/normal/texCoord * number repeated
	//
	//  If the above give a negative result, the optimization is made.  The calculation
	//  is not precise, but is approximate in a way that if this chisel is run, the resulting
	//  file will be smaller.
	//

	public void attemptOptimization( Node n ) {
		Field nodeField = n.getField( "coord" );
		Field indexField = n.getField( "coordIndex" );
		int numberNodeFieldValues = getNumberNodeFieldValues( nodeField, "point", 3 );
		int numberIndexFieldValues = getNumberIndexFieldValues( indexField );
		int numberFaces = getNumberFaces( indexField );
		if (( numberNodeFieldValues > 0 ) && ( numberIndexFieldValues > 0 )) {
			attemptOptimization( n, "color", "color", "colorIndex", "colorPerVertex", 3, numberFaces );
			attemptOptimization( n, "normal", "vector", "normalIndex", "normalPerVertex", 3, numberFaces );
			attemptOptimization( n, "texCoord", "point", "texCoordIndex", null, 2, numberFaces );
		}
	}

	//
	//  get the number of value units, which is dependent on the factor.  For color
	//  gets the number of RGB triplets, for normal gets the xyz triplet, for texture,
	//  gets the number of xy pairs.
	//
	int getNumberNodeFieldValues( Field nodeField, String nodeValueName, int factor ) {
		if ( nodeField != null ) {
			FieldValue fv = nodeField.getFieldValue();
			Node valueNode = (Node)fv.getChildAt( 0 );
			if ( valueNode != null ) {
				Field values = valueNode.getField( nodeValueName );
				if ( values != null ) {
    				MFFieldValue nodeValues = (MFFieldValue)values.getFieldValue();
    				return( nodeValues.getRawValueCount()/factor );
    			}
			}
		}
		return( 0 );
	}

    /** Get the number of values in an index field */
	int getNumberIndexFieldValues( Field indexField ) {
		if ( indexField != null ) {
			MFFieldValue fv = (MFFieldValue)indexField.getFieldValue();
			return( fv.getRawValueCount() );
		}
		return( 0 );
	}

    /** Get the number of faces in an index field */
	int getNumberFaces( Field indexField ) {
	    int numberFaces = 0;
	    if ( indexField != null ) {
    		int scannerOffset = indexField.getFirstTokenOffset();
    		int lastTokenOffset = indexField.getLastTokenOffset();
    		while ( scannerOffset != -1 ) {
    			if ( dataSource.isNumber( scannerOffset )) {
    				if ( dataSource.getIntValue( scannerOffset ) == -1 ) {
    					numberFaces++;
    				}
    			}
    			scannerOffset = dataSource.getNextToken( scannerOffset );
    			if ( scannerOffset == lastTokenOffset ) {
    				break;
    			}
    		}
    	}
   		return( numberFaces );
	}


	//
	//  estimate the size of an index field based on the "perVertex" flag.
	//  If this flag is true, the estimated size is about the size of the coordIndex.
	//  If this flag is false, the estimated size is a function of the number of faces.
	//	
	int getIndexSize( boolean perVertex, Node n ) {
		int size = 0;
		Field coordIndex = n.getField( "coordIndex" );
		int firstTokenOffset = coordIndex.getFirstTokenOffset();
		int lastTokenOffset = coordIndex.getLastTokenOffset();
		int scannerOffset = firstTokenOffset;
		if ( perVertex ) {
			while ( scannerOffset != -1 ) {
				size += dataSource.getSize( scannerOffset);
				size++;
				scannerOffset = dataSource.getNextToken( scannerOffset );
				if ( scannerOffset == lastTokenOffset ) {
					break;
				}
			}
			return( size );
		} else {
			int faceNumber = 0;
			while ( scannerOffset != -1	) {
				if ( dataSource.isNumber( scannerOffset )) {
					if ( dataSource.getIntValue( scannerOffset ) == -1 ) {
						if ( faceNumber < 10 ) {
							size += 2;
						} else if ( faceNumber < 100 ) {
							size += 3;
						} else if ( faceNumber < 1000 ) {
							size += 4;
						} else if ( faceNumber < 10000 ) {
							size += 5;
						} else {
							size += 6;
						}
						faceNumber++;
					}
				}
				scannerOffset = dataSource.getNextToken( scannerOffset );
				if ( scannerOffset == lastTokenOffset ) {
					break;
				}
			}
			return( size );
		}
	}
	
	void attemptOptimization( Node n, String nodeFieldName, String nodeValueName, 
			String indexFieldName, String perVertexFieldName, int factor, int numberFaces ) {
		Field nodeField = n.getField( nodeFieldName );
		Field indexField = n.getField( indexFieldName );
		Field perVertexField = null;
		boolean perVertex = true;
		if ( perVertexFieldName != null ) {
			perVertexField = n.getField( perVertexFieldName );
			if ( perVertexField != null ) {
				SFBoolValue bv = (SFBoolValue)perVertexField.getFieldValue();
				perVertex = bv.getValue();
			}
		}
		int numberNodeFieldValues = getNumberNodeFieldValues( nodeField, nodeValueName, factor );
		int numberIndexFieldValues = getNumberIndexFieldValues( indexField );

		//  If there are already index field values, this optimizer does nothing
		if ( numberIndexFieldValues > 0 ) {
			return;
		}

		// If there are values, but no indexes, this optimizer has the option of
		// introducing the "index" field.  To see if this really makes sense, first
		// estimate the size of the "index" field to be introduced.
		//
		int estimatedIndexFieldSize = getIndexSize( perVertex, n );
		int savings = -1 * estimatedIndexFieldSize;

		//  At this point, we need to adjust the savings based on the potential optimization.
		//  There are two possibilities.  One is that perVertex is TRUE, and we do not have
		//  an index field.  This means we are just using the coordIndex field.  In this
		//  case, there is one value per coord, and we are using the coordIndexes.  The
		//  possible optimization is related to the case where many of those node values
		//  are repeated, we can make them non-repeating, and introduce out own index field
		//  to access the non-repeating values.
		//
		//  The second possibility is when perVertex is FALSE, in which case, there is one
		//  node value per face.  If many of these values are repeats, then the possible
		//  optimization is to eliminate the repeats, and introduce an index field, with
		//  one index entry per face.
		//
		//  At the moment, this optimizer only handles this second possibility, since this
		//  one has the potential for the greatest savings.
		//
		if (( numberNodeFieldValues > 10 ) && !perVertex ) {
			// create a list of non-repeating values
			Vector nonRepeatingValues = new Vector();
			Hashtable valueTable = new Hashtable();

			// create an in memory index based on the number of faces
			int[] inMemoryIndex = new int[ numberFaces ];

			// create a single string out of each set of number tokens
			int scannerOffset = nodeField.getFirstTokenOffset();
			dataSource.setState( scannerOffset );
			int[] list = new int[ factor ];
			
			// handle error case, otherwise get Array out of bounds exception
			if ( numberNodeFieldValues > numberFaces ) {
			    numberNodeFieldValues = numberFaces;
			}
			for ( int i = 0; i < numberNodeFieldValues; i++ ) {
				for ( int j = 0; j < factor; j++ ) {
					scannerOffset = dataSource.skipNonNumbers();
					list[j] = scannerOffset;
					scannerOffset = dataSource.getNextToken();
					if ( scannerOffset == -1 ) {
					    break;
					}
				}
				if ( scannerOffset == -1 ) {
				    break;
				}
				String hashval = null;
				if ( factor == 2 ) {
					hashval = dataSource.getFloat( list[0] ) + " " + dataSource.getFloat( list[1] );
				} else { // factor is 3
					hashval = dataSource.getFloat( list[0] ) + " " + dataSource.getFloat( list[1] ) + " " + dataSource.getFloat( list[2] );
				}

				// look up the hashval in the hashtable, if it is there, this is a repeated
				// value
				Integer index = (Integer)valueTable.get( hashval );
				if ( index == null ) {
					valueTable.put( hashval, new Integer( nonRepeatingValues.size() ));
					String tokval = null;
					if ( factor == 2 ) {
						tokval = dataSource.toString( list[0] ) + " " + dataSource.toString( list[1] );
					} else {
						tokval = dataSource.toString( list[0] ) + " " + dataSource.toString( list[1] ) + " " + dataSource.toString( list[2] );
					}
					inMemoryIndex[ i ] = nonRepeatingValues.size();
					nonRepeatingValues.addElement( tokval );
				} else {
					// Its a repeat, its offset is indicated by "index"
					inMemoryIndex[i] = index.intValue();
					String s = (String)nonRepeatingValues.elementAt( index.intValue() );
					savings += s.length() - 4;  // roughly savings - index size (3 dig + space)
				}
			}

			//
			// Rough calculation, but savings > 100 should do it for now
			//
			if ( savings > 100 ) {
				replaceRange( nodeField.getFirstTokenOffset(), nodeField.getLastTokenOffset(), 
					new IFS_IndexInfo( n, nonRepeatingValues, inMemoryIndex, indexFieldName ));
			}
		}
	}

	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
		if ( param instanceof IFS_IndexInfo ) {
			IFS_IndexInfo indexInfo = (IFS_IndexInfo)param;
			Node node = indexInfo.getNode();
			int[] indexArray = indexInfo.getIndexArray();
			String indexFieldName = indexInfo.getIndexFieldName();
			Vector nonRepeatingValues = indexInfo.getNonRepeatingValues();

			// write out all tokens until we get to a number
			int scannerOffset = startTokenOffset;
			while ( true ) {
				if ( dataSource.isNumber( scannerOffset )) {
					break;
				}
				tp.print( dataSource, scannerOffset );
				if ( scannerOffset == endTokenOffset ) {
					break;
				}
				scannerOffset = dataSource.getNextToken( scannerOffset );
			}

			// now write out all the numbers
			for ( int i = 0; i < nonRepeatingValues.size(); i++ ) {
				String s = (String)nonRepeatingValues.elementAt( i );
				tp.print( s );
			}

			// write out end
			tp.print( "] }" );

			// Now we write out newly created index field
			tp.print( indexFieldName );
			tp.print( "[" );
			for ( int i = 0; i < indexArray.length; i++ ) {
				tp.print( indexArray[i] );
			}
			tp.print( "]" );
			valueRemovedCount += ( indexArray.length - nonRepeatingValues.size() );
		}
	}

	public void summarize( PrintStream ps ) {
		if ( valueRemovedCount == 0 ) {
			System.out.println( "IFS_IndexOptimizer removed no values" );
		} else if ( valueRemovedCount == 1 ) {
			System.out.println( "IFS_IndexOptimizer removed 1 value" );
		} else {
			System.out.println( "IFS_IndexOptimizer removed " + valueRemovedCount + " values" );
		}
	}
}

