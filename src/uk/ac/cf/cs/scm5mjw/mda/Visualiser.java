/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda;

import uk.ac.cf.cs.scm5mjw.mda.devices.*;
import uk.ac.cf.cs.scm5mjw.mda.mobility.*;

import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
     
/** 
  * This is class handles visualising a simulation. It is a SimulationListener 
  * which, when registered to a Simulator, will render the simulation
  * graphically. A Visualiser is a Java Swing component and should be added to 
  * a GUI. <br>
  * <br>
  * Note that an instance of a Visualiser should only ever be registered on one
  * Simulator.
  */
public final class Visualiser extends JComponent implements SimulationListener
{
    /* Constants / defaults */
    // General constants
    public static final double MARGIN_PERCENT = 0.2;
    public static final double DEFAULT_UPDATE_WAIT = 1;
    public static final double DEFAULT_PAUSE_FRACTION = 1;
    
    // Visualisation aesthetic constants
    public static final Color MOBILE_OBJECT_COLOUR = Color.RED;
    public static final double MOBILE_OBJECT_RADIUS = 2;
    
    public static final Color INFORMATION_SOURCE_COLOUR = Color.GRAY;
    public static final double INFORMATION_SOURCE_RADIUS = 2;
    
    public static final Color BEACON_COLOUR = Color.GREEN;
    public static final double BEACON_RADIUS = 2;
    
    public static final Color MAP_NODE_COLOUR = Color.BLUE;
    public static final double MAP_NODE_RADIUS = 1;
    
    public static final Color MAP_LINK_COLOUR = Color.BLUE;
    public static final Stroke DEFAULT_STROKE = new BasicStroke( 1.0f );
    public static final Color SESSION_COLOUR = Color.BLACK;
    public static final Stroke SESSION_STROKE = new BasicStroke( 1.5f );
    public static final DecimalFormat SESSION_TIME_FORMATTER = new DecimalFormat( "0.00" );
    
    public static final float FONT_SIZE = 10f;
    
    
    /* Instance variables */
    private Simulator sim;
    
    // The following variables are related to the visualisation updates
    private double updateWait;
    private double pauseFraction;
    
    // The following determine some of the aesthetic attributes of the visualisation
    private boolean showCommunicationRanges;
    private boolean showCommunicationSessions;
    private boolean showMap;
    
    // The following variables are changed during a given run of the Simulator
    private double simTimeSinceUpdate; // the amount of time in the SIMULATION that has elapsed since the last visulisation update
    
    // These are the minimum and maximum coordinates of all the devices that
    // will be visualised
    private double minX, maxX;
    private double minY, maxY;
    private double maxCommRange;
    
    // These values determine the area of the simulation which will be visualised
    private double viewMinX, viewMaxX;
    private double viewMinY, viewMaxY;
    
    // Zooming stuff...
    private double zoomFactor;
    
    
    
    
    /**
      * Construct a Visualiser with the given Simulator. <br>
      * Default values for the pause fraction and draw interval are used. These
      * may be changed by using the mutators for these attributes. <br>
      * <br>
      * The Simulator is needed for determining the size of the area
      * needed for the visualisation (based on the most distant MapNodes, Beacons,
      * and so on). <br>
      * The Simulator passed into this constructor should be the same as the one
      * that this Visualiser will be attached to. Otherwise, results are undefined.
      *  <br>
      *  <br>
      * The UPDATE WAIT duration is an amount of SIMULATION time (in seconds) that
      * will be elapsed between updating the visualisation. For example, a duration of
      * 2 seconds means that the visualisation will be updated once every 2 seconds of
      * timesteps have elapsed in the simulation. <br>
      * In the (likely) case that the wait duration is not exactly a whole number of
      * timesteps, the update will occur at the NEXT closest timestep. <br>
      * A duration of 0 means that the update wait will be equal to one simulation
      * timestep. <br>
      * <br>
      * PAUSE FRACTION is a fraction. This determines the amount of time that
      * the visualisation will pause by at each update. This is an artificial pause
      * in execution so that the visualisation can be slowed down or sped up.
      * The fraction gives us a fraction of the amount of time that has elapsed
      * in the simulator since the last update. <br>
      * For example, a pause fraction of 0.5 means that the visualisation's pauses
      * will be half the simulation time elapsed, thus speeding up the rate
      * of the visualisation. <br>
      * <br>
      * Note that none of these values will affect how the simulation runs. They
      * are solely for the purpose of manipulating the visualisation.
      */
    public Visualiser( Simulator inSim )
    {
        /* Initialise attributes */
        // Inputs
        sim = inSim;
        
        // Visualisation update stuff
        updateWait = DEFAULT_UPDATE_WAIT;
        pauseFraction = DEFAULT_PAUSE_FRACTION;
        simTimeSinceUpdate = 0;
        
        // Aesthetic stuff (defaults)
        showCommunicationRanges = false;
        showCommunicationSessions = false;
        showMap = true;
        
        zoomFactor = 1;
        
        
        /* Set up the visualisation */
        MobilityMap map = sim.getMap();
        
        if( map.isEmpty() )
            throw new UnsuitableMapException( "Visualiser needs simulator whose a map has at least one node" );
        
        
        /* Find the ACTUAL bounds values of the visualisation */
        /* (This is the x and y values for the distances in the ACTUAL
         * simulation)
         * (The view bounds are based on these actual values)
         * The following attributes are set:
         *     minX, maxX, minY, maxY,
         *     maxCommRange */
        // Initial minimum and maximums:
        MapNode n = map.getNodeAt(0);
        minX = maxX = n.getLocation().getX();
        minY = maxY = n.getLocation().getY();
        
        // Find the most distant: MapNode
        for( int i=1; i < map.getNumberOfNodes(); i++ )
        {
            double x = map.getNodeAt(i).getLocation().getX();
            double y = map.getNodeAt(i).getLocation().getY();
            
            if( x < minX )
                minX = x;
            
            if( x > maxX )
                maxX = x;
            
            if( y < minY )
                minY = y;
            
            if( y > maxY )
                maxY = y;
        }
        
        // Find the most distant: Beacon and Info Source
        findExtremes( sim.getBeacons() );
        findExtremes( sim.getInformationSources() );
        
        // Find the largest communication range of all the devices
        maxCommRange = 0;
        
        for( AbstractWirelessDevice dev : sim.getMobileObjects() )
            maxCommRange = Math.max( maxCommRange, dev.getCommunicationRange() );
            
        for( AbstractWirelessDevice dev : sim.getBeacons() )
            maxCommRange = Math.max( maxCommRange, dev.getCommunicationRange() );
            
        for( AbstractWirelessDevice dev : sim.getInformationSources() )
            maxCommRange = Math.max( maxCommRange, dev.getCommunicationRange() );
        
        
        /* Calculate the view bounds */
        updateViewBounds();
    }
    
    
    /**
      * This method calculates the viewMin/viewMax values for the visualisation,
      * based on the original bounds and the zoom factor. A margin (equal
      * to the largest communication range) is also added.<br>
      * The method also updates the size of this component<br>
      * <br>
      * Attributes updated:<br>
      * *    viewMinX, viewMaxX,<br>
      * *    viewMinY, viewMaxY<br>
      * <br>
      * Attributes used:<br>
      * *    maxX, minX, maxY, minY,<br>
      * *    maxCommRange,<br>
      * *    zoomFactor<br>
      * <br>
      * The maxX, minX, maxY, minY, maxCommRange values are assumed to have
      * already been calculated (i.e. calculated in the constructor).
      * <br>
      * The viewMin/viewMax values take into account the zoom factor.<br>
      * The viewMin/viewMax values give us a square whose area is equal to the
      * area of the zoomed visualisation. The viewMin values tell us how much
      * to shift the MAGNIFIED (i.e. multiplied) coordinates to put them in
      * view.
      */
    private void updateViewBounds()  
    {
        assert !sim.getMap().isEmpty() : "Visualiser needs simulator whose a map has at least one node"; 
        
        
        viewMinX = minX * zoomFactor;
        viewMinY = minY * zoomFactor;
        
        viewMaxX = maxX * zoomFactor;
        viewMaxY = maxY * zoomFactor;
        
        
        double marginLength = zoomFactor * maxCommRange;
        
        
        // Set the size of the component
        int prefWidth  = (int)Math.ceil( (viewMaxX - viewMinX) + (marginLength * 2) );
        int prefHeight = (int)Math.ceil( (viewMaxY - viewMinY) + (marginLength * 2) );
        setPreferredSize( new Dimension( prefWidth, prefHeight ) );
        
        
        // Adjust for margin lengths 
        viewMinX -= marginLength;
        viewMinY -= marginLength;
        
        viewMaxX += marginLength;
        viewMaxY += marginLength;
    }
    
    
    /**
      * This method will check the location of each item in an arbitrary collection
      * of AbstractWirelessDevices to see if their x and/or y values beat the
      * current minimum or maximum x and y values. If one of the minimum or maximums
      * is beaten, their value will be updated with the new most extreme value.
      */
    private void findExtremes( Vector<? extends AbstractWirelessDevice> coll )
    {
        for( AbstractWirelessDevice e: coll )
        {
            double x = e.getLocation().getX();
            double y = e.getLocation().getY();
            
            if( x < minX )
                minX = x;
            
            if( x > maxX )
                maxX = x;
            
            if( y < minY )
                minY = y;
            
            if( y > maxY )
                maxY = y;
        }
    }
    
    
    public void simulationFinished( SimulationEvent evt ) {}
    
    
    /**
      * This method handles the notification of beginning simulation. 
      */
    public void simulationStarted( SimulationEvent evt )
    {
        assert sim == evt.getSimulator();
        
        simTimeSinceUpdate = 0;
        repaint();
    }
    
    
    /**
      * This method handles the notification that the Simulator's timestep has
      * advanced.
      */
    public void simulationTimestepAdvanced( SimulationEvent evt )
    {
        assert sim == evt.getSimulator();
        
        
        simTimeSinceUpdate += sim.getTimestepLength();
        
        // Check if wait time for update has elapsed
        if( simTimeSinceUpdate >= updateWait )
        {
            // Update the visualisation
            repaint();
            
            // Wait for a fraction of the simulation of the elapsed time
            long millis = (long)Math.round( simTimeSinceUpdate * pauseFraction * 1000 ); 
            
                //millis = 0; //~ Setting millis=0 to bring out threadERS in drawCommunicationSession() 
            
            try
            {
                Thread.sleep( millis );
            }
            catch( InterruptedException ex ) {}
            
            simTimeSinceUpdate = 0;
        }
    }
    
    
    /**
      * This method overrides the component's painting method to carry out the
      * drawing of our visualisation.
      */
    public void paintComponent( Graphics g )
	{
        Graphics2D g2 = (Graphics2D)g;
        
        // Draw the map
        if( showMap )
            drawMap( g2 );
        
        // Draw mobile objects, beacons, and information sources
        drawAWDDevices( g2, sim.getMobileObjects(), MOBILE_OBJECT_COLOUR, MOBILE_OBJECT_RADIUS );
        drawAWDDevices( g2, sim.getBeacons(), BEACON_COLOUR, BEACON_RADIUS );
        drawAWDDevices( g2, sim.getInformationSources(), INFORMATION_SOURCE_COLOUR, INFORMATION_SOURCE_RADIUS );
        
        // Draw communication sessions
        if( showCommunicationSessions )
        {
            drawAWDSessions( g2, sim.getMobileObjects() );
            drawAWDSessions( g2, sim.getBeacons() );
            drawAWDSessions( g2, sim.getInformationSources() );
        }
    }
    
    
    /**
      * This method will draw the Simulator's map. This includes the map's nodes
      * and the links between these nodes.
      */
    private void drawMap( Graphics2D g2 )
    {
        MobilityMap map = sim.getMap();
        
        /* Draw map nodes and their links */
        int numNodes = map.getNumberOfNodes();
        
        for( int i=0; i < numNodes; i++ )
        {
            MapNode node = map.getNodeAt(i);
            Point2D.Double nodeLoc = node.getLocation();
            
            // Draw the node
            drawCircle( g2, nodeLoc, MAP_NODE_COLOUR, MAP_NODE_RADIUS, false );
            
            // Draw the node's links
            int numLinks = node.getNumberOfLinks();
            for( int j=0; j < numLinks; j++ )
            {
                MapNode destNode = node.getLinkAt(j).getGoesTo();
                Point2D.Double destNodeLoc = destNode.getLocation();
                drawLine( g2, nodeLoc, destNodeLoc, MAP_LINK_COLOUR );
            }
        }
    }
    
    
    /**
      * This method will draw an arbitrary collection of AbstractWirelessDevices.<br>
      * Note that this method also handles getting the communication range
      * for each device (this is relevant if the showCommunicationRanges is
      * true). This method will magnify the communication range according to the
      * zoom factor.
      */
    private void drawAWDDevices( Graphics2D g2, Vector<? extends AbstractWirelessDevice> coll, Color colour, double radius  )
    {
        for( AbstractWirelessDevice device: coll )
        {
            // Draw the device itself
            drawCircle( g2, device.getLocation(), colour, radius, true );
            
            // Draw the communication range (if desired)
            if( showCommunicationRanges )
            {
                double commRadius = device.getCommunicationRange() * zoomFactor;
                drawCircle( g2, device.getLocation(), colour, commRadius, false );
            }
        }
    }
    
    
    /**
      * This method will draw the communication sessions for each device
      * (that is communicating) in an arbitrary collection of devices.
      */
    private void drawAWDSessions( Graphics2D g2, Vector<? extends AbstractWirelessDevice> coll )
    {
        for( AbstractWirelessDevice dev : coll )
        {
            if( dev.isCommunicating() )
            {
                synchronized( dev ) //~
                {
                    drawCommunicationSession( g2, dev );
                }
            }
        }
    }
    
    
    /**
      * This method will draw (a visualisation of) the communication session
      * for a given device. <br>
      * Assumption: <br>
      *  - The inputted device is already communicating <br>
      * <br>
      * This will draw the given device's 'half' of the communication. That is, 
      * it will draw the time left for this device and half a the line between
      * this device and the other device.
      */
    private final void drawCommunicationSession( Graphics2D g2, AbstractWirelessDevice dev )
    {
        try
        {
            synchronized( dev ) //~
            {
                assert dev.isCommunicating();
                assert dev.getCommunicationPartner() != null;
                                                                                                                                                                                //~ pr fix...
                                                                                                                                                                                //~ assert ( dev.getCommunicationTimeRemaining() > 0.0 ) : "Phase: " + dev.getCommunicationPhase() + ", Time remaining: " + dev.getCommunicationTimeRemaining() + ", timeRemaining > 0: " + ( dev.getCommunicationTimeRemaining() > 0.0 );
                                                                                                                                                                                //~ Should be > or >= ?? order of simulation steps is that: decrement the amount of time left in d's communication phase; ...; THEN take action on state changes
                
                Point.Double p1 = dev.getLocation();
                AbstractWirelessDevice partner = dev.getCommunicationPartner();
                Point.Double p2 = partner.getLocation();
                double timeRem = dev.getCommunicationTimeRemaining();
                String timeRemStr = SESSION_TIME_FORMATTER.format( timeRem );
                
                // Find middle between the two devices
                double xMidDif = (p2.x - p1.x) / 2;
                double yMidDif = (p2.y - p1.y) / 2;
                Point.Double pMid = new Point.Double( p1.x + xMidDif, p1.y + yMidDif );
                
                drawLine( g2, p1, pMid, SESSION_COLOUR, SESSION_STROKE );
                drawString( g2, timeRemStr, p1, SESSION_COLOUR );
            }
        }
        catch( Exception ex )
        {
                                                                                                                                                                                                                        //~ !!! VERY !!! Poor programming here -- am using safety net for the weird errors...
            System.out.print( "-" ); //~ Printing out to indicate possible problems
            return;
        }
    }
    
    
    /* METHODS FOR ACTUAL DRAWING */
    
    /**
      * This method will draw a circle of given radius CENTERED at the given
      * point. The position of the circle will also be adjusted to moved it into
      * the correct location in the Visualiser's 'view'.<br>
      * It is important to note that while the POSITION of the circle (i.e. the
      * center of the circle) WILL be adjusted with the Visualsier's 'view', the
      * radius of the circle WILL NOT be adjusted. Magnifying the radius of the
      * circle is the responsibility of the calling method.
      */
    private void drawCircle( Graphics2D g2, Point2D.Double point, Color colour, double radius, boolean fill )
    {
        double x = translateX( point.getX() );     // Adjusted with the view
        double y = translateY( point.getY() );     // Adjusted with the view
        
        double diam = radius * 2;
        
        Ellipse2D.Double circle = new Ellipse2D.Double( x-(diam/2), y-(diam/2), diam, diam );
        
        g2.setColor( colour );
        
        if( fill )
            g2.fill( circle );
        else
            g2.draw( circle );
    }
    
    
    /**
      * This method will draw a line between two given points. The position of
      * the line will be adjusted to moved it into the correct location in the Visualiser's 'view'.
      */
    private void drawLine( Graphics2D g2, Point2D.Double p1, Point2D.Double p2, Color colour, Stroke strk )
    {
        double x1 = translateX( p1.getX() );
        double y1 = translateY( p1.getY() );
        
        double x2 = translateX( p2.getX() );
        double y2 = translateY( p2.getY() );
        
        Line2D.Double line = new Line2D.Double( x1, y1, x2, y2 );
        
        g2.setStroke( strk );
        
        g2.setColor( colour );
        g2.draw( line );
    }
    
    
    /**
      * This method overloads the complete drawLine so that the stroke may be
      * absent. In this case, the default stroke will be used.
      * 
      * @see #drawLine(Graphics2D, java.awt.geom.Point2D.Double, java.awt.geom.Point2D.Double, Color, Stroke)
      */
    private void drawLine( Graphics2D g2, Point2D.Double p1, Point2D.Double p2, Color colour )
    {
        drawLine( g2, p1, p2, colour, DEFAULT_STROKE );
    }
    
    
    /**
      * This method will draw a string at the given point. The position of
      * the string will be adjusted to moved it into the correct location in the
      * Visualiser's 'view'.
      */
    private void drawString( Graphics2D g2, String str, Point2D.Double p, Color colour )
    {
        double x = translateX( p.getX() );
        double y = translateY( p.getY() );
        
        Font f = g2.getFont().deriveFont( FONT_SIZE );
        g2.setFont( f );
        
        g2.setColor( colour );
        g2.drawString( str, (float)x, (float)y );
    }
    
    
    /**
      * This method handles translating a 'raw' coordinate to a coordinate
      * adjusted for display. This includes:<br>
      * * Flipping the coordinates<br>
      * * Zooming the coordinates
      */
    private double translateX( double xRaw )
    {
        double x = xRaw;
        
        // Zoom
        x = x * zoomFactor;
        
        // Shift
        x = x - viewMinX;
        
        // Center
        double cShift = (getWidth()/2) - ((viewMaxX-viewMinX)/2);
        x = x + cShift;
        
        return x;
    }
    
    
    /**
      * This method handles translating a 'raw' coordinate to a coordinate
      * adjusted for display. This includes:<br>
      * * Flipping the coordinates<br>
      * * Zooming the coordinates
      */
    private double translateY( double yRaw )
    {
        double y = yRaw;
        
        // Zoom
        y = y * zoomFactor;
        
        // Shift
        y = y - viewMinY;
        
        // Flip
        y = getHeight() - y;
        
        // Center
        double cShift = (getHeight()/2) - ((viewMaxY-viewMinY)/2);
        y = y - cShift;
        
        return y;
    }
    
    
    
    
    /* **** ACCESSORS AND MUTATORS **** */
    
    /**
      * Accessor for this Visualiser's update wait duration.
      */
    public double getUpdateWaitDuration()
    {
        return updateWait;
    }
    
    
    /**
      * Accessor for this Visualiser's pause fraction.
      */
    public double getPauseFraction()
    {
        return pauseFraction;
    }
    
    
    /**
      * Accessor for this Visualiser's Simulator.
      */
    public Simulator getSimulator()
    {
        return sim;
    }
    
    
    /**
      * Accessor for whether or not the communication ranges will be drawn
      * by this Visualiser.
      */
    public boolean getShowCommunicationRanges()
    {
        return showCommunicationRanges;
    }
    
    
    /**
      * Accessor for whether or not the communication sessions will be drawn
      * by this Visualiser.
      */
    public boolean getShowCommunicationSessions()
    {
        return showCommunicationSessions;
    }
    
    
    /**
      * Accessor for whether or not the map will be drawn by this Visualiser.
      */
    public boolean getShowMap()
    {
        return showMap;
    }
    
    
    /**
      * Accessor for the zoom factor for the visualisation.
      */
    public double getZoomFactor()
    {
        return zoomFactor;
    }
    
    
    /**
      * Mutator for the zoom factor for the visualisation.<br>
      * <br>
      * The effect of the zoom factor is to modify the distances 
      * between drawn 'things'. E.g. a zoom factor of 0.5 will essentially
      * result in the distance between two 'things' being halved.  
      */
    public void setZoomFactor( double inZoomFactor )
    {
        if( inZoomFactor <= 0 )
            throw new RuntimeException( "Zoom factor " + inZoomFactor + " is not > 0" );
        
        
        // Set zoom factor and update view bounds 
        zoomFactor = inZoomFactor;
        updateViewBounds();
        
        // Propagate the fact that the size has changed up the container hierarchy
        revalidate();
        
        // Repaint the visualisation now that the size and zoom factor have changed
        repaint();
    }
    
    
    /**
      * Mutator for this Visualiser's update wait duration.
      */
    public void setUpdateWaitDuration( double inUpdateWait )
    {
        updateWait = inUpdateWait;
    }
    
    
    /**
      * Mutator for this Visualiser's pause fraction.
      */
    public void setPauseFraction( double inPauseFraction)
    {
        pauseFraction = inPauseFraction;
    }
    
    
    /**
      * Mutator for whether or not the communication ranges will be drawn
      * by this Visualiser.
      */
    public void setShowCommunicationRanges( boolean show )
    {
        showCommunicationRanges = show;
    }
    
    
    /**
      * Mutator for whether or not communication sessions will be drawn by this
      * Visualiser.
      */
    public void setShowCommunicationSessions( boolean show )
    {
        showCommunicationSessions = show;
    }
    
    
    /**
      * Mutator for whether or not the map will be drawn by this Visualiser.
      */
    public void setShowMap( boolean show )
    {
        showMap = show;
    }
}



