package com.trapezium.vrml.visitor;

import com.trapezium.pattern.Visitor;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.NodeSelection;

public class NodeSelectionVisitor extends Visitor {
    int proposedStartLine;
    int proposedEndLine;
    int proposedStartColumn;
    int proposedEndColumn;
    boolean foundNode;
    NodeSelection nodeSelection;

    public NodeSelectionVisitor( TokenEnumerator dataSource, NodeSelection nodeSelection ) {
        super( dataSource );
        this.nodeSelection = nodeSelection;
        foundNode = false;
        proposedStartLine = -1;
        proposedEndLine = -1;
        proposedStartColumn = -1;
        proposedEndColumn = -1;
    }

    public void proposeSelection( int firstToken, int lastToken, int firstLine, int lastLine, int firstColumn, int lastColumn ) {
        if ( proposedStartLine == -1 ) {
            makeSelection( firstToken, lastToken, firstLine, lastLine, firstColumn, lastColumn );
        } else if ( firstLine > proposedStartLine ) {
            makeSelection( firstToken, lastToken, firstLine, lastLine, firstColumn, lastColumn );
        } else if ( lastLine < proposedEndLine ) {
            makeSelection( firstToken, lastToken, firstLine, lastLine, firstColumn, lastColumn );
        }
    }

    void makeSelection( int firstToken, int lastToken, int firstLine, int lastLine, int firstColumn, int lastColumn ) {
        proposedStartLine = firstLine;
        proposedEndLine = lastLine;
        proposedStartColumn = firstColumn;
        proposedEndColumn = lastColumn;
        nodeSelection.firstNodeToken = firstToken;
        nodeSelection.lastNodeToken = lastToken;
    }

    public boolean visitObject( Object a ) {
        if ( a instanceof Node ) {
            Node n = (Node)a;
            int firstToken = n.getFirstTokenOffset();
            int lastToken = n.getLastTokenOffset();
            if (( firstToken == -1 ) || ( lastToken == -1 )) {
                return( true );
            }
            int firstLine = dataSource.getLineNumber( firstToken );
            int lastLine = dataSource.getLineNumber( lastToken );
            // if node lines include selection, then this might be the node
            if (( firstLine <= nodeSelection.startLine ) && ( lastLine >= nodeSelection.endLine )) {
                int firstColumn = dataSource.getLineOffset( firstToken );
                int lastColumn = dataSource.getLineOffset( lastToken );
                // columns only matter if on same line
                if (( firstLine == nodeSelection.startLine ) && ( nodeSelection.startColumn < firstColumn )) {
                    return( true );
                }
                if (( lastLine == nodeSelection.endLine ) && ( nodeSelection.endColumn > lastColumn )) {
                    return( true );
                }
                // At this point, the Node contains the selection,
                proposeSelection( firstToken, lastToken, firstLine, lastLine, firstColumn, lastColumn );
            }
        }
        return( true );
    }
    
    /** Update the nodeSelection based on visitor results.
     *  
     *  @return true if nodeSelection modified, otherwise false
     */
    public boolean updateNodeSelection() {
        if ( proposedStartLine != -1 ) {
            nodeSelection.startLine = proposedStartLine;
            nodeSelection.endLine = proposedEndLine;
            nodeSelection.startColumn = proposedStartColumn;
            nodeSelection.endColumn = proposedEndColumn;
            return( true );
        } else {
            return( false );
        }
    }
}
