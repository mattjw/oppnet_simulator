/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.io;

/** 
  * The exception defined by this class is for errors encountered when parsing
  * mobile objects from a text file.
  */
public class MobileObjectsParseException extends ParseException
{
    public MobileObjectsParseException()
    {
        super();
    }
    
    public MobileObjectsParseException( String msg )
    {
        super( msg );
    }
}


