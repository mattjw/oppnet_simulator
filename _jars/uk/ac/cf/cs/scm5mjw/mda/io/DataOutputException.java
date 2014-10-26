/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.io;
     
/** 
  * The exception defined by this class is for errors with outputting
  * data from a simulation.
  */
public class DataOutputException extends RuntimeException
{
    public DataOutputException()
    {
        super();
    }
    
    public DataOutputException( String msg )
    {
        super( msg );
    }
}



