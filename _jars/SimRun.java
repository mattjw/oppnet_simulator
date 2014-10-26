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
 * This source code handles actually using the application to set up and run
 * simulations.
 */


public class SimRun
{
    public static double SIMULATION_DURATION = 60 * 60 *   1;
    public static double TIMESTEP_LENGTH = 0.1;
    
    public static int EXPERIMENT_REPEAT_NUM = 100;
    
    public static String INPUT_PATH = "./input/";
    public static String OUTPUT_PATH = "./output/";
    
    
    public static void main( String[] args ) throws FileNotFoundException
    {
        /* 
         * Run some experiments...
         */
        String mapFname = "queens_map";
        
        if( false )
        {
            runExperiment( "Experiment_A", EXPERIMENT_REPEAT_NUM,
                    mapFname,
                    "queens_IS_1",
                    "queens_beacs_0",
                    3 );
        }
        
        if( false )
        {
            runExperiment( "Experiment_B", EXPERIMENT_REPEAT_NUM,
                    mapFname,
                    "queens_IS_1",
                    "queens_beacs_3",
                    3 );
        }
         
        
        if( false )
        {
            runExperiment( "Exp-B1", EXPERIMENT_REPEAT_NUM,
                    mapFname,
                    "queens_IS_3",
                    "queens_beacs_1",
                    4 );
            
            runExperiment( "Exp-B2", EXPERIMENT_REPEAT_NUM,
                    mapFname,
                    "queens_IS_3",
                    "queens_beacs_1",
                    8 );
            
            runExperiment( "Exp-B3", EXPERIMENT_REPEAT_NUM,
                    mapFname,
                    "queens_IS_3",
                    "queens_beacs_1",
                    12 );
        }
        
        /*
         * Run a demonstration simulation...
         */
        if( true )
        {
            demoSimulator();
        }
    }
    
    
    
    
    /*
     * This is a method which will set up and run experiments.
     * It allows the same experiment to be run repeatedly. The primary output
     * for the experiments is to XML file (with CSV formatted transfers).
     * For repeat experiments, a number (starting from 1) is appended to each
     * file name.
     * 
     * There is also some minimal output to the console. This just indicates
     * the start and finish of each simulation run.
     * 
     * There is no visualisation (including visualisation would slow down the
     * experiments).
     * 
     * This method uses the following external (static) parameters:
     * * TIMESTEP_LENGTH
     * * SIMULATION_DURATION
     */
    public static void runExperiment( String expName, int repeat, String mapPrefix, String isPrefix, String beacPrefix, int numMobjs ) throws FileNotFoundException
    {
        MobilityMap map = Parser.parseMap( INPUT_PATH + mapPrefix + ".dat" );
        
        StandardCommController stdCC = new StandardCommController();
        AbstractWirelessDevice.setCommunicationController( stdCC );
        

        for( int i=0; i < repeat; i++ )
        {
            /* Set up components of simulation */
            AbstractWirelessDevice.resetNextDeviceID();
            
            Vector<InformationSource> infoSrcCollection = Parser.parseInformationSourceList( INPUT_PATH + isPrefix + ".dat" );
            Vector<Beacon> beacCollection = Parser.parseBeaconList( INPUT_PATH + beacPrefix + ".dat" );
            
            Simulator sim = new Simulator( map );
            sim.setTimestepLength( TIMESTEP_LENGTH );
            sim.setBeacons( beacCollection );
            sim.setInformationSources( infoSrcCollection );
            sim.generateRandomMobileObjects( numMobjs );
            
            /* Set up output */
            String outputFileStr = OUTPUT_PATH + expName + "_" + (i+1) + ".xml";
            File outputFile = new File( outputFileStr );
            
            // Create a XML monitor (outputs data to XML) and register it
            XMLMonitor xmlMon = new XMLMonitor( outputFile );
            sim.addSimulationListener( xmlMon ); 
            stdCC.addCommunicationListener( xmlMon );
            
            // Create a print stream monitor (to output to console) and register it
            PrintStreamMonitor conMon = new PrintStreamMonitor( System.out );
            conMon.setOutputIterations( false );
            conMon.setOutputCommunications( false );
            
            sim.addSimulationListener( conMon );
            stdCC.addCommunicationListener( conMon );
            
            
            /* Run the simulation */
            sim.run( SIMULATION_DURATION );
            
            
            /* ~ Unregister listeners (for efficiency) ~ */
            //sim.removeSimulationListener( conMon );
            //stdCC.removeCommunicationListener( conMon );
            
            //sim.removeSimulationListener( xmlMon );
            //stdCC.removeCommunicationListener( xmlMon );
        }
    }
    
    
    
    
    public static boolean ENABLE_VISUALISATION = true;
    public static boolean PRINT_TIMESTEPS = false;
    public static boolean PRINT_COMMUNICATIONS = false;
    
    public static boolean DO_RANDOM_MO_GENERATION = true;
    public static int NUM_RAND_MO = 5; //5... 
    
    public static boolean DO_RANDOM_IS_GENERATION = false;
    public static int NUM_RAND_IS = 2;
    
    public static double VIS_PAUSEFRCTION = 1.0 / 16.0;   // 1 second of real world time = 1/16th of simulation time 
    public static double VIS_UPDATEWAITDURATION = 1;
    
    public static Dimension FRAME_SIZE = new Dimension( 500, 500 );
    
    
    /*
     * This is a method which will set up and run a demonstration simulation.
     * This demonstration will include a visualisation, output to console,
     * and output to XML file.
     * 
     * The visualisation will include an option to pause and unpause the
     * simulation and options to zoom in and zoom out.
     */
    public static void demoSimulator() throws FileNotFoundException
    {
        /* Initialise file names for file input or output */
        // File output:
        String outputFilename = "output1.xml";
        File dataOutFile = new File( OUTPUT_PATH + outputFilename );
        
        
        // File input
        String mapFilename, infoSrcFilename, beacsFilename, mosFilename;
        
        //mapFilename = INPUT_PATH +"queens_map.dat";
        //mapFilename = INPUT_PATH +"queens_map_flow.dat";
        //mapFilename = INPUT_PATH +"queens_map_flow.dat";
        //mapFilename = INPUT_PATH +"queens_map_[3x].dat";
        mapFilename = INPUT_PATH + "queens_map.dat"; // !!
        
        mapFilename = INPUT_PATH +"queens_map_[3x].dat";
        
        //beacsFilename = INPUT_PATH + "queens_beacs_3.dat";
        beacsFilename = INPUT_PATH + "queens_beacs_1.dat";
        
        
        //infoSrcFilename = INPUT_PATH + "queens_IS_2.dat";
        infoSrcFilename = INPUT_PATH + "queens_IS_3.dat";
        
        mosFilename = INPUT_PATH + "mos1.dat";
        
        
        
        
        /* Set up a simulation */
        // Set up / input the map
        MobilityMap map = Parser.parseMap( mapFilename );
        System.out.println( "MAP: \n" + map + "\n" );
        
        
        // Set up the simulator
        Simulator sim = new Simulator( map );
        sim.setTimestepLength( TIMESTEP_LENGTH );
        
        
        // Set up / input the beacons
        Vector<Beacon> beacCollection = Parser.parseBeaconList( beacsFilename );
        sim.setBeacons( beacCollection );
        
        
        // Set up / input the mobile objects
        if( DO_RANDOM_MO_GENERATION )
        {
            sim.generateRandomMobileObjects( NUM_RAND_MO );
        }
        else
        {
            Vector<MobileObject> moCollection = Parser.parseMobileObjectList( mosFilename, sim.getMap() );
            sim.setMobileObjects( moCollection );
        }
        
        
        // Set up / input the information sources
        if( DO_RANDOM_IS_GENERATION )
        {
            sim.generateRandomInformationSources( NUM_RAND_IS );
        }
        else
        {
            Vector<InformationSource> infoSrcCollection = Parser.parseInformationSourceList( infoSrcFilename );
            sim.setInformationSources( infoSrcCollection );
        }
        
        
        System.out.println( "BEACONS: \n" + sim.getBeacons() + "\n" );
        System.out.println( "MOBILE OJBECTS: \n" + sim.getMobileObjects() + "\n" );
        System.out.println( "INFO SOURCES: \n" + sim.getInformationSources() + "\n" );
        
        
        // Set the CommunicationController
        StandardCommController stdCC = new StandardCommController();
        AbstractWirelessDevice.setCommunicationController( stdCC );
        
        
        
        
        /* Set up other stuff */
        // Create a visualiser for this simulator and register it
        Visualiser vis = new Visualiser( sim );
        
        vis.setUpdateWaitDuration( VIS_UPDATEWAITDURATION );
        vis.setPauseFraction( VIS_PAUSEFRCTION );
        
        vis.setShowCommunicationRanges( true );
        vis.setShowCommunicationSessions( true );
        vis.setShowMap( true );
        
        if( ENABLE_VISUALISATION )
            sim.addSimulationListener( vis );
        
        
        // Create a XML monitor (outputs data to XML) and register it
        XMLMonitor xmlMon = new XMLMonitor( dataOutFile );
        sim.addSimulationListener( xmlMon );
        stdCC.addCommunicationListener( xmlMon );
        xmlMon.setExtendedXML( false );
        
        
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
        toolBar.add( new ZoomInVisualiser( vis ) );
        toolBar.add( new ZoomOutVisualiser( vis ) );
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
        sim.run( SIMULATION_DURATION );
    }
    
}








/**
  * A custom action which will toggle a simulation between paused and unpaused.
  * This class will also handle changing the AbstractAction's name when the
  * simulation is toggled. e.g. this will cause a JButton's text to change
  * as appropriate.
  */
class ToggleSimulationPauseAction extends AbstractAction
{
    private static final String DO_PAUSE_TEXT = "Pause Simulation";
    private static final String DO_UNPAUSE_TEXT = "Unpause Simulation";
    private static final ImageIcon DO_PAUSE_ICON = new ImageIcon( "./resc/pause.png" );
    private static final ImageIcon DO_UNPAUSE_ICON = new ImageIcon( "./resc/play.png" );
    
    private static final String ACTION_TOOLTIP = "Pauses or unpauses the simulation";
    
    private Simulator sim;
    
    public ToggleSimulationPauseAction( Simulator sim )
    {
        super();
        this.sim = sim;
        
        handleToggleText();
        putValue( AbstractAction.SHORT_DESCRIPTION, ACTION_TOOLTIP ); // Tooltip
    }
    
    public void actionPerformed( ActionEvent evt )
    {
        if( sim.isPaused() )
            sim.unpause();
        else
            sim.pause();
        
        handleToggleText();
    }
    
    private void handleToggleText()
    {
        if( sim.isPaused() )
        {
            putValue( AbstractAction.NAME, DO_UNPAUSE_TEXT );
            putValue( AbstractAction.SMALL_ICON, DO_UNPAUSE_ICON );
        }
        else
        {
            putValue( AbstractAction.NAME, DO_PAUSE_TEXT );
            putValue( AbstractAction.SMALL_ICON, DO_PAUSE_ICON );
        }
    }
}


/**
  * A custom action to invoke a 'zoom in' on a Visualiser.
  */
class ZoomInVisualiser extends AbstractAction
{
    private static final double FACTOR = 3.0/4.0;
    
    private static final String ACTION_TEXT = "Zoom In";
    private static final String ACTION_TOOLTIP = "Zoom In";
    private static final ImageIcon ACTION_ICON = new ImageIcon( "./resc/zoomin.png" );
    
    private Visualiser vis;
    
    public ZoomInVisualiser( Visualiser vis )
    {
        super();
        this.vis = vis;
        
        putValue( AbstractAction.NAME, ACTION_TEXT ); // Text
        putValue( AbstractAction.SHORT_DESCRIPTION, ACTION_TOOLTIP ); // Tooltip
        putValue( AbstractAction.SMALL_ICON, ACTION_ICON );           // Icon
    }
    
    public void actionPerformed( ActionEvent evt )
    {
        double oldFactor = vis.getZoomFactor();
        vis.setZoomFactor( oldFactor * (1.0/FACTOR) );
    }
}


/**
  * A custom action to invoke a 'zoom in' on a Visualiser.
  */
class ZoomOutVisualiser extends AbstractAction
{
    private static final double FACTOR = 3.0/4.0;
    
    private static final String ACTION_TEXT = "Zoom Out";
    private static final String ACTION_TOOLTIP = "Zoom Out";
    private static final ImageIcon ACTION_ICON = new ImageIcon( "./resc/zoomout.png" );
    
    private Visualiser vis;
    
    public ZoomOutVisualiser( Visualiser vis )
    {
        super();
        this.vis = vis;
        
        putValue( AbstractAction.NAME, ACTION_TEXT ); // Text
        putValue( AbstractAction.SHORT_DESCRIPTION, ACTION_TOOLTIP ); // Tooltip
        putValue( AbstractAction.SMALL_ICON, ACTION_ICON );           // Icon
    }
    
    public void actionPerformed( ActionEvent evt )
    {
        double oldFactor = vis.getZoomFactor();
        vis.setZoomFactor( oldFactor * FACTOR );
    }
}