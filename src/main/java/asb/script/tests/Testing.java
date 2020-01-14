package asb.script.tests;

import java.io.FileNotFoundException;
import java.io.IOException;

import asb.io.FileIO;
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

		String output = efr.translateToScript(input);
        long duration = System.nanoTime() - begin;
		//System.out.println(output);
		System.out.println("Transcription took " + String.valueOf(duration / 1000000) + "ms");

	}
	
	public static void main(String[] args) throws FileNotFoundException {
		//andMatchTest();
		replacerSpeedTest();
	}
}