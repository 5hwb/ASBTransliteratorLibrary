package asb.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
	 * Copy the rulefiles from the runnable JAR where this class was initiated.
	 * Useful for extracting default rulefiles for immediate use.
	 * From here: https://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file/20073154
	 */
	public void copyRulefilesFromAssets() {
		/** Intended rulefile directory */
		final String rulefileFolderDir = "rulefiles/";
		
		/** File object representing the JAR file */
		final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

		System.out.printf("jarFile.getPath()=%s\n", jarFile.getPath());
		System.out.printf("jarFile.getParent()=%s\n", jarFile.getParent());
		
		// Check if the JAR file is actually a file
		if (jarFile.isFile()) {
			System.out.println("JAR MODE!");
		    JarFile jar;
			try {
				// Get the path to the rulefiles folder, which will be
				// located in the same directory as the JAR
				rulefileDir = jarFile.getParent() + "/" + rulefileFolderDir;
				
				// Get all entries in the JAR
				jar = new JarFile(jarFile);
			    final Enumeration<JarEntry> entries = jar.entries();

				// Go thru all entries in the JAR
			    System.out.println("(JAR) Getting names...");
			    while (entries.hasMoreElements()) {
			        final String name = entries.nextElement().getName();
			        
			        // Go thru entries that start with the rulefile folder directory
			        if (name.startsWith(rulefileFolderDir)) {
			            System.out.println("===== name=" + name + "=====");
			            
			            // Copy the rulefile!
			            String filePath = name.replace(rulefileFolderDir, "");
			            InputStream is = jar.getInputStream(jar.getEntry(name));
			            copyFromInputStreamToFile(is, rulefileDir, filePath);
			        }
			    }
			    jar.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Copy the contents of an InputStream to a given file.
	 * @param is The InputStream to copy from
	 * @param folderPath Absolute directory to parent folder of file
	 * @param filePath Name of file to write the InputStream contents to
	 * @throws IOException
	 */
	private void copyFromInputStreamToFile(InputStream is, String folderPath, String filePath) throws IOException {		
		File folder = new File(folderPath);
		File file = new File(folder, filePath);
        System.out.println("folderPath=" + folderPath);
        System.out.println("filePath=" + filePath);

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
