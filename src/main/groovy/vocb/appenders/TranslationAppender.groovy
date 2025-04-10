package vocb.appenders

import static vocb.Ansi.*

import vocb.HttpHelper
import vocb.aws.AwsTranslate
import vocb.azure.AzureTranslate
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager
import vocb.data.Term
import vocb.translator.PythonTranslation
import vocb.translator.Translator

public class TranslationAppender {


	AzureTranslate azTrn = new AzureTranslate(httpHelper: new HttpHelper() )
	AwsTranslate awsTrn = new AwsTranslate()
	PythonTranslation pyTrn = new PythonTranslation()
	Translator trn = pyTrn

	int sleep =0
	int limit= 0

	@Lazy
	Manager dbMan = new Manager()



	void translateWords() {

		dbMan.load()
		List<Concept> noCs = dbMan.db.concepts.findAll {
			it.terms && !it.ignore && (!it.termsByLang("cs"))
		}
		int i = 0
		for (Concept c in noCs) {			
			print("$c.firstTerm->")
			List<String> translations = trn.translations(c.firstTerm)
			println("$translations") 
			translations.each {String csWord ->
				c.terms.add(new Term(csWord, "cs"))
			}
			if (i % 10 == 0) {
				dbMan.save()				
			}
			
			if (i>limit && limit>0) {
				println color("Limit reached", RED)
				break
			}
			i++
			Thread.sleep(sleep)
			//break
		}
		dbMan.save()
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
			translateWords()
			//reuseTranlation()
			//translateExamples()

		}
//  profileName: ignore
		println "Done"
	}
}
