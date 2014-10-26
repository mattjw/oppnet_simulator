/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;
     
/** 
  * An exception for use when a mobile object is found to be invalid.
  */
public class InvalidMobileObjectException extends RuntimeException
{
    public InvalidMobileObjectException()
    {
        super();
    }
    
    public InvalidMobileObjectException( String msg )
    {
        super( msg );
    }
}


