package vocb.ord

import java.nio.file.Paths

import org.apache.groovy.parser.antlr4.GroovyParser.CreatedNameContext
import org.junit.jupiter.api.Test

class OrderSolverTest {

	URL dbEntry = getClass().getResource('/vocb/data/orderingTest.yaml')


	@Lazy OrderSolver sol = {
		assert dbEntry
		File dbFile = new File(dbEntry.toURI()).tap {
			assert exists() : absolutePath
			assert isFile() : absolutePath
		}

		new OrderSolver(
				dbConceptFilename: dbFile.name,
				dbStoragePath:  Paths.get(dbFile.parentFile.toURI() )
				)
	}()

	@Test
	void setupTest() {
		sol.with {
			assert ctx.initialSelection
			assert ctx.concepts
		}
	}
	
	@Test
	void freqOrder() {
		sol.with {
			Order o = ctx.createInitialOrder()			
			assert o == ctx.createInitialOrder()
			assert ctx.freqIdealOrder != o
			 
			assert o.freqFitness < ctx.freqIdealOrder.freqFitness
		}
	}
}
