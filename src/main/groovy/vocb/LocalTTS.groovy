package vocb;

import java.nio.charset.StandardCharsets

public class LocalTTS {
	
	HttpHelper httpHelper = new HttpHelper()

	void synth(String text, String voiceId="violka", String outFile="/tmp/work/1.mp3") {
		assert voiceId
		assert outFile
		assert text
		String txtEnc = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())

		URL u = "http://bb1:8081/?text=$txtEnc&voice=$voiceId&format=mp3".toURL()
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
