package vocb.appenders

import java.nio.file.Path

import vocb.corp.Corpus
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term

public class WordsSource {

	String sourceName
	WordNormalizer wn = new WordNormalizer()
	@Lazy Corpus corp = {
		Corpus c = new Corpus()
		c.loadInbuild()
		return c
	}()
	
	Manager dbMan = new Manager()

	void run(String text) {
		List<String> words = new ArrayList(wn.uniqueueTokens(text)).sort()
		words.each {
			println "$it: ${corp.wordFreq[it]?:0}"
		}
		
		dbMan.autoSave {
		
		}
	}


	public static void main(String[] args) {
		WordsSource a = new WordsSource()
		
		String supa = WordsSource.class.getResource('/Supaplex.txt').text
		a.run(supa)
		
		println "Done"
	}
}
