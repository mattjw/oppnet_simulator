/*   Matthew Williams (0515328)   */

import uk.ac.cf.cs.scm5mjw.mda.*;
import uk.ac.cf.cs.scm5mjw.mda.mobility.*;
import uk.ac.cf.cs.scm5mjw.mda.devices.*;
import uk.ac.cf.cs.scm5mjw.mda.io.*;

import java.util.Vector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;



/*
 * A demo to show:
 *     Visualisation of a complete investigation experiment
 *     What a visualisation looks like
 *     Queen's without flow control, (C/N junction)
 *     
 *     The effect of return biasing
 */



public class Demo3
{
    /* General, fixed parameters */
    private static String INPUT_PATH = "./input/";
    private static String OUTPUT_PATH = "./output/";
    private static Dimension FRAME_SIZE = new Dimension( 500, 500 );
    
    
    
    
    /* Simulation parameters */
    public static double SIMULATION_DURATION = 60 * 60 *   1;   // x hours
    public static double TIMESTEP_LENGTH = 0.1;
    
    public static int NUM_RAND_MO = 5;
    
    
    /* Visualisation options */
    private static boolean VIS_SHOW_COMM_RANGES = true;
    private static boolean VIS_SHOW_COMM_SESSIONS = true;
    private static boolean VIS_SHOW_MAP = true;
    
    private static double VIS_PAUSEFRCTION = 1.0 / 14.0;
            // 1 second of simulation time = x seconds of real-life time (or: 'a xth of a second of real-life time') (
            // Or: '1 second of simulation time will result in a pause of an xth of a second in real-life''
    private static double VIS_UPDATEWAITDURATION = 0.5;
    
    
    /* Console output options */
    private static boolean PRINT_TIMESTEPS = false;
    private static boolean PRINT_COMMUNICATIONS = false;
    
    
    
    
    public static void main( String[] args ) throws FileNotFoundException
    {
        /* Initialise file names for file input or output */
        // File output:
        String outputFilename = "output2.xml";
        File dataOutFile = new File( OUTPUT_PATH + outputFilename );
        
        
        // File input
        String mapFilename, infoSrcFilename, beacsFilename, mosFilename;
        mapFilename = INPUT_PATH + "queens_map.dat";
        beacsFilename = INPUT_PATH + "queens_beacs_1.dat";
        infoSrcFilename = INPUT_PATH + "queens_IS_3.dat";
        
        
        
        
        /* Set up a simulation -- map and device lists (and comm controller) */
        // Set up / input the map
        MobilityMap map = Parser.parseMap( mapFilename );
        System.out.println( "MAP: \n" + map + "\n" );
        
        
        // Set up the simulator
        Simulator sim = new Simulator( map );
        sim.setTimestepLength( TIMESTEP_LENGTH );
        
        
        // Set up / input the beacons
        Vector<Beacon> beacCollection = Parser.parseBeaconList( beacsFilename );
        sim.setBeacons( beacCollection );
        
        
        // Set up the mobile objects (from FILE)
        sim.generateRandomMobileObjects( NUM_RAND_MO );
        
        
        // Set up / input the information sources
        Vector<InformationSource> infoSrcCollection = Parser.parseInformationSourceList( infoSrcFilename );
        sim.setInformationSources( infoSrcCollection );
        
        
        // Set the CommunicationController
        StandardCommController stdCC = new StandardCommController();
        AbstractWirelessDevice.setCommunicationController( stdCC );
        
        
        
        /* Set up other stuff */
        // VISUALISATION
        // Create a visualiser for this simulator and register it
        Visualiser vis = new Visualiser( sim );
        
        vis.setUpdateWaitDuration( VIS_UPDATEWAITDURATION );
        vis.setPauseFraction( VIS_PAUSEFRCTION );
        
        vis.setShowCommunicationRanges( VIS_SHOW_COMM_RANGES );
        vis.setShowCommunicationSessions( VIS_SHOW_COMM_SESSIONS );
        vis.setShowMap( VIS_SHOW_MAP );
        
        sim.addSimulationListener( vis );
        
        
        // OUTPUT TO FILE
        // Create a XML monitor (outputs data to XML) and register it
        XMLMonitor xmlMon = new XMLMonitor( dataOutFile );
        sim.addSimulationListener( xmlMon );
        stdCC.addCommunicationListener( xmlMon );
        xmlMon.setExtendedXML( false );
        
        
        // OUTPUT TO CONSOLE
        // Create a print stream monitor (to output to console) and register it
        PrintStreamMonitor conMon = new PrintStreamMonitor( System.out );
        conMon.setOutputIterations( PRINT_TIMESTEPS );
        conMon.setOutputCommunications( PRINT_COMMUNICATIONS );
        
        sim.addSimulationListener( conMon );
        stdCC.addCommunicationListener( conMon );
        
        
        
        
        
        /* Set custom RETURN BIAS for mobile objects */                         //!
        double returnBias = 0.1;
        for( MobileObject mobj: sim.getMobileObjects() )
            mobj.setReturnBias( returnBias );
        
        
        
        
        
        /* GUI STUFF */
        // Set up a container/scroll pane for the visualiser
        SimulationViewer visScrollPane = new SimulationViewer( vis );
        
        // Set up the button to pause/unpause simulations
        JToolBar toolBar = new JToolBar();
        toolBar.setOrientation( JToolBar.VERTICAL );
        toolBar.setMargin( new Insets( 0, -5, 0, 0 ) );
        toolBar.setFloatable( false );
        
        toolBar.add( new ToggleSimulationPauseAction( sim ) );
        toolBar.add( new ZoomInVisualiserAction( vis ) );
        toolBar.add( new ZoomOutVisualiserAction( vis ) );
        toolBar.addSeparator();
        
        
        
        
        // Set up the whole frame
        JFrame f = new JFrame( "Simulation" );
        
        f.setLayout( new BorderLayout() );
        f.getContentPane().add( visScrollPane, BorderLayout.CENTER );
        f.getContentPane().add( toolBar, BorderLayout.EAST );
        
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        f.setVisible( true );
        f.pack();
        f.setSize( FRAME_SIZE );
        
        
        
        
        /* Run the simulation */
        // Output some info first
        System.out.println( "BEACONS: \n" + sim.getBeacons() + "\n" );
        System.out.println( "MOBILE OJBECTS: \n" + sim.getMobileObjects() + "\n" );
        System.out.println( "INFO SOURCES: \n" + sim.getInformationSources() + "\n" );
        
        // Run the simulation
        sim.run( SIMULATION_DURATION );
    }
}
