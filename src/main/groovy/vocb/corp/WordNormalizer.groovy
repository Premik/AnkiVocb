package vocb.corp

import java.util.function.Function
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream


public class WordNormalizer {

	int minLenght = 2
	int maxLenght = 15

	//Treat non-letter or non-digit as a space. Except underscore and hyphen.
	@Lazy Pattern spacesPattern = {~ /[^\p{L}]+/ }()
	@Lazy Pattern niceWordPatter = {~ /^[\p{L}]+/ }() //No digits in words etc

	public Set<String> uniqueueTokens(CharSequence input) {
		tokens(input).collect(Collectors.toSet())
	}

	public Stream<String> tokens(CharSequence input) {
		spacesPattern.splitAsStream(input)
				//.filter {String s -> (s=~ niceWordPatter).size() > 0 }
				.filter {String s -> s.length() >= minLenght && s.length() <=maxLenght}
				.map {String s ->s.toLowerCase()}
	}

	public Stream<String> tokens(Stream<CharSequence> listOfString) {
		return listOfString.flatMap(this.&tokens)
	}



	static void main(String... args) {
		WordNormalizer n = new WordNormalizer()
		String supa = getClass().getResource('/Supaplex.txt').text
		println n.uniqueueTokens(supa)

	}


}
