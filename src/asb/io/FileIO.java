package asb.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
}
