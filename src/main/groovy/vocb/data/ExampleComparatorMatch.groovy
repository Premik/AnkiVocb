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
	Collection<String> commonWords = a.wordsWithoutBrackets.intersect(b.wordsWithoutBrackets) as LinkedHashSet
	
	@Lazy
	Collection<String> commonWordVariants = a.wordVariants.intersect(b.wordVariants) as LinkedHashSet
	
	Collection<String> getMissWords() {
		(a.wordsWithoutBrackets + b.wordsWithoutBrackets) - commonWords
	}
	
	Collection<String> getMissWordVariants() {
		(a.wordVariants + b.wordVariants) - commonWordVariants
	}
	
	@Lazy	
	public int similarityScore =commonWords.size()*1000 + commonWordVariants.size()*10 - b.wordsWithoutBrackets.size()

	public String sentenceToAnsiString(String defaultColor=RED, boolean printMiss=true) {
		if (!missWords) { //Exact match
			return color(a.sentence, BOLD)
		}
		String s= a.words.withIndex().collect { String wa, int i->			
			String col = RED
			if (commonWords.contains(wa)) {
				col = BOLD
			} else {
				if (commonWordVariants.contains(wa)) {
					col = GREEN
				} else {
					if (!printMiss) return null
				}
			}			
			if (i==0) wa = wa.capitalize()
			return color(wa, col)
		}.findAll().join(" ") + "."
		
	}
	
	public String toAnsiString() {
		String inv = inverted().sentenceToAnsiString(NORMAL).take(1000).padRight(120)
		String tx= sentenceToAnsiString().take(1000)				
		return "$inv ${color('<-',BLUE)} $tx ${color("", NORMAL)}"	
	}
	
	public ExampleComparatorMatch inverted() {
		new ExampleComparatorMatch(a:b,b:a)
	}
	
	@Override
	public String toString() {
		return "Match($similarityScore, '$a.sentence' vs '$b.sentence')"
	}
	
	
	
	

}