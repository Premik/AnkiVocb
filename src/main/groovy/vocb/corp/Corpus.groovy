package vocb.corp

import static vocb.Helper.utf8

import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

import com.xlson.groovycsv.CsvParser

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import vocb.conf.ConfHelper

import static vocb.Ansi.*

public class Corpus {


	Map<String, BigDecimal> wordFreq = new HashMap(5000)
	Set<String> phrases = new HashSet<String>(2000)
	WordNormalizer wn = new WordNormalizer()
	
	ConfHelper cfgHelper = new ConfHelper()

	@Lazy public String[] sortedByFreq = {
		wordFreq.keySet().sort { String a, String b->
			wordFreq[b] <=> wordFreq[a]
		}
	}()


	//https://www.wordfrequency.info/free.asp
	void importWordFreqCsvCorpus(Map parserArgs = [:], Reader reader) {
		String dispersionCol="Dispersion"
		Iterator csvLines = CsvParser.parseCsv(parserArgs, reader)
		for (line in csvLines) {
			String w = line."Word".toLowerCase().replace('\u00A0',' ').trim()

			String f = line."Frequency"

			//String f  = line."Dispersion"
			if (!f || !w) {
				continue
			}
			wordFreq[w] = new BigDecimal(f)
		}
		println "Imported ${wordFreq.size()} words from Csv"
	}
	
	void loadWordFreq() {
		getClass().getResource('/wordFreq.csv').withReader(StandardCharsets.UTF_8.toString()) {
			importWordFreqCsvCorpus([:], it)
		}
		
	}
	
	
	
	//https://www.kaggle.com/datasets/rtatman/english-word-frequency
	void importKaggleEnglishWordFreqCsv(Reader reader) {
		Iterator csvLines = CsvParser.parseCsv([:], reader)
		for (line in csvLines) {
			String w = line."word".toLowerCase().trim()			
			String c = line."count"
			assert w && c				
			wordFreq[w] = new BigDecimal(c)*0.037 //Avg error from other corpuses
		}
		println "Imported ${wordFreq.size()} words from Kaggle"
	}
	
	void loadKaggleEnglishWordFreqCsv() {
		getClass().getResource('/unigram_freq.csv').withReader(StandardCharsets.UTF_8.toString()) {
			importKaggleEnglishWordFreqCsv(it)
		}
	}

	//https://github.com/en-wl/wordlist/tree/master/alt12dicts
	void import12WordFreq(InputStream input, int limit = 0) {
		//2+2+3frq.txt
		Pattern blockDeli= ~/-+\s*(\d+)\s*-+/
		Pattern wordPatter = ~/^\w+/
		int groupIndex = 0

		input.eachLine(utf8) { String line ->

			def blocMatcher = line =~  blockDeli
			if (blocMatcher) {
				groupIndex = blocMatcher[0][1] as int
				return
			}
			def wordMatcher = line =~  wordPatter
			if (!wordMatcher) {return} //Ignore lemming
			String w = wordMatcher[0]
			//println w

			if (limit > 1 || limit==0) {
				wordFreq[w.toLowerCase()] = new BigDecimal(1-groupIndex/120d) //Magic factor to get similar Disp as wordfreq
				if (limit != 0) {
					limit--
				}
			}
			//println "$groupIndex: $w "
		}
		println "Imported ${wordFreq.size()} words. Groups: $groupIndex"
	}

	void import12Phrases(InputStream input, int limit = 0) {
		//6of12.txt
		Pattern phrasePatter = ~/^\w\w+\s\w\w+/

		input.eachLine(utf8) { String line ->

			def phraseMatcher = line =~  phrasePatter
			if (phraseMatcher) {
				phrases+= phraseMatcher[0].toLowerCase()
				return
			}
		}

		println "Imported ${phrases.size()} phrases"
	}
	//https://en.wiktionary.org/wiki/Wiktionary:Frequency_lists/PG/2006/04/1-10000

	void importWikiWordList(InputStream input, int limit = 0) {
		assert input != null : "No input provided"
		//SAXParser parser = new SAXParser()
		//def parser =new org.ccil.cowan.tagsoup.Parser()
		//def page = new XmlSlurper(parser).parse(input)

		def page = new XmlSlurper().parse(input)
		GPathResult content = page.body.'**'.find {it.@id == 'bodyContent'}
		content.'**'.each { GPathResult table ->
			if (table.name() != 'table') {return}

			table.tbody.tr.eachWithIndex { GPathResult row, int i ->
				if (i == 0) {return}
				String w = row.td[1].a.toString().trim().toLowerCase()
				String r = row.td[2].toString().trim().toLowerCase()

				wordFreq[w.toLowerCase()] = new BigDecimal(r)
			}
		}
		println "Imported ${wordFreq.size()} words for wiki pages"


	}

	

	void loadWiki() {
		importWikiWordList(cfgHelper.resolveRes("Wiktionary_Frequency lists_PG_2006_04_1-10000 - Wiktionary"))
		importWikiWordList(cfgHelper.resolveRes("Wiktionary Frequency lists_PG_2006_04_10001-20000 - Wiktionary"))
		importWikiWordList(cfgHelper.resolveRes("Wiktionary Frequency lists_PG_2006_04_20001-30000 - Wiktionary"))
		importWikiWordList(cfgHelper.resolveRes("Wiktionary Frequency lists_PG_2006_04_30001-40000 - Wiktionary"))
	}

	void load12Dicts() {
		import12Phrases(getClass().getResource('/6of12.txt').openStream())
		import12WordFreq(getClass().getResource('/2+2+3frq.txt').openStream())
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

	public BigDecimal getAt(String word) {

		List<String> wordVariants = wn.wordVariants(word)
		BigDecimal ret = wordVariants.findResult {wordFreq[it]}
		if (ret) return ret
		println color(word.padLeft(10), BOLD) + color(" - not in corpus" , RED)
	}

	public void addStrange() {
		wordFreq.putAll( [
			"bumped" : 1000,
			"ve" : 1,
			"bigger":81717,
			"arrived":5735,
			"frosting":3735,
			"smelling":41717,
			"bouncy":81717,
			"antiquities":500,
			"you're":2382497,
			"i'm":4082139,
			"it's":4082139,
			"what's":408213,
			"let's":41000,
			"everyone's":5735,
			"hauling": 120,
			"shunting":2000,
			"babies":22544,
			"mommies":1456,
			"burg":456,
			"ponytail":10,
			"messes":4000,
			"successes":52956,

		])
		

	}

	static Corpus buildDef() {
		Corpus c1 = new Corpus().tap {loadWiki()}		
		Corpus c2 = new Corpus().tap {loadWordFreq()}
		c1.averageCommonWordsFrom(c2)
		c2 = new Corpus().tap {loadKaggleEnglishWordFreqCsv()}
		c1.averageCommonWordsFrom(c2)
		c1.addStrange()
		
		return c1
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
		
		buildDef().with {
			//load12Dicts()
			//phrases.take(30).each {println "${it}"}
			topX(2000).each  {println it }
			

		}


	}


}
