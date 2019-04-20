package asb.script.tests;

import java.io.FileNotFoundException;
import asb.script.transcoder.ExternalFileReplacer;

public class Testing {
	static void testExtFileRepl() {
		ExternalFileReplacer efr = new ExternalFileReplacer("src/main/java/rulefiles/hangul.json");
		
		//String testoEbeo = "Léts du đis strakt rāt nà. Yor đe véri bést Kév!";
		String testoEbeo = "Léts blē blkē ablkē inklud. Sdroŋ malds át đe hafs pibtkhśa níd ù tu éksklēm tu đe ūrld.";
		String testoHan = "ㄲㅕㄷㅅㅡ ㅌㅜ ㄸㅣㅅ ㅅㅡㄷㄹㅏㄱㄷㅡ ㄹㅐㄷ ㄴㅒ. ㅇㅣㅇㅗㄹ ㄸㅓ ㅍㅡㅎㅕㄹㅣ ㅍㅕㅅㄷㅡ ㄱㅕㅍㅎㅡ!";
		//String testoKhm = "លេត្ស ទុ ថិស ស្ត្រាក្ត រៃត នៅ៕ យួរ ថ់ ភេរិ ពេស្ត កេភ!";
		String testoKhm = "លេត្ស ព្លែ ព្លកែ អព្លកែ ឥន្កលុទ៕ ស្ទ្រួង មាល្ទ្ស អ៉ត ថ់ ហាផ្ស បិព្តក្ហឆា នីទ យុ តុ ឯក្សក្លែម តុ ថ់ វ់រល្ទ៕";
		
		// TEST EBEO -> SCRIPT
		if (true) {
			String res = efr.translateToScript(testoEbeo);
			System.out.println("============RESULT:=============");
			System.out.printf("[%s]\n", res);
			System.out.println("===========INTENDED:============");
			System.out.printf("[%s]\n", testoKhm);
			//System.out.println(HangulUtils.convertToHangulBlocks(res));
		}

		// TEST HANGUL -> EBEO
		if (true) {
			String res2 = efr.translateFromScript(testoHan);
			System.out.println("============RESULT:=============");
			System.out.printf("[%s]\n", res2);
			System.out.println("===========INTENDED:============");
			System.out.printf("[%s]\n", testoEbeo);		
		}

		// TEST KHMER -> EBEO
		if (true) {
			String res2 = efr.translateFromScript(testoKhm);
			System.out.println("============RESULT:=============");
			System.out.printf("[%s]\n", res2);
			System.out.println("===========INTENDED:============");
			System.out.printf("[%s]\n", testoEbeo);		
		}
	}
	
	static void testQuikscript() {
		ExternalFileReplacer efr = new ExternalFileReplacer("src/main/java/rulefiles/quikscript.json");
		String testoEbeo = "Input is glorès. Kán ù du it? Yés? Grēt trā, Korel!";
		String testoQuik = "  .    ? ?  , !";
		
		String res2 = efr.translateToScript(testoEbeo);
		System.out.println("============RESULT:=============");
		System.out.printf("[%s]\n", res2);
		System.out.println("===========INTENDED:============");
		System.out.printf("[%s]\n", testoQuik);		
	}
	
	static void andMatchTest() {
		boolean[][] isMatch = {
				{false, false, false},
				{false, false, true},
				{false, true, true},
				{true, false, true},
				{true, true, true},
				};
		for (int i = 0; i < isMatch.length; i++) {
			boolean theyDoMatch = true;
			for (int j = 0; j < 3; j++) {
				theyDoMatch &= isMatch[i][j];
			}
			System.out.printf("[%b]\n", theyDoMatch);
		}

	}
	
	public static void main(String[] args) throws FileNotFoundException {
		//testExtFileRepl();
		testQuikscript();
		//andMatchTest();
	}
}