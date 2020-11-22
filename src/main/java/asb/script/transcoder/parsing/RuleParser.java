package asb.script.transcoder.parsing;

public interface RuleParser {

	public boolean matchesCondition();
	public int getMatchingRule();
}
