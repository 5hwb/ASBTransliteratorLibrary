package asb.script.transcoder.parsing;

import java.util.Map;

import asb.schema.PhonemeRule;

public class Tokeniser {
	
	private int start = 0;
	private String input;
	private Map<String, PhonemeRule> mapping;
	private Map<String, Integer> graphemeVarIndexMap;
	private CharToken prevToken;
	private boolean isFirstToken = true;
	private int maxGraphemeSize = 6; // TODO change this when mature
	
	private PhonemeRule defaultPhoneme;
	
	public Tokeniser(String input, Map<String, PhonemeRule> mapping, Map<String, Integer> graphemeVarIndexMap) {
		this.input = input;
		this.mapping = mapping;
		this.graphemeVarIndexMap = graphemeVarIndexMap;
		
		this.defaultPhoneme = new PhonemeRule(
				new String[] { "" }, "punctuation", new String[] {""},
				new String[] { "" }, "punctuation", new String[] {""});
		
		prevToken = new CharToken(this.defaultPhoneme, 0, null, null);
	}
	
	public CharToken prevToken() {
		return this.prevToken;
	}
	
	public CharToken readNextToken() {
		
		if ((this.input == null || this.mapping == null) || start > this.input.length()) {
			return null;
		}
		
		String currGrapheme = "";
		
		///////////////////////////////////////////////
		// LOOK UP GRAPHEME AND APPEND TO TOKEN LIST //
		///////////////////////////////////////////////
		/*DEBUG*/System.out.println("LOOK UP GRAPHEME...");
		// Looks ahead at the next chars, detecting graphemes of decreasing size
		// so that they get detected first
		for (int limit = this.maxGraphemeSize; limit >= 1; limit--) {
			// Limit the lookup so it does not go beyond the end of the input
			int graphemeLimit = (start + limit >= input.length()) ? input.length() : start + limit;
			currGrapheme = input.substring(start, graphemeLimit);
			/*DEBUG*/System.out.printf("limit=%d currGrapheme=%s\n", limit, currGrapheme);

			int graphemeSize = 0;

			// Look up the reference HashMap with the current grapheme to see if there is an entry.
			PhonemeRule curr = this.mapping.get(currGrapheme);
			// If there is a match, add it to the phoneme stack
			if (curr != null) {
				/*DEBUG*/System.out.printf("\tPHONEME FOUND, INSERTING CORRS RULE TO STACK\n");
				CharToken charToken = new CharToken(curr, this.graphemeVarIndexMap.get(currGrapheme), prevToken, null);
				if (isFirstToken) {
					isFirstToken = false;
				}
				graphemeSize = limit;
				
				// Link the previous token to the current token
				prevToken.setNext(charToken);
				
				// Adjust current index to avoid parsing the components of the grapheme
				start += (graphemeSize); 
				
				prevToken = charToken;
				return charToken;
			}
			// Otherwise, insert the original (punctuation) character if it was not covered by a rule,
			// only if the grapheme is 1 char long.
			else if (limit == 1) {
				/*DEBUG*/System.out.printf("\tNO MATCH FOUND. INSERTING ORIGINAL PUNCTUATION INTO THE STACK\n");
				PhonemeRule defaultPhoneme = new PhonemeRule(
						new String[] { currGrapheme }, "punctuation", new String[] {""},
						new String[] { currGrapheme }, "punctuation", new String[] {""});
				CharToken charToken = new CharToken(defaultPhoneme, 0, prevToken, null);
				if (isFirstToken) {
					isFirstToken = false;
				}
				graphemeSize = 1;

				// Link the previous token to the current token
				prevToken.setNext(charToken);
				
				// Adjust current index to avoid parsing the components of the grapheme
				start += (graphemeSize); 

				prevToken = charToken;
				return charToken;
			}

			// Skip to the next iteration of the loop
			if (graphemeSize == 0)
				continue;
		}
		
		return null;
	}

}
