package vocb.corp

import static vocb.Helper.utf8

import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

import com.xlson.groovycsv.CsvParser

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult


public class Corpus {


	Map<String, Double> wordFreq = new HashMap(5000)
	Set<String> phrases = new HashSet<String>(2000)


	//https://www.wordfrequency.info/free.asp
	void importCsvCorpus(Map parserArgs = [:], Reader reader) {
		String dispersionCol="Dispersion"
		Iterator csvLines = CsvParser.parseCsv(parserArgs, reader)
		for (line in csvLines) {
			wordFreq[line.Word.toLowerCase()] = line."Dispersion"
		}
		println "Imported ${wordFreq.size()} words"
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
				wordFreq[w.toLowerCase()] = new Double(1-groupIndex/120d) //Magic factor to get similar Disp as wordfreq
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
		//SAXParser parser = new SAXParser()
		//def parser =new org.ccil.cowan.tagsoup.Parser()
		//def page = new XmlSlurper(parser).parse(input)
		def page = new XmlSlurper().parse(input)
		GPathResult content = page.body.'**'.find {it.@id == 'bodyContent'}
		content.'**'.each { GPathResult table ->
			if (table.name() != 'table') {return}

			table.tbody.tr.eachWithIndex { GPathResult row, int i ->
				if (i == 0) {return}
				String w = row.td[1].a.toString().trim()
				String r = row.td[2].toString().trim()

				wordFreq[w.toLowerCase()] = new Double(r)
			}
		}
		println "Imported ${wordFreq.size()} words."


	}

	void importCsvCorpus(Map parserArgs = [:], InputStream str) {
		importCsvCorpus(parserArgs, new InputStreamReader(str, StandardCharsets.UTF_8))
	}

	void loadInbuild() {
		//import12WordFreq(getClass().getResource('/2+2+3frq.txt').openStream())
		importWikiWordList(Corpus.class.getResource('/Wiktionary_Frequency lists_PG_2006_04_1-10000.html').openStream())
		import12Phrases(getClass().getResource('/6of12.txt').openStream())
	}

	static void main(String... args) {

		Corpus n = new Corpus()
		//n.importCsvCorpus(getClass().getResource('/wordFreq.csv').openStream())
		//n.import12WordFreq(getClass().getResource('/2+2+3frq.txt').openStream())
		//n.importWikiWordList(Corpus.class.getResource('/Wiktionary_Frequency lists_PG_2006_04_1-10000.html').openStream())

		n.loadInbuild()


	}


}
