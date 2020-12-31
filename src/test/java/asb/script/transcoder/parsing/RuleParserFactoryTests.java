package asb.script.transcoder.parsing;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

public class RuleParserFactoryTests {

	static RuleParserFactory rpf;
	
	@BeforeClass
	public static void setup() {
		System.out.println("===== setup() =====");
		rpf = RuleParserFactory.getInstance();
	}
	
	@Test
	public void test_getPatternRuleParser() {
		System.out.println("===== test_getPatternRuleParser() =====");
		RuleParser rp = rpf.getRuleParser("PatternRuleParser");
		assertEquals(rp.name(), "PatternRuleParser");
	}

	@Test
	public void test_getCounterRuleParser() {
		System.out.println("===== test_getCounterRuleParser() =====");
		RuleParser rp2 = rpf.getRuleParser("CounterRuleParser");
		assertEquals(rp2.name(), "CounterRuleParser");
	}

	@Test
	public void test_getallRuleParsers() {
		System.out.println("===== test_getallRuleParsers() =====");
		// Get the RuleParser names
		Set<RuleParser> ruleParsers = rpf.getRuleParsers();
		Set<String> ruleParserNames = new HashSet<String>();		
		for (RuleParser rp : ruleParsers) {
			ruleParserNames.add(rp.name());
		}
		
		// Create a set containing the actual RuleParser names
		Set<String> actualRuleParserNames = new HashSet<String>();
		actualRuleParserNames.add("PatternRuleParser");
		actualRuleParserNames.add("CounterRuleParser");
		actualRuleParserNames.add("PhonemeVariantRuleParser");

		assertEquals(true, ruleParserNames.equals(actualRuleParserNames));
	}

}
