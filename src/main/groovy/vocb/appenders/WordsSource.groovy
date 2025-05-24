package vocb.appenders

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

	void run(String text, int limit=20) {
		assert sourceName
		List<String> words = new ArrayList(wn.uniqueueTokens(text))
				.findAll {String s -> corp.wordFreq[s] > minFreq }
				.sort{ String a, String b->
					-(corp.wordFreq[a]?:0) <=> -(corp.wordFreq[b]?:0)
				}


		dbMan.load()
		words.take(limit).each { String w->


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
		run(top.join(" "), limit)
	}
	
	void fromOwnSamples(int limit=100) {
		sourceName = "conceptDbSamples"
		List<String> enSamples = dbMan.db.concepts.collect {Concept c->
			c.examples.values().findAll {it.lang = "en"}		
		}
		
	}

	public static void main(String[] args) {
		/*WordsSource a = new WordsSource(sourceName:"Supaplex", minFreq:2*1000)
		 String supa = WordsSource.class.getResource('/Supaplex.txt').text
		 a.run(supa)*/
		new WordsSource().tap {			
			fromOwnSamples(5)			
		}
		//a.fromCorups(500)
		


		println "Done"
	}
}
