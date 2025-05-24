package vocb

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.text.Normalizer
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import groovy.io.FileType

import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import org.apache.groovy.io.StringBuilderWriter

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.text.GStringTemplateEngine
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult



public class Helper {

	public static String utf8=StandardCharsets.UTF_8.toString()
	public static GStringTemplateEngine templEngine = new GStringTemplateEngine()
	public static Tuple3 null3 = [null, null, null]

	static public void printProcOut(Process proc, int waitSec=0) {
		StringBuffer b = new StringBuffer()
		proc.consumeProcessErrorStream(b)
		if (waitSec > 0) {
			proc.waitFor(waitSec, TimeUnit.SECONDS)
		}
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


		TransformerFactory factory = TransformerFactory.newInstance()
		//println factory
		//factory.setAttribute("indent-number", 2);
		Transformer transformer = factory.newTransformer()
		transformer.setOutputProperty(OutputKeys.INDENT, "yes")
		transformer.setOutputProperty(OutputKeys.METHOD, "xml")
		transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml")
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")


		Writer wrt = new StringBuilderWriter()
		transformer.transform(source, new StreamResult(wrt))
		return wrt.toString()


	}

	public static String shortHash(Object o) {
		Integer.toHexString(o.hashCode())
	}

	public static String word2Key(String word, boolean appendHash=false) {
		//https://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette
		//Normalizer.normalize(word, Normalizer.Form.NFKD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "_")
		String norm = Normalizer.normalize(word, Normalizer.Form.NFKD)
		String noPunc = norm.replaceAll(/\p{InCombiningDiacriticalMarks}+/, "").replaceAll("[!@#.&\\\\/:*?\"<>'|=]", "")
		if (!appendHash) return noPunc
		return "$noPunc-${shortHash(word)}"
	}



	public static String jsonToString(Object json) {
		String jsonString = JsonOutput.toJson(json)
		return JsonOutput.prettyPrint(jsonString)
	}

	public static Object cloneJson(Object jsonResult) {
		return new JsonSlurper().parseText(JsonOutput.toJson(jsonResult))
	}

	public static Object objectToJson(Object domainObject) {
		return parseJson(jsonToString(domainObject))
	}

	public  static <T> List<T> padList(List<T> lst, T pad, int len, boolean trim=false) {
		if (!trim && lst.size() >=len) return lst
		return (lst + [pad]*len).take(len)
	}

	public static Tuple2<String, String> splitFileNameExt(String filename) {
		if (!filename.contains(".")) return [filename, ""] //No ext
		int dot = filename.lastIndexOf('.')
		return [filename.take(dot), filename.substring(dot+ 1)]
	}

	public static Tuple2<String, String> splitFileNameExt(Path p) {
		splitFileNameExt(p.fileName.toString())
	}

	public static String indent(String s, int indent=1, String indChr=" ", String ignorePfxs='#|$' ) {
		String pfRx = //
				s.replaceAll(/(?m)^(?!$ignorePfxs)/, indChr*indent)
	}

	public static String indentNextLines(String s, int indent=1, int ingnoreFirstLines=1, String indChr=" " ) {
		List<String> lines = []
		s.eachLine(0) { String l, int i ->
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
	public static Tuple3<String, String, String> splitBy(String toSplit, String delimiter) {
		if (!toSplit || !delimiter) {
			return null3
		}
		int offset = toSplit.toLowerCase().indexOf(delimiter.toLowerCase())
		if (offset < 0) {
			return null3
		}

		String beforeDelimiter = toSplit.substring(0, offset)
		String middle = toSplit.substring(offset , offset  +delimiter.length())
		String afterDelimiter = toSplit.substring(offset + delimiter.length())
		return new Tuple3<String, String, String>(beforeDelimiter, middle, afterDelimiter)
	}


	public static Tuple3<String, String, String> splitByRex(String toSplit, Pattern delimiter) {
		if (!toSplit || !delimiter) {
			return null3
		}
		String[] matches = toSplit.findAll(delimiter)
		if (!matches) return null3
		assert matches.size() == 1 : "Found multiple matches of the '$delimiter'. ${matches}"
		return splitBy(toSplit, matches[0])
	}

	public static Tuple3<String, String, String> splitByWord(String toSplit, String word) {
		if (!toSplit || !word) {
			return null3
		}

		String del = /\s.,;?!:"'/
		Pattern rx = ~/(?i)([$del]+|^)(${Pattern.quote(word)})([$del]+|$)/

		def (a,b,c) = splitByRex(toSplit, rx)
		if (a == null) return null3
		def (b1,b2,b3) = splitBy(b, word) //Don't include the special characters to the middle word but to the borders
		if (b1 == null) return null3
		return [a+b1, b2, b3+c]
	}



	public static BigDecimal roundDecimal(BigDecimal d, int n=2) {
		if (d == null) return null
		return d.setScale(n, BigDecimal.ROUND_HALF_UP)
	}

	public static String expandTemplate(String templText, ctx=[:]) {
		if (!templText) return ""
		Writable templ = templEngine.createTemplate(templText).make(ctx)
		String ret = templ.toString()
	}

	public static Process runCommand(String templatedCmd, ctx=[:], int maxWaitSeconds=5) {
		String cmd = expandTemplate(templatedCmd, ctx)
		assert cmd : "No templated cmld provided"
		println "Running $cmd"
		Process p = cmd.execute()
		if (p.waitFor(maxWaitSeconds, TimeUnit.SECONDS)) {
			if (p.exitValue() != 0)  {
				Helper.printProcOut(p)
				throw new IllegalArgumentException("Error code ${p.exitValue()}.")
			}
		}
		return p
	}
	//https://stackoverflow.com/questions/16656651/does-java-have-a-clamp-function
	public static <T extends Comparable<T>> T clamp(T val, T min, T max) {
		if (val.compareTo(min) < 0) return min
		else if (val.compareTo(max) > 0) return max
		else return val
	}

	public static <T extends Comparable<T>> T clamp01(T val) {
		return clamp(val, 0, 1)
	}


	public static String progressBar(BigDecimal p) {
		List<String> m = "▁▂▃▄▅▆▇█" as List
		m[clamp( p* m.size(), 0, m.size()-1)]
	}

	public static <T> void withEachPairInDistance(List<T> coll, int atDistance=1,
			@ClosureParams(value= FromString, options=["int,T,T"] )  Closure c) {
		assert atDistance > 0
		assert atDistance < coll.size()
		for (int i=0;i<coll.size()-atDistance;i++) {
			c(i, coll[i], coll[i+atDistance])
		}
	}

	public static  <T>  List<T>  cutPaste(int from, int to, List<T> col) {
		assert from >=0 && from < col.size()
		assert to >=0 && to < col.size()
		if (to == from) {
			return col
		}
		int d = Math.signum(to-from) as int
		int count = Math.abs(to-from)
		for (int i=0;i<count;i++) {
			col.swap(from,from+d)
			from+=d
		}
		return col
	}

	public static  <T>  List<T> reposition(List<T> col, Closure<Integer> newPos) {
		if (!col) return []
		int safeGr = col.size()*col.size()
		for (Integer i=0;i<col.size();safeGr--) {
			assert safeGr >0  : "Endles loop"
			Integer ni = newPos(col[i], i)
			if (i == ni) {
				i++
				continue //No change in position. Next pos
			}
			cutPaste(i, ni, col)
			if (ni > i) {
				//Moved forward. This item is a new one, rerun closure on this pos again in next cycle
				continue
			}
			//ni <i
			i = ni //Moved backwards, jump to this position a re-wind from there
		}
		return col
	}

	public static <T> void withAllPairs(List<T> coll, int limit=coll.size()-1,@ClosureParams(value= FromString, options=["int,T,T"] )  Closure c) {
		for (int i=limit-1;i>0;i--) {
			withEachPairInDistance(coll, i, c)
		}
	}

	public static BigDecimal lerp(BigDecimal point1, BigDecimal point2, BigDecimal alpha)
	{
		point1 + alpha * (point2 - point1)
	}

	public static String stripExt(String fn) {
		fn?.replaceFirst(~/\.[^\.]+$/, '')?: ""
	}

	public static String ankiInterpoaltion2GString(String st) {
		st = st.replaceAll(/\{\{/, /\$\{cfg.ankiVars.'/)
		st = st .replaceAll(/\}\}/, /'\}/)
		st = st .replaceAll(/\[sound:/, /\[/)
		return st
	}

	public static List<Path> matchingFiles(List<Path> roots, Object nameFilter) {
		List<Path> ret =[]
		roots.each { Path p->
			if (Files.isDirectory(p)) {
				p.toFile().traverse(type: FileType.FILES, nameFilter: nameFilter) { File f ->
					ret.add(f.toPath())
				}
			}
		}
		return ret
	}
	
	
	public static String sndField(String soundRef, boolean wrap) {
		if (!soundRef) return ""
		if (wrap) return "[sound:$soundRef]"
		return  soundRef
	}

	public static String imgField(String ref, boolean wrap) {
		if (!ref) return ""		
		if (wrap) return """<img src='$ref'>"""
		return ref
	}
	
	public static void setAndCheckDirty(Object owner, String propName, Object propValue) {
		MetaClass ownerMop = owner.metaClass
		if (propName != "dirty") {
			Object oldValue = ownerMop.getProperty(owner, propName)
			if (oldValue == propValue) return
			owner.@dirty = true
		}
		//ownerMop.setAttribute(owner, propName, propValue)
		//ownerMop.setAttribute(owner.getClass(), owner, propName, propValue, true, false)
		owner.@"$propName" = propValue
	}
	
	
	
	


}


