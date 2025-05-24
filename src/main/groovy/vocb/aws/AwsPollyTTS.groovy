package vocb.aws;

import java.util.concurrent.TimeUnit

import vocb.Helper

public class AwsCliPollyTTS {

	Process synth(String text, String engine='standard', String voiceId="Emma", String outFile="/tmp/work/1.mp3") {
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

	void printProcOut(Process proc) {
		StringBuffer b = new StringBuffer()
		proc.consumeProcessErrorStream(b)
		println(proc.text)
		System.err.println( b)
	}

	static void main(String... args) {
		AwsCliPollyTTS tts = new AwsCliPollyTTS()
		Process p = tts.synth("Hello world")
		p.waitFor(5, TimeUnit.SECONDS)
		Helper.printProcOut(p)
	}
}
