package asb.hangul;

import static org.junit.Assert.*;

import org.junit.Test;

public class HangulUtilsTests {

	@Test
	public void test_convertToHangulBlocks() {
		/* NAKD: if the final consonant is followed by another consonant, 
		 * it's certainly a part of the current syllable */
		assertEquals("낰다",  HangulUtils.convertToHangulBlocks("ㄴㅏㅋㄷㅏ"));
		assertEquals("낰ㄷ",  HangulUtils.convertToHangulBlocks("ㄴㅏㅋㄷ"));
		
		/* NADA: if the final consonant is followed by a vowel, 
		 * it's certainly not part of the current syllable  */
		assertEquals("나다",  HangulUtils.convertToHangulBlocks("ㄴㅏㄷㅏ"));
		
		/* NAK: The first 2 situations do not apply but the current syllable has a final consonant */
		assertEquals("낰",  HangulUtils.convertToHangulBlocks("ㄴㅏㅋ"));
		
		/* NA: The first 2 situations do not apply but the current syllable has no final */
		assertEquals("나",  HangulUtils.convertToHangulBlocks("ㄴㅏ"));

		/* Non-Hangul punctuation should be left as-is */
		assertEquals("..!..",  HangulUtils.convertToHangulBlocks("..!.."));
		
		/* A more complicated string example */
		assertEquals("안녕하세요 세계가 없습니다",  HangulUtils.convertToHangulBlocks("ㅇㅏㄴㄴㅕㅇㅎㅏㅅㅔㅇㅛ ㅅㅔㄱㅖㄱㅏ ㅇㅓㅄㅅㅡㅂㄴㅣㄷㅏ"));
	}

	@Test
	public void test_convertFromHangulBlocks() {
		/* Hangul syllable block strings */
		assertEquals("ㄴㅏㅋㄷㅏ",  HangulUtils.convertFromHangulBlocks("낰다"));
		assertEquals("ㄴㅏㅋㄷ",  HangulUtils.convertFromHangulBlocks("낰ㄷ"));
		assertEquals("ㄴㅏㄷㅏ",  HangulUtils.convertFromHangulBlocks("나다"));
		assertEquals("ㄴㅏㅋ",  HangulUtils.convertFromHangulBlocks("낰"));
		assertEquals("ㄴㅏ",  HangulUtils.convertFromHangulBlocks("나"));

		/* Non-Hangul punctuation should be left as-is */
		assertEquals("..!..",  HangulUtils.convertFromHangulBlocks("..!.."));
		
		/* A more complicated string example */
		assertEquals("ㅇㅏㄴㄴㅕㅇㅎㅏㅅㅔㅇㅛ ㅅㅔㄱㅖㄱㅏ ㅇㅓㅄㅅㅡㅂㄴㅣㄷㅏ",  HangulUtils.convertFromHangulBlocks("안녕하세요 세계가 없습니다"));
	}
}
