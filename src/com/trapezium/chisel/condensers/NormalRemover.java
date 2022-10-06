package com.trapezium.chisel.condensers;

import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.node.Node;
import com.trapezium.chisel.*;

import java.io.PrintStream;

public class NormalRemover extends Optimizer {
    public NormalRemover() {
        super( "CoordinateOwner" , "Removing normal fields..." );
    }
    
    public void summarize( PrintStream ps ) {
    }

    public void attemptOptimization( Node n ) {
        Field normalPerVertex = n.getField( "normalPerVertex" );
        if ( normalPerVertex != null ) {
            replaceRange( normalPerVertex.getFirstTokenOffset(), normalPerVertex.getLastTokenOffset(), null );
        }
        Field normalIndex = n.getField( "normalIndex" );
        if ( normalIndex != null ) {
            replaceRange( normalIndex.getFirstTokenOffset(), normalIndex.getLastTokenOffset(), null );
        }
        Field normal = n.getField( "normal" );
        if ( normal != null ) {
            replaceRange( normal.getFirstTokenOffset(), normal.getLastTokenOffset(), null );
        }
    }

    public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
    }
}
