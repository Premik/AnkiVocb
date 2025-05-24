package vocb

import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.CompileStatic
import vocb.anki.crowd.Data2Crowd
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.ord.ConceptExtra
import vocb.ord.Order
import vocb.ord.OrderSolver


@CompileStatic
public class Pack {

	Path destFolder = Paths.get("/tmp/work")
	String pkgName ="basic0"

	@Lazy Path destPath = {
		destFolder.resolve(pkgName).tap {
			toFile().mkdirs()
		}
	}()

	@Lazy Data2Crowd d2c = new Data2Crowd(destCrowdRootFolder: destPath.toString() )

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
			println "${it.firstTerm.padRight(8)} ${it.examples.values()[0]?.term } "
		}
		println "[${export.size()}]"
	}

	public static void main(String[] args) {
		new Pack().with {
			exportWordsWithDepc(["I"], 5)
			printExport()
			sort()
			printExport()
			
		}

	}
}
