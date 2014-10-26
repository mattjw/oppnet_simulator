/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda;

import uk.ac.cf.cs.scm5mjw.mda.mobility.*;
import uk.ac.cf.cs.scm5mjw.mda.devices.*;

import java.awt.geom.Point2D;
import java.util.*;
     
/** 
  * This class is responsible for bringing together the various parts of the
  * simulator and executing a simulation. <br>
  * <br>
  * Note that this class is not designed to handle operations or features for
  * its constituent parts (such as its MobilityMap or Mobile Objects). Manipulating these
  * should be done externally from outside the simulator (the get and set methods
  * will aid in this). <br>
  * That said, a generateRandomMobileObjects method (and a few other similar generation
  * methods) has been included to allow the user of this class to easily generate 
  * a number of mobile objects and place them at random locations on the Simulator's 
  * map. Note that this will REPLACE the existing set of mobile objects.
  */
public final class Simulator
{
    /* Constants */
    public static final double DEFAULT_TIMESTEP_LENGTH = 0.1;
    
    
    /* Instance variables */
    // Execution parameters
    private double timestepLength;     // The length of each timestep (seconds)
    private double timeElapsed;
    private int iteration;
    private boolean pauseRequested;
    
    // Components that make up a simulation
    private MobilityMap map;
    private Vector<MobileObject> mobileObjs;
    private Vector<Beacon> beacons;
    private Vector<InformationSource> informationSources;
    private Vector<AbstractWirelessDevice> allDevices;
    
    // Other variables
    private Set<SimulationListener> simListeners;
    
    
    /* CONSTRUCTORS */
    
    /**
      * A basic constructor which uses default values for the absent parameters.
      */
    public Simulator( MobilityMap map )
    {
        this( map, DEFAULT_TIMESTEP_LENGTH );
    }
    
    
    /**
      * A constructor which also allows the length of timesteps (in seconds)
      * to be set. <br>
      * <br>
      * Components (other than the map) of the simulator will be initialised to
      * be empty. These can be modified using their accessors and mutators. These
      * components are:<br>
      *     - Mobile objects<br>
      *     - Beacons<br>
      *     - Information sources
      */
    public Simulator( MobilityMap map, double timestepLength )
    {
        setMap( map );
        setTimestepLength( timestepLength );
        
        timeElapsed = -1;
        iteration = -1;
        pauseRequested = false;
        
        mobileObjs = new Vector<MobileObject>();
        beacons = new Vector<Beacon>();
        informationSources = new Vector<InformationSource>();
        allDevices = new Vector<AbstractWirelessDevice>();
        
        simListeners = new HashSet<SimulationListener>();
    }
    
    
    
    
    /* ACCESSORS & MUTATORS (& OTHER GENERAL) */
    
    /**
      * Registers a SimulationListener to receive events when a communication
      * event occurs.
      */
    public void addSimulationListener( SimulationListener l )
    {
        simListeners.add( l );
    }
    
    
    /**
      * Unregisters a simulation listener.
      */
    public void removeSimulationListener( SimulationListener l )
    {
        simListeners.remove( l );
    }
    
    
    /**
      * An accessor for the MobilityMap being used by this simulator.
      */
    public MobilityMap getMap()
    {
        return map;
    }
    
    
    /**
      * An accessor for the timestep length (in seconds).
      */
    public double getTimestepLength()
    {
        return timestepLength;
    }
    
    
    /**
      * An accessor for this simulator's mobile objects (the devices that will
      * move around in the system).
      */
    public Vector<MobileObject> getMobileObjects()
    {
        return mobileObjs;
    }
    
    
    /**
      * An accessor for this simulator's beacons.
      */
    public Vector<Beacon> getBeacons()
    {
        return beacons;
    }
    
    
    /**
      * An accessor for this simulator's information sources.
      */
    public Vector<InformationSource> getInformationSources()
    {
        return informationSources;
    }
    
    
    /**
      * An accessor for this simulator's current iteration number.
      * -1 indicates that the simulation has not been started.
      */
    public int getIteration()
    {
        return iteration;
    }
    
    
    /**
      * An accessor for this simulator's current time.
      * -1 indicates that the simulation has not been started.
      */
    public double getTimeElapsed()
    {
        return timeElapsed;
    }
    
    
    /**
      * A mutator for the MobilityMap being used by this simulator. <br>
      * <br>
      * A map may not be empty or have dead ends. The sum of the probabilities
      * of all links from each node must be 1.
      */
    public void setMap( MobilityMap map )
    {
        if( map.isEmpty() )
            throw new UnsuitableMapException( "A simulation cannot have a map which does not have any nodes" );
        
        if( map.hasDeadEnds() )
            throw new UnsuitableMapException( "A simulation cannot have a map which has dead ends" );
        
        for( int i=0; i < map.getNumberOfNodes(); i++ )
        {
            MapNode node = map.getNodeAt( i );
            
            double sum = 0;
            for( int j=0; j < node.getNumberOfLinks(); j++ )
                sum += node.getLinkAt(j).getWeight();
            
            if( sum != 1 )
                throw new UnsuitableMapException( "A simulation cannot have a map where the sum of probabilities of links from a node is not 1" );
        }
        
        
        this.map = map;
    }
    
    
    /**
      * A mutator for the timestep length (in seconds).
      */
    public void setTimestepLength( double timestepLength )
    {
        if( timestepLength <= 0 )
            throw new IllegalArgumentException( "Timestep length must be greater than 0" );
        
        this.timestepLength = timestepLength;
    }
    
    
    /**
      * A mutator for this simulator's mobile objects (the devices that will
      * move around in the system).
      */
    public void setMobileObjects( Vector<MobileObject> inMobileObjs )
    {
        if( !map.isValidFor( inMobileObjs ) )
            throw new UnsuitableMobileObjectsException( "The set of mobile objects is not valid for this simulator's map" );
        
        // Remove the existing devices from the list of all devices and clean up
        allDevices.removeAll( mobileObjs );
        mobileObjs = null;
        
        // Add the new devices
        allDevices.addAll( inMobileObjs );
        mobileObjs = inMobileObjs;
        
        
        assert allDevices.containsAll( mobileObjs ) : "The allDevices collection does not contain all of the new mobile devices";
        assert allDevices.size() == ( informationSources.size() + beacons.size() + mobileObjs.size() ) : "The size of the allDevices collection is not the sum of the separate collections";
    }
    
    
    /**
      * A mutator for this simulator's beacons.
      */
    public void setBeacons( Vector<Beacon> inBeacons )
    {
        // Remove the existing devices from the list of all devices and clean up
        allDevices.removeAll( beacons );
        beacons = null;
        
        // Add the new devices
        allDevices.addAll( inBeacons );
        beacons = inBeacons;
        
        
        assert allDevices.containsAll( beacons ) : "The allDevices collection does not contain all of the new beacons";
        assert allDevices.size() == ( informationSources.size() + beacons.size() + mobileObjs.size() ) : "The size of the allDevices collection is not the sum of the separate collections";
    }
    
    
    /**
      * A mutator for this simulator's information sources.
      */
    public void setInformationSources( Vector<InformationSource> inInformationSources )
    {
        // Remove the existing devices from the list of all devices and clean up
        allDevices.removeAll( informationSources);
        informationSources= null;
        
        // Add the new devices
        allDevices.addAll( inInformationSources );
        informationSources = inInformationSources;
        
        
        assert allDevices.containsAll( informationSources ) : "The allDevices collection does not contain all of the information sources";
        assert allDevices.size() == ( informationSources.size() + beacons.size() + mobileObjs.size() ) : "The size of the allDevices collection is not the sum of the separate collections";
    }
    
    
    
    
    /* SIMULATOR FUNCTIONALITY */
    
    /**
      * This is a convenience method which generates the given number of mobile
      * objects placed on the map for this simulator. The start node for each
      * mobile object is chosen at random and its destination node is chosen at random
      * from the start node's set of links. <br>
      * <br>
      * Note that these mobile objects will REPLACE the existing set of mobile
      * objects.
      */
    public void generateRandomMobileObjects( int n )
    {
        if( n < 0 )
            throw new IllegalArgumentException( "Number of mobile objects to generate must be greater or equal to 0" );
        
        Vector<MobileObject> newMObjs = new Vector<MobileObject>( n );
        
        int totalNumNodes = map.getNumberOfNodes();
        
        for( int i=0; i < n; i++ )
        {
            MapNode startNode = map.getNodeAt( SimulatorTools.randInRange(0, totalNumNodes-1) );
            newMObjs.add( new MobileObject( startNode ) );
        }
        
        setMobileObjects( newMObjs ); // Must use existing mutator because it handles managing the allDevices list
    }
    
    
    /**
     * This is a convenience method which generates the given number of information
     * sources for this simulator's current map. <br>
     * <br>
     * Note that these information sources will REPLACE the existing set of 
     * information sources for this simulator. <br>
     * <br>
     * The potential locations for the Information Sources are the map nodes of
     * this simulator's current map. A maximum of one Information Source may
     * be generated for each map node. Note that this means that the number of
     * Information Sources to be generated should not be more than the number of
     * nodes in the simulator's current map (attempting to do this will result
     * in an exception). <br>
     * Also, each Information Source will have a unique resource ID. This is done
     * by simply giving the first Information Source the resource ID of 1, the
     * next 2, and so on.
     */
    public void generateRandomInformationSources( int n )
    {
        if( n < 0 )
            throw new IllegalArgumentException( "Number of information sources to generate must be greater or equal to 0" );
        
        int numNodes = map.getNumberOfNodes();
        
        if( n > numNodes )
            throw new IllegalArgumentException( "Number of information sources to generate must be less or equal to the number of nodes on the map" );
        
        
        // Generate a list of all possible map nodes (actually, node indexes)
        List<Integer> nodeList = new Vector<Integer>( numNodes );
        for( int i=0; i < numNodes; i++ )
            nodeList.add( new Integer(i) );
        
        // Randomly select map node numbers (and prune the list)
        // to use as a position for each new InformationSource 
        Vector<InformationSource> newISources = new Vector<InformationSource>( n );
        
        for( int i=1; i <= n; i++ )
        {
            int listIndex = SimulatorTools.randInRange( 0, nodeList.size()-1 );
            
            int mapNodeNum = nodeList.remove( listIndex ).intValue();
            Point2D.Double loc = map.getNodeAt( mapNodeNum ).getLocation();
            InformationSource newIS = new InformationSource( loc, i );
            
            newISources.add( newIS );
        }
        
        setInformationSources( newISources );  // Must use existing mutator because it handles managing the allDevices list
    }
    
    
    /**
      * This method executes the simulation for the given duration. (The duration
      * being the amount of simulation time)
      * 
      * @see SimulationListener#simulationTimestepAdvanced(SimulationEvent)
      */
    public void run( double timeToRun )
    {
        timeElapsed = 0;
        iteration = 0;
        pauseRequested = false;
        
        
        // Send notification to listeners (if any)
        if( !simListeners.isEmpty() )
        {
            SimulationEvent evt = new SimulationEvent( this, this.getTimeElapsed() );
            
            for( SimulationListener sl : simListeners )
                sl.simulationStarted( evt );
        }
        
        
        while( timeElapsed <= timeToRun )
        {
            // Advance each mobile object
            for( MobileObject mo: mobileObjs )
            {
                synchronized( mo ) //~ advanceMobility affects only the given device
                {
                    mo.advanceMobility( timestepLength );
                }
            }
            
            // Perform the communication activities
            performCommunication();
            
            // Increment timestep and iteration
            timeElapsed += timestepLength;
            iteration++;
            
            // Send notification of timestep advancement to listeners (if any)
            if( !simListeners.isEmpty() )
            {
                SimulationEvent evt = new SimulationEvent( this, this.getTimeElapsed() );
                
                for( SimulationListener sl : simListeners )
                    sl.simulationTimestepAdvanced( evt );
            }
            
            // Check if pause is requested
            while( pauseRequested )
            {
                try
                {
                    thread = Thread.currentThread();
                    Thread.sleep( 10000 );
                }
                catch( InterruptedException ex )
                {}
            }
        }
        
        
        // Send notification to listeners (if any)
        if( !simListeners.isEmpty() )
        {
            SimulationEvent evt = new SimulationEvent( this, this.getTimeElapsed() );
            
            for( SimulationListener sl : simListeners )
                sl.simulationFinished( evt );
        }     
    }
    
    
    
    
    private Thread thread = null;    // Tracking the thread to be
                                     // interrupted (for pause/unpause)
    
    /**
      * This method will pause the current run.
      * Multiple calls to this method will have no effect.
      *
      * @see #unpause
      */
    public void pause()
    {
        pauseRequested = true;
    }
    
    
    /**
      * This method will unpause the current run.
      * Multiple calls to this method will have no effect.
      *
      * @see #pause
      */
    public void unpause()
    {
        pauseRequested = false;
        
        if( thread != null )
        {
            thread.interrupt();
            thread = null;
        }
    }
    
    
    /**
      * An accessor to check if a pause request has been made.
      */
    public boolean isPaused()
    {
        return pauseRequested;
    }
    
    
    
    
    /* SIMULATION OF COMMUNICATION */
    
    /**
      * For the given device and collection of devices, this method returns
      * a collection of devices where each device in the collection: <br>
      *     1. can (mutually) communicate with the given device <br>
      *     2. is not communicating <br>
      * <br>
      * If the given device itself also exists in the collection of devices, it 
      * will be ignored. (existence is determined by checking object identifier 
      * equivalence)
      */
    private static Vector<AbstractWirelessDevice> getValidDevices( AbstractWirelessDevice dev1, Vector<? extends AbstractWirelessDevice> coll )
    {
        assert !dev1.isCommunicating() : "This method should not be called on a device that is already communicating";
        
        
        Vector<AbstractWirelessDevice> collValid = new Vector<AbstractWirelessDevice>();
        
        for( AbstractWirelessDevice dev2 : coll )
        {
            // (ignore the device if it is already in the collection)
            if( dev1 != dev2 )
            {
                if( !dev2.isCommunicating() && dev1.canCommunicateWith( dev2 ) )
                    collValid.add( dev2 );
            }
        }
        
        return collValid;
    }
    
    
    /**
      * This method finds a device (note: ANY type of AbstractWirelessDevice) in the
      * given AbstractWirelessDevice's range (so they can mutually communicate) that
      * is not communicating. <br>
      *  <br>
      * The device may be either: <br>
      *  - a mobile object <br>
      *  - a beacon <br>
      *  - an information source <br>
      * <br>
      * If there are multiple devices in range, then it will choose one at random.
      * If no device is found, this will return null.
      */
    private AbstractWirelessDevice findValidDevice( AbstractWirelessDevice dev )
    {
        assert !dev.isCommunicating() : "This method should not be called on a device that is already communicating";
        
        
        // Get a list of all devices in range
        Vector<AbstractWirelessDevice> coll = getValidDevices( dev, informationSources );
        coll.addAll( getValidDevices( dev, beacons ) );
        coll.addAll( getValidDevices( dev, mobileObjs ) );
        
        int numDevs = coll.size();
        
        if( numDevs == 0 )
            return null;
        else
        {
            int index = SimulatorTools.randInRange( 0, numDevs-1 );
            return coll.get( index );
        }
    }
    
    
    /**
      * This method finds a mobile object (note: a mobile object only) in the
      * given AbstractWirelessDevice's range (so they can mutually communicate) that
      * is not communicating.  <br>
      * <br>
      * If there are multiple devices in range, then it will choose one at random.
      * If no device is found, this will return null. <br>
      * <br>
      * This method is basically the equivalent to findValidDevice, except it's
      * specific in only returning a mobile object.
      * 
      * @see #findValidDevice( AbstractWirelessDevice )
      */
    private AbstractWirelessDevice findValidMobileObject( AbstractWirelessDevice dev )
    {
        assert !dev.isCommunicating() : "This method should not be called on a device that is already communicating";
        
        
        // Get a list of all mobile objects in range
        Vector<AbstractWirelessDevice> coll = getValidDevices( dev, mobileObjs );
        int numDevs = coll.size();
        
        if( numDevs == 0 )
            return null;
        else
        {
            int index = SimulatorTools.randInRange( 0, numDevs-1 );
            return coll.get( index );
        }
    }
    
    
    /**
      * This method handles performing the communication tasks (at a high level)
      * in a given time step. <br>
      * <br>
      * Part of this includes determining which devices are to communicate.
      * The method is careful to ensure that no preference is given to any
      * device when carrying out this process of determining devices to
      * communicate.
      */
    private final void performCommunication()
    {
        // Advance communication time on all devices
        for( AbstractWirelessDevice dev : allDevices )
        {
            synchronized( dev ) //~ advanceCommunication only affects the given device
            {
                dev.advanceCommunication( timestepLength );
            }
        }
        
        // Complete or abort communication on all devices
        // (at least one of the partners in a communication must be a
        // mobile object, so we only need to check through those)
        for( AbstractWirelessDevice dev : mobileObjs )
        {
            synchronized( dev ) //~ checkCommunication will affect BOTH devices (may also want to acheive a lock on partner?)
            {
                dev.checkCommunication();
            }
        }
        
        
        // Find valid partners for devices and initiate communication on them
        // 'remDevices' keeps track of the devices that have not yet been checked
        // for starting communication with their neighbours
        Vector<AbstractWirelessDevice> remDevices = new Vector<AbstractWirelessDevice>( allDevices );
        while( !remDevices.isEmpty() )
        {
            int index = SimulatorTools.randInRange( 0, remDevices.size()-1 );
            AbstractWirelessDevice dev = remDevices.remove( index );
            
            if( !dev.isCommunicating() )
            {
                AbstractWirelessDevice partner = null;
                
                if( dev instanceof MobileObject )
                    partner = findValidDevice( dev );
                else
                    partner = findValidMobileObject( dev );
                
                if( partner != null )
                {
                    synchronized( dev ) //~ initiateCommunication affects both devices
                    {
                        synchronized( partner ) //~
                        {
                            dev.initiateCommunication( partner );
                        }
                    }
                }
            }
        }
    }
}
