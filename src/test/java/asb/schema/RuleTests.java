package asb.schema;

import static org.junit.Assert.*;

import org.junit.Test;

public class RuleTests {

	@Test
	public void test_parseStringToRule1() {
		Rule exampleRule = new Rule(1);

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
		
		String exampleRuleString = "C_._V";
		Rule parsedRule = Rule.parseStringToRule(exampleRuleString);
		
		assertEquals(exampleRule.toString(), parsedRule.toString());
	}

	@Test
	public void test_parseStringToRule2() {
		Rule exampleRule = new Rule(1);

		exampleRule.setSubsubRuleType(0, 0, "consonant");
		exampleRule.setSubsubRuleType(0, 1, "vowel");
		exampleRule.setSubsubRuleType(0, 2, "narrowConso");
		exampleRule.setSubsubRuleIsNot(0, 0, true);
		exampleRule.setSubsubRuleIsNot(0, 1, false);
		exampleRule.setSubsubRuleIsNot(0, 2, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(0, 0, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(0, 1, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(0, 2, true);
		exampleRule.setSubRulecVal(0, 999);
		
		String exampleRuleString = "!C_._<narrowConso>";
		Rule parsedRule = Rule.parseStringToRule(exampleRuleString);
		
		assertEquals(exampleRule.toString(), parsedRule.toString());
	}
}
