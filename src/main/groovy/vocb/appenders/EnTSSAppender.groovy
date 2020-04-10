package vocb.appenders

import java.nio.file.Path
import java.util.concurrent.TimeUnit

import vocb.Helper
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term
import vocb.tts.AwsCliPollyTTS

public class EnTSSAppender {

	AwsCliPollyTTS enTts = new AwsCliPollyTTS()
	Manager dbMan = new Manager()

	void run() {	
		dbMan.autoSave {
			dbMan.withTermsByLang("en") {Concept c, Term t->				
				if (!dbMan.linkedMediaExists(t.tts) ) {
					t.tts = dbMan.resolveMedia(t.term, "mp3") { Path path ->
						Process p=  enTts.synth(t.term, "neural", "Emma", path.toString())
						Helper.printProcOut(p)
						p.waitFor(5, TimeUnit.SECONDS)
					}
				}
			}
		}
	}


	public static void main(String[] args) {
		EnTSSAppender a = new EnTSSAppender()
		a.run()
		println "Done"
	}
}
