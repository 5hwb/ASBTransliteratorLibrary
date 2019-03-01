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
	
	public static void aaa() {
		String fooFolder = "rulefiles/";
		URI uri = null;
		
		ClassLoader classLoader = FileIO.class.getClassLoader();
		try {
		    uri = classLoader.getResource(fooFolder).toURI();
		} catch (URISyntaxException e) {
		    e.printStackTrace();
		} catch (NullPointerException e){
		    e.printStackTrace();
		}

		if(uri == null){
		    System.err.println("something is wrong directory or files missing");
		}

		/** i want to know if i am inside the jar or working on the IDE*/
		if(uri.getScheme().contains("jar")){
		    /** jar case */
		    try {
		        URL jar = FileIO.class.getProtectionDomain().getCodeSource().getLocation();
		        //jar.toString() begins with file:
		        //i want to trim it out...
		        System.out.println("DIR! " + jar.toString());
		        Path jarFile = Paths.get(jar.toString().substring("file:/".length()));
		        FileSystem fs = FileSystems.newFileSystem(jarFile, null);
		        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fs.getPath(fooFolder));
		        for(Path p: directoryStream){
		            InputStream is = FileIO.class.getResourceAsStream(p.toString());
		            System.out.println("FILE from JAR! " + p.toString());
		            /** your logic here **/
		            copyRulefile(p.toString());
		        }
		    } catch(IOException e) {
		        e.printStackTrace();     
		    }
		}
		else{
		    /** IDE case */
		    Path path = Paths.get(uri);
		    try {
		        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
		        for(Path p : directoryStream){
		            InputStream is = new FileInputStream(p.toFile());
		            System.out.println("FILE! " + p.toString());
		            /** your logic here **/
		            copyRulefile(p.toString());
		        }
		    } catch (IOException _e) {
		    	_e.printStackTrace();
		    }
		}
	}
	
	
	public static void copyRulefile(String filePath) throws IOException {		
        InputStream is = new FileInputStream(filePath);
        //InputStream is = FileIO.class.getResourceAsStream("rulefiles/");
		byte[] buffer = new byte[is.available()];
		is.read(buffer);
		
		File file = new File(filePath);
		// if the file doesn't exist, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		
	    OutputStream os = new FileOutputStream(filePath); 
	    os.write(buffer);
	    os.close();		
	}
}
