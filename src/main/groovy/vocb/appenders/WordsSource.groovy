package vocb.appenders

import java.math.BigDecimal

import groovyjarjarantlr4.v4.runtime.tree.Trees
import vocb.Helper
import vocb.corp.Corpus
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term

public class WordsSource {

	String sourceName
	WordNormalizer wn = new WordNormalizer()
	BigDecimal minFreq = 0
	@Lazy Corpus corp = Corpus.buildDef()


	Manager dbMan = new Manager()

	void run(String text) {
		assert sourceName
		List<String> words = new ArrayList(wn.uniqueueTokens(text))
				.findAll {String s -> corp.wordFreq[s] > minFreq }
				.sort{ String a, String b->
					-(corp.wordFreq[a]?:0) <=> -(corp.wordFreq[b]?:0)
				}


		dbMan.load()
		words.take(20).each { String w->


			Term t = new Term(w, "en")
			Concept c = new Concept(terms: [w:t], freq:corp.wordFreq[w], origins:[sourceName])
			if (dbMan.conceptByFirstTerm.containsKey(w)) {
				println "X $w"
				if (!c.origins.contains(sourceName)) {
					c.origins.add(sourceName)
					dbMan.save()
				}
				return
			}
			println "$w: ${Helper.roundDecimal((corp.wordFreq[w]?:0)/1000, 3)}"
			dbMan.db.concepts.add(c)
			dbMan.save()
		}
		dbMan.save()
	}

	void fromCorups(int limit=100) {
		sourceName = "corpus"
		Map<String, BigDecimal> wf = corp.wordFreq		
		String[] top = wf.keySet().sort { String a, String b->
			wf[b] <=> wf[a]
		}.take(limit)
		run(top.join(" "))
		
	}

	public static void main(String[] args) {
		/*WordsSource a = new WordsSource(sourceName:"Supaplex", minFreq:2*1000)
		 String supa = WordsSource.class.getResource('/Supaplex.txt').text
		 a.run(supa)*/
		WordsSource a = new WordsSource()
		a.fromCorups(100)


		println "Done"
	}
}
