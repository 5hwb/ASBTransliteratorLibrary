package asb.script.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asb.ds.FixedStack;
import asb.io.FileIO;
import asb.schema.PhonemeRule;
import asb.script.transcoder.ExternalFileReplacer;

public class Testing {
	
	static ExternalFileReplacer efr;

	static void andMatchTest() {
		boolean[][] isMatch = {
				{false, false, false},
				{false, false, true},
				{false, true, true},
				{true, false, true},
				{true, true, true},
				};
		for (int i = 0; i < isMatch.length; i++) {
			boolean theyDoMatch = true;
			for (int j = 0; j < 3; j++) {
				theyDoMatch &= isMatch[i][j];
			}
			System.out.printf("[%b]\n", theyDoMatch);
		}
	}
	
	static void replacerSpeedTest() {
		efr = new ExternalFileReplacer("src/main/java/rulefiles/khmer.json");
		
		String input = null;
		try {
			input = FileIO.readFile("input.txt");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("There is an issue with loading the input file");
		}
	
        // Starts a timer to measure how long it took to read it
        long begin = System.nanoTime();

		String output = efr.translateFromScript(input);
        long duration = System.nanoTime() - begin;
		System.out.println(output);
		System.out.println("Transcription took " + String.valueOf(duration / 1000000) + "ms");

	}
	
	static void readerTest() throws IOException {
		String input = "this is a test. you fine?";
		Reader r = new StringReader(input);
		
		// Maps graphemes to phonemes
		Map<String, String> map = new HashMap<>();
		map.put("th", "đ");
		map.put("i", "i");
		map.put("s", "s");
		map.put("a", "e");
		map.put("e", "é");
		map.put("t", "t");
		map.put("y", "y");
		map.put("ou", "ù");
		map.put("f", "f");
		map.put("ine", "ān");

		
		int currChar;
		String currGrapheme = "";
		boolean isDone = false;
		FixedStack<String> detectedGraphemes = new FixedStack<>(4);
		
		List<String> tokens = new ArrayList<>();
		
		// Go thru each char
		while ((currChar = r.read()) != -1) {
			currGrapheme += (char) currChar;
			System.out.printf("currGrapheme=%s\n", currGrapheme);
			
			// Check if curr grapheme matches anything in the map
			if (map.containsKey(currGrapheme)) {
				System.out.println("Match!");
			} else {
				System.out.println("No match!");
				
				tokens.add(currGrapheme.substring(0, currGrapheme.length() - 1));
				currGrapheme = "" + (char) currChar;
			}
		}
		
		for (String s : tokens) {
			System.out.println("TOKEN: " + s);
		}
		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
//		andMatchTest();
		replacerSpeedTest();
//		try {
//			readerTest();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}