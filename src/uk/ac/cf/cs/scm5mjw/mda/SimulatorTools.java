/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda;

import java.awt.geom.Point2D;
     
/** 
  * This is a static class which provides general tools to the Simulator.
  */
public final class SimulatorTools
{
    
    /** 
      * A method which finds the distance between the two points.
      */
    public static double distance( Point2D a, Point2D b )
    {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        
        double dxPow2 = dx*dx;
        double dyPow2 = dy*dy;
        
        return Math.sqrt( dxPow2 + dyPow2 );
    }
    
    
    /** A method which generates a random integer in the given range,
      * INCLUSIVE of the values at either end of the range.
      */
    public static int randInRange( int min, int max )
    {
        int interval = max-min;
        double rand = Math.random() * (interval + 1);
        return min + (int)rand;
    }
}
