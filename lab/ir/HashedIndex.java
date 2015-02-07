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
	//If there is no entry for the docID
	if(!list.checkContains(docID)){
		PostingsEntry newEntry=new PostingsEntry(docID);
		newEntry.addOffset(offset);
		list.addEntry(newEntry);	
	}
	//Else if there already is a entry just add the offset
	else{
		list.addOffsetToLastEntry(offset); 
	}
	
	
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
    		
    	int nrQTerms=query.size();
    	    
    	    
    	if(queryType==Index.INTERSECTION_QUERY){
    	    
    		//Put the postingslist of the first term in res
    		PostingsList res=index.get(terms.getFirst());
    		
    		// i starts at 1 since the first postingslist is already in res
    		for(int i=1;i<nrQTerms;i++){
    			String term=terms.get(i);
    			res=this.intersect(res,index.get(term));
    		}
	
    		return res;
	}
	else if(queryType==Index.PHRASE_QUERY){
		
		PostingsList res=index.get(terms.getFirst());
		
		for(int i=1;i<nrQTerms;i++){
			String term=terms.get(i);
			res=this.phraseIntersect(res,index.get(term),1);
		}
		
		
		return res;
	}
	else{
		System.err.println("ERROR: QueryType not recognized or unimplemented");
		return null;
	}
    }
    
    private PostingsList intersect(PostingsList p1, PostingsList p2){
    	   
    	   //If either postingslist is null or empty return null
    	   if(p1==null || p2==null || p1.size()==0 || p2.size()==0){
    	   	return null;	   
    	   }
    	   
    	   //One iterator for each postingslist
    	   Iterator<PostingsEntry> it1=p1.iterator();
    	   Iterator<PostingsEntry> it2=p2.iterator();
    	  
    	   //Get the first position in each postingslist
    	   PostingsEntry curP1=it1.next();
    	   PostingsEntry curP2=it2.next();
    	   
    	   PostingsList res=new PostingsList();
    	   
    	   //This while returns if either it1 or it2 has no more entries
    	   while(true){
    	   	   if(curP1.equals(curP2)){
    	   	   	   PostingsEntry ent=new PostingsEntry(curP1.docID);
    	   	   	   //System.err.println("Match. DocID:"+curP1.docID);
    	   	   	   res.addEntry(ent);
    	   	   	   //If there is more entries left
    	   	   	   if(it1.hasNext() && it2.hasNext()){
    	   	   	   	   curP1=it1.next();
    	   	   	   	   curP2=it2.next();
    	   	   	   }
    	   	   	   else{
    	   	   	   	return res;	   
    	   	   	   }
    	   	   }
    	   	   else if(curP1.docID<curP2.docID){
    	   	   	if(it1.hasNext()){
    	   	   		curP1=it1.next();
    	   	   	}
    	   	   	else{
    	   	   		return res;
    	   	   	}
    	   	   }
    	   	   else{
    	   	   	if(it2.hasNext()){
    	   	   		curP2=it2.next();	
    	   	   	}
    	   	   	else{
    	   	   		return res;
    	   	   	}
    	   	   }
    	   }  
    }
    
    /**
    Function to intersect two postingslist in a Phrase Query. This means that 
    the postingslists has to be intersected in the correct order, i.e. the order
    of the words in the query.
    @param p1 The first PostingsList
    @param p2 The second PostingsList
    @param diff The difference in offset of the words between p1 and p2 (=1 in most cases)
    @return A PostingsList with entries for every matching entries in p1 and p2 where
    	the entry in p2 should be +diff in offset from the entry in p1
    */
    private PostingsList phraseIntersect(PostingsList p1, PostingsList p2, int diff){
    	   
    	   //If either postingslist is null or empty return null
    	   if(p1==null || p2==null || p1.size()==0 || p2.size()==0){
    	   	return null;	   
    	   }
    	   
    	   //One iterator for each postingslist
    	   Iterator<PostingsEntry> it1=p1.iterator();
    	   Iterator<PostingsEntry> it2=p2.iterator();
    	  
    	   //Get the first position in each postingslist
    	   PostingsEntry curP1=it1.next();
    	   PostingsEntry curP2=it2.next();
    	   
    	   PostingsList res=new PostingsList();
    	   
    	   //This while returns if either it1 or it2 has no more entries
    	   while(true){
    	   	   if(curP1.equals(curP2)){
    	   	   	   //If the docID is the same we retrieve the offsets
    	   	   	   Iterator<Integer> offsetsP1=curP1.getOffsets().iterator();
    	   	   	   Iterator<Integer> offsetsP2=null;
    	   	   	   
    	   	   	   //The offsets are ordered in increasing order
    	   	   	   int curOffsetP1;
    	   	   	   int curOffsetP2;
    	   	   	   PostingsEntry match=null;
    	   	   	   while(offsetsP1.hasNext()){
    	   	   	   	   curOffsetP1=offsetsP1.next();
    	   	   	   	   //We need to "reset" p2 every time
    	   	   	   	   offsetsP2=curP2.getOffsets().iterator();
    	   	   	   	   //For every offset in p1 check for a match in p2
    	   	   	   	   while(offsetsP2.hasNext()){
    	   	   	   	   	   curOffsetP2=offsetsP2.next();
    	   	   	   	   	   //If the current offset in p2 is more
    	   	   	   	   	   //than diff higher than the current
    	   	   	   	   	   //offset in p1 we can break the inner loop
    	   	   	   	   	   if(curOffsetP1+diff<curOffsetP2){
    	   	   	   	   	   	break;	   
    	   	   	   	   	   }
    	   	   	   	   	    if(curOffsetP1+diff==curOffsetP2){
    	   	   	   	   	   	//System.err.println("Match. DocID:"+curP1.docID+" offset:"+curOffsetP2);
    	   	   	   	   	   	//Match
    	   	   	   	   	   	if(match==null){
    	   	   	   	   	   		match=new PostingsEntry(curP1.docID);
    	   	   	   	   	   	}
    	   	   	   	   	   	match.addOffset(curOffsetP2);
    	   	   	   	   	   }
    	   	   	   	   	   
    	   	   	   	   }   
    	   	   	   }
    	   	   	   if(match!=null){
    	   	   	   	res.addEntry(match);	   
    	   	   	   }   
    	   	   	   
    	   	   	   //If there is more entries left
    	   	   	   if(it1.hasNext() && it2.hasNext()){
    	   	   	   	   curP1=it1.next();
    	   	   	   	   curP2=it2.next();
    	   	   	   }
    	   	   	   else{
    	   	   	   	return res;	   
    	   	   	   }
    	   	   }
    	   	   else if(curP1.docID<curP2.docID){
    	   	   	if(it1.hasNext()){
    	   	   		curP1=it1.next();
    	   	   	}
    	   	   	else{
    	   	   		return res;
    	   	   	}
    	   	   }
    	   	   else{
    	   	   	if(it2.hasNext()){
    	   	   		curP2=it2.next();	
    	   	   	}
    	   	   	else{
    	   	   		return res;
    	   	   	}
    	   	   }
    	   } 
    	   
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
