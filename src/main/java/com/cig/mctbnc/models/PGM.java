package main.java.com.cig.mctbnc.models;

/**
 * Defines the methods of a probabilistic graphical model
 * @author carlosvillablanco
 * @param <T>
 *
 */
public interface PGM<T> {
	
	public void learn();
	
	/**
	 * Modify the structure of the PGM by modifying the parents of the nodes and their CPD.
	 * @param adjacencyMatrix
	 */
	public void setStructure(boolean[][] adjacencyMatrix);
	
	/**
	 * Displays the probabilistic graphical model.
	 */
	public void display();
	
	public T[][] predict();
	
	public void sample();
	

}
