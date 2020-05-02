package vocb.ord



import java.util.Map.Entry

import vocb.corp.Difficulty
import vocb.corp.Similarity
import vocb.data.Concept
import vocb.data.Manager


public class OrderSolver {
	
	SolvingContext ctx = new SolvingContext()

	
	Order createInitialOrder() {
		return new Order(ord: ctx.concepts)
	}

	//
	static void main(String... args) {
		//println "${vocb.ord.OrderSolver.class.hashCode()}"
		new OrderSolver().tap {
			ctx.concepts.take(5).each {println "${it}"}
			println createInitialOrder()
			println ctx.freqIdealOrder
			
			
			

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
