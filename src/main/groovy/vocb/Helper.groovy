package vocb

import java.nio.charset.StandardCharsets
import java.text.Normalizer

import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import org.apache.groovy.io.StringBuilderWriter

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult


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

	public static GPathResult parseXml(String xmlString) {
		try {
			return new XmlSlurper().parseText( xmlString)
		}
		catch (Exception e) {
			return null
		}
	}
	
	
	public static String prettyFormatXml(String src) {
		//Based on groovy.xml.XmlUtils but omnits xml declr
		Source source = new StreamSource(new StringReader(src))
		//return xmlStr
		//return XmlUtil.serialize(xmlStr)


		TransformerFactory factory = TransformerFactory.newInstance();
		//println factory
		//factory.setAttribute("indent-number", 2);
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");


		Writer wrt = new StringBuilderWriter();
		transformer.transform(source, new StreamResult(wrt));
		return wrt.toString();


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

	/**
	 * Split a {@code String} at the first occurrence of the delimiter.
	 * Does not include the delimiter in the result.
	 * @param toSplit the string to split (potentially {@code null} or empty)
	 * @param delimiter to split the string up with (potentially {@code null} or empty)
	 * @return a two element array with index 0 being before the delimiter, and
	 * index 1 being after the delimiter (neither element includes the delimiter);
	 * or {@code null} if the delimiter wasn't found in the given input {@code String}
	 */
	public static Tuple2<String, String> splitBy(String toSplit, String delimiter) {
		if (!toSplit || !delimiter) {
			return null
		}
		int offset = toSplit.indexOf(delimiter)
		if (offset < 0) {
			return null
		}

		String beforeDelimiter = toSplit.substring(0, offset)
		String afterDelimiter = toSplit.substring(offset + delimiter.length())
		return new Tuple2<String, String>(beforeDelimiter, afterDelimiter)
	}
	
	public static BigDecimal roundDecimal(BigDecimal d, int n=2) {
		if (d == null) return null
		return d.setScale(n, BigDecimal.ROUND_HALF_UP);
	}
	
}


