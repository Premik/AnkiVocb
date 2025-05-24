package vocb.data

import java.util.stream.Stream
import static vocb.Ansi.*
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

	public String sentenceToAnsiString(String defaultColor=RED) {
		if (!missWords) { //Exact match
			return color(a.words.join(" "), BOLD)
		}
		return a.words.collect { String wa->
			String col = RED
			if (commonWords.contains(wa)) {
				col = BOLD
			} else {
				if (commonWordVariants.contains(wa)) {
					col = GREEN
				}
			}
			return color(wa, col)
		}.join(" ")		
	}
	
	public String toAnsiString() {				
		return "${sentenceToAnsiString()} -> ${inverted().sentenceToAnsiString(NORMAL)}"	
	}
	
	public ExampleComparatorMatch inverted() {
		new ExampleComparatorMatch(a:b,b:a)
	}
	
	@Override
	public String toString() {
		return "Match($similarityScore, '$a.sentence' vs '$b.sentence')"
	}
	
	
	
	

}