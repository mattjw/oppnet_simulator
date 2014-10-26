/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;
     
/** 
  * An exception for use when an attempt for two devices to communicate is not
  * valid.
  */
public class CommunicationException extends RuntimeException
{
    public CommunicationException()
    {
        super();
    }
    
    public CommunicationException( String msg )
    {
        super( msg );
    }
}


