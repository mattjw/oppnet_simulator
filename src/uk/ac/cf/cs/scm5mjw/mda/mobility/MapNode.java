/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.mobility;
     
import java.awt.geom.Point2D;
import java.util.Vector;
     
/** 
  * This is a class which represents a node on a map. A node is similar to a
  * vertex in graph theory. A vertex can be connected to many other vertices.
  * In the context of our system, a node is a point at which a mobile object may: <br>
  *     - Change direction <br>
  * (Decision points may even be used as points at which a pause may occur) <br>
  * The possible directions a mobile object may move in, at a given node, is
  * determined by the other nodes it is linked to.
  */
public final class MapNode
{
    /* Instance variables */
    private Vector<NodeLink> links;   // Links to other nodes
    private Point2D.Double coord;
    
    
    
    
    /**
      * Construct a node with no links.
      */
    public MapNode( Point2D.Double inCoord )
    {
        links = new Vector<NodeLink>();
        coord = inCoord;
    }
    
    
    
    
    /**
      * This adds a link to another node in one direction. To elaborate:
      * This node will be connected to the given node. Note that the given node
      * might not be connected in the other direction. <br>
      * <br>
      * A node may not link to itself. 
      * A node may not link to a node at the same location as itself.
      * A node may not link to two nodes at the same location.
      */
    public void addLinkOneway( NodeLink inLink )
    {
        if( this == inLink.getGoesTo() )
            throw new InvalidLinkException( "A node cannot link to itself" );
        
        if( inLink.getGoesTo().locationEquals( this ) )
            throw new InvalidLinkException( "A node cannot link to a node at the same location as itself" );
        
        if( this.isLinkedToLocation( inLink.getGoesTo() ) )
            throw new InvalidLinkException( "A node cannot have two links to the same location" );
        
        links.add( inLink );
    }
    
    
    /**
      * The method overloads the addLinkOneway method to give a way of
      * adding a link without having to create the NodeLink object explicitly.
      * This method will handle creating the NodeLink from the given parameters.
      */
    public void addLinkOneway( MapNode dest, double wgt )
    {
        NodeLink l = new NodeLink( dest, wgt );
        this.addLinkOneway( l );
    }
    
    
    /**
      * This method checks whether this node has a link to a map node with the
      * same location as the given MapNode. <br>
      * <br>
      * The algorithm simply runs through each link currently in the collection
      * and determines if its is equal to the given node (two nodes are equal if 
      * they are at the same coordinates). This will have poor efficiency for nodes
      * with a large number of links. <br>
      * Since this method is not used during the simulation, poor performance
      * here will not affect the performance of the simulation.
      */
    public boolean isLinkedToLocation( MapNode inLink )
    {
        for( int i=0; i < links.size(); i++ )
        {
            if( links.get(i).getGoesTo().locationEquals( inLink ) )
                return true;
        }
        
        return false;
    }
    
    
    /**
      * This method will return the link that links this MapNode to the given
      * MapNode. If no such link exists, null is returned;
      */
    public NodeLink getLinkToLocation( MapNode inNode )
    {
        for( NodeLink link : links )
        {
            if( link.getGoesTo().locationEquals( inNode ) )
                return link;
        }
        
        return null;
    }
    
    
    /**
      * Accessor for the location of this node.
      */
    public Point2D.Double getLocation()
    {
        return coord;
    }
    
    
    /**
      * An accessor to get this node's ith link. <br>
      * Note that since the links are held internally as a Vector, this
      * will throw an exception if the index is out of bounds (as intended).
      */
    public NodeLink getLinkAt( int i )
    {
        return links.get(i);
    }
    
    
    /**
      * Returns the number of nodes that this node is linked to.
      */
    public int getNumberOfLinks()
    {
        return links.size();
    }
    
    
    /**
      * This method simply checks whether this MapNode and the given MapNode
      * are at the same location.
      */
    public boolean locationEquals( MapNode n )
    {
        return this.coord.equals( n.coord ); 
    }
    
    
    /**
      * Get a string representation of this MapNode. It also includes the coords
      * of the nodes that this node is linked to.
      */
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        
        // Add info on THIS node to the text
        String str = "(" + coord.x + "," + coord.y + ")";
        buff.append( "Node coords: " + str );
        
        
        // Add info on this node's linked nodes to the text
        buff.append( "  \tLinks to: " );
        
        if( links.isEmpty() )
            buff.append( "nothing" );
        else
        {
            for( int i=0; i < links.size(); i++ )
            {
                buff.append( links.get(i) );
                
                if( i < links.size()-1 )
                    buff.append( "; " );
            }
        }
        
        
        return buff.toString();
    }
}


