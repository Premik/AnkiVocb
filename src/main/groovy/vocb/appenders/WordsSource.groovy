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


	@Lazy Manager dbMan = {
		new Manager().tap {
			load()
		}
	}()

	void fromText(String text, int limit=20) {
		assert sourceName
		List<String> words = new ArrayList(wn.uniqueueTokens(text))
				.findAll {String s -> corp.wordFreq[s] > minFreq }
				.sort{ String a, String b->
					-(corp.wordFreq[a]?:0) <=> -(corp.wordFreq[b]?:0)
				}




		int i= 0
		for (String w in words) {
			if (i> limit) {
				break
			}

			Term t = new Term(w, "en")

			Concept c= dbMan.conceptByFirstTerm[w]
			if (c != null) {
				println "X $w - already in db"
				if (!c?.origins?.contains(sourceName)) {
					if (c.origins == null) {
						c.origins = []
					}
					c.origins.add(sourceName)
					
				}
				continue
			}
			c = new Concept(terms: [w:t], freq:corp.wordFreq[w], origins:[sourceName])
			println "+ $w: ${Helper.roundDecimal((corp.wordFreq[w]?:0)/1000, 3)} added"
			i++
			dbMan.db.concepts.add(c)
			if (i % 10 == 0) {
				dbMan.save()
			}
		}
		dbMan.save()
	}

	void fromCorups(int limit=100) {
		sourceName = "corpus"
		Map<String, BigDecimal> wf = corp.wordFreq
		String[] top = wf.keySet().sort { String a, String b->
			wf[b] <=> wf[a]
		}.take(limit)
		fromText(top.join(" "), limit)
	}

	void fromOwnSamples(int limit=100) {
		sourceName = "conceptDbSamples"
		fromText(dbMan.allTextWithLang("en").join("\n"), limit)
	}

	public static void main(String[] args) {
		/*WordsSource a = new WordsSource(sourceName:"Supaplex", minFreq:2*1000)
		 String supa = WordsSource.class.getResource('/Supaplex.txt').text
		 a.run(supa)*/
		new WordsSource().tap {
			fromOwnSamples(100)
		}
		//a.fromCorups(500)



		println "Done"
	}
}
