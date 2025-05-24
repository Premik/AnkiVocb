package vocb.appenders

import static vocb.Ansi.*

import vocb.HttpHelper
import vocb.aws.AwsTranslate
import vocb.azure.AzureTranslate
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager
import vocb.data.Term

public class TranslationAppender {


	AzureTranslate trn = new AzureTranslate(httpHelper: new HttpHelper() )
	AwsTranslate awsTrn = new AwsTranslate()

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
			Map trnJson = trn.dictLookup(c.firstTerm)
			trn.extractTopTrns(trnJson).each {String csWord ->
				c.terms.add(new Term(csWord, "cs"))
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
		dbMan.load()
		List<Example> noCs = dbMan.db.examples.collect().findAll { Example e->
			e.terms.size() ==1
		}

		int i = 0
		println "Found ${noCs.size()} examples with missing cs trn"
		for (Example e in noCs) {
			String enSam = e.firstTerm
			String[] csSams = [awsTrn.trn(enSam)]
			println "$enSam -> ${color(csSams.join("|"), BLUE)}"
			csSams.each {String csTrn ->
				e.terms.add(Term.csTerm(csTrn))
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

			limit = 20
			translateWords()
			//reuseTranlation()
			translateExamples()

		}

		println "Done"
	}
}
