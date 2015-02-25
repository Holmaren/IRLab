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
import java.lang.StringBuffer;
import java.util.Collections;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();

    public PostingsList(){
    		    
    }
    
    
    /**  Number of postings in this list  */
    public int size() {
	return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
	return list.get( i );
    }

    
    public void addEntry(PostingsEntry newEntry){
    	    //The entries needs to be sorted according to docID, therefore
    	    //we need to find the right location for the entry
    	 
    	    if(list.size()>0){
    	    	    if(!this.checkContains(newEntry.docID)){
    	    		    list.add(newEntry);
    	    	    }
    	    }
    	    else{
    	    	list.add(newEntry);	    
    	    }
    	   
    }
    
    public void addOffsetToLastEntry(int offset){
    	    list.getLast().addOffset(offset);
    }
    
    //Checks if the entry already exists in the list
    public boolean checkContains(int newdocID){
    	    
    	    if(this.list.size()==0){
    	    	return false;	    
    	    }
    	    
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
    
    public Iterator<PostingsEntry> iterator(){
    	return list.iterator();	    
    }
    
    public String toString(){
    	    
    	    StringBuffer buf=new StringBuffer();
    	    
    	    if(list.size()!=0){
    	    	    buf.append(list.get(0));
    	    	    for(int i=1;i<list.size();i++){
    	    		    buf.append("&");
    	    		    buf.append(list.get(i));
    	    	    }

    	    }
	    return buf.toString();
    }
    
    
    public void sortPostingsList(){
    
    	    Collections.sort(list);
    	    
    }
    
    //
    //  YOUR CODE HERE
    //
}
	

			   
