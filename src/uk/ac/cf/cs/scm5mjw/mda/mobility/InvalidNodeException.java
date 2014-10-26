/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.mobility;
     
/** 
  * An exception for use when a node is found to be invalid (such as adding
  * multiple nodes which have the same coordinates to a map).
  */
public class InvalidNodeException extends RuntimeException
{
    public InvalidNodeException()
    {
        super();
    }
    
    public InvalidNodeException( String msg )
    {
        super( msg );
    }
}


