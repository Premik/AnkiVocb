package vocb.appenders

import groovy.transform.CompileStatic
import vocb.Helper
import vocb.corp.Corpus
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term

@CompileStatic
public class FreqAppender {

	@Lazy Corpus corp = Corpus.buildDef()


	Manager dbMan = new Manager()
	int sleep =500


	void run() {
		dbMan.load()


		
		dbMan.withTermsByLang("en") {Concept c, Term t->
			if (!c.freq) {
				c.freq = corp[t.term]				
			}
		}
		/*dbMan.db.concepts.each {
			it.freq =corp[it.firstTerm]
		}*/
		
		dbMan.save()
	}


	public static void main(String[] args) {
		FreqAppender a = new FreqAppender()
		a.run()

		println "Done"
	}
}
