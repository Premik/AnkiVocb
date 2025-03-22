package vocb.translator

interface Translator {
    /**
     * Looks up translations for a given English word 
     */
    List<String> translations(String word)
}
