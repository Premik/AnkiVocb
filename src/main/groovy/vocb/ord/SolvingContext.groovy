package vocb.ord



import vocb.corp.Difficulty
import vocb.corp.Similarity
import vocb.data.Concept
import vocb.data.Manager


public class SolvingContext {

	Similarity sm = new Similarity()
	Difficulty dfc = new Difficulty()


	Random rnd = new Random(123456)

	double getRndRate() {
		//Much higher chances to yield 0 or 1 exactly.
		switch (rnd.nextInt(5)) {
			case 0..1: return 0
			case 2: return rnd.nextDouble()
			case 3..4: return 1
		}
	}

	int getRndConceptIndex() {
		rnd.nextInt(concepts.size())
	}


	/*@Lazy List<Concept> initialSelection = {
		assert dbMan
		dbMan.db.concepts.findAll{ it.state != "ignore"}
	}()*/
	
	List<Concept> initialSelection = []

	@Lazy ConceptExtra[] concepts = {
		assert initialSelection : "Configure listof the Concepts first"
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

	@Lazy Map<String, ConceptExtra> byFirstTerm = {
		concepts.collectEntries {
			[it.c.firstTerm, it]
		}
	}()

	@Lazy Order freqWorstOrder= new Order(ord:freqIdealOrder.ord.reverse(), ctx:this)

	@Lazy Order initialOrder = createInitialOrder()

	Order createInitialOrder(int genNum=0) {
		return new Order(ord: concepts, ctx:this)
	}
	
	
	





}
