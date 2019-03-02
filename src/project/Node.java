package project;

/**
 * Node class for the MerkleTree
 * It keeps DATA, LEFT, RIGHT, and L fields.
 * These fields necessary for the MerkleTree processes.
 * @author Burak_2016400186
 *
 */
public class Node {

	/**
	 * Data field of node
	 */
	private String data;
	/**
	 * Left node field of node
	 */
	private Node left = null;
	/**
	 * Right node field of node
	 */
	private Node right = null;
	/**
	 * chunk node field of node
	 */
	private Node l = null;
	
	/**
	 * Constructor for the node
	 * @param value data for the created node
	 */
	public Node(String value) {
		data = value;
	}
	
	/**
	 * Getter for the left node
	 * @return left node
	 */
	public Node getLeft() {
		return left;
	}
	/**
	 * Getter for the right node
	 * @return right node
	 */
	public Node getRight() {
		return right;
	}
	/**
	 * Getter for the data
	 * @return the data in the node
	 */
	public String getData() {
		return data;
	}
	
	/**
	 * Getter for the L node
	 * @return the chunk node of the node
	 */
	public Node getL() {
		return l;
	}
	
	/**
	 * Setter for the left node
	 * @param n node to set left
	 */
	public void setLeft(Node n) {
		this.left = n;
	}
	/**
	 * Setter for the right node
	 * @param n node to set right
	 */
	public void setRight(Node n) {
		this.right = n;
	}
	
	/**
	 * Setter for the data on the node
	 * @param s string to set data
	 */
	public void setData(String s) {
		this.data = s;
	}
	
	/**
	 * Setter for the chunk node
	 * @param l node to set chunk, l
	 */
	public void setL(Node l) {
		this.l = l;
	}
	
	/**
	 * Getter for the height of the node
	 * @return the height of the node
	 */
	public int getHeight() {
		int lefth = -1;
		int righth = -1;
		
		if(left != null) {
			lefth = left.getHeight();
		}
		if(right != null) {
			righth = right.getHeight();
		}
		return Math.max(righth, lefth)+1;
	}
	
}
