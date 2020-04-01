package asb.script.transcoder.parsing;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import asb.schema.PhonemeRule;

public class TokeniserUnitTests {

	@Test
	public void test_readNextToken() {
		String input = "this is a test. you fine?";
		
		// Maps graphemes to phonemes
		Map<String, PhonemeRule> mapping = new HashMap<>();
		mapping.put("th", new PhonemeRule( new String[] { "th" }, "consonant", new String[] {""},
				new String[] { "đ" }, "consonant", new String[] {""}));

		mapping.put("i", new PhonemeRule( new String[] { "i" }, "vowel", new String[] {""},
				new String[] { "i" }, "vowel", new String[] {""}));
		
		mapping.put("s", new PhonemeRule( new String[] { "s" }, "consonant", new String[] {""},
				new String[] { "s" }, "consonant", new String[] {""}));
		
		mapping.put("a", new PhonemeRule( new String[] { "a" }, "vowel", new String[] {""},
				new String[] { "e" }, "vowel", new String[] {""}));
		
		mapping.put("t", new PhonemeRule( new String[] { "t" }, "consonant", new String[] {""},
				new String[] { "t" }, "consonant", new String[] {""}));
		
		mapping.put("e", new PhonemeRule( new String[] { "e" }, "vowel", new String[] {""},
				new String[] { "é" }, "vowel", new String[] {""}));
		
		mapping.put("y", new PhonemeRule( new String[] { "y" }, "consonant", new String[] {""},
				new String[] { "y" }, "consonant", new String[] {""}));
		
		mapping.put("ou", new PhonemeRule( new String[] { "ou" }, "vowel", new String[] {""},
				new String[] { "ù" }, "vowel", new String[] {""}));
		
		mapping.put("f", new PhonemeRule( new String[] { "f" }, "consonant", new String[] {""},
				new String[] { "f" }, "consonant", new String[] {""}));
		
		mapping.put("ine", new PhonemeRule( new String[] { "ine" }, "syllable", new String[] {""},
				new String[] { "ān" }, "syllable", new String[] {""}));

		Tokeniser tokeniser = new Tokeniser(input, mapping);
		
		assertEquals("đ", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("i", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("s", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(" ", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("i", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("s", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(" ", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("e", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(" ", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("t", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("é", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("s", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("t", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(".", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(" ", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("y", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("ù", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(" ", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("f", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("ān", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("?", tokeniser.readNextToken().phonemeRule().l2()[0]);
	}

}
