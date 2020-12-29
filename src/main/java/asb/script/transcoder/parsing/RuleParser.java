package asb.script.transcoder.parsing;

import asb.schema.Rule;

public interface RuleParser {

	public boolean matchesCondition(CharToken cToken, Rule pRule, int j, boolean toScript);
	public boolean isMatch(CharToken cToken, Rule pRule, int j, boolean toScript);

//	public boolean matchesCondition();
//	public int getMatchingRule();
}
