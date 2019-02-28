package asb.schema;

public class RuleSchema {
	private PhonemeRule[] rules;
	private PhonemeType[] types;
	private PhonemeCounter[] counters;
	
	public RuleSchema() {
	}

	public PhonemeRule[] rules() {
		return rules;
	}

	public void setRules(PhonemeRule[] rules) {
		this.rules = rules;
	}

	public PhonemeType[] types() {
		return types;
	}

	public void setTypes(PhonemeType[] types) {
		this.types = types;
	}

	public PhonemeCounter[] counters() {
		return counters;
	}

	public void setCounters(PhonemeCounter[] counters) {
		this.counters = counters;
	}

}
