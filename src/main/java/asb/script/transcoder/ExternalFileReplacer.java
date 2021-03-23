package asb.script.transcoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import asb.schema.Rule;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import asb.io.FileIO;
import asb.mappings.Mappings;
import asb.schema.PhonemeCounter;
import asb.schema.PhonemeRule;
import asb.schema.PhonemeType;
import asb.schema.RuleSchema;
import asb.script.transcoder.parsing.CharToken;
import asb.script.transcoder.parsing.RuleParser;
import asb.script.transcoder.parsing.RuleParserFactory;
import asb.script.transcoder.parsing.Tokeniser;

/**
 * Replace each occurrence of a Script 1 grapheme with the corresponding grapheme for Script 2
 * (and vice versa) using context from the surrounding graphemes.
 * @author perry
 *
 */
public class ExternalFileReplacer {

	/**
	 * These HashMaps map String keys representing the original char with
	 * PhonemeRule class values representing what they will be replaced with
	 */
	protected Map<String, PhonemeRule> l1GraphemeToPhonemeMap; // Script 1 grapheme -> Script 2 PhonemeRule
	protected Map<String, PhonemeRule> l2GraphemeToPhonemeMap; // Script 2 grapheme -> Script 1 PhonemeRule
	protected Map<String, Integer> graphemeVarIndexMap; // Grapheme -> its index in the list of phoneme variants

	/** The maximum grapheme size, which determines the number of chars to scan ahead */
	protected int maxGraphemeSize;

	/** Directory of the rule file */
	protected String rulefileDir;

	/**
	 * Initialise a new ExternalFileReplacer with the substitution rules from the given rulefile.
	 * @param filePath The directory of the rulefile
	 */
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
		this.graphemeVarIndexMap = new HashMap<String, Integer>();
		this.maxGraphemeSize = 0;
	}

	/**
	 * Translate a text written in Script 1 into Script 2
	 *
	 * @param input A text written in Script 1
	 * @return The transliterated text in Script 2
	 */
	public String translateToScript(String input) {
		return translateFromToScript(input, true);
	}

	/**
	 * Translate a text written in Script 2 into Script 1
	 *
	 * @param input A text written in Script 2
	 * @return The transliterated text in Script 1
	 */
	public String translateFromScript(String input) {
		return translateFromToScript(input, false);
	}
	
	/**
	 * Reset all phoneme counters.
	 */
	private void resetCounters() {
		Set<String> counterKeySet = Mappings.getConsoTypeToCounterMap().keySet();
		for (String key : counterKeySet) {
			Mappings.getConsoTypeToCounterMap().get(key).reset();
			/*DEBUG*/System.out.printf("Counter for '%s' has been resetted\n", key);
		}
	}
	
	/**
	 * Reset all phoneme counters, given the current phoneme type.
	 * @param cToken Current token
	 * @param toScript Translate the input to script, or back?
	 */
	private void resetCounters(CharToken cToken, boolean toScript) {
		String currType = (toScript) ? Mappings.getPhonemeTypeReferenceMap().get(cToken.phonemeRule().l2type()).name()
				: Mappings.getPhonemeTypeReferenceMap().get(cToken.phonemeRule().l1type()).name();
		
		// Reset counter values if they have reached the maximum value
		Set<String> counterKeySet = Mappings.getConsoTypeToCounterMap().keySet();
		for (String key : counterKeySet) {
			boolean counterIsMax = !(key.equals(currType) && !Mappings.getConsoTypeToCounterMap().get(key).valueIsMax());
			if (counterIsMax) {
				Mappings.getConsoTypeToCounterMap().get(key).reset(); // reset counter value to 0
				/*DEBUG*/System.out.printf("Counter for '%s' has been resetted\n", key);
			}
		}
	}
	
	/**
	 * Convert an input string into an array of CharTokens.
	 * @param input Self-descriptive
	 * @param toScript Translate the input to script, or back?
	 * @return List of CharTokens
	 */
	private List<CharToken> convertToTokens(String input, boolean toScript) {
		List<CharToken> tokenOutput = new ArrayList<>();
		Map<String, PhonemeRule> mapping = (toScript) 
				? this.l1GraphemeToPhonemeMap 
				: this.l2GraphemeToPhonemeMap;
		Tokeniser tokeniser = new Tokeniser(input, mapping, graphemeVarIndexMap);
		CharToken token;

		while ((token = tokeniser.readNextToken()) != null) {
			CharToken prev = tokeniser.prevToken();
			tokenOutput.add(prev);
		}
		
		return tokenOutput;
	}
	
	/**
	 * Look for a rule that matches the current pattern
	 *
	 * @param cToken   The current token
	 * @param pRules   List of rules to check for matches
	 * @param toScript Translate the input to script, or back?
	 * @param pCounter Phoneme counter for this type
	 * @return Index of matching rule. -1 if no match was found
	 */
	private int selectRule(CharToken cToken, Rule[] pRules, boolean toScript, PhonemeCounter pCounter) {
		RuleParserFactory ruleParserFactory = RuleParserFactory.getInstance();
		
		// Parse each rule until one matching the current pattern is found
		int letterIndex = -1;
		for (int i = 0; i < pRules.length; i++) {
			// True = AND matching - all subrules MUST match.
			// False = OR matching - at least 1 subrule shall match.
			boolean isAndRuleMatch = pRules[i].isAndRuleMatch();
			// True if all subrules in this rule are found to be a match
			boolean subRulesDoMatch = true;
			/*DEBUG*/System.out.printf("\tISANDRULEMATCH: [%b]\n", isAndRuleMatch);
			/*DEBUG*/System.out.printf("\tSUBRULESDOMATCH: [%b]\n", subRulesDoMatch);

			// Parse each subrule: should be either AND (all must match) or OR (at least 1 must match).
			for (int j = 0; j < pRules[i].numOfSubRules(); j++) {
				/*DEBUG*/System.out.printf("\tGoing thru subrule num %d\n", j);

				// Check all rule types
				for (RuleParser ruleParser : ruleParserFactory.getRuleParsers()) {
					if (ruleParser.matchesCondition(cToken, pRules[i], j, toScript)) {
						// Match if the pattern matches the scenario
						boolean isMatch = ruleParser.isSubruleMatch(cToken, pRules[i], j, toScript);
						
						subRulesDoMatch &= isMatch;
						/*DEBUG*/System.out.printf("\t%s's ISMATCH: [%b]\n", ruleParser.name(), isMatch);

						// If a subrule matches the current pattern, select the index of its corresponding grapheme
						// for insertion to output
						if (!isAndRuleMatch && isMatch) {
							ruleParser.postMatch(cToken, pRules[i], j, toScript);
							letterIndex = i;
							/*DEBUG*/System.out.printf("\tChosen %s rule num: %d\n", ruleParser.name(), i);
							return letterIndex;
						}
					}					
				}
			}
			
			// If all subrules match the current pattern, select the index of its corresponding grapheme
			// for insertion to output
			if (isAndRuleMatch && subRulesDoMatch) {
				letterIndex = i;
				/*DEBUG*/System.out.printf("\tAll subrules match! Rule num: %d\n", i);
				return letterIndex;
			} else {
				/*DEBUG*/System.out.printf("\tAll subrules do not match. Selecting default letter.\n");
			}
		}
		return letterIndex;
	}
	
	/**
	 * Increment the type counter for this token.
	 * @param cToken Current token
	 * @param toScript Translate the input to script, or back?
	 */
	private void incrementCounter(CharToken cToken, boolean toScript) {
		String currType = (toScript) ? Mappings.getPhonemeTypeReferenceMap().get(cToken.phonemeRule().l2type()).name()
				: Mappings.getPhonemeTypeReferenceMap().get(cToken.phonemeRule().l1type()).name();
		PhonemeCounter pCounter = Mappings.getConsoTypeToCounterMap().get(currType);
		
		if (pCounter != null) {
			/*DEBUG*/System.out.printf("Counter for '%s' value: %d\n", currType, pCounter.value());
			Rule[] cRules = pCounter.incrRuleParsed();
			int matchingRuleIndex = selectRule(cToken, cRules, toScript, null);
			if (matchingRuleIndex >= 0) {
				//System.out.println(cRules[matchingRuleIndex]);
				pCounter.increment();
				/*DEBUG*/System.out.printf("\tRule for counter increment is a match (counter=%d). index=%d\n", pCounter.value(), matchingRuleIndex);
			} else {
				/*DEBUG*/System.out.println("\tRule for counter increment is NOT a match");
			}
		} else {
			/*DEBUG*/System.out.printf("Counter for '%s' does not exist\n", currType);
		}
	}
	
	/**
	 * Select the grapheme to append - find the right grapheme variant for the current pattern.
	 * @param cToken Current token
	 * @param toScript Translate the input to script, or back?
	 * @return The index of the chosen grapheme variant in the list of graphemes
	 */
	private int selectGraphemeIndex(CharToken cToken, boolean toScript) {
		/*DEBUG*/System.out.println("SELECT GRAPHEME TO APPEND...");
		String currType = (toScript) ? Mappings.getPhonemeTypeReferenceMap().get(cToken.phonemeRule().l2type()).name()
				: Mappings.getPhonemeTypeReferenceMap().get(cToken.phonemeRule().l1type()).name();
		PhonemeCounter pCounter = Mappings.getConsoTypeToCounterMap().get(currType);
		Rule[] pRules = (toScript) ? cToken.phonemeRule().l2ruleParsed() : cToken.phonemeRule().l1ruleParsed();
		int letterIndex = selectRule(cToken, pRules, toScript, pCounter);

		if (letterIndex < 0) {
			// Set default grapheme variant (the last one) if none were found
			letterIndex = (toScript) ? cToken.phonemeRule().l2().length - 1 : cToken.phonemeRule().l1().length - 1;
		}
		
		return letterIndex;
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
		StringBuilder output = new StringBuilder();

		// The current grapheme to analyse
		String currGrapheme = "";
		
		// Reset the counters
		resetCounters();

		////////////////////////////////////////////////////////////
		// CONVERT THE INPUT STRING INTO A LIST OF TOKENS
		////////////////////////////////////////////////////////////
		List<CharToken> tokenOutput = convertToTokens(input, toScript);

		////////////////////////////////////////////////////////////
		// GO THROUGH TOKENS AND INSERT REPLACEMENT IN OUTPUT
		////////////////////////////////////////////////////////////

		for (CharToken cToken : tokenOutput) {
			/*DEBUG*/System.out.println(cToken);
			
			// Set dummy sentence edge token for the last token
			if (cToken.next() == null) {
				PhonemeRule sentenceEdgePhoneme = PhonemeRule.sentenceEdge();
				CharToken sentenceEdgeToken = new CharToken(sentenceEdgePhoneme, 0, cToken, null);
				cToken.setNext(sentenceEdgeToken);
			}
			/*DEBUG*/System.out.println("INSERT REPLACEMENT IN OUTPUT...");

			// If no replacement phoneme could be found, the current phoneme is a non-defined punctuation mark
			if (cToken.phonemeRule() == null) {
				/*DEBUG*/System.out.println("GRAPHEME: no repl found");
				output.append(currGrapheme);
				continue;
			}
			/*DEBUG*/System.out.println("GRAPHEME: repl found - " + cToken.phonemeRule().l2()[0]);
			
			// Increment the counter for the current phoneme's type
			/*DEBUG*/System.out.println("INCREMENT PHONEME TYPE COUNTER...");
			incrementCounter(cToken, toScript);

			// Select the grapheme to append - find the right grapheme variant for the current pattern
			int letterIndex = selectGraphemeIndex(cToken, toScript);

			// Reset counter values if they have reached the maximum value
			resetCounters(cToken, toScript);

			// Append the replacement grapheme to output
			/*DEBUG*/System.out.printf("OUTPUT: [%s]\n", output.toString());
			String repl = (toScript) ? cToken.phonemeRule().l2()[letterIndex] : cToken.phonemeRule().l1()[letterIndex];
			output.append(repl);
		}

		return output.toString();
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

				Mappings.getConsoTypeToCounterMap().put(phonemeCounter.type(), phonemeCounter);
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
					graphemeVarIndexMap.put(letter1Grapheme, j);

					// Update maxGraphemeSize to match the longest grapheme found
					maxGraphemeSize = Math.max(letter1Grapheme.length(), maxGraphemeSize);
					/*DEBUG*/System.out.printf("1: Loading %s, maxGraphemeSize = %d\n", letter1Grapheme, maxGraphemeSize);
				}

				// Read letter 2 graphemes
				for (int j = 0; j < phonemeRule.l2().length; j++) {
					String letter2Grapheme = phonemeRule.l2()[j];
					l2GraphemeToPhonemeMap.put(letter2Grapheme, phonemeRule);
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
				Mappings.getPhonemeTypeReferenceMap().put(phonemeType.name(), phonemeType);

				// Insert subtypes
				for (int j = 0; j < phonemeType.extraTypes().length; j++) {
					Mappings.getPhonemeTypeReferenceMap().put(phonemeType.extraTypes()[j], phonemeType);
				}
			}

			System.out.println("Rules file parsed successfully");
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			System.err.println("There is an issue with the JSON rules file syntax");
		}
	}

	/**
	 * Set a new rule file for the replacer to load.
	 * @param filePath The full file path to the rules file
	 */
	public void setFilePath(String filePath) {
		this.rulefileDir = filePath;
		initialiseValues();
		loadJsonRulefile(readExternalJsonFile(filePath));
	}
}
