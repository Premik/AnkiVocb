package vocb.appenders

import groovy.transform.CompileStatic
import vocb.HttpHelper
import vocb.azure.AzureTranslate
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term



public class ExampleAppender {


	AzureTranslate trn = new AzureTranslate(httpHelper: new HttpHelper() )

	Manager dbMan = new Manager()
	int sleep=500
	int limit = 5

	void run() {

		dbMan.load()
		List<Concept> noEx = dbMan.db.concepts.findAll {
			it.terms && it.state!="ignore" && (!it.examples) && it.firstTerm
		}

		int i =0
		for (Concept c in noEx) {
			String enWord = c.firstTerm
			String[] czWords = c.terms.values().findAll {it.lang == "cs"}*.term
			
			List<Tuple2<String, String>> xtractedSamples = czWords.collect { String czWord ->
				Map trnJson = trn.exampleJsonRun(enWord, czWord)
			    return trn.extractExamples(trnJson)
			}.collectMany {it}
			 
			xtractedSamples.each {Tuple2<String, String> t ->
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


	public static void main(String[] args) {
		ExampleAppender a = new ExampleAppender()
		a.run()

		println "Done"
	}
}
