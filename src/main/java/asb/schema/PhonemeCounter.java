package asb.schema;

/**
 * Count the presence of a certain phoneme type to help decide which grapheme will be selected.
 * @author perry
 *
 */
public class PhonemeCounter {
	private String type;       // Phoneme type name
	private int maxNum = 0;    // Maximum number of the counter
	private int value = 0;     // Current counter value
	private String[] incrRule; // List of pattern rules defining when to increment the counter
	private Rule[] incrRuleParsed; // Parsed version of incrRule
	
	//////////////////////////////////////////////////
	// GETTER AND SETTER METHODS
	//////////////////////////////////////////////////

	public String type() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public int maxNum() {
		return maxNum;
	}
	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}
	
	public int value() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	
	public String[] incrRule() {
		return incrRule;
	}
	public void setIncrRule(String[] incrRule) {
		this.incrRule = incrRule;
	}
	
	public Rule[] incrRuleParsed() {
		return incrRuleParsed;
	}
	public void setIncrRuleParsed(Rule[] incrRuleParsed) {
		this.incrRuleParsed = incrRuleParsed;
	}

	//////////////////////////////////////////////////
	// COUNTER METHODS
	//////////////////////////////////////////////////
	
	public void increment() {
		this.value++;
	}
	public void reset() {
		this.value = 0;
	}
	public boolean valueIsMax() {
		return value >= maxNum;
	}
}
