package uk.ac.cf.cs.scm5mjw.mda;

/**
  * This is an enumerated type that the monitors use to track the stage the
  * simulation being monitored is in. The expected transition of stages is:<br>
  * 1. BEFORE_START :  Before the simulation has started<br>
  * 2. RUNNING : While the simulation is running<br>
  * 3. FINISHED : Once the simulation has finished running
  */
public enum MonitorPhase
{
    BEFORE_START,
    RUNNING,
    FINISHED
}