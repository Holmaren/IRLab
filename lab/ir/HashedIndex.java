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
import java.lang.Math;
import java.io.*;



/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>(10000);

    private int filesProcessedSinceClear=0;
    private int maxFilesProcessedBeforeClear=10000;
    private int lastDocID=-1;
    
    private String filePrefix="index";
    
    private String indexInfoDirectory="indexInfo";
    private String docNamesFile="IndexDocNames.txt";
    private String docLengthFile="IndexDocLength.txt";
    
    private HashMap<Integer,Double> pageRanks;
    private HashMap<String,Integer> pageRankNamesToDocID;
    
    
    public HashedIndex(){
    	    pageRanks=this.readPageRankFromFile();
    	    
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
    			System.err.println("Writing to files");
    		
    			//Write the current index to files
    			this.writeCurrentIndexToFiles();
    			//Clear the index
    			index.clear();
    			this.docIDs.clear();
    			this.docLengths.clear();
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
		newEntry.setDocName(this.docIDs.get(""+docID));
		//System.err.println(this.docIDs.get(""+docID));
		newEntry.addOffset(offset);
		list.addEntry(newEntry);	
	}
	//Else if there already is a entry just add the offset
	else{
		list.addOffsetToLastEntry(offset); 
	}
	
    }
    
    public void flushIndex(){
    
    	System.err.println("Flushing the current index");
    	    
	this.writeCurrentIndexToFiles();
	index.clear();
	this.docIDs.clear();
    	this.docLengths.clear();
    	    
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
    	    
    	File indexDir=new File(filePrefix);
    	    
    	if(queryType==Index.INTERSECTION_QUERY){
    	    
    		//If the index is saved on disk
    		if(SearchGUI.saveIndex){
    			
    			ArrayList<PostingsList> lists=new ArrayList<PostingsList>();
    			
    			for(int i=0;i<nrQTerms;i++){
    				String curTerm=terms.get(i);
    				try{
    					//If the filename is too large
    					String fileName=curTerm;
    					if(curTerm.length()>100){
    						fileName=fileName.substring(0,100);
    					}
    					File file=new File(indexDir,fileName);
    					if(file.exists()){
    						ObjectInputStream oins=new ObjectInputStream(new FileInputStream(file));
    						IndexEntry ent=(IndexEntry) oins.readObject();
    						if(!ent.getTerm().equals(curTerm)){
    							System.err.println("ERROR: The terms doesn't match");	
    							//System.err.println("Ent.getTerm="+ent.getTerm());
    							//System.err.println("CurTerm="+curTerm);
    						}
    						lists.add(ent.getList());
    						oins.close();
    					}
    					else{
    						//if atleast one term doesn't exist
    						return null;
    					}
    				}
    				catch(Exception ex){
    					ex.printStackTrace();	
    				}	
    			}
    			
    			if(lists.size()==0){
    				return null;	
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
		
		if(SearchGUI.saveIndex){
			ArrayList<PostingsList> lists=new ArrayList<PostingsList>();
    			
    			for(int i=0;i<nrQTerms;i++){
    				String curTerm=terms.get(i);
    				try{
    					//If the filename is too large
    					String fileName=curTerm;
    					if(curTerm.length()>100){
    						fileName=fileName.substring(0,100);
    					}
    					File file=new File(indexDir,fileName);
    					if(file.exists()){
    						ObjectInputStream oins=new ObjectInputStream(new FileInputStream(file));
    						IndexEntry ent=(IndexEntry) oins.readObject();
    						if(!ent.getTerm().equals(curTerm)){
    							System.err.println("ERROR: The terms doesn't match");	
    							//System.err.println("Ent.getTerm="+ent.getTerm());
    							//System.err.println("CurTerm="+curTerm);
    						}
    						lists.add(ent.getList());
    						oins.close();
    					}
    					else{
    						//if atleast one term doesn't exist
    						return null;	
    					}
    				}
    				catch(Exception ex){
    					ex.printStackTrace();	
    				}	
    			}
    			
    			if(lists.size()==0){
    				return null;	
    			}
    			
    			PostingsList res=lists.get(0);
    			
    			for(int i=1;i<nrQTerms;i++){
    				res=this.phraseIntersect(res,lists.get(i),1);	
    			}
    			
    			return res;
			
		}
		else{	
			PostingsList res=index.get(terms.getFirst());
		
			for(int i=1;i<nrQTerms;i++){
				String term=terms.get(i);
				res=this.phraseIntersect(res,index.get(term),1);
			}
		
		
			return res;
		}
	}
	else if(queryType==Index.RANKED_QUERY){
		if(rankingType==Index.TF_IDF){
			PostingsList res=this.fastCosineScore(query,0);
			return res;
		}
		else if(rankingType==Index.PAGERANK){
			PostingsList res=this.pageRankQuery(query);
			return res;
		}
		else if(rankingType==Index.COMBINATION){
			PostingsList res=this.CombinationRank(query,0);
			return res;
		}
		else{
			System.err.println("ERROR: Unknown rankingType");
			return null;
		}
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
    	   	   	   ent.setDocName(curP1.docName);
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
    	   	   	   	   	   		match.setDocName(curP1.docName);
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
    	    try{
    	    	    File indexDir=new File(filePrefix);
    	    	    if(!indexDir.exists()){
    	    	    	indexDir.mkdir();	    
    	    	    }
    	    	    
    	    	    while(keySet.hasNext()){
    	    
    	    	    	    String curKey=keySet.next();
    	    	    	    PostingsList curList=index.get(curKey);
    	    	    	    PostingsList toWrite=null;
    	    	     
    	    	    	    //Create a file object
    	    	    	    //If the filename is too large
    	    	    	    String fileName=curKey;
    	    	    	    if(curKey.length()>100){
    	    	    	    	fileName=fileName.substring(0,100);
    	    	    	    }
    	    	    	    File curF=new File(indexDir,fileName);
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
    	    	    
    	    	    //Also has to save the docNames and the docLengths
    	    	    File infoDir=new File(indexDir,indexInfoDirectory);
    	    	    if(!infoDir.exists()){
    	    	    	infoDir.mkdir();    
    	    	    }
    	    	    File docNames=new File(infoDir,docNamesFile);
    	    	    
    	    	    PrintWriter namesOut= new PrintWriter(new BufferedWriter(new FileWriter(docNames,true)));
    	    	    
    	    	    Iterator<String> allDocIDs=this.docIDs.keySet().iterator();
    	    	    
    	    	    while(allDocIDs.hasNext()){
    	    	    
    	    	    	    String curDocID=allDocIDs.next();
    	    	    	    
    	    	    	    String writeString=curDocID+ " " + docIDs.get(curDocID);
    	    	    	    
    	    	    	    namesOut.println(writeString);
    	    	    	    
    	    	    }
    	    	    namesOut.close();
    	    	    
    	    	    File lenFile=new File(infoDir,docLengthFile);
    	    	    
    	    	    PrintWriter lenOut=new PrintWriter(new BufferedWriter(new FileWriter(lenFile,true)));
    	    	    
    	    	    Iterator<String> lenIt=this.docLengths.keySet().iterator();
    	    	    
    	    	    while(lenIt.hasNext()){
    	    	    
    	    	    	    String curDocID=lenIt.next();
    	    	    	    
    	    	    	    String writeString=curDocID + " " + docLengths.get(curDocID);
    	    	    	    
    	    	    	    lenOut.println(writeString);
    	    	    	    
    	    	    }
    	    	    lenOut.close();
    	    	    
    	    	    
    	    
    } catch(Exception e){
    	    	   System.err.println("Error writing index to file");
    	    	   e.printStackTrace();
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
    	Function to calculate the cosine score and return the top
    	K results
    */
    private PostingsList fastCosineScore(Query q, int K){
    	    
    	    HashMap<String,Double> scores=new HashMap<String,Double>();
    	    LinkedList<String> terms=q.terms;
    	    
    	    int nrDocsInCorpus=this.docIDs.keySet().size();
    	    
    	    //First add up all scores
    	    for(String term:terms){
    	    	
    	    	PostingsList curList=index.get(term);
    	    	int dft=curList.size();
    	    	double termIDF=Math.log((double)nrDocsInCorpus/(double)dft);
    	    	
    	    	//System.err.println("Term:"+term);
    	    	//System.err.println("dft:"+dft);
    	    	//System.err.println("TermIDF:"+termIDF);
    	    	
    	    	Iterator<PostingsEntry> it=curList.iterator();
    	    	PostingsEntry curEnt=null;
    	    	
    	    	while(it.hasNext()){
    	    	
    	    		curEnt=it.next();
    	    		double tf=(double)curEnt.getOffsets().size();
    	    		double tf_idf=tf*termIDF;
    	    		Double curScore=scores.get(""+curEnt.getDocID());
    	    		if(curScore==null){
    	    			curScore=tf_idf;	
    	    		}
    	    		else{
    	    			curScore=curScore+tf_idf;	
    	    		}
    	    		scores.put(""+curEnt.getDocID(),curScore);
    	    		
    	    	}
    	    	
    	    }
    	    //Postingslist to save all entries
    	    PostingsList finalList=new PostingsList();
    	    //Now we divide all scores by their docLength
    	    Iterator<String> keys=scores.keySet().iterator();
    	    while(keys.hasNext()){
    	    
    	    	    String curKey=keys.next();
    	    	    Integer docLength=this.docLengths.get(curKey);
    	    	    Double curScore=scores.get(curKey);
    	    	    int docID=Integer.parseInt(curKey);
    	    	    double finalScore=curScore/(double)docLength;
    	    	    PostingsEntry ent=new PostingsEntry(docID);
    	    	    ent.setScore(finalScore);
    	    	    finalList.addEntry(ent);
    	    }
    	    
    	    //When all entries are added we sort the finalList and pick out
    	    //the top K entries
    	    finalList.sortPostingsList();
    	    if(finalList.size()<K || K==0){
    	    	return finalList;	    
    	    }
    	    PostingsList retList=new PostingsList();
    	    for(int i=0;i<K;i++){
    	    	retList.addEntry(finalList.get(i));	    
    	    }
    	    return retList;
    }
    

    private HashMap<Integer,Double> readPageRankFromFile(){
    
    	//System.err.println("Read PageRanks from file");
    	    
	String fileName="DavisPageRankText";
	String dirName="pagerank";
	String titlesFileName="articleTitles.txt";
	
	File pagerankDir=new File(dirName);
	File davisPageRank=new File(pagerankDir,fileName);
	File nameFile=new File(pagerankDir,titlesFileName);
	
	HashMap<Integer,Double> pageRank=new HashMap<Integer,Double>();
	
	pageRankNamesToDocID=new HashMap<String,Integer>();
	
	try{
	
		BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(davisPageRank)));
    		
		String line=in.readLine();
		while(line!=null){
		
			String[] parts=line.split(" ");
			Integer docID=Integer.parseInt(parts[0]);
			Double rank=Double.parseDouble(parts[1]);
			
			pageRank.put(docID,rank);
			line=in.readLine();
		}
		
		
    		in.close();
    		
    		in=new BufferedReader(new InputStreamReader(new FileInputStream(nameFile)));
    		
    		line=in.readLine();
    		while(line!=null){
		
			String[] parts=line.split(";");
			Integer docID=Integer.parseInt(parts[0]);
			String name=parts[1];
			
			pageRankNamesToDocID.put(name,docID);
			line=in.readLine();
		}
    		
    		
    		
    		return pageRank;
    		
    	}catch(Exception e){
    	    	   System.err.println("Error reading PageRank from file");
    	    	   e.printStackTrace();
    	    	    }
    	    
    	return null;
    	
    }
    
    /**
    	Function to calculate the cosine score, combine it with PageRank
    	and return the top K results
    */
    private PostingsList CombinationRank(Query q, int K){
    	    
    	    HashMap<String,Double> scores=new HashMap<String,Double>();
    	    LinkedList<String> terms=q.terms;
    	    
    	    int nrDocsInCorpus=this.docIDs.keySet().size();
    	    
    	    //First add up all scores
    	    for(String term:terms){
    	    	
    	    	PostingsList curList=index.get(term);
    	    	if(curList==null){
    	    		System.err.println("Error: PosingsList is empty. Term:"+term);	
    	    	}
    	    	int dft=curList.size();
    	    	double termIDF=Math.log((double)nrDocsInCorpus/(double)dft);
    	    	
    	    	//System.err.println("Term:"+term);
    	    	//System.err.println("dft:"+dft);
    	    	//System.err.println("TermIDF:"+termIDF);
    	    	
    	    	Iterator<PostingsEntry> it=curList.iterator();
    	    	PostingsEntry curEnt=null;
    	    	
    	    	while(it.hasNext()){
    	    	
    	    		curEnt=it.next();
    	    		double tf=(double)curEnt.getOffsets().size();
    	    		double tf_idf=tf*termIDF;
    	    		Double curScore=scores.get(""+curEnt.getDocID());
    	    		if(curScore==null){
    	    			curScore=tf_idf;	
    	    		}
    	    		else{
    	    			curScore=curScore+tf_idf;	
    	    		}
    	    		scores.put(""+curEnt.getDocID(),curScore);
    	    		
    	    	}
    	    	
    	    }
    	    //Postingslist to save all entries
    	    PostingsList finalList=new PostingsList();
    	    //Now we divide all scores by their docLength
    	    Iterator<String> keys=scores.keySet().iterator();
    	    while(keys.hasNext()){
    	    
    	    	    String curKey=keys.next();
    	    	    Integer docLength=this.docLengths.get(curKey);
    	    	    Double curScore=scores.get(curKey);
    	    	    int docID=Integer.parseInt(curKey);
    	    	    double finalScore=curScore/(double)docLength;
    	    	    finalScore=pageRankTFIDFCombination(docID,finalScore);
    	    	    PostingsEntry ent=new PostingsEntry(docID);
    	    	    ent.setScore(finalScore);
    	    	    finalList.addEntry(ent);
    	    }
    	    
    	    //When all entries are added we sort the finalList and pick out
    	    //the top K entries
    	    finalList.sortPostingsList();
    	    if(finalList.size()<K || K==0){
    	    	return finalList;	    
    	    }
    	    PostingsList retList=new PostingsList();
    	    for(int i=0;i<K;i++){
    	    	retList.addEntry(finalList.get(i));	    
    	    }
    	    return retList;
    }
    
    private PostingsList pageRankQuery(Query query){
    	    
    	    HashMap<String,Double> scores=new HashMap<String,Double>();
    	    LinkedList<String> terms=query.terms;
    	    
    	    //First add up all scores
    	    for(String term:terms){
    	    	
    	    	PostingsList curList=index.get(term);
    	    	if(curList==null){
    	    		System.err.println("Error: PosingsList is empty. Term:"+term);	
    	    	}
    	    	
    	    	Iterator<PostingsEntry> it=curList.iterator();
    	    	PostingsEntry curEnt=null;
    	    	
    	    	while(it.hasNext()){
    	    		curEnt=it.next();
    	    		double curPageRank=this.getPageRank(curEnt.getDocID());
    	    		scores.put(""+curEnt.getDocID(),curPageRank);	
    	    	}
    	    	
    	    }
    	    //Postingslist to save all entries
    	    PostingsList finalList=new PostingsList();
    	    //Now we divide all scores by their docLength
    	    Iterator<String> keys=scores.keySet().iterator();
    	    while(keys.hasNext()){
    	    
    	    	    String curKey=keys.next();
    	    	    Integer docLength=this.docLengths.get(curKey);
    	    	    Double curPageRank=scores.get(curKey);
    	    	    int docID=Integer.parseInt(curKey);
    	    	    PostingsEntry ent=new PostingsEntry(docID);
    	    	    ent.setScore(curPageRank);
    	    	    finalList.addEntry(ent);
    	    }
    	    
    	    //When all entries are added we sort the finalList and pick out
    	    //the top K entries
    	    finalList.sortPostingsList();
    	    	
    	    return finalList;	    
    	    
    	    
    }

	/**
	Function to combine the TF_IDF score with the pageRank
	*/
    private double pageRankTFIDFCombination(int docID, double tfIDF){
    
    	    double pageRankInfluence=0.5;

    	    //First we need to get the correct pageRankDocID
    	    String docName=this.docIDs.get(""+docID);
    	    //String[] splitUp=docName.split("/");
    	    //String parsedDocName=splitUp[1].substring(0,splitUp[1].length()-2);
    	    String parsedDocName=docName.substring(10,docName.length()-2);
    	    //System.err.println("DocName "+parsedDocName);
    	    
    	    Integer pageRankDocID=this.pageRankNamesToDocID.get(parsedDocName);
    	    
    	    Double pageRank=pageRanks.get(pageRankDocID);
    	    if(pageRank==null){
    	    	System.err.println("PageRank is null for docID:"+docID+ " docName:"+this.docIDs.get(""+docID));	    
    	    	pageRank=0.0;
    	    }
    	    //Scale pageRank by 100 to get it in the same scale as the tf_idf
    	    pageRank=pageRank*100;
    	    
    	    double finalScore=tfIDF*(1-pageRankInfluence)+pageRank*pageRankInfluence;
    	    
    	    return finalScore;
    }
    
    /**
    	Function to get the pageRank for a specific documentID
    */
    private double getPageRank(int docID){

    	    //First we need to get the correct pageRankDocID
    	    String docName=this.docIDs.get(""+docID);
    	    //String[] splitUp=docName.split("/");
    	    //String parsedDocName=splitUp[1].substring(0,splitUp[1].length()-2);
    	    String parsedDocName=docName.substring(10,docName.length()-2);
    	    //System.err.println("DocName "+parsedDocName);
    	    
    	    Integer pageRankDocID=this.pageRankNamesToDocID.get(parsedDocName);
    	    
    	    Double pageRank=pageRanks.get(pageRankDocID);
    	    if(pageRank==null){
    	    	System.err.println("PageRank is null for docID:"+docID+ " docName:"+this.docIDs.get(""+docID));	    
    	    	pageRank=0.0;
    	    }

	    return pageRank;
    	    
    }
    	    

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
