package vocb.tts;

import java.nio.charset.StandardCharsets

import vocb.Helper
import vocb.HttpHelper

import static vocb.Helper.utf8

public class LocalTTS {
	
	HttpHelper httpHelper = new HttpHelper()
	TTSConf conf = new TTSConf( voiceId:"violka")

	void synth(String text, String outFile="/tmp/work/1.mp3", String voice) {
		assert conf.voiceId
		assert outFile
		assert text
		String txtEnc = URLEncoder.encode(text, utf8)

		URL u = "http://r9:8081/?text=$txtEnc&voice=$conf.voiceId&format=mp3".toURL()
		println u
		httpHelper.withUrlGetResponse(u) {
			new File(outFile) << it
		}

	}



	static void main(String... args) {
		LocalTTS tts = new LocalTTS()
		tts.synth("Dobrý den.")
		println "Done"
	}
}
