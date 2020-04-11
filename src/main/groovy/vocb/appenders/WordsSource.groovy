package vocb.appenders

import vocb.Helper
import vocb.corp.Corpus
import vocb.corp.WordNormalizer
import vocb.data.Manager

public class WordsSource {

	String sourceName
	WordNormalizer wn = new WordNormalizer()
	BigDecimal minFreq = 5*1000
	@Lazy Corpus corp = Corpus.buildDef()


	Manager dbMan = new Manager()

	void run(String text) {
		List<String> words = new ArrayList(wn.uniqueueTokens(text))
				.findAll {String s -> corp.wordFreq[s] > minFreq }
				.sort{ String a, String b->
					-(corp.wordFreq[a]?:0) <=> -(corp.wordFreq[b]?:0)
				}


		dbMan.autoSave {
			words.each {
				println "$it: ${Helper.roundDecimal((corp.wordFreq[it]?:0)/1000, 3)}"
			}
		}
	}


	public static void main(String[] args) {
		WordsSource a = new WordsSource(sourceName:"Supaplex", minFreq:2*1000)

		String supa = WordsSource.class.getResource('/Supaplex.txt').text
		a.run(supa)

		println "Done"
	}
}
