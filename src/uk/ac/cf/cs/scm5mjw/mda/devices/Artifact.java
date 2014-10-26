/*   Matthew Williams (0515328)   */

package uk.ac.cf.cs.scm5mjw.mda.devices;
     
/** 
  * This class is for representing an artifact in the communication system. <br>
  * An artifact is a report on a particular resource at a given time (i.e. it is
  * time dependent). <br>
  *  <br>
  * The age of an artifact is the time since it was generated. <br>
  * An artifact also has a resource ID which identifies the resource the artifact
  * corresponds to. <br>
  * <br>
  * An Artifact's resource ID is immutable, but its age is mutable. This means that
  * great care must be taken when handling these Artifacts. Having multiple references
  * to the same Artifact object may result in undesirable behaviour..
  */
public final class Artifact implements Comparable<Artifact>, Cloneable
{
    private double age;
    private int resourceID;
    
    
    
    
    /**
      * This constructor sets up a complete artifact (including the age).
      */
    public Artifact( int resourceID, double age )
    {
        if( resourceID < 1 )
            throw new ArtifactException( "Artifact resource ID must be > 0" );
        
        if( age < 0 )
            throw new ArtifactException( "Artifact age must be >= 0" );
        
        this.resourceID = resourceID;
        this.age = age;
    }
    
    
    /**
      * This constructor sets up a brand new Artifact (has age 0).
      */
    public Artifact( int resourceID )
    {
        this( resourceID, 0 );
    }
    
    
    
    
    /* ACCESSORS AND MUTATORS */
    
    /**
      * Accessor for this artifact's resource ID.
      */
    public int getResourceID()
    {
        return resourceID;
    }
    
    
    /**
      * Accessor for this artifact's age.
      */
    public double getAge()
    {
        return age;
    }
    
    
    /**
      * A mutator which will advance this Artifact's age by the given increment
      */
    public void advanceAge( double inc )
    {
        age = age + inc;
    }
    
    
    /* OTHER GENERIC JAVA METHODS */
    
    /**
      * Creates a copy of this Artifact. The copied Artifact has the same attributes
      * (age and resource ID) as this one, but will be a separate object.
      */
    public Artifact clone()
    {
        return new Artifact( resourceID, age );
    }
    
    
    /**
      * Compares the given object with this artifact for equality. <br>
      * This artifact and the object are equal if: <br>
      *  - the object is also an artifact, and <br>
      *  - their ages are equal, and <br>
      *  - their resource IDs are equal
      */
    public boolean equals( Object obj )
    {
        if( obj instanceof Artifact )
        {
            Artifact art = (Artifact)obj;
            return (art.age == this.age) && (art.resourceID == this.resourceID);
        }
        
        return false;
    }
    
    
    /**
      * Compares the given artifact with this artifact for order. <br>
      * Note that this ordering is solely based on resource ID. The age of an
      * Artifact is not taken into account. (Two Artifcats of different age but
      * same resource ID are still equal) <br>
      * This is consistent with the equals( Object obj) method. <br>
      * <br>
      * A return value of 0 indicates equality. Otherwise, the sign of the number
      * determines the relationship between this object and the inputted object: <br>
      *  * Negative indicates: <br>
      *  - - THIS is less than ART <br>
      *  - - (THIS object is before ART in order) <br>
      *  * Positive indicates: <br>
      *  - -  THIS is greater than ART <br>
      *  - -  (THIS object is after ART in order)
      */
    public int compareTo( Artifact art )
    {
        int idDiff = this.resourceID - art.resourceID;
        return idDiff;
    }
    
    
    /**
      * This method must be overriden as a requirement of overriding equals.
      * The hashcode of an artifact is simply the concatenation of the numerical
      * values of its resourceID and age. The age is rounded to give
      * an integer. <br>
      * <br>
      * Note that very large ages will require more digits than a int primitive
      * can supply. Thus, the resulting hash code in such cases may not resemble
      * a concatenation due to type conversion. Even if this does happen, the hash code
      * still meets Java's requirements for a hash code.
      */
    public int hashCode()
    {
        // hashAge is the age modified to make it suitable for concatenation
        double hashAge = (double)Math.round( Math.abs(age) );
        
        // Handle the case that the age is 0
        int shiftNum;
        if( hashAge != 0 )
            shiftNum = 1 + (int)( Math.log(hashAge) / Math.log(10) );
        else
            shiftNum = 1;
        
        int shiftedRID = resourceID * (int)Math.pow(10, shiftNum);
        return shiftedRID + (int)hashAge;
    }
    
    
    /**
      * Get a string representation of this Artifact.
      */
    public String toString()
    {
        return "<Artifact> Resource ID: " + resourceID + ", Age: " + age;
    }
}
