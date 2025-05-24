package vocb.appenders

import vocb.Helper
import vocb.HttpHelper
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
					Term enTrm = c.termsByLang("en")[0]
					if (!enTrm) return
						return !enTrm.pron
				}

		println words.join("\n")
	}

	void importPron(String pronCopied) {
		pronCopied.readLines()
				.collect{it?.trim()}
				.findAll()
				.collect { new Tuple2<String,String> (*it.split(/\s+/)) }
				.each { Tuple2<String,String> wp->
					assert wp.v1
					assert wp.v2
					Concept c=  dbMan.conceptByFirstTerm[wp.v1]
					if (!c) {
						println "$wp not in db"
						return
					}
					Term enTrm = c.termsByLang("en")[0]
					if (enTrm.pron) {
						assert enTrm.pron == wp.v2 : "Strange replacement"
						return
					}
					enTrm.pron = wp.v2
					println "${wp}"
				}
		dbMan.save()

	}


	public static void main(String[] args) {
		new PronAppender().tap {
			printWords()
			importPron """
				year 	jɪr
				spoke 	spoʊk
				bring	brɪŋ
			"""
		}

		println "Done"
	}
}
