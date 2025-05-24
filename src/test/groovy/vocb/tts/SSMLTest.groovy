package vocb.tts

import org.junit.jupiter.api.Test

import groovy.xml.slurpersupport.GPathResult
import vocb.Helper

class SSMLTest {

	@Lazy AwsCliPollyTTS tts

	@Test
	void innerWrap() {
		def w = tts.&SSMLWrapInner
		assert w("text",new TTSTextMod()) == "text"
		assert w("text",new TTSTextMod(volumeChange: -1)) =='<prosody volume="-6dB">text</prosody>'
		TTSTextMod m = new TTSTextMod(volumeChange: -1, speedChange: 1)
		assert w("text",m) =='<prosody rate="fast" volume="-6dB">text</prosody>'
	}
	
	@Test
	void SSMLEmphSubstr() {
		String s = tts.SSMLEmphSubstr("The quick brown fox jumps over the lazy dog.", "brown fox")				
		
		GPathResult xml = Helper.parseXml(s)
		assert xml		
		println s		 
		assert xml.prosody.'@rate' == "slow"
		assert xml.prosody.prosody == "brown fox"		 
	}
	
	@Test
	void classSplit() {
		String s = tts.SSMLEmphSubstr("We were studying in class as usual.", "as")		
		GPathResult xml = Helper.parseXml(s)
		assert xml
		assert xml.prosody.prosody == "as" 
		println s		
	}
	
	@Test
	void splitStrange() {
		def (a,b,c) =  Helper.splitBy("Film was shot using hand-held camera.", "held")
		assert b == "held"
		println "'$a' '$b' '$c'" 
	}
	
	
	
	
	
	


}
