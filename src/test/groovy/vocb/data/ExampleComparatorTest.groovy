package vocb.data

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Newify(value=[Example, Term])
@Execution(ExecutionMode.SAME_THREAD)
class ExampleComparatorTest {

	@BeforeAll
	static void init() {
		ExampleComparatorMatch.preferedWords = ["hello", "world", "today"]
	}

	ExampleComparator c1 = ExampleComparator.of("Hello world.")
	ExampleComparator c2 = ExampleComparator.of("Hello good old worlds.")

	Example e1 = Example().tap {
		terms.add(Term("Hello world example"))
	}
	
	
	
	
	
	public Example example(String en) {
		Example().tap {
			terms.add(Term(en))
		}
	}
	

	ExampleComparator c3 = ExampleComparator.of(e1)
	ExampleComparator c3Uncached = new ExampleComparator(sentence:e1.firstTerm)

	ExampleComparator c4 = ExampleComparator.of("Hello good old worlds")




	@Test
	void testREls() {
		assert c3.example == e1
		assert c1.example == null
	}

	@Test
	void cornerScores() {

		assert c1.words.containsAll("hello", "world")
		assert c1.similarityScoreOf("") ==0
		assert c1.similarityScoreOf("non this word") <=0
		assert c1.similarityScoreOf("hello") >0
		//assert c1.similarityScoreOf("Hello2") == c1.similarityScoreOf("world2")
		assert c3.similarityScoreOf("hello world example") == c3Uncached.similarityScoreOf("hello world example")
	}

	@Test
	void testEq() {
		assert !(c3Uncached === c3)
		assert  c3Uncached == c3
	}

	@Test
	void testOrder() {
		assert c1.bestFromComparators([c3, c3Uncached]).size() ==2
		assert c1.bestFromComparators([c1, c2])[0].a == c1
		assert c1.bestFromComparators([c1, c2])[0].b == c1
	}

	@Test
	void printAnsi() {

		println c2.bestFromComparators([c1, c3])[0].toAnsiString()
	}

	@Test
	void preferedWordsScoreTest() {
		ExampleComparatorMatch m =new ExampleComparatorMatch()
		["notPress","hello", "world"].each {println m.preferedWordsScore([it])}
		assert m.preferedWordsScore(["notPres"]) <=0
		assert m.preferedWordsScore(["hello"]) >0
		assert m.preferedWordsScore(["world"]) >0
		assert m.preferedWordsScore(["hello"]) > m.preferedWordsScore(["world"])
		
	}

	@Test
	void bracketVariants() {
		ExampleComparator word = new ExampleComparator(words:["That's (that is)"])
		assert word.wordVariants.containsAll("that is", "that's")
		assert word.wordVariants.containsAll("that", "is")

		ExampleComparator ex = new ExampleComparator(words:["That's", "that is"])
		assert ex.wordVariants.containsAll("that is", "that's")
		assert ex.wordVariants.containsAll("that", "is")

		ExampleComparatorMatch m =ex.similarityCompareTo(word)
		assert m.commonWords as Set == ["That's"] as Set
		assert m.commonWordVariants.containsAll("that is", "that's")
		assert m.commonWordVariants.containsAll("That is", "That's")
		assert m.commonWordVariants.containsAll("that", "is")
	}
	
	@Test
	void matchIll() {
		ExampleComparator ex =ExampleComparator.of(example("I'll always remember that moment."))
		assert ex.words.contains("I'll")
		ExampleComparator wr = new ExampleComparator(words:["I'll (I will)"])
		assert wr.wordsWithoutBrackets == ["I'll"]
		ExampleComparatorMatch m = wr.similarityCompareTo(ex)
		assert m.commonWords.contains("I'll")
	}
	
	@Test
	void matchIts() {
		ExampleComparator ex =ExampleComparator.of(example("Then it's true."))
		assert ex.words.contains("it's")
		ExampleComparator wr = new ExampleComparator(words:["it's (it is, it has)"])
		assert wr.wordsWithoutBrackets == ["it's"]
		ExampleComparatorMatch m = wr.similarityCompareTo(ex)
		assert m.commonWords.contains("it's")
	}
	
	@Test
	void matchIve() {
		ExampleComparator ex =ExampleComparator.of(example("I've been worried since yesterday."))
		assert ex.words.contains("I've")
		assert ex.words.contains("i've")
		ExampleComparator wr = new ExampleComparator(words:["I've (I have)"])
		assert wr.wordsWithoutBrackets == ["I've"]
		ExampleComparatorMatch m = wr.similarityCompareTo(ex)
		assert m.commonWords.contains("I've")
		
	}

	@Test
	void wordsMatchings() {
		Set<String> noBrackets = ["That's", "I'm"] as Set
		Set<String> brackets = [
			"That's (that is)",
			"I'm (I am)"
		] as Set
		ExampleComparator c = new ExampleComparator(words:brackets)
		assert c.wordsMatchingWithoutBrackets(noBrackets) as Set == brackets
		Set<String> inBrackets = ["that is", "I am"] as Set
		assert c.wordsMatchingVariant(inBrackets) as Set == brackets
	}

	@Test
	@Disabled
	void complexVariants() {
		List<Example> examples = [
			//"That is it.",
			"That's very funny",
			//"What fun it is to ride"
		].collect {String s->Example().tap{terms.add(Term(s))}}

		List<String> words = [
			"That's (that is)",
			"it's (it is, it has)",
			"fun",
			"to"
		]
		List<ExampleComparatorMatch> ms = new ExampleComparator(words:words).bestFromExamples(examples.stream())
		def p = { ExampleComparatorMatch match->
			println "-"*100
			println "$match.a.sentence $match.a.wordVariants"
			println match.toAnsiString()
			println match.commonWords
			println match.commonWordVariants
			println "-"*100
		}
		assert ms
		ExampleComparatorMatch m = ms.first()
		p(m)
		p(m.inverted())
		assert m.commonWords.contains("That's")
		assert m.commonWordVariants.contains("that")
	}
}
