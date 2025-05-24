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

	void run() {
		dbMan.load()
		int i = 0;
		dbMan.withTermsByLang("cs", true) {Concept c, Term t->
			i++;
			if (i < 10) {
				if (!dbMan.linkedMediaExists(t.tts) ) {
					t.tts = dbMan.resolveMedia(t.term, "mp3") { Path p ->
						ttsCz.synth(t.term, "violka", p.toString() )
						dbMan.save()
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
