package asb.schema;

/**
 * Representation of the JSON rulefile schema used for parsing the rulefiles.
 * @author perry
 *
 */
public class RuleSchema {
	private PhonemeRule[] rules; // List of phoneme rules
	private PhonemeType[] types; // List of phoneme types used
	private PhonemeCounter[] counters; // List of phoneme type counters
	
	public RuleSchema() {
	}

	//////////////////////////////////////////////////
	// GETTER AND SETTER METHODS
	//////////////////////////////////////////////////

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
