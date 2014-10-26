/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;
     
/** 
  * An exception for use when an information source is found to be invalid.
  */
public class InvalidInformationSourceException extends RuntimeException
{
    public InvalidInformationSourceException()
    {
        super();
    }
    
    public InvalidInformationSourceException( String msg )
    {
        super( msg );
    }
}


