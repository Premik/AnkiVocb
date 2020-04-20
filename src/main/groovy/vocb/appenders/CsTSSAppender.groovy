package vocb.appenders

import java.nio.file.Files
import java.nio.file.Path

import vocb.Helper
import vocb.aws.AwsTranslate
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term
import vocb.tts.AwsCliPollyTTS
import vocb.tts.LocalTTS

public class CsTSSAppender {

	LocalTTS ttsCz = new LocalTTS()
	Manager dbMan = new Manager()
	int limit = 10

	void run() {
		dbMan.load()
		int i = 0;
		dbMan.withTermsByLang("cs", true) {Concept c, Term t->
			if (c.terms.size() >3 || c.examples.size()>2) {
				//println "Ignoring: $c"
				return
			}
			if (i >limit) {

				return
			}

			
			t.tts = dbMan.resolveMedia(t.term, "mp3") { Path p ->
				ttsCz.synth(t.term, "violka", p.toString() )
				i++;
				dbMan.save()
			}
			
		}
		dbMan.save()
	}


	public static void main(String[] args) {
		CsTSSAppender a = new CsTSSAppender(limit:200)
		a.run()
		println "Done"
	}
}
