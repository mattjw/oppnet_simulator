/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.mobility;
     
/** 
  * An exception for use when a link between MapNodes is not valid.
  */
public class InvalidLinkException extends RuntimeException
{
    public InvalidLinkException()
    {
        super();
    }
    
    public InvalidLinkException( String msg )
    {
        super( msg );
    }
}


