/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;

import java.util.*;

     
/** 
  * This class handles storing a collection of Artifacts. <br>
  * Internally, the collection is stored as a list and this class ensures that
  * the list is sorted by artifact ID, thus allowing binarySearch to be used to
  * support insertion, removal and searching. <br>
  * Two Artifacts with the same resource ID are not allowed in the collection
  * (i.e. all Artifacts in the collection have unique resource IDs). <br>
  * <br>
  * An important feature of this class is that it also has a number of methods to
  * aid in the tasks necessary for handling Artifacts in the mobile communication system.
  */
public final class ArtifactContainer implements Iterable<Artifact>
{
    private List<Artifact> list;
    
    
    
    
    /**
      * Constructor for an ArtifactContainer (initially empty).
      */
    public ArtifactContainer()
    {
        list = new LinkedList<Artifact>();
    }
    
    
    
    
    /* ACCESSORS, MUTATORS, GENERIC JAVA METHODS */
    
    /**
      * This method returns an iterator over this ArtifactContainer's Artifacts. <br>
      * This allows the use of the Java foreach loop with ArtifactContainers.
      */
    public Iterator<Artifact> iterator()
    {
        return list.iterator();
    }
    
    
    /**
      * Returns a string representation of this ArtifactContainer.
      */
    public String toString()
    {
        return list.toString();
    }
    
    
    
    
    /* CONTAINER FUNCTIONALITY METHODS */
    
    /**
      * This method handles adding the Artifact newArt to the container. 
      * Note that it is illegal to add an Artifact which is older or the same age
      * as an existing Artifact (given that both have the same resource ID). <br>
      * If the Artifact being added is newer than an Artifact already in the
      * container (given that both have the same resource ID), then older Artifact
      * is replaced with the new one. <br>
      * <br>
      * Note that the Artifact object is NOT copied.
      */
    public void add( Artifact newArt )
    {
        int index = Collections.binarySearch( list, newArt );
        
        /* Artifact with same resource ID did not exist in the container... */
        if( index < 0 )
            list.add( -(index)-1, newArt );
        else
        {
            /* Otherwise, an Artifact with same resource ID was found in the container... */
            Artifact oldArt = list.get( index );
            
            if( newArt.getAge() >= oldArt.getAge() )
                throw new ArtifactException( "Artifact being added is older or same age as the one it's replacing (Existing Artifact: " + oldArt + ") (Artifact to be added: " + newArt + ")" );
            
            list.set( index, newArt );
        }
    }
    
    
    /**
      * This method is the same as add( Artifact newArt ), except that the Artifact
      * is copied and added (thus this will NOT result in another reference to the
      * same Artifact object).
      *
      * @see #add
      */
    public void addCopy( Artifact newArt )
    {
        add( newArt.clone() );
    }
    
    
    /**
      * This method will add a COPY of each Artifact in the given List to this ArtifactContainer.
      * The addCopy method is used to handle adding each individual Artifact.
      * There is not prerequisite ordering required of the input list. The addCopy
      * method will handles placing the Artifact at the right location in the container.
      *
      *@see #addCopy
      */
    public void copyAll( List<Artifact> l )
    {
        for( Artifact a : l )
            addCopy( a );
    }
    
    
    /**
      * For the given ArtifactContainer cont, this method will return a list of all
      * the Artifacts in b that are: <br>
      *  -  not in this ArtifactContainer, or <br>
      *  -  are newer (more recent) than the corresponding Artifact in this ArtifactContainer <br>
      * <br>
      * Basically, this will give a list of all of the Artifacts that should be transferred
      * from cont to this. <br>
      * <br>
      * Also, the following code: <br>
      * <code>cont1.copyAll( cont1.transferFrom( cont2 ) )</code> <br>
      * will result in cont1 being updated to contain all of the Artifacts that need
      * to be transferred from cont2.
      * 
      * @param cont a container whose artifacts may be transferred to this artifact container
      * @see #copyAll(List)
      */
    public List<Artifact> transferFrom( ArtifactContainer cont )
    {
        List<Artifact> l = new LinkedList<Artifact>();
        
        for( Artifact art : cont )
        {
            // Get the Artifact with the same resource ID in this ArtifactContainer
            Artifact thisArt = this.findByRID( art );
            
            // This ArtifactContainer does not have an Artifact of the same resource ID...
            if( thisArt == null )
                l.add( art );
            else
            {
                // This ArtifactContainer has an Artifact (of same RID) that is out of date...
                if( thisArt.getAge() > art.getAge() ) 
                    l.add( art );
            }
        }
        
        return l;
    }
    
    
    /**
      * This method is an accessor to get the list of all the Artifacts stored in
      * this ArtifactContainer.
      */
    public List<Artifact> list()
    {
        return list;
    }
    
    
    /**
      * This will find the Artifact in this ArtifactContainer which has the
      * same resource ID as Artifact art. <br>
      * If there is no Artifact with the same resource ID, this will return null.
      */
    private Artifact findByRID( Artifact art )
    {
        int index = Collections.binarySearch( list, art );
        
        if( index < 0 )
            return null;
        else
            return list.get( index );
    }
    
    
    
    
    
    
    
    //
    
}
