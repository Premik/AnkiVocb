package vocb

import java.nio.charset.StandardCharsets
import java.text.Normalizer

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

public class Helper {
	
	public static String utf8=StandardCharsets.UTF_8.toString()

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
		String noPunc = norm.replaceAll(/\p{InCombiningDiacriticalMarks}+/, "").replaceAll("[!@#.&\\\\/:*?\"<>'|=]", "")
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

	public static Tuple2<String, String> splitFileNameExt(String filename) {
		if (!filename.contains(".")) return [filename, ""] //No ext
		int dot = filename.lastIndexOf('.')
		return [
			filename.take(dot),
			filename.substring(dot+ 1)
		]
	}

	public static String indent(String s, int indent=1, String indChr=" " ) {
		s.replaceAll(/(?m)^/, indChr*indent)
	}

	public static String indentNextLines(String s, int indent=1, int ingnoreFirstLines=1, String indChr=" " ) {
		List<String> lines = []
		s.eachLine(0) {String l, int i ->
			lines+= "${i>=ingnoreFirstLines ? indChr*indent : ""}$l"			
		}
		return lines.join("\n")
	}



	public static int countIndent(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isWhitespace(s.charAt(i))) {
				return i
			}
		}
	}
}
