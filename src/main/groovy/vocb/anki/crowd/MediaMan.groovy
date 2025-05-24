package vocb.anki.crowd;

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import vocb.Helper

public class MediaMan {

	File mediaPath

	@Lazy Pattern niceWordPatter = {~ /\p{Lu}+/ }() //No digits in words etc

	MediaMan(File ankiCrowdExportPath) {
		assert ankiCrowdExportPath.exists()
		assert ankiCrowdExportPath.isDirectory()
		mediaPath = new File(ankiCrowdExportPath, "media")
		if (!mediaPath.exists()) mediaPath.mkdirs()
	}



	File fileForWord(String word, String ext="mp3") {
		new File(mediaPath, "${Helper.word2Key(word)}.$ext")
	}

	public boolean hasMediaForWord(String word, String ext="mp3") {
		return fileForWord(word, ext).exists()
	}
	
	static Path resizeImage(Path path, int width, int height) {
		Tuple2<String, String> fn = Helper.splitFileNameExt(path.fileName.toString())
		
		Path target = path.resolveSibling("${fn[0]}-$width.${fn[1]}")
		List<String> cmd =[
			'convert',
			path.toAbsolutePath(),
			'-resize',
			"${width}x$height>",
			target
		]

		println cmd
		Process p = cmd.execute()
		p.waitFor(10, TimeUnit.SECONDS)
		if (p.exitValue() != 0)  {
			Helper.printProcOut(p)
			throw new IllegalArgumentException("Error code ${p.exitValue()}.")
		}
		return target
	}


	static void main(String... args) {
		//MediaMan mm  = new MediaMan(new File('/tmp/work/cust'))
		MediaMan.resizeImage(Paths.get("/tmp/work/1.jpeg"), 100, 200)

	}
}
