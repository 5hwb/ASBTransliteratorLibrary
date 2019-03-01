package asb.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileIO {
	
	// File directory to rule files
	public static String rulefileDir;
	
	// File path to currently used replacer rules file
	public static String rulefile = "hangul.json";
	
	/**
	 * Reads a file and returns the content as a string.
	 * From here: http://www.mkyong.com/java/how-to-convert-array-of-bytes-into-file/
	 * @param filename The directory of the file to be read
	 * @return Contents of the file
	 */
	public static String readFile(String fileName) throws IOException {
		FileInputStream is = new FileInputStream(fileName);
		
		int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, "UTF-8");
	}
	
	/** 
	 * Writes contents to a file.
	 * From here: http://www.mkyong.com/java/how-to-convert-array-of-bytes-into-file/
	 * @param contentToWrite The String containing the stuff to write to the file.
	 * @param filename The directory of the file to write to
	 * @throws IOException 
	 */
	public static void writeFile(String contentToWrite, String filename) throws IOException {
		File file = new File(filename);
		// if the file doesn't exist, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		//convert array of bytes into file
	    FileOutputStream fileOutputStream = new FileOutputStream(filename); 
	    fileOutputStream.write(contentToWrite.getBytes("UTF-8"));
	    fileOutputStream.close();
	}
	
	/**
	 * From here: https://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file/20073154
	 */
	public static void copyRulefilesFromAssets() {
		String rulefileFolderDir = "rulefiles/";
		URI uri = null;
		
		ClassLoader classLoader = FileIO.class.getClassLoader();
		try {
		    uri = classLoader.getResource(rulefileFolderDir).toURI();
		} catch (URISyntaxException e) {
		    e.printStackTrace();
		} catch (NullPointerException e) {
		    e.printStackTrace();
		}

		if (uri == null) {
		    System.err.println("Directory or files are missing");
		}

		/** App is running from a JAR file: copy assets to external folder */
		if (uri.getScheme().contains("jar")) {
			try {
				// Get full filepath to JAR file
				URL jar = FileIO.class.getProtectionDomain().getCodeSource().getLocation();
		        System.out.println("DIR! " + jar.toString());
		        
		        // Trim out the 'file:/' part
		        Path jarFile = Paths.get(jar.toString().substring("file:/".length()));
		        FileSystem fs = FileSystems.newFileSystem(jarFile, null);
		        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fs.getPath(rulefileFolderDir));
		        
		        for (Path p: directoryStream) {
		            InputStream is = FileIO.class.getResourceAsStream(p.toString());
		            
		            // Need to move 'rulefileFolderDir' to the folder dir, to make initialisation of folder and files easier
		            rulefileDir = jar.toString().substring("file:/".length()).replace("/ASBTranscriptorApp.jar", "")
		            		+ "/" + rulefileFolderDir;
		            String fileDir = p.toString().replace("/" + rulefileFolderDir, "");
		            System.out.println("FILE from JAR! " + rulefileDir + " - " + fileDir);
		            
		            copyFromInputStreamToFile(is, rulefileDir, fileDir);
		            is.close();
		        }
		    } catch (IOException e) {
		        e.printStackTrace();     
		    }
		}
		/** App is running from IDE */
		else {
			URL rulefileDirUrl = FileIO.class.getClassLoader().getResource("rulefiles/");
			FileIO.rulefileDir = rulefileDirUrl.toString().replace("file:/", "");
			System.out.println("rulefileDir = " + FileIO.rulefileDir);
		}
	}
	
	
	private static void copyFromInputStreamToFile(InputStream is, String folderPath, String filePath) throws IOException {		
		File folder = new File(folderPath);
		File file = new File(folder, filePath);
		// If the folder doesn't exist, then create it
		if (!folder.exists()) {
			System.out.println("Creating folder at '" + folder.getPath() + "'");
			folder.mkdir();
		}
		
		// If the folder doesn't exist, then create it and write the InputStream contents
		if (!file.exists()) {
			System.out.println("Creating file at '" + file.getPath() + "'");
			file.createNewFile();
			
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
					
		    OutputStream os = new FileOutputStream(file); 
		    os.write(buffer);
		    os.close();
		}
		
	}
}
