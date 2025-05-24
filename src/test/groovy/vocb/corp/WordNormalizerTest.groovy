package vocb.corp

import java.util.stream.Collectors

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import vocb.corp.WordNormalizer.CaseHandling

class WordNormalizerTest {

	WordNormalizer wn = new WordNormalizer()

	@Test
	void tokensSimple() {
		List<String> t =  wn.tokens("123 aaa bbb fooBar a123 mm_word").collect(Collectors.toList())
		assert t == [
			"aaa",
			"bbb",
			"foobar",
			"a",
			"mm",
			"word"
		]
	}
	
	@Test
	void tokensCae() {
		wn.tokens("Hello World!", CaseHandling.OriginalPlusLower ).toList() == ["Hello hello World world"]
		
	}

	@Test
	void ampersand() {
		List<String> t = wn.tokens("That's 'cause you're young").collect(Collectors.toList())
		assert t == [
			"that's",
			"'cause",
			"you're",
			"young"
		]
	}


	@Test
	void stringSteam() {
		List<String> res = wn.tokens(["aaa bbb", "ccc ddd"].stream()).collect(Collectors.toList())
		assert res == ["aaa", "bbb", "ccc", "ddd"]
	}

	@Test
	void pairs() {
		String[] w = ["a", "b", "c", "d", "e"]

		assert wn.tuples(w.stream(),1).toList() == w
		assert wn.tuples(w.stream(), 2).toList() == ['a b', 'b c', 'c d', 'd e']
		assert wn.tuples(w.stream(), 3).toList() == ['a b c', 'b c d', 'c d e']

		//assert norm.pairs(w.stream(),1, 3).toList() == ['a', 'a b', 'b c', 'c d', 'd']
	}
	
	

	@Test
	void phraseFreqs() {
		List<String> w = ["a", "b", "a", "b", "e"]
		Map<String , Integer> f = wn.phraseFreqs(w, 1, 4)
		assert f["a"] == 2
		assert f["e"] == 1
		assert f["a b"] == 2
		assert f["b a b e"] == 1
		//println "${f}"
		//assert norm.pairs(w.stream(),1, 3).toList() == ['a', 'a b', 'b c', 'c d', 'd']
	}

	@Test
	void sentencesTriv() {
		assert  wn.sentences("Hello world.") == ["Hello world"]
		assert  wn.sentences("Hello world! Lazy dog?") == ["Hello world", "Lazy dog"]
	}

	@Test
	void sentencesSimpleMulti() {
		String[] sts = wn.sentences('''
			The itsy bitsy spider climbed up the waterspout.
			Down came the rain
			and washed the spider out.
			'''.stripIndent())
		assert sts[0] == "The itsy bitsy spider climbed up the waterspout"
		assert sts[1] == "Down came the rain and washed the spider out"
	}

	@Test
	void sentencesI() {
		assert wn.sentences('''You say I am sad.''').size() == 1
		assert wn.sentences('''
              The story I must tell
             ''').size() == 1
	}

	@Test
	void sentencesMultiNoDots() {
		String[] sts = wn.sentences('''
			Now the ground is white
			Go it while you're young,
			Take the girls tonight
			and sing this sleighing song;'''.stripIndent())
		sts.each {println "\n${it}"}
		assert sts[0] == "Now the ground is white"
		assert sts[1] == "Go it while you're young"
		assert sts.size() == 3
	}

	@Test
	void sentencesNorm() {
		assert wn.normalizeSentence("Hello world!") == "hello world"
		assert wn.normalizeSentence("hello world") == "hello world"
	}

	@Test
	void lemm() {
		LinkedHashSet exp = [
			'hello',
			'hellos',
			'helloed',
			'helloing',
			'Hello',
			'Hellos',
			'Helloed',
			'Helloing',
			'world',
			'worlds',
			'worlded',
			'worlding',
			'World',
			'Worlds',
			'Worlded',
			'Worlding',
		] as LinkedHashSet
		assert wn.uniqueueTokens("Hello world!", true) == exp
	}
	
	@Test
	void lemmSort() {
		assert wn.wordVariants("ed")
		assert wn.wordVariants("ing")
		assert wn.wordVariants("s")
	}

	@Test
	void variants() {
		assert wn.wordVariants("cats") as Set == "cat cats catsed catsing Cat Cats Catsed Catsing".split() as Set
		assert wn.wordVariants("cat") as Set == "cat cats cated cating Cat Cats Cated Cating".split() as Set
		assert wn.wordVariants("Cat") as Set ==  wn.wordVariants("cat") as Set
		assert wn.wordVariants("look") as Set ==  "look looks looked looking Look Looks Looked Looking".split() as Set
		//assert norm.wordVariants("antiquities") as Set == "cat cats".split() as Set
	}
	
	@Test
	void variantsCornet() {
		assert !wn.wordVariantUnsorted("I").any{it.contains("[")}
		
	}
	
	

	@Test
	void stripBrkSimple() {
		assert wn.stripBracketsOut ("Hello (world)") == "Hello"
		assert wn.stripBracketsOut ("Hello ( world ) there.") == "Hello there."
		assert wn.stripBracketsOut ("Hello world.") == "Hello world."
	}
	
	@Test
	void splitBrk() {		
		assert wn.splitBrackets ("Hello (world)") == ["Hello", "world"] as Tuple2<String, String>
		assert wn.splitBrackets ("Hello ( world ) there.") == ["Hello there.", "world"] as Tuple2<String, String>
		assert wn.splitBrackets ("Hello world.") == ["Hello world.", ""] as Tuple2<String, String>
	}
	
	@Test
	void expandBrk() {
		assert wn.expandBrackets (["I've (I have)", "foo"]) == ["I've", "I have", "foo"] 
		
	}
	
	
	
	@Test
	@Disabled
	void stripMultiple() {
		wn.stripBracketsOut ("(The) Hello (world)") == "Hello"		
	}
	
	@Test
	void pairTokens() {
		assert wn.tokensWithPairs("Hello").toList() == ["Hello"]		
		List<String> tp = wn.tokensWithPairs("Hello big world").toList()
		assert tp == ['Hello big', 'big Hello', 'Hello', 'big world', 'world big', 'big', 'world']
	}
	
		
		
	@Test
	void sortVariantsBasic() {
		assert wn.sortWordVariants(["Hello", "world"]) == ["world", "Hello"] 
		assert wn.sortWordVariants(["abc", "def"]) == ["abc", "def"]
		assert wn.sortWordVariants(["def", "abc"]) == ["def", "abc"]
		assert wn.sortWordVariants(["abcd", "efg"]) == ["efg", "abcd"]
		assert wn.sortWordVariants(["abcd", "efg", "hij"]) == ["efg", "hij", "abcd"]
		assert wn.sortWordVariants(["abcd", "hij", "efg"]) == ["hij", "efg", "abcd"]
	}
	
	@Test
	void variantBracketsSimple() {
		assert wn.wordVariantsWithBrackets("cats").containsAll("cat", "cats")		
		List<String> catDog = wn.wordVariantsWithBrackets("cat (dog)")
		assert !catDog.any { it.contains("[") }
		assert catDog.containsAll("cat", "cats", "dog", "dogs")
		assert catDog.indexOf("cat") < catDog.indexOf("cats")
		assert catDog.indexOf("dog") < catDog.indexOf("dogs")
		assert catDog.indexOf("cat") < catDog.indexOf("dog")		
	}
	
	@Test
	void variantBracketsPairs() {		
		List<String> iAm = wn.wordVariantsWithBrackets("I'm (I am)")
		assert !iAm.any { it.contains("[") }
		assert iAm.containsAll("I", "am", "I'm", "i")
		assert iAm.indexOf("I'm") < iAm.indexOf("I")
		assert iAm.indexOf("I") < iAm.indexOf("am")	
	}
	
	@Test
	void pairVariantsTest() {
		assert wn.pairVariants("Hello", "world").containsAll("hello", "world", "hello world")
		assert wn.pairVariants("Hello").containsAll("hello", "Hello")
		assert wn.pairVariants("I'm (I am)").containsAll("I'm", "I am", "I", "am")
	}
	
}
