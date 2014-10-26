/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.mobility;

import java.awt.geom.Point2D;
     
/** 
  * This class is for representing a link to a map node. A link is
  * uni-directional. A link has a weight which indicates the probability
  * that this link will be selected to be traversed by a mobile object.
  * NodeLinks are intentionally immutable because the end node of the link
  * should not be able to change.
  */
public final class NodeLink
{
    /* Instance variables */
    private MapNode destination;
    private double weight;
    
    
    
    
    /**
      * Construct a link to the given node with a given weight..
      */
    public NodeLink( MapNode dest, double wgt )
    {
        if( (wgt < 0) || (wgt > 1) )
            throw new IllegalArgumentException( "Weight was not in the interval [0,1]" );
        
        weight = wgt;
        destination = dest;
    }
    
    
    
    
    /**
      * An accessor for the node that this link goes to (i.e. the 'end of this
      * link').
      */
    public MapNode getGoesTo()
    {
        return destination;
    }
    
    
    /**
      * An accessor for the weight of this link.
      */
    public double getWeight()
    {
        return weight;
    }
    
    
    /**
      * This method introduces the concept of whether or not NodeLinks are equal.
      * Links are defined to be equal solely based on the node they go to. Two
      * links are equal if the map nodes they go to (i.e. their destination)
      * are at the same location. <br>
      *  <br>
      * Note that this means two links with different weights can still be equal.
      */
    public boolean equals( NodeLink n )
    {
        return this.destination.locationEquals( n.destination );
    }
    
    
    /**
      * Get a string representation of this NodeLink. Includes the coords of the
      * destination node and the link's weight.
      */
    public String toString()
    {
        Point2D.Double destLoc = destination.getLocation();
        String str = "(" + destLoc.x + "," + destLoc.y + ") [" + weight + "]";
        return str;
    }
}
