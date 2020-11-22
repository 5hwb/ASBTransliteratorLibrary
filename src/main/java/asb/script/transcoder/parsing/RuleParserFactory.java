package asb.script.transcoder.parsing;

import java.util.HashMap;
import java.util.Map;

public class RuleParserFactory {
	Map<String, RuleParser> ruleParserMap;
	
	private static RuleParserFactory instance;
	
	public RuleParserFactory() {
		ruleParserMap = new HashMap<>();
		
		// add all RuleParser types
		addRuleParser(new PatternRuleParser(), "PatternRuleParser");
		addRuleParser(new CounterRuleParser(), "CounterRuleParser");
	}
	
	/**
	 * (Private) Add the RuleParser instance with the given name to the map. 
	 * @param ruleParser The RuleParser instance
	 * @param name The name
	 */
	private void addRuleParser(RuleParser ruleParser, String name) {
		if (!ruleParserMap.containsKey(name)) {
			ruleParserMap.put(name, ruleParser);
		}
	}
	
	/**
	 * Get the singleton instance of RuleParserFactory.
	 * @return The RuleParserFactory instance
	 */
	public static RuleParserFactory getInstance() {
		if (instance == null) {
			instance = new RuleParserFactory();
		}
		
		return instance;
	}
	
	/**
	 * Get the rule parser with the given name.
	 * @param rpName The name of the RuleParser. e.g. 'PatternRuleParser'
	 * @return The matching RuleParser if present. and null if the name is invalid.
	 */
	public RuleParser getRuleParser(String rpName) {
		return ruleParserMap.get(rpName);
	}
	
	
}
