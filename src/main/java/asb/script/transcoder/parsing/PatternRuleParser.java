package asb.script.transcoder.parsing;

import asb.mappings.Mappings;
import asb.schema.Rule;

/**
 * RuleParser implementation which deals with pattern rules
 * (e.g. 'V_._.' - match if preceding grapheme is a vowel).
 * @author perry
 *
 */
public class PatternRuleParser implements RuleParser {
	
	private final String NAME = "PatternRuleParser";
	
	@Override
	public String name() {
		return NAME;
	}

	@Override
	public boolean matchesCondition(CharToken cToken, Rule pRule, int j, boolean toScript) {
		return (pRule.subRulecVal(j) == 0 && pRule.subRulePvVal(j) < 0);
	}

	@Override
	public boolean isSubruleMatch(CharToken cToken, Rule pRule, int j, boolean toScript) {
		String prevType = (toScript) ? cToken.prev().phonemeRule().l2type() : cToken.prev().phonemeRule().l1type();
		String currType = (toScript) ? cToken.phonemeRule().l2type() : cToken.phonemeRule().l1type();
		String nextType = (toScript) ? cToken.next().phonemeRule().l2type() : cToken.next().phonemeRule().l1type();
		
		// COMPARISON!
		boolean prevIsMatch = (pRule.subsubRuleType(j, 0).equals("anything"))
				? true // always true if it matches 'anything'
				: (pRule.subsubRuleIsNot(j, 0))
					? !typeEquals(pRule.subsubRuleType(j, 0), prevType, pRule.subsubRuleIsStrictTypeMatch(j, 0))
					: typeEquals(pRule.subsubRuleType(j, 0), prevType, pRule.subsubRuleIsStrictTypeMatch(j, 0));
		/*DEBUG*/System.out.printf("\t\tPREVMATCH: [%b]\n", prevIsMatch);

		boolean currIsMatch = (pRule.subsubRuleType(j, 1).equals("anything"))
				? true // always true if it matches 'anything'
				: (pRule.subsubRuleIsNot(j, 1))
					? !typeEquals(pRule.subsubRuleType(j, 1), currType, pRule.subsubRuleIsStrictTypeMatch(j, 1))
					: typeEquals(pRule.subsubRuleType(j, 1), currType, pRule.subsubRuleIsStrictTypeMatch(j, 1));
		/*DEBUG*/System.out.printf("\t\tCURRMATCH: [%b]\n", currIsMatch);

		boolean nextIsMatch = (pRule.subsubRuleType(j, 2).equals("anything"))
				? true // always true if it matches 'anything'
				: (pRule.subsubRuleIsNot(j, 2))
					? !typeEquals(pRule.subsubRuleType(j, 2), nextType, pRule.subsubRuleIsStrictTypeMatch(j, 2))
					: typeEquals(pRule.subsubRuleType(j, 2), nextType, pRule.subsubRuleIsStrictTypeMatch(j, 2));
		/*DEBUG*/System.out.printf("\t\tNEXTMATCH: [%b]\n", nextIsMatch);
		////////System.out.printf("\t\tRule num: %d\n", i);

		// Match if the pattern matches the scenario
		return ((prevIsMatch && nextIsMatch) && currIsMatch);		
	}

	@Override
	public void postMatch(CharToken cToken, Rule pRule, int j, boolean toScript) {
		// TODO Auto-generated method stub
		
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
				: (Mappings.getPhonemeTypeReferenceMap().get(a).name().equals(b) 
						|| Mappings.getPhonemeTypeReferenceMap().get(b).name().equals(a));
		boolean matchesSubType = (a.equals(b));
		/*DEBUG*/System.out.printf("\t\t\ttypeEquals(%s, %s): matchesMainType=%b, matchesSubType=%b\n",
		/*DEBUG*/		a, b, matchesMainType, matchesSubType);
		return matchesMainType || matchesSubType;
	}
}
