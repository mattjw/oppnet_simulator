/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda;
     
/** 
  * An exception for use when a MobilityMap is not suitable for a Simulator. 
  */
public class UnsuitableMapException extends RuntimeException
{
    public UnsuitableMapException()
    {
        super();
    }
    
    public UnsuitableMapException( String msg )
    {
        super( msg );
    }
}


