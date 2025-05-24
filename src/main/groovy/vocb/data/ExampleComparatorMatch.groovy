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

	public static LinkedHashSet<String> preferedWords= []
	
	public WordNormalizer getWn() {
		WordNormalizer.instance
	}


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
	public double similarityScore = {
		Number pairs = commonWords.count {it.contains(" ") || it.contains("'")} //Composed/pairs higher
		double longWordMatch = (commonWords.sum {it.length()}?:0) as double //Long exact match word is better
		double exactMatches = commonWords.size()*500 //Exact word matches
		double variants = commonWordVariants.size()
		double sentenceLen = b.wordsWithoutBrackets.size() //Prefer shorter sentences
		double preferedList = commonWords.intersect(preferedWords).size()
		double preferedListVars = commonWordVariants.intersect(preferedWords).size()
		return pairs*1000 + longWordMatch*3 + exactMatches*500 + variants*50 - sentenceLen + preferedList*5 + preferedListVars*2
	}()

	public String sentenceToAnsiString(String defaultColor=RED, boolean printMiss=true) {
		if (!missWords) {
			//Exact match
			return color(a.sentence, BOLD)
		}
		String s= a.words.withIndex().collect { String wa, int i->
			String col = RED
			String waNoBrackets = wn.stripBracketsOut(wa)
			if (commonWords.contains(wa) || commonWords.contains(waNoBrackets) ) {
				col = BOLD
			} else {
				if (commonWordVariants.contains(wa) || commonWordVariants.contains(waNoBrackets)) {
					col = GREEN
				} else {
					if (!printMiss) return null
				}
			}
			if (i==0) wa = wa.capitalize()
			return color(wa, col)
		}.findAll().join(" ") + "."
	}

	Collection<String> matchesWordlist(Collection<String> wordList) {
		assert wordList

		Collection<String> remove = commonWords.intersect(wordList) 	//Exact matches first
		if (remove) return remove
		remove = a.wordsMatchingWithoutBrackets(commonWords).intersect(wordList) //Exact match without brackets
		if (remove) return remove
		return commonWordVariants.intersect(wordList) //All variants last
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