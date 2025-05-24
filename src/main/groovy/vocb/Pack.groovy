package vocb

import java.nio.file.Path
import java.nio.file.Paths
import java.util.Map.Entry

import vocb.anki.crowd.Data2Crowd
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.ord.ConceptExtra
import vocb.ord.Order
import vocb.ord.OrderSolver


//@CompileStatic
public class Pack {

	Path destFolder = Paths.get("/tmp/work")
	String pkgName ="basic0"

	@Lazy Path destPath = {
		destFolder.resolve(pkgName).tap {
			toFile().mkdirs()
		}
	}()

	@Lazy Data2Crowd d2c = new Data2Crowd (destCrowdRootFolder: destPath.toString()).tap {
		staticMedia.add "_${pkgName}Background.jpg"
	}

	@Lazy Manager dbMan = d2c.dbMan



	WordNormalizer wn = new WordNormalizer()

	LinkedHashSet<Concept> export = [] as LinkedHashSet
	Order lastOrder

	List<Concept> conceptsFrom(List<String> enWords) {
		enWords
				.findAll()
				.collect {it.toLowerCase()}
				.collect { dbMan.conceptByFirstTerm[it] }
				.findAll { it.state != 'ignore'}
	}

	void exportConceptsWithDepc(List<Concept> enConcepts, int depth=1) {
		if (depth <= 0) return

			if (depth > 1) {
				//Export dependencies first
				for (Concept c in enConcepts) {
					exportConceptsWithDepc(dependenciesOf(c), depth-1)
				}
			}
		export += enConcepts
	}

	void exportWordsWithDepc(List<String> enWords, int depth=1) {
		exportConceptsWithDepc(conceptsFrom(enWords), depth)
	}

	void addDependencies(int depth=1) {
		List<Concept> cp = new ArrayList(export)
		exportConceptsWithDepc(cp, depth)
	}

	List<Concept> dependenciesOf(Concept c) {
		assert c
		Set<String> allWords = wn.uniqueueTokens((c.termsByLang("en") + c.examplesByLang("en"))
				.collect {
					it.term}
				.join(" "))

		allWords
				.collectMany { String w ->
					Map m = dbMan.conceptByFirstTerm
					[
						m[w],
						m[w.endsWith('s') ? w.dropRight(1) : w + 's'] //Add/remove s for crude sing/plur
					]
				}
				.findAll()
				.findAll {it.state != 'ignore'}
				.minus(c)
	}

	void exportByOrigin(String origin) {
		export.addAll(dbMan.conceptsByOrigin[origin].findAll {it.state != 'ignore'} )
	}

	void filterByStars(List<Integer> starsToTake) {
		export = export.findAll { Concept c->
			dbMan.numberOfStarsFreq(c.freq) in starsToTake
		} as LinkedHashSet
	}

	void sort() {
		OrderSolver os = new OrderSolver().tap {
			maxGens = 5000
			maxPopsize = 10000
			crossAtOnce = 200
			spawnNew = 200
			dbStoragePath = d2c.dbMan.storagePath
			initialSelection = export as List<Concept>
			//loadInitialSelection(new File("/data/src/AnkiVocb/pkg/JingleBells/order.yaml").newReader())
		}
		/*os.runEpoch(5)
		lastOrder = os.bestFirst[0]*/
		lastOrder = os.ctx.createInitialOrder()
		lastOrder.load(Paths.get("/data/src/AnkiVocb/pkg/JingleBells/order.yaml"))
		export = lastOrder.ord.collect {ConceptExtra ce ->ce.c } as LinkedHashSet
	}

	void printExport() {
		if (lastOrder) OrderSolver.printDetails(lastOrder)
			else OrderSolver.printDetails(export)
	}

	Map<String, Set<Concept>> findBestExamples() {
		dbMan.conceptsByEnSample
				.collectEntries {String normSent, Set<Concept> cps->
					[normSent, cps.intersect(export) ] //Only concept for export
				}
				.findAll {it.value?.size() >0}
				.sort {Entry<String, Set<Concept>> it->
					Set<String> words = wn.uniqueueTokens(it.key)
					int coverCount = export.count {it.firstTerm in words}
					coverCount+= dbMan.ignoreConcepts.count {it.firstTerm in words } //Count ignoring concepts as being exported
					int newWordsCount = words.size()-coverCount //Penalty for introducing new words
					//println "$it.key:  $coverCount-$newWordsCount"
					-coverCount+newWordsCount*3
				}
	}

	Map<String, Concept> findConceptsCandidatesForGivenExample(String example, boolean includeAlreadyHavingSample=false, boolean includeNonExported=false) {
		example = wn.normalizeSentence(example)
		Set<String> words = wn.uniqueueTokens(example)
		dbMan.conceptByFirstTerm
				.subMap(words)
				.findAll{ String word, Concept c-> c.state != "ignore" }
				.findAll{ String word, Concept c->
					includeNonExported || export.contains(c)
				}
				.findAll { String word, Concept c->
					includeAlreadyHavingSample || wn.normalizeSentence(c.examplesByLang("en")[0]?.term) != example
				}
	}

	void forceBestExamplesReuse() {
		Set<Concept> replaced = [] as HashSet
		findBestExamples().each {String normExample, Set<Concept> cps->
			Concept sourceCp = cps.find { wn.normalizeSentence(it.examplesByLang("en")[0]?.term) == normExample }
			//assert sourceCp
			if (sourceCp) {
			findConceptsCandidatesForGivenExample(normExample).values()
					.each { Concept c->
						if (!(c in replaced)) {
						//println "$c ${c.examplesByLang('en')[0]?.term} <- ${sourceCp.examplesByLang('en')[0].term} "
							c.examples = sourceCp.examples.clone()
						}
						replaced.add(c)
					}
			}
		}
	}
	
	void export() {
		d2c.vocbModel.parser.deckName = pkgName
		d2c.exportToCrowd(export)
	}



	public static void main(String[] args) {
		new Pack().with {
			pkgName = "JingleBells"
			//exportWordsWithDepc(["I"], 1)
			exportByOrigin(pkgName)
			//findBestExamples().each {println "${it}"}

			/*findConceptsCandidatesForGivenExample("the horse was lean", false, true).each {k,v->
			 println "${v} ${v.examples.values()*.term}"
			 }*/

			//filterByStars (0..2)
			
			//addDependencies(1)
			forceBestExamplesReuse()

			//printExport()
			sort()
			printExport()
			//_JingleBellsBackground.jpg
			export()

		}

	}
}
