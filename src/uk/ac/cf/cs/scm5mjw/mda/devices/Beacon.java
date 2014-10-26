/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;

import java.awt.geom.Point2D;
     
/** 
  * This is a class which represents a beacon in the mobile communications
  * system. 
  *
  * Notes on units:
  *     n/a
  */
public final class Beacon extends AbstractWirelessDevice
{ 
    public static final String BEACON_TEXT_IDENTIFIER = "Beacon";
    
    
    
    
    /**
      * Construct a Beacon at the given location.
      */
    public Beacon( Point2D.Double inLocation )
    {
        super();
        setLocation( inLocation );
    }
    
    
    
    
    /**
      * Get a string representation of this Beacon.
      */
    public String toString()
    {
        String str = "[ <Beacon> " + super.toString() + "]";
        return str;
    }
    
    
    /**
      * A textual identifier for this TYPE of device.
      */
    public String getDeviceTypeIdentifier()
    {
        return BEACON_TEXT_IDENTIFIER;
    }
}
