package vocb.data

import org.junit.jupiter.api.Test

@Newify(value=[Example, Term])
class ExampleComparatorTest {
	
	ExampleComparator c1 = ExampleComparator.of("Hello world.")
	ExampleComparator c2 = ExampleComparator.of("Hello good old world.")
	
	Example e1 = Example().tap {
		terms.add(Term("Hello world example"))
	}
	
	ExampleComparator c3 = ExampleComparator.of(e1)
	ExampleComparator c3Uncached = new ExampleComparator(sentence:c3.sentence)
	
	@Test
	void testREls() {
		assert c3.example == e1
		assert c1.example == null
	}
	
	@Test
	void cornerScores() {
		
		assert c1.words == ["hello", "world"] as Set 
		assert c1.similarityScoreOf("") ==0
		assert c1.similarityScoreOf("non this word") <=0
		assert c1.similarityScoreOf("hello") >0
		assert c1.similarityScoreOf("hello") == c1.similarityScoreOf("world")
		assert c3.similarityScoreOf("hello world example") == c3Uncached.similarityScoreOf("hello world example") 		
	}
	
	@Test
	void testEq() {
		assert !(c3Uncached === c3)
		assert  c3Uncached == c3
	}
	
	@Test
	void testOrder() {
		assert c1.bestExamplesFrom([c3, c3Uncached]).size() == 2
		assert c1.bestExamplesFrom([c1,c2])[0].a == c1
		assert c1.bestExamplesFrom([c1,c2])[0].b == c1
	}
	
	
}
