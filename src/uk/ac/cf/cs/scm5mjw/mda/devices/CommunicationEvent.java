/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;

import java.util.List;

import uk.ac.cf.cs.scm5mjw.mda.devices.AbstractWirelessDevice;
import uk.ac.cf.cs.scm5mjw.mda.devices.Artifact;

/**
  * This event indicates a communication event (e.g. transfer of an artifact)
  * has occurred in the Simulation.
  */
public class CommunicationEvent
{
    private AbstractWirelessDevice device1;
    private AbstractWirelessDevice device2;
    private List<Artifact> toDevice1;
    private List<Artifact> toDevice2;
    
    
    
    
    /**
      * Constructs a CommunicationEvent object with the various items of data that
      * describe the event.
      * 
      * @param device1 one of the two devices involved in the communication event
      * @param device2 one of the two devices involved in the communication event
      * @param toDevice1 the artifacts that were to be committed to device1
      * @param toDevice2 the artifacts that were to be committed to device2
      */
    public CommunicationEvent( AbstractWirelessDevice device1, List<Artifact> toDevice1, AbstractWirelessDevice device2, List<Artifact> toDevice2 )
    {
        this.device1 = device1;
        this.device2 = device2;
        this.toDevice1 = toDevice1;
        this.toDevice2 = toDevice2;
    }
    
    
    
    
    /**
      * An accessor to return one of the devices involved in the communication
      * event.
      */
    public AbstractWirelessDevice getDevice1()
    {
        return device1;
    }
    
    
    /**
      * An accessor to return one of the devices involved in the communication
      * event.
      */
    public AbstractWirelessDevice getDevice2()
    {
        return device2;
    }
    
    
    /**
      * An accessor to return the list of artifacts that were to be committed to
      * device1 (i.e. these are the artifacts that will be added to device1 if
      * the communication and transmission are successful).
      */
    public List<Artifact> getArtifactsToDevice1()
    {
        return toDevice1;
    }
    
    
    /**
      * An accessor to return the list of artifacts that were to be committed to
      * device2 (i.e. these are the artifacts that will be added to device2 if
      * the communication and transmission are successful).
      */
    public List<Artifact> getArtifactsToDevice2()
    {
        return toDevice2;
    }
}
