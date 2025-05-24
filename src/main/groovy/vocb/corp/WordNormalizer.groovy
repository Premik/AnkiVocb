package vocb.corp

import java.text.BreakIterator
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream

import org.apache.commons.collections4.queue.CircularFifoQueue
import org.apache.commons.lang3.NotImplementedException

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Field
import groovy.transform.Memoized
import vocb.Helper


//@Singleton(lazy=true, strict=false) //Doens't work in eclipse
@CompileStatic
public class WordNormalizer {

	public enum CaseHandling {
		Original,
		Lower,
		OriginalPlusLower
	}

	public static WordNormalizer instance = new WordNormalizer()

	int minLenght = 1
	int maxLenght = 20

	//Treat non-letter or non-digit as a space. Except single quote
	@Lazy Pattern spacesPattern = ~ /[^\p{L}']+/
	@Lazy Pattern niceWordPatter = ~ /^[\p{L}]+/  //No digits in words etc
	@Lazy Pattern stripBrkPattern = ~ /\s*\([^)]+\)\s*/
	@Lazy Pattern brkPattern = ~ /\s*[()]\s*/


	@CompileDynamic
	public Set<String> uniqueueTokens(CharSequence input, boolean doLemming=false) {
		if (!input) return [] as Set
		Stream<String> stream = tokens(input)
		if (doLemming) stream = lemming(stream)
		stream.collect(Collectors.toCollection( LinkedHashSet.&new ))
	}

	public Stream<String> tokens(CharSequence input, CaseHandling caseHadling=CaseHandling.Lower) {
		if (!input) return Stream.empty()
		Stream<String> ret =spacesPattern.splitAsStream(input)
				//.filter {String s -> (s=~ niceWordPatter).size() > 0 }
				.filter {String s -> s.length() >= minLenght && s.length() <=maxLenght}
		if (caseHadling == CaseHandling.Original) {
			return ret
		}
		if (caseHadling == CaseHandling.Lower) {
			return ret.map {String s ->s.toLowerCase()}
		}
		if (caseHadling == CaseHandling.OriginalPlusLower) {
			return ret.flatMap {String s ->
				[s, s.toLowerCase()].toUnique().stream()				
			}
		}
		throw new IllegalArgumentException("$caseHadling not supported")
	}


	public Stream<String> tokensWithPairs(CharSequence input) {
		//Hack, statful
		String last = ""
		Stream<String> strm = Stream.concat(spacesPattern.splitAsStream(input), Stream.of(null))
		strm
				.filter {String s ->
					s==null || (s.length() >= minLenght && s.length() <=maxLenght)
				}
				.flatMap { String w->
					if (w==null) {
						//EOS. Append the last(if any)
						if (last) return Stream.of(last)
						return Stream.empty()
					}
					if (!last) {
						last = w
						return Stream.empty()
					}
					return [
						[last, w].findAll().join(' '),
						[w, last].findAll().join(' '),
						last
					]
					.toUnique()
					.tap {last=w}.stream()
				}.map {it as String}
	}

	public Stream<String> lemming( Stream<String> inp, boolean preferOrigialWord=false) {
		inp.flatMap { String s->
			wordVariants(s).stream()
		}
	}

	public List<String> sortWordVariants(List<String> variants) {
		variants.toUnique()
				//.tap {println it}
				.sort { String a, String b->
					//Lower-cased first
					Character.isUpperCase(a[0] as Character) <=> Character.isUpperCase(b[0] as Character)?:
							a.length() <=> b.length() //Shorter first
				}
	}

	public List<String> wordVariantUnsorted(String s) {
		if (!s) return []
		String cap = swapCapitalFirstLetter(s)
		[
			s,
			swapPluralSingular(s),
			*ingVariants(s),
			*edVariants(s),
			swapPluralSingular(cap),
			*ingVariants(cap),
			*edVariants(cap),
			cap,
		]as List<String>
	}

	public List<String> wordVariants(String s, boolean preferOrigialWord=false) {
		List<String> variants = sortWordVariants(wordVariantUnsorted(s))
		if (preferOrigialWord) {
			//The exact word first,
			variants.push(s)
		}
		return variants
	}

	public String swapPluralSingular(String s) {
		if (s.length() < 2) return s // I -x> is
		if (s.endsWith("'s")) {
			return s[0..<-2] as String
		}
		if (s.endsWith('s')) {
			if (s.length() <3) return s // is -x> I
			return s[0..<-1] as String
		}
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
		if (s.length() ==1) return first
		return "$first${s[1..-1]}" as String
	}

	public List<String> edVariants(String s) {
		//Drop 'ed' and just 'd'
		if (s.length() < 3) return [s]
		if (s.endsWith('e')) return ["${s}d" as String]
		if (s.endsWith('ed')) {
			if (s.length() > 4) {
				return [
					s[0..<-2] as String,
					s[0..<-1] as String
				]
			} else {
				return [s[0..<-1] as String]
			}
		}
		return ["${s}ed" as String]
	}

	public List<String> ingVariants(String s) {
		if (s.length() < 3) return [s]
		if (s.endsWith('e')) return ["${s[0..<-1]}ing" as String]
		if (s.endsWith('ing')) {
			if (s.length() > 5) {
				return[
					s[0..<-3],
					"${s[0..<-3]}e" as String
				]
			} else {
				return ["${s[0..<-3]}e" as String]
			}
		}
		return ["${s}ing" as String]
	}

	@CompileDynamic
	@Memoized
	public List<String> wordVariantsWithBrackets(String s) {
		def (String a, String b) = splitBrackets(s)
		//if (!b) return wordVariants(s, true) //No brackets
		List<String> exact = wordVariantUnsorted(a) + wordVariantUnsorted(b)
		Closure<List<String>> pair = { String w->
			tokensWithPairs(w).flatMap {wordVariantUnsorted(it).stream()}.toList()
		}
		List<String> pairs = pair(a) + pair(b)
		return (exact + pairs).toUnique()
	}



	@CompileDynamic
	public String normalizeSentence(CharSequence sentence) {
		if (!sentence) return ""
		tokens(sentence).collect( Collectors.joining( " ") )
	}



	@CompileDynamic
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
				//.collectMany { it.split(/\s+(?=[\p{Lu}&&[^I]])/) as List}//Missing dots, but capital letter next (but ignore capital I)
				.collect {it.trim()}
				.collect {it.replaceAll(/[!?;.,"'":]$/, "") } //Remove sentence terminators
				.collect {it.trim()}
				.collect {String s->s.replaceAll("[\n\r]+", " ")} //remove newlines
	}
	
	public List<String> splitSentenceMissingDots(CharSequence input) {
		
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

	public Stream<String> tuples(Stream<String> listOfString, int sz=2) {
		//Side effect, don't run in  parallel, consumes the stream
		CircularFifoQueue<String> prevs= new CircularFifoQueue(sz)
		listOfString.map { CharSequence s->
			prevs.add(s as String)
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

	public List<String> pairVariants(String w, String w2="") {
		String pair = [w, w2].collect {stripBracketsOut(it)}.findAll().join(" ")
		[w, w2, pair].collectMany { wordVariantsWithBrackets(it) }
	}

	public Map<String , Integer> phraseFreqs(Collection<String> words, int minSz=1, int maxSz=4) {
		Map<String , Integer> ret = [:].withDefault {0}
		for (int sz = minSz;sz<=maxSz;sz++) {
			for(String s in tuples(words.stream(), sz )) {
				ret[s]++
			}
		}
		return ret
	}

	public Map<String , Integer> phraseFreqs(String input, int minSz=1, int maxSz=4) {
		phraseFreqs (tokens( input).toList() as List<String>, minSz, maxSz)
	}

	public Map<String , Integer> topPhrases(Map<String , Integer> phraseFreq, int cutOff=2) {
		phraseFreq
				.findAll {String w, Integer f-> f >=cutOff }
				.sort { -it.value}
	}

	public Set<String> commonWordOf(String sen, String sen2) {
		uniqueueTokens(sen, true).intersect(uniqueueTokens(sen2, true))
	}

	@CompileDynamic
	public String stripBracketsOut(String tx) {
		def (String a, String b) = splitBrackets(tx)
		return a
	}

	@CompileDynamic
	public Tuple2<String, String> splitBrackets(String tx) {
		def (String a, String b, String c) = Helper.splitByRex(tx, stripBrkPattern)
		if (a == null) return [tx, ""] as Tuple2<String, String>
		b = b.replaceAll(brkPattern, "")
		//return ["${a.trim()} ${c.trim()}" as String, b]
		return [[a, c].findAll().join(" "), b] as Tuple2<String, String>
	}
	
	public List<String> expandBrackets(Collection<String> words) {
		words.collectMany{ splitBrackets(it).toUnique().findAll()}		
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
