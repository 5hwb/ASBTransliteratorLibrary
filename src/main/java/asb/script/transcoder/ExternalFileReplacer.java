package asb.script.transcoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import asb.schema.Rule;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import asb.ds.FixedStack;
import asb.io.FileIO;
import asb.schema.PhonemeCounter;
import asb.schema.PhonemeRule;
import asb.schema.PhonemeType;
import asb.schema.RuleSchema;
import asb.script.transcoder.parsing.CharToken;
import asb.script.transcoder.parsing.Tokeniser;

/**
 * An ExternalFileReplacer replaces each Latin script character in EBEO with the
 * corresponding letter for an adapted script.
 *
 * @author perry
 *
 */
public class ExternalFileReplacer {

	/**
	 * These HashMaps map String keys representing the original char with
	 * PhonemeRule class values representing what they will be replaced with
	 */
	protected Map<String, PhonemeRule> l1GraphemeToPhonemeMap; // Latin grapheme -> Script PhonemeRule
	protected Map<String, PhonemeRule> l2GraphemeToPhonemeMap; // Script grapheme -> Latin PhonemeRule
	protected Map<String, PhonemeRule> graphemeToPhonemeMap; // Grapheme -> corresponding PhonemeRule
	protected Map<String, Integer> graphemeVarIndexMap; // Grapheme -> its index in the list of phoneme variants

	/** Stores the output string */
	protected StringBuilder output;

	/** The current grapheme to analyse */
	// protected String currGrapheme;

	/** Stack of the last 3 phonemes */
	protected FixedStack<PhonemeRule> phonemeStack;
	
	/** Stack of the phoneme variant indexes of the last 3 selected graphemes */
	protected FixedStack<Integer> graphemeVarIndexStack;

	/** The PhonemeRule which will replace the currently selected grapheme */
	protected PhonemeRule replacementPhoneme;

	/** A placeholder PhonemeRule for non-replaced characters */
	protected PhonemeRule defaultPhoneme;

	/** The maximum grapheme size, which determines the number of chars to scan ahead */
	protected int maxGraphemeSize;

	/** Indicate if the replacer's script supports case */
	protected boolean hasCase;

	/** Consonant counters */
	protected Map<String, PhonemeCounter> consoTypeToCounterMap;

	/** Reference hashmap for phoneme types */
	protected Map<String, PhonemeType> PhonemeTypeReferenceMap;

	protected String ruleFile;

	public ExternalFileReplacer(String filePath) {
		initialiseValues();
		loadJsonRulefile(readExternalJsonFile(filePath));
	}

	/**
	 * Initialise the ExternalFileReplacer values
	 */
	private void initialiseValues() {
		this.l1GraphemeToPhonemeMap = new HashMap<String, PhonemeRule>();
		this.l2GraphemeToPhonemeMap = new HashMap<String, PhonemeRule>();
		this.graphemeToPhonemeMap = new HashMap<String, PhonemeRule>();
		this.PhonemeTypeReferenceMap = new HashMap<String, PhonemeType>();
		this.graphemeVarIndexMap = new HashMap<String, Integer>();
		this.phonemeStack = new FixedStack<PhonemeRule>(3);
		this.graphemeVarIndexStack = new FixedStack<Integer>(3);
		this.consoTypeToCounterMap = new HashMap<String, PhonemeCounter>();
		this.maxGraphemeSize = 0;
		this.hasCase = false;
	}

	/**
	 * Translate a Latin EBEO text into the desired script
	 *
	 * @param input Self-descriptive
	 * @return
	 */
	public String translateToScript(String input) {
		return translateFromToScript(input, true);
	}

	/**
	 * Translate a text in the desired script into Latin EBEO script
	 *
	 * @param input Self-descriptive
	 * @return
	 */
	public String translateFromScript(String input) {
		return translateFromToScript(input, false);
	}

	/**
	 * The base method for the translateFromScript and translateToScript methods.
	 *
	 * @param input    Self-descriptive
	 * @param toScript Translate the input to script, or back?
	 * @return The transliterated output
	 */
	protected String translateFromToScript(String input, boolean toScript) {
		/**
		 * NOTE: A 'grapheme' is a string of up to n characters representing a single phoneme.
		 */
		ArrayList<CharToken> tokenOutput = new ArrayList<>();
		output = new StringBuilder();

		/* The current grapheme to analyse */
		String currGrapheme = "";
		
		Set<String> counterKeySet = consoTypeToCounterMap.keySet();

		/*DEBUG*/System.out.println("Initialised!");

		// Reset the counters
		for (String key : counterKeySet) {
			/*DEBUG*/System.out.printf("Resetting %s...\n", key);			
			consoTypeToCounterMap.get(key).reset();
		}

		///////////////////////////////////////////////
		// LOOK UP GRAPHEME AND APPEND TO TOKEN LIST //
		///////////////////////////////////////////////
		Map<String, PhonemeRule> mapping = (toScript) 
				? this.l1GraphemeToPhonemeMap 
				: this.l2GraphemeToPhonemeMap;
		Tokeniser tokeniser = new Tokeniser(input, mapping, graphemeVarIndexMap);
		CharToken token;

		// Process all chars in the input string.
		while ((token = tokeniser.readNextToken()) != null) {
			CharToken prev = tokeniser.prevToken();
			tokenOutput.add(prev);
		}
		
		//////////////////////////////////////////
		// INSERT REPLACEMENT IN OUTPUT         //
		//////////////////////////////////////////

		for (CharToken cToken : tokenOutput) {
			/*DEBUG*/System.out.println(cToken);
			
			// TODO find out why last char sometimes doesn't get inserted in output!
			if (cToken.next() == null)
				break;
			
			/*DEBUG*/System.out.println("INSERT REPLACEMENT IN OUTPUT...");
			// Get the PhonemeRule for the currently selected grapheme
			replacementPhoneme = cToken.phonemeRule();
			Integer currGraphemeIndex = cToken.graphemeVarIndex()/*graphemeVarIndexStack.nthTop(1)*/;

			// If no replacement phoneme could be found, the current phoneme is a non-defined punctuation mark
			if (cToken.phonemeRule() == null) {
				/*DEBUG*/System.out.println("GRAPHEME: no repl found");
				output.append(currGrapheme);
				continue;
			}
			/*DEBUG*/System.out.println("GRAPHEME: repl found - " + cToken.phonemeRule().l2()[0]);

			// Increment the counter for the current phoneme's type
			String currType = (toScript) ? PhonemeTypeReferenceMap.get(cToken.phonemeRule().l2type()).name()
					: PhonemeTypeReferenceMap.get(cToken.phonemeRule().l1type()).name();
			PhonemeCounter pCounter = consoTypeToCounterMap.get(currType);
			if (pCounter != null) {
				/*DEBUG*/System.out.printf("Counter for '%s' value: %d\n", currType, pCounter.value());
				Rule[] cRules = pCounter.incrRuleParsed();
				int matchingRuleIndex = selectRule(cToken, cRules, toScript, null, null);
				if (matchingRuleIndex >= 0) {
					/*DEBUG*/System.out.printf("\tRule for counter increment is a match. index=%d, matchingRule=%s\n", matchingRuleIndex, cRules[matchingRuleIndex]);
					pCounter.increment();
				} else {
					/*DEBUG*/System.out.println("\tRule for counter increment is NOT a match");
				}
			} else {
				/*DEBUG*/System.out.printf("Counter for '%s' does not exist\n", currType);
			}

			// Select the grapheme to append
			Rule[] pRules = (toScript) ? cToken.phonemeRule().l2ruleParsed() : cToken.phonemeRule().l1ruleParsed();
			int letterIndex = selectRule(cToken, pRules, toScript, pCounter, currGraphemeIndex);
			if (letterIndex < 0) {
				// default letter is the last one
				letterIndex = (toScript) ? cToken.phonemeRule().l2().length - 1 : cToken.phonemeRule().l1().length - 1;
			}

			// Reset counter values if they have reached the maximum value
			for (String key : counterKeySet) {
				boolean counterIsMax = !(key.equals(currType) && !consoTypeToCounterMap.get(key).valueIsMax());
				if (counterIsMax) {
					consoTypeToCounterMap.get(key).reset(); // reset counter value to 0
				}
			}

			// Append the replacement grapheme
			/*DEBUG*/System.out.printf("OUTPUT: [%s]\n", output.toString());
			String repl = (toScript) ? cToken.phonemeRule().l2()[letterIndex] : cToken.phonemeRule().l1()[letterIndex];
			output.append(repl);
//			output.append(cToken.phonemeRule().l1()[0]);
		}

		return output.toString();
	}
	
	/**
	 * Look for a rule that matches the current situation
	 *
	 * @param pRules   List of rules to check for matches
	 * @param toScript Translate the input to script, or back?
	 * @param pCounter Phoneme counter for this type
	 * @param pVariantIndex The index of the selected grapheme in phoneme's variant list
	 * @return Index of matching rule. -1 if no match was found
	 */
	private int selectRule(CharToken cToken, Rule[] pRules, boolean toScript, PhonemeCounter pCounter, Integer pVariantIndex) {
		String prevType = (toScript) ? cToken.prev().phonemeRule().l2type() : cToken.prev().phonemeRule().l1type();
		String currType = (toScript) ? cToken.phonemeRule().l2type() : cToken.phonemeRule().l1type();
		String nextType = (toScript) ? cToken.next().phonemeRule().l2type() : cToken.next().phonemeRule().l1type();

		// Go through each rule until one matching the current pattern is found
		int letterIndex = -1;
		for (int i = 0; i < pRules.length; i++) {
			
			boolean isAndRuleMatch = pRules[i].isAndRuleMatch();
			boolean subRulesDoMatch = true;
			/*DEBUG*/System.out.printf("\t\tISANDRULEMATCH: [%b]\n", isAndRuleMatch);
			/*DEBUG*/System.out.printf("\t\tSUBRULESDOMATCH: [%b]\n", subRulesDoMatch);

			// Go through each subrule: should be either AND (all must match) or OR (at least 1 must match).
			// If any 1 of them matches the current pattern, select its corresponding grapheme for insertion to output
			for (int j = 0; j < pRules[i].numOfSubRules(); j++) {
				/*DEBUG*/System.out.printf("\t\tGoing thru subrule num %d\n", j);

				//////////////////////////////
				// Rule is a pattern rule   //
				//////////////////////////////
				if (pRules[i].subRulecVal(j) == 0 && pRules[i].subRulePvVal(j) < 0) {

					// COMPARISON!
					boolean prevIsMatch = (pRules[i].subsubRuleType(j, 0).equals("anything"))
							? true // always true if it matches 'anything'
							: (pRules[i].subsubRuleIsNot(j, 0))
								? !typeEquals(pRules[i].subsubRuleType(j, 0), prevType, pRules[i].subsubRuleIsStrictTypeMatch(j, 0))
								: typeEquals(pRules[i].subsubRuleType(j, 0), prevType, pRules[i].subsubRuleIsStrictTypeMatch(j, 0));
					/*DEBUG*/System.out.printf("\t\tPREVMATCH: [%b]\n", prevIsMatch);

					boolean currIsMatch = (pRules[i].subsubRuleType(j, 1).equals("anything"))
							? true // always true if it matches 'anything'
							: (pRules[i].subsubRuleIsNot(j, 1))
								? !typeEquals(pRules[i].subsubRuleType(j, 1), currType, pRules[i].subsubRuleIsStrictTypeMatch(j, 1))
								: typeEquals(pRules[i].subsubRuleType(j, 1), currType, pRules[i].subsubRuleIsStrictTypeMatch(j, 1));
					/*DEBUG*/System.out.printf("\t\tCURRMATCH: [%b]\n", currIsMatch);

					boolean nextIsMatch = (pRules[i].subsubRuleType(j, 2).equals("anything"))
							? true // always true if it matches 'anything'
							: (pRules[i].subsubRuleIsNot(j, 2))
								? !typeEquals(pRules[i].subsubRuleType(j, 2), nextType, pRules[i].subsubRuleIsStrictTypeMatch(j, 2))
								: typeEquals(pRules[i].subsubRuleType(j, 2), nextType, pRules[i].subsubRuleIsStrictTypeMatch(j, 2));
					/*DEBUG*/System.out.printf("\t\tNEXTMATCH: [%b]\n", nextIsMatch);
					/*DEBUG*/System.out.printf("\t\tRule num: %d\n", i);

					// Match if the pattern matches the scenario
					boolean isMatch = ((prevIsMatch && nextIsMatch) && currIsMatch);
					subRulesDoMatch &= isMatch;
					/*DEBUG*/System.out.printf("\t\tPatmat's ISMATCH: [%b]\n", isMatch);
					if (isMatch && !isAndRuleMatch) {
						letterIndex = i;
						/*DEBUG*/System.out.printf("\t\tChosen PATTERN rule num: %d\n", i);
						return letterIndex;
					}
				}
				//////////////////////////////
				// Rule is a counter rule   //
				//////////////////////////////
				else if (pRules[i].subRulecVal(j) >= 1 && pCounter != null) {
					int cVal = pRules[i].subRulecVal(j);
					/*DEBUG*/System.out.printf("\t\tRULEd counter? curr counter val = %d\n", pCounter.value());

					// Match if counter value for current phoneme's type equals cVal.
					// Useful for consonant clusters
					boolean isMatch = (pCounter.value() >= cVal);
					subRulesDoMatch &= isMatch;
					/*DEBUG*/System.out.printf("\t\tCountmat's ISMATCH: [%b]\n", isMatch);
					if (isMatch && !isAndRuleMatch) {
						pCounter.reset(); // reset counter value to 0
						letterIndex = i;
						/*DEBUG*/System.out.printf("\t\tChosen matching COUNTER rule num: %d\n", i);
						return letterIndex;
					}
				}
				//////////////////////////////
				// Rule is a phoneme variant selection rule
				//////////////////////////////
				else if (pRules[i].subRulePvVal(j) >= 0 && pVariantIndex != null) {
					int pvVal = pRules[i].subRulePvVal(j);
					/*DEBUG*/System.out.printf("\t\tRULEd phovarsel? curr variant val = %d\n", pVariantIndex);

					// Match if counter value for current phoneme's type equals cVal.
					// Useful for scripts that have uppercase and lowercase forms
					boolean isMatch = (pVariantIndex == pvVal);
					subRulesDoMatch &= isMatch;
					/*DEBUG*/System.out.printf("\t\tPhovarsel's ISMATCH: [%b]\n", isMatch);
					if (isMatch && !isAndRuleMatch) {
						letterIndex = i;
						/*DEBUG*/System.out.printf("\t\tChosen matching PHOVARSEL rule num: %d\n", i);
						return letterIndex;
					}
				}
			}
			
			if (isAndRuleMatch && subRulesDoMatch) {
				letterIndex = i;
				/*DEBUG*/System.out.printf("\t\tAll subrules match! Rule num: %d\n", i);
				return letterIndex;
			} else {
				/*DEBUG*/System.out.printf("\t\tAll subrules do not match.\n");
			}
		}
		return letterIndex;
	}
	
	
	/**
	 * (OLD) The base method for the translateFromScript and translateToScript methods.
	 *
	 * @param input    Self-descriptive
	 * @param toScript Translate the input to script, or back?
	 * @return The transliterated output
	 */
	protected String translateFromToScriptOld(String input, boolean toScript) {
		/**
		 * NOTE: A 'grapheme' is a string of up to n characters representing a single phoneme.
		 */
		output = new StringBuilder();

		/* The current grapheme to analyse */
		String currGrapheme = "";

		// Insert the 1st phoneme with the type SENTENCEEND, since the beginning marks the start of a new sentence
		PhonemeRule initPhoneme = new PhonemeRule(
				new String[] { "" }, "sentenceEdge", new String[] {""},
				new String[] { "" }, "sentenceEdge", new String[] {""});
		phonemeStack.fill(initPhoneme); // fill it so the phoneme stack is never empty

		Set<String> counterKeySet = consoTypeToCounterMap.keySet();

		/*DEBUG*/System.out.println("Initialised!");

		// Reset the counters
		for (String key : counterKeySet) {
			consoTypeToCounterMap.get(key).reset();
		}

		// Process all chars in the input string.
		// Need to accomodate the maximum possible grapheme length in the for loop
		for (int i = 0; i < input.length() + maxGraphemeSize; i++) {
			/*DEBUG*/System.out.printf("----------i=%d----------\n", i);

			////////////////////////////////////////////
			// LOOK UP GRAPHEME AND INSERT INTO STACK //
			////////////////////////////////////////////
			/*DEBUG*/System.out.println("LOOK UP GRAPHEME AND INSERT INTO STACK...");
			if (i < input.length()) {
				// Looks ahead at the next chars, detecting graphemes of decreasing size
				// so that they get detected first
				for (int limit = this.maxGraphemeSize; limit >= 1; limit--) {
					/*DEBUG*/System.out.printf("limit=%d\n", limit);
					// Limit the lookup so it does not go beyond the end of the input
					int graphemeLimit = (i + limit >= input.length()) ? input.length() : i + limit;
					currGrapheme = input.substring(i, graphemeLimit);

					int graphemeSize = 0;

					// Look up the reference HashMap with the current grapheme to see if there is an entry.
					PhonemeRule curr = (toScript) ? this.l1GraphemeToPhonemeMap.get(currGrapheme)
							: this.l2GraphemeToPhonemeMap.get(currGrapheme);
					// If there is a match, add it to the phoneme stack
					if (curr != null) {
						/*DEBUG*/System.out.printf("\tPHONEME FOUND, INSERTING CORRS RULE TO STACK\n");
						phonemeStack.push(curr);
						graphemeVarIndexStack.push(graphemeVarIndexMap.get(currGrapheme));
						graphemeSize = limit;
					}
					// Otherwise, insert the original (punctuation) character if it was not covered by a rule,
					// only if the grapheme is 1 char long.
					else if (limit == 1) {
						/*DEBUG*/System.out.printf("\tNO MATCH FOUND. INSERTING ORIGINAL PUNCTUATION INTO THE STACK\n");
						defaultPhoneme = new PhonemeRule(
								new String[] { currGrapheme }, "punctuation", new String[] {""},
								new String[] { currGrapheme }, "punctuation", new String[] {""});
						phonemeStack.push(defaultPhoneme);
						graphemeVarIndexStack.push(null); // no phoneme variant index if there's no corresponding phoneme
						graphemeSize = 1;
					}

					// Skip to the next iteration of the loop
					if (graphemeSize == 0)
						continue;

					// Adjust current index to avoid parsing the components of the grapheme
					i += (graphemeSize - 1); // subtract 1 since the next iteration of the for loop will add 1 again
					break;
				}
			}
			// The last 3 graphemes are in the stack, but the last grapheme hasn't been processed yet.
			// Insert a dummy PhonemeRule to push the last grapheme into position
			else {
				/*DEBUG*/System.out.println("\tDummy phonemerule inserted");
				phonemeStack.push(initPhoneme);
				graphemeVarIndexStack.push(null);
			}

			//////////////////////////////////////////
			// INSERT REPLACEMENT IN OUTPUT         //
			//////////////////////////////////////////
			/*DEBUG*/System.out.println("INSERT REPLACEMENT IN OUTPUT...");
			// Get the PhonemeRule for the currently selected grapheme
			replacementPhoneme = currPhoneme();
			Integer currGraphemeIndex = graphemeVarIndexStack.nthTop(1);

			// If no replacement phoneme could be found, the current phoneme is a non-defined punctuation mark
			if (replacementPhoneme == null) {
				/*DEBUG*/System.out.println("GRAPHEME: no repl found");
				output.append(currGrapheme);
				continue;
			}
			/*DEBUG*/System.out.println(phonemeStack);
			/*DEBUG*/System.out.println(graphemeVarIndexStack);
			/*DEBUG*/System.out.println("GRAPHEME: repl found - " + replacementPhoneme.l2()[0]);

			// Increment the counter for the current phoneme's type
			String currType = (toScript) ? PhonemeTypeReferenceMap.get(replacementPhoneme.l2type()).name()
					: PhonemeTypeReferenceMap.get(replacementPhoneme.l1type()).name();
			PhonemeCounter pCounter = consoTypeToCounterMap.get(currType);
			if (pCounter != null) {
				/*DEBUG*/System.out.printf("Counter for '%s' value: %d\n", currType, pCounter.value());
				Rule[] cRules = pCounter.incrRuleParsed();
				int matchingRuleIndex = selectRuleOld(cRules, toScript, null, null);
				if (matchingRuleIndex >= 0) {
					/*DEBUG*/System.out.printf("\tRule for counter increment is a match. index=%d, matchingRule=%s\n", matchingRuleIndex, cRules[matchingRuleIndex]);
					pCounter.increment();
				} else {
					/*DEBUG*/System.out.println("\tRule for counter increment is NOT a match");
				}
			} else {
				/*DEBUG*/System.out.printf("Counter for '%s' does not exist\n", currType);
			}

			// Select the grapheme to append
			Rule[] pRules = (toScript) ? replacementPhoneme.l2ruleParsed() : replacementPhoneme.l1ruleParsed();
			int letterIndex = selectRuleOld(pRules, toScript, pCounter, currGraphemeIndex);
			if (letterIndex < 0) {
				// default letter is the last one
				letterIndex = (toScript) ? replacementPhoneme.l2().length - 1 : replacementPhoneme.l1().length - 1;
			}

			// Reset counter values if they have reached the maximum value
			for (String key : counterKeySet) {
				boolean counterIsMax = !(key.equals(currType) && !consoTypeToCounterMap.get(key).valueIsMax());
				if (counterIsMax) {
					consoTypeToCounterMap.get(key).reset(); // reset counter value to 0
				}
			}

			// Append the replacement grapheme
			/*DEBUG*/System.out.printf("OUTPUT: [%s]\n", output.toString());
			String repl = (toScript) ? replacementPhoneme.l2()[letterIndex] : replacementPhoneme.l1()[letterIndex];
			output.append(repl);
		}

		return output.toString();
	}

	/**
	 * Look for a rule that matches the current situation
	 *
	 * @param pRules   List of rules to check for matches
	 * @param toScript Translate the input to script, or back?
	 * @param pCounter Phoneme counter for this type
	 * @param pVariantIndex The index of the selected grapheme in phoneme's variant list
	 * @return Index of matching rule. -1 if no match was found
	 */
	private int selectRuleOld(Rule[] pRules, boolean toScript, PhonemeCounter pCounter, Integer pVariantIndex) {
		String prevType = (toScript) ? prevPhoneme().l2type() : prevPhoneme().l1type();
		String currType = (toScript) ? replacementPhoneme.l2type() : replacementPhoneme.l1type();
		String nextType = (toScript) ? nextPhoneme().l2type() : nextPhoneme().l1type();

		// Go through each rule until one matching the current pattern is found
		int letterIndex = -1;
		for (int i = 0; i < pRules.length; i++) {
			
			boolean isAndRuleMatch = pRules[i].isAndRuleMatch();
			boolean subRulesDoMatch = true;
			/*DEBUG*/System.out.printf("\t\tISANDRULEMATCH: [%b]\n", isAndRuleMatch);
			/*DEBUG*/System.out.printf("\t\tSUBRULESDOMATCH: [%b]\n", subRulesDoMatch);

			// Go through each subrule: should be either AND (all must match) or OR (at least 1 must match).
			// If any 1 of them matches the current pattern, select its corresponding grapheme for insertion to output
			for (int j = 0; j < pRules[i].numOfSubRules(); j++) {
				/*DEBUG*/System.out.printf("\t\tGoing thru subrule num %d\n", j);

				//////////////////////////////
				// Rule is a pattern rule   //
				//////////////////////////////
				if (pRules[i].subRulecVal(j) == 0 && pRules[i].subRulePvVal(j) < 0) {

					// COMPARISON!
					boolean prevIsMatch = (pRules[i].subsubRuleType(j, 0).equals("anything"))
							? true // always true if it matches 'anything'
							: (pRules[i].subsubRuleIsNot(j, 0))
								? !typeEquals(pRules[i].subsubRuleType(j, 0), prevType, pRules[i].subsubRuleIsStrictTypeMatch(j, 0))
								: typeEquals(pRules[i].subsubRuleType(j, 0), prevType, pRules[i].subsubRuleIsStrictTypeMatch(j, 0));
					/*DEBUG*/System.out.printf("\t\tPREVMATCH: [%b]\n", prevIsMatch);

					boolean currIsMatch = (pRules[i].subsubRuleType(j, 1).equals("anything"))
							? true // always true if it matches 'anything'
							: (pRules[i].subsubRuleIsNot(j, 1))
								? !typeEquals(pRules[i].subsubRuleType(j, 1), currType, pRules[i].subsubRuleIsStrictTypeMatch(j, 1))
								: typeEquals(pRules[i].subsubRuleType(j, 1), currType, pRules[i].subsubRuleIsStrictTypeMatch(j, 1));
					/*DEBUG*/System.out.printf("\t\tCURRMATCH: [%b]\n", currIsMatch);

					boolean nextIsMatch = (pRules[i].subsubRuleType(j, 2).equals("anything"))
							? true // always true if it matches 'anything'
							: (pRules[i].subsubRuleIsNot(j, 2))
								? !typeEquals(pRules[i].subsubRuleType(j, 2), nextType, pRules[i].subsubRuleIsStrictTypeMatch(j, 2))
								: typeEquals(pRules[i].subsubRuleType(j, 2), nextType, pRules[i].subsubRuleIsStrictTypeMatch(j, 2));
					/*DEBUG*/System.out.printf("\t\tNEXTMATCH: [%b]\n", nextIsMatch);
					/*DEBUG*/System.out.printf("\t\tRule num: %d\n", i);

					// Match if the pattern matches the scenario
					boolean isMatch = ((prevIsMatch && nextIsMatch) && currIsMatch);
					subRulesDoMatch &= isMatch;
					/*DEBUG*/System.out.printf("\t\tPatmat's ISMATCH: [%b]\n", isMatch);
					if (isMatch && !isAndRuleMatch) {
						letterIndex = i;
						/*DEBUG*/System.out.printf("\t\tChosen PATTERN rule num: %d\n", i);
						return letterIndex;
					}
				}
				//////////////////////////////
				// Rule is a counter rule   //
				//////////////////////////////
				else if (pRules[i].subRulecVal(j) >= 1 && pCounter != null) {
					int cVal = pRules[i].subRulecVal(j);
					/*DEBUG*/System.out.printf("\t\tRULEd counter? curr counter val = %d\n", pCounter.value());

					// Match if counter value for current phoneme's type equals cVal.
					// Useful for consonant clusters
					boolean isMatch = (pCounter.value() >= cVal);
					subRulesDoMatch &= isMatch;
					/*DEBUG*/System.out.printf("\t\tCountmat's ISMATCH: [%b]\n", isMatch);
					if (isMatch && !isAndRuleMatch) {
						pCounter.reset(); // reset counter value to 0
						letterIndex = i;
						/*DEBUG*/System.out.printf("\t\tChosen matching COUNTER rule num: %d\n", i);
						return letterIndex;
					}
				}
				//////////////////////////////
				// Rule is a phoneme variant selection rule
				//////////////////////////////
				else if (pRules[i].subRulePvVal(j) >= 0 && pVariantIndex != null) {
					int pvVal = pRules[i].subRulePvVal(j);
					/*DEBUG*/System.out.printf("\t\tRULEd phovarsel? curr variant val = %d\n", pVariantIndex);

					// Match if counter value for current phoneme's type equals cVal.
					// Useful for scripts that have uppercase and lowercase forms
					boolean isMatch = (pVariantIndex == pvVal);
					subRulesDoMatch &= isMatch;
					/*DEBUG*/System.out.printf("\t\tPhovarsel's ISMATCH: [%b]\n", isMatch);
					if (isMatch && !isAndRuleMatch) {
						letterIndex = i;
						/*DEBUG*/System.out.printf("\t\tChosen matching PHOVARSEL rule num: %d\n", i);
						return letterIndex;
					}
				}
			}
			
			if (isAndRuleMatch && subRulesDoMatch) {
				letterIndex = i;
				/*DEBUG*/System.out.printf("\t\tAll subrules match! Rule num: %d\n", i);
				return letterIndex;
			} else {
				/*DEBUG*/System.out.printf("\t\tAll subrules do not match.\n");
			}
		}
		return letterIndex;
	}

	/**
	 * Compare 2 phoneme types to see if they match. Matches main types with their
	 * sub-types as defined in the replacer rules file.
	 *
	 * @param a                 1st type
	 * @param b                 2nd type
	 * @param isStrictTypeMatch If true, only match if a == b . If false, allow
	 *                          matches between subtypes and main types
	 * @return Whether the 2 phoneme types are a match
	 */
	private boolean typeEquals(String a, String b, boolean isStrictTypeMatch) {
		boolean matchesMainType = (isStrictTypeMatch) ? false
				: (PhonemeTypeReferenceMap.get(a).name().equals(b) || PhonemeTypeReferenceMap.get(b).name().equals(a));
		boolean matchesSubType = (a.equals(b));
		/*DEBUG*/System.out.printf("\t\t\ttypeEquals(%s, %s): matchesMainType=%b, matchesSubType=%b\n",
		/*DEBUG*/		a, b, matchesMainType, matchesSubType);
		return matchesMainType || matchesSubType;
	}

	/**
	 *  Read the contents of an external JSON rulefile.
	 * @param filePath The filepath to the JSON rulefile
	 * @return The rulefile contents
	 */
	public String readExternalJsonFile(String filePath) {
		String jsonRulefile = null;
		
		try {
			jsonRulefile = FileIO.readFile(filePath);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("There is an issue with loading the JSON rules file");
		}
		
		return jsonRulefile;
	}
	
	/**
	 * Load the phoneme rules into the replacer.
	 * @param jsonRulefile The JSON rulefile string to process
	 */
	public void loadJsonRulefile(String jsonRulefile) {
		Gson rulefileGson = new Gson();

		try {
			RuleSchema rulefileSchema = rulefileGson.fromJson(jsonRulefile, RuleSchema.class);

			// Get the counters
			for (int i = 0; i < rulefileSchema.counters().length; i++) {
				PhonemeCounter phonemeCounter = rulefileSchema.counters()[i];

				// Parse phoneme counter rule strings into Rule objects
				Rule[] rule = new Rule[phonemeCounter.incrRule().length];
				for (int j = 0; j < phonemeCounter.incrRule().length; j++) {
					rule[j] = Rule.parseStringToRule(phonemeCounter.incrRule()[j]);
					phonemeCounter.setIncrRuleParsed(rule);
				}

				consoTypeToCounterMap.put(phonemeCounter.type(), phonemeCounter);
			}

			// Read each phoneme
			for (int i = 0; i < rulefileSchema.rules().length; i++) {
				PhonemeRule phonemeRule = rulefileSchema.rules()[i];

				// Parse phoneme rule strings into Rule objects
				Rule[] rule1 = new Rule[phonemeRule.l1rule().length];
				Rule[] rule2 = new Rule[phonemeRule.l2rule().length];
				for (int j = 0; j < phonemeRule.l1rule().length; j++) {
					rule1[j] = Rule.parseStringToRule(phonemeRule.l1rule()[j]);
					phonemeRule.setL1ruleParsed(rule1);
				}
				for (int j = 0; j < phonemeRule.l2rule().length; j++) {
					rule2[j] = Rule.parseStringToRule(phonemeRule.l2rule()[j]);
					phonemeRule.setL2ruleParsed(rule2);
				}

				// Read letter 1 graphemes
				for (int j = 0; j < phonemeRule.l1().length; j++) {
					String letter1Grapheme = phonemeRule.l1()[j];
					l1GraphemeToPhonemeMap.put(letter1Grapheme, phonemeRule);
					graphemeToPhonemeMap.put(letter1Grapheme, phonemeRule);
					graphemeVarIndexMap.put(letter1Grapheme, j);

					// Update maxGraphemeSize to match the longest grapheme found
					maxGraphemeSize = Math.max(letter1Grapheme.length(), maxGraphemeSize);
					/*DEBUG*/System.out.printf("1: Loading %s, maxGraphemeSize = %d\n", letter1Grapheme, maxGraphemeSize);
				}

				// Read letter 2 graphemes
				for (int j = 0; j < phonemeRule.l2().length; j++) {
					String letter2Grapheme = phonemeRule.l2()[j];
					l2GraphemeToPhonemeMap.put(letter2Grapheme, phonemeRule);
					graphemeToPhonemeMap.put(letter2Grapheme, phonemeRule);
					graphemeVarIndexMap.put(letter2Grapheme, j);

					// Update maxGraphemeSize to match the longest grapheme found
					maxGraphemeSize = Math.max(letter2Grapheme.length(), maxGraphemeSize);
					/*DEBUG*/System.out.printf("2: Loading %s, maxGraphemeSize = %d\n", letter2Grapheme, maxGraphemeSize);
				}

				/*DEBUG*/System.out.println("Rules object parsed successfully " + phonemeRule.toString());
			}

			// Read each type
			for (int i = 0; i < rulefileSchema.types().length; i++) {
				// Insert main type
				PhonemeType phonemeType = rulefileSchema.types()[i];
				PhonemeTypeReferenceMap.put(phonemeType.name(), phonemeType);

				// Insert subtypes
				for (int j = 0; j < phonemeType.extraTypes().length; j++) {
					PhonemeTypeReferenceMap.put(phonemeType.extraTypes()[j], phonemeType);
				}
			}

			System.out.println("Rules file parsed successfully");
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			System.err.println("There is an issue with the JSON rules file syntax");
		}
	}

	/**
	 * Get the previously selected phoneme from the stack.
	 * @return
	 */
	protected PhonemeRule prevPhoneme() {
		return phonemeStack.nthTop(2);
	}

	/**
	 * Get the currently selected phoneme from the stack.
	 * @return
	 */
	protected PhonemeRule currPhoneme() {
		return phonemeStack.nthTop(1);
	}

	/**
	 * Get the next upcoming phoneme from the stack.
	 * @return
	 */
	protected PhonemeRule nextPhoneme() {
		return phonemeStack.top();
	}

	public String filePath() {
		return this.ruleFile;
	}

	/**
	 * Set a new rule file for the replacer to load.
	 * @param filePath The full file path to the rules file
	 */
	public void setFilePath(String filePath) {
		this.ruleFile = filePath;
		initialiseValues();
		loadJsonRulefile(readExternalJsonFile(filePath));
	}
}
