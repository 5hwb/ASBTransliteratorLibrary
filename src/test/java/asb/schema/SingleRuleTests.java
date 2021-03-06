package asb.schema;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SingleRuleTests {
	
	Rule exampleRule;
	
	@Before
	public void init() {
		// Create rule that represents the rule string "C_._V"
		exampleRule = new Rule(1);
		exampleRule.setSubsubRuleType(0, 0, "consonant");
		exampleRule.setSubsubRuleType(0, 1, "anything");
		exampleRule.setSubsubRuleType(0, 2, "vowel");
		exampleRule.setSubsubRuleIsNot(0, 0, false);
		exampleRule.setSubsubRuleIsNot(0, 1, false);
		exampleRule.setSubsubRuleIsNot(0, 2, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(0, 0, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(0, 1, false);
		exampleRule.setSubsubRuleIsStrictTypeMatch(0, 2, false);
	}

	@Test
	public void test_parseStringToEmptyRule() {
		// Create empty rule
		exampleRule = Rule.emptyRule();
		
		String exampleRuleString = "";
		Rule parsedRule = Rule.parseStringToRule(exampleRuleString);		
		assertEquals(exampleRule.toString(), parsedRule.toString());
	}

	@Test
	public void test_parseStringToRule() {
		// Check if parsing the given rule string matches the example Rule object
		String exampleRuleString = "C_._V";
		Rule parsedRule = Rule.parseStringToRule(exampleRuleString);
		assertEquals(exampleRule.toString(), parsedRule.toString());
	}

	@Test
	public void test_parseStringToRule_isNot() {
		// Check if parsing the given rule string matches the example Rule object,
		// except that the first match MUST NOT be a consonant
		exampleRule.setSubsubRuleIsNot(0, 0, true);
		
		String exampleRuleString = "!C_._V";
		Rule parsedRule = Rule.parseStringToRule(exampleRuleString);
		assertEquals(exampleRule.toString(), parsedRule.toString());
	}

	@Test
	public void test_parseStringToRule_isStrictTypeMatch() {
		// Check if parsing the given rule string matches the example Rule object,
		// except that the last match MUST be a narrowConso type
		exampleRule.setSubsubRuleType(0, 2, "narrowConso");
		exampleRule.setSubsubRuleIsStrictTypeMatch(0, 2, true);
		
		String exampleRuleString = "C_._<narrowConso>";
		Rule parsedRule = Rule.parseStringToRule(exampleRuleString);
		assertEquals(exampleRule.toString(), parsedRule.toString());
	}

	@Test
	public void test_parseStringToRule_counterRule() {
		// Create rule that represents the rule string "c=2"
		exampleRule = new Rule(1);
		exampleRule.setSubRulecVal(0, 2);
		
		String exampleRuleString = "c=2";
		Rule parsedRule = Rule.parseStringToRule(exampleRuleString);		
		assertEquals(exampleRule.toString(), parsedRule.toString());
	}

	@Test
	public void test_parseStringToRule_phonemeVariantRule() {
		// Create rule that represents the rule string "pv=1"
		exampleRule = new Rule(1);
		exampleRule.setSubRulePvVal(0, 1);
		
		String exampleRuleString = "pv=1";
		Rule parsedRule = Rule.parseStringToRule(exampleRuleString);
		assertEquals(exampleRule.toString(), parsedRule.toString());
	}

}
