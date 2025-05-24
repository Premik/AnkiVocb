package vocb.ord



import java.util.Map.Entry

import vocb.corp.Difficulty
import vocb.corp.Similarity
import vocb.data.Concept
import vocb.data.Manager


public class SolvingContext {

	Similarity sm = new Similarity()
	Difficulty dfc = new Difficulty()

	@Lazy Manager dbMan = {
		new Manager().tap {
			load()
		}
	}()

	@Lazy Concept[] initialSelection = {
		dbMan.db.concepts.findAll{ it.state != "ignore"}.take(50)
	}()
	
	@Lazy ConceptExtra[] concepts = {		
		//println "${this.class.hashCode()} ${OrderSolver.class.hashCode()}"
		initialSelection.collect {new ConceptExtra(c:it, solver:this)}
	}()
	
	@Lazy Order freqIdealOrder = {
		new Order(ord:concepts.sort { ConceptExtra a, ConceptExtra b->
			b.c.freq <=> a.c.freq
		})
	}()
	
	
	


}
