package com.trapezium.chisel.mutators;

import com.trapezium.chisel.*;

import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.node.Node;
import java.io.PrintStream;

public class IFS_Triangulator extends Optimizer {
    class TriParam {
        Field color;
        Field colorIndex;
        Field normal;
        Field normalIndex;
        int rawValueCount;
        
        TriParam( Field color, Field colorIndex, Field normal, Field normalIndex, int rawValueCount ) {
            this.color = color;
            this.colorIndex = colorIndex;
            this.normal = normal;
            this.normalIndex = normalIndex;
            this.rawValueCount = rawValueCount;
        }

        Field getNormal() {
            return( normal );
        }
        
        Field getNormalIndex() {
            return( normalIndex );
        }
        
        Field getColor() {
            return( color );
        }
        
        Field getColorIndex() {
            return( colorIndex );
        }
        
        int getApproxFaceCount() {
            return( rawValueCount/4 );
        }
    }
    
    boolean triangulateQuads = true;

	public IFS_Triangulator() {
		super( "IndexedFaceSet", "Triangulating all faces..." );
	}

	public void attemptOptimization( Node n ) {
		Field coordIndex = n.getField( "coordIndex" );
		if ( coordIndex != null ) {
    		Field colorIndex = n.getField( "colorIndex" );
    		Field color = n.getField( "color" );
    		if ( n.getBoolValue( "colorPerVertex" )) {
    		    colorIndex = null;
    		    color = null;
    		} else if ( colorIndex != null ) {
    		    color = null;
    		}
    		Field normalIndex = n.getField( "normalIndex" );
    		Field normal = n.getField( "normal" );
    		if ( n.getBoolValue( "normalPerVertex" )) {
    		    normalIndex = null;
    		    normal = null;
    		} else if ( normalIndex != null ) {
    		    normal = null;
    		}
		    FieldValue fv = coordIndex.getFieldValue();
		    if (( fv != null ) && ( fv instanceof MFFieldValue )) {
		        MFFieldValue mfv = (MFFieldValue)fv;
    		    if ( mfv.getRawValueCount() > 0 ) {
        			replaceRange( coordIndex.getFirstTokenOffset(), coordIndex.getLastTokenOffset(), new TriParam( color, colorIndex, normal, normalIndex, mfv.getRawValueCount() ));
        		}
        		if ( colorIndex != null ) {
        		    replaceRange( colorIndex.getFirstTokenOffset(), colorIndex.getLastTokenOffset(), null );
        		}
        		if ( color != null ) {
        		    replaceRange( color.getFirstTokenOffset(), color.getLastTokenOffset(), null );
        		}
        		if ( normalIndex != null ) {
        		    replaceRange( normalIndex.getFirstTokenOffset(), normalIndex.getLastTokenOffset(), null );
        		}
        		if ( normal != null ) {
        		    replaceRange( normal.getFirstTokenOffset(), normal.getLastTokenOffset(), null );
        		}
    		}
		}
	}

    class Triangle {
        int v1;
        int v2;
        int v3;

        Triangle() {
            reset();
        }

        int getV1() {
            return( v1 );
        }

        int getV2() {
            return( v2 );
        }

        int getV3() {
            return( v3 );
        }

        void reset() {
            v1 = -1;
            v2 = -1;
            v3 = -1;
        }

        boolean isFull() {
            return( (v1 != -1) && (v2 != -1) && (v3 != -1) );
        }

        void add( int value ) {
            if ( value == -1 ) {
                reset();
            } else if ( v1 == -1 ) {
                v1 = value;
            } else if ( v2 == -1 ) {
                v2 = value;
            } else if ( v3 == -1 ) {
                v3 = value;
            }
        }

        void shiftTriangle() {
            v2 = v3;
            v3 = -1;
        }
    }


	// RangeReplacer calls this when it has a range of tokens to replace
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    if ( param == null ) {
	        return;
	    }
	    TriParam tparam = (TriParam)param;
	    Field colorIndex = tparam.getColorIndex();
	    Field color = tparam.getColor();
	    Field normalIndex = tparam.getNormalIndex();
	    Field normal = tparam.getNormal();
	    int faceCount = tparam.getApproxFaceCount();
	    int[] faceMultiple = null;
	    if (( colorIndex != null ) || ( color != null ) || ( normalIndex != null ) || ( normal != null )) {
	        faceMultiple = new int[ faceCount ];
	    }
	    
		int scanner = startTokenOffset;
		dataSource.setState( scanner );
		Triangle t = new Triangle();
		int faceNo = 0;
		while ( scanner != -1 ) {
			if ( dataSource.isNumber( scanner )) {
			    int value = dataSource.getIntValue( scanner );
			    t.add( value );
   			    if ( t.isFull() ) {
   			        if ( triangulateQuads ) {
       			        tp.print( t.getV1() );
       			        tp.print( t.getV2() );
       			        tp.print( t.getV3() );
       			        tp.print( -1 );
       			        t.shiftTriangle();
       			        if (( faceNo < faceCount ) && ( faceMultiple != null )) {
       			            faceMultiple[ faceNo ] += 1;
       			        }

       			    }
    			}
       		    if ( value == -1 ) {
       		        faceNo++;
       		    }
            } else {
			    tp.print( dataSource, scanner );
			}
			if ( scanner == endTokenOffset ) {
			    break;
			}
			scanner = dataSource.getNextToken();
		}
		
		// if we have to deal with colors, do it here
		if ( colorIndex != null ) {
		    replaceIndexFaceMultiple( tp, colorIndex, faceMultiple, faceCount );
		}
		
		if ( color != null ) {
		    replaceValueNodeFaceMultiple( tp, color, faceMultiple, faceCount );
		}
		
		if ( normalIndex != null ) {
		    replaceIndexFaceMultiple( tp, normalIndex, faceMultiple, faceCount );
		}
		
		if ( normal != null ) {
		    replaceValueNodeFaceMultiple( tp, normal, faceMultiple, faceCount );
		}
    }

	public void summarize( PrintStream ps ) {
	}
}


