package com.trapezium.vrml.grammar;

/** The DEFNameFactory provides an interface option for the VRML97parser
 *  that allows it to automatically generate DEF names for nodes that
 *  do not have DEF names.
 */
public interface DEFNameFactory {
    /** Create a DEF name.
     *
     *  @param nodeOrPROTOname the name of the Node or PROTO.  This is
     *     provided so that name generation can be limited to a subset of
     *     nodes.
     *
     *  @return the generated name, or null if no DEF is to be provided
     *     for the node.
     */
    String createDEFName( String nodeOrPROTOname );
}
