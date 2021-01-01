package asb.script.transcoder.parsing;

import asb.mappings.Mappings;
import asb.schema.PhonemeCounter;
import asb.schema.Rule;

/**
 * RuleParser implementation which deals with counter rules
 * (e.g. 'c=2' - match if 2 occurrences of this type have been recorded).
 * @author perry
 *
 */
public class CounterRuleParser implements RuleParser {

	private final String NAME = "CounterRuleParser";
	
	@Override
	public String name() {
		return NAME;
	}

	@Override
	public boolean matchesCondition(CharToken cToken, Rule pRule, int j, boolean toScript) {
		String currType = (toScript) ? cToken.phonemeRule().l2type() : cToken.phonemeRule().l1type();
		PhonemeCounter pCounter = Mappings.getConsoTypeToCounterMap().get(currType);
		return pRule.subRulecVal(j) >= 1 && pCounter != null;
	}

	@Override
	public boolean isSubruleMatch(CharToken cToken, Rule pRule, int j, boolean toScript) {
		String currType = (toScript) ? cToken.phonemeRule().l2type() : cToken.phonemeRule().l1type();
		PhonemeCounter pCounter = Mappings.getConsoTypeToCounterMap().get(currType);
		
		int cVal = pRule.subRulecVal(j);
		/*DEBUG*/System.out.printf("\t\tRULEd counter? curr counter val = %d\n", pCounter.value());

		// Match if counter value for current phoneme's type equals cVal.
		// Useful for consonant clusters
		return (pCounter.value() >= cVal);
	}

	@Override
	public void postMatch(CharToken cToken, Rule pRule, int j, boolean toScript) {
		String currType = (toScript) ? cToken.phonemeRule().l2type() : cToken.phonemeRule().l1type();
		PhonemeCounter pCounter = Mappings.getConsoTypeToCounterMap().get(currType);
		pCounter.reset(); // reset counter value to 0		
		/*DEBUG*/System.out.println("\t\tCounter was reset to 0");
	}

}
