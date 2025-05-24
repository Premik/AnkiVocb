package vocb;

import java.text.Normalizer

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

public class Helper {

	static public void printProcOut(Process proc) {
		StringBuffer b = new StringBuffer()
		proc.consumeProcessErrorStream(b)
		println(proc.text)
		System.err.println( b)
	}

	public static Object parseJson(String jsonString) {
		try {
			return new JsonSlurper().parseText(jsonString)
		}
		catch (Exception e) {
			return null
		}
	}

	public static String word2Key(String word, boolean appendHash=false) {
		//https://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette
		//Normalizer.normalize(word, Normalizer.Form.NFKD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "_")
		String norm = Normalizer.normalize(word, Normalizer.Form.NFKD)
		String noPunc = norm.replaceAll(/\p{InCombiningDiacriticalMarks}+/, "").replaceAll("[!@#.&\\\\/:*?\"<>'|]", "");
		if (!appendHash) return noPunc
		return "$noPunc-${Integer.toHexString(word.hashCode())}"
	}



	public static String jsonToString(Object json) {
		String jsonString = JsonOutput.toJson(json)
		return JsonOutput.prettyPrint(jsonString)
	}

	public static Object cloneJson(Object jsonResult) {
		return new JsonSlurper().parseText(JsonOutput.toJson(jsonResult))
	}

	public  static <T> List<T> padList(List<T> lst, T pad, int len, boolean trim=false) {
		if (!trim && lst.size() >=len) return lst
		return (lst + [pad]*len).take(len)
	}

	public static void withUrlGetResponse(Map hdrs=[:], URL u, Closure<BufferedInputStream> c) {
		BufferedInputStream b = u.newInputStream(
				/**/connectTimeout: 5000,
				/**/readTimeout: 10000,
				/**/requestProperties: ['Connection': 'close'] + hdrs)
		b.withCloseable(c)
	}
}
