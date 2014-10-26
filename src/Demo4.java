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
 *     An investigation experiment
 *     Multiple repeat experiments
 *     Multiple output files
 *     
 *     Scripts for processing metrics
 *     Device type flexibility of metric scripts
 *     Metric processing example
 */



public class Demo4
{
    public static double SIMULATION_DURATION = 60 * 60 *   1;
    public static double TIMESTEP_LENGTH = 0.1;
    
    public static int EXPERIMENT_REPEAT_NUM = 100;                         //!
    
    public static String INPUT_PATH = "./input/";
    public static String OUTPUT_PATH = "./output/";
    
    
    
    
    public static void main( String[] args ) throws Exception
    {
        String experimentName = "Experiment_Demo";
            
        String mapFname = "queens_map";
        String isFname = "queens_IS_3";
        String beacFname = "queens_beacs_1";
        int numMobjs = 3;
        
        
        runExperiment( experimentName, EXPERIMENT_REPEAT_NUM,
                mapFname,
                isFname,
                beacFname,
                numMobjs );
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
            sim.removeSimulationListener( conMon );
            stdCC.removeCommunicationListener( conMon );
            
            sim.removeSimulationListener( xmlMon );
            stdCC.removeCommunicationListener( xmlMon );
        }
    }
}