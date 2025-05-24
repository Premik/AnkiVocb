package vocb.azure

import java.nio.charset.StandardCharsets

import vocb.SearchData

public class AzureEnv {

	String AZURE_KEY_ENV="AZURE_KEY"
	String AZURE_ENDPOINT_ENV="AZURE_ENDPOINT"
	String defaultBaseUrl = "https://api.cognitive.microsoft.com/bing/v7.0"
	String lastClientId

	@Lazy String baseUrl = {
		String endpointUrl = System.getenv(AZURE_ENDPOINT_ENV)
		if (endpointUrl) return endpointUrl
		return "https://api.cognitive.microsoft.com/bing/v7.0"
	}()

	public String getAzureKey() {
		String key = System.getenv(AZURE_KEY_ENV)
		assert key: "Please export env variable $AZURE_KEY_ENV with the api key"
		return key
	}

	public Map<String,String> getHttpHeaders() {
		Map<String,String> ret = ["Ocp-Apim-Subscription-Key": azureKey]
		if (lastClientId) ret += ["X-MSEdge-ClientID": lastClientId]
		return ret
	}

	public String urlParam(String key, Object val, String prefx="&") {
		if (val == null || val == "") return ""
		String utf8=StandardCharsets.UTF_8.toString()
		return "$prefx$key=${URLEncoder.encode(val.toString(), utf8)}"
	}
	
	//https://docs.microsoft.com/en-us/rest/api/cognitiveservices-bingsearch/bing-web-api-v7-reference
	URL imageSearchUrl(String q, int count, String imageType, String license)  {
		"$baseUrl/images/search${urlParam('q', q, '?')}${urlParam('count', count)}${urlParam('imageType', imageType)}${urlParam('license', license)}".toURL()
	}
}
