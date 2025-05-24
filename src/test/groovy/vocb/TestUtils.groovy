package vocb
import static vocb.Ansi.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


public class TestUtils {
	
	public static Path testConfPath = Paths.get( TestUtils.getResource('/conf/test.conf').toURI())

	public static void compareString(String aText, String bText) {

		//Compare by lines to ignore line endings
		if (aText.readLines().collect{it.trim()} ==bText.readLines().collect{it.trim()}) {
			return
		}
		println aText
		String lastDiff = printLineDifference(aText, bText)
		assert false, "Mismatch around line:$lastDiff"
	}

	public static void compareFiles(Path a, Path b, int maxLines=2000) {
		compareFiles(a.toFile(), b.toFile(), maxLines)
	}
	public static void compareFiles(File a, File b, int maxLines=200) {
		String aText = a.text
		String bText = b.text
		//Compare by lines to ignore line endings
		if (a.text.readLines() == b.text.readLines()) {
			return
		}
		String name = "${new File(a.parent).name}/$a.name"
		def encIt = {File f->  URLEncoder.encode(f.toURI().toString(), "UTF-8") 	}
		println "Mismatch in file ${color(name, BOLD)} \n $a.absolutePath \n $b.absolutePath "
		println "bcompare '$a.absolutePath'  '$b.absolutePath'"
		String lastDiff = printLineDifference(aText, bText, maxLines)
		assert false, "File \n$a and \n$b are different at:$lastDiff"
	}

	public static String printLineDifference(String a, String b,int maxLines=200) {
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
				lastDiff = "$l \n'$lineA'\n'$lineB'"
			}
			if (l > maxLines) {
				println color("${'.'*20} SKIPPED ${'.'*20}", RED)

				break
			}
		}
		println color("-"*80, WHITE)
		return lastDiff
	}
	
	@Lazy
	public static Path projectRootPath = {
		assert TestUtils.getResource('/resourceRoot.txt')
		URL r = TestUtils.getResource('/resourceRoot.txt')
		Paths.get( TestUtils.getResource('/resourceRoot.txt').toURI())
	}()
}
