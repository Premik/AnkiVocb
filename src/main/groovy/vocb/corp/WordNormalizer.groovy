package vocb.corp

import java.text.BreakIterator
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream

import org.apache.commons.collections4.queue.CircularFifoQueue

import groovy.transform.CompileStatic
import vocb.Helper


public class WordNormalizer {

	int minLenght = 2
	int maxLenght = 20

	//Treat non-letter or non-digit as a space. Except single quote
	@Lazy Pattern spacesPattern = ~ /[^\p{L}']+/
	@Lazy Pattern niceWordPatter = ~ /^[\p{L}]+/  //No digits in words etc
	@Lazy Pattern stripBrkPattern = ~ /\s*\([^)]+\)\s*/


	public Set<String> uniqueueTokens(CharSequence input, boolean doLemming=false) {
		if (!input) return [] as Set
		Stream<String> stream = tokens(input)
		if (doLemming) stream = lemming(stream)
		stream.collect(Collectors.toCollection( LinkedHashSet.&new ) )
	}

	public Stream<String> tokens(CharSequence input) {
		spacesPattern.splitAsStream(input)
				//.filter {String s -> (s=~ niceWordPatter).size() > 0 }
				.filter {String s -> s.length() >= minLenght && s.length() <=maxLenght}
				.map {String s ->s.toLowerCase()}
	}

	public Stream<String> lemming( Stream<String> inp) {
		inp.flatMap { String s->
			wordVariants(s).stream()
		}
	}

	public List<String> wordVariants(String s) {
		//if (s.endsWith('es')) return [s, s[0..-3]] //Remove 'es'			
		if (s.endsWith('s')) return [s, s[0..-2]] //Remove 's'
		return [s, "${s}s" as String] //Add 's'
	}
	
	public String swapPluralSingular(String s) { //Removes or adds 's'
		if (s.endsWith('s')) return s[0..-2]
		return "${s}s" as String //Add 's'
	}
	
	@CompileStatic
	public String swapCapitalFirstLetter(String s) {
		Character first = s[0] as Character
		if (Character.isUpperCase(first)) {
			first = Character.toLowerCase(first)
		} else {
			first = Character.toUpperCase(first)
		}
		return "$first${s[1..-1]}" as String
	}



	public String normalizeSentence(CharSequence sentence) {
		if (!sentence) return ""
		tokens(sentence).collect( Collectors.joining( " ") )
	}



	public List<String> sentences(CharSequence input) {
		int start
		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US).tap {
			text = input
			start = first()
		}

		List<String> ret=[]
		for (int end = iterator.next();end != BreakIterator.DONE;) {
			String s = input[start..end-1]
			ret.add(s.trim())
			start = end
			end = iterator.next()
		}
		ret
				.collectMany {  it.split(/[,;:]\s(=\s*)/) as List} //Split on non-full sentences
				.collectMany { it.split(/\s+(?=[\p{Lu}&&[^I]])/) as List } //Missing dots, but capital letter next (but ignore capital I)
				.collect {it.trim()}
				.collect {it.replaceAll(/[!?;.,"'":]$/, "") } //Remove sentence terminators
				.collect {it.trim()}
				.collect {it.replaceAll("[\n\r]+", " ")} //remove newlines
	}

	public Map<String, Set<String>> wordsInSentences(List<String> sentences) {
		Map<String, Set<String>> ret = [:].withDefault{[] as LinkedHashSet}
		sentences.each { String sen->
			tokens(sen).each { String word->
				ret[word].add(sen)
			}
		}
		return ret
	}


	public Map<String, Set<String>> wordsInSentences(CharSequence input) {
		wordsInSentences(sentences(input))
	}



	public Stream<String> tokens(Stream<CharSequence> listOfString) {
		return listOfString.flatMap(this.&tokens)
	}

	public Stream<String> pairs(Stream<CharSequence> listOfString, int sz=2) {
		//Side effect, don't run in  parallel, consumes the stream
		CircularFifoQueue<String> prevs= new CircularFifoQueue(sz)
		listOfString.map { CharSequence s->
			prevs.add(s)
			//println "B: ${prevs} ${prevs.getClass()} ${prevs.size()} ${prevs.maxSize()}"
			//(1..maxSz).inject([]) { ret, it-> ret += prevs.collate(it, 1, false)}
			prevs.collate(sz, 1, false)
		}
		//.peek { println "${it}"}
		.flatMap {it.stream()}
		.filter {List s -> s.size() >= sz}
		//.map {Collection<List<String>> s-> s.collect { it.join(' ')} }
		.map {List<String> s -> s.join(' ') }
	}

	public Map<String , Integer> phraseFreqs(Collection<CharSequence> words, int minSz=1, int maxSz=4) {
		Map<String , Integer> ret = [:].withDefault {0}
		for (int sz = minSz;sz<=maxSz;sz++) {
			for(String s in pairs(words.stream(), sz )) {
				ret[s]++
			}
		}
		return ret
	}

	public Map<String , Integer> phraseFreqs(CharSequence input, int minSz=1, int maxSz=4) {
		phraseFreqs (tokens( input).toList(), minSz, maxSz)
	}

	public Map<String , Integer> topPhrases(Map<String , Integer> phraseFreq, int cutOff=2) {
		phraseFreq
				.findAll {String w, Integer f-> f >=cutOff }
				.sort { -it.value}
	}

	public Set<String> commonWordOf(String sen, String sen2) {
		uniqueueTokens(sen, true).intersect(uniqueueTokens(sen2, true))
	}

	public String stripBracketsOut(String tx) {
		def (a,b,c) = Helper.splitByRex(tx, stripBrkPattern)
		if (a == null) return tx
		return "$a$c"
	}

	static void main(String... args) {
		new WordNormalizer().with {
			String supa = getClass().getResource('/Supaplex.txt').text
			topPhrases(phraseFreqs(supa, 2, 5)).each { w,i->
				println "${w.padRight(10)} $i"

			}
		}

	}


}
