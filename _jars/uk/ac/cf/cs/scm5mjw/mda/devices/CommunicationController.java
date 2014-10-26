/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;

/** 
  * A class implementing this interface is one that can be used to handle carrying
  * out tasks relating to communication sessions, specifically: <br>
  *   1. Initiation <br>
  *   2. Completion <br>
  *   3. Abortion <br>
  */
public interface CommunicationController
{
    /**
      * This method is called to initiate a communication session between two 
      * devices. <br>
      * This will result in the devices entering the 'initiation' phase
      * (unconditionally).
      */
    public void initiateCommunication( AbstractWirelessDevice dev1, AbstractWirelessDevice dev2 );
    
    
    /**
      * This method is called after the time required for the initiation phase has
      * elapsed. At this point, the devices may enter the transmission phase if the
      * discovery attempt succeeds. <br>
      * Two examples of why a discovery may fail are: <br>
      *  - The devices have moved out of range when discovery is attempted <br>
      *  - We are modelling a blanket discovery failure rate in the system  <br>
      */
    public void attemptDiscovery( AbstractWirelessDevice dev1, AbstractWirelessDevice dev2 );
    
    
    /**
      * This method is called after the time required for the transmission phase 
      * has elapsed (and hence the time required for the entire communication has
      * also elapsed). <br>
      * This is also known as the completion of the communication.
      */
    public void completeCommunication( AbstractWirelessDevice dev1, AbstractWirelessDevice dev2 );
    
    
    /**
      * This method is called to abort a communication session between two devices.
      */
    public void abortCommunication( AbstractWirelessDevice dev1, AbstractWirelessDevice dev2 );
}
