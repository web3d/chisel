package com.trapezium.chisel.cleaners;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.FieldId;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.grammar.VRML97;
import com.trapezium.chisel.*;

import java.io.PrintStream;

public class DefaultFieldValueRemover extends Optimizer {
	int numberFieldsRemoved = 0;

	public DefaultFieldValueRemover() {
		super( "All", "Removing default field values..." );
	}

	public boolean isDEFUSElistener() {
		return( true );
	}
	
	public void attemptOptimization( Node n ) {
		VrmlElement parent = n.getParent();
		if ( parent instanceof DEFUSENode ) {
			DEFUSENode dun = (DEFUSENode)parent;
			if ( !dun.isDEF() ) {
				return;
			}
		}
		int numberFields = n.getNumberFields();
		for ( int i = 0; i < numberFields; i++ ) {
			Field f = n.getFieldAt( i );
			String fieldId = f.getFieldId();
			FieldValue fieldValue = f.getFieldValue();
			if ( fieldValue == null ) {
			    continue;
			}
			if ( VRML97.fieldIsDefault( n.getNodeName(), fieldId, fieldValue.getFirstTokenOffset(), fieldValue.getLastTokenOffset(), dataSource )) {
				if ( f.getFirstTokenOffset() != -1 ) {
					replaceRange( f.getFirstTokenOffset(), f.getLastTokenOffset(), null );
				}
			}
		}
		// special case for unused index fields
		if ( n.getBaseName().compareTo( "IndexedFaceSet" ) == 0 ) {
		    checkIndex( n, "color", "colorIndex" );
		    checkIndex( n, "normal", "normalIndex" );
		    checkIndex( n, "texCoord", "texCoordIndex" );
		} else if ( n.getBaseName().compareTo( "IndexedLineSet" ) == 0 ) {
		    checkIndex( n, "color", "colorIndex" );
		}
	}
	
	void checkIndex( Node n, String fieldName, String indexName ) {
	    Field nodeField = n.getField( fieldName );
	    if ( nodeField != null ) {
	        return;
	    }
        Field indexField = n.getField( indexName );
        if ( indexField != null ) {
            replaceRange( indexField.getFirstTokenOffset(), indexField.getLastTokenOffset(), null );
        }
    }

	// Since we are removing the default field, optimize by just doing nothing
	public void optimize( TokenPrinter tp, Object param, int start, int end ) {
		numberFieldsRemoved++;
	}

	public void summarize( PrintStream ps ) {
		if ( numberFieldsRemoved == 0 ) {
			ps.println( "DefaultFieldValueRemover removed no fields." );
		} else if ( numberFieldsRemoved == 1 ) {
			ps.println( "DefaultFieldValueRemover removed 1 field." );
		} else {
			ps.println( "DefaultFieldValueRemover removed " + numberFieldsRemoved + " fields." );
		}
	}
}


