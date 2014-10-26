/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
  * This class is a scrollable container for the simulation visualisation.<br>
  * It builds on the JScrollPane to add a 'grabbable' movement feature. It also
  * handles centering the viewport over the visualiser when the viewport
  * is smaller than the Visualiser. (Note that it does not handle centering the
  * visualisation in its own component -- this is the responsibility of the
  * Visualiser class.)<br>
  * The Visualiser for a viewer is immutable.<br
  * (N.B. the 'Visualiser' may also be referred to as the 'Component' (because 
  * this is the Swing component that is being contained within this Viewer.)
  */
public class SimulationViewer extends JScrollPane implements ComponentListener, MouseMotionListener, MouseListener, AdjustmentListener
{
    /* Constants associated with this class */
    private static final String UNGRABBED_CURSOR = "./resc/hand_open.gif";
    private static final String GRABBED_CURSOR = "./resc/hand_closed.gif";
    private static final Point HOTSPOT = new Point( 10, 9 ); 

    
    /* Instance variables for this class */
    private Visualiser vis;
    
    private Cursor ungrabbed;
    private Cursor grabbed;
    
    
    
    
    /**
      * Construct a SimulationViewer which acts as a container/viewer for the
      * given Visualiser.
      */
    public SimulationViewer( Visualiser vis )
    {
        super();
        
        // Set up the component for use with this viewer
        this.vis = vis;
        vis.addComponentListener( this );
        getViewport().setView( vis );
        
        // Set up the ability to navigate the pane using click-dragging
        getViewport().addMouseMotionListener( this );
        getViewport().addMouseListener( this );
        
        // Set up cursors for 'dragging' a component and not 'dragging' a component
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img;
        
        img = new ImageIcon(UNGRABBED_CURSOR).getImage(); 
        ungrabbed = tk.createCustomCursor( img, HOTSPOT, "Ungrabbed" );
        
        img = new ImageIcon(GRABBED_CURSOR).getImage(); 
        grabbed = tk.createCustomCursor( img, HOTSPOT, "Grabbed" );
        
        getViewport().setCursor( ungrabbed );
        
        // Set up stuff for keeping the view position between zoom in/outs 
        getVerticalScrollBar().addAdjustmentListener( this );
        getHorizontalScrollBar().addAdjustmentListener( this );
        centerPoint = null;
        dimensions = null;
    }
    
    
    
    
    /* ********** CODE FOR THE 'KEEP VIEWPORT POSITION' FEATURE ********** */
    
    /*
     * Info:
     *     dimensions
     *         the width and height of the component (i.e. Visualiser)
     *         (updated after the component is resized)
     *     centerPoint
     *         see comments for 'getCenterPoint' method
     */
    
    private Dimension dimensions;
    private Point centerPoint;
    
    
    /**
      * This method is triggered when the size of the component (i.e. the 
      * Visualiser) is changed. <br>
      * 
      * Note that this event will be triggered by the Visualiser (the 
      * ComponentListener is attached to the Visualiser, NOT the viewport.)
      * (Note that we obviously can still access the viewport through this 
      * object's instance variable.)
      */
    public void componentResized( ComponentEvent evt )
    {
        if( ( centerPoint == null ) || ( dimensions == null ) )
        {
            centerPoint = getCenterPoint();
            dimensions = vis.getSize();
        }
        else
        {
            Dimension newDimensions = vis.getSize();
            
            /* Calculate the proportions
             * (compare new dimensions with old dimensions) */
            double widthProp = newDimensions.getWidth() / dimensions.getWidth();
            double heightProp = newDimensions.getHeight() / dimensions.getHeight();
            
            // Prop > 1 indicates that the newDimensions is BIGGER
            // oldDimensions * prop = newDimensions
            
            double xNew = centerPoint.x * widthProp;
            double yNew = centerPoint.y * heightProp;
            
            /* Set the position of the viewport */
            Point newCenter = new Point( (int)xNew, (int)yNew );
            centerAt( newCenter );
            centerPoint = newCenter;
            
            /* Update the variables */
            dimensions = newDimensions;
        }
    }
    
    
    /**
      * This method is triggered when the viewport of this JScrollPane is
      * moved. This is used for updating the new centerPoint values.<br>
      * (The user moving the JScrollPane's ScrollBars means that the viewport 
      * will be positioned over a different part of the component.) 
      */
    public void adjustmentValueChanged( AdjustmentEvent evt )
    {
        centerPoint = getCenterPoint();
    }
    
    
    public void componentHidden( ComponentEvent evt ) {}
    public void componentMoved( ComponentEvent evt ) {}
    public void componentShown( ComponentEvent evt ) {}
    
    
    /**
      * This method will return the Point that is at the center of the viewport
      * over the component. In other words, the point returned is the point given 
      * by the location on the component (i.e. the returned coordinate is in terms 
      * of the component) that is at the center of the viewport's current position.
      */
    private Point getCenterPoint()
    {
        Point p = new Point( getViewport().getViewPosition() );    // The top left location over the Visualiser component (cloned)
        
        int vpWidth = getViewport().getWidth();
        int vpHeight = getViewport().getWidth();
        
        // Get center
        p.x += vpWidth / 2;
        p.y += vpHeight / 2;
        
        return p;
    }
    
    
    /**
      * This is a method to center the scrollpane about a given point on the component.
      * I.e. this method will try to position the viewport over the component such that
      * the center of the viewport is at the given point. This is most useful for
      * positioning over a component that is larger than the viewport. If the 
      * component is smaller than the viewport, then this method has no effect.<br>
      * Note: The JScrollPane is positioned in terms of the top left viewable point
      * (not the center), thus translation is carried out.
      */ 
    public void centerAt( Point p )
    {
        // Get scroll bars
        JScrollBar hor = getHorizontalScrollBar();
        JScrollBar ver = getVerticalScrollBar();
        
        // Calculate point in proportion to component size
        float xProp = (float)p.x / (float)vis.getWidth();
        float yProp = (float)p.y / (float)vis.getHeight();
        
        // Calculate the x and y coords to center at the given point
        int xDiff = hor.getMaximum() - getViewport().getWidth();
        int yDiff = ver.getMaximum() - getViewport().getHeight();
        hor.setValue( (int)(xDiff*xProp) );
        ver.setValue( (int)(yDiff*yProp) );
    }
    
    
    
    
    /* ********** CODE FOR THE DRAGGABLE FEATURE ********** */
    
    /* 
     * The following code adds the ability for the user to 'navigate' the viewable
     * area of the component by click-dragging (as an alternative to using scroll
     * bars.)
     */
    
    private Point componentPoint;
    
    
    public void mousePressed( MouseEvent  evt )
    {
        getViewport().setCursor( grabbed );
        
        Point viewportPoint = getViewport().getViewPosition();
        Point clickPoint = evt.getPoint();
        
        // Calculate point in relation to the component
        componentPoint = new Point();
        componentPoint.x = viewportPoint.x + clickPoint.x;
        componentPoint.y = viewportPoint.y + clickPoint.y;
    }
    
    
    public void mouseDragged( MouseEvent  evt )
    {
        Point p = evt.getPoint();
        
        // Calculate new point to place the corner
        int xNew = componentPoint.x - p.x;
        int yNew = componentPoint.y - p.y;
        
        // Keep component within bounds
        int maxWidth = vis.getWidth();
        int maxHeight = vis.getHeight();

        int width = getViewport().getWidth();
        int height = getViewport().getHeight();
        
        if( xNew < 0 )
            xNew = 0;
        
        if( yNew < 0 )
            yNew = 0;
        
        if( (xNew+width) > maxWidth )
            xNew = maxWidth - width;
            
        if( (yNew+height) > maxHeight )
            yNew = maxHeight - height;
        
        // Move the viewport's position accordingly
        Point pNew = new Point( xNew, yNew );
        getViewport().setViewPosition( pNew );
    }

    
    public void mouseReleased( MouseEvent  evt )
    {
        getViewport().setCursor( ungrabbed );
    }
    
    public void mouseClicked( MouseEvent  evt ) {}
    public void mouseEntered( MouseEvent  evt ) {}
    public void mouseExited( MouseEvent  evt ) {}
    public void mouseMoved( MouseEvent  evt ) {}

    
    







    
    
}