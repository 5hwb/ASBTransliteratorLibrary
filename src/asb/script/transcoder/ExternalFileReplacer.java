package asb.script.transcoder;

import java.io.IOException;
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

/**
 * An ExternalFileReplacer replaces each Latin script character in EBEO with the
 * corresponding letter for an adapted script. EXPERIMENTAL REPLACEMENT FOR THE
 * ORIGINAL REPLACER DESIGN
 *
 * @author perry
 *
 */
public class ExternalFileReplacer {

	/**
	 * These HashMaps map String keys representing the original char with
	 * PhonemeRule class values representing what they will be replaced with
	 */
	protected Map<String, PhonemeRule> rulesToScript; // Latin ngraph -> Script PhonemeRule
	protected Map<String, PhonemeRule> rulesFromScript; // Script ngraph -> Latin PhonemeRule
	protected Map<String, PhonemeRule> rulesReference; // Ngraph -> corresponding PhonemeRule

	/** Stores the output string */
	protected StringBuilder sb;

	/** The current ngraph to analyse */
	// protected String currNGraph;

	/** Stack of the last 3 phonemes */
	protected FixedStack<PhonemeRule> phonemes;

	/** The PhonemeRule which will replace the currently selected ngraph */
	protected PhonemeRule replPhoneme;

	/** A placeholder PhonemeRule for non-replaced characters */
	protected PhonemeRule defPhoneme;

	/** The maximum number of chars to scan ahead */
	protected int nMax;

	/** Indicate if the replacer's script supports case */
	protected boolean hasCase;

	/** Consonant counters */
	protected Map<String, PhonemeCounter> counters;

	/** Reference hashmap for phoneme types */
	protected Map<String, PhonemeType> typeReference;

	protected String ruleFile;

	public ExternalFileReplacer(String filePath) {
		initialiseValues();
		loadJsonReplFile(filePath);
	}

	/**
	 * Initialise the ExternalFileReplacer values
	 */
	private void initialiseValues() {
		this.rulesToScript = new HashMap<String, PhonemeRule>();
		this.rulesFromScript = new HashMap<String, PhonemeRule>();
		this.rulesReference = new HashMap<String, PhonemeRule>();
		this.typeReference = new HashMap<String, PhonemeType>();
		this.phonemes = new FixedStack<PhonemeRule>(3);
		this.counters = new HashMap<String, PhonemeCounter>();
		this.nMax = 0;
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
		 * NOTE: An 'ngraph' is a string of up to n characters representing a single
		 * phoneme. Digraphs are ngraphs with 2 characters
		 */
		sb = new StringBuilder();

		/* The current ngraph to analyse */
		String currNGraph = "";

		// Insert the 1st phoneme with the type SENTENCEEND, since
		// the beginning marks the start of a new sentence
		PhonemeRule initPhoneme = new PhonemeRule(new String[] { "" }, "sentenceEdge", new String[] {""},
				new String[] { "" }, "sentenceEdge", new String[] {""});
		phonemes.push(initPhoneme);
		phonemes.push(initPhoneme);
		phonemes.push(initPhoneme); // enter it 3 times so the phoneme stack is never empty

		Set<String> counterKeySet = counters.keySet();

		/*DEBUG*/System.out.println("Initialised!");

		// Reset the counters
		for (String key : counterKeySet) {
			counters.get(key).reset();
		}

		// Need to accomodate the maximum possible ngraph length in the for loop
		for (int i = 0; i < input.length() + nMax; i++) {
			/*DEBUG*/System.out.printf("----------i=%d----------\n", i);

			//////////////////////////////////////////
			// LOOK UP NGRAPH AND INSERT INTO STACK //
			//////////////////////////////////////////
			/*DEBUG*/System.out.println("LOOK UP NGRAPH AND INSERT INTO STACK...");
			// Looks ahead at the next chars, detecting ngraphs of decreasing size so that
			// they get detected first
			if (i < input.length()) {
				for (int c = this.nMax; c >= 1; c--) {
					/*DEBUG*/System.out.printf("c=%d\n", c);
					// Limit the lookup so it does not go beyond the end of the input
					int limit = (i + c >= input.length()) ? input.length() : i + c;
					currNGraph = input.substring(i, limit);

					int ngraphSize = 0;

					// Look up the reference HashMap with the current ngraph
					// to see if there is an entry.
					// If there is a match, add it to the phoneme stack
					PhonemeRule curr = (toScript) ? this.rulesToScript.get(currNGraph)
							: this.rulesFromScript.get(currNGraph);
					if (curr != null) {
						/*DEBUG*/System.out.printf("\tPHONEME FOUND, INSERTING CORRS RULE TO STACK\n");
						phonemes.push(curr);
						ngraphSize = c;
					}
					// Insert the original (punctuation) character if it was not covered by a rule
					else if (c == 1) {
						// BUT only if the ngraph's 1 char long!
						/*DEBUG*/System.out.printf("\tNO MATCH FOUND. INSERTING ORIGINAL PUNCTUATION INTO THE STACK\n");
						defPhoneme = new PhonemeRule(new String[] { currNGraph }, "punctuation", new String[] {""},
								new String[] { currNGraph }, "punctuation", new String[] {""});
						phonemes.push(defPhoneme);
						ngraphSize = 1;
					}

					// Skip to the next iteration of the loop
					if (ngraphSize == 0)
						continue;

					// Adjust current index to avoid parsing the components of the ngraph
					i += (ngraphSize - 1); // subtract 1 since the next iteration of the for loop will add 1 again
					break;
				}
			}
			// The last 3 ngraphs are in the stack, but the last ngraph hasn't
			// been processed yet.
			// Insert a dummy PhonemeRule to push the last ngraph into position
			else {
				/*DEBUG*/System.out.println("\tDummy phonemerule inserted");
				phonemes.push(initPhoneme);
			}

			//////////////////////////////////////////
			// INSERT REPLACEMENT IN OUTPUT //
			//////////////////////////////////////////
			/*DEBUG*/System.out.println("INSERT REPLACEMENT IN OUTPUT...");
			// Get the PhonemeRule for the currently selected ngraph
			replPhoneme = currPhoneme();

			// If no replacement phoneme could be found, the current phoneme is a
			// non-defined punctuation mark
			if (replPhoneme == null) {
				/*DEBUG*/System.out.println("NGRAPH: no repl found");
				sb.append(currNGraph);
				continue;
			}
			/*DEBUG*/System.out.println(phonemes);
			/*DEBUG*/System.out.println("NGRAPH: repl found - " + replPhoneme.l2()[0]);

			// Increment counter for the current phoneme's type
			String currType = (toScript) ? typeReference.get(replPhoneme.l2type()).name()
					: typeReference.get(replPhoneme.l1type()).name();
			PhonemeCounter pCounter = counters.get(currType);
			if (pCounter != null) {
				/*DEBUG*/System.out.printf("Counter for '%s' value: %d\n", currType, pCounter.value());
				Rule[] cRules = pCounter.incrRuleParsed();
				int matchingRuleIndex = selectRule(cRules, toScript, null);
				if (matchingRuleIndex >= 0) {
					/*DEBUG*/System.out.printf("\tRule for counter increment is a match. index=%d, matchingRule=%s\n", matchingRuleIndex, cRules[matchingRuleIndex]);
					pCounter.increment();
				} else {
					/*DEBUG*/System.out.println("\tRule for counter increment is NOT a match");
				}
			} else {
				/*DEBUG*/System.out.printf("Counter for '%s' does not exist\n", currType);
			}

			Rule[] pRules = (toScript) ? replPhoneme.l2ruleParsed() : replPhoneme.l1ruleParsed();
			int letterIndex = selectRule(pRules, toScript, pCounter);
			if (letterIndex < 0) {
				// default letter is the last one
				letterIndex = (toScript) ? replPhoneme.l2().length - 1 : replPhoneme.l1().length - 1;
			}

			// Reset counter values
			for (String key : counterKeySet) {
				if (key.equals(currType) && !counters.get(key).valueIsMax()) {
				} else {
					counters.get(key).reset(); // reset counter value to 0
				}
			}

			// Append the replacement ngraph
			/*DEBUG*/System.out.printf("OUTPUT: [%s]\n", sb.toString());
			String repl = (toScript) ? replPhoneme.l2()[letterIndex] : replPhoneme.l1()[letterIndex];
			sb.append(repl);
		}

		return sb.toString();
	}

	/**
	 * Look for a rule that matches the current situation
	 *
	 * @param pRules   List of rules to check for matches
	 * @param toScript Translate the input to script, or back?
	 * @param pCounter
	 * @return Index of matching rule. -1 if no match was found
	 */
	private int selectRule(Rule[] pRules, boolean toScript, PhonemeCounter pCounter) {
		String prevType = (toScript) ? prevPhoneme().l2type() : prevPhoneme().l1type();
		String currType = (toScript) ? replPhoneme.l2type() : replPhoneme.l1type();
		String nextType = (toScript) ? nextPhoneme().l2type() : nextPhoneme().l1type();

		// Get the rules, go through each one until one matching the current pattern is found
		int letterIndex = -1;
		boolean ruleNotFound = true;
		for (int j = 0; j < pRules.length && ruleNotFound; j++) {

			// If any 1 of them matches the current pattern, select its corresponding ngraph for insertion to output
			for (int k = 0; k < pRules[j].numOfSubRules(); k++) {

				// Rule is a pattern rule
				if (pRules[j].subRulecVal(k) == 0) {

					// COMPARISON!
					boolean prevIsMatch = (pRules[j].subsubRuleType(k, 0).equals("anything"))
							? true // always true if it matches 'anything'
							: (pRules[j].subsubRuleIsNot(k, 0))
								? !typeEquals(pRules[j].subsubRuleType(k, 0), prevType, pRules[j].subsubRuleIsStrictTypeMatch(k, 0))
								: typeEquals(pRules[j].subsubRuleType(k, 0), prevType, pRules[j].subsubRuleIsStrictTypeMatch(k, 0));

					boolean currIsMatch = (pRules[j].subsubRuleType(k, 1).equals("anything"))
							? true // always true if it matches 'anything'
							: (pRules[j].subsubRuleIsNot(k, 1))
								? !typeEquals(pRules[j].subsubRuleType(k, 1), currType, pRules[j].subsubRuleIsStrictTypeMatch(k, 1))
								: typeEquals(pRules[j].subsubRuleType(k, 1), currType, pRules[j].subsubRuleIsStrictTypeMatch(k, 1));

					boolean nextIsMatch = (pRules[j].subsubRuleType(k, 2).equals("anything"))
							? true // always true if it matches 'anything'
							: (pRules[j].subsubRuleIsNot(k, 2))
								? !typeEquals(pRules[j].subsubRuleType(k, 2), nextType, pRules[j].subsubRuleIsStrictTypeMatch(k, 2))
								: typeEquals(pRules[j].subsubRuleType(k, 2), nextType, pRules[j].subsubRuleIsStrictTypeMatch(k, 2));
					/*DEBUG*/System.out.printf("\t\tPREVMATCH: [%b]\n", prevIsMatch);
					/*DEBUG*/System.out.printf("\t\tCURRMATCH: [%b]\n", currIsMatch);
					/*DEBUG*/System.out.printf("\t\tNEXTMATCH: [%b]\n", nextIsMatch);
					/*DEBUG*/System.out.printf("\t\tRule num: %d\n", j);

					// Match if the pattern matches the scenario
					if ((prevIsMatch && nextIsMatch) && currIsMatch) {
						letterIndex = j;
						ruleNotFound = false;
						/*DEBUG*/System.out.printf("\t\tChosen PATTERN rule num: %d\n", j);
						return letterIndex;
					}
				}
				// Rule is a counter rule
				else if (pRules[j].subRulecVal(k) >= 1 && pCounter != null) {
					int cVal = pRules[j].subRulecVal(k);
					/*DEBUG*/System.out.printf("\t\tRULEd counter? curr counter val = %d\n", pCounter.value());

					// Match if counter value for current phoneme's type equals cVal.
					// Useful for consonant clusters
					if (pCounter.value() >= cVal) {
						pCounter.reset(); // reset counter value to 0
						letterIndex = j;
						ruleNotFound = false;
						/*DEBUG*/System.out.printf("\t\tChosen matching COUNTER rule num: %d\n", j);
						return letterIndex;
					}
				}
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
				: (typeReference.get(a).name().equals(b) || typeReference.get(b).name().equals(a));
		boolean matchesSubType = (a.equals(b));
		/*DEBUG*/System.out.printf("\t\t\ttypeEquals(%s, %s): matchesMainType=%b, matchesSubType=%b\n",
		/*DEBUG*/		a, b, matchesMainType, matchesSubType);
		return matchesMainType || matchesSubType;
	}

	/**
	 * Load the JSON replacer file and load the phoneme rules into the rules
	 * reference hashmaps
	 */
	public void loadJsonReplFile(String filePath) {
		try {
			String jsonRulefile = FileIO.readFile(filePath);

			Gson jrf = new Gson();

			try {
				RuleSchema tjfRuleSchema = jrf.fromJson(jsonRulefile, RuleSchema.class);

				// Get the counters
				for (int i = 0; i < tjfRuleSchema.counters().length; i++) {
					PhonemeCounter phonemeCounter = tjfRuleSchema.counters()[i];

					// Parse phoneme counter rule strings into Rule objects
					Rule[] rule = new Rule[phonemeCounter.incrRule().length];
					for (int j = 0; j < phonemeCounter.incrRule().length; j++) {
						rule[j] = Rule.parseStringToRule(phonemeCounter.incrRule()[j]);
						phonemeCounter.setIncrRuleParsed(rule);
					}

					counters.put(phonemeCounter.type(), phonemeCounter);
				}

				// Read each phoneme
				for (int i = 0; i < tjfRuleSchema.rules().length; i++) {
					PhonemeRule phonemeRule = tjfRuleSchema.rules()[i];

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

					// Read letter 1 ngraphs
					for (int j = 0; j < phonemeRule.l1().length; j++) {
						String letter1Ngraph = phonemeRule.l1()[j];
						rulesToScript.put(letter1Ngraph, phonemeRule);
						rulesReference.put(letter1Ngraph, phonemeRule);

						// Update nMax to match the longest ngraph found
						nMax = Math.max(letter1Ngraph.length(), nMax);
						/*DEBUG*/System.out.printf("1: Loading %s, nMax = %d\n", letter1Ngraph, nMax);
					}

					// Read letter 2 ngraphs
					for (int j = 0; j < phonemeRule.l2().length; j++) {
						String letter2Ngraph = phonemeRule.l2()[j];
						rulesFromScript.put(letter2Ngraph, phonemeRule);
						rulesReference.put(letter2Ngraph, phonemeRule);

						// Update nMax to match the longest ngraph found
						nMax = Math.max(letter2Ngraph.length(), nMax);
						/*DEBUG*/System.out.printf("2: Loading %s, nMax = %d\n", letter2Ngraph, nMax);
					}

					/*DEBUG*/System.out.println("Rules object parsed successfully " + phonemeRule.toString());
				}

				// Read each type
				for (int i = 0; i < tjfRuleSchema.types().length; i++) {
					// Insert main type
					PhonemeType phonemeType = tjfRuleSchema.types()[i];
					typeReference.put(phonemeType.name(), phonemeType);

					// Insert subtypes
					for (int j = 0; j < phonemeType.extraTypes().length; j++) {
						typeReference.put(phonemeType.extraTypes()[j], phonemeType);
					}
				}

				System.out.println("Rules file parsed successfully");
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				System.err.println("There is an issue with the JSON rules file syntax");
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("There is an issue with loading the JSON rules file");
		}
	}

	/**
	 * Get the previously selected phoneme from the stack.
	 * @return
	 */
	protected PhonemeRule prevPhoneme() {
		return phonemes.nthTop(2);
	}

	/**
	 * Get the currently selected phoneme from the stack.
	 * @return
	 */
	protected PhonemeRule currPhoneme() {
		return phonemes.nthTop(1);
	}

	/**
	 * Get the next upcoming phoneme from the stack.
	 * @return
	 */
	protected PhonemeRule nextPhoneme() {
		return phonemes.top();
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
		loadJsonReplFile(filePath);
	}
}
