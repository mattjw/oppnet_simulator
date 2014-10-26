/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.io;
     
/** 
  * The exception defined by this class is for errors encountered when parsing
  * a map from a text file.
  */
public class MapParseException extends ParseException
{
    public MapParseException()
    {
        super();
    }
    
    public MapParseException( String msg )
    {
        super( msg );
    }
}


