package asb.script.transcoder.parsing;

import static org.junit.Assert.*;

import org.junit.Before;
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
		//assertEquals(rp.getMatchingRule(), 888);
	}

	@Test
	public void test_getCounterRuleParser() {
		System.out.println("===== test_getCounterRuleParser() =====");
		RuleParser rp2 = rpf.getRuleParser("CounterRuleParser");
		//assertEquals(rp2.getMatchingRule(), 777);
	}

}
