package asb.script.transcoder.parsing;

import asb.schema.PhonemeRule;

/**
 * Represents a grapheme in the input string.
 * @author perry
 *
 */
public class CharToken {

	/** PhonemeRule containing grapheme information */
	protected PhonemeRule phonemeRule;
	/** Index of selected grapheme variant */
	protected int graphemeVarIndex;
	
	/** Preceding token */
	protected CharToken prev;
	/** Following token */
	protected CharToken next;

	public CharToken(PhonemeRule phonemeRule, int graphemeVarIndex, CharToken prev, CharToken next) {
		this.phonemeRule = phonemeRule;
		this.graphemeVarIndex = graphemeVarIndex;
		this.prev = prev;
		this.next = next;
	}
	
	//////////////////////////////////////////////////
	// GETTER AND SETTER METHODS
	//////////////////////////////////////////////////

	public PhonemeRule phonemeRule() {
		return phonemeRule;
	}

	public int graphemeVarIndex() {
		return graphemeVarIndex;
	}

	public void setPhonemeRule(PhonemeRule phonemeRule) {
		this.phonemeRule = phonemeRule;
	}

	public CharToken prev() {
		return prev;
	}

	public void setPrev(CharToken prev) {
		this.prev = prev;
	}

	public CharToken next() {
		return next;
	}

	public void setNext(CharToken next) {
		this.next = next;
	}
	
	//////////////////////////////////////////////////
	// OTHER METHODS
	//////////////////////////////////////////////////
	
	@Override
	public String toString() {
		String prevString = "";
		if (this.prev() != null) {
			prevString += this.prev().graphemeVarIndex() + " ";
			prevString += this.prev().phonemeRule().toString();
		}
		String nextString = "";
		if (this.next() != null) {
			nextString += this.next().graphemeVarIndex() + " ";
			nextString += this.next().phonemeRule().toString();
		}		
		
		StringBuilder sb = new StringBuilder();
		sb.append("==========\n");
		sb.append("PREV: " + prevString + "\n");
		sb.append("CURR: " + this.graphemeVarIndex + " " + this.phonemeRule.toString() + "\n");
		sb.append("NEXT: " + nextString + "\n");
		return sb.toString();
	}
	
	
}
