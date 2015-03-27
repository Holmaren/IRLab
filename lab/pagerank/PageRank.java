/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;
import java.lang.Math;
import java.util.Random;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.00001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    //To save the matrix P
    private Hashtable<Integer,Hashtable<Integer,Double>> P= new Hashtable<Integer,Hashtable<Integer,Double>>();
    
    
    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	computePagerank( noOfDocs );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
		if ( out[i] == 0 )
		    numberOfSinks++;
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
	
	double[] res=new double[numberOfDocs];
	
	//res=this.powerIteration(numberOfDocs);
	//System.err.println("PowerIteration returned");
	
	ArrayList<PageRankEntry> powerItResults=this.readPageRankFromFile();
	
	boolean doTests=true;
	
	if(doTests){
	
	int numberOfTestIterations=10;
	
	double[] sumOfSquaredDiffTop=new double[numberOfTestIterations];
	
	double[] sumOfSquaredDiffBot=new double[numberOfTestIterations];
	
	int useMethod=5;
	
	int multiplyIBy=10;
	
	System.err.println("Using Monte-Carlo method number "+useMethod);
	
	for(int i=0;i<numberOfTestIterations;i++){
	
		//System.err.println("Iteration:"+(i+1));
		
		int nrIterations=numberOfDocs*multiplyIBy*(i+1);
		
		switch(useMethod){
		case 1:
			//Method1
			res=this.MCendPointRandStart(numberOfDocs,nrIterations);
			break;
		case 2:
			//Method2
			res=this.MCendPointCyclicStart(numberOfDocs,nrIterations);
			break;
		case 3:
			//Method 3
			res=this.MCcompletePath(numberOfDocs,nrIterations);
			break;
		case 4:
			//Method 4
			res=this.MCcompletePathStopDangling(numberOfDocs,nrIterations);
			break;
		case 5:
			//Method 5
			res=this.MCcompletePathStopDanglingRandStart(numberOfDocs,nrIterations);
			break;
		default:
			System.err.println("Incorrect value of MethodNr");
			System.exit(1);
			break;
		}
		
		//Printing top50
		//this.sortAndPrintTop50(res,numberOfDocs);
	
		//First calculate the sum of squared diff for the top 50
		double diffTop50=0;
		for(int a=0;a<50;a++){
			PageRankEntry ent=powerItResults.get(a);
			int thisID=this.docNumber.get(""+ent.docID);
			double exactRank=ent.pageRank;
		
			double estRank=res[thisID];
		
			diffTop50=diffTop50+Math.pow(exactRank-estRank,2);
		
		}
		//Save the difference for this iteration
		sumOfSquaredDiffTop[i]=diffTop50;
	
		//Then calculate for the bottom 50 docs
		double diffBot50=0;
		for(int a=powerItResults.size()-50;a<powerItResults.size();a++){
		
			PageRankEntry ent=powerItResults.get(a);
			int thisID=this.docNumber.get(""+ent.docID);
			double exactRank=ent.pageRank;
		
			double estRank=res[thisID];
			
			diffBot50=diffBot50+Math.pow(exactRank-estRank,2);
		}
		//Save the difference for this iteration
		sumOfSquaredDiffBot[i]=diffBot50;
	}
	
	//Print the results
	System.out.println("Sum of squared Differences for the top 50 docs");
	for(int i=0;i<numberOfTestIterations;i++){
		System.out.println("N=numberOfDocs*"+((i+1)*multiplyIBy)+" Diff="+sumOfSquaredDiffTop[i]);	
	}
	System.out.println("Sum of squared Differences for the bottom 50 docs");
	for(int i=0;i<numberOfTestIterations;i++){
		System.out.println("N=numberOfDocs*"+((i+1)*multiplyIBy)+" Diff="+sumOfSquaredDiffBot[i]);	
	}
	
	}
	
	/*
	ArrayList<PageRankEntry> pageRanks=this.sortPageRanks(res, numberOfDocs);
	
	for(int i=0;i<50;i++){
	
		System.err.println((i+1)+". "+pageRanks.get(i).toString());
		
	}
	*/
	/*
	for(int i=0;i<50;i++){
	
		System.err.println((i+1)+". "+powerItResults.get(i).toString());
		
	}*/
	/*
	//Used to write the results to a file (to be used with the search method)
	//this.writeResultToFile(pageRanks);
	this.writeResultAsHashMap(pageRanks);*/
	
	//this.writeResultAsText(powerItResults);
	
	
    }
    
    private void sortAndPrintTop50(double[] res, int numberOfDocs){
    	   
    	    
    	ArrayList<PageRankEntry> pageRanks=this.sortPageRanks(res, numberOfDocs);
	
	
	for(int i=0;i<50;i++){
	
		System.err.println((i+1)+". "+pageRanks.get(i).toString());
	}
    	    
    }

    private void writeResultToFile(ArrayList<PageRankEntry> pageRank){
    	    
    	    System.err.println("Writing PageRanks to file");
    	    
    	    String fileName="DavisPageRank";
    	    
    	    try{
    	    
    	    	ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(fileName));
    	    	oos.writeObject(pageRank);
    	    	oos.flush();
    	    	oos.close();
    	    
    	    }catch(Exception e){
    	    	   System.err.println("Error writing pageRank to file");
    	    	   e.printStackTrace();
    	    	    }
    	    
    }
    
    private void writeResultAsHashMap(ArrayList<PageRankEntry> pageRank){
    	    
    	    System.err.println("Writing PageRanks to file as HashMap");
    	    
    	    HashMap<Integer,PageRankEntry> pageRankHashMap=new HashMap<Integer,PageRankEntry>();
    	    for(int i=0;i<pageRank.size();i++){
    	    	PageRankEntry ent=pageRank.get(i);
    	    	Integer docID=ent.docID;
    	    	pageRankHashMap.put(docID,ent);
    	    }
    	    
    	    String fileName="DavisPageRankHashMap";
    	    
    	    try{
    	    
    	    	ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(fileName));
    	    	oos.writeObject(pageRankHashMap);
    	    	oos.flush();
    	    	oos.close();
    	    
    	    }catch(Exception e){
    	    	   System.err.println("Error writing pagerank to file as HashMap");
    	    	   e.printStackTrace();
    	    	    }
    	    
    }
    
    private void writeResultAsText(ArrayList<PageRankEntry> pageRank){
    	    
    	    System.err.println("Writing PageRanks to file as text");
    	    
    	    String fileName="DavisPageRankText";
    	    
    	    try{
    	    
    	    	PrintWriter writer=new PrintWriter(new FileOutputStream(fileName));
    	    	
    	    	for(int i=0;i<pageRank.size();i++){
    	    		PageRankEntry ent=pageRank.get(i);
    	    		int docID=ent.docID;
    	    		double rank=ent.pageRank;
    	    		String writeString=""+docID+" "+rank;
    	    		writer.println(writeString);
    	    		
    	    	}
    	    	
    	    	
    	    	writer.flush();
    	    	writer.close();
    	    
    	    }catch(Exception e){
    	    	   System.err.println("Error writing pagerank to file");
    	    	   e.printStackTrace();
    	    	    }
    	    
    }
    
    
    private ArrayList<PageRankEntry> readPageRankFromFile(){
    
    	System.err.println("Read PageRanks from file");
    	    
	String fileName="DavisPageRank";
	
	try{
	
		ObjectInputStream oins=new ObjectInputStream(new FileInputStream(fileName));
    		ArrayList<PageRankEntry> pageRank=(ArrayList<PageRankEntry>) oins.readObject();
    		oins.close();
    		
    		return pageRank;
    		
    	}catch(Exception e){
    	    	   System.err.println("Error writing index to file");
    	    	   e.printStackTrace();
    	    	    }
    	    
    	return null;
    	
    }
    
    
    private double[] powerIteration(int numberOfDocs){
    	    

    	//Initial X
	double[] curX=new double[numberOfDocs];
	curX[0]=1;
	double[] newX=new double[numberOfDocs];
	
	double alphaN=(this.BORED/(double)numberOfDocs);
	
	double oneOverN=1.0/(double)numberOfDocs;
	
	double curError=1;
	
	int nrIterations=0;
	
	while(curError>this.EPSILON && nrIterations<this.MAX_NUMBER_OF_ITERATIONS){
	
		//Iterate all documents
		for(int i=0;i<numberOfDocs;i++){
		
			//int fromDoc=Integer.parseInt(docName[i])-1;
			int fromDoc=i;
			
			Hashtable<Integer,Boolean> curLinks=link.get(fromDoc);
		
			double noLinksProb=0;
		
			if(curLinks==null){
				noLinksProb=oneOverN;
				//noLinksProb=noLinksProb*(1.0-this.BORED);
				//noLinksProb=noLinksProb+alphaN;
				noLinksProb=noLinksProb*curX[fromDoc];
			}
		
			for(int j=0;j<numberOfDocs;j++){
		
				//int toDoc=Integer.parseInt(docName[j])-1;
				int toDoc=j;

				double prob=0;
			
				//If the fromDoc doesn't have any links
				if(curLinks==null){
					newX[toDoc]=newX[toDoc]+noLinksProb;	
				}
				//If the fromDoc has links
				else{
					int nrOutLinks=curLinks.keySet().size();
					if(curLinks.get(toDoc)!=null){
						prob=1.0/(double)nrOutLinks;
						prob=prob*(1.0-this.BORED);
					}
					prob=prob+alphaN;
					prob=prob*curX[fromDoc];
				
					newX[toDoc]=newX[toDoc]+prob;
				}
			
			}
		}
		
		//Calculate the error
		double newError=0;
		for(int i=0;i<numberOfDocs;i++){
		
			newError=newError+Math.pow(curX[i]-newX[i],2);
			
		}
		newError=Math.sqrt(newError);
		curError=newError;
		
		curX=newX;
		//Reset newX
		newX=new double[numberOfDocs];
		nrIterations++;
	
	}//End while
    	    
	System.err.println("NrIterations:"+nrIterations);
	
	
	return curX;
    }
    
    private ArrayList<PageRankEntry> sortPageRanks(double[] pageRanks, int nrOfDocs){
    	    
    	    ArrayList<PageRankEntry> res=new ArrayList<PageRankEntry>();
		    
    	    for(int i=0;i<nrOfDocs;i++){
    	    	    int docID=Integer.parseInt(docName[i]);
    	    	    //int docID=i+1;
    	    	    PageRankEntry ent=new PageRankEntry(docID,this.docName[i],pageRanks[i]);
    	    	    res.add(ent);
    	    }
    	    
    	    Collections.sort(res);
    	    
    	    return res;
    }
    
    
    private double[] MCendPointRandStart(int nrOfDocs, int nrIterations){

    	    int[] nrStops=new int[nrOfDocs];
    	    
    	    double[] pageRank=new double[nrOfDocs];
    	    
    	    Random rand=new Random();
    	    
    	    for(int i=0;i<nrIterations;i++){
    	    
    	    	    double jumpOrNot=rand.nextDouble();
    	    	    
    	    	    int curPoint=rand.nextInt(nrOfDocs);
    	    	    
    	    	    //While the random surfer is not bored
    	    	    while(jumpOrNot<(1.0-this.BORED)){
    	    	    	    
    	    	    	Hashtable<Integer,Boolean> curLinks=link.get(curPoint);
    	    	    	//If there are no links
    	    	    	if(curLinks==null){
    	    	    		//Then we random any page
    	    	    		curPoint=rand.nextInt(nrOfDocs);
    	    	    	}
    	    	    	else{
    	    	    		int nrOfLinks=curLinks.keySet().size();
    	    	    		int whatLink=rand.nextInt(nrOfLinks);
    	    	    		Iterator<Integer> it=curLinks.keySet().iterator();
    	    	    		int nextPos=it.next();
    	    	    		for(int a=0;a<whatLink;a++){
    	    	    			nextPos=it.next();	
    	    	    		}
    	    	    		curPoint=nextPos;
    	    	    	}
    	    	    	  jumpOrNot=rand.nextDouble();  
    	    	    }
    	    	    
    	    	    //End of one sample
    	    	    //We save the endpoint
    	    	    nrStops[curPoint]=nrStops[curPoint]+1;
    	    	    
    	    }
    	    
    	    //When we have done N iterations we calculate the pagerank
    	    for(int i=0;i<nrOfDocs;i++){
    	    
    	    	    pageRank[i]=(double)nrStops[i]/(double)nrIterations;
    	    	    
    	    }
    	    
    	    return pageRank;
    }
    
    
    private double[] MCendPointCyclicStart(int nrOfDocs, int nrIterations){

    	    int[] nrStops=new int[nrOfDocs];
    	    
    	    double[] pageRank=new double[nrOfDocs];
    	    
    	    Random rand=new Random();
    	    
    	    int startPos=0;
    	    
    	    for(int i=0;i<nrIterations;i++){
    	    
    	    	    double jumpOrNot=rand.nextDouble();
    	    	    
    	    	    int curPoint=startPos;
    	    	    
    	    	    //While the random surfer is not bored
    	    	    while(jumpOrNot<(1.0-this.BORED)){
    	    	    	    
    	    	    	Hashtable<Integer,Boolean> curLinks=link.get(curPoint);
    	    	    	//If there are no links
    	    	    	if(curLinks==null){
    	    	    		//Then we random any page
    	    	    		curPoint=rand.nextInt(nrOfDocs);
    	    	    	}
    	    	    	else{
    	    	    		int nrOfLinks=curLinks.keySet().size();
    	    	    		int whatLink=rand.nextInt(nrOfLinks);
    	    	    		Iterator<Integer> it=curLinks.keySet().iterator();
    	    	    		int nextPos=it.next();
    	    	    		for(int a=0;a<whatLink;a++){
    	    	    			nextPos=it.next();	
    	    	    		}
    	    	    		curPoint=nextPos;
    	    	    	}
    	    	    	  jumpOrNot=rand.nextDouble();  
    	    	    }
    	    	    
    	    	    //End of one sample
    	    	    //We save the endpoint
    	    	    nrStops[curPoint]=nrStops[curPoint]+1;
    	    	    
    	    	    startPos++;
    	    	    if(startPos>=nrOfDocs){
    	    	    	startPos=0;	    
    	    	    }
    	    	    
    	    }
    	    
    	    //When we have done N iterations we calculate the pagerank
    	    for(int i=0;i<nrOfDocs;i++){
    	    
    	    	    pageRank[i]=(double)nrStops[i]/(double)nrIterations;
    	    	    
    	    }
    	    
    	    return pageRank;
    }
 
    
    private double[] MCcompletePath(int nrOfDocs, int nrIterations){

    	    int[] nrVisits=new int[nrOfDocs];
    	    
    	    double[] pageRank=new double[nrOfDocs];
    	    
    	    Random rand=new Random();
    	    
    	    int startPos=0;
    	    
    	    long nrOfNodesVisited=0;
    	    
    	    for(int i=0;i<nrIterations;i++){
    	    
    	    	    double jumpOrNot=rand.nextDouble();
    	    	    
    	    	    int curPoint=startPos;
    	    	    
    	    	    ArrayList<Integer> path=new ArrayList<Integer>();
    	    	    path.add(curPoint);
    	    	    
    	    	    //While the random surfer is not bored
    	    	    while(jumpOrNot<(1.0-this.BORED)){
    	    	    	    
    	    	    	Hashtable<Integer,Boolean> curLinks=link.get(curPoint);
    	    	    	//If there are no links
    	    	    	if(curLinks==null){
    	    	    		//Then we random any page
    	    	    		curPoint=rand.nextInt(nrOfDocs);
    	    	    	}
    	    	    	else{
    	    	    		int nrOfLinks=curLinks.keySet().size();
    	    	    		int whatLink=rand.nextInt(nrOfLinks);
    	    	    		Iterator<Integer> it=curLinks.keySet().iterator();
    	    	    		int nextPos=it.next();
    	    	    		for(int a=0;a<whatLink;a++){
    	    	    			nextPos=it.next();	
    	    	    		}
    	    	    		curPoint=nextPos;
    	    	    	}
    	    	    	  path.add(curPoint);
    	    	    	  jumpOrNot=rand.nextDouble();  
    	    	    }
    	    	    
    	    	    //End of one sample
    	    	    //We save the path
    	    	    for(int a=0;a<path.size();a++){
    	    	    	    nrVisits[path.get(a)]=nrVisits[path.get(a)]+1;
    	    	    	    nrOfNodesVisited++;
    	    	    }
    	    	    
    	    	    startPos++;
    	    	    if(startPos>=nrOfDocs){
    	    	    	startPos=0;	    
    	    	    }
    	    	    
    	    	    
    	    }
    	    
    	    //When we have done N iterations we calculate the pagerank
    	    for(int i=0;i<nrOfDocs;i++){
    	    
    	    	    pageRank[i]=(double)nrVisits[i]/(double)nrOfNodesVisited;
    	    	    
    	    }
    	    
    	    return pageRank;
    }
    
    private double[] MCcompletePathStopDangling(int nrOfDocs, int nrIterations){

    	    int[] nrVisits=new int[nrOfDocs];
    	    
    	    double[] pageRank=new double[nrOfDocs];
    	    
    	    Random rand=new Random();
    	    
    	    int startPos=0;
    	    
    	    long nrOfNodesVisited=0;
    	    
    	    for(int i=0;i<nrIterations;i++){
    	    
    	    	    double jumpOrNot=rand.nextDouble();
    	    	    
    	    	    int curPoint=startPos;
    	    	    
    	    	    ArrayList<Integer> path=new ArrayList<Integer>();
    	    	    path.add(curPoint);
    	    	    
    	    	    //While the random surfer is not bored
    	    	    while(jumpOrNot<(1.0-this.BORED)){
    	    	    	    
    	    	    	Hashtable<Integer,Boolean> curLinks=link.get(curPoint);
    	    	    	//If there are no links
    	    	    	if(curLinks==null){
    	    	    		//Then we break the while loop
    	    	    		break;
    	    	    	}
    	    	    	else{
    	    	    		int nrOfLinks=curLinks.keySet().size();
    	    	    		int whatLink=rand.nextInt(nrOfLinks);
    	    	    		Iterator<Integer> it=curLinks.keySet().iterator();
    	    	    		int nextPos=it.next();
    	    	    		for(int a=0;a<whatLink;a++){
    	    	    			nextPos=it.next();	
    	    	    		}
    	    	    		curPoint=nextPos;
    	    	    	}
    	    	    	  path.add(curPoint);
    	    	    	  jumpOrNot=rand.nextDouble();  
    	    	    }
    	    	    
    	    	    //End of one sample
    	    	    //We save the path
    	    	    for(int a=0;a<path.size();a++){
    	    	    	    nrVisits[path.get(a)]=nrVisits[path.get(a)]+1;
    	    	    	    nrOfNodesVisited++;
    	    	    }
    	    	    
    	    	    startPos++;
    	    	    if(startPos>=nrOfDocs){
    	    	    	startPos=0;	    
    	    	    }
    	    	    
    	    	    
    	    }
    	    
    	    //When we have done N iterations we calculate the pagerank
    	    for(int i=0;i<nrOfDocs;i++){
    	    
    	    	    pageRank[i]=(double)nrVisits[i]/(double)nrOfNodesVisited;
    	    	    
    	    }
    	    
    	    return pageRank;
    }
    
    private double[] MCcompletePathStopDanglingRandStart(int nrOfDocs, int nrIterations){

    	    int[] nrVisits=new int[nrOfDocs];
    	    
    	    double[] pageRank=new double[nrOfDocs];
    	    
    	    Random rand=new Random();
    	    
    	    long nrOfNodesVisited=0;
    	    
    	    for(int i=0;i<nrIterations;i++){
    	    
    	    	    double jumpOrNot=rand.nextDouble();
    	    	    
    	    	    int curPoint=rand.nextInt(nrOfDocs);
    	    	    
    	    	    ArrayList<Integer> path=new ArrayList<Integer>();
    	    	    path.add(curPoint);
    	    	    
    	    	    //While the random surfer is not bored
    	    	    while(jumpOrNot<(1.0-this.BORED)){
    	    	    	    
    	    	    	Hashtable<Integer,Boolean> curLinks=link.get(curPoint);
    	    	    	//If there are no links
    	    	    	if(curLinks==null){
    	    	    		//Then we break the while loop
    	    	    		break;
    	    	    	}
    	    	    	else{
    	    	    		int nrOfLinks=curLinks.keySet().size();
    	    	    		int whatLink=rand.nextInt(nrOfLinks);
    	    	    		Iterator<Integer> it=curLinks.keySet().iterator();
    	    	    		int nextPos=it.next();
    	    	    		for(int a=0;a<whatLink;a++){
    	    	    			nextPos=it.next();	
    	    	    		}
    	    	    		curPoint=nextPos;
    	    	    	}
    	    	    	  path.add(curPoint);
    	    	    	  jumpOrNot=rand.nextDouble();  
    	    	    }
    	    	    
    	    	    //End of one sample
    	    	    //We save the path
    	    	    for(int a=0;a<path.size();a++){
    	    	    	    nrVisits[path.get(a)]=nrVisits[path.get(a)]+1;
    	    	    	    nrOfNodesVisited++;
    	    	    }

    	    	    
    	    	    
    	    }
    	    
    	    //When we have done N iterations we calculate the pagerank
    	    for(int i=0;i<nrOfDocs;i++){
    	    
    	    	    pageRank[i]=(double)nrVisits[i]/(double)nrOfNodesVisited;
    	    	    
    	    }
    	    
    	    return pageRank;
    }
    
    
 
    /* --------------------------------------------- */


    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}
