package asb.schema;

public class PhonemeCounter {
	private String type;
	private int maxNum = 0;
	private int value = 0;
	private String[] incrRule;
	private Rule[] incrRuleParsed;
	
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