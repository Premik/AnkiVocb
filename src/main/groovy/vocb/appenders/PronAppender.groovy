package vocb.appenders

import vocb.Helper
import vocb.HttpHelper
import vocb.corp.Corpus
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term

public class PronAppender {

	HttpHelper httpHelper = new HttpHelper()
	WordNormalizer wn = new WordNormalizer()

	@Lazy Manager dbMan = {
		new Manager().tap {
			load()
		}
	}()

	void scrape() {
		//tophonetics.com
		//application/x-www-form-urlencoded
		String form = "text_to_transcribe=them&submit=Show+transcription&output_dialect=am&output_style=columns"
		httpHelper.withUrlPostResponse(azEnv.dictLookupHttpHeaders, trnUrl, Helper.jsonToString(rq)) {InputStream inp ->
			ret = jsonSlurper.parse(inp, utf8)

		}
	}


	void printWords() {
		List<String> words = dbMan.conceptByFirstTerm.keySet()
				.collectMany {wn.uniqueueTokens(it) }
				.findAll {
					Concept c=  dbMan.conceptByFirstTerm[it]
					if (!c) return false
					if (c.state == "ignore") return false
					return true
				}

		println "${words}"

	}


	public static void main(String[] args) {
		new PronAppender().tap {
			printWords()
		}

		println "Done"
	}
}
