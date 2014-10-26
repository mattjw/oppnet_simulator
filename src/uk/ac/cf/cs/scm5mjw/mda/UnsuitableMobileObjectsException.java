/*   Matthew Williams (0515328)   */
     
package uk.ac.cf.cs.scm5mjw.mda;

/** 
  * An exception for use when a set of MobileObjects is not suitable for a Simulator. 
  */
public class UnsuitableMobileObjectsException extends RuntimeException
{
    public UnsuitableMobileObjectsException()
    {
        super();
    }
    
    public UnsuitableMobileObjectsException( String msg )
    {
        super( msg );
    }
}


