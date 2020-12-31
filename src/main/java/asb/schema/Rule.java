package asb.schema;

/**
 * The Rule helps to map the graphemes of 1 script to the graphemes of another script.
 * Rules are composed of 1 or more SubRules, which defines the properties of the
 * previous, current and next graphemes that must match the situation.
 * The SubRules themselves are composed of SubSubRules, 1 for each grapheme to be checked.
 * @author perry
 *
 */
public class Rule {

	/** List of SubRules */
	private SubRule[] subRules;

	/** Do all SubRules need to match? */
	private boolean isAndRuleMatch;

	public Rule(int subruleLength) {
		subRules = new SubRule[subruleLength];
		for (int i = 0; i < subRules.length; i++) {
			subRules[i] = new SubRule();
		}
	}

	//////////////////////////////////////////////////
	// GETTER AND SETTER METHODS
	//////////////////////////////////////////////////
	
	public int numOfSubRules() {
		return subRules.length;
	}

	public int subRulecVal(int srIndex) {
		return subRules[srIndex].cVal;
	}

	public void setSubRulecVal(int srIndex, int cVal) {
		subRules[srIndex].cVal = cVal;
	}

	public int subRulePvVal(int srIndex) {
		return subRules[srIndex].pvVal;
	}

	public void setSubRulePvVal(int srIndex, int pvVal) {
		subRules[srIndex].pvVal = pvVal;
	}

	public String subsubRuleType(int srIndex, int ssrIndex) {
		return subRules[srIndex].subsubRules[ssrIndex].type;
	}

	public boolean subsubRuleIsNot(int srIndex, int ssrIndex) {
		return subRules[srIndex].subsubRules[ssrIndex].isNot;
	}

	public boolean subsubRuleIsStrictTypeMatch(int srIndex, int ssrIndex) {
		return subRules[srIndex].subsubRules[ssrIndex].isStrictTypeMatch;
	}

	public void setSubsubRuleType(int srIndex, int ssrIndex, String type) {
		subRules[srIndex].subsubRules[ssrIndex].type = type;
	}

	public void setSubsubRuleIsNot(int srIndex, int ssrIndex, boolean isNot) {
		subRules[srIndex].subsubRules[ssrIndex].isNot = isNot;
	}

	public void setSubsubRuleIsStrictTypeMatch(int srIndex, int ssrIndex, boolean isStrictTypeMatch) {
		subRules[srIndex].subsubRules[ssrIndex].isStrictTypeMatch = isStrictTypeMatch;
	}

	public boolean isAndRuleMatch() {
		return isAndRuleMatch;
	}

	public void setIsAndRuleMatch(boolean isAndRuleMatch) {
		this.isAndRuleMatch = isAndRuleMatch;
	}

	//////////////////////////////////////////////////
	// TOSTRING()
	//////////////////////////////////////////////////

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("isAndRuleMatch=%s\n", this.isAndRuleMatch));

		for (SubRule sr: subRules) {
			sb.append("[\n");
			sb.append(String.format("  cVal=%s\n", sr.cVal));
			sb.append(String.format("  pvVal=%s\n", sr.pvVal));
			for (SubSubRule ssr: sr.subsubRules) {
				sb.append("  {");
				sb.append(String.format(" type=%s ", ssr.type));
				sb.append(String.format("isNot=%s ", ssr.isNot));
				sb.append(String.format("isStrictTypeMatch=%s ", ssr.isStrictTypeMatch));
				sb.append("}\n");
			}
			sb.append("]\n");
		}
		return sb.toString();
	}

	//////////////////////////////////////////////////
	// USEFUL METHODS
	//////////////////////////////////////////////////

	/**
	 * Create a new Rule object that matches anything.
	 * @return New Rule object
	 */
	public static Rule emptyRule() {
		Rule rule = new Rule(1);
		rule.setSubsubRuleType(0, 0, "anything");
		rule.setSubsubRuleType(0, 1, "anything");
		rule.setSubsubRuleType(0, 2, "anything");
		return rule;
	}

	/**
	 * Convert a rule string into a Rule object.
	 * @param pRule The string representing a rule
	 * @return a Rule object with the same contents as the rule String
	 */
	public static Rule parseStringToRule(String pRule) {
		//*OLDDEBUG*/System.out.printf("RULE: [%s]\n", pRule);

		if (pRule.length() == 0) {
			return emptyRule();
		}

		// rule1 OR rule2 ... OR ruleN
		// Split up each rule into 2 or more 'subrules'.
		// If any 1 of them matches the current pattern, select its corresponding grapheme
		// for insertion to output
		String[] pRulesSubOr = pRule.split(" \\| ");
		String[] pRulesSubAnd = pRule.split(" & ");
		boolean isAndRuleMatch = (pRulesSubAnd.length > pRulesSubOr.length);
		String[] pRulesSub = (isAndRuleMatch) ? pRulesSubAnd : pRulesSubOr;
		Rule rule = new Rule(pRulesSub.length);
		rule.setIsAndRuleMatch(isAndRuleMatch);
		for (int k = 0; k < pRulesSub.length; k++) {
			//*OLDDEBUG*/System.out.printf("\tRULEd: [%s]\n", pRulesSub[k]);
			String[] pRulesSubSub = pRulesSub[k].split("_");

			// Rule is a pattern rule
			if (pRulesSubSub.length == 3) {
				boolean[] notToBeMatched = { false, false, false }; // if true, only return as true if type does NOT
																	// match
				boolean[] isStrictTypeMatch = { false, false, false }; // if true, enforce strict type checking. conso1
																		// == conso1, but conso != consonant.

				// Convert the rule syntax into valid types
				for (int l = 0; l < 3; l++) {
					// Check if type is NOT to be matched
					if (pRulesSubSub[l].charAt(0) == '!') {
						notToBeMatched[l] = true;
						pRulesSubSub[l] = pRulesSubSub[l].substring(1);
						rule.setSubsubRuleIsNot(k, l, true);
					}

					// Convert type shorthand to full type names
					// TODO put the types in a separate class file later
					if (pRulesSubSub[l].equals("V"))
						pRulesSubSub[l] = "vowel";
					if (pRulesSubSub[l].equals("C"))
						pRulesSubSub[l] = "consonant";
					if (pRulesSubSub[l].equals("N"))
						pRulesSubSub[l] = "numeral";
					if (pRulesSubSub[l].equals("P"))
						pRulesSubSub[l] = "punctuation";
					if (pRulesSubSub[l].equals("#"))
						pRulesSubSub[l] = "sentenceEdge";
					if (pRulesSubSub[l].equals("."))
						pRulesSubSub[l] = "anything";
					if (pRulesSubSub[l].charAt(0) == '<'
							&& pRulesSubSub[l].charAt(pRulesSubSub[l].length() - 1) == '>') {
						pRulesSubSub[l] = pRulesSubSub[l].substring(1, pRulesSubSub[l].length() - 1);
						isStrictTypeMatch[l] = true;
						rule.setSubsubRuleIsStrictTypeMatch(k, l, true);
					}
					rule.setSubsubRuleType(k, l, pRulesSubSub[l]);
				}
				//*OLDDEBUG*/System.out.printf("\t\tRULEdd: %b [%s], %b [%s], %b [%s]\n",
				//*OLDDEBUG*/notToBeMatched[0], pRulesSubSub[0], notToBeMatched[1], pRulesSubSub[1], notToBeMatched[2], pRulesSubSub[2]);
				rule.setSubRulePvVal(k, -1);
			}
			// Rule is a counter rule
			else if (pRulesSubSub.length == 1 && pRulesSub[k].contains("c=")) {
				int cVal = Integer.parseInt(pRulesSub[k].substring(2));
				rule.setSubRulecVal(k, cVal);
			}
			// Rule is a phoneme variant selection rule
			else if (pRulesSubSub.length == 1 && pRulesSub[k].contains("pv=")) {
				int pvVal = Integer.parseInt(pRulesSub[k].substring(3));
				rule.setSubRulePvVal(k, pvVal);
			}
		}
		return rule;
	}
}

// SubSubRule
class SubSubRule {
	// SubSubRule attributes
	String type;
	boolean isNot;
	boolean isStrictTypeMatch;
}

// SubRule
class SubRule {
	// SubRule attributes
	SubSubRule[] subsubRules;
	int cVal;  // counter value
	int pvVal; // phoneme variant value. -1 = match any value, 0 = 1st value, 1 = 2nd value, etc.

	public SubRule() {
		subsubRules = new SubSubRule[3];
		for (int i = 0; i < subsubRules.length; i++) {
			subsubRules[i] = new SubSubRule();
		}
		
		// Set default values
		this.cVal = 0;
		this.pvVal = -1;
	}
}
