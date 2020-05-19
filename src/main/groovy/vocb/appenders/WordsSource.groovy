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
	
	 
	@Lazy Corpus corp = Corpus.buildDef()
	BigDecimal minFreq= 0
	BigDecimal maxFreq= 10e10
	int limit=100
	
	Similarity sim = new Similarity()
	boolean simulation = false


	@Lazy Manager dbMan = {
		new Manager().tap {
			load()
		}
	}()

	void fromText(String text) {
		assert sourceName
		List<String> words = new ArrayList(wn.uniqueueTokens(text))
				.findAll {String s ->				
					corp.wordFreq[s] > minFreq && corp.wordFreq[s] < maxFreq }
				.sort{ String a, String b->
					-(corp.wordFreq[a]?:0) <=> -(corp.wordFreq[b]?:0)
				}




		println "Processing ${words.size()} words"
		Map<String, List<String>> wordsInSentences = wn.wordsInSentences(text)
		int i= 0
		for (String w in words) {
			if (i> limit) {
				break
			}

			Term t = new Term(w, "en")
			BigDecimal frq = Helper.roundDecimal((corp.wordFreq[w]?:0), 3)
			String stars  = '🟊'*dbMan.numberOfStarts(frq)
			
			Concept c= dbMan.conceptByFirstTerm[w]
			if (c != null) {
				println "X $w - already in db $stars "
				if (!c?.origins?.contains(sourceName)) {
					if (c.origins == null) {
						c.origins = []
					}
					c.origins.add(sourceName)
				}
				continue
			}
			c = new Concept(terms: [w:t], freq:corp.wordFreq[w], origins:[sourceName])
			
			println "+ $w: added $frq $stars ${wordsInSentences[w].join('|')}"
			i++
			dbMan.db.concepts.add(c)
			if (i % 10 == 0) {
				if (!simulation) dbMan.save()
			}
		}
		if (!simulation) dbMan.save()
	}

	void fromCorups() {
		sourceName = "corpus"
		String[] top = corp.sortedByFreq.take(limit)
		fromText(top.join(" "))
	}

	void fromOwnSamples() {
		sourceName = "conceptDbSamples"
		fromText(dbMan.allTextWithLang("en").join("\n"))
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
					corp.wordFreq[s]?:0 > minFreq //Reasonable words
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
		 a.run(supa)*/
		new WordsSource().tap {
			//fromOwnSamples(100)
			//minFreq = 1
			//simulation = true
			//String tx = getClass().getResource('/Supaplex.txt').text
			String tx = getClass().getResource('/sources/JingleBells.txt').text
			
			sourceName = "JingleBells"
			fromText(tx)
			return
			wn.phraseFreqs(tx,2, 3)
			   .collectEntries{ String w, BigDecimal fqInText-> [w, corp.phraseFreq(w)*fqInText]}
			   .findAll {it.value > 0}
			   .sort {-it.value }
			   .each {println it}
			
		}
		//a.fromCorups(500)

		println "Done"
	}
}
