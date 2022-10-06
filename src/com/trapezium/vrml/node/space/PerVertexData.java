package com.trapezium.vrml.node.space;

import com.trapezium.util.TypedObject;

public class PerVertexData implements TypedObject {
	int[] vValues;
	int[] tValues;
	int type;
	
	// the three types of per vertex data
	static public final int Texture = 1;
	static public final int Normal = 2;
	static public final int Color = 3;

	public PerVertexData( int[] vertices, int[] textures, int number, int type ) {
	    this.type = type;
		vValues = new int[ number ];
		tValues = new int[ number ];
		for ( int i = 0; i < number; i++ ) {
			vValues[i] = vertices[i];
			tValues[i] = textures[i];
		}
	}

	public int getSize() {
		return( vValues.length );
	}

	public int getVValue( int offset ) {
		return( vValues[ offset ] );
	}

	public int getTValue( int offset ) {
		return( tValues[ offset ] );
	}
	
	/** TypedObject interface */
	public int getType() {
	    return( type );
	}
	
	/** TypedObject interface */
	public Object getObject( int otype ) {
	    if ( otype == type ) {
	        return( this );
	    } else {
	        return( null );
	    }
	}

	/** Get the value corresponding to a particular vertex.
	 *
	 *  @param vertex the vertex value (index into vertex list)
	 *  @return the corresponding value, or -1 if not found
	 */
	public int getValue( int vertex ) {
		for ( int i = 0; i < vValues.length; i++ ) {
			if ( vValues[i] == vertex ) {
				return( tValues[i] );
			}
		}
		return( -1 );
	}

	public PerVertexData( Object o1, Object o2 ) {
		if (( o1 instanceof PerVertexData ) && ( o2 instanceof PerVertexData )) {
		    type = ((PerVertexData)o1).getType();
			PerVertexData t1 = (PerVertexData)o1;
			PerVertexData t2 = (PerVertexData)o2;
			int t1size = t1.getSize();
			int t2size = t2.getSize();
			vValues = new int[ t1size + t2size ];
			tValues = new int[ t1size + t2size ];
			for ( int i = 0; i < t1size; i++ ) {
				vValues[i] = t1.getVValue( i );
				tValues[i] = t1.getTValue( i );
			}
			for ( int i = 0; i < t2size; i++ ) {
				vValues[ i + t1size ] = t2.getVValue( i );
				tValues[ i + t1size ] = t2.getTValue( i );
			}
/*			for ( int i = 0; i < ( t1size + t2size - 1 ); i++ ) {
			    for ( int j = i + 1; j < ( t1size + t2size ); j++ ) {
			        if ( vValues[i] == vValues[j] ) {
			            tValues[j] = tValues[i];
			        }
			    }
			}*/
		}
	}
}
