/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.io;

import uk.ac.cf.cs.scm5mjw.mda.mobility.*;
import uk.ac.cf.cs.scm5mjw.mda.devices.*;

import java.util.Vector;
import java.util.Scanner;
import java.io.*;
import java.util.regex.Pattern;
import java.awt.geom.Point2D;

/** 
  * This is a class which handles reading input from a file and converting it
  * into data usable by the simulator. It handles the lexical analysis aspects (converting
  * the input stream to tokens/symbols) and the parsing (using the tokens to
  * generate useful constructs) aspects.
  */
public final class Parser
{
    /* Constants */
    public static final String NEWLINE_SYMBOL = System.getProperty("line.separator");
    
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile( "\\G[ \t]+" );
    public static final Pattern WHITELINE_PATTERN = Pattern.compile( "\\G[ \t]*" + NEWLINE_SYMBOL );
    public static final Pattern INTEGER_PATTERN = Pattern.compile( "\\G[+-]?\\d+" );
    public static final Pattern REAL_PATTERN = Pattern.compile( "\\G[+-]?\\d+(\\.\\d+)?" );
        // \G will force matching to begin AFTER the last match
    
    public static final Pattern MATCH_ALL_DELIMITER = Pattern.compile( "\\z" );
    
    
    /* Instance variables */
    private Scanner feed;
    private int lineNum;
    
    
    
    
    /**
      * Construct a parser with the given file. The data from this file is the
      * input which will be processed into tokens.
      *
      * @throws FileNotFoundException if the file does not exist or does exist, but is not a file
      */
    public Parser( File inFile ) throws FileNotFoundException
    {
        if( inFile.isDirectory() )
            throw new FileNotFoundException( "Excepted a file but found a directory" );
        
        feed = new Scanner( inFile );
        lineNum = 1;
    }
    
    
    
    
    /** 
      * An accessor to get the current line number. Line numbering begins at 1.
      */
    public int getLineNumber()
    {
        return lineNum;
    }
    
    
    
    
    /** 
      * This method will skip as many 'whitelines' of immediate input as
      * possible and returns the number of lines skipped. When a line which has
      * non-whitespace characters on it is encountered, that line will NOT be
      * skipped and none of the input on that line will be consumed (even
      * any whitespace starting on that line). <br>
      * <br>
      * A 'whiteline' is a line containing only whitespace (or just a line on
      * its own). 
      */
    public int skipWhitelines()
    {
        int i = 0;
        
        boolean success;
        do
        {
            String str = feed.findWithinHorizon( WHITELINE_PATTERN, 0 );
            
            if( (str != null) && (str.length() > 0) )
            {
                success = true;
                ++i;
                ++lineNum;
            }
            else
                success = false;
        }
        while( success );
        
        return i;
    }
    
    
    /**
      * This method will skip as much whitespace in the immediate input and returns
      * true if whitespace was skipped. If no whitespace was skipped, this will
      * return false.
      */
    public boolean skipWhitespace()
    {
        String str = feed.findWithinHorizon( WHITESPACE_PATTERN, 0 );
        
        if( str == null )
            return false;
        else
            return true;
    }
    
    
    /**
      * This method will skip a single specified character. This will return true
      * if a character was successfully skipped and false otherwise. <br>
      * The method is written to handle the case that the char has some special
      * meaning in regular expressions. For example, ( has special meaning in 
      * a regular expression. If the character is a (, the method will handle this
      * and correctly attempt to skip a literal (.
      */
    public boolean skipCharacter( char c )
    {
        String literal = Pattern.quote( new Character(c).toString() );
        
        Pattern charOnce = Pattern.compile( "\\G" + literal + "{1}" );
        String str = feed.findWithinHorizon( charOnce, 0 );
        
        if( str == null )
            return false;
        else
            return true;
    }
    
    
    /**
      * This method will parse an integer (and return it) using forthcoming
      * input. Only the integer itself will be consumed. It will NOT ignore
      * whitespace or any other characters -- the integer MUST start in the
      * input immediately. It will not consume any characters that are not
      * part of the integer itself.
      *
      * @throws NumberFormatException if no integer can be found immediately in the input or the integer was not in the expected format of an integer
      */
    public int parseInt()
    {
        String str = feed.findWithinHorizon( INTEGER_PATTERN, 0 );
        
        if( str == null )
            throw new NumberFormatException( "[Line " + lineNum + "] Input did not match an integer" );
        else if( str.length() == 0 )
            throw new NumberFormatException( "Input did not match an integer" );
        
        return Integer.parseInt( str );
    }
    
    
    /**
      * This method will parse a real number as a double (and return it) using
      * forthcoming input. Only the number itself will be consumed. It will NOT 
      * ignore whitespace or any other characters -- the number MUST start in the
      * input immediately. It will not consume any characters that are not
      * part of the number itself.
      *
      * @throws NumberFormatException if no real number can be found immediately in the input or the number was not in the expected format of a real number
      */
    public double parseDouble()
    {
        String str = feed.findWithinHorizon( REAL_PATTERN, 0 );
        
        if( str == null )
            throw new NumberFormatException( "[Line " + lineNum + "] Input did not match a real number" );
        else if( str.length() == 0 )
            throw new NumberFormatException( "[Line " + lineNum + "] Input did not match a real number" );
        
        return Double.parseDouble( str );
    }
    
    
    /**
      * This will parse a point (x and y coordinates) using forthcoming input.  <br>
      * This will NOT ignore whitespace (or other characters) before or after
      * the point. Similarly, it will NOT consume characters that are not related
      * to the point itself. <br>
      * A point is composed of two coordinates (numbers) separated by a comma, 
      * surrounded by parenthesis. Any amount of whitespace is allowed at the
      * following positions: <br>
      *  - after the ( <br>
      *  - before the ) <br>
      *  - before or after the comma <br>
      * <br>
      * Examples are: <br>
      * <code>
      *   (3,4)          <br>
      *   (   3,4)       <br>
      *   (  3 ,  4)     <br>   
      *   ( 3, 4 )        <br>
      * </code>
      * <br>
      * This method will parse points whose coordinate values are real numbers as
      * doubles.
      */
    public Point2D.Double parseDoublePoint()
    {
        boolean skipped;
        
        // Opening parenthesis
        skipped = skipCharacter( '(' );
        if( !skipped )
            throw new ParseException( "[Line " + lineNum + "] Could not find an opening bracket" );
        
        skipWhitespace();
        
        // x coordinate
        double x = parseDouble();
        
        skipWhitespace();
        
        // Comma
        skipped = skipCharacter( ',' );
        if( !skipped )
            throw new ParseException( "[Line " + lineNum + "] Could not find a comma" );
        
        skipWhitespace();
        
        // y coordinate
        double y = parseDouble();
        
        skipWhitespace();
        
        // Closing parenthesis
        skipped = skipCharacter( ')' );
        if( !skipped )
            throw new ParseException( "[Line " + lineNum + "] Could not find a closing bracket" );
        
        return new Point2D.Double( x, y );
    }
    
    
    /**
      * This method will check for end of file by checking if any input is left.
      * This will not consume any input.
      */
    public boolean isEOF()
    {
        Pattern oldDelim = feed.delimiter();
        
        feed.useDelimiter( MATCH_ALL_DELIMITER );
        boolean moreInput = feed.hasNext();
        
        feed.useDelimiter( oldDelim );
        
        return !moreInput;
    }
    
    
    
    
    /* (STATIC) PARSING METHODS */
    
    
    /**
      * This method will interpret a representation of a MobilityMap from a text file,
      * building and returning a MobilityMap object from this representation. <br>
      * It is a high-level method and makes use of the lexical facilities offered
      * by Parser. <br>
      * <br>
      * We interpret the adjacency matrix as follows: <br>
      *     A non-zero in entry A[i][j] indicates that the ith node is connected
      *     to the jth node.       <br>
      *     (where i indicates the row number and j indicates the column number) <br>
      *      <br>
      * To clarify what 'connected to' means: <br>
      *     "node i is connected to node j" <br>
      *     Means that there is an edge going AWAY from node i, and going TOWARDS
      *     node j. Thus, FROM node i we can get to node j (but not necessarily
      *     the other way around) <br>
      * <br>
      * For example: <br>
      * <code>
      *      A    B    C       <br>
      * A    0    0    0       <br>
      * B    0.5  0    0.5     <br>
      * C    0    0    0       <br>
      * </code>
      * This matrix would make a graph with the following links:<br>
      * - Node B connects to node A<br>
      * - Node B connects to node C<br> 
      * <br>
      * I have also made the assumption that for the two-dimensional array, the
      * array-of-arrays is the array containing references to ROWS. For example,
      * given a two-dimensional array M, the array given by <br>
      *     <code> M[3] </code> <br>
      * has all the elements of the row at index 3 (i.e. the fourth row). Thus
      * we have: <br>
      *     <code> M[rowNum][colNum] </code> <br>
      * For example, in matrix: <br>
      * <code>
      *     a    b                 <br>
      *     c    d                 <br>
      * </code>
      * 'b' is given by M[0][1] <br>
      * <br>
      * Also, internally we index map nodes from 0. However, it is easier for users
      * to think of indexing from 1 (especially since the mathematical convention
      * is for the rows and columns of adjacency matrixes to index from 1).
      * As such, I adopt the convention that, to the USER, map nodes should index
      * from 1. <br>
      * This is is not directly relevant to this method (since there is no explicit
      * indexing or reference to nodes when parsing a map), but it is important
      * to bear in mind his convention in other situations. <br>
      * <br>
      * <br>
      * Note that the sum of weights going from a given node should equal 1. Thus
      * the sum of the values in each row should equal 1. <br>
      * <br>
      * <br>
      * If the adjacency matrix states that a node is connected to itself, this
      * method will accept this as a valid link and attempt to connect the node
      * to itself (as is permissible in graphs). However, in the case of this
      * system we do not allow a map node to be connected to itself, thus this
      * an error will be raised if an attempt to do so is made. <br>
      * <br>
      * <br>
      * The following requirements are enforced while parsing the map: <br>
      *     - The map must NOT have any dead ends, i.e. every node must have
      *       at least one link going away from it.
      *       (thus the sum of every row should be 1)
      */
    public static MobilityMap parseMap( File inFile ) throws FileNotFoundException
    {
        /* SET UP INPUT AND PARSING STUFF */
        Parser parser = new Parser( inFile );
        int linesSkipped;
        
        
        /* PREAMBLE */
        parser.skipWhitespace();
        int numNodes = parser.parseInt();
        
        // Set up storage for inputted map details
        Point2D.Double[] coords = new Point2D.Double[numNodes];
        double[][] matrix = new double[numNodes][numNodes];
        
        // Skip between preamble and coords list
        linesSkipped = parser.skipWhitelines();
        if( linesSkipped < 2 )
            throw new MapParseException( "[Line " + parser.getLineNumber() + "] Did not find a blank line between preamble and coordinates list" );
        
        
        /* COORDINATES LIST */
        for( int i=0; i < coords.length; i++ )
        {
            parser.skipWhitespace();
            coords[i] = parser.parseDoublePoint();
            parser.skipWhitespace();
            
            // Skip whitelines BETWEEN coord entries (not after)
            if( i < (coords.length-1) )
            {
                linesSkipped = parser.skipWhitelines();
                
                if( linesSkipped < 1 )
                    throw new MapParseException( "[Line " + parser.getLineNumber() + "] Line did not end after a coordinates entry" );
                else if( linesSkipped > 1 )
                    throw new MapParseException( "[Line " + parser.getLineNumber() + "] Coordinates list ended unexpectedly" );
            }
        }
        
        linesSkipped = parser.skipWhitelines();
        if( linesSkipped < 2 )
            throw new MapParseException( "[Line " + parser.getLineNumber() + "] Did not find a blank line between coordinates list and matrix" );
        
        
        /* MATRIX */
        // This loop will iterate over each row 
        for( int rowNum=0; rowNum < matrix.length; rowNum++ )
        {
            parser.skipWhitespace(); // Skip whitespace before first entry of each row
            
            // This loop will iterate over each column in the current row
            for( int colNum=0; colNum < matrix[rowNum].length; colNum++ )
            {
                double val = parser.parseDouble();
                if( (val < 0) || (val > 1) )
                    throw new MapParseException( "[Line " + parser.getLineNumber() + "] Matrix entries must be between 0 and 1 (inclusive)" );
                else
                    matrix[rowNum][colNum] = val;
                
                // Skip whitespace BETWEEN matrix entries in a given row
                if( colNum < (matrix[rowNum].length-1) )
                {
                    boolean skipped = parser.skipWhitespace();
                    if( !skipped )
                        throw new MapParseException( "[Line " + parser.getLineNumber() + "] Did not find whitespace between entries in a row" );
                }
            }
        
            // Skip whitelines BETWEEN matrix rows (not after)
            if( rowNum < (matrix.length-1) )
            {
                linesSkipped = parser.skipWhitelines();
                
                if( linesSkipped < 1 )
                    throw new MapParseException( "[Line " + parser.getLineNumber() + "] Line did not end after a matrix row" );
                else if( linesSkipped > 1 )
                    throw new MapParseException( "[Line " + parser.getLineNumber() + "] Matrix ended unexpectedly" );
            }
        }
        
        /* CHECK END OF FILE */
        parser.skipWhitelines(); // Allow blank lines at end of file
        parser.skipWhitespace(); // Allow whitespace on the last line
        if( !parser.isEOF() )
            throw new MapParseException( "Input did not end after map processing complete" );
        
        /* CHECK THE SUM OF PROBABILITIES/WEIGHTS FOR EACH NODE EQUALS 1 */
        // Check sums of rows (iterate over each row)
        for( int rowNum=0; rowNum < matrix.length; rowNum++ )
        {
            double rowSum = 0;
            
            // Iterate over each column in the current row
            for( int colNum=0; colNum < matrix[rowNum].length; colNum++ )
                rowSum = rowSum + matrix[rowNum][colNum];
            
            if( rowSum != 1 )
                throw new MapParseException( "The sum of the probabilities that node " + (rowNum+1) + " links to does not equal 1 (actual sum was " + rowSum + ")" );
        }
        
        /* CONSTRUCT A MOBILITYMAP OBJECT FROM THE DATA */
        MobilityMap m = new MobilityMap();
        MapNode[] nodes = new MapNode[numNodes];
        
        // Create MobilityMap MapNodes (without links yet) and add the nodes to the map
        for( int i=0; i < nodes.length; i++ )
        {
            nodes[i] = new MapNode( coords[i] );
            m.addNode( nodes[i] );
        }
        
        // Read the adjacency matrix and connect nodes as directed
        for( int rowNum=0; rowNum < matrix.length; rowNum++ )
        {
            for( int colNum=0; colNum < matrix[rowNum].length; colNum++ )
            {
                if( matrix[rowNum][colNum] != 0 )
                    nodes[rowNum].addLinkOneway( nodes[colNum], matrix[rowNum][colNum] );
            }
        }
        
        // Finally, return the map
        return m;
    }
    
    /**
      * This method overloads parseMap to allow specifying the file to be parsed
      * as a filename and/or path.
      */
    public static MobilityMap parseMap( String filename ) throws FileNotFoundException
    {
        File f = new File( filename );
        return parseMap( f );
    }
    
    
    /**
      * This method will interpret a representation of a list of MobileObjects from
      * a text file based on the given map. <br>
      *  <br>
      * At the start of the file the number of list entries must be specified
      * (the number of list entries is not necessarily the number of mobile 
      * objects!). <br>
      * <br>
      * Examples of possible list entries are:<br>
      * <code> node_index: number_of_mobileobjects; speed </code>   <br>
      * (a number of mobile objects at the given node with a given speed) <br>
      * <code> node_index: number_of_mobileobjects </code>          <br>
      * (a number of mobile objects at the given node with default speed) <br>
      *  <br>
      *  <br>
      * Note that here we assume map nodes index from 1, not 0. 
      * (Internally we index map nodes from 0, as in MobilityMap) <br>
      * <br>
      * Multiple entries may exist for the same node index, thus allowing multiple
      * mobile objects at the same map node to have different speeds. <br>
      * The destination of mobile objects will be chosen using the weighted random
      * method.
      */
    public static Vector<MobileObject> parseMobileObjectList( File inFile, MobilityMap map ) throws FileNotFoundException
    {
        /* SET UP INPUT AND PARSING STUFF */
        Parser parser = new Parser( inFile );
        boolean skipped;
        int linesSkipped;
        int numNodes = map.getNumberOfNodes();
        
        
        
        /* PREAMBLE */
        parser.skipWhitespace();
        int numEntries = parser.parseInt();
        
         // Set up storage for inputted MobileObjects
        Vector<MobileObject> mObjects = new Vector<MobileObject>();
        
        // Skip between preamble and list
        linesSkipped = parser.skipWhitelines();
        if( linesSkipped < 2 )
            throw new MobileObjectsParseException( "[Line " + parser.getLineNumber() + "] Did not find a blank line between preamble and list of mobile objects" );
        
        
        /* LIST OF MOBILE OBJECTS */
        for( int i=0; i < numEntries; i++ )
        {
            int nodeIndex;
            int numMobileObjects;
            boolean useDefaultSpeed;
            double speed = 0;

            /* PARSE THE VALUES FOR AN ENTRY */
            // Get node index
            parser.skipWhitespace();
            
            nodeIndex = parser.parseInt();

            if( nodeIndex > numNodes )
                throw new MobileObjectsParseException( "[Line " + parser.getLineNumber() + "] Node index " + nodeIndex + " is out of range (expected range is 1 <= index <= " + numNodes + ")" );
            
            if( nodeIndex < 1 )
                throw new MobileObjectsParseException( "[Line " + parser.getLineNumber() + "] Node index " + nodeIndex + " is out of range (expected range is 1 <= index <= " + numNodes + ")" );
            
            
            // Skip delimiter 
            parser.skipWhitespace();
            skipped = parser.skipCharacter( ':' );
            if( !skipped )
                throw new MobileObjectsParseException( "[Line " + parser.getLineNumber() + "] Did not find ':' after node index" );
            
            
            // Get number of mobile objects to be added
            parser.skipWhitespace();
            numMobileObjects = parser.parseInt();
            
            
            // Get speed for these mobile objects (or default if not given)
            parser.skipWhitespace();
            skipped = parser.skipCharacter( ';' );
            
            if( !skipped )     // (no delimiter, thus use default speed)
                useDefaultSpeed = true;
            else               // (delimiter found, thus use specified speed)
            {
                useDefaultSpeed = false;
                
                parser.skipWhitespace();
                speed = parser.parseDouble();
                parser.skipWhitespace();
            }
            
            
            
            
            /* TRANSLATE THE VALUES TO MOBILE OBJECTS */
            // Recall that to the user, nodes index from 1, but internally nodes
            // index from 0
            MapNode startNode = map.getNodeAt( nodeIndex-1 );
            
            for( int j=0; j < numMobileObjects; j++ )
            {
                if( useDefaultSpeed )
                {
                    MobileObject mo = new MobileObject( startNode );
                    mObjects.add( mo );
                }
                else
                {
                    MobileObject mo = new MobileObject( startNode );
                    mo.setMovementSpeed( speed );
                    mObjects.add( mo );
                }
            }
            
            
            // Skip whitelines BETWEEN entries (not after)
            if( i < (numEntries-1) )
            {
                linesSkipped = parser.skipWhitelines();
                
                if( linesSkipped < 1 )
                    throw new MobileObjectsParseException( "[Line " + parser.getLineNumber() + "] Line did not end after a mobile object entry" );
                else if( linesSkipped > 1 )
                    throw new MobileObjectsParseException( "[Line " + parser.getLineNumber() + "] List of mobile objects ended unexpectedly" );
            }
        }
        
        
        /* CHECK END OF FILE */
        parser.skipWhitelines(); // Allow blank lines at end of file
        parser.skipWhitespace(); // Allow whitespace on the last line
        if( !parser.isEOF() )
            throw new MobileObjectsParseException( "Input did not end after list processing complete" );
        
        
        // Finally, return the objects
        return mObjects;
    }
    
    /**
      * This method overloads parseMobileObjectList to allow specifying the file
      * to be parsed as a filename and/or path.
      */
    public static Vector<MobileObject> parseMobileObjectList( String filename, MobilityMap map ) throws FileNotFoundException
    {
        File f = new File( filename );
        return parseMobileObjectList( f, map );
    }
    
    
    /**
      * This method will interpret a representation of a list of Beacons from
      * a text file. <br>
      *  <br>
      * At the start of the file the number of beacons must be specified.
      * The rest of the list is just a list of coordinates where each beacon is
      * to be placed.
      */
    public static Vector<Beacon> parseBeaconList( File inFile ) throws FileNotFoundException
    {
        /* SET UP INPUT AND PARSING STUFF */
        Parser parser = new Parser( inFile );
        int linesSkipped;
        
        
        
        
        /* PREAMBLE */
        parser.skipWhitespace();
        int numBeacons = parser.parseInt();
        
         // Set up storage for inputted Beacons
        Vector<Beacon> beacons = new Vector<Beacon>(numBeacons);
        
        // Skip between preamble and list
        linesSkipped = parser.skipWhitelines();
        if( linesSkipped < 2 )
            throw new BeaconsParseException( "[Line " + parser.getLineNumber() + "] Did not find a blank line between preamble and list of beacons" );
        
        
        /* LIST OF BEACONS */
        for( int i=0; i < numBeacons; i++ )
        {
            parser.skipWhitespace();
            Point2D.Double p = parser.parseDoublePoint();
            Beacon b = new Beacon( p );
            beacons.add( b );
            parser.skipWhitespace();
            
            
            // Skip whitelines BETWEEN entries (not after)
            if( i < (numBeacons-1) )
            {
                linesSkipped = parser.skipWhitelines();
                
                if( linesSkipped < 1 )
                    throw new BeaconsParseException( "[Line " + parser.getLineNumber() + "] Line did not end after a beacon entry" );
                else if( linesSkipped > 1 )
                    throw new BeaconsParseException( "[Line " + parser.getLineNumber() + "] List of beacons ended unexpectedly" );
            }
        }
        
        
        /* CHECK END OF FILE */
        parser.skipWhitelines(); // Allow blank lines at end of file
        parser.skipWhitespace(); // Allow whitespace on the last line
        if( !parser.isEOF() )
            throw new BeaconsParseException( "Input did not end after list processing complete" );
        
        
        // Finally, return the beacons
        return beacons;
    }
    
    /**
      * This method overloads parseBeaconList to allow specifying the file
      * to be parsed as a filename and/or path.
      */
    public static Vector<Beacon> parseBeaconList( String filename ) throws FileNotFoundException
    {
        File f = new File( filename );
        return parseBeaconList( f );
    }
    
    
    /**
      * This method will interpret a representation of a list of Information Sources
      * from a text file. <br>
      *  <br>
      * At the start of the file the number of information sources must be specified.
      * Next (after a blankline delimiter) should be a list of information sources (one
      * per line). <br>
      * The format of an information source should be:  <br>
      *   <code> coord: resourceID </code> <br>
      * For example: <br>
      *   <code> (3,50): 1 </code> <br>
      * <br>
      * Any amount of whitespace is allowed before or after the colon.
      */
    public static Vector<InformationSource> parseInformationSourceList( File inFile ) throws FileNotFoundException
    {
        /* SET UP INPUT AND PARSING STUFF */
        Parser parser = new Parser( inFile );
        int linesSkipped;
        boolean skipped;
        
        
        
        /* PREAMBLE */
        parser.skipWhitespace();
        int numInfoSources = parser.parseInt();
        
         // Set up storage for inputted Information Sources
        Vector<InformationSource> infoSources = new Vector<InformationSource>(numInfoSources);
        
        // Skip between preamble and list
        linesSkipped = parser.skipWhitelines();
        if( linesSkipped < 2 )
            throw new InformationSourcesParseException( "[Line " + parser.getLineNumber() + "] Did not find a blank line between preamble and list of information sources" );
        
        
        /* LIST OF INFORMATION SOURCES */
        for( int i=0; i < numInfoSources; i++ )
        {
            // A particular information source entry:
            parser.skipWhitespace();
            Point2D.Double p = parser.parseDoublePoint();
            parser.skipWhitespace();
            skipped = parser.skipCharacter( ':' );
            if( !skipped )
                throw new InformationSourcesParseException( "[Line " + parser.getLineNumber() + "] Could not find a colon" );
            parser.skipWhitespace();
            int rescID = parser.parseInt();
            
            if( rescID < 1 )
                throw new InformationSourcesParseException( "[Line " + parser.getLineNumber() + "] Resource ID must be greater or equal to 1" );
            
            InformationSource is = new InformationSource( p, rescID );
            infoSources.add( is );
            parser.skipWhitespace();
            
            
            // Handle for multiple entries:
            // Skip whitelines BETWEEN entries (not after)
            if( i < (numInfoSources-1) )
            {
                linesSkipped = parser.skipWhitelines();
                
                if( linesSkipped < 1 )
                    throw new InformationSourcesParseException( "[Line " + parser.getLineNumber() + "] Line did not end after an information source entry" );
                else if( linesSkipped > 1 )
                    throw new InformationSourcesParseException( "[Line " + parser.getLineNumber() + "] List of information sources ended unexpectedly" );
            }
        }
        
        
        /* CHECK END OF FILE */
        parser.skipWhitelines(); // Allow blank lines at end of file
        parser.skipWhitespace(); // Allow whitespace on the last line
        if( !parser.isEOF() )
            throw new InformationSourcesParseException( "Input did not end after list processing complete" );
        
        
        // Finally, return the information sources
        return infoSources;
    }
    
    /**
      * This method overloads parseInformationSourceList to allow specifying the file
      * to be parsed as a filename and/or path.
      */
    public static Vector<InformationSource> parseInformationSourceList( String filename ) throws FileNotFoundException
    {
        File f = new File( filename );
        return parseInformationSourceList( f );
    }
}
