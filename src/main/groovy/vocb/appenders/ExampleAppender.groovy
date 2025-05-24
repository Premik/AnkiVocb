package vocb.appenders

import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.stream.Stream

import groovy.transform.CompileStatic
import vocb.Helper
import vocb.HttpHelper
import vocb.azure.AzureTranslate
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager
import vocb.data.Term
import static vocb.Ansi.*


public class ExampleAppender {


	AzureTranslate trn = new AzureTranslate(httpHelper: new HttpHelper() )
	WordNormalizer wn = new WordNormalizer()

	//Manager dbMan = new Manager()
	Manager dbMan = new Manager(defaultExamplesFileName:"examplesDraft.yaml")
	
	int sleep=500
	int limit = 5000
	int maxLength = 60

	Stream<Concept> todo

	@Deprecated
	void todoFromDbLegacy() {
		todo = dbMan.db.concepts.findAll {
			it.terms && !it.ignore && (!it.examples) && it.firstTerm
		}.findAll { Concept c ->
			wn.tokens(c.firstTerm).count() <= 2  //Only single or two words phrases
		}.stream()
	}

	void todoFromText(CharSequence input, Manager dbMan=dbMan) {
		todo = wn.tokens(input).map { String w->
			Concept c = dbMan.findConceptByFirstTermAnyVariant(w)
			assert c: "No concept found for word '$w'"
			return c
		}
	}

	@CompileStatic
	List<Example> azureDictSampleForConcept(Concept c) {
		assert c
		String enWord = c.firstTerm
		List<String> czWords = c.terms
				.findAll {it.lang == "cs"}
				*.term
				.findAll()
				.collectMany {String term->
					//Any word of the two-words phrase
					//wn.tokens(term).collect(Collectors.toList())
					wn.splitBrackets(term)
				}
				.findAll()
				.unique()
		println "$c $czWords"

		czWords // Each alt translation
				.collect { String czWord ->
					Map trnJson = trn.exampleJsonRun(enWord, czWord)
					return trn.extractExamples(trnJson)
				}
				.collectMany {it}
				.sort {Tuple2<String, String> a, Tuple2<String, String> b ->
					String sa= a[0]
					String sb= b[0]
					sa.length() <=> sb.length()
				}
				.findAll { Tuple2<String, String> t->
					String s = t[0]
					s.length() < maxLength
				}.collect { Tuple2<String, String> t->
					new Example().tap { 
						terms.add(Term.enTerm(t[0] as String))
						terms.add(Term.csTerm(t[1] as String))						
					}
				}
	}

	@Deprecated
	void saveSampleCandidatesFromTodoList(Path target = Paths.get("/tmp/work/examplesDraft.yaml")) {
		int ln=0
		Path rootPath = Paths.get("/tmp/work")
		rootPath.resolve("enSamples.txt").withPrintWriter(Helper.utf8) { PrintWriter enPw->
			rootPath.resolve("czSamples.txt").withPrintWriter(Helper.utf8) { PrintWriter czPw->
				todo
				.flatMap { Concept c->
					azureDictSampleForConcept(c).stream()
				}.forEachOrdered { Tuple2<String, String> sample ->
					enPw.println(sample[0])
					czPw.println(sample[1])
					ln++
				}
			}
		}
		println "Saved $ln lines to $rootPath"
	}

	@CompileStatic
	void loadFromAzureDict(Manager dbMan=dbMan) {	
		
		todo
		.limit(limit)
		.flatMap { Concept c->			
			List<Example> ex = azureDictSampleForConcept(c)
			if (!ex) {
				println "${color('No example found for', RED)} ${color(c.toString(), BOLD)}"
			}
			dbMan.save()
			Thread.sleep(sleep)
			return ex.stream()
		}
		.forEachOrdered { Example e ->
			dbMan.db.examples.add(e)
			e.location = dbMan.defaultExamplesLocation
			
		}
		dbMan.db.dedup()
		dbMan.save()
	}

	@Deprecated
	int reuseExisting(boolean loadSave=true) {

		if (loadSave) dbMan.load()
		List<Concept> noEx = findTodo()

		int added = 0
		for (Concept c in noEx) {
			List<Concept> samples = dbMan.conceptsByEnWordsInSample[c.firstTerm]
			if (samples) {
				Concept fromC = samples[0]
				println "'$c.firstTerm': ${fromC.examples.values()[0].term}"
				c.examples = fromC.examples.values()*.clone().collectEntries {Term t->[(t.term): t] }
				added++
			}
		}

		if (loadSave) dbMan.save()
		return added
	}

	void fromCorpus(String text) {

		dbMan.load()
		dbMan.withBestExample(text) { Example e, String sen, Set<String> com, Set<String> mis->

			if (!mis)  {
				println color(sen, WHITE)
				return
			}
			if (mis.size() > 2 || !e)  {
				print color(sen, RED)
				e = new Example().tap {
					terms.add(Term.enTerm(sen))
				}
				dbMan.db.examples.add(e)
				dbMan.save()
			} else {
				print sen
			}
			println "->${color(e?.firstTerm, BLUE)} ${color(mis.join(' '), MAGENTA)}"
		}
		dbMan.save()
	}


	@CompileStatic
	public static void main(String[] args) {
		new ExampleAppender().with {
			//a.run()

			//String tx = getClass().getResource('/sources/JingleBells.txt').text
			//String tx  = new File("/data/src/AnkiVocb/pkg/FiveLittleMonkeys/sentences.txt").text
			//String tx  = new File("/data/src/AnkiVocb/pkg/EverythingIsAwesome/sentences.txt").text
			//String tx  = new File("/data/src/AnkiVocb/pkg/DuckTales/sentences.txt").text
			String tx = '''
			available privacy general development local section security total download media including location account content provide sale credit categories advanced application
			topic comment financial below mobile login legal options status browse issue range request professional reference term original 
            common display daily natural official average technical region record environment district calendar update resource material written adult requirements via 
			cheap third individual plus usually percent fast function global subscribe various knowledge error currently construction loan taken friday lake basic response 
			practice holiday chat speed loss discount higher political kingdom storage across inside solution necessary according particular
			'''.split(/\s+/).reverse().join(" ")						
			dbMan.load()
			todoFromText(tx)
			loadFromAzureDict()



			//reuseExisting()
		}

		println "Done"
	}
}
