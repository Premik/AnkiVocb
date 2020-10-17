package vocb.corp

import java.util.stream.Collectors

import org.junit.jupiter.api.Test

class WordNormalizerTest {

	WordNormalizer norm = new WordNormalizer()

	@Test
	void simple() {
		List<String> t =  norm.tokens("123 aaa bbb fooBar a123 mm_word").collect(Collectors.toList())
		assert t == ["aaa", "bbb", "foobar", "mm", "word"]
	}

	@Test
	void ampersand() {
		List<String> t = norm.tokens("That's 'cause you're young").collect(Collectors.toList())
		assert t == ["that's", "'cause", "you're", "young"]
	}


	@Test
	void stringSteam() {
		List<String> res = norm.tokens(["aaa bbb", "ccc ddd"].stream()).collect(Collectors.toList())
		assert res == ["aaa", "bbb", "ccc", "ddd"]
	}

	@Test
	void pairs() {
		String[] w = ["a", "b", "c", "d", "e"]


		assert norm.pairs(w.stream(),1).toList() == w
		assert norm.pairs(w.stream(), 2).toList() == ['a b', 'b c', 'c d', 'd e']
		assert norm.pairs(w.stream(), 3).toList() == ['a b c', 'b c d', 'c d e']

		//assert norm.pairs(w.stream(),1, 3).toList() == ['a', 'a b', 'b c', 'c d', 'd']
	}

	@Test
	void phraseFreqs() {
		List<String> w = ["a", "b", "a", "b", "e"]
		Map<String , Integer> f = norm.phraseFreqs(w, 1, 4)
		assert f["a"] == 2
		assert f["e"] == 1
		assert f["a b"] == 2
		assert f["b a b e"] == 1
		//println "${f}"
		//assert norm.pairs(w.stream(),1, 3).toList() == ['a', 'a b', 'b c', 'c d', 'd']
	}

	@Test
	void sentencesTriv() {
		assert  norm.sentences("Hello world.") == ["Hello world"]
		assert  norm.sentences("Hello world! Lazy dog?") == ["Hello world", "Lazy dog"]
	}

	@Test
	void sentencesSimpleMulti() {
		String[] sts = norm.sentences('''
			The itsy bitsy spider climbed up the waterspout.
			Down came the rain
			and washed the spider out.
			'''.stripIndent())
		assert sts[0] == "The itsy bitsy spider climbed up the waterspout"
		assert sts[1] == "Down came the rain and washed the spider out"
	}

	@Test
	void sentencesI() {
		assert norm.sentences('''You say I am sad.''').size() == 1
		assert norm.sentences('''
              The story I must tell
             ''').size() == 1
	}

	@Test
	void sentencesMultiNoDots() {
		String[] sts = norm.sentences('''
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
		assert norm.normalizeSentence("Hello world!") == "hello world"
		assert norm.normalizeSentence("hello world") == "hello world"
	}

	@Test
	void lemm() {
		assert norm.uniqueueTokens("Hello world!", true) == ["hello", "hellos", "world", "worlds"] as LinkedHashSet
	}
	
	@Test
	void variants() {
		assert norm.wordVariants("cats") as Set == "cat cats".split() as Set
		assert norm.wordVariants("cat") as Set == "cat cats".split() as Set
		//assert norm.wordVariants("antiquities") as Set == "cat cats".split() as Set
	}

	@Test
	void stripBrk() {
		norm.stripBracketsOut ("Hello (world)") == "Hello"
		norm.stripBracketsOut ("Hello ( world ) there.") == "Hello there."
		norm.stripBracketsOut ("Hello world.") == "Hello world."
	}
}
