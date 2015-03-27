/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.io.File;

public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    private double alpha=1;
    private double beta=0.5;
    
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	StringTokenizer tok = new StringTokenizer( queryString );
	while ( tok.hasMoreTokens() ) {
	    terms.add( tok.nextToken() );
	    weights.add( new Double(1) );
	}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
	Query queryCopy = new Query();
	queryCopy.terms = (LinkedList<String>) terms.clone();
	queryCopy.weights = (LinkedList<Double>) weights.clone();
	return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
	// results contain the ranked list from the current search
	// docIsRelevant contains the users feedback on which of the 10 first hits are relevant
	
	HashMap<String,Double> termScores=new HashMap<String,Double>();
	
	for(int i=0;i<terms.size();i++){
		String term=terms.get(i);
		double curWeight=weights.get(i);
		double newScore=this.alpha*curWeight;
		termScores.put(term,newScore);
	}
	//First calculate the number of relevant documents
	int nrOfRelevantDocs=0;
	for(int i=0;i<docIsRelevant.length;i++){
		if(docIsRelevant[i]){
			nrOfRelevantDocs++;
		}
	}
	
	int nrDocsInCorpus=indexer.index.docIDs.keySet().size();
	
	if(nrOfRelevantDocs>0){
		//Go through the relevance list
		for(int i=0;i<docIsRelevant.length;i++){
			//If the document is relevant
			if(docIsRelevant[i]){
				//Get the info about the file
				PostingsEntry ent=results.get(i);
				int docID=ent.docID;
				String docPath=indexer.index.docIDs.get(""+docID);
				//Read the file
				//HardCoded to the davisWiki structure...
				String dirName=docPath.substring(0,9);
				String docName=docPath.substring(10);
				
				//System.err.println("dirName:"+dirName+" docName:"+docName);
				
				File fileDir=new File(dirName);
				File f=new File(fileDir,docName);
				String fileContent=Indexer.processFile(f);
				StringTokenizer tok=new StringTokenizer(fileContent);
				while ( tok.hasMoreTokens() ) {
					String term=tok.nextToken();
					PostingsList curList=indexer.index.getPostings(term);
					int dft=curList.size();
					double termIDF=Math.log((double)nrDocsInCorpus/(double)dft);
					
					
					Iterator<PostingsEntry> it=curList.iterator();
					PostingsEntry curEnt=null;
					//Iterate and find the entry belonging to the document we are looking at
					while(it.hasNext()){
						curEnt=it.next();
						//When we find the correct entry
						if(curEnt.docID==docID){
							//Calculate the tf_idf score for the term
							double tf=(double)curEnt.getOffsets().size();
							double tf_idf=tf*termIDF;
							Integer docLength=indexer.index.docLengths.get(""+docID);
							double finalScore=tf_idf/(double)docLength;
							
							//Multiply with beta and divide by number of relevant
							finalScore=(finalScore*beta)/(double)nrOfRelevantDocs;
							
							Double prevScore=termScores.get(term);
							if(prevScore==null){
								termScores.put(term,finalScore);
							}
							else{
								Double newScore=prevScore+finalScore;
								termScores.put(term,newScore);
							}
							//break the while loop (since we found the correct doc)
							break;
						}
					}
				} 
			}
		}
	//Clear the old term and weight lists
	terms.clear();
	weights.clear();
	//Then add the new terms and their weights
	//We also have to normalize the weights
	Set<String> newTerms=termScores.keySet();
	int nrOfTerms=newTerms.size();
	for(String newTerm:newTerms){
		double termScore=termScores.get(newTerm);
		//Normalize it (divide by #Terms in query)
		termScore=termScore/(double)nrOfTerms;
		//Add the term and the weight
		terms.add(newTerm);
		weights.add(termScore);
	}
		
	}
	

    }
}

    
