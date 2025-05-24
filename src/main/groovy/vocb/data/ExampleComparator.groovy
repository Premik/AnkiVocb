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
	
	/**
	 * How much is this example similar to the provided example (  list of words and words with its variants). 
	 * Example covering the biggest number of words in the give sentence. Shorter examples preferable.
	 */
	public int similarityScoreOf(Collection<String> wordsB, Collection<String> wordVariantsB=[]) {
		int exact = words.intersect(wordsB).size()		
		int variants = wordVariants.intersect(wordVariantsB?:wordsB).size()
		return exact*50 + variants*20 - wordsB.size()
	}
	
	public int similarityScoreOf(ExampleComparator compB) {
		similarityScoreOf(compB.words, compB.wordVariants)
	}
	
	public int similarityScoreOf(Example eB) {
		similarityScoreOf(of(eB))
	}
	
	
	
	@Memoized
	public static ExampleComparator of(String sentence) {		
		assert sentence
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

	List<ExampleComparator> bestExamplesFrom(Stream<ExampleComparator> bs) {
		int max=Integer.MIN_VALUE
		List<ExampleComparator> best = []
		bs.forEach { ExampleComparator b->
			int score = similarityScoreOf(b)
			if (score > max) {
				max = score
				best.clear()
			}
			if (score == max) {
				best.add(b)
			}
		}
		return best
	}
	
	List<ExampleComparator> bestExamplesFrom(Iterable<ExampleComparator> bs) {
		bestExamplesFrom(bs.stream())
	}

}