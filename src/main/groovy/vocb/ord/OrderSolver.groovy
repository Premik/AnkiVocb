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
	
	List<Order> gen = []
	
	void spawn(int count = 10) {
		gen.addAll( (0..count).collect {ctx.createInitialOrder().mix() })
	}

	

	//
	static void main(String... args) {
		//println "${vocb.ord.OrderSolver.class.hashCode()}"
		new OrderSolver().tap {
			spawn()
			gen.each {println "${it}"}

		}


	}


}
