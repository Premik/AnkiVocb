package vocb.ord



import java.nio.file.Path
import java.util.stream.IntStream
import java.util.stream.Stream

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
	
	List<Order> gen = []
	
	void spawn(int count = 100) {
		gen.addAll( (0..count).collect {ctx.createInitialOrder().mix() })
	}
	
	IntStream getRndIndexes() { ctx.rnd.ints(0, gen.size()) }
	
	Stream<Tuple2<Order,Order>> getPairs() {
		rndIndexes.mapToObj { Integer i->
			new Tuple2(gen[i], gen[ctx.rndConceptIndex])
		}
	}
	
	void crossSome(int count=10) {
		pairs.limit(count).forEach {Tuple2<Order,Order> p->
			println "${p}"
		}
	}

	

	//
	static void main(String... args) {
		//println "${vocb.ord.OrderSolver.class.hashCode()}"
		new OrderSolver().tap {
			spawn()
			//gen.each {println "${it}"}
			crossSome()

		}


	}


}
