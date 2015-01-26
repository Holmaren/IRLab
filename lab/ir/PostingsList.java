/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;
import java.util.Iterator;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();


    /**  Number of postings in this list  */
    public int size() {
	return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
	return list.get( i );
    }

    public PostingsList(){
    		    
    }
    
    public void addEntry(PostingsEntry newEntry){
    	    //The entries needs to be sorted according to docID, therefore
    	    //we need to find the right location for the entry
    	    /*Iterator<PostingsEntry> it=list.iterator();
    	    
    	    int pos=0;
    	    
    	    PostingsEntry temp=it.next();
    	    
    	    while(newEntry.docID>temp.docID){
    	    	pos++;	 
    	    	if(it.hasNext()){
    	    		temp=it.next();
    	    	}
    	    	else{
    	    		break;
    	    	}
    	    }*/
    	    //if(pos>=list.size()){
    	    
    	    if(list.size()>0){
    	    
    	    	    if(!this.checkContains(newEntry.docID)){
    	    		    list.add(newEntry);
    	    	}
    	    }
    	    else{
    	    	list.add(newEntry);	    
    	    }
    	    
    	    	    
    	    //}
    	    //else{
    	    	  //  list.add(pos,newEntry);
    	    //}
    }
    
    //Checks if the entry already exists in the list
    private boolean checkContains(int newdocID){
    	    int last=list.getLast().docID;
    	    if(newdocID>last){
    	    	return false;	    
    	    }
    	    if(newdocID==last){
    	    	    return true;
    	    }
    	    Iterator<PostingsEntry> it=list.iterator();
    	    
    	    while(it.hasNext()){
    	    	if(it.next().docID==newdocID){
    	    		return true;	
    	    	}
    	    }
    	    return false;
    }
    
    
    //
    //  YOUR CODE HERE
    //
}
	

			   
