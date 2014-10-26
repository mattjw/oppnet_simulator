/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda;

/**
  * This event indicates an event has occurred during a Simulation and encapsulates
  * information about that event.
  */
public class SimulationEvent
{
    private Simulator sim;
    private double timeOccurred;
    
    
    
    
    /**
      * Constructs a SimulationEvent object with the various items of data that
      * describe the event.
      * 
      * @param sim the simulator responsible for the event
      * @param timeOccurred the time in the simulation that the event occurred
      */
    public SimulationEvent( Simulator sim, double timeOccurred )
    {
        this.sim = sim;
        this.timeOccurred = timeOccurred;
    }
    
    
    
    
    /**
      * An accessor to return the Simulator that was responsible for the event.
      */
    public Simulator getSimulator()
    {
        return sim;
    }
    
    
    /**
      * An accessor to return the time the event occurred. Note that this is the
      * time in terms of the Simulation (not real world time).
      */
    public double getTimeOccurred()
    {
        return timeOccurred;
    }
}
