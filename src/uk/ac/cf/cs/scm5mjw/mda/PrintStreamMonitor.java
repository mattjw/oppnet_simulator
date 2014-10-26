/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda;

import uk.ac.cf.cs.scm5mjw.mda.devices.*;
import uk.ac.cf.cs.scm5mjw.mda.io.*;

import java.io.*;
import java.text.*;

/**
  * This class handles obtaining data about a simulation (via events) and
  * outputting it to a PrintStream as text. In particular, this is aimed at 
  * outputting to the console. <br>
  *  <br>
  * For correct functionality, a PrintStreamMonitor must, at the least, be registered 
  * on a Simulator. Most importantly, two notifications from the observable Simulation
  * are required: <br>
  *  - The Simulation start notification, and <br>
  *  - The Simulation finish notification <br>
  * <br>
  * It is expected that any other notifications (e.g. communication notifications)
  * come between the simulation start and the simulation finish notifications.
  * The Simulator held in the SimulationEvent when the simulationStart is 
  * fired is used as the Simulator that is responsible for subsequent 
  * communication notifications.<br>
  * <br>
  * After one run is complete (from start to finish), any subsequent
  * notifications are ignored by this monitor. This means that this monitor
  * can be left registered on listeners and those listeners can still fire 
  * notifications (e.g. the CommunicationController may be used again
  * in subsequent simulations) without resulting in any problems. (Only the
  * first run is monitored -- subsequent notifications will be ignored
  * by this monitor.)
  */
public final class PrintStreamMonitor implements CommunicationListener, SimulationListener
{
    /* Constants */
    public static final NumberFormat ITERATION_NUMBER_FORMAT = new DecimalFormat( "0000" );
    
    
    /* Instance variables */
    private Simulator sim;
    private MonitorPhase phase;
    
    private PrintStream out;
    
    private boolean doOutputIterations;
    private boolean doOutputCommunications;
    
    private int numArtifactsTransferred;
    
    
    
    
    /**
      * Construct a default PrintStreamMonitor. This will output to the
      * console.
      * 
      * @see java.lang.System#out
      */
    public PrintStreamMonitor()
    {
        this( System.out );
    }
    
    
    /**
      * Construct a PrintStreamMonitor where text output will be directed to
      * the specified PrintStream. <br>
      *  <br>
      * By default, the following are enabled: <br>
      * - iteration/timestep monitoring <br>
      * - communication monitoring
      */
    public PrintStreamMonitor( PrintStream out )
    {
        phase = MonitorPhase.BEFORE_START;
        this.out = out;
        
        doOutputIterations = true;
        doOutputCommunications = true;
    }
    
    
    
    /* ACCESSORS / MUTATORS / OTHER PUBLIC METHODS */
    
    /**
      * A mutator for this PrintStreamMonitor's PrintStream. This is the stream 
      * to which the text is outputted.
      */
    public void setPrintStream( PrintStream stream )
    {
        out = stream;
    }
    
    
    /**
      * An accessor to get this PrintStreamMonitor's PrintStream.
      */
    public PrintStream getPrintStream()
    {
        return out;
    }
    
    
    /**
      * A mutator for whether notifications about the timestep/iteration changing
      * should be outputted.
      */
    public void setOutputIterations( boolean m )
    {
        doOutputIterations = m;
    }
    
    
    /**
      * An accessor for whether notifications about the timestep/iteration changing
      * should be outputted.
      */
    public boolean getOutputIterations()
    {
        return doOutputIterations;
    }
    
    
    /**
      * A mutator for whether notifications about any device communications
      * should be outputted.
      */
    public void setOutputCommunications( boolean m )
    {
        doOutputCommunications = m;
    }
    
    
    /**
      * An accessor for whether notifications about any device communications
      * should be outputted.
      */
    public boolean getOutputCommunications()
    {
        return doOutputCommunications;
    }
    
    
    
    
    /* METHODS RELATED TO ACTUAL DATA OUTPUT */
    
    public void simulationTimestepAdvanced( SimulationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( phase == MonitorPhase.BEFORE_START )
            throw new DataOutputException( "Received notification about a timestep advancement without having received a notification that the simulation had begun beforehand" );
        
        
        // Do not print out if iteration printing is not enabled 
        if( !doOutputIterations )
            return;
        
        
        int itNum =  sim.getIteration();
        out.println( formatIteration(itNum) + "\tSIMULATION ITERATION" );
    }
    
    
    public void communicationInitiated( CommunicationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( phase == MonitorPhase.BEFORE_START )
            throw new DataOutputException( "Received notification about a communication without having received a notification that the simulation had begun beforehand" );
        
        
        // Do not print out if communication printing is not enabled
        if( !doOutputCommunications )
            return;
        
        
        String strIteration = formatIteration( sim.getIteration() );
        String strDevices = formatDevices( evt.getDevice1(), evt.getDevice2() );
        
        out.println( strIteration + "\t" + strDevices + "\tInitiated\t\t\t(session STARTED)" );
    }
    
    
    public void discoverySucceeded( CommunicationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( phase == MonitorPhase.BEFORE_START )
            throw new DataOutputException( "Received notification about a communication without having received a notification that the simulation had begun beforehand" );
        
        
        // Do not print out if communication printing is not enabled
        if( !doOutputCommunications )
            return;
        
        
        String strIteration = formatIteration( sim.getIteration() );
        String strDevices = formatDevices( evt.getDevice1(), evt.getDevice2() );
        
        out.println( strIteration + "\t" + strDevices + "\tDiscovery succeeded" );
    }
    
    
    public void discoveryFailed( CommunicationEvent evt ) 
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( phase == MonitorPhase.BEFORE_START )
            throw new DataOutputException( "Received notification about a communication without having received a notification that the simulation had begun beforehand" );
        
        
        // Do not print out if communication printing is not enabled
        if( !doOutputCommunications )
            return;
        
        
        String strIteration = formatIteration( sim.getIteration() );
        String strDevices = formatDevices( evt.getDevice1(), evt.getDevice2() );
        
        out.println( strIteration + "\t" + strDevices + "\tDiscovery failed\t\t(session FIN.)" );
    }
    
    
    public void communicationAborted( CommunicationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( phase == MonitorPhase.BEFORE_START )
            throw new DataOutputException( "Received notification about a communication without having received a notification that the simulation had begun beforehand" );
        
        
        // Do not print out if communication printing is not enabled
        if( !doOutputCommunications )
            return;
        
        
        String strIteration = formatIteration( sim.getIteration() );
        String strDevices = formatDevices( evt.getDevice1(), evt.getDevice2() );
        
        out.println( strIteration + "\t" + strDevices + "\tAborted\t\t\t\t(session FIN.)" );
    }
    
    
    public void communicationTransmissionFailed( CommunicationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( phase == MonitorPhase.BEFORE_START )
            throw new DataOutputException( "Received notification about a communication without having received a notification that the simulation had begun beforehand" );
        
        
        // Do not print out if communication printing is not enabled
        if( !doOutputCommunications )
            return;
        
        
        String strIteration = formatIteration( sim.getIteration() );
        String strDevices = formatDevices( evt.getDevice1(), evt.getDevice2() );
        
        out.println( strIteration + "\t" + strDevices + "\tTransmission failed\t\t(session FIN.)" );
    }
    
    
    public void communicationTransmissionSucceeded( CommunicationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( phase == MonitorPhase.BEFORE_START )
            throw new DataOutputException( "Received notification about a communication without having received a notification that the simulation had begun beforehand" );
        
        
        numArtifactsTransferred += evt.getArtifactsToDevice1().size() + evt.getArtifactsToDevice2().size();
        
        // Do not print out if communication printing is not enabled
        if( !doOutputCommunications )
            return;
        
        String strIteration = formatIteration( sim.getIteration() );
        String strDevices = formatDevices( evt.getDevice1(), evt.getDevice2() );
        
        out.println( strIteration + "\t" + strDevices + "\tTransmission succeeded\t\t(session FIN.)" );
    }
    
    
    /** 
      * In addition to outputting text about the notification, this method also
      * stores the Simulator in the SimulationEvent for use
      * in other listener/notification methods.
      */
    public void simulationStarted( SimulationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( phase == MonitorPhase.RUNNING )
            throw new DataOutputException( "Received notification that another simulation has started while one is currently running" );
        
        
        // Handle simulation started...
        phase = MonitorPhase.RUNNING;
        sim = evt.getSimulator();
        
        numArtifactsTransferred = 0;
        
        // Output text...
        int itNum = sim.getIteration();
        out.println( formatIteration(itNum) + "\tSIMULATION STARTED" );
    }
    
    
    /** 
      * In addition to outputting text about the notification, this method also
      * unsets the locally held Simulator (as it should not be needed anymore). <br>
      * <br>
      * Text outputted about the simulation finishing includes: <br> 
      *  - a value for the total number of artifacts transferred during the simulation <br>
      *  - a list of each mobile object and beacon along with the artifacts they hold
      */
    public void simulationFinished( SimulationEvent evt )
    {
        // Bomb out of method if one run of the simulation has already been run and finished
        if( phase == MonitorPhase.FINISHED )
            return;
        
        
        if( !( phase == MonitorPhase.RUNNING ) )
            throw new DataOutputException( "Received notification a simulation has finished but simulation is not running" );
        
        assert this.sim == evt.getSimulator();
        
        
        // Output text...
        int itNum = sim.getIteration();
        out.println( formatIteration(itNum) + "\tSIMULATION FINISHED" );
        out.println( "Summary:" );
        out.println( "\tTotal number of artifacts transferred:\t" + numArtifactsTransferred ); 
        out.println( "List of mobile objects and beacons and their artifacts:" );
        
        for( AbstractWirelessDevice dev : sim.getBeacons() )
        {
            out.println( "\t" + dev.getDeviceTypeIdentifier() + " " + dev.getDeviceID() + ":" );
            for( Artifact art : dev.getArtifactContainer() )
                out.println( "\t\t" + art );
        }
        
        for( AbstractWirelessDevice dev : sim.getMobileObjects() )
        {
            out.println( "\t" + dev.getDeviceTypeIdentifier() + " " + dev.getDeviceID() + ":" );
            for( Artifact art : dev.getArtifactContainer() )
                out.println( "\t\t" + art );
        }
        
        
        // Handle simulation finished...
        phase = MonitorPhase.FINISHED;
        sim = null;
    }
    
    
    
    
    
    /* (REUSABLE) HELPER METHODS FOR OUTPUTTING DATA */
    
    /**
      * This helper method formats the given iteration number and returns
      * it as a string.
      */
    private static String formatIteration( int iterationNumber )
    {
        return "[" + ITERATION_NUMBER_FORMAT.format(iterationNumber) + "]";
    }
    
    
    /**
      * This helper method will get the device IDs of the two given devices and formats
      * them into a string. The method will ensure that the lowest device ID of 
      * the two devices is first.
      */
    private static String formatDevices( AbstractWirelessDevice dev1, AbstractWirelessDevice dev2 )
    {
        int deviceID1 = Math.min( dev1.getDeviceID(), dev2.getDeviceID() );
        int deviceID2 = Math.max( dev1.getDeviceID(), dev2.getDeviceID() );
        
        return "<" + deviceID1 + "," + deviceID2 + ">";
    }
}