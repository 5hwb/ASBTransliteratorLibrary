package asb.script.transcoder.parsing;

import java.util.Map;

import asb.schema.PhonemeRule;

/**
 * Handles the parsing of graphemes in an input string.
 * @author perry
 *
 */
public class Tokeniser {
	
	private int start = 0;
	private String input;
	private Map<String, PhonemeRule> mapping;
	private Map<String, Integer> graphemeVarIndexMap;
	private CharToken prevToken;
	private boolean isFirstToken = true;
	private int maxGraphemeSize = 0;
	
	private PhonemeRule defaultPhoneme;
	
	public Tokeniser(String input, Map<String, PhonemeRule> mapping, Map<String, Integer> graphemeVarIndexMap) {
		this.input = input;
		this.mapping = mapping;
		this.graphemeVarIndexMap = graphemeVarIndexMap;
		
		if (mapping != null) {
			// Find the maximum grapheme size required for the given mapping
			for (String key : mapping.keySet()) {
				String[] listOfL1Graphemes = mapping.get(key).l1();
				String[] listOfL2Graphemes = mapping.get(key).l2();
				
				// Update maxGraphemeSize to match the longest graphemes found
				for (String l1Grapheme : listOfL1Graphemes) {
					maxGraphemeSize = Math.max(l1Grapheme.length(), maxGraphemeSize);
				}
				for (String l2Grapheme : listOfL2Graphemes) {
					maxGraphemeSize = Math.max(l2Grapheme.length(), maxGraphemeSize);				
				}
			}
		}
		
		this.defaultPhoneme = new PhonemeRule(
				new String[] { "" }, "sentenceEdge", new String[] {""},
				new String[] { "" }, "sentenceEdge", new String[] {""});
		
		prevToken = new CharToken(this.defaultPhoneme, 0, null, null);
	}

	/**
	 * Get the previous token.
	 * @return A CharToken
	 */
	public CharToken prevToken() {
		return this.prevToken;
	}
	
	/**
	 * Read the next grapheme in the string input as a token.
	 * @return The CharToken corresponding to the next grapheme.
	 *         Returns null if the input string or grapheme-to-PhonemeRule mapping is null
	 *         or the end of the input string has been reached.
	 */
	public CharToken readNextToken() {
		// TODO change this to throw an exception
		if ((this.input == null || this.mapping == null)) {
			return null;
		}
		
		String currGrapheme = "";
		
		///////////////////////////////////////////////
		// LOOK UP GRAPHEME AND APPEND TO TOKEN LIST //
		///////////////////////////////////////////////
		/*DEBUG*/System.out.println("LOOK UP GRAPHEME...");
		
		// Looks ahead at the next chars, detecting graphemes of decreasing size
		// so that they get detected first.
		// First though, limit the lookup so it does not go beyond the end of the input.
		int limitValue = ((this.start + this.maxGraphemeSize) > this.input.length()) 
				? this.maxGraphemeSize - ((this.start + this.maxGraphemeSize) - input.length())
				: this.maxGraphemeSize;
		
		for (int limit = limitValue; limit >= 1; limit--) {
			// Limit the substring end index so it does not go beyond the end of the input
			int graphemeLimit = (start + limit >= input.length()) ? input.length() : start + limit;
			currGrapheme = input.substring(start, graphemeLimit);
			/*DEBUG*/System.out.printf("limit=%d currGrapheme=%s\n", limit, currGrapheme);

			int graphemeSize = 0;

			// Look up the reference HashMap with the current grapheme to see if there is an entry.
			PhonemeRule curr = this.mapping.get(currGrapheme);
			
			CharToken charToken;
			
			// If there is a match, add it to the phoneme stack
			if (curr != null) {
				/*DEBUG*/System.out.printf("\tPHONEME FOUND, INSERTING CORRS RULE TO STACK\n");
				charToken = new CharToken(curr, this.graphemeVarIndexMap.get(currGrapheme), prevToken, null);
				graphemeSize = limit;
				
				// Link the previous token to the current token
				prevToken.setNext(charToken);
				
				// Adjust current index to avoid parsing the components of the grapheme
				start += (graphemeSize); 
				
				prevToken = charToken;
				
				if (isFirstToken) {
					isFirstToken = false;
				}
				return charToken;
			}
			// Otherwise, insert the original (punctuation) character if it was not covered by a rule,
			// only if the grapheme is 1 char long.
			else if (limit == 1) {
				/*DEBUG*/System.out.printf("\tNO MATCH FOUND. INSERTING ORIGINAL PUNCTUATION INTO THE STACK\n");
				PhonemeRule punctPhoneme = new PhonemeRule(
						new String[] { currGrapheme }, "punctuation", new String[] {""},
						new String[] { currGrapheme }, "punctuation", new String[] {""});
				charToken = new CharToken(punctPhoneme, 0, prevToken, null);
				graphemeSize = 1;

				// Link the previous token to the current token
				prevToken.setNext(charToken);
				
				// Adjust current index to avoid parsing the components of the grapheme
				start += (graphemeSize); 

				prevToken = charToken;
				
				if (isFirstToken) {
					isFirstToken = false;
				}
				return charToken;
			}

			// Skip to the next iteration of the loop
			if (graphemeSize == 0)
				continue;
		}
		
		return null;
	}

}
