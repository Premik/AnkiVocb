package vocb.appenders

import vocb.Helper
import vocb.corp.Corpus
import vocb.corp.Similarity
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term

public class WordsSource {

	String sourceName
	WordNormalizer wn = new WordNormalizer()
	BigDecimal minFreq = 0
	@Lazy Corpus corp = Corpus.buildDef()
	Similarity sim = new Similarity()


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
		String[] top = corp.sortedByFreq.take(limit)
		fromText(top.join(" "), limit)
	}

	void fromOwnSamples(int limit=100) {
		sourceName = "conceptDbSamples"
		fromText(dbMan.allTextWithLang("en").join("\n"), limit)
	}

	void decomposition() {
		sourceName = "decomp"
		dbMan.db
		dbMan.db.concepts.stream()
				.flatMap{ Concept c ->
					c.termsByLang("en").collect {it.term}.stream()
				}
				.flatMap { String s->
					wn.tokens(s) //Phrases by words
				}
				.flatMap { String s->
					sim.allSubstringsOf(s).stream() //Decompose
				}
				.filter {String s->
					s.length() > 2  //Longer than 2 letter words candidates
				}
				.filter {String s->
					(corp.wordFreq[s]?:0) > minFreq //Reasonable words
				}.filter {String s->
					!dbMan.conceptsByTerm.containsKey(s) //Ignore already known words
				}.collect()
				.toSet()
				.sort{ String a, String b -> 
					corp.wordFreq[b] <=> corp.wordFreq[a]
				}
				.each {
					println "${it} ${corp.wordFreq[it]}"
				}
	}

	public static void main(String[] args) {
		/*WordsSource a = new WordsSource(sourceName:"Supaplex", minFreq:2*1000)
		 String supa = WordsSource.class.getResource('/Supaplex.txt').text
		 a.run(supa)*/
		new WordsSource().tap {
			//fromOwnSamples(100)
			minFreq = 40000
			decomposition()
		}
		//a.fromCorups(500)

		println "Done"
	}
}
