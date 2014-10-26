/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.io;
     
import java.io.*;
import java.util.*;
import org.apache.xml.serialize.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
  * This class handles outputting XML to a file an a easy to use manner.
  * It essentially uses the Xerces package (http://xerces.apache.org/xerces-j/)
  * behind the scenes. The aim of this class is to allow the use of Xerces in an
  * easy and quick to use fashion. <br>
  * This class is designed SPECIFICALLY with output for the mobile communication
  * simulator in mind.
  */
public class SimpleXMLWriter
{
    public static final AttributesImpl ATT_TYPEDOUBLE = new AttributesImpl();
    public static final AttributesImpl ATT_TYPEINTEGER = new AttributesImpl();
    public static final AttributesImpl ATT_EMPTY = new AttributesImpl();
    
    public static final String DOUBLETYPE_STRING = "double";
    public static final String INTEGERTYPE_STRING = "integer";
    public static final String TYPE_STRING = "type";
    
    private Stack<String> tagStack;
    private File f;
    private ContentHandler hd;
    private FileOutputStream fos;
    
    
    
    
    /**
      * This constructor will set up an output to the given file and prepare
      * for XML to be written. <br>
      * As soon as this constructor is finished, XML is ready to be outputted.
      * (Note that this does NOT, however, write any tags to the document. 
      * startDocument() is NOT yet called.)
      */
    public SimpleXMLWriter( File inFile ) throws FileNotFoundException, IOException
    {
        /* Record variables and initialise stuff */
        f = inFile;
        tagStack = new Stack<String>();
        
        
        /* Set up for output */
        fos = new FileOutputStream( f );
        
        OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
        of.setIndent(1);
        of.setIndenting(true);
        
        XMLSerializer serializer = new XMLSerializer(fos,of);
        hd = serializer.asContentHandler();
    }
    
    
    
    
    /**
      * Indicates the beginning of the document.
      */
    public void startDocument() throws SAXException
    {
        hd.startDocument();
    }
    
    
    /**
      * Indicates the end of the document.
      */
    public void endDocument() throws SAXException
    {
        hd.endDocument();
    }
    
    
    /**
      * This method will add an element containing a value to the document. <br>
      * The element will also be given an attribute to indicate the data (value)
      * it encloses is of type double. <br>
      * <br>
      * Note that this will output a complete element (i.e. start AND end tags).
      */
    public void elemDouble( String elemName, double value ) throws SAXException
    {
        String strValue = Double.toString( value );
        
        hd.startElement( "", "", elemName, ATT_TYPEDOUBLE );
        hd.characters( strValue.toCharArray(), 0, strValue.length() );
        hd.endElement( "", "", elemName );
    }
    
    
    /**
      * This method will add an element containing a value to the document. <br>
      * The element will also be given an attribute to indicate the data (value)
      * it encloses is of type integer. <br>
      * <br>
      * Note that this will output a complete element (i.e. start AND end tags).
      */
    public void elemInt( String elemName, int value ) throws SAXException
    {
        String strValue = Integer.toString( value );
        
        hd.startElement( "", "", elemName, ATT_TYPEINTEGER );
        hd.characters( strValue.toCharArray(), 0, strValue.length() );
        hd.endElement( "", "", elemName );
    }
    
    
    /**
      * This method will add an element containing a value/string to the document.
      * The element will have no attributes. <br>
      * <br>
      * Note that this will output a complete element (i.e. start AND end tags).
      */
    public void elem( String elemName, String value ) throws SAXException
    {
        hd.startElement( "", "", elemName, ATT_EMPTY );
        hd.characters( value.toCharArray(), 0, value.length() );
        hd.endElement( "", "", elemName );
    }
    
    
    /**
      * This method will print the string to the current XML element in
      * the XML document. <br>
      * Note that this will not begin or end a new or existing XML element. Thus
      * users should be careful not to break the XML format (e.g. by printing
      * text directly after an XML end tag if that text is not put in its own
      * element).
      */
    public void print( String str ) throws SAXException
    {
        hd.characters( str.toCharArray(), 0, str.length() );
    }
    
    /**
      * This method will begin an element in the document (thus allowing nesting
      * of elements).
      * The element will have no attributes.
      */
    public void startElement( String elemName ) throws SAXException
    {
        hd.startElement( "", "", elemName, ATT_EMPTY );
        tagStack.push( elemName );
    }
    
    
    /**
      * This method will end the most recent element to be started in the document.
      * The element will have no attributes.
      */
    public void endElement() throws SAXException
    {
        if( tagStack.empty() )
            throw new NoUnclosedElementException( "Cannot end an element if there are no remaining unended elements" );
        
        String tag = tagStack.pop();
        hd.endElement( "", "", tag );
    }
    
    
    /**
      * This method will close the output stream and release and resources that
      * were associated with it.
      */
    public void close() throws IOException
    {
        fos.close();
    }
    
    
    
    /**
      * The static initialiser.
      * This simply sets up some of the public constants.
      */
    static
    {
        ATT_TYPEDOUBLE.addAttribute( "", "", TYPE_STRING, "", DOUBLETYPE_STRING );
        ATT_TYPEINTEGER.addAttribute( "", "", TYPE_STRING, "", INTEGERTYPE_STRING );
        ATT_EMPTY.clear();
    }
}


/** 
  * The exception defined by this class is for an error where a request is made
  * to end an element when there are no un-ended elements.
  */
class NoUnclosedElementException extends RuntimeException
{
    public NoUnclosedElementException()
    {
        super();
    }
    
    public NoUnclosedElementException( String msg )
    {
        super( msg );
    }
    
    
}
