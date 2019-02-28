package asb.hangul;

public class HangulUtils {

	// 19 initials
	private static char[] initials = {
			'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ',
			'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
			'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ',
			'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
	};

	// 21 medials
	private static char[] medials = {
			'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ',
			'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
			'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ',
			'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ',
			'ㅣ'
	};

	// 28 finals
	private static char[] finals = {
			' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ',
			'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
			'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ',
			'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
			'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ',
			'ㅌ', 'ㅍ', 'ㅎ'
	};
	
	private static int getIndex(char[] data, char c) {
		for (int i = 0; i < data.length; i++) {
			if (c == data[i]) return i;
		}
		return 0;
	}
	
	public static String convertToHangulBlocks(String input) {
		StringBuilder sb = new StringBuilder();
		
		// Regular expression to match a Korean syllable
		String allInitials = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
		String allMedials = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
		String allFinals = "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";
		String allConsos = "ㄱㄲㄳㄴㄵㄶㄷㄸㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅃㅄㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
		String matchIM = String.format("[%s][%s]", allInitials, allMedials);
		String matchIMF = matchIM + String.format("[%s]", allFinals);
		String matchIMIM = matchIM + matchIM;
		String matchIMFI = matchIMF + String.format("[%s]", allInitials);		

		int i;
		for (i = 0; i < input.length(); i++) {
			//*DEBUG*/System.out.println("=============================");
			String buf4 = (input.length() < 4)
					? (input + "    ").substring(0, 4)
					: (i+4 >= input.length())
							? (input.substring(i, input.length()) + "    ").substring(0, 4)
							: input.substring(i, i + 4);
			
			String buf3 = (i+3 >= input.length())
					? (input.substring(i, input.length()) + "   ").substring(0, 3)
					: buf4.substring(0, 3);
			
			String buf2 = (i+2 >= input.length())
					? (input.substring(i, input.length()) + "  ").substring(0, 2)
					: buf4.substring(0, 2);

			//*DEBUG*/System.out.printf("buf4=[%s]\n", buf4);
			//*DEBUG*/System.out.printf("buf3=[%s]\n", buf3);
			//*DEBUG*/System.out.printf("buf2=[%s]\n", buf2);					
			int init = getIndex(initials, buf4.charAt(0));
			int med = getIndex(medials, buf4.charAt(1));
			int fin = getIndex(finals, buf4.charAt(2));
			
			/**
			 * There are 4 possible arrangements for a bunch of 4 Hangul jamo.
			 * Each arrangement determines whether a consonant jamo will be the
			 * final consonant of the 1st syllable or the initial of the 2nd
			 */
			
			/** NAKD: if the final consonant is followed by another consonant, 
			 * it's certainly a part of the current syllable */
			if (buf4.matches(matchIMFI)) {
				char syl = (char) ((init * 588 + med * 28 + fin) + 44032);
				//*DEBUG*/System.out.println("NAKD:   ");
				//*DEBUG*/System.out.printf("i=%d init=%c med=%c fin=%c SYL=[%s]\n", i, initials[init], medials[med], finals[fin], syl);
				sb.append(syl);
				i += 2;
				continue;
			}
			
			/** NADA: if the final consonant is followed by a vowel, 
			 * it's certainly not part of the current syllable  */
			else if (buf4.matches(matchIMIM)) {
				char syl = (char) ((init * 588 + med * 28) + 44032);
				//*DEBUG*/System.out.println("NADA:   ");
				//*DEBUG*/System.out.printf("i=%d init=%c med=%c fin=%c SYL=[%s]\n", i, initials[init], medials[med], finals[fin], syl);
				sb.append(syl);
				i += 1;
				continue;
			}
			
			/** NAK: The above 2 situations do not apply but the current syllable has a final consonant */
			else if (buf3.matches(matchIMF)) {
				char syl = (char) ((init * 588 + med * 28 + fin) + 44032);
				//*DEBUG*/System.out.println("NAK:    ");
				//*DEBUG*/System.out.printf("i=%d init=%c med=%c fin=%c SYL=[%s]\n", i, initials[init], medials[med], finals[fin], syl);
				sb.append(syl);
				i += 2;
				continue;
			}
			
			/** NA: The above 2 situations do not apply but the current syllable has no final */
			else if (buf2.matches(matchIM)) {
				char syl = (char) ((init * 588 + med * 28) + 44032);
				//*DEBUG*/System.out.println("NA:     ");
				//*DEBUG*/System.out.printf("i=%d init=%c med=%c fin=%c SYL=[%s]\n", i, initials[init], medials[med], finals[fin], syl);
				sb.append(syl);
				i += 1;
				continue;
			}
			
			
			/** NO MATCH: it's a punctuation char or stray Hangul jamo */
			//*DEBUG*/System.out.printf("NOMATCH:%s\n", buf4);
			sb.append(buf4.charAt(0));
		}
		
		// At this point, there are no more Hangul syllables to process. Add the remaining part of the input
		sb.append(input.substring(i));
		return sb.toString();
	}

	public static String convertFromHangulBlocks(String input) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < input.length(); i++) {
			char s = input.charAt(i);
			int sIndex = (int) s - 44032; // Syllable index
			int iIndex = sIndex / 588; // Initial consonant index
			int mIndex = (sIndex % 588) / 28; // Medial vowel index
			int fIndex = sIndex % 28; // Final consonant index
			boolean isHangulSyllable = (s >= 0xAC00 && s <= 0xD7A3);
			//*DEBUG*/System.out.printf("s=%c sVal=%d i=%d m=%d f=%d\n", s, sIndex, iIndex, mIndex, fIndex);

			if (isHangulSyllable) {
				sb.append(initials[iIndex]);
				sb.append(medials[mIndex]);
				if (finals[fIndex] != ' ') sb.append(finals[fIndex]);
				//*DEBUG*/System.out.printf("JUST APPENDED: [%c, %c, %c]\n", initials[iIndex], medials[mIndex], finals[fIndex]);
			} else {
				sb.append(s);
				//*DEBUG*/System.out.printf("JUST APPENDED: [%c]\n", s);
			}
		}

		return sb.toString();
	}
	
}
