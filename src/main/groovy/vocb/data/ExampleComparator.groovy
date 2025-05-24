package vocb.data

import java.util.stream.Stream

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Memoized
import vocb.corp.WordNormalizer



@CompileStatic
@EqualsAndHashCode(includes=["words"])
public class ExampleComparator {

	public WordNormalizer getWn() {
		WordNormalizer.instance
	}

	Example example
	Collection<String> words = []


	@Lazy
	Collection<String> wordVariants = wordPairs.collectMany {
		wn.wordVariantsWithBrackets(it)
	} as LinkedHashSet

	public Collection<String> getWordPairs() {
		Stream.concat(
				words.stream(),
				wn.tuples(words.stream().map {wn.stripBracketsOut(it)}))
				.toList() as LinkedHashSet
	}
	
	
	Collection<String> wordsMatchingWithoutBrackets(Collection<String> wordsWithoutBrackets) {
		words.findAll { String w->
			wordsWithoutBrackets.contains(wn.stripBracketsOut(w))			
		}
	}
	
	Collection<String> wordsMatchingVariant(Collection<String> variants) {
		words.findAll { String w->
			List<String> common = wn.pairVariants(w).intersect(variants)
			return common.size() > 0
		}
	}
	
	String getSentence() {
		if (example) return example.firstTerm
		words.join(" ").capitalize() + "."
	}

	void setSentence(String s) {
		example = null
		s= s?.uncapitalize()
		words = WordNormalizer.instance.tokens(s, false).toList() as LinkedHashSet
	}

	void setExample(Example e) {		
		sentence = e?.firstTerm
		example= e
	}

	Collection<String> getWordsWithoutBrackets() {
		words.collect{WordNormalizer.instance.stripBracketsOut(it)}
	}

	/*Collection<String> getWordsVariantWithPairs() {
	 }*/


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
		double max=Double.NEGATIVE_INFINITY
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