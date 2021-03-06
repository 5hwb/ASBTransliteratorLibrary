package asb.script.transcoder.parsing;

import asb.schema.Rule;

/**
 * RuleParser implementation which deals with phoneme variant rules
 * (e.g. 'pv=1' - match if the current grapheme index is 1).
 * @author perry
 *
 */
public class PhonemeVariantRuleParser implements RuleParser {

	private final String NAME = "PhonemeVariantRuleParser";
	
	@Override
	public String name() {
		return NAME;
	}

	@Override
	public boolean matchesCondition(CharToken cToken, Rule pRule, int j, boolean toScript) {
		// Get the index of the selected grapheme in phoneme's variant list
		Integer pVariantIndex = cToken.graphemeVarIndex();

		return pRule.subRulePvVal(j) >= 0 && pVariantIndex != null;
	}

	@Override
	public boolean isSubruleMatch(CharToken cToken, Rule pRule, int j, boolean toScript) {

		// Get the index of the selected grapheme in phoneme's variant list
		Integer pVariantIndex = cToken.graphemeVarIndex();

		int pvVal = pRule.subRulePvVal(j);
		/*DEBUG*/System.out.printf("\t\tRULEd phovarsel? curr variant val = %d\n", pVariantIndex);

		// Match if counter value for current phoneme's type equals cVal.
		// Useful for scripts that have uppercase and lowercase forms
		return (pVariantIndex == pvVal);
	}
	
	@Override
	public void postMatch(CharToken cToken, Rule pRule, int j, boolean toScript) {
		// TODO Auto-generated method stub
		
	}
}
