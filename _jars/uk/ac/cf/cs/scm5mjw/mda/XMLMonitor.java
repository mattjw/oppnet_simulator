/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda;

import uk.ac.cf.cs.scm5mjw.mda.devices.*;
import uk.ac.cf.cs.scm5mjw.mda.io.*;

import org.xml.sax.SAXException;
import java.awt.geom.Point2D;
import java.util.*;
import java.io.*;
import java.text.*;

/**
  * This class handles obtaining data about a simulation (via events) and
  * outputting it to a XML file. <br>
  * <br>
  * For correct functionality, a XMLMonitor must, at the least, be registered on
  * a Simulator. Most importantly, two notifications from the observable Simulation
  * are required:<br>
  * <br>
  * 1. Simulation Start:<br>
  *  - Open an output stream<br>
  *  - Start the XML document for output<br>
  *  - Outputs information about the simulation ('metadata')<br>
  * <br>
  * 2. Simulation Finish:<br>
  *  - End the XML document<br>
  *  - Close the output stream<br>
  * <br>
  * Both of these are necessary for correct behaviour. If a finish notification
  * is not received then the output file may not have been completed. <br>
  * <br>
  * Also, Simulator held in the SimulationEvent when the simulationStart is 
  * fired is used as the Simulator that is responsible for subsequent 
  * communication notifications. <br>
  * <br>
  * After one run is complete (from start to finish), any subsequent
  * notifications are ignored by this monitor. This means that this monitor
  * can be left registered on listeners and those listeners can still fire 
  * notifications (e.g. the CommunicationController may be used again
  * in subsequent simulations) without resulting in any problems. (Only the
  * first run is monitored -- subsequent notifications will be ignored
  * by this monitor.)
  * <br>
  * <br>
  * The file written to is determined by the file specified by the constructor
  * or mutator method. On initiation, the output stream is established to this
  * file. After this (until the finish notification), changes to the file are
  * not legal. <br>
  * Note that this class is written so that it will not overwrite a file if
  * it exists already. Instead, a new file with a unique name (based on the
  * specified file) will be created and written to. <br>
  * <br>
  * <br>
  * The outputted data is in an XML format. However, there is the option to have
  * some data items formatted in CSV (comma-separated values) format, rather than
  * completely XML. In particular, this applies to the output of data during the
  * actual simulation run. Clearly this is where the bulk of the data out will be.
  * Formatting all of the data about each event during the simulation run in XML
  * may result in very large files. Furthermore, they may take a considerable
  * amount of time to process. <br>
  * The CSV format is much more compact and quicker to process. <br> 
  * <br>
  * To have the XMLMonitor output data in a complete and exhaustive XML format,
  * enable the extended XML option. <br>
  * In extended XML mode, the data during the simulation run will also be
  * formatted in XML. <br>
  * In CSV mode (extended XML disabled), the data during the simulation run
  * will be formatted in a comma-separated values format. CSV stores data in
  * a tabular manner. Each column is separated by a comma. Each row has its own
  * line. The whole CSV list is preceded by a line of headers for each column
  * (also separated by commas). <br>
  * The whole CSV list is held inside an XML element, hence the resulting
  * document is still a valid XML document. <br>
  * Note that this <i>only</i>this means that the simulation run data is
  * in the CSV format. Other data (such as metadata) will be XML. <br>
  * <br>
  * Also note that since the CSV format is less flexible than the XML format
  * (because with CSV the data must be tabular), not all the data items will be
  * available in non-extended XML than there is in extended XML. <br>  
  * <br>
  * 
  * @see #CSV_HEADER
  */
public final class XMLMonitor implements CommunicationListener, SimulationListener
{
    /* Constants - names for XML elements */
    public static final String ROOT_ELEMNAME = "Simulation";
    
    public static final String METADATA_ELEMNAME = "SimulationInfo";
    public static final String TIMESTEP_LENGTH_ELEMNAME = "TimestepLength";
    public static final String START_DATE_ELEMNAME = "StartDate";
    public static final String START_TIME_ELEMNAME = "StartTime";
    public static final String METADATA_DEVICELIST_ELEMNAME = "AllDevices";
    public static final String METADATA_DEVICE_ELEMNAME = "Device";
    public static final String METADATA_RESOURCE_TYPE_LIST_ELEMNAME = "ResourceTypeList";
    public static final String METADATA_RESOURCE_TYPE_ELEMNAME = "ResourceTypeID";
    
    public static final String RUN_DATA_ELEMNAME = "SimulationData";
    
    public static final String TRANSACTION_SUCCESS_ELEMNAME = "Transfer";
    
    public static final String FROM_DEVICE_ELEMNAME = "FromDevice";
    public static final String TO_DEVICE_ELEMNAME = "ToDevice";
    
    public static final String DEVICE_TYPE_ELEMNAME = "DeviceType";
    public static final String DEVICEID_ELEMNAME = "DeviceID";
    
    public static final String ARTIFACT_LIST_ELEMNAME = "ArtifactList";
    
    public static final String ARTIFACT_ELEMNAME = "Artifact";
    public static final String ARTIFACT_RESOURCEID_ELEMNAME = "ResourceID";
    public static final String ARTIFACT_AGE_ELEMNAME = "Age";
    
    public static final String SIM_TIME_ELAPSED_ELEMNAME = "Time";
    
    public static final String COORD_ELEMNAME = "Coord";
    public static final String X_ELEMNAME = "x";
    public static final String Y_ELEMNAME = "y";
    
    /* Constants - for CSV */
    /** A header line which gives a name to each column in the comma-separated values list **/
    public static final String CSV_HEADER = "FromDevice_ID, FromDevice_Type, FromDevice_X, FromDevice_Y, ToDevice_ID, ToDevice_Type, ToDevice_X, ToDevice_Y, Artifact_Type, Artifact_Age, Time, Iteration";
    
    /* Instance variables */
    private boolean monitorSessionSuccesses;
    private boolean doOutputMetadata;
    private boolean extendedXML;
    
    private MonitorPhase phase;
    private Simulator sim;
    private File outF;
    
    private SimpleXMLWriter out;
    
    
    
    
    /**
      * Construct a XML monitor which will output to the given file. <br>
      * <br>
      * By default, the following are enabled:<br>
      *  - Output of metadata (information about the simulation)<br> 
      *  - Output of successful sessions<br>
      *  The following are disabled:<br>
      *  - output of extended XML (output of simulation data will be in comma-
      *  separated values format)<br>
      */
    public XMLMonitor( File outF )
    {
        this.outF = outF;
        phase = MonitorPhase.BEFORE_START;
        
        monitorSessionSuccesses = true;
        doOutputMetadata = true;
        extendedXML = false;
    }
    
    
    
    
    /* ACCESSORS / MUTATORS / OTHER PUBLIC METHODS */
    
    /**
      * A mutator to determine whether outputting of session successes during
      * the simulation should be carried out.
      */
    public void setMonitorSessionSuccesses( boolean b )
    {
        monitorSessionSuccesses = b;
    }
    
    
    /**
      * An accessor to find out whether outputting of session successes during
      * the simulation should be carried out.
      */
    public boolean getMonitorSessionSuccesses()
    {
        return monitorSessionSuccesses;
    }
    
    
    /**
      * A mutator to determine whether or not metadata (information about the
      * simulation before it begins) should be outputted.
      */
    public void setOutputMetadata( boolean b )
    {
        doOutputMetadata = b;
    }
    
    
    /**
      * An accessor to find out determine whether or not metadata (information 
      * about the simulation before it begins) should be outputted.
      */
    public boolean getOutputMetadata()
    {
        return doOutputMetadata;
    }
    
    
    /**
      * A mutator to determine whether or not extended XML will be output.
      * Extended XML is where all of the simulation data (during the run) will 
      * be formatted as XML elements. This will result in the entire output data
      * being in XML. <br>
      * When this is disabled, the simulation data is outputted in a comma-
      * separated values (CSV) format. Note that this will not affect whether 
      * other parts of the XML documents (e.g. metadata) are outputted as XML.
      * The CSV will be contained within an XML element in
      * the document. <br>
      * 
      * @see #CSV_HEADER
      */
    public void setExtendedXML( boolean b )
    {
        extendedXML = b;
    }
    
    
    /**
      * An accessor to find out determine whether or not metadata (information 
      * about the simulation before it begins) should be outputted.
      */
    public boolean getExtendedXML()
    {
        return extendedXML;
    }
    
    
    /**
      * A mutator for the output file. This is the file where the data will be
      * written to. <br>
      * A file cannot be changed while the simulation is running.
      */
    public void setOutputFile( File outF )
    {
        if(  !( phase == MonitorPhase.BEFORE_START )  )
            throw new DataOutputException( "The output file can only be changed before the simulation has begun" );
        
        
        this.outF = outF;
    }
    
    
    /**
      * An accessor for the output file. This is the file where the data will be
      * written to. <br>
      * Note that this is the ORIGINAL output file (not the renamed one). If
      * a file is found to exist already, then output will go to a renamed file
      * (so that the original is not overwritten). 
      */
    public File getOutputFile()
    {
        return outF;
    }
    
    
    /* METHODS RELATED TO ACTUAL DATA OUTPUT */
    
    public void simulationTimestepAdvanced( SimulationEvent evt ) {}
    
    public void communicationInitiated( CommunicationEvent evt ) {}
    public void communicationAborted( CommunicationEvent evt ) {}
    public void communicationTransmissionFailed( CommunicationEvent evt ) {}
    public void discoverySucceeded( CommunicationEvent evt ) {}
    public void discoveryFailed( CommunicationEvent evt ) {}
    
    
    /**
      * This method will handle the notification of beginning simulation. <br> 
      * <br>
      * Note that this will store the Simulator in the SimulationEvent for use
      * in other listener/notification methods. <br>
      * <br>
      * This method will catch any exceptions that may occur due to the
      * file setup. In the result of any of these exceptions being encountered,
      * this method will print out the stack trace and manually exit the program.
      */
    public void simulationStarted( SimulationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( phase == MonitorPhase.RUNNING )
                throw new DataOutputException( "Received notification that another simulation has started while one is currently running" );
        
        
        try
        {           
            initiate();
            sim = evt.getSimulator();
            
            if( doOutputMetadata )
                outputMetadata();
            
            out.startElement( RUN_DATA_ELEMNAME );
            if( !extendedXML )
            {
                // Output the CSV header if the extended XML is disabled
                out.print( CSV_HEADER );
                out.print( "\n" );
            }
        }
        catch( SAXException ex )
        {
            ex.printStackTrace();
            System.exit(0);
        }
        catch( IOException ex )
        {
            ex.printStackTrace();
            System.exit(0);
        }
        catch( DataOutputException ex )
        {
            ex.printStackTrace();
            System.exit(0);
        }
    }
    
    
    /**
      * This method will handle the notification that a simulation has finished. <br>
      * <br>
      * Note that it is here that the locally held Simulator is unset (as it should
      * not be needed anymore). <br>
      * <br>
      * This method will catch any exceptions that may occur due to the
      * file setup. In the result of any of these exceptions being encountered,
      * this method will print out the stack trace and manually exit the program.
      */
    public void simulationFinished( SimulationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( !( phase == MonitorPhase.RUNNING ) )
                throw new DataOutputException( "Received notification a simulation has finished but simulation is not running" );
        
        assert this.sim == evt.getSimulator();
        
        
        try
        {
            out.endElement();   // End the data being output from the simulation
            
            finish();
            sim = null;
        }
        catch( SAXException ex )
        {
            ex.printStackTrace();
            System.exit(0);
        }
        catch( IOException ex )
        {
            ex.printStackTrace();
            System.exit(0);
        }
    }
    
    
    /**
      * Handles the notification that a successful (that is, artifacts were
      * successfully transferred when the communication completed) communication 
      * has occurred.
      */
    public void communicationTransmissionSucceeded( CommunicationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( phase == MonitorPhase.BEFORE_START )
            throw new DataOutputException( "Received notification about a communication without having received a notification that the simulation had begun beforehand" );
        
        
        if( monitorSessionSuccesses )
        {
            // We need to extract the data into individual artifact transfers
            double time = sim.getTimeElapsed();
            int iteration = sim.getIteration();
            
            try
            {
                if( extendedXML )
                {
                    outputTransaction( evt.getArtifactsToDevice1(), evt.getDevice2(), evt.getDevice1(), time );
                    outputTransaction( evt.getArtifactsToDevice2(), evt.getDevice1(), evt.getDevice2(), time );
                }
                
                if( !extendedXML )
                {
                    outputTransactionCSV( evt.getArtifactsToDevice1(), evt.getDevice2(), evt.getDevice1(), time, iteration );
                    outputTransactionCSV( evt.getArtifactsToDevice2(), evt.getDevice1(), evt.getDevice2(), time, iteration );
                }
                
            }
            catch( SAXException ex )
            {
                ex.printStackTrace();
                System.exit(0);
            }
        }
    }
    
    
    
    
    /* (REUSABLE) HELPER METHODS FOR OUTPUTTING DATA - COMMA-SEPARATED */
    /**
      * A method to handle outputting data about EACH artifact in a transaction
      * to the document in a comma-separated values format. Data about each 
      * artifact transfer will have its own line.
      * 
      * @see #CSV_HEADER
      */
    private void outputTransactionCSV( List<Artifact> artList, AbstractWirelessDevice from, AbstractWirelessDevice to, double time, int iteration ) throws SAXException
    {
        assert phase == MonitorPhase.RUNNING;
        
        for( Artifact art : artList )
            outputArtifactTransferCSV( art, from, to, time, iteration );
    }
    
    
    /**
      * This method handles outputting one line of data about an artifact
      * transfer in a comma-separated values format.
      */
    private void outputArtifactTransferCSV( Artifact art, AbstractWirelessDevice from, AbstractWirelessDevice to, double time, int iteration ) throws SAXException
    {
        StringBuffer buff = new StringBuffer();
        
        // Device 'from':
        buff.append( formatDeviceCSV(from) );
        buff.append( ",\t" );
        
        // Device 'to':
        buff.append( formatDeviceCSV(to) );
        buff.append( ",\t" );
        
        // Artifact / transfer info:
        buff.append( art.getResourceID() );
        buff.append( ",\t" );
        buff.append( art.getAge() );
        buff.append( ",\t" );
        buff.append( time );
        buff.append( ",\t" );
        buff.append( iteration );
        
        out.print( buff.toString() + "\n" );
    }
    
    
    /**
      * This method handles simply formatting a Point (coordinate) to comma
      * separated values. <br>
      * The resulting string is returned.
      */
    private static final String formatCoordCSV( Point2D.Double p )
    {
        return p.x + ",\t" + p.y ;
    }
    
    
    /**
      * This method handles simply formatting data about an AbstractWirelessDevice
      * into comma-separated values. <br>
      * The resulting string is returned.
      */
    private static final String formatDeviceCSV( AbstractWirelessDevice dev )
    {
        StringBuffer buff = new StringBuffer();
        
        buff.append( dev.getDeviceID() );
        buff.append( ",\t" );
        buff.append( dev.getDeviceTypeIdentifier() );
        buff.append( ",\t" );
        buff.append( formatCoordCSV( dev.getLocation() ) );
        
        return buff.toString();
    }
    
    
    /* (REUSABLE) HELPER METHODS FOR OUTPUTTING DATA - XML */
    
    /**
      * A method to handle outputting data about a transaction (in ONE direction)
      * to the XML document.
      */
    private void outputTransaction( List<Artifact> artList, AbstractWirelessDevice from, AbstractWirelessDevice to, double time ) throws SAXException
    {
        assert phase == MonitorPhase.RUNNING;
        
        
        out.startElement( TRANSACTION_SUCCESS_ELEMNAME );
        
        
        out.elemDouble( SIM_TIME_ELAPSED_ELEMNAME, time );
        outputAWD( FROM_DEVICE_ELEMNAME, from );
        outputAWD( TO_DEVICE_ELEMNAME, to );
        
        out.startElement( ARTIFACT_LIST_ELEMNAME );
        for( Artifact art : artList )
            outputArtifact( art );
        out.endElement();
        
        
        out.endElement();
    }
    
    
    /**
      * A helper method which will output an element containing the data items
      * attached to an artifact.
      */
    private void outputArtifact( Artifact art ) throws SAXException
    {
        assert phase == MonitorPhase.RUNNING;
        
        
        out.startElement( ARTIFACT_ELEMNAME );
        
        out.elemInt( ARTIFACT_RESOURCEID_ELEMNAME, art.getResourceID() );
        out.elemDouble( ARTIFACT_AGE_ELEMNAME, art.getAge() );
        
        out.endElement();
    }
    
    
    /**
      * A method to handle outputting details about the simulation ('metadata').
      * This includes various parameters for the simulation, including:<br>
      *  - The timestep length<br>
      *  - A list of all the devices in the simulation<br>
      *  - The date and time (in the real world) the simulation began executing
      */
    private void outputMetadata() throws SAXException
    {
        assert phase == MonitorPhase.RUNNING;
        
        
        out.startElement( METADATA_ELEMNAME );
        
        
        /* Format and output the date and time */
        Date now = new Date();
        String nowDate = DateFormat.getDateInstance().format( now );
        String nowTime = DateFormat.getTimeInstance().format( now );
        
        out.elem( START_DATE_ELEMNAME, nowDate );
        out.elem( START_TIME_ELEMNAME, nowTime );
        
        
        /* Output some info on the simulation */
        out.elemDouble( TIMESTEP_LENGTH_ELEMNAME, sim.getTimestepLength() );
        
        
        /* Output all the devices */
        out.startElement( METADATA_DEVICELIST_ELEMNAME );
        
        for( AbstractWirelessDevice dev : sim.getInformationSources() )
            outputAWD( METADATA_DEVICE_ELEMNAME, dev );
        
        for( AbstractWirelessDevice dev : sim.getBeacons() )
            outputAWD( METADATA_DEVICE_ELEMNAME, dev );
        
        for( AbstractWirelessDevice dev : sim.getMobileObjects() )
            outputAWD( METADATA_DEVICE_ELEMNAME, dev );
        
        out.endElement();
        
        
        /* Output a list of all the resource types in this simulation */
        out.startElement( METADATA_RESOURCE_TYPE_LIST_ELEMNAME );
        
        for( InformationSource is : sim.getInformationSources() )
            out.elemInt( METADATA_RESOURCE_TYPE_ELEMNAME, is.getResourceID() );
        
        out.endElement();
        
        
        out.endElement();
    }
    
    
    /**
      * A helper method which will output a coordinate element (containing 
      * the x and y values) corresponding to the inputted double.
      */
    private void outputCoord( Point2D.Double p ) throws SAXException
    {
        assert phase == MonitorPhase.RUNNING;
        
        
        out.startElement( COORD_ELEMNAME );
        
        out.elemDouble( X_ELEMNAME, p.x );
        out.elemDouble( Y_ELEMNAME, p.y );
        
        out.endElement();
    }
    
    
    /**
      * A helper method which will output an element containing various details
      * about an abstract wireless device.
      */
    private void outputAWD( String elemName, AbstractWirelessDevice dev ) throws SAXException
    {
        assert phase == MonitorPhase.RUNNING;
        
        
        out.startElement( elemName );
        
        out.elemInt( DEVICEID_ELEMNAME, dev.getDeviceID() );
        out.elem( DEVICE_TYPE_ELEMNAME, dev.getDeviceTypeIdentifier() );
        outputCoord( dev.getLocation() );
        
        out.endElement();
    }
    
    
    /* METHODS TO HANDLE SIMULATION NOTIFICATIONS (initation and finishing)  */
    
    /**
      * This method will set up the output and XML document for data to be
      * added. <br>
      * In particular, it:<br>
      *  - Opens an output stream to the file currently being specified<br>
      *  - Starts the XML document<br>
      *  - Begins the root element of the XML document
      */
    private void initiate() throws SAXException, IOException, DataOutputException
    {
        assert phase == MonitorPhase.BEFORE_START;
        
        
        File newFile = outF;
        if( newFile.exists() )
        {
            if( outF.isDirectory() )
                throw new DataOutputException( "Output file is a directory" );
            else
            {
                // Generate a unique filename
                int num = 2;
                
                while( newFile.exists() )
                {
                    String fpath = outF.getCanonicalPath();
                    int indexLastDot = fpath.lastIndexOf( '.' );
                    
                    if( indexLastDot == -1 )
                    {
                        // Extension/dot does not exist, so just append the number
                        fpath = fpath + " (" + num + ")";
                        
                    }
                    else
                    {
                        // Need to place the number just before the dot
                        String preDot = fpath.substring( 0, indexLastDot );
                        String postDot = fpath.substring( indexLastDot+1 );
                        
                        fpath = preDot + " (" + num + ")." + postDot;
                    }
                    
                    newFile = new File( fpath );
                    num++;
                }
            }
        }
        
        out = new SimpleXMLWriter( newFile );
        out.startDocument();
        out.startElement( ROOT_ELEMNAME );
        
        phase = MonitorPhase.RUNNING;
    }
    
    
    /**
      * This method will finish up and finalise the output. Essentially, it does
      * the opposite of the tasks carried out in initiate.
      * In particular:<br>
      *  - Ending the root element of the XML document<br>
      *  - Ending the XML document<br>
      *  - Closing the output stream
      */
    private void finish() throws SAXException, IOException
    {
        assert phase == MonitorPhase.RUNNING;
        
        
        out.endElement();
        out.endDocument();
        out.close();
        out = null;
        
        phase = MonitorPhase.FINISHED;
    }
}
