package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class DownloadChunk {

	/**
	 * Method to read the file that exists in a given path and by using the data in it download the meta and chunks.
	 * If the first downloaded chunks is not valid then it tries for the alternative link.
	 * @param path path of the file that includes links of meta and chunks.
	 */
	public static void downloadFromGivenFile(String path) {
		File downloadFrom = new File(path);
		//scanner to read file
		Scanner scan = null;
		try {
			scan = new Scanner(downloadFrom);
			//if file has more things to download, it continues
			while(scan.hasNextLine()) {
				Queue<String> pathQueue = new LinkedList<String>();
				//in the file it reads the lines and adds them to a queue for each file
				//the limit is 3 because meta, link, and alternative link in three line
				//basically it reads 3 line for one file
				while(pathQueue.size() != 3) {
					String s = scan.nextLine();
					if(s.equals("")) {
						s= scan.nextLine();
						pathQueue.add(s);
					}else {
						pathQueue.add(s);
					}
				}
				//determining the path of the given file
				String filePath = "";
				for(int i = 0; i < path.length(); i++) {
					if(path.charAt(i) == '/') {
						filePath = path.substring(0, i+1);
					}
				}
				//Proceeding to download meta from the server
				//it uses a for loop to find meta's name
				String metaLink = pathQueue.peek();
				String metaName = "";
				for(int i=0; i < metaLink.length(); i++) {
					if(metaLink.charAt(i) == '/') {
						metaName = metaLink.substring(i+1);
					}
				}
				try {
					//downloads meta file 
					downloadAFile(pathQueue.poll(), metaName, filePath);
					//download chunks from server
					//if corrupted files were downloaded, then the alternative source is used for download true file
					arrangeDownload(pathQueue, filePath, metaName);		
				}catch(Exception e){
					System.out.println("FAILED ");
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found!");
		}

	}

	/**
	 * Method to arrange download. It first downloads files a folder named temporary/, temporarily.
	 * After the downloading files to that folder, it uses these files to download chunks to a folder named split/.
	 * After the downloading chunks to that folder, it arranges another file for the creation of the MerkleTree by using a print stream.
	 * After the creation of the file to create tree, it creates the tree and checks it with meta downloaded in downloadFromGivenFile() method.
	 * If there is a corruption in the tree, it uses the queue for corrupt chunks' names and downloads them from the alternative source.
	 * At the end it deletes all the temporary files. Only the correct chunks and the files that help to create tree exist 
	 * @param pathQueue queue to get links of the chunks
	 * @param filePath path to download files in it
	 * @param metaName name of the meta file to check validation of tree
	 */
	private static void arrangeDownload(Queue<String> pathQueue, String filePath, String metaName) {
		String chunkPath = pathQueue.peek();
		String fileName = "";
		for(int i=0; i < chunkPath.length(); i++) {
			if(chunkPath.charAt(i) == '/') {
				fileName = chunkPath.substring(i+1);
			}
		}
		File dir = new File(filePath + "temporary/");
		if(!dir.exists()) {
			dir.mkdir();
		}
		try {
			downloadAFile(pathQueue.poll(), fileName, filePath + "temporary/");
			File tempFile = new File(filePath + "temporary/"+ fileName);
			Scanner scanFile = null;
			try {
				scanFile = new Scanner(tempFile);
				Queue<String> download = new LinkedList<>();
				while(scanFile.hasNextLine()){
					String s = scanFile.nextLine();
					if(!s.equals("")) {
						download.add(s);
					}
				}
				scanFile.close();
				tempFile.delete();
				PrintStream p = null;
				File file = new File(filePath +  fileName);
				if(!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						System.out.println("Error!");
					}
				}
				try {
					p = new PrintStream(file);
					while(!download.isEmpty()) {
						String name = "";
						String chunkName = "";
						for(int i = 0; i < download.peek().length(); i++) {
							if(download.peek().charAt(i) == '/') {
								name = download.peek().substring(i+1);
							}
						}
						for(int i=0; i < fileName.length(); i++) {
							if(fileName.charAt(i) == '.') {
								chunkName = fileName.substring(0, i);
							}
						}
						downloadAFile(download.poll(), name, filePath + "split/" + chunkName);
						p.println(filePath + "split/" + chunkName + "/" + name);
					}
					p.close();

					MerkleTree testDownloaded = new MerkleTree(filePath + fileName);
					if(!testDownloaded.checkAuthenticity(filePath + metaName)) {
						downloadAFile(pathQueue.poll(), fileName , filePath + "temporary/");
						testDownloaded.findCorruptChunks(filePath + metaName);
						tempFile = new File(filePath + "temporary/"+ fileName );
						scanFile = null;
						try {
							download = new LinkedList<>();
							for(String s : testDownloaded.getCorruptNames()) {
								scanFile = new Scanner(tempFile);
								String corruptChunkName = "";
								for(int i=0; i<s.length(); i++) {
									if(s.charAt(i) == '/') {
										corruptChunkName = s.substring(i+1);
									}
								}
								while(scanFile.hasNextLine()){
									String line = scanFile.nextLine();
									if(line.substring(line.length() - corruptChunkName.length()).contains(corruptChunkName)) {
										download.add(line);
									}
								}
								scanFile.close();
							}
							tempFile.delete();
							while(!download.isEmpty()) {
								String chunkName = "";
								String name = "";
								for(int i = 0; i < download.peek().length(); i++) {
									if(download.peek().charAt(i) == '/') {
										chunkName = download.peek().substring(i+1);
									}
								}
								for(int i=0; i < fileName.length(); i++) {
									if(fileName.charAt(i) == '.') {
										name = fileName.substring(0, i);
									}
								}
								File deleteCorrupt = new File(filePath + "split/" + name + chunkName);
								deleteCorrupt.delete();
								downloadAFile(download.poll(), chunkName, filePath + "split/" + name);
							}
						}
						catch(Exception E) {
							System.out.println("Error!");
						}
						//						File delete = new File(filePath + fileName);
						//						delete.delete();
					}
					File deltempDir = new File(filePath + "temporary");
					deltempDir.delete();
					//					File deleteLink = new File(filePath + fileName);
					//					deleteLink.delete();
					//					File deleteMeta = new File(filePath + metaName);
					//					deleteMeta.delete();

					testDownloaded = new MerkleTree(filePath + fileName);
					if(testDownloaded.checkAuthenticity(filePath + metaName)) {
						System.out.println("SUCCESS! FILE : " + fileName);
						System.out.println("DOWNLOADED FILES PUT INTO \""+ filePath + "split/" + "\" FOLDER");
					}else {
						System.out.println("FAILED TO DOWNLOAD FILE : " + fileName);
					}

				} catch (FileNotFoundException e) {
					System.out.println("File Not Found!");
				}
			} catch (FileNotFoundException e1) {
				System.out.println("File Not Found!");
			}
		}catch(Exception e){
			System.out.println("Error!");
		}
	}

	/**
	 * With The Help of : https://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
	 * It downloads file from the given url to the given path
	 * @param url the adress of the file in online server
	 * @param name name for the download file
	 * @param path path for the download file
	 */
	private static void downloadAFile(String url, String name, String path) {
		//help from : https://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
		URL website = null;
		try {
			website = new URL(url);
			ReadableByteChannel rbc = null;
			try {
				rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = null;
				File f = new File(path);
				//it creates the directory if not exists
				if(!f.exists()) {
					f.mkdirs();
				}
				try {
					fos = new FileOutputStream(path + "/" + name);
					try {
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
						try {
							fos.close();
						} catch (IOException e) {
							System.out.println("Error!");
						}
					} catch (IOException e) {
						System.out.println("Error!");
					}
				} catch (FileNotFoundException e) {
					System.out.println("File Not Found!");
				}
			} catch (IOException e) {
				System.out.println("Connection Error!");
			}
		} catch (MalformedURLException e) {
			System.out.println("Website Error!");
		}	
	}

}
