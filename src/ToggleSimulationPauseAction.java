import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import uk.ac.cf.cs.scm5mjw.mda.Simulator;

/**
  * A custom action which will toggle a simulation between paused and unpaused.
  * This class will also handle changing the AbstractAction's name when the
  * simulation is toggled. e.g. this will cause a JButton's text to change
  * as appropriate.
  */
class ToggleSimulationPauseAction extends AbstractAction
{
    private static final String DO_PAUSE_TEXT = "Pause Simulation";
    private static final String DO_UNPAUSE_TEXT = "Unpause Simulation";
    private static final ImageIcon DO_PAUSE_ICON = new ImageIcon( "./resc/pause.png" );
    private static final ImageIcon DO_UNPAUSE_ICON = new ImageIcon( "./resc/play.png" );
    
    private static final String ACTION_TOOLTIP = "Pauses or unpauses the simulation";
    
    private Simulator sim;
    
    public ToggleSimulationPauseAction( Simulator sim )
    {
        super();
        this.sim = sim;
        
        handleToggleText();
        putValue( AbstractAction.SHORT_DESCRIPTION, ACTION_TOOLTIP ); // Tooltip
    }
    
    public void actionPerformed( ActionEvent evt )
    {
        if( sim.isPaused() )
            sim.unpause();
        else
            sim.pause();
        
        handleToggleText();
    }
    
    private void handleToggleText()
    {
        if( sim.isPaused() )
        {
            putValue( AbstractAction.NAME, DO_UNPAUSE_TEXT );
            putValue( AbstractAction.SMALL_ICON, DO_UNPAUSE_ICON );
        }
        else
        {
            putValue( AbstractAction.NAME, DO_PAUSE_TEXT );
            putValue( AbstractAction.SMALL_ICON, DO_PAUSE_ICON );
        }
    }
}