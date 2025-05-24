package vocb.appenders

import static vocb.Ansi.*

import vocb.Helper
import vocb.corp.Corpus
import vocb.corp.Similarity
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term
import vocb.pck.Pack

public class WordsSource {

	
	WordNormalizer wn = new WordNormalizer()
	
	 
	@Lazy 
	Corpus corp = Corpus.buildDef()
	BigDecimal minFreq= null
	BigDecimal maxFreq= 10e10
	int limit=0
	
	Similarity sim = new Similarity()
	boolean simulation = false
	Pack pack = new Pack()


	@Lazy Manager dbMan = {
		new Manager().tap {
			load()
		}
	}()

	void fromText(String text) {
		
		
		List<String> words = new ArrayList(wn.uniqueueTokens(text))
				.findAll {String s ->
					BigInteger f = corp[s]
					f == minFreq || (f > minFreq && f < maxFreq) 
				}
				.sort{ String a, String b->
					-(corp[a]?:0) <=> -(corp[b]?:0)
				}




		println "Processing ${color(words.size().toString(), BOLD)} words"
		
		Map<String, List<String>> wordsInSentences = wn.wordsInSentences(text)
		int i= 0
		for (String w in words) {
			if (limit > 0 && i> limit) {
				println color("Limit reached", RED)
				break
			}

			Term t = new Term(w, "en")
			BigDecimal frq = Helper.roundDecimal((corp[w]?:0), 3)
			String stars  = color('🟊'*(dbMan.numberOfStarsFreq(frq)?:0), YELLOW )
			
			Concept c= dbMan.findConceptByFirstTermAnyVariant(w)
			if (c != null) {
				if (words.size() < 30) {
					println color("${w.padLeft(10)}  - already in db $stars ", WHITE)
				}				
				continue
			}
			c = new Concept(freq:corp[w], location: dbMan.defaultConceptsLocation).tap {
				terms.add(t)
			}
			
			println "${color(w.padLeft(10), BOLD)}: added $frq $stars ${color(wordsInSentences[w].join('|'), BLUE)}"
			i++
			dbMan.db.concepts.add(c)
			if (i % 10 == 0) {
				if (!simulation) dbMan.save()
			}
		}
		if (!simulation) dbMan.save()
	}

	void fromCorups() {
		
		String[] top = corp.sortedByFreq.take(limit)
		fromText(top.join(" "))
	}

	void fromOwnSamples() {
		
		fromText(dbMan.allTextWithLang("en").join("\n"))
	}

	void decomposition() {		
		dbMan.db
		termsStream
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
	
	void basicWords() {
		dbMan.db
		Set<String> simpleWords = new File("/data/src/AnkiVocb/pkg/SimpleWords/words.txt").readLines().toSet()
		corp.topX(2000)
				.findAll {String s-> !s.contains("'")}
				.findAll {String s->
					dbMan.conceptsByTerm[s].any { Concept c-> !c.ignore }
				}.findAll { !simpleWords.contains(it) }
				.each {
					//println "${it} ${corp.wordFreq[it]}"
					println it
				}
	}

	public static void main(String[] args) {
		/*WordsSource a = new WordsSource(sourceName:"Supaplex", minFreq:2*1000)		 
		 a.run(supa)*/
		new WordsSource().tap {
			//fromOwnSamples()
			//minFreq = 1
			//simulation = true
			//String tx = getClass().getResource('/Supaplex.txt').text
			//String tx = getClass().getResource('/sources/JingleBells.txt').text
			//String tx = ''''''
			
			//String tx = new File("/data/src/AnkiVocb/pkg/DuckTales/sentences.txt").text
			String tx = new File("/data/src/AnkiVocb/pkg/Uncommon/words.txt").text
			
			
			fromText(tx)
			//fromOwnSamples()
			//basicWords()
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
