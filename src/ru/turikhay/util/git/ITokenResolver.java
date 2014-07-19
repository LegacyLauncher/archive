package ru.turikhay.util.git;

/**
 * @author Jakob Jenkov
 * @see <a href="http://tutorials.jenkov.com/java-howto/replace-strings-in-streams-arrays-files.html">Java: Replace Strings in Streams, Arrays, Files etc.</a>
 */
public interface ITokenResolver {
	public String resolveToken(String tokenName);
}
