package vocb.data

import static vocb.Ansi.*

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import vocb.Helper
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
		/*if (a.sentence.contains("resource") && b.sentence.contains("resource") ) {
			println "break"
		}*/

		Number pairs = commonWordVariants.count {it.contains(" ")} //Composed/pairs higher
		double longWordMatch = (commonWordVariants.sum {it.length()}?:0) as double //Long exact match word is better
		double exactMatches = commonWords.size() //Exact word matches
		double variants = commonWordVariants.size()
		double sentenceLen = b.wordsWithoutBrackets.size() //Prefer shorter sentences
		double prefered = preferedWordsScore(commonWords.intersect(preferedWords))
		double preferedVars = preferedWordsScore(commonWordVariants.intersect(preferedWords))

		double d= pairs*500 + longWordMatch*5 + exactMatches*400 + variants*100 - sentenceLen + prefered*10 + preferedVars*5		
		return d
	}()

	public static Closure<String> defaultColors = {ExampleComparatorMatch self, String word, String defaultColor ->
		if (!word) return null
		//if (defaultColor == RED) return null //Skip misses
		if (self.a.words.size() > 100 && defaultColor == RED) return null //Skip misses only for long sentences
		color(word, defaultColor)
	}

	public static Closure<String> invertedFgBgColors = {ExampleComparatorMatch self, String word, String defaultColor ->
		if (!word) return null
		//if (defaultColor == RED) return null //Skip misses
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
				} //RED
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
		String inv = Helper.padRightAnsi(inverted().sentenceToAnsiString(colors),50).take(500)
		String tx= sentenceToAnsiString(colors).take(500)
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