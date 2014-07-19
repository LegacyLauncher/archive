package ru.turikhay.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Random;

import javax.swing.plaf.basic.BasicHTML;

import ru.turikhay.exceptions.ParseException;

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

		StringBuilder s = new StringBuilder(str);
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

	public static boolean isHTML(char[] s) {
		if (s != null) {
			if ((s.length >= 6) && (s[0] == '<') && (s[5] == '>')) {
				String tag = new String(s, 1, 4);
				return tag.equalsIgnoreCase(BasicHTML.propertyKey);
			}
		}
		return false;
	}

	public static String wrap(char[] s, int maxChars, boolean rudeBreaking, boolean detectHTML) {
		if(s == null)
			throw new NullPointerException("sequence");
		if(maxChars < 1)
			throw new IllegalArgumentException("maxChars < 1");

		detectHTML = detectHTML && isHTML(s);

		String lineBreak = detectHTML? "<br />" : "\n";

		StringBuilder builder = new StringBuilder();

		int len = s.length, remaining = maxChars;
		boolean tagDetecting = false, ignoreCurrent = false;
		char current;

		for (int x = 0; x < len; x++) {
			current = s[x];

			if(current == '<' && detectHTML) {
				// Start of tag detected
				tagDetecting = true;
				ignoreCurrent = true;
			} else if(tagDetecting) {

				if(current == '>')
					tagDetecting = false;

				ignoreCurrent = true;
			}

			if(ignoreCurrent) {
				ignoreCurrent = false;

				builder.append(current);
				continue;
			}


			--remaining;

			if(s[x] == '\n' || (remaining < 1 && current == ' ')) {
				remaining = maxChars;
				builder.append(lineBreak);
				continue;
			} else if (lookForward(s, x, lineBreak)) {
				remaining = maxChars;
			}

			builder.append(current);

			if (remaining > 0)
				continue;
			if (!rudeBreaking)
				continue;

			remaining = maxChars;
			builder.append(lineBreak);
		}

		return builder.toString();
	}

	private static boolean lookForward(char[] c, int caret, CharSequence search) {
		if(c == null)
			throw new NullPointerException("char array");

		if(caret < 0)
			throw new IllegalArgumentException("caret < 0");

		if(caret >= c.length)
			return false;

		int length = search.length(), available = c.length - caret;

		if(length < available)
			return false;

		for(int i=0;i<length;i++)
			if(c[caret+i] != search.charAt(i))
				return false;

		return true;
	}

	public static String wrap(String s, int maxChars, boolean rudeBreaking, boolean detectHTML) {
		return wrap(s.toCharArray(), maxChars, rudeBreaking, detectHTML);
	}

	public static String wrap(char[] s, int maxChars, boolean rudeBreaking) {
		return wrap(s, maxChars, rudeBreaking, true);
	}

	public static String wrap(char[] s, int maxChars) {
		return wrap(s, maxChars, false);
	}

	public static String wrap(String s, int maxChars) {
		return wrap(s.toCharArray(), maxChars);
	}

	public static String cut(String string, int max) {
		if(string == null)
			return null;

		int len = string.length();
		if (len <= max)
			return string;

		String[] words = string.split(" ");
		String ret = "";
		int remaining = max + 1;

		for (int x = 0; x < words.length; x++) {
			String curword = words[x];
			int curlen = curword.length();

			if (curlen < remaining) {
				ret += " " + curword;
				remaining -= curlen + 1;

				continue;
			}

			if (x == 0)
				ret += " " + curword.substring(0, remaining - 1);
			break;
		}

		if (ret.length() == 0)
			return "";
		return ret.substring(1) + "...";
	}

	public enum EscapeGroup {
		DOUBLE_QUOTE('"'), COMMAND(DOUBLE_QUOTE, '\'', ' '),
		REGEXP(COMMAND, '/', '\\', '?', '*', '+', '[', ']', ':', '{', '}', '(', ')');

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
