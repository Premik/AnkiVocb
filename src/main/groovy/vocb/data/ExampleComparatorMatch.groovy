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

	double preferedWordsScore(Collection<String> preferedList) {
		if (!preferedWords) return 0
		if (!preferedList) return 0
		preferedList.sum {String cw->
			int idx = preferedWords.findIndexOf { cw.equalsIgnoreCase(it) }
			if(idx<0) return 0
			return 1-(idx+1)/(preferedWords.size())			
		} as double
	}

	@Lazy
	public double similarityScore = {

		Number pairs = commonWordVariants.count {it.contains(" ")} //Composed/pairs higher
		double longWordMatch = (commonWordVariants.sum {it.length()}?:0) as double //Long exact match word is better
		double exactMatches = commonWords.size()*500 //Exact word matches
		double variants = commonWordVariants.size()
		double sentenceLen = b.wordsWithoutBrackets.size() //Prefer shorter sentences		
		double prefered = preferedWordsScore(commonWords.intersect(preferedWords))
		double preferedVars = preferedWordsScore(commonWordVariants.intersect(preferedWords))

		return pairs*500 + longWordMatch*5 + exactMatches*100 + variants*50 - sentenceLen + prefered*40 + preferedVars*30
	}()
	
	public static Closure<String> defaultColors = {ExampleComparatorMatch self, String word, String defaultColor ->
		color(word, defaultColor)
	}
	
	public static Closure<String> invertedFgBgColors = {ExampleComparatorMatch self, String word, String defaultColor ->
		color(color(word, defaultColor), REVERSE_VIDEO)
	}

	public String sentenceToAnsiString(Closure<String> colors=defaultColors) {
		if (!missWords) {
			//Exact match
			return colors(this, a.sentence, BOLD)
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
					//if (!printMiss) return null
				}
			}
			if (i==0) wa = wa.capitalize()
			return colors(this, wa, col)
		}.findAll().join(" ") + "."
	}

	Collection<String> matchesWordlist(Collection<String> wordList) {
		assert wordList

		Collection<String> remove = commonWords.intersect(wordList) 	//Exact matches first		
		Collection<String> removeBrackets = a.wordsMatchingWithoutBrackets(commonWords).intersect(wordList) //Exact match without brackets
		if (remove ||removeBrackets) return remove + removeBrackets
		return commonWordVariants.intersect(wordList) //All variants last
	}

	public String toAnsiString(Closure<String> colors=defaultColors) {
		String inv = inverted().sentenceToAnsiString(colors).take(1000).padRight(120)
		String tx= sentenceToAnsiString(colors).take(1000)
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