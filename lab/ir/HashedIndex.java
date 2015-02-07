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
import java.util.ArrayList;
import java.io.*;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>(10000);

    private int filesProcessedSinceClear=0;
    private int maxFilesProcessedBeforeClear=500;
    private int lastDocID=-1;
    
    private String filePrefix="index";
    
    
    public HashedIndex(){
    }
    

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
    	    
    	//If we should save the index to disk (Task 1.6 lab1)
    	if(SearchGUI.saveIndex){
    		
    		//If we have processed the maximum amount of files and this is a new file
    		if(filesProcessedSinceClear>maxFilesProcessedBeforeClear && 
    			lastDocID!=docID){
    		
    			//Write the current index to files
    			this.writeCurrentIndexToFiles();
    			//Clear the index
    			index.clear();
    			filesProcessedSinceClear=0;
    		
    			}
    		
		if(lastDocID!=docID){
			filesProcessedSinceClear++;	
		}
		
    		lastDocID=docID;
    		    	    
    	}
    	   
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
    	    
    		//If the index is saved on disk
    		if(SearchGUI.saveIndex){
    			
    			ArrayList<PostingsList> lists=new ArrayList<PostingsList>();
    			
    			for(int i=0;i<nrQTerms;i++){
    				String curTerm=terms.get(i);
    				try{
    					ObjectInputStream oins=new ObjectInputStream(new FileInputStream(curTerm));
    					IndexEntry ent=(IndexEntry) oins.readObject();
    					if(ent.getTerm()!=curTerm){
    					System.err.println("ERROR: The terms doesn't match");	
    					}
    					lists.add(ent.getList());
    					oins.close();
    				}
    				catch(Exception ex){
    					ex.printStackTrace();	
    				}
    				
    					
    			}
    			
    			PostingsList res=lists.get(0);
    			
    			for(int i=1;i<nrQTerms;i++){
    				res=this.intersect(res,lists.get(i));	
    			}
    			
    			return res;
    		}
    		else{
    		
    			//Put the postingslist of the first term in res
    			PostingsList res=index.get(terms.getFirst());
    		
    			// i starts at 1 since the first postingslist is already in res
    			for(int i=1;i<nrQTerms;i++){
    				String term=terms.get(i);
    				res=this.intersect(res,index.get(term));
    			}
	
    			return res;
    		}
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
    	Function to write the current Index to files
    */
    private void writeCurrentIndexToFiles(){
    	    
    	    Iterator<String> keySet=index.keySet().iterator();
    	    
    	    while(keySet.hasNext()){
    	    
    	    	    String curKey=keySet.next();
    	    	    PostingsList curList=index.get(curKey);
    	    	    PostingsList toWrite=null;
    	    	    
    	    	    try{
    	    	    	//Create a file object
    	    	    	File indexDir=new File(filePrefix);
    	    	    	File curF=new File(indexDir,curKey);
    	    	    	//Check if the file exists (in that case read the object in it)
    	    	   	 if(curF.exists()){
    	    	    	    	ObjectInputStream oins=new ObjectInputStream(new FileInputStream(curF));
    	    	    	   	 IndexEntry prevEntry=(IndexEntry) oins.readObject();
    	    	    	   	 oins.close();
    	    	    	    
    	    	    	   	 if(!prevEntry.getTerm().equals(curKey)){
    	    	    	    	    System.err.println("ERROR: The Keys does not match!");    
    	    	    		    }
    	    	    	    
    	    	    		    toWrite=HashedIndex.mergeLists(prevEntry.getList(),curList);
    	    	    	    
    	    	   	 }
    	    	    	else{
    	    	    	    toWrite=curList;	    
    	    	    	}
    	    	    
    	    	    	IndexEntry writeToFile=new IndexEntry(curKey,toWrite);
    	    	    
    	    	    	ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(curF));
    	    	    	oos.writeObject(writeToFile);
    	    	    	oos.flush();
    	    	    	oos.close();
    	    	    
    	    	    }
    	    	    catch(Exception e){
    	    	    	    System.err.println("Error writing index to file");
    	    	    	    e.printStackTrace();
    	    	    } 
    	    	    
    	    }    
    }
    
    /**
    	Function to merge two postingslists (docID should be sorted)
    */
    public static PostingsList mergeLists(PostingsList p1, PostingsList p2){

    	    PostingsList res=new PostingsList();
    	    
    	    //If the first entry in p2 has a bigger docID than the last in p1 we can just
    	    //add all elements in p2 to the end of p1
    	    if(p2.get(0).getDocID()>p1.get(p1.size()-1).getDocID()){
    	    	    Iterator<PostingsEntry> it=p2.iterator();
    	    	    res=p1;
    	    	    while(it.hasNext()){
    	    	    	PostingsEntry curEntry=it.next();
    	    	    	res.addEntry(curEntry);
    	    	    }
    	    }
    	    else if(p1.get(0).getDocID()>p2.get(p2.size()-1).getDocID()){
    	    	Iterator<PostingsEntry> it=p1.iterator();
    	    	res=p2;
    	    	while(it.hasNext()){
    	    	    	PostingsEntry curEntry=it.next();
    	    	    	res.addEntry(curEntry);
    	    	    }    
    	    }
    	    else{
    	    	
    	    	    if(p1.size()==0){
    	    	    	return p2;	    
    	    	    }
    	    	    else if(p2.size()==0){
    	    	    	return p1;	    
    	    	    }
    	    	
    	    	    
    	    	Iterator<PostingsEntry> it1=p1.iterator();
    	    	Iterator<PostingsEntry> it2=p2.iterator();
    	    	
    	    	PostingsEntry curP1=it1.next();
    	    	PostingsEntry curP2=it2.next();
    	    	
    	    	while(curP1!=null || curP2!=null){
    	    		//If iterator1 is empty
    	    		if(curP1==null){
    	    			res.addEntry(curP2);
    	    			if(it2.hasNext()){
    	    				curP2=it2.next();	
    	    			}
    	    			else{
    	    				curP2=null;
    	    			}
    	    		}
    	    		//If iterator 2 is empty
    	    		if(curP2==null){
    	    			res.addEntry(curP1);
    	    			if(it1.hasNext()){
    	    				curP1=it1.next();	
    	    			}
    	    			else{
    	    				curP1=null;	
    	    			}
    	    		}
    	    		else{
    	    			if(curP1.getDocID()<=curP2.getDocID()){
    	    				res.addEntry(curP1);
    	    				if(it1.hasNext()){
    	    					curP1=it1.next();	
    	    				}
    	    				else{
    	    					curP1=null;	
    	    				}
    	    				
    	    			}
    	    			else{
    	    				res.addEntry(curP2);
    	    				if(it2.hasNext()){
    	    					curP2=it2.next();	
    	    				}
    	    				else{
    	    					curP2=null;
    	    				}
    	    			}
    	    		}  		
    	    	}
    	    
	    }
	    
	    return res;
    	    
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
