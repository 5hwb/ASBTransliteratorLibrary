package asb.schema;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MultipleRuleTests {
	
	@Test
	public void test_parseStringToRule_twoPatternRules() {
	
		// Create rule that represents the rule string "C_._V | #_._."
		Rule exampleRule = new Rule(2);
		exampleRule.setSubsubRuleType(0, 0, "consonant");
		exampleRule.setSubsubRuleType(0, 1, "anything");
		exampleRule.setSubsubRuleType(0, 2, "vowel");
		exampleRule.setSubsubRuleIsNot(0, 0, false);
		exampleRule.setSubsubRuleIsNot(0, 1, false);
		exampleRule.setSubsubRuleIsNot(0, 2, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(0, 0, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(0, 1, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(0, 2, false);
		exampleRule.setSubRulecVal(0, 0);

		exampleRule.setSubsubRuleType(1, 0, "sentenceEdge");
		exampleRule.setSubsubRuleType(1, 1, "anything");
		exampleRule.setSubsubRuleType(1, 2, "anything");
		exampleRule.setSubsubRuleIsNot(1, 0, false);
		exampleRule.setSubsubRuleIsNot(1, 1, false);
		exampleRule.setSubsubRuleIsNot(1, 2, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(1, 0, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(1, 1, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(1, 2, false);
		exampleRule.setSubRulecVal(1, 0);

		// Check if parsing the given rule string matches the example Rule object,
		// except that the last match MUST be a narrowConso type
		
		String exampleRuleString = "C_._V | #_._.";
		Rule parsedRule = Rule.parseStringToRule(exampleRuleString);
		
		assertEquals(exampleRule.toString(), parsedRule.toString());
	}
}
