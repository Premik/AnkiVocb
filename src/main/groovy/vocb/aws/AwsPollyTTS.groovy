package vocb.aws;

import java.util.concurrent.TimeUnit

import vocb.Helper

public class AwsCliPollyTTS {

	Process synth(String text, String engine='standard', String voiceId="Emma", String outFile="/tmp/work/1.mp3") {
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
			'--text',
			text,
			outFile
		]

		println cmd
		cmd.execute()
	}



	static void main(String... args) {
		AwsCliPollyTTS tts = new AwsCliPollyTTS()
		Process p = tts.synth("Hello world")
		p.waitFor(5, TimeUnit.SECONDS)
		Helper.printProcOut(p)
		/*
		 * <speak>
<prosody volume="-8dB">Hi! My</prosody><prosody rate="x-slow">name is</prosody><prosody volume="-8dB">Joanna.</prosody>

</speak>

		 */
	}
}
