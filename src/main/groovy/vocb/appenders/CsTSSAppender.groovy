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
import vocb.tts.AzureTTs
import vocb.tts.LocalTTS
import static vocb.Ansi.*

public class CsTSSAppender {
	
	//String[] voices = ["cs-CZ-AntoninNeural", "cs-CZ-VlastaNeural", "cs-CZ-Jakub"]
	String[] voices = ["cs-CZ-AntoninNeural", "cs-CZ-VlastaNeural"]
	//String[] voices = ["cs-CZ-AntoninNeural"]
	int voiceCounter = 0
	
	String getSomeVoice() {
		voiceCounter++
		voices[voiceCounter%(voices.length)]
	}

	//LocalTTS ttsCz = new LocalTTS()
	Object ttsCz = new AzureTTs()
	Manager dbMan = new Manager()
	int limit = 10
	WordNormalizer wn = new WordNormalizer()
	int sleep = 1000
	Set<String> includeOnlyTerms = [] as HashSet

	void synth() {
		dbMan.load()
		int i = 0
		//dbMan.withTermsByLang("cs", false) {Concept c, Term t->
		Closure filter = { Concept c->
			c.validationProfile.termRequiredFields.contains("tts")
		}
		Collection<Term> terms = dbMan.db.conceptsByLang("cs", filter) + dbMan.db.examplesByLang("cs")
		if (includeOnlyTerms) terms = terms.findAll {includeOnlyTerms.contains(it.term) } 
		for (Term t in terms) {
			if (i >limit) {
				println color("Limit reached", RED)
				break
			}

			String trm = wn.stripBracketsOut(t.term);
			Closure synthIt = { Path p ->
				ttsCz.synth(trm, p.toString(), someVoice )
				i++
				dbMan.save()
				Thread.sleep(sleep)
			}
			String folder
			if (wn.uniqueueTokens(trm).size() > 2) folder = "cs-samples"
			else folder = "cs-terms"
			if (!t.tts) {
				t.tts = dbMan.resolveMedia(trm, "mp3", folder, synthIt)
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
			limit = 200
			//ttsCz = new LocalTTS()
			//includeOnlyTerms = ["koutek", "kůň", "závodník" ]
			synth()
		}
		println "Done"
	}
}
