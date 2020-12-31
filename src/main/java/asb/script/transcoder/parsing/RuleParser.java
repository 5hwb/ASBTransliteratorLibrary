package asb.script.transcoder.parsing;

import asb.schema.Rule;

/**
 * Interface for RuleParser implementations.
 * RuleParser contains methods for handling rule parsing to determine which
 * grapheme character should be substituted based on the surrounding context of
 * its neighbouring graphemes.
 * @author perry
 *
 */
public interface RuleParser {
	
	/**
	 * The name of this RuleParser.
	 * @return The RuleParser name
	 */
	public String name();

	/**
	 * Select this rule if it matches a certain set of conditions.
	 * 
	 * @param cToken The current token
	 * @param pRule The Rule to check
	 * @param j Index of subrule to check
	 * @param toScript Translate the input to script, or back?
	 * @return True if this rule is supported by a particular RuleParser implementation
	 */
	public boolean matchesCondition(CharToken cToken, Rule pRule, int j, boolean toScript);
	
	/**
	 * Determine if a subrule matches the context.
	 * 
	 * @param cToken The current token
	 * @param pRule The Rule to check
	 * @param j Index of subrule to check
	 * @param toScript Translate the input to script, or back?
	 * @return True if the subrule is a match for the context
	 */
	public boolean isSubruleMatch(CharToken cToken, Rule pRule, int j, boolean toScript);

	
	/**
	 * Action to carry out after a successful match.
	 * 
	 * @param cToken The current token
	 * @param pRule The Rule to check
	 * @param j Index of subrule to check
	 * @param toScript Translate the input to script, or back?
	 */
	public void postMatch(CharToken cToken, Rule pRule, int j, boolean toScript);

//	public boolean matchesCondition();
//	public int getMatchingRule();
}
