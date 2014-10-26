/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;

import java.util.*;

/** 
  * This class facilitates performing a communication between two devices. This
  * includes calculating the time needed to complete a communication session, and
  * carrying out the transfers between two devices. <br>
  * <br>
  * Information on variables and units that affect communication: <br>
  *  * Channel setup time  (seconds) <br>
  *  * Metadata size       (kB) (kiloBytes) <br>
  *  * Artifact size       (kB) (kiloBytes) <br>
  *  * Data rate           (kb/s) (kbps) (kilobits per second)
  */
public final class StandardCommController implements CommunicationController
{
    /* Constants / defaults */
    public static final double DEFAULT_CHANNEL_SETUP_TIME = 0.5;
    public static final double DEFAULT_METADATA_SIZE = 0.1;      // 0.1 secs => takes 0.0032 seconds to transfer one metadata
    public static final double DEFAULT_ARTIFACT_SIZE = 3;
    public static final double DEFAULT_DATA_RATE = 250;
    public static final double DEFAULT_TRANSMISSION_SUCCESS_RATE = 0.95;
    public static final double DEFAULT_HARDWARE_DISCOVERY_SUCCESS_RATE = 0.95;
    private static final double BYTE_SIZE = 8.0;
    
    // A single empty Artifact list to avoid repeatedly creating a new empty list
    private static final List<Artifact> EMPTY_ARTIFACT_LIST = new LinkedList<Artifact>(); 
    
    /* Instance variables */
    // Communication
    private double channelSetupTime;
    private double metadataSize;
    private double artifactSize;
    private double dataRate;
    private double transmissionSuccessRate;
    private double hardwareDiscoverySuccessRate;
    
    // Non-communication
    Set<CommunicationListener> commListeners;
    
    
    
    /**
      * Construct a new StandardCommController with default values.
      */
    public StandardCommController()
    {
        channelSetupTime = DEFAULT_CHANNEL_SETUP_TIME;
        metadataSize = DEFAULT_METADATA_SIZE;
        artifactSize = DEFAULT_ARTIFACT_SIZE;
        dataRate = DEFAULT_DATA_RATE;
        transmissionSuccessRate = DEFAULT_TRANSMISSION_SUCCESS_RATE;
        hardwareDiscoverySuccessRate = DEFAULT_HARDWARE_DISCOVERY_SUCCESS_RATE;
        
        commListeners = new HashSet<CommunicationListener>();
    }
    
    
    
    
    /* COMMUNICATION METHODS */
    
    /**
      * This method will initiate a communication session between two devices. 
      * The end result is that the devices involves are set up to be communicating
      * with each other. The devices will be put in to the initiating phase. <br>
      *  <br>
      * The duration of the initiating phase is the channel setup time. <br>
      * After the initiating phase is elapsed, discovery should be attempted.
      */
    public void initiateCommunication( AbstractWirelessDevice dev1, AbstractWirelessDevice dev2 )
    {
        assert !dev1.isCommunicating();
        assert !dev2.isCommunicating(); 
        
        
        double setupTime = channelSetupTime;
        
        dev1.setInitiationPhase( setupTime, dev2 );
        dev2.setInitiationPhase( setupTime, dev1 );
        
        
        /* Send notification to listeners (if any) */
            if( !commListeners.isEmpty() )
            {
                CommunicationEvent evt = new CommunicationEvent( dev1, null, dev2, null );
                
                for( CommunicationListener cl : commListeners )
                    cl.communicationInitiated( evt );
            }
    }
        
        
    
    /**
      * This method will simulator the attempt of the devices in a communication
      * session to discover each other. <br>
      * On entry to this method, both devices should be in the initiation phase
      * and both should be at the end of this phase (the time should be fully
      * elapsed). <br>
      *  <br>
      * If the discovery is successful, the devices will be moved into the
      * transmission phase. The transmission phase is where metadata and
      * artifacts will be transferred. The duration of the transmission is the
      * time needed to transmit this metadata and the artifacts. <br>
      * Note that this will check to ensure that at least one of the two devices
      * involved in the communication is a mobile device. <br>
      *  <br>
      * If discovery fails, then the devices' communication will be ended. A
      * discovery can fail for one of two reasons: <br>
      *  - The devices are out of range (by the end of the initiation phase, the
      *    devices may have moved out of range) <br>
      *  - Because of hardware discovery failure, this is modelled by the hardware
      *    discovery success rate<br>
      *  <br>
      *  
      * Note that hardware discovery is always checked after whether the devices
      * are still in range.<br>
      * The method will first check whether devices are still in range. If not,
      * then the discovery will fail before hardware discovery is taken into
      * consideration.<br>
      * If the devices ARE still in range, then the method will subsequently
      * consider hardware discovery (using the hardware discovery success rate
      * as a probability for hardware discovery success).
      */
    public void attemptDiscovery( AbstractWirelessDevice dev1, AbstractWirelessDevice dev2 )
    {
        assert dev1.getCommunicationPhase() == CommunicationPhase.INITIATION;
        assert dev2.getCommunicationPhase() == CommunicationPhase.INITIATION;
        assert dev1.getCommunicationPartner() == dev2;
        assert dev1.getCommunicationTimeRemaining() == 0;
        assert dev2.getCommunicationTimeRemaining() == 0;
        
        
        boolean discoveryIsSuccess;
        // First check discovery based on whether devices are still in range...
        if( !dev1.canCommunicateWith( dev2 ) )
            discoveryIsSuccess = false;
        else
        {
            // Check hardware discovery...
            double rand = Math.random();
            discoveryIsSuccess = rand < hardwareDiscoverySuccessRate;
        }
        
        
        if( !discoveryIsSuccess )
        {
            // Discovery failure...
            dev1.unsetCommunicationAttributes();
            dev2.unsetCommunicationAttributes();
            
            
            /* Send notification to listeners (if any) */
            if( !commListeners.isEmpty() )
            {
                CommunicationEvent evt = new CommunicationEvent( dev1, null, dev2, null );
                
                for( CommunicationListener cl : commListeners )
                    cl.discoveryFailed( evt );
            }
        }
        else
        {
            // Discovery success...
            MobileObject arg1;
            AbstractWirelessDevice arg2;
            
            /* The following will ensure that dev1 is definitely a mobile object (by swapping with dev2 if necessary) */
            if( dev1 instanceof MobileObject )
            {
                // (Here we have the case that dev1 is a MobileObject)
                
                arg1 = (MobileObject)dev1;
                arg2 = dev2;
            }
            else
            {
                // (Here we have the case that dev1 is not a MobileObject)
                
                if( dev2 instanceof MobileObject )
                {
                    arg1 = (MobileObject)dev2;
                    arg2 = dev1;
                }
                else
                    throw new CommunicationException( "At least one of the two devices involved in a communication must be a mobile object" );
            }
                
            /* Decide which method will handle initiating the communication
               (discovery was successful) */
            if( arg2 instanceof MobileObject )
                transmissionWithMobileObject( arg1, (MobileObject)arg2 );
            else if( arg2 instanceof Beacon )
                transmissionWithBeacon( arg1, (Beacon)arg2 );
            else if( arg2 instanceof InformationSource )
                transmissionWithInformationSource( arg1, (InformationSource)arg2 );
            
            
            /* Send notification to listeners (if any) */
            if( !commListeners.isEmpty() )
            {
                List<Artifact> toArg1 = arg1.getArtifactsToCommit();
                List<Artifact> toArg2 = arg2.getArtifactsToCommit();
                CommunicationEvent evt = new CommunicationEvent( arg1, toArg1, arg2, toArg2 );
                
                for( CommunicationListener cl : commListeners )
                    cl.discoverySucceeded( evt );
            }
        }
    }
    
    
    /**
      * This method will abort the communication session between the two inputted
      * devices.  <br>
      * Note that the initiation phase cannot be aborted.
      */
    public void abortCommunication( AbstractWirelessDevice dev1, AbstractWirelessDevice dev2 )
    {
        assert dev1.getCommunicationPhase() != CommunicationPhase.INITIATION : "Cannot abort the initiation phase";
        assert dev2.getCommunicationPhase() != CommunicationPhase.INITIATION : "Cannot abort the initiation phase";
        
        if( !dev1.isCommunicating() )
            throw new CommunicationException( "Device 1 is not communicating" );
        
        if( !dev2.isCommunicating() )
            throw new CommunicationException( "Device 2 is not communicating" );
        
        if( dev1.getCommunicationPartner() != dev2 )
            throw new CommunicationException( "The devices are not partners in the same communication session" );
        
        if( dev1.canCommunicateWith(dev2) )
        {
            if( dev1.getCommunicationTimeRemaining() == 0 )
                throw new CommunicationException( "Communication should not be aborted - the communication time has fully elapsed and the devices are still in range" );
            else
                throw new CommunicationException( "Communication should not be aborted - the devices are still in range"  );
        }
        
        
        /* Send notification to listeners (if any) */
        if( !commListeners.isEmpty() )
        {
            List<Artifact> toDev1 = dev1.getArtifactsToCommit();
            List<Artifact> toDev2 = dev2.getArtifactsToCommit();
            CommunicationEvent evt = new CommunicationEvent( dev1, toDev1, dev2, toDev2 );
            
            for( CommunicationListener cl : commListeners )
                cl.communicationAborted( evt );
        }
        
        
        /* Abort the communication */
        dev1.unsetCommunicationAttributes();
        dev2.unsetCommunicationAttributes();
    }
    
    
    /**
      * This method will complete the communication session between the two
      * inputted devices. This is when the transmission phase is fully elapsed. <br>
      *  <br>
      * Note that completion means that the devices have completed the time
      * required to communicate. However, it does not imply that the communication
      * is successful. Hence, it may still be possible that no artifacts are
      * transferred (committed). <br>
      *  <br>
      * In particular, transmission success rate is considered here (by comparison
      * to a random number). If the transmission fails, then simply no artifacts
      * are committed.
      */
    public void completeCommunication( AbstractWirelessDevice dev1, AbstractWirelessDevice dev2 )
    {
        if( !dev1.isCommunicating() )
            throw new CommunicationException( "Device 1 is not communicating" );
        
        if( !dev2.isCommunicating() )
            throw new CommunicationException( "Device 2 is not communicating" );
        
        if( dev1.getCommunicationPhase() != CommunicationPhase.TRANSMISSION )
            throw new CommunicationException( "Device 1 is not in the transmission phase (completion can only occur at the end of the transmission phase)" );
        
        if( dev2.getCommunicationPhase() != CommunicationPhase.TRANSMISSION )
            throw new CommunicationException( "Device 2 is not in the transmission phase (completion can only occur at the end of the transmission phase)" );
        
        if( dev1.getCommunicationPartner() != dev2 )
            throw new CommunicationException( "The devices are not partners in the same communication session" );
        
        if( !dev1.canCommunicateWith(dev2) )
            throw new CommunicationException( "Cannot complete communication - the devices are out of range"  );
        
        if( dev1.getCommunicationTimeRemaining() > 0 )
            throw new CommunicationException( "Cannot complete communication - communication time has not fully elapsed" );
        
        
        // Simulate a transmission success rate
        double rand = Math.random();
        if( rand < transmissionSuccessRate )
        {
            /* Transmission successful -- commit the artifacts */
            List<Artifact> toDev1 = dev1.commitArtifacts();
            List<Artifact> toDev2 = dev2.commitArtifacts();
            
            
            /* Send notification to listeners (if any)*/
            if( !commListeners.isEmpty() )
            {
                CommunicationEvent evt = new CommunicationEvent( dev1, toDev1, dev2, toDev2 );
                
                for( CommunicationListener cl : commListeners )
                    cl.communicationTransmissionSucceeded( evt );
            }
        }
        else
        {
            /* Transmission not successful (do nothing) */
            
            /* Send notification to listeners (if any)*/
            if( !commListeners.isEmpty() )
            {
                List<Artifact> toDev1 = dev1.getArtifactsToCommit();
                List<Artifact> toDev2 = dev2.getArtifactsToCommit();
                CommunicationEvent evt = new CommunicationEvent( dev1, toDev1, dev2, toDev2 );
                
                for( CommunicationListener cl : commListeners )
                    cl.communicationTransmissionFailed( evt );
            }
        }
        
        
        dev1.unsetCommunicationAttributes();
        dev2.unsetCommunicationAttributes();
    }
    
    
    /* COMMUNICATION -- PRIVATE HELPER METHODS */
    
    /**
      * This method will handle setting up the transmission phase between a 
      * mobile object and another mobile object.
      */
    private void transmissionWithMobileObject( MobileObject dev1, MobileObject dev2 )
    {
        ArtifactContainer dev1Cont = dev1.getArtifactContainer();
        ArtifactContainer dev2Cont = dev2.getArtifactContainer();
        
        /* Get/calculate attributes for communication */
        List<Artifact> toDev1 = dev1Cont.transferFrom( dev2Cont );
        List<Artifact> toDev2 = dev2Cont.transferFrom( dev1Cont );
        
        double commTime = calcTransmissionTime( toDev1.size() + toDev2.size() );
        
        /* Set the attributes */
        dev1.setTransmissionPhase( commTime, toDev1 );
        dev2.setTransmissionPhase( commTime, toDev2 );
    }
    
    
    /**
      * This method will handle setting up the transmission phase between a 
      * mobile object and a beacon.
      */
    private void transmissionWithBeacon( MobileObject dev1, Beacon dev2 )
    {
        ArtifactContainer dev1Cont = dev1.getArtifactContainer();
        ArtifactContainer dev2Cont = dev2.getArtifactContainer();
        
        /* Get/calculate attributes for communication */
        List<Artifact> toDev1 = dev1Cont.transferFrom( dev2Cont );
        List<Artifact> toDev2 = dev2Cont.transferFrom( dev1Cont );
        
        double commTime = calcTransmissionTime( toDev1.size() + toDev2.size() );
        
        /* Set the attributes */
        dev1.setTransmissionPhase( commTime, toDev1 );
        dev2.setTransmissionPhase( commTime, toDev2 );
    }
    
    
    /**
      * This method will handle setting up the transmission phase between a 
      * mobile object and an information source.
      */
    private void transmissionWithInformationSource( MobileObject dev1, InformationSource dev2 )
    {
        List<Artifact> list = new LinkedList<Artifact>();
        list.add( dev2.generateArtifact() );
        
        double commTime = calcTransmissionTime( list.size() );
        
        /* Set the attributes */
        dev1.setTransmissionPhase( commTime, list );
        // No artifacts should be transferred TO an information source, so
        // we must give it an empty list:
        dev2.setTransmissionPhase( commTime, EMPTY_ARTIFACT_LIST );
    }
    
    
    /**
      * This method will calculate the duration of a transmission phase.
      * The transmission phase includes the time taken to transfer the
      * metadata and the time taken to transfer the given number of artifacts. <br>
      * Note that half-duplex communication is assumed. Time will account
      * for the fact that transfer of metadata and artifacts will occur sequentially
      */
    private double calcTransmissionTime( int numArtifacts )
    {
        double byteRate = dataRate / BYTE_SIZE;
        double total = 0;
        
        total += (metadataSize / byteRate) * 2; // have assumed half duplex, thus two metadtaa ranfers in sequence
        total += (artifactSize * numArtifacts) / byteRate;
        
        return total;
    }
    
    
    /* OTHER METHODS (NON-COMMUNICATION) */
    
    /**
      * Registers a CommunicationListener to receive events when a communication
      * event occurs.
      */
    public void addCommunicationListener( CommunicationListener l )
    {
        commListeners.add( l );
    }
    
    
    /**
      * Unregisters a CommunicationListener.
      */
    public void removeCommunicationListener( CommunicationListener l )
    {
        commListeners.remove( l );
    }
}