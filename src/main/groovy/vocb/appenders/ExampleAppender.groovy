package vocb.appenders

import java.util.stream.Collectors

import vocb.HttpHelper
import vocb.azure.AzureTranslate
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term



public class ExampleAppender {


	AzureTranslate trn = new AzureTranslate(httpHelper: new HttpHelper() )
	WordNormalizer wn = new WordNormalizer()

	Manager dbMan = new Manager()
	int sleep=500
	int limit = 10
	int maxLength = 40

	private List<Concept> findTodo() {
		dbMan.db.concepts.findAll {
			it.terms && it.state!="ignore" && (!it.examples) && it.firstTerm
		}.findAll { Concept c ->
			wn.tokens(c.firstTerm).count() <= 2  //Only single or two words phrases
		}
	}

	void run() {

		dbMan.load()
		List<Concept> noEx= findTodo()

		int i =0
		for (Concept c in noEx) {
			String enWord = c.firstTerm
			String[] czWords = c.terms.values()
					.findAll {it.lang == "cs"}
					*.term
					.findAll()
					.collectMany {String term->
						//Any word of the two-words phrase
						wn.tokens(term).collect(Collectors.toList())
					}.unique()
			//println czWords

			List<Tuple2<String, String>> xtractedSamples = czWords // Each alt translation
					.collect { String czWord ->
						Map trnJson = trn.exampleJsonRun(enWord, czWord)
						return trn.extractExamples(trnJson)
					}
					.collectMany {it}
					.sort {Tuple2<String, String> a, Tuple2<String, String> b ->
						a[0].length() <=> b[0].length()
					}

			xtractedSamples
					.findAll { Tuple2<String, String> t->
						t[0].length() < maxLength
					}
					.each {Tuple2<String, String> t ->
						c.examples.put(t[0], new Term(t[0], "en"))
						c.examples.put(t[1], new Term(t[1], "cs"))
					}
			dbMan.save()
			Thread.sleep(sleep)
			if (i>limit) break
				i++
		}
		dbMan.save()
	}

	void reuseExisting() {

		dbMan.load()
		List<Concept> noEx = findTodo()
		Map<String, Concept> enWords = dbMan.db.concepts.collectEntries {Concept c ->
			wn.uniqueueTokens(c.examples.values().find {it.lang=="en"}?.term ?: "").collectEntries { String word->
				[(word):c]
			} 			
		}
		
		for (Concept c in noEx) {
			Concept fromC = enWords[c.firstTerm]
			if (fromC) {	
				println "'$c.firstTerm': ${fromC.examples.values()[0].term}"
				c.examples = fromC.examples.values()*.clone().collectEntries {Term t->[(t.term): t] }
			}
			
		}

		dbMan.save()
	}


	public static void main(String[] args) {
		ExampleAppender a = new ExampleAppender()
		//a.run()
		a.reuseExisting()

		println "Done"
	}
}
