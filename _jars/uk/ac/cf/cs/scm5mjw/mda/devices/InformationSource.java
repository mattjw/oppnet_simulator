/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;

import java.util.*;
import java.awt.geom.Point2D;
     
/** 
  * This is a class which represents an information source in the mobile
  * communications system. <br>
  * An information source reports on a particular resource. (An instance of a
  * report at a given time is called an artifact.) A resource ID is used to
  * indicate the resource an information source reports on. <br>
  * <br>
  * Notes on units: <br>
  * * n/a
  */
public final class InformationSource extends AbstractWirelessDevice
{
    public static final String INFORMATION_SOURCE_TEXT_IDENTIFIER = "InformationSource";
    
    private int resourceID;
    
    
    
    
    /**
      * Construct a InformationSource at the given location.
      */
    public InformationSource( Point2D.Double inLocation, int inResourceID )
    {
        super();
        
        if( inResourceID < 1 )
            throw new InvalidInformationSourceException( "Resource ID must be greater or equal to 1" );
        
        setLocation( inLocation );
        resourceID = inResourceID;
    }
    
    
    /**
      * An accessor for the resource ID of the resource this information source
      * reports on.
      */
    public int getResourceID()
    {
        return resourceID;
    }
    
    
    /**
      * Get a string representation of this InformationSource.
      */
    public String toString()
    {
        String str = "[ <InformationSource> " + super.toString() + ", Resource ID: " + resourceID + "]";
        return str;
    }
    
    
    /**
      * A textual identifier for this TYPE of device.
      */
    public String getDeviceTypeIdentifier()
    {
        return INFORMATION_SOURCE_TEXT_IDENTIFIER;
    }
    
    
    
    
    /* COMMUNICATION METHODS */
    
    /**
      * This method will generate an Artifact of the resource that this device
      * reports on.
      */
    public Artifact generateArtifact()
    {
        return new Artifact( resourceID );
    }
    
    
    /**
      * This method overrides the corresponding AbstractWirelessDevice method. <br>
      * This method will throw an error if called - Information sources do NOT
      * maintain an artifact container (they simply generate artifacts).
      */
    public ArtifactContainer getArtifactContainer()
    {
        throw new CommunicationException( "An information source does not maintain a collection of artifacts" );
    }
    
    
    /** 
      * This method will override the original commArtifacts(). <br>
      * An information source will never add any artifacts to itself, thus this
      * method is implemented so that it will do NOTHING (nothing is actually
      * committed!). <br>
      * Aside from the fact that it will not actually add any artifacts (i.e. it
      * will not commit any artifacts), it does the same things as the original
      * commtArtifacts. <br>
      *  <br>
      * Also note that it will still return whatever list of artifacts was specified
      * to be added in the communication initiation. This SHOULD be an empty list!!!
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
        
        // (do not actually do any committing)
        
        return artifactsToAdd;
    }
    
    
    /**
      * This method overrides the original setTransmissionPhase method
      * with an additional check to ensure that the artifactsToAdd is an empty
      * list (this is a requirement of an information source). <br>
      * Aside from this, this method is identical to the original (in fact,
      * it calls the original after performing the check).
      */
    public void setTransmissionPhase( double duration, List<Artifact> artifactsToAdd )
    {
        if( artifactsToAdd == null )
            throw new CommunicationException( "The list of artifacts to be transferred must never be null (an empty list should be given if there are no artifacts to be transferred" );
        
        if( !artifactsToAdd.isEmpty() )
            throw new CommunicationException( "The list of artifacts to be transferred to an information source must be empty" );
        
        super.setTransmissionPhase( duration, artifactsToAdd );
    }
}
