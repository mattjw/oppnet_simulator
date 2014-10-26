/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda;

/**
  * This is a listener interface for receiving events/notifications from a
  * Simulator.
  */
public interface SimulationListener
{
    /**
      * Invoked just before a Simulation's run starts.
      */
    public void simulationStarted( SimulationEvent evt );
    
    
    /**
      * Invoked after a timestep in the Simulation's run has been advanced. <br>
      * <br>
      * A note on what this means in terms of the current iteration number:
      * A timestep advancement occurs after all of the simulation calculations
      * an actions have been carried out AND after the timestep iteration and 
      * time elapsed have been incremented. Thus a given iteration number 
      * corresponds to all of the actions of the simulation taken BEFORE that
      * iteration. <br>
      * For example, if the current iteration is iteration 3, then this corresponds
      * to all of the simulation actions carried out BETWEEN iteration 2 and 3.
      * Thus, iteration 0 means that nothing has been carried out by the
      * simulator, so there never be an timestep advancement event with an
      * iteration of 0. <br> 
      * 1 will always be the iteration number of the first timestep advancement
      * event.
      */
    public void simulationTimestepAdvanced( SimulationEvent evt );
    
    
    /**
      * Invoked just after a Simulation's run finishes.
      */
    public void simulationFinished( SimulationEvent evt );
}