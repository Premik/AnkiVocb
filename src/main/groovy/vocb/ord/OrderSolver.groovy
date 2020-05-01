package vocb.ord



import java.util.Map.Entry

import groovy.transform.Canonical
import vocb.corp.Difficulty
import vocb.corp.Similarity
import vocb.data.Concept
import vocb.data.Manager



public class OrderSolver {

	Similarity sm = new Similarity()
	Difficulty dfc = new Difficulty()

	@Lazy Manager dbMan = {
		new Manager().tap {
			load()
		}
	}()

	@Lazy Concept[] initialSelection = {
		dbMan.db.concepts
	}()
	
	@Lazy ConceptExtra[] concepts = {
		initialSelection.collect {new ConceptExtra(c:it)}
	}()

	@Canonical
	public class ConceptExtra {
		Concept c

		@Lazy int difficulty = { dfc.conceptDifficulty(c) }()

		@Lazy LinkedHashMap<Concept, Double> similarities=  {			
			(initialSelection as Concept[])
			.collectEntries {[it, sm.conceptSimilarityNorm(c, it)]}
			.findAll {Concept c, Double d -> d > 0.15d && d <0.99 }
			.sort {Entry<Concept, Double>  a, Entry<Concept, Double>  b ->				
				b.value <=> a.value
			}.take(20)
		}()



		@Override
		public String toString() {
			"$c.firstTerm($difficulty, ${similarities.keySet().take(50).collect{it.firstTerm} })"
		}
	}







	//
	static void main(String... args) {
		new OrderSolver().tap {
			concepts.take(100).each {println "${it}"}
			
			

			/*def printSim = {Concept cp->
				dbMan.db.concepts.sort {Concept a, Concept b ->
					sm.conceptSimilarity(cp,b) <=> sm.conceptSimilarity(cp,a)
				}.take(30).each{
					println "$it.firstTerm ${sm.conceptSimilarityNorm(cp, it)}"
				}
			}
			printSim(dbMan.conceptByFirstTerm['and']) //3.5 cutof?
			*/
			

		}


	}


}
