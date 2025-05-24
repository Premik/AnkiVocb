package vocb.translator

import groovy.json.JsonSlurper
import vocb.Helper

class PythonTranslation implements Translator {
	
    private static final String QUERY_TEMPLATE = '''
        Translate the English word '{}' to Czech. Provide 2-3 basic translations in JSON format. 
        Add additional translations only if the word has very distinct forms or meanings in Czech. Prioritize translations that are less similar to the literal English word.
        
        Examples:
        1. Translate the word `run`:
        {
        "translations": [
        "běžet",
        "spustit",
        "provozovat"
        ]
        }
        
        2. Translate the word `table`:
        {
        "translations": [
        "stůl",
        "tabulka"
        ]
        }
        
        3. Translate the word `light`:
        {
        "translations": [
        "světlo",
        "lehký"
        ]
        }
        
        4. Translate the word `match`:
        {
        "translations": [
        "zápas",
        "shoda",
        "zápalka"
        ]

        5. Translate the word `design`:
        {
        "translations": [
        "návrh"
        ]
        }
        
        Make sure to keep the examples concise but varied in meaning. This approach ensures clarity and relevance when dealing with English-to-Czech translations.
    '''.stripIndent()

    private final JsonSlurper jsonSlurper = new JsonSlurper()
    private final String pythonScript = "/wrk/dev/MyUtils/llm/llm_cli.py"

    

	@Override
    public List<String> translations(String word) {
        String query = QUERY_TEMPLATE.replace("{}", word)
        Process process = ["python", pythonScript, "--provider", "openrouter", "-q", query].execute()
        String output = process.text?.trim()
		output = output.replaceAll(/```json/, '')
        output = output.replaceAll(/```/, '')
        Map result = jsonSlurper.parseText(output)
		return result.translations.collect()
        
    }
}
