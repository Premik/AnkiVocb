package vocb.data

import java.util.stream.Stream

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Memoized
import vocb.corp.WordNormalizer



@CompileStatic
@EqualsAndHashCode(includes=["words"])
public class ExampleComparator {
	
		
	
	Example example
	Collection<String> words = [] 
	
	@Lazy
	Collection<String> wordVariants = words.collectMany {WordNormalizer.instance.wordVariants(it, true)} as LinkedHashSet
	
	String getSentence() {
		words.join(" ").capitalize() + "."		
	}
	
	void setSentence(String s) {		
		example = null
		words = WordNormalizer.instance.uniqueueTokens(s)		
	}
	
	void setExample(Example e) {		
		sentence = e?.firstTerm
		example= e		
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
	
	List<ExampleComparatorMatch> bestFromComparators(Iterable<ExampleComparator> bs) {
		bestExamplesFrom(bs.stream())
	}
	
	List<ExampleComparatorMatch> bestFromExamples(Stream<Example> ex) {
		bestExamplesFrom(ex.map(ExampleComparator.&of))
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

}