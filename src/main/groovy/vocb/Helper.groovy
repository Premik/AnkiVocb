package vocb;

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

	public static String jsonToString(Object json) {
		String jsonString = JsonOutput.toJson(json)
		return JsonOutput.prettyPrint(jsonString)
	}

	public static Object cloneJson(Object jsonResult) {
		return new JsonSlurper().parseText(JsonOutput.toJson(jsonResult))

	}



}
