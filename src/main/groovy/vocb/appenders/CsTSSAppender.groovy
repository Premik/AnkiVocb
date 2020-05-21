package vocb.appenders

import java.nio.file.Files
import java.nio.file.Path

import vocb.Helper
import vocb.aws.AwsTranslate
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term
import vocb.tts.AwsCliPollyTTS
import vocb.tts.LocalTTS

public class CsTSSAppender {

	LocalTTS ttsCz = new LocalTTS()
	Manager dbMan = new Manager()
	int limit = 10
	WordNormalizer wn = new WordNormalizer()

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
			
			Closure synthIt = { Path p ->
				ttsCz.synth(t.term, "violka", p.toString() )
				i++;
				dbMan.save()
			}
			String folder
			if (wn.uniqueueTokens(t.term).size() > 2) folder = "cs-samples"
			else folder = "cs-terms"  			
			if (!t.tts) {
				t.tts = dbMan.resolveMedia(t.term, "mp3", folder, synthIt)
			} else { //has medialink 
				//but doesn't exist
				if (!dbMan.linkedMediaExists(t.tts)) {
					synthIt(dbMan.mediaLinkPath(t.tts))
				}
			}
			
		}
		dbMan.save()
	}


	public static void main(String[] args) {
		CsTSSAppender a = new CsTSSAppender(limit:1)
		a.run()
		println "Done"
	}
}
