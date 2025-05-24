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

	void run() {

		dbMan.load()
		List<Concept> noEx = dbMan.db.concepts.findAll {
			it.terms && it.state!="ignore" && (!it.examples) && it.firstTerm
		}

		int i =0
		for (Concept c in noEx) {
			String enWord = c.firstTerm
			String czWord = c.terms.values().find {it.lang == "cs"}.term
			
			Map trnJson = trn.exampleJsonRun(enWord, czWord)
			println trn.extractExamples(trnJson)
			trn.extractExamples(trnJson).each {Tuple2<String, String> t ->
				c.examples.put(t[0], new Term(t[0], "en"))
				c.examples.put(t[1], new Term(t[1], "cs"))
			}
						
			if (i>5) break
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
