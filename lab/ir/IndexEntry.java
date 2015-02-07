


package ir;


import java.lang.Comparable;
import java.io.Serializable;


public class IndexEntry implements Serializable {
	
	
	private String term;
	private PostingsList list;
	
	
	public IndexEntry(String term, PostingsList list){
		this.term=term;
		this.list=list;
	}
	
	
	public String getTerm(){
		return term;	
	}
	
	
	public PostingsList getList(){
		return list;	
	}
	
	
	public boolean equals(IndexEntry other){
		return term.equals(other.getTerm());	
	}
	
	
	
}
