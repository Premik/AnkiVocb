package vocb.appenders

import static vocb.Ansi.*

import java.nio.file.Path
import java.util.concurrent.TimeUnit

import vocb.Helper
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager
import vocb.data.Term
import vocb.tts.AwsCliPollyTTS
import static vocb.Ansi.*

public class EnTSSAppender {

	String[] voices = ["Emma", "Amy", "Brian", "Joanna", "Matthew"]
	int voiceCounter = 0
	int limit = 5
	int sleep = 1000
	WordNormalizer wn = new WordNormalizer()

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
				String trm = wn.stripBracketsOut(t.term)
				t.tts = dbMan.resolveMedia(trm, "mp3", "en-terms") { Path path ->
					Process p=  enTts.synth(trm, "neural", someVoice, "text", path.toString())
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
		List<Example> todo = dbMan.db.examples.collect().findAll { !it[0].tts}

		println "Found ${color(todo.size().toString(), BOLD)} example terms with no tts"
		int i =0
		for (Example e in todo) {
			if (e.terms.size() >3 ) {
				println "Ignoring: ${color(e.firstTerm, RED)}"
				continue
			}

			assert e[0].lang == "en"
			i++
			if (i > limit) {
				println color("Limit reached", RED)
				break
			}
			//String enWord = e.firstTerm
			//

			e[0].tts = dbMan.resolveMedia(e.firstTerm, "mp3", "en-samples") { Path path ->
				
  			    String trm = wn.stripBracketsOut(e.firstTerm)
				//String tts = enTts.SSMLEmphSubstr(e.term, enWord )
				//Process p=  enTts.synth(tts, "neural", someVoice, "ssml", path.toString())
				Process p=  enTts.synth(trm, "neural", someVoice, "text", path.toString())
				Helper.printProcOut(p)
				p.waitFor(10, TimeUnit.SECONDS)
				dbMan.save()
				Thread.sleep(sleep)
			}
		}
		dbMan.save()
	}


	public static void main(String[] args) {
		new EnTSSAppender().with {
			limit = 50
			runTerms()
			runExamples()
		}
		println "Done"
	}
}
