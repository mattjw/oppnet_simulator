/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.mobility;

import uk.ac.cf.cs.scm5mjw.mda.devices.*;

import java.util.Vector;
    
/** 
  * This is a class which represents a map. A map is the layout of a particular
  * scenario.
  */
public final class MobilityMap
{
    /* Instance variables */
    private Vector<MapNode> nodes;
    
    
    
    
    /**
      * Construct a MobilityMap with no nodes (yet).
      */
    public MobilityMap()
    {
        nodes = new Vector<MapNode>();
    }
    
    
    
    
    /**
      * An accessor to get the ith node in this map. <br>
      * Note that since the nodes are held internally as a Vector, this will
      * throw an exception if the index is out of bounds (as intended).
      */
    public MapNode getNodeAt( int i )
    {
        if( i < 0 )
            throw new IndexOutOfBoundsException( "Index " + i + " is less than 0" );
        if( i > (getNumberOfNodes()-1) )
            throw new IndexOutOfBoundsException( "Index " + i + " is out of the range of the indexable nodes" );
        
        return nodes.get(i);
    }
    
    
    /**
      * Returns the number of nodes being held in this map.
      */
    public int getNumberOfNodes()
    {
        return nodes.size();
    }
    
    
    /**
      * Tests if this map has no nodes.
      */
    public boolean isEmpty()
    {
        return nodes.isEmpty();
    }
    
    
    /** 
      * This method adds a node to this map.
      * If a node already exists at the given location (coordinates), then an
      * error will be thrown.
      */
    public void addNode( MapNode n )
    {
        if( containsByLocation(n) )
            throw new InvalidNodeException( "A map cannot contain two nodes at the same location" );
        
        nodes.add( n );
    }
    
    
    /**
      * This method checks whether a node at the same location as the given node
      * already exists in this map.
      *
      * Note that this checks EVERY node in the map sequentially, which can have
      * poor performance for maps with many nodes. However, since this method should
      * only be used while constructing a map, not during a simulation, the simulation's
      * performance should not be affected.
      */
    public boolean containsByLocation( MapNode node )
    {
        for( int i=0; i < nodes.size(); i++ )
        {
            if( nodes.get(i).locationEquals( node ) )
                return true;
        }
        
        return false;
    }
    
    
    /**
      * This method tests if the map contains a given node based on the equality
      * of their references.
      */
    public boolean contains( MapNode node )
    {
        return nodes.contains( node );
    }
    
    
    /**
      * This method will check if this map is valid for the given set of mobile
      * objects. To be valid, each mobile object:
      *     1. Must have a source node which belongs to the map
      *     2. Must have a destination node which belongs to the map
      *     3. Must have a source node and destination node where the source node
      *        has a link to the destination node.
      */
    public boolean isValidFor( Vector<MobileObject> coll )
    {
        for( MobileObject mo: coll )
        {
            MapNode source = mo.getSourceNode();
            MapNode dest = mo.getDestinationNode();
            
            if( !source.isLinkedToLocation( dest ) )
                return false;
            
            if( !contains( source ) )
                return false;
            
            if( !contains( dest ) )
                return false;
        }
        
        return true;
    }
    
    
    /**
      * This method will check whether this map has dead ends. A dead end is
      * a node which does not have any links going away from it.
      */
    public boolean hasDeadEnds()
    {
        // Check for dead ends
        for( MapNode n: nodes )
        {
            if( n.getNumberOfLinks() < 1 )
                return true;
        }
        
        return false;
    }
    
    
    /**
      * This method generates and returns a string representation of this
      * Map.
      */
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        
        buff.append( "[\n" );
        for( int i=0; i < nodes.size(); i++ )
            buff.append( "\t" + nodes.get(i) + "\n" );
        buff.append( "]\n" );
        
        return buff.toString();
    }
}


