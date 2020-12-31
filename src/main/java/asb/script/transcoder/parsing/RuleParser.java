package asb.script.transcoder.parsing;

import asb.schema.Rule;

public interface RuleParser {
	
	/**
	 * The name of this RuleParser.
	 * @return The RuleParser name
	 */
	public String name();

	/**
	 * Select this rule if it matches a certain set of conditions.
	 * 
	 * @param cToken
	 * @param pRule
	 * @param j
	 * @param toScript
	 * @return
	 */
	public boolean matchesCondition(CharToken cToken, Rule pRule, int j, boolean toScript);
	
	/**
	 * Determine if a subrule matches the context.
	 * 
	 * @param cToken
	 * @param pRule
	 * @param j
	 * @param toScript
	 * @return
	 */
	public boolean isSubruleMatch(CharToken cToken, Rule pRule, int j, boolean toScript);

//	public boolean matchesCondition();
//	public int getMatchingRule();
}
