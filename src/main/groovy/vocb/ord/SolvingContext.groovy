package vocb.ord



import java.nio.file.Path
import java.util.Map.Entry

import vocb.corp.Difficulty
import vocb.corp.Similarity
import vocb.data.Concept
import vocb.data.Manager


public class SolvingContext {

	Similarity sm = new Similarity()
	Difficulty dfc = new Difficulty()
	Manager dbMan



	@Lazy List<Concept> initialSelection = {
		assert dbMan
		dbMan.db.concepts.findAll{ it.state != "ignore"}
	}()

	@Lazy ConceptExtra[] concepts = {		
		initialSelection.withIndex().collect { Concept ce, int i ->
			new ConceptExtra(c:ce, ctx:this, id:i)
		}
	}()

	@Lazy Order freqIdealOrder = {
		new Order(ctx:this, ord:concepts.sort { ConceptExtra a, ConceptExtra b->
			assert a?.c : a
			assert b?.c : b
			b.c.freq <=> a.c.freq
		})
	}()
	
	@Lazy Order freqWorstOrder= new Order(ord:freqIdealOrder.ord.reverse(), ctx:this) 
	
	@Lazy Order initialOrder = createInitialOrder()
	
	Order createInitialOrder() {
		return new Order(ord: concepts, ctx:this)
	}





}
