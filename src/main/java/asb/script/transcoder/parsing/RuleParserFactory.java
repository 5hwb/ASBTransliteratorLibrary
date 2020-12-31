package asb.script.transcoder.parsing;

import java.util.HashSet;
import java.util.Set;

/**
 * Factory that handles RuleParser instances.
 * @author perry
 *
 */
public class RuleParserFactory {
	private static RuleParserFactory instance;

	Set<RuleParser> ruleParsers;
	
	private RuleParserFactory() {
		ruleParsers = new HashSet<RuleParser>();

		// Add all RuleParser types
		ruleParsers.add(new PatternRuleParser());
		ruleParsers.add(new CounterRuleParser());
		ruleParsers.add(new PhonemeVariantRuleParser());
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
	 * Get the RuleParser with the given name.
	 * @param rpName The name of the RuleParser. e.g. 'PatternRuleParser'
	 * @return The matching RuleParser if present. Returns null if the name is invalid.
	 */
	public RuleParser getRuleParser(String rpName) {
		for (RuleParser rp : ruleParsers) {
			if (rp.name().equals(rpName))
				return rp;
		}
		return null;
	}
	
	/**
	 * Get all RuleParser instances.
	 * @return HashSet of all RuleParser instances
	 */
	public Set<RuleParser> getRuleParsers() {
		return ruleParsers;
	}
	
	
}
