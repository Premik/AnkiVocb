package vocb.anki.crowd;

import java.text.Normalizer
import java.util.regex.Pattern

public class MediaMan {

	File mediaPath

	@Lazy Pattern niceWordPatter = {~ /\p{Lu}+/ }() //No digits in words etc

	MediaMan(File ankiCrowdExportPath) {
		assert ankiCrowdExportPath.exists()
		assert ankiCrowdExportPath.isDirectory()
		mediaPath = new File(ankiCrowdExportPath, "media")
		if (!mediaPath.exists()) mediaPath.mkdirs()
	}

	String word2Key(String word) {
		//https://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette
		//Normalizer.normalize(word, Normalizer.Form.NFKD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "_")
		Normalizer.normalize(word, Normalizer.Form.NFKD).replaceAll(/\p{InCombiningDiacriticalMarks}+/, "").replaceAll("[!@#.&\\\\/:*?\"<>'|]", "");
	}

	File fileForWord(String word, String ext="mp3") {
		new File(mediaPath, "${word2Key(word)}.$ext")
	}

	public boolean hasMediaForWord(String word, String ext="mp3") {
		return fileForWord(word, ext).exists()
	}


	static void main(String... args) {
		MediaMan mm  = new MediaMan('/tmp/work/cust')

	}
}
