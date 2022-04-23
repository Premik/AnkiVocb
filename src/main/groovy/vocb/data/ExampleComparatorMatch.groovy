package vocb.data

import java.util.stream.Stream

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Memoized
import vocb.corp.WordNormalizer



@CompileStatic
@EqualsAndHashCode(includes=["a", "b"])
public class ExampleComparatorMatch {
	
	ExampleComparator a
	ExampleComparator b
	
	@Lazy
	Set<String> commonWords = a.words.intersect(b.words) as LinkedHashSet
	
	@Lazy
	Set<String> commonWordVariants = a.wordVariants.intersect(b.wordVariants) as LinkedHashSet
	
	Set<String> getMissWords() { 
		(a.words + b.words) - commonWords
	}
	
	Set<String> getMissWordVariants() {
		(a.wordVariants + b.wordVariants) - commonWordVariants
	}
	
	@Lazy	
	public int similarityScore =commonWords.size()*50 + commonWordVariants.size()*20 - b.words.size()

	@Override
	public String toString() {
		return "Match($similarityScore, '$a.sentence' vs '$b.sentence')"
	}
	
	
	
	

}