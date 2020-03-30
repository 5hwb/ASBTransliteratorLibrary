package asb.script.transcoder.parsing;

import asb.schema.PhonemeRule;

public class CharToken {

	protected PhonemeRule phonemeRule;
	
	protected CharToken prev;
	protected CharToken next;

	public CharToken(PhonemeRule phonemeRule, CharToken prev, CharToken next) {
		this.phonemeRule = phonemeRule;
		this.prev = prev;
		this.next = next;
	}

	public PhonemeRule phonemeRule() {
		return phonemeRule;
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
	
	
	
}
