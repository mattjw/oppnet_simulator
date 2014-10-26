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
 *     Map file format
 *     Parse error checking
 *     Pausable simulations
 *     View bounds
 *     
 *     Simulation duration
 *     Random mobile object placement
 *     
 *     Zoomable
 *     Draggable
 *     Centered about view bounds
 *     Icons for actions
 *     
 *     What visualisation looks like
 *     Real-time visualisation speed
 *     
 *     Flow control
 *     Mobile object movement
 *     
 *     Minimal console output 
 */



public class Demo1
{
    /* General, fixed parameters */
    public static String INPUT_PATH = "./input/";
    public static Dimension FRAME_SIZE = new Dimension( 500, 500 );
    
    
    
    
    /* Simulation parameters */
    public static double SIMULATION_DURATION = 60 * 60 *   1;   // x hours
    public static double TIMESTEP_LENGTH = 0.1;
    
    public static int NUM_RAND_MO = 30;
    
    
    /* Visualisation options */
    public static boolean VIS_SHOW_COMM_RANGES = false;
    public static boolean VIS_SHOW_COMM_SESSIONS = false;
    public static boolean VIS_SHOW_MAP = true;
    
    public static double VIS_PAUSEFRCTION = 1.0 / 70.0;       //!
            // 1 second of simulation time = x seconds of real-life time (or: 'a xth of a second of real-life time') (
            // Or: '1 second of simulation time will result in a pause of an xth of a second in real-life''
    public static double VIS_UPDATEWAITDURATION = 0.5;
    
    
    /* Console output options */
    public static boolean PRINT_TIMESTEPS = false;
    public static boolean PRINT_COMMUNICATIONS = false;
    
    
    
    
    public static void main( String[] args ) throws FileNotFoundException
    {
        /* Initialise file names for file input or output */
        // File input
        String mapFilename, infoSrcFilename, beacsFilename, mosFilename;
        mapFilename = INPUT_PATH + "queens_map_flow.dat";
        
        
        
        
        /* Set up a simulation -- map and device lists (and comm controller) */
        // Set up / input the map
        MobilityMap map = Parser.parseMap( mapFilename );
        System.out.println( "MAP: \n" + map + "\n" );
        
        
        // Set up the simulator
        Simulator sim = new Simulator( map );
        sim.setTimestepLength( TIMESTEP_LENGTH );
        
        
        // Set up the mobile objects
        sim.generateRandomMobileObjects( NUM_RAND_MO );
        
        
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
        
        
        
        
        // OUTPUT TO CONSOLE
        // Create a print stream monitor (to output to console) and register it
        PrintStreamMonitor conMon = new PrintStreamMonitor( System.out );
        conMon.setOutputIterations( PRINT_TIMESTEPS );
        conMon.setOutputCommunications( PRINT_COMMUNICATIONS );
        
        sim.addSimulationListener( conMon );
        stdCC.addCommunicationListener( conMon );
        
        
        
        
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
