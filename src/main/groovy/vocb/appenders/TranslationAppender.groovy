package vocb.appenders

import vocb.HttpHelper
import vocb.azure.AzureTranslate
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term

public class TranslationAppender {


	AzureTranslate trn = new AzureTranslate(httpHelper: new HttpHelper() )
	int sleep =500
	int limit= 100

	Manager dbMan = new Manager()

	void translateWords() {

		dbMan.load()
		List<Concept> noCs = dbMan.db.concepts.findAll {
			it.terms && it.state!="ignore" && (!it.termsByLang("cs"))
		}
		int i = 0
		for (Concept c in noCs) {
			Map trnJson = trn.trnJsonrunTrn(c.firstTerm)
			trn.extractTopTrns(trnJson).each {String csWord ->
				c.terms.put(csWord, new Term(csWord, "cs"))
			}
			dbMan.save()
			if (i>limit) break
				i++
			Thread.sleep(sleep)
			//break
		}
	}

	void translateExamples() {

		dbMan.load()
		List<Concept> noCs = dbMan.db.concepts.findAll {
			it.terms && it.state!="ignore" && it.examplesByLang("en") && it.firstTerm && (!it.examplesByLang("cs"))  
		}
		int i = 0
		for (Concept c in noCs) {
			String enSam = c.examplesByLang("en")[0]
			Map trnJson = trn.trnJsonrunTrn(enSam)
			List<String> csSams =trn.extractTopTrns(trnJson)
			println "$c.firstTerm $enSam $csSams"
			csSams.each {String csTrn ->
				
				c.examples.put(csTrn, new Term(csTrn, "cs"))
				
			}
			dbMan.save()
			if (i>limit) break
				i++
			Thread.sleep(sleep)
			//break
		}
	}


	public static void main(String[] args) {
		new TranslationAppender().with {
		
		limit = 1
		//a.translateWords()
		translateExamples()
		}

		println "Done"
	}
}
