/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;

/**
  * This is a listener interface for receiving events/notifications about a 
  * communication during a simulation. <br>
  */
public interface CommunicationListener
{
    /**
      * Invoked when a communication is initiated between two devices. <br>
      *  <br>
      * Note that at this stage the artifacts to be transferred between devices
      * have not yet been calculated, so the CommunicationEvent will have
      * null references instead of the lists of artifacts. 
      */
    public void communicationInitiated( CommunicationEvent evt );
    
    
    /**
      * Invoked when devices manage to successfully discover each other (and
      * hence proceed to the transmission phase) when a discovery attempt is made. <br>
      *  <br>
      * (Counterpart to discoveryFailed.)
      * 
      * @see #discoveryFailed
      */
    public void discoverySucceeded( CommunicationEvent evt );
    
    
    /**
      * Invoked when devices do not manage to successfully discover each other
      * when a discovery attempt is made (and hence the transmission phase
      * is not reached). <br>
      *  <br>
      * Note that there will be no artifacts to be transferred to either device,
      * since discovery failed. Hence, in the CommunicationEvent object, both
      * lists of artifacts to be transferred will be null. <br>
      *  <br>
      * (Counterpart to discoverySucceeded.)
      * 
      * @see #discoverySucceeded
      */
    public void discoveryFailed( CommunicationEvent evt );
    
    
    /**
      * Invoked if a communication session is aborted.
      */
    public void communicationAborted( CommunicationEvent evt );
    
    
    /**
      * Invoked if a communication session's transmission was successful. Hence,
      * this notification indicates a completely successful communication session
      * (and artifacts will be successfully transferred between the two devices 
      * involved). <br>
      *  <br>
      * (Counterpart to communicationTransmissionFailed.)
      * 
      * @see #communicationTransmissionFailed
      */
    public void communicationTransmissionSucceeded( CommunicationEvent evt );
    
    
    /**
      * Invoked if a communication session fails because of a transmission
      * failure. <br>
      *  <br>
      * (Counterpart to communicationTransmissionSucceeded.)
      * 
      * @see #communicationTransmissionSucceeded
      */
    public void communicationTransmissionFailed( CommunicationEvent evt );
}