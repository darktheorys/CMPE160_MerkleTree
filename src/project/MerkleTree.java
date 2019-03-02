package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import util.HashGeneration;

/**
 * 
 * MerkleTree Class of the third project for the CMPE160 class.
 * 
 * A tree class that keeps hashes in its nodes.
 * Leaf nodes keep another node named as L that keeps the real path of the chunks.
 * 
 * @author Burak_2016400186 
 */
public class MerkleTree {
	/**
	 * A field for findCorruptChunks() method, to keep all corrupted chunks' hashes in one place
	 */
	private ArrayList<Stack<String>> corruptedChunks;
	
	/**
	 * Scanner to read from the file
	 */
	private Scanner readText;
	
	/**
	 * Root of the current tree
	 */
	private Node root;
	
	/**
	 * Queue that keeps the chunks that is taken from the file
	 */
	private Queue<String> dataQueue;

	/**
	 * Queue to keep hashes of chunks
	 */
	private Queue<String> validQueue;

	/**
	 * Queue for the corrupt chunk's names
	 */
	private Queue<String> corruptNames;

	/**
	 * Number of the chunks
	 */
	private int chunkSize = 0;

	/**
	 * Constructor for the tree, it calls another method named createTree() which creates tree, actually
	 * @param path file path for the chunks
	 */
	public MerkleTree(String path){
		createTree(path);
	}

	/**
	 * Calls fillQueue() method to keep chunks in a queue.
	 * By using the size of the queue it determines what the height of the tree will be. It uses logarithm based 2.
	 * Calls createNLevelTree() method to create tree with blank values.
	 * At the end, calls the add() method to add hashes and chunks to the root node , tree.
	 * @param path file path for the chunks
	 */
	private void createTree(String path) {
		File f = new File(path);
		fillQueue(f);
		int level = 0;
		if(dataQueue.size()%2 == 0 && !dataQueue.isEmpty()) {
			level = (int) Math.ceil((Math.log(dataQueue.size())/Math.log(2.0)));
		}else if(dataQueue.isEmpty()) {
			level = -1;
		}
		else {
			level = (int) Math.ceil((Math.log(dataQueue.size())/Math.log(2.0)));
		}
		//the calculations above gives the level but in this code createNLevelTree() method creates the tree with n-1 levels
		//therefore we send the level as n+1 to get right tree
		root = createNLevelTree(root, level + 1);
	}

	/**
	 * Takes a node and number and fills it with empty strings to the given level value.
	 * It works as recursive
	 * @param n node to create with levels on it
	 * @param level number to create levels on node
	 * @return the node that is created
	 */
	private Node createNLevelTree(Node n,int level) {
		//ender for recursive
		if(level == 0 ) {
			return null;
		}
		n = new Node("");
		n.setLeft(createNLevelTree(n.getLeft(), level - 1));
		n.setRight(createNLevelTree(n.getRight(), level - 1));
		//setting L
		if(level-1 == 0) {
			if(!dataQueue.isEmpty()) {
				n.setL(new Node(dataQueue.peek()));
				n.setData(createHash(new File(dataQueue.poll())));
			}else if(validQueue != null) {
				if(!validQueue.isEmpty()) {
					n.setL(new Node(""));
					n.setData(validQueue.poll());
				}
			}
		}
		//setting hashes of all nodes
		if(n.getData().equals("") && n.getLeft() != null &&n.getRight() != null) {
			n.setData(createHash(n.getLeft().getData(), n.getRight().getData()));
		}else if(n.getData().equals("") && n.getLeft() != null  &&n.getRight() == null ){
			n.setData(createHash(n.getLeft().getData(), ""));
		}
		return n;
	}

	/**
	 * Method to arrange validQueue with chunks' hashes
	 * @param valid queue that includes all valid hashes
	 */
	private void getTrueQueue(Queue<String> valid) {
		validQueue = new LinkedList<String>();
		int x = valid.size() - chunkSize;
		for(int i = 0; i < x ; i++ ) {
			valid.poll();
		}
		validQueue = valid;
	}

	/**
	 * Reads from the given file and adds chunks to the dataQueue field.
	 * @param f file that includes the chunks
	 */
	private void fillQueue(File f) {
		dataQueue = new LinkedList<String>();
		try {
			readText = new Scanner(f);
			while(readText.hasNextLine()) {
				String s = readText.nextLine();
				dataQueue.add(s);
			}
			this.chunkSize = dataQueue.size();
			readText.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found!");
		}
	}

	/**
	 * Creates hash for the given values of strings. Uses try-catch method to avoid unexpected crashes.
	 * @param s1 String to create hash
	 * @param s2 String to create hash
	 * @return Hash code of the given strings
	 */
	private static String createHash(String s1, String s2) {
		try {
			if(!(s1+s2).equals("")) {
				return HashGeneration.generateSHA256(s1+s2);
			}
			return "";
		} catch (NoSuchAlgorithmException e) {
			System.out.println("String Hashing Error!");
		} catch (UnsupportedEncodingException e) {
			System.out.println("String Hashing Error2!");
		}
		return null;
	}

	/**
	 * Creates hash for the given file. Uses try-catch method to avoid unexpected crashes.
	 * @param f File to create hash
	 * @return Hash of the given file
	 */
	private static String createHash(File f) {
		try {
			return HashGeneration.generateSHA256(f);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("File Hashing Error!");
		} catch (IOException e) {
			System.out.println("File Hashing Error2!");
		}
		return null;
	}

	/**
	 * Method to get root node of the tree
	 * @return root node of the tree
	 */
	public Node getRoot() {
		return this.root;
	}

	/**
	 * Checks if the tree includes trusted hashes or not in a short way 
	 * @param path path of the text that includes valid hashes
	 * @return true if the root hash and first element in the valid text
	 */
	public boolean checkAuthenticity(String path) {
		String validText;
		//finding true hashes
		validText = findValid(path).peek();
		if(validText.equals(null) || root == null) {
			return false;
		}
		return validText.equals(root.getData());
	}

	/**
	 * Checks the equity for the given nodes' data.
	 * @param n Node to check
	 * @param m Node to check
	 * @return true if the data's on the nodes are equal and if one of the nodes is null
	 */
	private boolean checkNodeDatas(Node n, Node m) {
		if(n == null || m == null) {
			return true;
		}
		return n.getData().equals(m.getData());
	}

	/**
	 * Takes valid hashes from the file and adds them to a queue that we can reach.
	 * @param path Path that includes text file for valid hashes
	 * @return A queue with valid hashes taken from the file
	 */
	private Queue<String> findValid(String path) {
		File metaFile = new File(path);
		Queue<String> validHashes = new LinkedList<String>();
		try {
			readText = new Scanner(metaFile);
			while(readText.hasNextLine()) {
				String line = readText.nextLine();
				if(!line.equals("")) {
					validHashes.add(line);
				}
			}
			readText.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found!");
		}
		return validHashes;
	}

	/**
	 * Method to find corrupted chunks in the tree, add them to stack and add them to an ArrayList.
	 * It first creates another tree for the valid hashes. We can consider this tree as what the actual tree must be
	 * Then, it calls a recursive method to find corrupted ones.
	 * @param path Path of file that includes valid hashes 
	 * @return List of stacks that include corrupted chunk's hashes
	 */
	public ArrayList<Stack<String>> findCorruptChunks(String path){
		corruptNames = new LinkedList<String>();
		corruptedChunks = new ArrayList<>();
		//creating the tree with valid values of hashes
		if(root != null) {
			Node valid = null;
			//filling the necessary queue for creating the valid tree
			getTrueQueue(findValid(path));
			//creating the tree with valid hashes
			valid = createNLevelTree(valid, root.getHeight()+1);
			Node mine = root;
			Stack<String> chunk = new Stack<String>();
			//Starting point of recursive
			goRecursive(mine, chunk, valid);
		}
		return corruptedChunks;
	}

	/**
	 * Method to get corrupt chunks, recursively. It also fills the corruptNames queue for another process called download.
	 * @param mine node for the current tree
	 * @param s stack that includes the corrupt chunks' hashes
	 * @param valid node for the valid tree
	 */
	@SuppressWarnings("unchecked")
	public void goRecursive(Node mine, Stack<String> s, Node valid) {
		Stack<String> cloneS = ((Stack<String>) s.clone());
		if(mine.getL() != null) {
			s.push(mine.getData());
			corruptNames.add(mine.getL().getData());
			valid.setData(mine.getData());
			corruptedChunks.add(s);
		}else {
			if(!checkNodeDatas(mine.getLeft(),valid.getLeft())){
				s.push(mine.getData());
				goRecursive(mine.getLeft(), s, valid.getLeft());
				valid.setLeft(mine.getLeft());
			}
			if(!checkNodeDatas(mine.getRight(),valid.getRight())) {
				cloneS.push(mine.getData());
				goRecursive(mine.getRight(), cloneS, valid.getRight());
				valid.setRight(mine.getRight());
			}
		}
	}

	/**
	 * Method to get a queue that includes corrupt chunks' paths
	 * @return a queue that includes corrupt chunks' paths
	 */
	public Queue<String> getCorruptNames(){
		return this.corruptNames;
	}

	/**
	 * Method to call another classes method. This is for preventing main from importing DownloadChunk class
	 * @param path path of the file that includes links for the chunks
	 */
	public static void downloadChunks(String path) {
		DownloadChunk.downloadFromGivenFile(path);
	}

}
