/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.io;

/** 
  * The exception defined by this class is for errors while parsing information
  * sources from a text file.
  */
public class InformationSourcesParseException extends ParseException
{
    public InformationSourcesParseException()
    {
        super();
    }
    
    public InformationSourcesParseException( String msg )
    {
        super( msg );
    }
}


