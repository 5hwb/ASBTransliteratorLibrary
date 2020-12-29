package asb.script.transcoder.parsing;

import asb.schema.Rule;

public class PhonemeVariantRuleParser implements RuleParser {

//	@Override
//	public boolean matchesCondition() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public int getMatchingRule() {
//		// TODO Auto-generated method stub
//		return 777;
//	}

	@Override
	public boolean matchesCondition(CharToken cToken, Rule pRule, int j, boolean toScript) {
		// Get the index of the selected grapheme in phoneme's variant list
		Integer pVariantIndex = cToken.graphemeVarIndex();

		return pRule.subRulePvVal(j) >= 0 && pVariantIndex != null;
	}

	@Override
	public boolean isMatch(CharToken cToken, Rule pRule, int j, boolean toScript) {

		// Get the index of the selected grapheme in phoneme's variant list
		Integer pVariantIndex = cToken.graphemeVarIndex();

		int pvVal = pRule.subRulePvVal(j);
		/*DEBUG*/System.out.printf("\t\tRULEd phovarsel? curr variant val = %d\n", pVariantIndex);

		// Match if counter value for current phoneme's type equals cVal.
		// Useful for scripts that have uppercase and lowercase forms
		return (pVariantIndex == pvVal);
	}

}
