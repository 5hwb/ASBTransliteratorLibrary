package asb.script.transcoder.parsing;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import asb.schema.PhonemeRule;

public class TokeniserUnitTests {
	
	String input, input2, input3, inputPunct;
	Map<String, PhonemeRule> mapping;
	Map<String, Integer> graphemeVarIndexMap;
	
	@Before
	public void init() {
		input = "this is a test. you fine?";
		input2 = "are you ready!";
		input3 = "fine it is.";
		inputPunct = "..reaine";
		
		// Maps graphemes to phonemes
		mapping = new HashMap<>();
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

		mapping.put("are", new PhonemeRule( new String[] { "are" }, "vowel", new String[] {""},
				new String[] { "ar" }, "vowel", new String[] {""}));

		mapping.put("rea", new PhonemeRule( new String[] { "rea" }, "syllable", new String[] {""},
				new String[] { "ré" }, "syllable", new String[] {""}));

		mapping.put("dy", new PhonemeRule( new String[] { "dy" }, "syllable", new String[] {""},
				new String[] { "di" }, "syllable", new String[] {""}));

		graphemeVarIndexMap = new HashMap<String, Integer>();
		graphemeVarIndexMap.put("th", 0);		                             
		graphemeVarIndexMap.put("i", 0);		                            
		graphemeVarIndexMap.put("s", 0);		                             
		graphemeVarIndexMap.put("a", 0);		                             
		graphemeVarIndexMap.put("t", 0);		                             
		graphemeVarIndexMap.put("e", 0);		                             
		graphemeVarIndexMap.put("y", 0);		                             
		graphemeVarIndexMap.put("ou", 0);		                             
		graphemeVarIndexMap.put("f", 0);		                             
		graphemeVarIndexMap.put("ine", 0);		                             
		graphemeVarIndexMap.put("are", 0);		                             
		graphemeVarIndexMap.put("rea", 0);		                             
		graphemeVarIndexMap.put("dy", 0);
}
	
	@Test
	public void test_tokeniser_oddArguments() {
		Tokeniser tokeniser_inputIsNull = new Tokeniser(null, mapping, graphemeVarIndexMap);
		assertNull(tokeniser_inputIsNull.readNextToken());

		Tokeniser tokeniser_mapIsNull = new Tokeniser(input, null, graphemeVarIndexMap);
		assertNull(tokeniser_mapIsNull.readNextToken());

		Tokeniser tokeniser_allNull = new Tokeniser(null, null, graphemeVarIndexMap);
		assertNull(tokeniser_allNull.readNextToken());
	}

	@Test
	public void test_readNextToken_input() {
		Tokeniser tokeniser = new Tokeniser(input, mapping, graphemeVarIndexMap);
		
		// input = "this is a test. you fine?";
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
		assertEquals("", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertNull(tokeniser.readNextToken());
	}

	@Test
	public void test_readNextToken_input2() {
		Tokeniser tokeniser = new Tokeniser(input2, mapping, graphemeVarIndexMap);
		
		// input2 = "are you ready!";
		assertEquals("ar", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(" ", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("y", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("ù", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(" ", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("ré", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("di", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("!", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertNull(tokeniser.readNextToken());
	}

	@Test
	public void test_readNextToken_input3() {
		Tokeniser tokeniser = new Tokeniser(input3, mapping, graphemeVarIndexMap);
		
		// input3 = "fine it is.";
		assertEquals("f", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("ān", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(" ", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("i", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("t", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(" ", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("i", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("s", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(".", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertNull(tokeniser.readNextToken());
	}

	@Test
	public void test_readNextToken_inputPunct() {
		Tokeniser tokeniser = new Tokeniser(inputPunct, mapping, graphemeVarIndexMap);
		
		// inputPunct = "..reaine";
		assertEquals(".", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals(".", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("ré", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertEquals("ān", tokeniser.readNextToken().phonemeRule().l2()[0]);
		assertNull(tokeniser.readNextToken());
	}

}
