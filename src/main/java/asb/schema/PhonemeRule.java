package asb.schema;

import java.util.Arrays;

/**
 * Map L1 graphemes to L2 graphemes using a set of rules to decide which distinct grapheme will be selected.
 * @author perry
 *
 */
public class PhonemeRule {
	private String[] l1; // List of L1 graphemes
	private String l1type; // The type of the L1 phoneme
	private String[] l1rule; // List of rules for converting L2 to L1
	private Rule[] l1ruleParsed; // Parsed version of l1rule
	private String[] l2; // List of L2 graphemes
	private String l2type; // The type of the L2 phoneme
	private String[] l2rule; // List of rules for converting L1 to L2
	private Rule[] l2ruleParsed; // Parsed version of l2rule
	
	public PhonemeRule(String[] l1, String l1type, String[] l1rule, String[] l2, String l2type,
			String[] l2rule) {
		super();
		this.l1 = l1;
		this.l1type = l1type;
		this.l1rule = l1rule;
		this.l1ruleParsed = new Rule[l1rule.length];
		for (int i = 0; i < l1rule.length; i++) {
			l1ruleParsed[i] = Rule.emptyRule();
		}
		this.l2 = l2;
		this.l2type = l2type;
		this.l2rule = l2rule;
		this.l2ruleParsed = new Rule[l2rule.length];
		for (int i = 0; i < l2rule.length; i++) {
			l2ruleParsed[i] = Rule.emptyRule();
		}
	}

	@Override
	public String toString() {
		return "PhonemeRule [l1=" + Arrays.toString(l1) + ", l1type=" + l1type + ", l1rule=" + Arrays.toString(l1rule)
				+ ", l2=" + Arrays.toString(l2) + ", l2type=" + l2type + ", l2rule=" + Arrays.toString(l2rule) + "]";
	}

	public String[] l1() {
		return l1;
	}
	public void setL1(String[] l1) {
		this.l1 = l1;
	}
	public String l1type() {
		return l1type;
	}
	public void setL1type(String l1type) {
		this.l1type = l1type;
	}
	public String[] l1rule() {
		return l1rule;
	}
	public void setL1rule(String[] l1rule) {
		this.l1rule = l1rule;
	}
	public Rule[] l1ruleParsed() {
		return l1ruleParsed;
	}
	public void setL1ruleParsed(Rule[] l1ruleParsed) {
		this.l1ruleParsed = l1ruleParsed;
	}	
	public String[] l2() {
		return l2;
	}
	public void setL2(String[] l2) {
		this.l2 = l2;
	}
	public String l2type() {
		return l2type;
	}
	public void setL2type(String l2type) {
		this.l2type = l2type;
	}
	public String[] l2rule() {
		return l2rule;
	}
	public void setL2rule(String[] l2rule) {
		this.l2rule = l2rule;
	}
	public Rule[] l2ruleParsed() {
		return l2ruleParsed;
	}
	public void setL2ruleParsed(Rule[] l2ruleParsed) {
		this.l2ruleParsed = l2ruleParsed;
	}

}
