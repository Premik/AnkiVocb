package vocb.aws

import java.util.concurrent.TimeUnit

import vocb.Helper

public class AwsTranslate {

	String trn(String text, String srcLang="en", String destLang="cs") {
		List<String> cmd =[
			'aws',
			'translate',
			'translate-text',
			'--source-language-code',
			srcLang,
			'--target-language-code',
			destLang,
			'--text',
			text
		]

		println cmd
		Process p = cmd.execute()
		p.waitFor(5, TimeUnit.SECONDS)
		if (p.exitValue() != 0)  {
			Helper.printProcOut(p)
			throw new IllegalArgumentException("Error code ${p.exitValue()}.")
h		}
		return Helper.parseJson(p.text)?.TranslatedText
	}



	static void main(String... args) {
		println new AwsTranslate().trn("Foo bar.")
	}
}
