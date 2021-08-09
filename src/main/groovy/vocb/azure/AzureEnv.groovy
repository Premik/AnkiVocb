package vocb.azure

import static vocb.Helper.utf8

import vocb.conf.ConfHelper

import static vocb.Ansi.*

public class AzureEnv {

	ConfHelper cfgHelper = new ConfHelper()
	@Lazy ConfigObject cfg = cfgHelper.config

	String lastClientId = "df5978d6-eb16-4b61-aa72-d89c665dd8ef"
	

	private Map<String,String> buildHttpHeaders(String azureKey) {
		Map<String,String> ret = ["Ocp-Apim-Subscription-Key": azureKey, "Content-Type": "application/json"]
		if (lastClientId) ret += ["X-MSEdge-ClientID": lastClientId]
		return ret
	}

	public Map<String,String> getImgSearchHttpHeaders() {
		buildHttpHeaders(cfg.azure.imageSearch.key)
	}

	public Map<String,String> getDictLookupHttpHeaders() {
		buildHttpHeaders(cfg.azure.dictLookup.key)
	}
	
	public Map<String,String> getTtsTokenHttpHeaders() {
		buildHttpHeaders(cfg.azure.tts.key) + ["Content-Type": "application/x-www-form-urlencoded"]
	}
	
	public Map<String,String> ttsHeaders(String token, contentType="application/ssml+xml") {
		Map<String,String> ret = [
			"Authorization": "Bearer $token", 
			"Content-Type": contentType,
			"X-Microsoft-OutputFormat": cfg.azure.tts.outFormat?:"audio-16khz-64kbitrate-mono-mp3"]
		if (lastClientId) ret += ["X-MSEdge-ClientID": lastClientId]
		return ret
	}
	
	
	public String urlParam(String key, Object val, String prefx="&") {
		if (val == null || val == "") return ""
		return "$prefx$key=${URLEncoder.encode(val.toString(), utf8)}"
	}
	
	URL tokenUrl(String regionId="northeurope")  {
		"https://${regionId}.api.cognitive.microsoft.com/sts/v1.0/issueToken".toURL()
	}

	//https://docs.microsoft.com/en-us/rest/api/cognitiveservices-bingsearch/bing-web-api-v7-reference
	URL imageSearchUrl(String q, int count, String imageType, String license)  {
		String baseUrl = cfg.azure.imageSearch.baseUrl
		"$baseUrl/images/search${urlParam('q', q, '?')}${urlParam('count', count)}${urlParam('imageType', imageType)}${urlParam('license', license)}".toURL()
	}

	//https://docs.microsoft.com/en-us/azure/cognitive-services/translator/reference/v3-0-translate
	URL translateUrl(String from="en", String to="cs")  {
		String baseUrl = cfg.azure.dictLookup.baseUrl
		"$baseUrl${urlParam('from', from)}${urlParam('to', to)}".toURL()
	}

	//https://docs.microsoft.com/en-us/rest/api/cognitiveservices/translatortext/translator/dictionaryexamples
	URL dictExampleUrl(String from="en", String to="cs")  {
		String baseUrl = cfg.azure.dictExample.baseUrl
		"$baseUrl${urlParam('from', from)}${urlParam('to', to)}".toURL()
	}
	
	//https://docs.microsoft.com/en-us/azure/cognitive-services/speech-service/rest-text-to-speech
	URL ttsUrl()  {		
		"https://${cfg.azure.tts.region}.tts.speech.microsoft.com/cognitiveservices/v1".toURL()
		             
	}
	
	URL getTTSListVoiceUrl()  {		
		"https://${cfg.azure.tts.region}.tts.speech.microsoft.com/cognitiveservices/voices/list".toURL()
		             
	}
	
	
}
