package asb.script.transcoder;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import asb.hangul.HangulUtils;
import asb.schema.Rule;
import asb.script.transcoder.ExternalFileReplacer;

public class EFReplacerUnitTests {
	
	ExternalFileReplacer efr;

	String testo = "Yor jast in tām Goĝ! Sē cíz, đe nù kar śòs of its ékstrávegens "
			+ "wiŧ áźer kalers, dánsi bíts ánd hopiŋ Loĥ Nés stāl saspénśen. "
			+ "Luk àt hèr, đis ēnt nò tō! Kya tei syo 1234567890.";
	
	String testoHan = "이올 찻드 인 댐 콬흐! 세 즷흐, 떠 뉴 갈 쑛 오쁘 읻스 엯드럎허컨스 "
			+ "위쯔 얐헐 가껈, 탼시 픧스 얀트 호빙 꼭흐 녓 스댂 삿변썬. "
			+ "꾹 얟 혤, 띳 엔드 뇨 되! 기아 더이 시오 一二三四五六七八九〇.";
	
	String testoKhm = "យួរ ជាស្ត ឥន តៃម គួឃ! សែ ចីឌ។ ថ់ នឹ ការ ឆូស ឲផ ឥត្ស ឯក្សត្រ៉ភ់គ់ន្ស "
			+ "វិឋ អ៉ឈ់រ កាល់រ្ស។ ទ៉ន្សិ ពីត្ស អ៉ន្ទ ហួបិង លួខ នេស ស្តៃល សាស្បេន្ឆ់ន៕ "
			+ "លុក ឳត ហៀរ។ ថិស ឯះន្ត នូ តួះ! ក្យា ត់ឥ ស្យួ ១២៣៤៥៦៧៨៩០៕";

	String testoQuik = "    !  ,        " 
			+ "  ,        . "
			+ "  ,    !    .";
	
	String testoConsoEbeo2 = "Léts hopz hopza pzi pz. Sdroŋ malds át đe hafs pozt níd ù.";
	String testoConsoHan2 = "껻스 홊흐 홊하 븟히 븟흐. 슽롱 맊틋 얃 떠 하쁫 봇흗 닅 유.";
	
	String testoConsoEbeo1 = "Léts blē blkē ablkē inklud. Sdroŋ malds át đe hafs pibtkhśa níd ù tu éksklēm tu đe ūrld.";
	String testoConsoKhm1 = "លេត្ស ព្លែ ព្លកែ អព្លកែ ឥន្កលុទ៕ ស្ទ្រួង មាល្ទ្ស អ៉ត ថ់ ហាផ្ស បិព្តក្ហឆា នីទ យុ តុ ឯក្សក្លែម តុ ថ់ វ់រល្ទ៕";
	
	@Before
	public void init() {
		efr = new ExternalFileReplacer("src/main/java/rulefiles/hangul.json");
	}

	@Test
	public void test_parseStringToRule() {
		
		Rule examplarRule = new Rule(3);
		examplarRule.setSubsubRuleType(0, 0, "consonant");
		examplarRule.setSubsubRuleIsNot(0, 0, true);
		examplarRule.setSubsubRuleType(0, 1, "anything");
		examplarRule.setSubsubRuleIsNot(0, 1, false);
		examplarRule.setSubsubRuleType(0, 2, "vowel");
		examplarRule.setSubsubRuleIsNot(0, 2, false);
		examplarRule.setSubsubRuleType(1, 0, "punctuation");
		examplarRule.setSubsubRuleIsNot(1, 0, false);
		examplarRule.setSubsubRuleType(1, 1, "anything");
		examplarRule.setSubsubRuleIsNot(1, 1, false);
		examplarRule.setSubsubRuleType(1, 2, "vowel");
		examplarRule.setSubsubRuleIsNot(1, 2, true);
		examplarRule.setSubRulecVal(2, 6);
		
		String testRule = "!C_._V | P_._!V | c=6";
		Rule testRuleFinal = Rule.parseStringToRule(testRule);
		assertEquals(examplarRule.toString(), testRuleFinal.toString());
	}
	
	@Test
	public void test_toScript_hangul() {
		efr.setFilePath("src/main/java/rulefiles/hangul.json");
		assertEquals(testoHan, HangulUtils.convertToHangulBlocks(efr.translateToScript(testo)));
	}
	
	@Test
	public void test_fromScript_hangul() {
		efr.setFilePath("src/main/java/rulefiles/hangul.json");
		assertEquals(testo, efr.translateFromScript(HangulUtils.convertFromHangulBlocks(testoHan)));
	}
	
	@Test
	public void test_consonants_hangul() {
		efr.setFilePath("src/main/java/rulefiles/hangul.json");
		assertEquals(testoConsoHan2, HangulUtils.convertToHangulBlocks(efr.translateToScript(testoConsoEbeo2)));
	}

	@Test
	public void test_toScript_khmer() {
		efr.setFilePath("src/main/java/rulefiles/khmer.json");
		assertEquals(testoKhm, efr.translateToScript(testo));
	}
	
	@Test
	public void test_fromScript_khmer() {
		efr.setFilePath("src/main/java/rulefiles/khmer.json");
		assertEquals(testo, efr.translateFromScript(testoKhm));
	}
	
	@Test
	public void test_consonants_khmer() {
		efr.setFilePath("src/main/java/rulefiles/khmer.json");
		assertEquals(testoConsoKhm1, efr.translateToScript(testoConsoEbeo1));
	}

	@Test
	public void test_toScript_quikscript() {
		efr.setFilePath("src/main/java/rulefiles/quikscript.json");
		assertEquals(testoQuik, efr.translateToScript(testo));
	}
	
	@Test
	public void test_fromScript_quikscript() {
		efr.setFilePath("src/main/java/rulefiles/quikscript.json");
		assertEquals(testo, efr.translateFromScript(testoQuik));
	}
}
