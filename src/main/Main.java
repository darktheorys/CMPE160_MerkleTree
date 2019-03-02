package main;

import java.util.ArrayList;
import java.util.Stack;
import project.MerkleTree;

public class Main {

	public static void main(String[] args){
		
		
		MerkleTree m0 = new MerkleTree("sample/white_walker.txt");		
		boolean valid = m0.checkAuthenticity("sample/white_walkermeta.txt");
		System.out.println(valid);
		
		MerkleTree m1 = new MerkleTree("data/0.txt");		
		boolean valid2 = m1.checkAuthenticity("data/0meta.txt");
		System.out.println(valid2);
		
		MerkleTree m2 = new MerkleTree("data/1.txt");		
		boolean valid3 = m2.checkAuthenticity("data/1meta.txt");
		System.out.println(valid3);
		
		MerkleTree mx = new MerkleTree("data/1_bad.txt");		
		boolean validx = mx.checkAuthenticity("data/1meta.txt");
		System.out.println(validx);
		
		MerkleTree m3 = new MerkleTree("data/2.txt");		
		boolean valid4 = m3.checkAuthenticity("data/2meta.txt");
		System.out.println(valid4);
		
		MerkleTree m4 = new MerkleTree("data/3.txt");		
		boolean valid5 = m4.checkAuthenticity("data/3meta.txt");
		System.out.println(valid5);
		
		MerkleTree m5 = new MerkleTree("data/9.txt");		
		boolean valid6 = m5.checkAuthenticity("data/9meta.txt");
		System.out.println(valid6);
		
		
		
		
		// The following just is an example for you to see the usage. 
		// Although there is none in reality, assume that there are two corrupt chunks in this example.
		ArrayList<Stack<String>> corrupts = mx.findCorruptChunks("data/1meta.txt");
		System.out.println("Number of the corrupted chunks : " + corrupts.size());
		int n = 1;
		for(Stack<String> s : corrupts) {
			System.out.println(s.size() + " <--Size.   Corrupt hash of "+ n + ". corrupt chunk is: " + s.pop());
			n++;
		}
		
		
		download("secondaryPart/data/download_from_trusted.txt");
		
	}
	
	public static void download(String path) {
		MerkleTree.downloadChunks(path);
	}

}
