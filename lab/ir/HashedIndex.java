/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

    
    public HashedIndex(){
    }
    

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
	PostingsList list=index.get(token);
	if(list==null){
		list=new PostingsList();
		index.put(token,list);
	}
	PostingsEntry newEntry=new PostingsEntry(docID);
	list.addEntry(newEntry);
	
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
	Set<String> keySet=index.keySet();
	Iterator<String> it=keySet.iterator();
	return it;
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	PostingsList list=index.get(token);
	return list;
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
	LinkedList<String> terms=query.terms;
	
	String term=terms.getFirst();
	
	return index.get(term);
	
	//return null;
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
