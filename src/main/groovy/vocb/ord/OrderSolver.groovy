package vocb.ord



import java.nio.file.Path
import java.nio.file.Paths

import vocb.data.Manager


public class OrderSolver {

	Path dbStoragePath
	String dbConceptFilename

	@Lazy SolvingContext ctx = {
		new SolvingContext(dbMan:new Manager().tap {
			if (dbStoragePath) storagePath =dbStoragePath
			if (dbConceptFilename) conceptFilename= dbConceptFilename 
			load()
		})
	}()

	

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
