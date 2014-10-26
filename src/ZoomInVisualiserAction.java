import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import uk.ac.cf.cs.scm5mjw.mda.Visualiser;

/**
  * A custom action to invoke a 'zoom in' on a Visualiser.
  */
class ZoomInVisualiserAction extends AbstractAction
{
    private static final double FACTOR = 3.0/4.0;
    
    private static final String ACTION_TEXT = "Zoom In";
    private static final String ACTION_TOOLTIP = "Zoom In";
    private static final ImageIcon ACTION_ICON = new ImageIcon( "./resc/zoomin.png" );
    
    private Visualiser vis;
    
    public ZoomInVisualiserAction( Visualiser vis )
    {
        super();
        this.vis = vis;
        
        putValue( AbstractAction.NAME, ACTION_TEXT ); // Text
        putValue( AbstractAction.SHORT_DESCRIPTION, ACTION_TOOLTIP ); // Tooltip
        putValue( AbstractAction.SMALL_ICON, ACTION_ICON );           // Icon
    }
    
    public void actionPerformed( ActionEvent evt )
    {
        double oldFactor = vis.getZoomFactor();
        vis.setZoomFactor( oldFactor * (1.0/FACTOR) );
    }
}


