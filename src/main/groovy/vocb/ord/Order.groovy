package vocb.ord

public class Order {
	
	SolvingContext ctx
	
	ConceptExtra[] ord
		
	@Lazy int freqFitness = {
		assert ctx
		assert ord
		//ctx.freqIdealOrder.ord.
		0
	}()
	
	int[] deltasFrom(Order o) {
		assert o?.ord
		assert ord.size() == o.ord.size()
		
		
	}
	
	int getFitness() {
		
	}
	
	public ConceptExtra getAt(int i) {
		assert ord
		return ord[i]
	}

	@Override
	public String toString() {		
		"${ord?.collect{it.c.firstTerm} }"
	}
	
	@Override
	public boolean equals(Object obj) {	
		if (super.equals(obj)) return true
		if (!(obj instanceof Order)) return false
		return ord == ((Order)obj).ord 
	}
	
	@Override
	public int hashCode() {
		return ord.hashCode()
	}
}
