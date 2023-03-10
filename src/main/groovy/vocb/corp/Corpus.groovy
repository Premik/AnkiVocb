package vocb.corp

import static vocb.Helper.utf8

import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

import com.xlson.groovycsv.CsvParser

import groovy.transform.CompileStatic
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import vocb.conf.ConfHelper

import static vocb.Ansi.*

public class Corpus {


	int limit=0
	private int limitCounter=0
	Map<String, BigDecimal> wordFreq = new HashMap(5000)
	Set<String> phrases = new HashSet<String>(2000)
	WordNormalizer wn = new WordNormalizer()

	ConfHelper cfgHelper = new ConfHelper()

	@Lazy public String[] sortedByFreq = {
		wordFreq.keySet().sort { String a, String b->
			wordFreq[b] <=> wordFreq[a]
		}
	}()
	
	private boolean checkLimit() {
		if (!limit) return true //unlimited		
		if (limitCounter>limit) return false
		limitCounter++		
		return true
	}
	
	//https://www.kaggle.com/datasets/rtatman/english-word-frequency
	void importKaggleEnglishWordFreqCsv(Reader reader, boolean normalize=true) {
		Iterator csvLines = CsvParser.parseCsv([:], reader)
		BigDecimal sum = 0
		for (line in csvLines) {
			if (!checkLimit()) break
			String w = line."word".toLowerCase().trim()
			String c = line."count"
			assert w && c
			BigDecimal bc = new BigDecimal(c)
			wordFreq[w] = bc
			sum+=bc
		}

		if (normalize) {
			//Convert freq to ppm
			wordFreq = wordFreq.collectEntries { String w, BigDecimal f->
				[w, f/sum*1000000]
			}
		}
		println "Imported ${wordFreq.size()} words from Kaggle. Freq sum=$sum"
	}

	void loadKaggleEnglishWordFreqCsv(boolean normalize=true) {
		getClass().getResource('/unigram_freq.csv').withReader(StandardCharsets.UTF_8.toString()) {
			importKaggleEnglishWordFreqCsv(it)
		}
	}

	void averageCommonWordsFrom(Corpus c) {
		assert c?.wordFreq : "No corpus or blank freq. words provided"
		Map<String, BigDecimal> f1 = wordFreq
		Map<String, BigDecimal> f2 = c.wordFreq



		//Set<String> commonWords = f1.keySet().intersect(f2.keySet())
		Set<String> allWords = f1.keySet() + (f2.keySet())

		/*commonWords.take(100).each { String w->
		 println "$w:  ${f1[w]/f2[w]} ( ${f1[w]-f2[w]})  ${f1[w]} ${f2[w]} }"
		 }*/

		for (String w in allWords) {
			//If only one is defined, take it
			if (!f2[w]) continue
				if (!f1[w]) {
					f1[w] = f2[w]
					continue
				}
			f1[w] = (f1[w] + f2[w]) /2 //When both do avg
		}
	}

	String[] topX(int x=100) {
		wordFreq.keySet().sort { String a, String b->
			wordFreq[b] <=> wordFreq[a]
		}.take(x)
	}

	public BigDecimal phraseFreq(String phrase, int skipWordsFreq=1) {
		Map<String, BigDecimal> wf = wordFreq.withDefault {0}
		List<String> parts = phrase.split(' ')
		if (parts.any {wf[it] < skipWordsFreq}) return 0 //Phrase contains an unknow word,
		parts.add(phrase)
		parts.collect {wf[it]}.average()
	}

	List<String> topXOf(List<String> terms, int minFreq =1) {
		terms
				.findAll {String s-> phraseFreq(s) >=minFreq}
				.sort { -phraseFreq(it)  }
	}

	public BigDecimal getFreqAnyVariant(String word, BigDecimal penalty=0.25) {
		List<String> wordVariants = wn.wordVariants(word, true)
		int foundAt = wordVariants.findIndexOf {wordFreq[it]}
		if (foundAt<0) return null //Not found
		BigDecimal ret = wordFreq[wordVariants[foundAt]]
		if (foundAt == 0) {
			//Exact match
			return ret
		}
		if (foundAt>0) {
			//Found a variant
			return ret*penalty //just random fraction of it
		}
		return null
	}
	
	public BigDecimal getFreqFromSentenceAnyVariant(String sentense, BigDecimal penaltySingle=1, BigDecimal penaltyMore=0.1) {
		List<BigDecimal> frqs = wn.tokens(sentense).map(this.&getFreqAnyVariant).toList().findAll()
		if (!frqs) return null
		int sz = frqs.size()
		if (sz ==1) return frqs.first()*penaltySingle
		return (frqs.sum()/sz)*penaltyMore //Average freq, random small fraction
	}


	public BigDecimal getAt(String word) {
		BigDecimal ret = getFreqFromSentenceAnyVariant(word)
		if (ret) return ret
		def (String a, String b) = wn.splitBrackets(word)
		if (b) {

			//Brackets were included. Try main word without the brackets first
			ret = getFreqFromSentenceAnyVariant(a, 1, 0.2)
			if (ret) return ret
			//Try word(s) in the bracket
			ret = getFreqFromSentenceAnyVariant(b, 0.5)
			if (ret) return ret
		}
		println color(word.padLeft(10), BOLD) + color(" - not in corpus" , RED)
		return null
	}

	public void addStrange() {
		wordFreq.putAll( [
			"tepidity" : 3,
			"extrauterine" : 2,
			"eradicte":3,
			"clearsighted":2,
			"ilustrious":1,
			"anathematize":3,
			"contagiousness":1,
			"surrende":3,
			"could've":100,
			"should've":100,
		])
	}

	static Corpus buildAll() {
		Corpus c1 = new Corpus().tap {loadWiki()}
		Corpus c2 = new Corpus().tap {loadWordFreq()}
		c1.averageCommonWordsFrom(c2)
		c2 = new Corpus().tap {loadKaggleEnglishWordFreqCsv()}
		c1.averageCommonWordsFrom(c2)
		c1.addStrange()

		return c1
	}

	static Corpus buildDef(int max=0) {
		new Corpus().tap {
			limit = max
			loadKaggleEnglishWordFreqCsv()
			addStrange()
		}
	}

	void statKaggle() {
		Corpus kg = new Corpus().tap {
			loadKaggleEnglishWordFreqCsv()
		}
		Corpus df = buildDef()
		Collection<String> rndWords=df.wordFreq.keySet().take(2000) + (df.sortedByFreq.take(500) as Set)
		Set<String> commonWords = kg.wordFreq.keySet().intersect(rndWords).findAll {it.length() >2}
		commonWords.take(100).each { String w->
			println "${w.padRight(20)} ${df[w]} ${kg[w]} ${kg[w]-df[w]}"
			//println "${w.padRight(20)} ${df[w]/kg[w]}"
		}
		//*0.06
		//0.037
		println commonWords.average { String w-> df[w]/(kg[w])}
	}

	static void main(String... args) {

		//buildDef().with { Corpus c->
		new Corpus().with { Corpus c->
			loadKaggleEnglishWordFreqCsv()
			//load12Dicts()
			//phrases.take(30).each {println "${it}"}
			println c["could"]
			println c["upon"]
			println c["I"]
			println c["you"]
			
			String[] s = sortedByFreq
			println s.take(10).each {
				println "${it.padRight(20)} ${wordFreq[it]}"
			}
			println s.takeRight(10).each {
				println "${it.padRight(20)} ${wordFreq[it]}"
			}
		}
	}
}
