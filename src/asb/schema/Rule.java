package asb.schema;

public class Rule {

	// Rule attributes
	private SubRule[] subRules;

	public Rule(int subruleLength) {
		subRules = new SubRule[subruleLength];
		for (int i = 0; i < subRules.length; i++) {
			subRules[i] = new SubRule();
		}
	}
	
	public int numOfSubRules() {
		return subRules.length;
	}
	
	public int subRulecVal(int srIndex) {
		return subRules[srIndex].cVal;
	}
	
	public void setSubRulecVal(int srIndex, int cVal) {
		subRules[srIndex].cVal = cVal;
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (SubRule sr: subRules) {
			sb.append('[');
			for (SubSubRule ssr: sr.subsubRules) {
				sb.append('{');
				sb.append("type="+ssr.type);
				sb.append("isNot="+ssr.isNot);
				sb.append("isStrictTypeMatch="+ssr.isStrictTypeMatch);
				sb.append('}');
			}
			sb.append("cVal="+sr.cVal);
			sb.append(']');
		}
		return sb.toString();
	}
	
	public static Rule emptyRule() {
		Rule rule = new Rule(1);
		rule.setSubsubRuleType(0, 0, "anything");
		rule.setSubsubRuleType(0, 1, "anything");
		rule.setSubsubRuleType(0, 2, "anything");
		return rule;
	}
	
	/**
	 * Convert a rule string into a Rule object
	 * 
	 * @param pRule
	 * @return a Rule object
	 */
	public static Rule parseStringToRule(String pRule) {
		//*DEBUG*/System.out.printf("RULE: [%s]\n", pRule);

		if (pRule.length() == 0) {
			return emptyRule();
		}
		
		// rule1 OR rule2 ... OR ruleN
		// Split up each rule into 2 or more 'subrules'.
		// If any 1 of them matches the current pattern, select its corresponding ngraph
		// for insertion to output
		String[] pRulesSub = pRule.split(" \\| ");
		Rule rule = new Rule(pRulesSub.length);
		for (int k = 0; k < pRulesSub.length; k++) {
			//*DEBUG*/System.out.printf("\tRULEd: [%s]\n", pRulesSub[k]);
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
				//*DEBUG*/System.out.printf("\t\tRULEdd: %b [%s], %b [%s], %b [%s]\n",
				//*DEBUG*/notToBeMatched[0], pRulesSubSub[0], notToBeMatched[1], pRulesSubSub[1], notToBeMatched[2], pRulesSubSub[2]);
			}
			// Rule is a counter rule
			else if (pRulesSubSub.length == 1 && pRulesSub[k].contains("c=")) {
				int cVal = Integer.parseInt(pRulesSub[k].substring(2));
				rule.setSubRulecVal(k, cVal);
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
	int cVal;
	
	public SubRule() {
		subsubRules = new SubSubRule[3];
		for (int i = 0; i < subsubRules.length; i++) {
			subsubRules[i] = new SubSubRule();
		}
	}
}
