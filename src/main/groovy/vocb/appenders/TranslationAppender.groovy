package vocb.appenders

import vocb.HttpHelper
import vocb.azure.AzureTranslate
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term

public class TranslationAppender {


	AzureTranslate trn = new AzureTranslate(httpHelper: new HttpHelper() )
	int sleep =500

	Manager dbMan = new Manager()

	void run() {

		dbMan.load()
		List<Concept> noCs = dbMan.db.concepts.findAll {
			it.terms && it.state!="ignore" && (!it.termsByLang("cs"))
		}

		for (Concept c in noCs) {
			Map trnJson = trn.trnJsonrunTrn(c.firstTerm)
			trn.extractTopTrns(trnJson).each {String csWord ->
				c.terms.put(csWord, new Term(csWord, "cs"))
			}
			dbMan.save()
			Thread.sleep(sleep)
			//break
		}
	}


	public static void main(String[] args) {
		TranslationAppender a = new TranslationAppender()
		a.run()

		println "Done"
	}
}
