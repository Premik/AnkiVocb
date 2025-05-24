package vocb
import static vocb.Ansi.*


public class TestUtils {

	public static void compareString(String aText, String bText) {

		//Compare by lines to ignore line endings
		if (aText.readLines().collect{it.trim()} ==bText.readLines().collect{it.trim()}) {
			return
		}
		println aText
		String lastDiff = printLineDifference(aText, bText)
		assert false, "Mismatch around line:$lastDiff"
	}

	public static void compareFiles(File a, File b) {
		String aText = a.text
		String bText = b.text
		//Compare by lines to ignore line endings
		if (a.text.readLines() == b.text.readLines()) {
			return
		}
		String name = "${new File(a.parent).name}/$a.name"
		println "Mismatch in file ${color(name, BOLD)} \n $a.absolutePath \n $b.absolutePath "
		String lastDiff = printLineDifference(aText, bText)
		assert false, "File \n$a and \n$b are different at:$lastDiff"
	}

	public static String printLineDifference(String a, String b) {
		List<String> al = a.readLines()
		List<String> bl = b.readLines()
		int lines = Math.min(al.size(), bl.size())
		String lastDiff ="None"

		println color("-"*80, WHITE)
		for (int l=0;l<=lines;l++) {
			def lineA="", lineB=""

			if (l < bl.size() ) lineB = bl[l]
			if (l < al.size()) lineA = al[l]
			println color(lineA, WHITE)
			if (lineA.trim() != lineB.trim()) {

				println "*"*80
				println color(lineA, RED)
				println color(lineB, RED)
				println "*"*80
				lastDiff = "$l"
			}
		}
		println color("-"*80, WHITE)
		return lastDiff
	}
}
