package vocb.pck

import static vocb.Ansi.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Map.Entry

import vocb.ConfHelper
import vocb.anki.crowd.Data2Crowd
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager
import vocb.ord.ConceptExtra
import vocb.ord.Order
import vocb.ord.OrderSolver

//@CompileStatic
public class Pack {

	Path destRootFolder = Paths.get("/tmp/work/pkg")
	Path packageRootPath = Paths.get("/data/src/AnkiVocb/pkg/")
	ConfHelper cfgHelper = new ConfHelper()
	@Lazy ConfigObject cfg = cfgHelper.config

	@Lazy List<String> allPackageNames = packageRootPath.toFile().listFiles().collect {it.name}

	@Lazy Map<String, PackInfo> allPackages = allPackageNames.collectEntries {String name->
		[name, new PackInfo(name:name, packageRootPath: packageRootPath, destRootFolder: destRootFolder)]}





	WordNormalizer wn = new WordNormalizer()

	@Deprecated
	LinkedHashSet<Concept> export = [] as LinkedHashSet
	LinkedHashSet<Example> exportExamples = [] as LinkedHashSet

	Order lastOrder



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
		exportConceptsWithDepc(dbMan.conceptsFromWordList(enWords), depth)
	}

	void addDependencies(int depth=1) {
		List<Concept> cp = new ArrayList(export)
		exportConceptsWithDepc(cp, depth)
	}

	@Deprecated
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
		dbMan.conceptsByEnWordsInSample
				.submap(export.collect{it.firstTerm})
				.sort {Entry<String, Set<Concept>> it->
					Set<String> words = wn.uniqueueTokens(it.key)
					int coverCount = export.count {it.firstTerm in words}
					coverCount+= dbMan.ignoreConcepts.count {it.firstTerm in words } //Count ignoring concepts as being exported
					int newWordsCount = words.size()-coverCount //Penalty for introducing new words
					//println "$it.key:  $coverCount-$newWordsCount"
					-coverCount+newWordsCount*3
				}
	}

	Map<String, Concept> findConceptsCandidatesForGivenExample(String example, boolean includeNonExported=false) {
		example = wn.normalizeSentence(example)
		Set<String> words = wn.uniqueueTokens(example)
		dbMan.conceptByFirstTerm
				.subMap(words)
				.findAll{ String word, Concept c-> c.state != "ignore" }
				.findAll{ String word, Concept c->
					includeNonExported || export.contains(c)
				}
		/*.findAll { String word, Concept c->
		 includeAlreadyHavingSample || wn.normalizeSentence(c.examplesByLang("en")[0]?.term) != example
		 }*/
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

	void doExport(PackInfo info) {
		assert info
		File pkgFile = info.packagePath.toFile()
		cfgHelper.extraLookupFolders.add(pkgFile)
		Data2Crowd d2c = new Data2Crowd (info : info, cfgHelper:cfgHelper)
		if (info.sentences) {
			exportSentences(info.sentences, d2c.dbMan)
			d2c.exportExamplesToCrowd(exportExamples)
		}
		
		if (info.wordList) {
			d2c.exportWordsToCrowd(info.wordList)
		}

		
		cfgHelper.extraLookupFolders.remove(pkgFile)

	}

	void exportSentences(String text, Manager dbMan) {
		assert text
		dbMan.withBestExample(text) { Example e, String sen, Set<String> com, Set<String> mis->

			if (!mis)  {
				println color(sen, BOLD)
				exportExamples.add(e)
				return
			}

			String col = NORMAL
			if (mis.size() > 1 || !e)  {
				col = RED
			} else {
				exportExamples.add(e)
			}
			println "${color(sen, col)} -> ${color(e?.firstTerm, BLUE)} ${color(mis.join(' '), MAGENTA)}"
		}
	}



	public static void main(String[] args) {
		
		int cnt = new Pack().allPackages.size()
		(0..cnt-1).stream().parallel()
				.forEach( {int p->
					if (p == 0)
					new Pack().with {
						//allPackages.each {println it}
						Collection<PackInfo>  pkgs
						synchronized (Pack.class ) {
							//pkgs = [allPackages.values()[p]]
							pkgs = [allPackages["MaryHadALittleLamb"]]
							//pkgs = [allPackages.values()[p]]
						}
						
						
						pkgs.each { PackInfo i->
							println '*'*100
							println "* ${i.name}"
							println '*'*100

							doExport(i)
						}

					}
				})

		println "Done"

	}
}
