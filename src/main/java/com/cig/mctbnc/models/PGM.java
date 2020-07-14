package com.cig.mctbnc.models;

/**
 * Defines the methods of a probabilistic graphical model
 * @author carlosvillablanco
 * @param <T>
 *
 */
public interface PGM<T> {
	
	/**
	 * Learn the structure and parameters of the model.
	 */
	public void learn();
	
	/**
	 * Modify the structure of the PGM by modifying the parents of the nodes and their CPD.
	 * @param adjacencyMatrix
	 */
	public void setStructure(boolean[][] adjacencyMatrix);
	
	public T[][] predict();
	
	public void sample();
	
	/**
	 * Displays the probabilistic graphical model.
	 */
	public void display();

}
