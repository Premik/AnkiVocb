package vocb.appenders

import java.nio.file.Path
import java.util.concurrent.TimeUnit

import vocb.Helper
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term
import vocb.tts.AwsCliPollyTTS

public class EnTSSAppender {
	
	String[] voices = ["Emma", "Amy", "Brian", "Joanna", "Matthew"]
	int voiceCounter = 0
	String getSomeVoice() {
		voiceCounter++
		voices[voiceCounter%(voices.length)]
	} 

	AwsCliPollyTTS enTts = new AwsCliPollyTTS()
	Manager dbMan = new Manager()

	void runTerms() {
		dbMan.load()
		
		dbMan.withTermsByLang("en") {Concept c, Term t->
			if (!dbMan.linkedMediaExists(t.tts) ) {
				t.tts = dbMan.resolveMedia(t.term, "mp3") { Path path ->
					Process p=  enTts.synth(t.term, "neural", someVoice, "text", path.toString())
					Helper.printProcOut(p)
					p.waitFor(5, TimeUnit.SECONDS)
					dbMan.save()
				}
			}
		}
	}
	
	void runExamples() {
		dbMan.load()
		List<Concept> noEx = dbMan.db.concepts.findAll {
			it.terms && it.state!="ignore" && it.firstTerm && it.examples 
		}

		int i =0
		for (Concept c in noEx) {
			
			Term enSample = c.examples.values()[0]
			if (enSample.lang != 'en' || enSample.tts) continue
			String enWord = c.firstTerm
			
			String tts = enTts.SSMLEmphSubstr(enSample.term, enWord )
			if (!dbMan.linkedMediaExists(tts) ) {
				enSample.tts = dbMan.resolveMedia(tts, "mp3") { Path path ->
					Process p=  enTts.synth(tts, "neural", someVoice, "ssml", path.toString())
					Helper.printProcOut(p)
					p.waitFor(10, TimeUnit.SECONDS)
					dbMan.save()
				}
			}
			break
									
		}
		
		
	}


	public static void main(String[] args) {
		EnTSSAppender a = new EnTSSAppender()
		//a.runTerms()
		a.runExamples()
		println "Done"
	}
}
