package vocb.appenders

import vocb.HttpHelper
import vocb.aws.AwsTranslate
import vocb.azure.AzureTranslate
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term
import static vocb.Ansi.*

public class TranslationAppender {


	AzureTranslate trn = new AzureTranslate(httpHelper: new HttpHelper() )
	AwsTranslate awsTrn = new AwsTranslate()

	int sleep =500
	int limit= 100

	Manager dbMan = new Manager()


	@Deprecated
	List<Concept> findTodo() {
		dbMan.db.concepts.findAll {
			it.terms && it.state!="ignore" && it.examplesByLang("en") && it.firstTerm && (!it.examplesByLang("cs"))
		}
	}

	void translateWords() {

		dbMan.load()
		List<Concept> noCs = dbMan.db.concepts.findAll {
			it.terms && it.state!="ignore" && (!it.termsByLang("cs"))
		}
		int i = 0
		for (Concept c in noCs) {
			Map trnJson = trn.dictLookup(c.firstTerm)
			trn.extractTopTrns(trnJson).each {String csWord ->
				c.terms.put(csWord, new Term(csWord, "cs"))
			}
			dbMan.save()
			if (i>limit) {
				println color("Limit reached", RED)
				break
			}
				i++
			Thread.sleep(sleep)
			//break
		}
	}

	void translateExamples() {
		assert false: "Refactor"
		dbMan.load()
		List<Concept> noCs = findTodo()
		int i = 0
		println "Found ${noCs.size()} concept with missing cs trn"
		for (Concept c in noCs) {
			String enSam = c.examplesByLang("en")[0]?.term
			String[] csSams = [awsTrn.trn(enSam)]
			//Map trnJson = trn.dictLookup(enSam)
			//List<String> csSams =trn.extractTopTrns(trnJson)
			println "$c.firstTerm $enSam $csSams"
			csSams.each {String csTrn ->
				c.examples.put(csTrn, new Term(csTrn, "cs"))
			}

			dbMan.save()
			if (i>=limit) {
				println color("Limit reached", RED)
				break
			}
			i++
			Thread.sleep(sleep)
			//break
		}
	}

	@Deprecated
	void reuseTranlation() {
		dbMan.load()
		Map<String, Concept> byEnSample = dbMan.db.concepts
				.findAll {it.examplesByLang("cs")}
				.collectEntries { Concept c->
					[c.examplesByLang("en")[0]?.term, c]
				}
				
				

		for (Concept c in findTodo()) {
			String enSample = c.examplesByLang("en")[0]?.term
			if (!enSample) continue
			String csSample = byEnSample[enSample]?.examplesByLang("cs")?.first()?.term
			if (!csSample) continue
				println "'$c.firstTerm' Reusing: $csSample"
			c.examples.put(csSample, new Term(csSample, "cs"))

		}
		dbMan.save()
	}



	public static void main(String[] args) {
		new TranslationAppender().with {

			//limit = 1
			translateWords()
			//reuseTranlation()
			//translateExamples()

		}

		println "Done"
	}
}
