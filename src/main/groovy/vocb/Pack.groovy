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
		staticMedia.add "${pkgName}"
	}

	@Lazy Manager dbMan = d2c.dbMan



	WordNormalizer wn = new WordNormalizer()

	LinkedHashSet<Concept> export = [] as LinkedHashSet

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
		OrderSolver os = new OrderSolver(initialSelection: export as List<Concept>).tap {
			maxGens = 1000
			maxPopsize = 15000
		}
		os.runEpoch(30)
		Order o = os.bestFirst[0]
		export = o.ord.collect {ConceptExtra ce ->ce.c } as LinkedHashSet
	}

	void printExport() {
		export.each {
			println "${it.firstTerm.padRight(8)} ${dbMan.starsOf(it)} ${it.examples.values()[0]?.term } ${it.origins} "
		}
		println "[${export.size()}]"
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
					println "$it.key:  $coverCount-$newWordsCount"
					-coverCount+newWordsCount*2
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
			assert sourceCp
			findConceptsCandidatesForGivenExample(normExample).values()
					.each { Concept c->
						if (!(c in replaced)) {
						println "$c ${c.examplesByLang('en')[0]?.term} <- ${sourceCp.examplesByLang('en')[0].term} "
						}
						replaced.add(c)
					}
		}
	}



	public static void main(String[] args) {
		new Pack().with {
			pkgName = "JingleBells"
			//exportWordsWithDepc(["I"], 1)
			exportByOrigin(pkgName)
			findBestExamples().each {println "${it}"}

			/*findConceptsCandidatesForGivenExample("the horse was lean", false, true).each {k,v->
			 println "${v} ${v.examples.values()*.term}"
			 }*/
			//forceBestExamplesReuse()
			return

			filterByStars (0..2)
			//addDependencies(2)

			printExport()
			//sort()
			//printExport()

		}

	}
}
