package vocb.tts

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import vocb.Helper



public class AwsCliPollyTTS {

	String[] voices = [		
		//"Aditi",
		"Amy",
		"Aria",
		"Arthur",
		"Ayanda",
		"Brian",
		"Danielle",
		"Emma",
		"Geraint",
		"Gregory",
	//	"Ivy",
		"Joanna",
		"Joey",
		"Justin",
		"Kajal",
		"Kendra",
		"Kevin",
		"Kimberly",
		"Matthew",
		"Niamh",
		//"Nicole",
		"Olivia",
		//"Patrick",
		//"Raveena",
		//"Russell",
		"Ruth",
		"Salli",
		"Stephen",
	]

	Map<Integer, String> speeds = [(0-2):"x-slow", (0-1):"slow", 1:"fast", 2:"x-fast"]
	Map<Integer, String> vols = [(0-2):"-8dB", (0-1):"-6dB",1:"+6dB", 2:"+8dB"]

	TTSTextMod normalMod = new TTSTextMod(speedChange: -1) //Bit slower by default
	TTSTextMod empMod = new TTSTextMod(speedChange: -2, volumeChange: 1 ) //Emphasis even slower and louder
	TTSConf defaultConf = new TTSConf( engine:'standard', voiceId:"Emma")

	Process synth(String text, String engine='neural', String voiceId="Emma", String textType="text", String outFile="/tmp/work/1.mp3") {
		assert engine
		assert voiceId
		assert outFile
		List<String> cmd =[
			'aws',
			'polly',
			'synthesize-speech',
			'--output-format',
			'mp3',
			'--engine',
			engine,
			'--voice-id',
			voiceId,
			'--text-type',
			textType,
			'--text',
			text,
			outFile
		]

		println cmd
		cmd.execute()
	}

	public String SSMLWrapInner(String text, TTSTextMod mod) {
		String speed = speeds[mod.speedChange]
		String spdAttr = speed ? / rate="$speed"/ : ""
		String vol = vols[mod.volumeChange]
		String volAttr = vol ? / volume="$vol"/ : ""
		if (!spdAttr && !volAttr) return text
		"""<prosody$spdAttr$volAttr>$text</prosody>"""
	}

	public String SSMLWrap(String text, TTSTextMod mod=normalMod) {
		"""\
        <speak>
          ${SSMLWrapInner(text, mod)}
        </speak>""".stripIndent()
	}

	public String SSMLEmphSubstr(String text, String substr, TTSTextMod normalMod=normalMod, TTSTextMod highlMod=empMod) {
		assert text.toLowerCase().contains(substr.toLowerCase()) : "The '$text' doesn't contain the '$substr'"
		assert normalMod
		assert highlMod
		final def (String a, String w, String b) =  Helper.splitByWord(text, substr)
		assert w : "Failed to split '$text' by '$substr'"
		String innerSSML = SSMLWrapInner(w.trim(), highlMod)
		SSMLWrap(a.trim() + innerSSML + b.trim(), normalMod)
	}

	// Helper function to parse the dialog string
	static List<Map<String, String>> splitDialog(String dialog) {
		List<Map<String, String>> dialogParts = []
		String[] lines = dialog.split("\\r?\\n")
		lines.each { String line ->
			assert (line ==~ /^\s*(\w+):\s*"(.*)"$/ || line ==~ /^\s*(\w+):\s*(.*)$/)

			// Extract voice and text
			String voice = line.replaceAll(/^\s*(\w+):.*$/, '$1')
			String text = line.replaceAll(/^\s*(\w+):\s*"(.*)"$/, '$2').replaceAll(/^\s*(\w+):\s*(.*)$/, '$2')
			dialogParts.add([voice: voice, text: text])
		}
		return dialogParts
	}

	void synthDialog(String dialog, Path rootPath = Paths.get("/tmp/work")) {
		List<Map<String, String>> dialogParts = splitDialog(dialog)
		int index = 0

		dialogParts.each { Map<String, String> part ->
			String voice = part['voice']
			String text = part['text']
			String fileName = String.format("%02d-%s.mp3", index, voice)
			String outFile = rootPath.resolve(fileName).toString()

			// Call the existing synth function to create the audio file
			synth(text, "neural", voice, "text", outFile)

			index++
		}
	}





	static void main(String... args) {
		AwsCliPollyTTS tts = new AwsCliPollyTTS()

		tts.voices.each { String v->
			String tx = "Hello world."
			Process p = tts.synth(tx, "neural", v, "text", "/tmp/work/${v}.mp3")
			Helper.printProcOut(p)
			p.waitFor(5, TimeUnit.SECONDS)
		}
		return
		//Process p = tts.synth(tx, "neural", v, "text", "/tmp/work/${v}.mp3")
		String dialog = '''Gregory: "Mushroom soup is delicious."
							Ivy: "I don't think so. Mushrooms are poisonous."
							Gregory: "Not all of them. Some are edible."
							Ivy: Well, not me.
							Gregory: "You sure?"'''

		tts.synthDialog(dialog)

		//			Helper.printProcOut(p)
		//			p.waitFor(5, TimeUnit.SECONDS)

		//Ayanda
		//ssml

		/*
		 * <speak>
		 <prosody volume="-8dB">Hi! My</prosody><prosody rate="x-slow">name is</prosody><prosody volume="-8dB">Joanna.</prosody>
		 </speak>
		 */
		print "Done"
	}
}
