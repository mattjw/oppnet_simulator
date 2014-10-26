/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;

import uk.ac.cf.cs.scm5mjw.mda.SimulatorTools;
import java.awt.geom.Point2D;
import java.util.*;
     
/** 
  * This is a class is a skeletal implementation of a supertype for all objects
  * in the simulation which can communicate wirelessly. <br>
  * The functionality which these objects have in common has been grouped
  * into this class so as to reduce duplication. <br>
  * <br>
  * Note that on creation, each device is given an immutable device ID number.
  * These numbers are automatically assigned by this class and increment
  * in the order that device are created. (For example, the first
  * device created has the ID 1). Device IDs are unique across ALL
  * devices (their subclass type is irrelevant). <br>
  * Static method <code>resetNextDeviceID</code> will reset the device IDs to 1. Thus, IDs
  * for devices created after a reset will start at 1. Users of this class should thus
  * be careful as it means two devices may have the same ID. <br>  
  * <br>
  * Notes on units: <br>
  * * Communication range is in meters    <br>
  * * Communication time (time remaining) is in seconds
  */
public abstract class AbstractWirelessDevice
{
    /* Constants / defaults */
    public static final double DEFAULT_COMMUNICATION_RANGE = 20;
    
    /* Class variables */
    private static int nextDeviceID = 1;
    
    /* Instance variables */
    // General device variables
    private Point2D.Double location;
    private double communicationRange;
    private ArtifactContainer artCont;
    private int deviceID;
    
    // Variables relating to this device's current communication session
    protected boolean isCommunicating;
    protected double timeLeft;          // The time left for THIS phase of communication
    protected CommunicationPhase commPhase;
    protected AbstractWirelessDevice commPartner;
    protected List<Artifact> artifactsToAdd;
    
    
    
    
    /* ***** CONSTRUCTORS ***** */
    
    /**
      * This constructor JUST sets up the default values of a wireless device. If
      * a subclass overrides this constructor, it must be sure to call this
      * constructor so that these initial variables are set up correctly. <br>
      * This includes the set up of: <br>
      * * Communication Range <br>
      * * Initial communication session variables <br>
      * * The artifact container
      */
    public AbstractWirelessDevice()
    {
        communicationRange = DEFAULT_COMMUNICATION_RANGE;
        isCommunicating = false;
        timeLeft = -1;
        artCont = new ArtifactContainer();
        deviceID = nextDeviceID++;
    }
    
    
    
    
    /* ***** GENERAL METHODS ***** */
    
    /**
      * This method checks whether the input AbstractWirelessDevice is in the communication
      * range of this AbstractWirelessDevice, but NOT the other way around. <br>
      * Thus, given two AbstractWirelessDevices a and b, <br>
      *     <code>a.reaches( b )</code> <br>
      * will return true if b is within a's communication range. However,
      * b does NOT necessarily need to be in a's communication range. <br>
      *  <br>
      * (Note that communication range is inclusive, thus an device that is
      * exactly at another's communication range still counts as being in range.)
      */
    public boolean reaches( AbstractWirelessDevice dev )
    {
        double dist = SimulatorTools.distance( this.getLocation(), dev.getLocation() );
        return dist <= this.communicationRange;
    }
    
    
    /** 
      * This method checks whether this AbstractWirelessDevice and the inputted AbstractWirelessDevice
      * can communicate. For two devices to communicate they both need to be within
      * eachother's communication ranges. <br>
      * Note that this method is NOT responsible for taking into account whether 
      * or not the devices are already communicating. <br>
      * Thus, given two AbstractWirelessDevices a and b, this returns true if: <br>
      * <code> a.reaches( b ) AND b.reaches( a ) </code>
      *
      * @see #reaches(AbstractWirelessDevice)
      */    
    public boolean canCommunicateWith( AbstractWirelessDevice dev )
    {
        return this.reaches( dev ) && dev.reaches( this );
    }
    
    
    /**
      * Accessor for the location of this device.
      */
    public Point2D.Double getLocation()
    {
        return location;
    }
    
    
    /**
      * Accessor for this device's communication range.
      */
    public double getCommunicationRange()
    {
        return communicationRange;
    }
    
    
    /**
      * Accessor for this device's device ID.
      */
    public int getDeviceID()
    {
        return deviceID;
    }
    
    
    /**
      * Mutator for this device's location.
      */
    public void setLocation( Point2D.Double loc )
    {
        location = loc;
    }
    
    
    /**
      * Mutator for this device's communication range.
      */
    public void setCommunicationRange( double commRange )
    {
        if( commRange < 0 )
            throw new IllegalArgumentException( "Communication range cannot be negative" );
        
        communicationRange = commRange;
    }
    
    
    /**
      * Get a string representation of this AbstractWirelessDevice.
      */
    public String toString()
    {
        String coordStr = "(" + location.getX() + "," + location.getY() + ")";
        String str = "Device ID: " + deviceID + ", Location: " + coordStr + ", Communication Range: " + communicationRange;
        return str;
    }
    
    
    
    
    /* ***** METHODS FOR COMMUNICATION ***** */
    
    /* ACCESSORS / MUTATORS */
    
    /**
      * Accessor for the device's current communication phase. Only a 
      * communicating device has a communication phase.
      * 
      * @throws CommuicationException if the device is not communicating
      * 
      * @see #isCommunicating()
      */
    public CommunicationPhase getCommunicationPhase()
    {
        if( !isCommunicating )
            throw new CommunicationException( "A device that is not communicating does not have a communication phase" );
        
        return commPhase;
    }
    
    
    /**
      * Accessor for the amount of communication time remaining. This is the
      * amount of time remaining in the current phase of communication, not
      * necessarily the whole communication.
      *
      * @throws CommunicationException if this device is not communicating
      */
    public double getCommunicationTimeRemaining()
    {
        if( !isCommunicating )
            throw new CommunicationException( "A device that is not communicating does not have a communication time remaining" );
        
        return timeLeft;
    }
    
    
    /**
      * Accessor for the current communication partner (the other device that
      * this device is communicating with).
      *
      * @throws CommunicationException if this device is not communicating
      */
    public AbstractWirelessDevice getCommunicationPartner()
    {
        if( !isCommunicating )
            throw new CommunicationException( "A device that is not communicating does not have a communication partner" );
        
        assert commPartner != null;
        
        
        return commPartner;
    }
    
    
    /**
      * Accessor for whether or not this device is currently communicating. <br>
      * Note that a device may still return true for this device even if the remaining
      * time is 0. (It is assumed that once a device's remaining time reaches 0,
      * a communication completion invocation should be made.)
      */
    public boolean isCommunicating()
    {
        return isCommunicating;
    }
    
    
    /**
      * Accessor for this device's ArtifactContainer (and hence the artifacts this
      * device holds).
      */
    public ArtifactContainer getArtifactContainer()
    {
        return artCont;
    }
    
    
    /**
      * The method returns the list of artifacts which are to be committed (if
      * the communication session is a success). <br>
      * This method will never return null. If no artifacts are to be transferred,
      * then an empty list SHOULD be returned (this behaviour is enforced by
      * the setCommunicationAttributes method and should be dealt with by the
      * communication controller).
      */
    public List<Artifact> getArtifactsToCommit()
    {
        if( !isCommunicating )
            throw new CommunicationException( "A device that is not communicating does not have any artifacts to commit" );
        
        return artifactsToAdd;
    }
      
    
    /* COMMUNICATION METHODS */
    
    /**
      * Advance this device's communication by the given time.  <br>
      * This method will increment this device's attributes that vary with time by
      * timeIncrement. <br>
      * These attributes are: <br>
      *  - the age of the device's artifacts <br>
      *  - the time remaining in this device's communication session (if it is communicating)
      * <br>
      * Notes on communication time remaining: <br>
      *   - Only THIS device's time left is updated (the time left of the communication
      *     partner is unaffected). <br>
      *   - If all of the device's communication time elapses as a result of this
      *     advancement, the time remaining will be set to 0.
      */
    public void advanceCommunication( double timeIncrement )
    {
        // Advance artifact age
        List<Artifact> list = artCont.list();
        for( Artifact art : list )
            art.advanceAge( timeIncrement );
        
        // Advance communication session time remaining
        if( isCommunicating )
        {
            timeLeft = timeLeft - timeIncrement;
            
            if( timeLeft < 0 )
                timeLeft = 0;
        }
    }
    
    
    /**
      * This method will check to see if this device's communication needs
      * to attempt discovery, complete or abort. If so, it will carry out the 
      * relevant action.<br>
      *  <br>
      * This will affect BOTH the devices involved in the communication.
      *  <br>
      * This method MAY be called on a device that is not in a communication
      * session. In this case, the method will simply have no affect. <br>
      * <br>
      * The work of discovery / completion / abortion is actually deferred to
      * the corresponding method if this class (e.g. <code>attemptDiscovery</code>),
      * which is in turn deferred to this device's communication controller. 
      * 
      * @see #attemptDiscovery()
      * @see #abortCommunication()()
      * @see #completeCommunication()
      */
    public void checkCommunication()
    {
        if( isCommunicating )
        {
            if( commPhase == CommunicationPhase.INITIATION )
            {
                if( this.getCommunicationTimeRemaining() <= 0 )
                    this.attemptDiscovery();
            }
            else if( commPhase == CommunicationPhase.TRANSMISSION )
            {
                // Partner out of range => abort
                // Communication time remaining is 0 => session completed
                if( !this.canCommunicateWith( this.getCommunicationPartner() ) )
                    this.abortCommunication();
                else if( this.getCommunicationTimeRemaining() <= 0 )
                    this.completeCommunication();

                // If neither of the above branches is taken, then it must mean that:
                //     the communication phase is the transmission phase
                //     AND
                //     the devices are still in range
                //     AND
                //     there is still time remaining
                    
            }
        }
    }
    
    
    /**
      * This method will set up this device's attributes (and only this device's
      * attributes) for a communication session with another device. This will
      * put the communication session in the first communication phase -- the
      * initiation phase. <br>
      * Essentially, this method sets up a communication session with another
      * device. <br>
      *
      * @throws CommunicationException if the devices cannot reach each other
      * @throws CommunicationException if THIS device is already in a communication session
      * @throws CommunicationException if the communication partner is this device
      * @throws NullPointerException if the communication partner is the null reference
      * 
      * @see #setTransmissionPhase(double, List)
      * @see #unsetCommunicationAttributes()
      */
    public void setInitiationPhase( double duration, AbstractWirelessDevice partner )
    {
        if( this.isCommunicating )
            throw new CommunicationException( "A device cannot communicate with two or more devices at once" );
        
        if( !this.canCommunicateWith( partner ) )
            throw new CommunicationException( "A device cannot communicate with a device that is out of its communication range" );
        
        if( this == partner )
            throw new CommunicationException( "A device cannot communicate with itself" );
        
        if( partner == null )
            throw new NullPointerException( "The partner device in communication should not be null" );
        
        
        isCommunicating = true;
        commPhase = CommunicationPhase.INITIATION;
        commPartner = partner;
        timeLeft = duration;
        
        
        assert commPartner != null;
    }
    
    
    /**
      * This method will move this device's communication session into the 
      * transmission phase and set up the attributes for this phase accordingly. <br>
      * Again, note that this method will only affect THIS device in the communication
      * session, not the partner. <br>
      * A null value for artifactsToAdd will not be accepted. If there are
      * no artifacts to transfer, then an empty list should be passed in. <br>
      * 
      * @throws CommunicationException if the device is not in a communication session
      * @throws CommunicationException if the devices cannot reach each other
      * @throws CommunicationException if the device is not currently in the initiation phase (on entry to this method)
      * @throws CommunicationException if the list of artifacts to be transferred is the null reference
      * 
      * @see #unsetCommunicationAttributes()
      */
    public void setTransmissionPhase( double duration, List<Artifact> artifactsToAdd )
    {
        if( !this.isCommunicating )
            throw new CommunicationException( "A non-communicating device cannot move to the transmission phase" );
        
        if( !this.canCommunicateWith( commPartner ) )
            throw new CommunicationException( "A device cannot move to the transmission phase if its partner is out of range" );
        
        if( this.commPhase != CommunicationPhase.INITIATION )
            throw new CommunicationException( "A device can only move to the transmission phase from the initiation phase" );
        
        if( artifactsToAdd == null )
            throw new CommunicationException( "The list of artifacts to be transferred must never be null (an empty list should be given if there are no artifacts to be transferred" );
        
        assert commPartner != null;
        
        
        commPhase = CommunicationPhase.TRANSMISSION;
        this.artifactsToAdd = artifactsToAdd;
        timeLeft = duration;
    }
    
    
    /**
      * This method will set the communication session attributes such that there
      * is no communication session. The role of the method is to put the device into
      * a state where it is not in a communication session, FROM a state where
      * it WAS in a communication session. <br>
      * Note that calling this method should not be called when a device is not
      * in a communication session. <br>
      * (Also note that this method will only affect THIS device -- no others.) <br>
      * The affected attributes are: <br>
      *  - isCommunicating <br>
      *  - commPartner <br>
      *  - timeLeft <br>
      *  - artifactsToAdd <br>
      *  - commPhase <br>
      * <br>
      * The value -1 is used for timeLeft when a device is not communicating
      * (this should not matter, however, since isCommunication is the attribute
      * to be used to indicate a communicating device).
      */
    public void unsetCommunicationAttributes()
    {
        if( !this.isCommunicating )
            throw new CommunicationException( "Only a device that is currently communicating can have its session attributes unset" );
        
        this.timeLeft = -1;
        this.commPartner = null;
        this.isCommunicating = false;
        this.artifactsToAdd = null;
        this.commPhase = null;
    }
    
    
    /** 
      * This method will commit the transfer (if any) of artifacts to this device.
      * These are the artifacts that are to be added to this device on completion
      * of communication. <br>
      * Note that this will not handle unsetting the artifactsToAdd attribute. <br>
      * <br>
      * The method returns the list of artifacts which were committed. <br>
      * This method will never return null. If no artifacts were transferred,
      * then an empty list SHOULD be returned (this behaviour is enforced by
      * the setCommunicationAttributes method and should be dealt with by the
      * communication controller).
      */
    public List<Artifact> commitArtifacts()
    {
        if( !isCommunicating )
            throw new CommunicationException( "Cannot commit artifacts - device is not communicating" );
        
        if( timeLeft > 0 )
            throw new CommunicationException( "Cannot commit artifacts - communication time has not fully elapsed" );
        
        if( !this.canCommunicateWith( commPartner ) )
            throw new CommunicationException( "Cannot commit artifacts - partner device is out of range"  );
        
        assert commPartner != null;
        
        
        artCont.copyAll( artifactsToAdd );
        
        return artifactsToAdd;
    }
    
    
    /**
      * Initiates a communication with another device. <br>
      * The communication for BOTH devices (this device and the argument device)
      * will be initiated. This is really an entry method which defers the actual
      * (complicated) work of calculating and performing various operations for
      * a communication to the Communication Controller for this class.
      */
    public void initiateCommunication( AbstractWirelessDevice device )
    {
        assert device != null;
        
        // (error checking is deferred to the Communication Controller class)
        
        commController.initiateCommunication( this, device );
    }
    
    
    /**
      * At some point during the communication between devices, we need to
      * model the process of attempting discovery. This method handles attempting
      * discovery. <br>
      * The discovery for BOTH devices in this communication will be attempted.
      * This is really an entry method which defers the actual work of calculating 
      * and performing various operations for discovery to the 
      * Communication Controller for this class.
      */
    public void attemptDiscovery()
    {
        assert commPartner != null;
        // (error checking is deferred to the Communication Controller class)
        
        commController.attemptDiscovery( this, commPartner );
    }
    
    
    /**
      * This method will cause the communication that this node is currently involved
      * in to be completed. Note that this will complete the communication for BOTH
      * partners in the communication. <br>
      * Communication completion is where actual changes to devices due to a communication
      * are committed (because the communication time has fully elapsed with the
      * device still in range).  <br>
      * A completed communication does not necessarily imply a successful
      * communication. Transmission may still fail since we are modelling a
      * transmission success rate in the communication completion. <br>
      * This is really an entry method which defers the actual work of completing
      * communication to the Communication Controller for this class.
      */
    public void completeCommunication()
    {
        assert commPartner != null;
        // (error checking is deferred to the Communication Controller class)
        
        commController.completeCommunication( this, commPartner );
    }
    
    
    /**
      * This method will cause the communication that this node is currently involved
      * in to be aborted. Note that this will abort the communication for BOTH
      * partners in the communication. <br>
      * (An aborted communication means that the communication session is finished
      * prematurely -- it does not manage to send any messages) <br>
      * <br>
      * This is really an entry method which defers the actual work of aborting
      * communication to the CommunicationController for this class.
      */
    public void abortCommunication()
    {
        assert commPartner != null;
        // (error checking is deferred to the Communication Controller class)
        
        commController.abortCommunication( this, commPartner );
    }
    
    
    
    
    /* ***** CLASS METHODS AND ATTRIBUTES ***** */
    /**
      * This method should return a textual string which identifies the type
      * of device that this is. (e.g. a MobileDevice). <br>
      * Other classes may use this when they want to get a textual name for the
      * type of device.
      */
    public abstract String getDeviceTypeIdentifier();
    
    
    private static CommunicationController commController = new StandardCommController();
    
    /**
      * An accessor for the class's CommunicationController. Note that a communication
      * controller exists for a class (and hence it is the same for all 
      * AbstractWirelessDevice objects).
      */
    public static CommunicationController getCommunicationController()
    {
        return commController;
    }
    
    
    /**
      * A mutator for the class's CommunicationController. Note that a communication
      * controller exists for a class (and hence it is the same for all 
      * AbstractWirelessDevice objects).
      */
    public static void setCommunicationController( CommunicationController inCommController )
    {
        commController = inCommController;
    }
    
    
    /**
      * This method will reset the next ID to 1. <br>
      * IDs for devices created after a reset will start from 1 again.
      */
    public static void resetNextDeviceID()
    {
        nextDeviceID = 1;
    }
     
}
