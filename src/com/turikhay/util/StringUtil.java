package com.turikhay.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Random;

import com.turikhay.exceptions.ParseException;

public class StringUtil {
	private static String addQuotes(String a, char quote) {
		if (a == null)
			return null;
		if (a.length() == 0)
			return "";

		return quote + a.replaceAll("\\" + quote, "\\\\" + quote) + quote;
	}

	public static String addQuotes(String a) {
		return addQuotes(a, '"');
	}

	public static String addSlashes(String str, EscapeGroup group) {
		if (str == null)
			return "";
		StringBuffer s = new StringBuffer(str);
		for (int i = 0; i < s.length(); i++) {
			char curChar = s.charAt(i);

			for (char c : group.getChars())
				if (curChar == c)
					s.insert(i++, '\\');
		}
		return s.toString();
	}

	public static String[] addSlashes(String[] str, EscapeGroup group) {
		if (str == null)
			return null;

		int len = str.length;
		String[] ret = new String[len];

		for (int i = 0; i < len; i++)
			ret[i] = addSlashes(str[i], group);

		return ret;
	}

	public static String iconv(String inChar, String outChar, String str) {
		Charset in = Charset.forName(inChar), out = Charset.forName(outChar);
		CharsetDecoder decoder = in.newDecoder();
		CharsetEncoder encoder = out.newEncoder();

		try {
			// Convert a string to ISO-LATIN-1 bytes in a ByteBuffer
			// The new ByteBuffer is ready to be read.
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(str));

			// Convert ISO-LATIN-1 bytes in a ByteBuffer to a character
			// ByteBuffer and then to a string.
			// The new ByteBuffer is ready to be read.
			CharBuffer cbuf = decoder.decode(bbuf);

			return cbuf.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean parseBoolean(String b) throws ParseException {
		if (b == null)
			throw new ParseException("String cannot be NULL!");

		if (b.equalsIgnoreCase("true"))
			return true;
		if (b.equalsIgnoreCase("false"))
			return false;

		throw new ParseException("Cannot parse value (" + b + ")!");
	}

	public static int countLines(String str) {
		if (str == null || str.length() == 0)
			return 0;

		int lines = 1;
		int len = str.length();
		for (int pos = 0; pos < len; pos++) {
			char c = str.charAt(pos);
			if (c == '\r') {
				lines++;
				if (pos + 1 < len && str.charAt(pos + 1) == '\n')
					pos++;
			} else if (c == '\n') {
				lines++;
			}
		}

		return lines;
	}

	public static char lastChar(String str) {
		if (str == null)
			throw new NullPointerException();
		int len = str.length();

		if (len == 0)
			return '\u0000';

		if (len == 1)
			return str.charAt(0);

		return str.charAt(len - 1);
	}

	public static String randomizeWord(String str, boolean softly) {
		if (str == null)
			return null;

		int len = str.length();

		if (len < 4)
			return str;

		boolean[] reversedFlag = new boolean[len];

		if (softly)
			// Do not reverse first letter
			reversedFlag[0] = true;

		// Flag indicating if the last letter is chosen for "soft" random
		boolean chosenLastLetter = !softly;

		char[] chars = str.toCharArray();

		for (int i = len - 1; i > -1; i--) {
			char curChar = chars[i];
			int charType = Character.getType(curChar);
			boolean canBeReversed = charType == Character.UPPERCASE_LETTER
					|| charType == Character.LOWERCASE_LETTER;

			reversedFlag[i] |= !canBeReversed;

			if (canBeReversed && !chosenLastLetter) {
				reversedFlag[i] = true;
				chosenLastLetter = true;
			}
		}

		for (int i = 0; i < len; i++) {
			if (reversedFlag[i])
				continue;

			int newPos = i, tries = 0;

			while (tries < 3) {
				tries++;

				newPos = new Random().nextInt(len);

				if (reversedFlag[newPos])
					continue;

				tries = 10;
				break;
			}

			if (tries != 10)
				continue;

			char curChar = chars[i], replaceChar = chars[newPos];

			chars[i] = replaceChar;
			chars[newPos] = curChar;

			reversedFlag[i] = true;
			reversedFlag[newPos] = true;
		}

		return new String(chars);
	}

	public static String randomizeWord(String str) {
		return randomizeWord(str, true);
	}

	public static String randomize(String str, boolean softly) {
		if (str == null)
			return null;

		if (str.isEmpty())
			return str;

		String[] lines = str.split("\n");
		StringBuilder lineBuilder = new StringBuilder();

		boolean isFirstLine = true;

		for (int l = 0; l < lines.length; l++) {
			String line = lines[l];

			String[] words = line.split(" ");
			StringBuilder wordBuilder = new StringBuilder();

			boolean isFirstWord = true;

			for (int w = 0; w < words.length; w++) {

				if (isFirstWord)
					isFirstWord = false;
				else
					wordBuilder.append(' ');

				wordBuilder.append(randomizeWord(words[w]));
			}

			if (isFirstLine)
				isFirstLine = false;
			else
				lineBuilder.append('\n');

			lineBuilder.append(wordBuilder);
		}

		return lineBuilder.toString();
	}

	public static String randomize(String str) {
		return randomize(str, true);
	}

	public enum EscapeGroup {
		COMMAND('\'', '"', ' '), REGEXP(COMMAND, '/', '\\', '?', '*', '+', '[',
				']', ':', '{', '}', '(', ')');

		private final char[] chars;

		private EscapeGroup(char... symbols) {
			this.chars = symbols;
		}

		private EscapeGroup(EscapeGroup extend, char... symbols) {
			int len = extend.chars.length + symbols.length;
			this.chars = new char[len];

			int x = 0;
			for (; x < extend.chars.length; x++)
				this.chars[x] = extend.chars[x];

			System.arraycopy(symbols, 0, this.chars, x, symbols.length);

			// for (int i = 0; i < symbols.length; i++)
			// this.chars[i + x] = symbols[i];
		}

		public char[] getChars() {
			return chars;
		}
	}
}
