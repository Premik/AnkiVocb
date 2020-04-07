package vocb.appenders

import java.nio.file.Files
import java.nio.file.Path

import vocb.Helper
import vocb.LocalTTS
import vocb.aws.AwsCliPollyTTS
import vocb.aws.AwsTranslate
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term

public class CsTSSAppender {

	LocalTTS ttsCz = new LocalTTS()
	Manager dbMan = new Manager()

	void run() {
		dbMan.autoSave {
			dbMan.withTermsByLang("cs") {Concept c, Term t->
				if (!dbMan.linkedMediaExists(t.tts) ) {
					t.tts = dbMan.resolveMedia(t.term, "mp3") { Path p ->
						ttsCz.synth(t.term, "violka", p.toString() )
					}
				}
			}
		}
	}


	public static void main(String[] args) {
		CsTSSAppender a = new CsTSSAppender()
		a.run()
		println "Done"
	}
}
