package vocb.tts

import static vocb.Helper.utf8
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import vocb.Helper
import vocb.HttpHelper
import vocb.azure.AzureEnv

public class AzureTTs {

	// http -v  https://api.cognitive.microsoft.com/bing/v7.0/images/search\?q\=sailing+dinghies\&mkt\=en-us\&answerCount\=3 Ocp-Apim-Subscription-Key:$AZURE_KEY

	AzureEnv azEnv = new AzureEnv()
	@Lazy HttpHelper httpHelper = new HttpHelper()

	JsonSlurper jsonSlurper = new JsonSlurper()
	JsonBuilder jsonBuilder = new JsonBuilder()



	@Lazy String token = {
		assert httpHelper
		ConfigObject cfg = azEnv.cfg.azure.tts
		URL url = azEnv.tokenUrl(cfg.region)
		//println "Getting token $url"
		String ret
		httpHelper.withUrlPostResponse(azEnv.ttsTokenHttpHeaders, url,  { InputStream inp ->
			ret =inp.text
		})
		return ret
	}()

	List<Map> listVoices() {
		List<Map> ret
		httpHelper.withUrlGetResponse(azEnv.ttsHeaders(token), azEnv.TTSListVoiceUrl,  { InputStream inp ->
			ret = jsonSlurper.parse(inp, utf8)
		})
		assert ret : "Got an empty response"
		return ret
	}

	public String SSMLWrap(String text) {
		"""\
		<speak version='1.0' xml:lang='en-US'>
		   <voice xml:lang='cs' name='$azEnv.cfg.azure.tts.voice'>
		   	$text
		   </voice>
		</speak>""".stripIndent()
	}

	void synth(String text, String outFile="/tmp/work/1.mp3") {
		File f= new File(outFile)
		String sw = SSMLWrap(text)
		httpHelper.withUrlPostResponse(azEnv.ttsHeaders(token), azEnv.ttsUrl(), sw) { InputStream res->
			 f.newOutputStream() << res
		}

	}


	static void main(String... args) {
		AzureTTs bs = new AzureTTs().with{
			//println Helper.jsonToString(listVoices())
			println  synth("Testovací text.")



		}
	}
}
