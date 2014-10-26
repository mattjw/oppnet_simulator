/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;

import uk.ac.cf.cs.scm5mjw.mda.SimulatorTools;
import uk.ac.cf.cs.scm5mjw.mda.mobility.*;

import java.awt.geom.Point2D;
     
/** 
  * This is a class which represents a mobile object in the mobile communications
  * system.  <br>
  * <br>
  * Notes on units: <br>
  * * Movement speed is measured in meters-per-second (m/s)
  */
public final class MobileObject extends AbstractWirelessDevice
{
    /* Constants */
    public static final String MOBILE_OBJECT_TEXT_IDENTIFIER = "MobileObject";
    public static final double DEFAULT_MOVEMENT_SPEED = 1.51;
    public static final double DEFAULT_RETURN_BIAS = 0.1;
    
    /* Instance variables */
    private MapNode sourceNode;
    private MapNode destNode;
    private double movementSpeed;
    private double returnBias;
    
    
    
    /**
      * Construct a mobile object with a source MapNode. <br>
      * This constructor will handle determining a destination node by using 
      * getLinkAtWeightedRandom. <br>
      * <br>
      * The source MapNode is where the mobile object will start and the 
      * the destination is where it is heading. <br>
      *  <br>
      * The initial location for the node is the location of the source node. <br>
      * The default speed for mobile objects will be used.
      * 
      * @see #DEFAULT_MOVEMENT_SPEED
      * @see #DEFAULT_RETURN_BIAS
      * @see #getLinkAtWeightedRandom(MapNode)
      */
    public MobileObject( MapNode source )
    {
        super();  // Use default initial values for a wireless device
        
        if( source.getNumberOfLinks() == 0 )
            throw new InvalidMobileObjectException( "Cannot find a destination node for the mobile object when a source node has no links" );
        
        sourceNode = source;
        destNode = getLinkAtWeightedRandom( source ).getGoesTo();
        this.setMovementSpeed( DEFAULT_MOVEMENT_SPEED );
        this.setReturnBias( DEFAULT_RETURN_BIAS );
        
        // We want a new Point object because a MO's location should be independent
        // of its MapNode (we do not want updates of a MO's location to affect the 
        // location of a MapNode) 
        Point2D.Double currentLoc = new Point2D.Double( sourceNode.getLocation().x, sourceNode.getLocation().y );
        setLocation( currentLoc );
    }
    
    
    /**
      * Construct a mobile object with a source MapNode and an explicit destination 
      * MapNode. <br>
      *  <br>
      * The source MapNode is where the mobile object will start and the 
      * the destination is where it is heading. <br>
      *  <br>
      * The initial location for the node is the location of the source node. <br>
      * The default speed for mobile objects will be used.
      * 
      * @see #DEFAULT_MOVEMENT_SPEED
      * @see #DEFAULT_RETURN_BIAS
      */
    public MobileObject( MapNode source, MapNode destination )
    {
        this( source );
        
        destNode = destination;
    }
    
    
    /**
      * Construct a mobile object with the given movement speed as well as the
      * source and destination MapNodes.
      * 
      * @see #DEFAULT_RETURN_BIAS
      */
    public MobileObject( MapNode source, MapNode destination, double inMovementSpeed )
    {
        this( source, destination );
        
        this.setMovementSpeed( inMovementSpeed );
    }
    
    
    
    
    /**
      * Accessor for this MobileObject's source node.
      */
    public MapNode getSourceNode()
    {
        return sourceNode;
    }
    
    
    /**
      * Accessor for this MobileObject's destination node.
      */
    public MapNode getDestinationNode()
    {
        return destNode;
    }
    
    
    /**
      * Accessor for this MobileObject's movement speed.
      */
    public double getMovementSpeed()
    {
        return movementSpeed;
    }
    
    
    /**
      * Accessor for this MobileObject's return bias. <br>
      *  <br>
      * When deciding the next map node to move to, the return bias is used to
      * modify the likelihood that a mobile object will choose a link that takes
      * it back to the map node it just came from (the 'previous node'). <br>
      *  <br>
      * The return bias itself is a fraction. This is used to obtain a fraction
      * of the probability of the link that takes the mobile object to its
      * previous node. <br>
      * For example, a return fraction of 0.5 means that a mobile object is 
      * half as likely to choose the link that takes it back to the previous node. <br>
      *  <br>
      * The fraction should be in the range 0 to 1 (inclusive). <br>
      * A fraction of 0 means that the return link will never be chosen. <br>
      * A fraction of 1 means that the likelihood of the return is not affected
      * at all. <br>
      */
    public double getReturnBias()
    {
        return returnBias;
    }
    
    
    /**
      * Mutator for this MobileObject's source node.
      */
    public void setSourceNode( MapNode source )
    {
        sourceNode = source;
    }
    
    
    /**
      * Mutator for this MobileObject's destination node.
      */
    public void setDestinationNode( MapNode destination )
    {
        destNode = destination;
    }
    
    
    /**
      * Mutator for this MobileObject's movement speed.
      */
    public void setMovementSpeed( double inMovementSpeed )
    {
        if( inMovementSpeed <= 0 )
            throw new IllegalArgumentException( "Movement speed " + inMovementSpeed + " is less than or equal to 0" );
        
        movementSpeed = inMovementSpeed;
    }
    
    
    /**
      * A mutator for this MobileObject's return bias.
      * For a description of what return bias means, see the accessor.
      * 
      * @see #getReturnBias()
      */
    public void setReturnBias( double retBias)
    {
        if( (retBias < 0) || (retBias > 1) )
            throw new IllegalArgumentException( "Return bias should be in the range [0,1]" );
        
        returnBias = retBias;
    }
    
    
    /**
      * Get a string representation of this MobileObject.
      */
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        
        buff.append( "[\n" );
        buff.append( "\t<Mobile Object>\n" );
        buff.append( "\t" + super.toString() + ", Speed: " + movementSpeed + "\n" );
        buff.append( "\tSource: \t" + sourceNode + "\n" );
        buff.append( "\tDestination: \t" + destNode + "\n" );
        buff.append( "]" );
        
        return buff.toString();
    }
    
    
    /**
      * A textual identifier for this TYPE of device.
      */
    public String getDeviceTypeIdentifier()
    {
        return MOBILE_OBJECT_TEXT_IDENTIFIER;
    }
    
    
    
    
    /**
      * Advance this mobile object's mobility (movement) by the given time.
      * i.e. this moves this mobile object forward by the given duration. <br>
      * <br>
      * This method handles: <br>
      * * choosing the next map node, when the device reaches the map node  <br>
      * * advancing the device past many map nodes (in the case that distance
      *   travelled will cover a number of links)
      * * using backtrack biasing when deciding which link/node to choose next
      * 
      * @see #getLinkAtWeightedRandom(MapNode, MapNode, double)
      */
    public void advanceMobility( double duration )
    {
        MapNode sourceNode = this.getSourceNode();          // Local var for local manipulation
        MapNode destNode = this.getDestinationNode();       // Local var for local manipulation
        Point2D.Double currLoc = this.getLocation();        // Local var for local manipulation
        
        // The REMAINING distance the mobile object will travel in this time step:
        double distToTravel = duration * this.getMovementSpeed();      
        // The distance the mobile object has to travel to reach its current destination node:
        double distToNode = SimulatorTools.distance( currLoc, destNode.getLocation() );
        
        
        
        
        /* Here we do the traversing to move the mobile obj. along its route */
        while( distToTravel > distToNode )     // keep going while we still have leftover distance to travel
        {
            // Choose the new destination based on the current destination and 
            // its previous node (i.e. the source node).
            NodeLink newDestLink = getLinkAtWeightedRandom( destNode, sourceNode, returnBias );
            
            // Jump to the destination node and 'use up' the distance needed to reach there
            distToTravel -= distToNode;
            sourceNode = destNode;
            currLoc = destNode.getLocation();
            
            destNode = newDestLink.getGoesTo();
            
            // Update the distance to the dest node (because destination has changed );
            distToNode = SimulatorTools.distance( currLoc, destNode.getLocation() );
        }
        
        
        /* We now have the source, destination and current location of the 
           mobile object WITH DISTANCE STILL REMAINING TO GO. We are now
           sure that the distance remaining is LESS THAN OR EQUAL to the
           distance to the next node. We are also sure that the distance remaining
           to the next node is greater than 0. */
        this.setSourceNode( sourceNode );
        this.setDestinationNode( destNode );
        
        
        /* Move the mobile object along its final route */
        double ratio = distToTravel / distToNode;
        
        Point2D.Double destLoc = destNode.getLocation();
        double dx = destLoc.x - currLoc.x;
        double dy = destLoc.y - currLoc.y;
        double newX = currLoc.x + (dx*ratio);
        double newY = currLoc.y + (dy*ratio);
        
        Point2D.Double mObjLoc = this.getLocation(); // The Point object (coordinates) for the mobile object
        mObjLoc.x = newX;
        mObjLoc.y = newY;
        this.setLocation( mObjLoc );
    }
    
    
        
    
    /**
      * This method will get one of the MapNode's links at random, taking into
      * account the weight of each link. (For example, if a link has a weight 0.3,
      * this means the probability that that link is chosen is 0.3) <br>
      * <br>
      * If the MapNode does not have any links, then it will return null.
      * Thus, it is possible for a MapNode to be a dead end. Ensuring that 
      * there are no dead ends in a map is not the responsibility of this class. <br>
      *  <br>
      * Note that this method does not take into account return bias (i.e. it
      * does not reduce the likelihood of returning to previous node).
      */
    private static NodeLink getLinkAtWeightedRandom( MapNode node )
    {
        int numLinks = node.getNumberOfLinks();
        double x = Math.random();
        
        double sum = 0;
        for( int i=0; i < numLinks; i++ )
        {
            sum = sum + node.getLinkAt(i).getWeight();
            
            if( x < sum )
                return node.getLinkAt(i);
        }
        
        // Node has no links...
        return null;
    }
    
    
    /**
      * This method is similar to getLinkAtWeightedRandom, except it will carry
      * out the biasing using the return bias. <br>
      *  <br>
      * It is possible that there is no link from the given node back to the
      * previous node (because the graph is directed). If this is the case, then
      * no biasing is carried out. <br>
      *  
      * @param node the node from which a link should be chosen
      * @param prev the node that <i>node</i> just came from
      * @param k the return bias (a fraction)
      */
    private static NodeLink getLinkAtWeightedRandom( MapNode node, MapNode prev, double k )
    {
        assert 0 <= k;
        assert k <= 1;
        
        NodeLink returnLink = node.getLinkToLocation( prev );
        
        if( returnLink == null )
        {
            // If the node does not actually have any link back to the previous
            // node, then we do not need to adjust the probabilities
            
            return getLinkAtWeightedRandom( node );
        }
        else
        {
            // If the node does link back to the previous node, then we need to
            // adjust the probabilities as we determine the link to be chosen
            
            int numLinks = node.getNumberOfLinks();
            
            if( numLinks == 1 )
            {
                // If there is only one link, then it does not matter about bias
                // -- we HAVE to choose that link
                return node.getLinkAt(0);
            }
            else
            {
                // If there is more than one link (and since we know that one of
                // them leads is back to our previous node) we must apply the
                // adjustment of probabilities with the given fraction
                
                double x = Math.random();
                
                double a = returnLink.getWeight();     // the return weight (AKA the return probability) (this is the original probability for the return link)
                double c = ( 1 - k*a ) / ( 1 - a );
                    // c is a constant which any of the other probabilities can 
                    // be multiplied by to bring the sum of adjusted probabilities
                    // to 1 while also maintaining their proportions
                
                double sum = 0;
                for( int i=0; i < numLinks; i++ )
                {
                    NodeLink link = node.getLinkAt( i );
                    
                    if( link == returnLink )
                    {
                        sum = sum + link.getWeight()*k;
                    }
                    else
                    {
                        sum = sum + link.getWeight()*c;
                    }
                    
                    if( x < sum )
                        return node.getLinkAt(i);
                }
            }
        }
        
        return null;
    }
}
