/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.io;
     
/** 
  * The exception defined by this class is for errors while parsing beacons
  * from a text file.
  */
public class BeaconsParseException extends ParseException
{
    public BeaconsParseException()
    {
        super();
    }
    
    public BeaconsParseException( String msg )
    {
        super( msg );
    }
}


