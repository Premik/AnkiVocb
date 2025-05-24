package vocb.appenders

import java.nio.file.Files
import java.nio.file.Path

import vocb.Helper
import vocb.aws.AwsTranslate
import vocb.azure.AzureTTs
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term
import vocb.tts.AwsCliPollyTTS
import vocb.tts.LocalTTS
import static vocb.Ansi.*

public class CsTSSAppender {

	//LocalTTS ttsCz = new LocalTTS()
	AzureTTs ttsCz = new AzureTTs()
	Manager dbMan = new Manager()
	int limit = 10
	WordNormalizer wn = new WordNormalizer()
	int sleep = 1000

	void synth() {
		dbMan.load()
		int i = 0;
		//dbMan.withTermsByLang("cs", false) {Concept c, Term t->
		(dbMan.db.conceptsByLang("cs") + dbMan.db.examplesByLang("cs")).each  {Term t->			
			if (i >limit) {
				println color("Limit reached", RED)
				return
			}
			
			Closure synthIt = { Path p ->
				//ttsCz.synth(t.term, "violka", p.toString() )
				ttsCz.synth(t.term, p.toString() )
				i++;
				dbMan.save()
				Thread.sleep(sleep)
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
		new CsTSSAppender().with {
			limit = 1
			synth()
		}
		println "Done"
	}
}
