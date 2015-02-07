/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.ArrayList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public ArrayList<Integer> offsets=null;
    public double score;

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
     
     public PostingsEntry(int docID){
    	    this.docID=docID;
    	    //this.score=score;
    }
    
    public void addOffset(int offset){
    	    if(this.offsets==null){
    	    	this.offsets=new ArrayList<Integer>();	    
    	    }
    	    this.offsets.add(offset);
    }
     
    public int compareTo( PostingsEntry other ) {
    	    return Double.compare( other.score, score );
    }
    
    public boolean equals(PostingsEntry other){
    	    return this.docID==other.docID;
    }

    
    public int getDocID(){
    	    return this.docID;
    }
    
    public ArrayList<Integer> getOffsets(){
    	return this.offsets;	    
    }


    //
    //  YOUR CODE HERE
    //

}

    
