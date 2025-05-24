package vocb.data

import java.util.stream.Stream

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Memoized
import vocb.corp.WordNormalizer



@CompileStatic
@EqualsAndHashCode(includes=["sentence"])
public class ExampleComparator {
	
		
	String sentence
	Example example
	
	@Lazy
	Set<String> words = WordNormalizer.instance.uniqueueTokens(sentence)
	
	@Lazy
	Set<String> wordVariants = WordNormalizer.instance.uniqueueTokens(sentence, true)
	
	void setSentence(String s) {		
		example = null
		sentence = s
		clearCache()		
	}
	
	void setExample(Example e) {		
		example= e
		sentence = e?.firstTerm
		clearCache()
	}
	
	void clearCache() {
		this.@$words = null
		this.@$wordVariants = null
	}
	
	
	public ExampleComparatorMatch similarityCompareTo(ExampleComparator b) {		
		new ExampleComparatorMatch(a:this, b:b)
	}
	
	
	public int similarityScoreOf(ExampleComparator compB) {
		similarityCompareTo(compB).similarityScore
	}
	
	public int similarityScoreOf(Example eB) {
		similarityScoreOf(of(eB))
	}
	
	public int similarityScoreOf(String eB) {
		similarityScoreOf(of(eB))
	}
	
	
	
	@Memoized
	public static ExampleComparator of(String sentence) {		
		assert sentence!= null
		new ExampleComparator(sentence: sentence)
	}
	
	@Memoized
	public static ExampleComparator of(Example ex) {
		new ExampleComparator(example: ex)
	}

	@Override
	public String toString() {		
		return "ExampleComparator(${sentence.take(100)})";
	}

	List<ExampleComparatorMatch> bestExamplesFrom(Stream<ExampleComparator> bs) {
		int max=Integer.MIN_VALUE
		List<ExampleComparatorMatch> best = []
		bs.forEach { ExampleComparator b->
			ExampleComparatorMatch m = similarityCompareTo(b)			
			if (m.similarityScore > max) {
				max = m.similarityScore
				best.clear()
			}
			if (m.similarityScore == max) {
				best.add(m)
			}
		}
		return best
	}
	
	List<ExampleComparatorMatch> bestExamplesFrom(Iterable<ExampleComparator> bs) {
		bestExamplesFrom(bs.stream())
	}

}