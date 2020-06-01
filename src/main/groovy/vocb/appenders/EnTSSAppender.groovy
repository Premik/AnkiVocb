package vocb.appenders

import java.nio.file.Path
import java.util.concurrent.TimeUnit

import vocb.Helper
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term
import vocb.tts.AwsCliPollyTTS
import static vocb.Ansi.*

public class EnTSSAppender {

	String[] voices = [
		"Emma",
		"Amy",
		"Brian",
		"Joanna",
		"Matthew"
	]
	int voiceCounter = 0
	int limit = 5
	int sleep = 1000

	String getSomeVoice() {
		voiceCounter++
		voices[voiceCounter%(voices.length)]
	}

	AwsCliPollyTTS enTts = new AwsCliPollyTTS()
	Manager dbMan = new Manager()
	int i =0
	void runTerms() {
		dbMan.load()


		dbMan.withTermsByLang("en") {Concept c, Term t->
			if (c.terms.size() >3) {
				println color("Ignoring: $c", WHITE)
				return
			}
			if (!dbMan.linkedMediaExists(t.tts) ) {
				i++
				if (i > limit) {
					println color("Limit reached", RED)
					return
				}
				t.tts = dbMan.resolveMedia(t.term, "mp3", "en-terms") { Path path ->
					Process p=  enTts.synth(t.term, "neural", someVoice, "text", path.toString())
					Helper.printProcOut(p)
					p.waitFor(5, TimeUnit.SECONDS)
					dbMan.save()
					Thread.sleep(sleep)
				}
			}
		}
		dbMan.save()
	}

	void runExamples() {
		dbMan.load()
		List<Concept> noEx = dbMan.db.concepts.findAll {
			it.terms && it.state!="ignore" && it.firstTerm && it.examples
		}

		int i =0
		for (Concept c in noEx) {
			if (c.terms.size() >3 || c.examples.size()>2) {
				println "Ignoring: $c"
				continue
			}

			Term enSample = c.examples.values()[0]
			if (enSample.lang != 'en' || enSample.tts) {
				continue
			}
			i++
			if (i > limit) {
				println color("Limit reached", RED)
				break
			}
			String enWord = c.firstTerm

			String tts = enTts.SSMLEmphSubstr(enSample.term, enWord )

			enSample.tts = dbMan.resolveMedia(enSample.term, "mp3", "en-samples") { Path path ->
				Process p=  enTts.synth(tts, "neural", someVoice, "ssml", path.toString())
				Helper.printProcOut(p)
				p.waitFor(10, TimeUnit.SECONDS)
				dbMan.save()
				Thread.sleep(sleep)
			}
		}
		dbMan.save()
	}


	public static void main(String[] args) {
		EnTSSAppender a = new EnTSSAppender(limit:200)
		a.runTerms()
		a.runExamples()
		println "Done"
	}
}
