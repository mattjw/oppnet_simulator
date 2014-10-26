/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;
     
/** 
  * An exception for attempts to create invalid artifacts or to use or transfer
  * artifacts in an incorrect way.
  */
public class ArtifactException extends RuntimeException
{
    public ArtifactException()
    {
        super();
    }
    
    public ArtifactException( String msg )
    {
        super( msg );
    }
}


