package vocb.ord

public class Order {

	ConceptExtra[] ord
	
	int getFreqFitness() {
		
	}
	
	int getFitness() {
		
	}

	@Override
	public String toString() {
		"${ord.collect{it.c.firstTerm} }"
	}
}
