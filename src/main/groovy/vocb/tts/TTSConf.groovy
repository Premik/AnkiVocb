package vocb.tts;

import groovy.transform.AutoClone
import groovy.transform.Canonical

@Canonical
@AutoClone
public class TTSConf {
	String engine='neural'
	String voiceId="Emma"
}

@Canonical
@AutoClone
public class TTSTextMod {
	int speedChange = 0
	int volumeChange = 0
}


