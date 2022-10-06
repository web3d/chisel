package com.trapezium.chisel.condensers;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.chisel.*;

import java.io.PrintStream;
import java.util.Vector;

//
//  The DEFmaker chisel either creates DEF/USE out of everything possible
//  in the file, or creates DEF/USE for a single node type.
//

public class DEFmaker extends Optimizer {
	Vector optimizations;
	int DEFsCreated = 0;
	int USEsCreated = 0;
	static int lastGenId = 1;
	boolean optionValues[];

	class OptimizationParam {
		Node node;
		boolean def;
		boolean use;
		OptimizationParam defRef;
		String name;

		public OptimizationParam( Node node ) {
			this.node = node;
			def = false;
			use = false;
			name = null;
		}

		public void generateName() {
			Scene scene = (Scene)node.getScene();
			while ( true ) {
				String testName = "chsl_" + lastGenId;
				if ( !scene.DEFexists( testName )) {
//				    System.out.println( testName + " generated for node " + node.getBaseName() + ", scene " + scene.getSceneId() );
					scene.registerDEF( testName );
					name = testName;
					break;
				}
				lastGenId++;
			}
		}

		public OptimizationParam getDefRef() {
			return( defRef );
		}

		public String getName() {
			return( name );
		}

		public Node getNode() {
			return( node );
		}

		public void markAsUSE( OptimizationParam defRef ) {
			use = true;
			this.defRef = defRef;
		}

		public boolean isUSE() {
			return( use );
		}

		public void markAsDEF() {
			def = true;
		}

		public boolean isDEF() {
			return( def );
		}
	}

	public DEFmaker( String nodeType ) {
		super( nodeType, "Creating DEF/USE nodes..." );
		reset();
	}
	
	public void reset() {
		optimizations = new Vector();
		initValues();
	}

	public DEFmaker() {
		super( "All", "Creating DEF/USE nodes..." );
		setNodeName( "All" );
		reset();
	}
	
	void initValues() {
	    if ( optionValues == null ) {
	        optionValues = new boolean[3];
	    }
	    optionValues[0] = true;
	    optionValues[1] = false;
	    optionValues[2] = false;
	}

    
	//
	//  Each time a node is found, we immediately save it, along with an object describing
	//  the optimization to be performed on it.  The optimization is either to DEF the object
	//  or to USE a previously DEFed object.  
	//
	//  When two identical objects are found, the following scenarios must be handled:
	//
	//  1. neither object is DEFed.  In this case, the first object is DEFed with a generated
	//     name "chsl_<n>", and the second object USEs that name.
	//  2. the first object is DEFed, the second is not.  In this case, it would seem that
	//     we can just USE the second object.  However, this is only possible if there is
	//     no other object DEFed with that name.  During parsing we are not marking this
	//     condition, so in this case, we do nothing.
	//  3. the first object is not DEFed, the second object is.  In this case, it would seem
	//     that we can just move the DEF from the second object to the first.  However, we
	//     encounter a similar problem as in step 2 -- that we don't know the valid scope of
	//     the DEF.
	//  4. both objects are DEFed.
	//
	//  At the moment, steps 2 through 4 have a problem due to the current state of parsing,
	//  which is that we have no idea of the valid scope of a DEF.  This could be solved
	//  with the "double DEF" solution, but there has been no resolution on whether this
	//  is actually valid according to the spec (as well as whether the major viewers support
	//  it).  So until that time, at the time we create the optimization object, we check
	//  if the node is part of a DEF, and if it is, we note in the optimization object that
	//  the node is unavailable for the optimization.
	//
	//  The above will be corrected by the parsing when we know that a DEF is not in
	//  conflict with others (probably should add a method to DEF/USE to indicate this).
	//  In that case, we can do steps #2 and #3 if there are no conflicts.  
	//
	//  Step #4 is more difficult because it requires making the second DEF a USE of the
	//  first, and renaming all subsequent USE _#2_ into USE _#1_.  The problem with this
	//  is that the NodeLocatorVisitor ignores USE nodes.
	//

	public void attemptOptimization( Node n ) {
		boolean isDEF = ( n.getParent() instanceof DEFUSENode );
		VrmlElement scanner = n;
//		System.out.println( "AttemptOptimization for " + n.getBaseName() );
		while ( scanner != null ) {
		    if ( scanner instanceof PROTO ) {
//		        System.out.println( "Bail, parent is PROTO" );
		        return;
		    }
		    if ( scanner instanceof PROTOInstance ) {
		        return;
		    }
		    scanner = scanner.getParent();
		}
		if ( !isDEF ) {
			int firstTokenOffset = n.getFirstTokenOffset();
			if ( firstTokenOffset == -1 ) {
			    return;
			}
			OptimizationParam op = new OptimizationParam( n );
			int lastTokenOffset = n.getLastTokenOffset();
			replaceRange( firstTokenOffset, lastTokenOffset, op );
//			System.out.println( "not a DEF, replace range from " + firstTokenOffset + " to " + lastTokenOffset );

			// for the moment, we implement only step #1 above.  The optimizations list
			// only contains nodes that are not DEFed.  We look through all previously
			// listed optimization parameters, and if we find any node with exactly the
			// same tokens as this one, we mark the first as a DEF, and ours as a USE.
			int optimizationCount = optimizations.size();
			optimizations.addElement( op );
			for ( int i = 0; i < optimizationCount; i++ ) {
				OptimizationParam testop = (OptimizationParam)optimizations.elementAt( i );

				// If this is already a "USE" optimization, the identical DEF has already
				// been encountered since we look at these in order.
				if ( testop.isUSE() ) {
					continue;
				}
				Node on = testop.getNode();
				int testOpFirstTokenOffset = on.getFirstTokenOffset();
				int testOpLastTokenOffset = on.getLastTokenOffset();
            
                // Note:  this "tokensMatch" method should be replaced by a field by field node
                // level match.  This field by field match has to take into account MF fields
                // (or as a first pass match these at the token level as well)
				if ( tokensMatch( firstTokenOffset, lastTokenOffset, 
				    testOpFirstTokenOffset, testOpLastTokenOffset )) {
				    // We have identical tokens between the node in the "testop" and the node
				    // we are possibly USEing.  
				    //
				    // At this point, there are a couple possible problems.  
				    //
				    //  1. If the area we are about to USE is already within a DEF, it has
				    //     already been optimized and there is no point in continuing.  If the
				    //     area is already within a USE, it too has already been optimized.
				    //  2. If the area we are about to DEF is already within another USE area, we 
				    //     cannot DEF it, since that DEF would disappear when the USE is generated.
				    // 
				    
				    // handle problem #1
				    if ( rangeContainedInDEF( op, firstTokenOffset, lastTokenOffset )) {
				        return;
				    }
				    if ( rangeContainedInUSE( op, firstTokenOffset, lastTokenOffset )) {
				        return;
				    }
				    // handle problem #2
				    if ( !rangeContainedInUSE( testop, testOpFirstTokenOffset, testOpLastTokenOffset )) {
       					testop.markAsDEF();
       					op.markAsUSE( testop );
    				}
				}
			}
		}
	}
	
	//
	//  Check if the token range is contained in something turning into a USE node.
	//
	boolean rangeContainedInDEFUSE( OptimizationParam op, int firstOpOffset, int lastOpOffset ) {
	    int optimizationCount = optimizations.size();
	    for ( int i = 0; i < optimizationCount; i++ ) {
	        OptimizationParam testop = (OptimizationParam)optimizations.elementAt( i );
	        if ( testop == op ) {
	            continue;
	        }
	        if ( testop.isUSE() || testop.isDEF() ) {
    	        Node on = testop.getNode();
    	        int testOpFirstTokenOffset = on.getFirstTokenOffset();
    	        int testOpLastTokenOffset = on.getLastTokenOffset();
    	        if (( testOpFirstTokenOffset >= firstOpOffset ) && 
    	            ( testOpLastTokenOffset <= lastOpOffset )) {
    	                return( true );
                }
            }
        }
        return( false );
    }
	boolean rangeContainedInUSE( OptimizationParam op, int firstOpOffset, int lastOpOffset ) {
	    int optimizationCount = optimizations.size();
	    for ( int i = 0; i < optimizationCount; i++ ) {
	        OptimizationParam testop = (OptimizationParam)optimizations.elementAt( i );
	        if ( testop == op ) {
	            continue;
	        }
	        if ( testop.isUSE() ) {
    	        Node on = testop.getNode();
    	        int testOpFirstTokenOffset = on.getFirstTokenOffset();
    	        int testOpLastTokenOffset = on.getLastTokenOffset();
    	        if (( testOpFirstTokenOffset <= firstOpOffset ) && 
    	            ( testOpLastTokenOffset >= lastOpOffset )) {
    	                return( true );
                }
            }
        }
        return( false );
    }
	boolean rangeContainedInDEF( OptimizationParam op, int firstOpOffset, int lastOpOffset ) {
	    int optimizationCount = optimizations.size();
	    for ( int i = 0; i < optimizationCount; i++ ) {
	        OptimizationParam testop = (OptimizationParam)optimizations.elementAt( i );
	        if ( testop == op ) {
	            continue;
	        }
	        if ( testop.isDEF() ) {
    	        Node on = testop.getNode();
    	        int testOpFirstTokenOffset = on.getFirstTokenOffset();
    	        int testOpLastTokenOffset = on.getLastTokenOffset();
    	        if (( testOpFirstTokenOffset <= firstOpOffset ) && 
    	            ( testOpLastTokenOffset >= lastOpOffset )) {
    	                return( true );
                }
            }
        }
        return( false );
    }
	        

	boolean tokensMatch( int a_first, int a_last, int b_first, int b_last ) {
		int a_scanner = a_first;
		int b_scanner = b_first;
		while ( true ) {
			if ( a_scanner == -1 ) {
				return( false );
			}
			if ( b_scanner == -1 ) {
				return( false );
			}
			if ( !dataSource.sameAs( a_scanner, b_scanner )) {
				return( false );
			}
			if ( a_scanner == a_last ) {
				if ( b_scanner == b_last ) {
					return( true );
				} else {
					return( false );
				}
			} else if ( b_scanner == b_last ) {
				return( false );
			}
			a_scanner = dataSource.getNextToken( a_scanner );
			b_scanner = dataSource.getNextToken( b_scanner );
		}
	}

	public boolean optimizePossible( Object param ) {
		if ( param instanceof OptimizationParam ) {
			OptimizationParam op = (OptimizationParam)param;
			return( op.isDEF() || op.isUSE() );
		} else {
			return( false );
		}
	}

	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
		if ( param instanceof OptimizationParam ) {
			OptimizationParam op = (OptimizationParam)param;
			if ( op.isDEF() ) {
				op.generateName();
//			    System.out.println( startTokenOffset + " to " + endTokenOffset + " is DEF! " + op.getName() );
				tp.print( "DEF " + op.getName() );
//				System.out.println( op.getName() + " used for " + op.getNode().getBaseName() );
				tp.printRange( startTokenOffset, startTokenOffset, false );
				replaceStartEnd( startTokenOffset, endTokenOffset, startTokenOffset, startTokenOffset );
				DEFsCreated++;
			} else if ( op.isUSE() ) {
				OptimizationParam defRef = op.getDefRef();
//			    System.out.println( startTokenOffset + " to " + endTokenOffset + " is USE! " + defRef.getName() );
				tp.print( "USE " + defRef.getName() );
				USEsCreated++;
			} else {
//			    System.out.println( "Not optimizing " + startTokenOffset + " to " + endTokenOffset );
				tp.printRange( startTokenOffset, endTokenOffset, false );
			}
		}
	}

	public void summarize( PrintStream ps ) {
		if ( DEFsCreated == 0 ) {
			ps.println( "DEFmaker did nothing." );
		} else if ( DEFsCreated == 1 ) {
			ps.println( "DEFmaker created 1 DEF." );
		} else {
			ps.println( "DEFmaker created " + DEFsCreated + " DEFs." );
		}
		if ( USEsCreated == 1 ) {
			ps.println( "DEFmaker created 1 USE." );
		} else if ( USEsCreated > 1 ) {
			ps.println( "DEFmaker created " + USEsCreated + " USEs." );
		}
	}
}


