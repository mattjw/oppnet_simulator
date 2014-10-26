/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.io;

/** 
  * This class is a supertype for errors which occur while parsing from a text
  * file.
  */
public class ParseException extends RuntimeException
{
    public ParseException()
    {
        super();
    }
    
    public ParseException( String msg )
    {
        super( msg );
    }
}


