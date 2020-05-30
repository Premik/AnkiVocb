package vocb;

// https://gist.github.com/tvinke/db4d21dfdbdae49e6f92dcf1ca6120de
// Ansi colors in Groovy
// Author: Ted Vinke
import static vocb.Ansi.*

def test() {
	println color("BOLD", AnsiConsole.BOLD)
	println color("ITALIC", AnsiConsole.ITALIC)
	println color("UNDERLINE", AnsiConsole.UNDERLINE)
	println color("BLINK", AnsiConsole.BLINK)
	println color("RAPID_BLINK", AnsiConsole.RAPID_BLINK)
	println color("REVERSE_VIDEO", AnsiConsole.REVERSE_VIDEO)
	println color("INVISIBLE_TEXT", AnsiConsole.INVISIBLE_TEXT)

	println color("RED", AnsiConsole.RED)
	println color("BLACK", AnsiConsole.BLACK)
	println color("BOLD", AnsiConsole.BOLD)
	println color("GREEN", AnsiConsole.GREEN)
	println color("YELLOW", AnsiConsole.YELLOW)
	println color("BLUE", AnsiConsole.BLUE)
	println color("MAGENTA", AnsiConsole.MAGENTA)
	println color("CYAN", AnsiConsole.CYAN)
	println color("WHITE", AnsiConsole.WHITE)
	println color("DARK_GRAY", AnsiConsole.DARK_GRAY)
	println color("LIGHT_BLUE", AnsiConsole.LIGHT_BLUE)
	println color("LIGHT_GREEN", AnsiConsole.LIGHT_GREEN)
	println color("LIGHT_CYAN", AnsiConsole.LIGHT_CYAN)
	println color("LIGHT_RED", AnsiConsole.LIGHT_RED)
	println color("LIGHT_PURPLE", AnsiConsole.LIGHT_PURPLE)
	println color("LIGHT_YELLOW", AnsiConsole.LIGHT_YELLOW)

	println(
			[
				'Look',
				AnsiConsole.LIGHT_RED,
				'ma',
				AnsiConsole.REVERSE_VIDEO,
				',',
				AnsiConsole.GREEN,
				'no ',
				AnsiConsole.MAGENTA,
				'hands!',
				AnsiConsole.LIGHT_YELLOW
			]
			.collate(2)
			.collect { pair ->
				color(pair.first(), pair.last())
			}.join(' ')
			)

}
/**
 * Small ANSI coloring utility.
 *
 * @see http://www.bluesock.org/~willg/dev/ansi.html
 * @see https://gist.github.com/dainkaplan/4651352
 */
public class Ansi {

	static final String NORMAL          = "\u001B[0m"

	static final String	BOLD            = "\u001B[1m"
	static final String	ITALIC	        = "\u001B[3m"
	static final String	UNDERLINE       = "\u001B[4m"
	static final String	BLINK           = "\u001B[5m"
	static final String	RAPID_BLINK	    = "\u001B[6m"
	static final String	REVERSE_VIDEO   = "\u001B[7m"
	static final String	INVISIBLE_TEXT  = "\u001B[8m"

	static final String	BLACK           = "\u001B[30m"
	static final String	RED             = "\u001B[31m"
	static final String	GREEN           = "\u001B[32m"
	static final String	YELLOW          = "\u001B[33m"
	static final String	BLUE            = "\u001B[34m"
	static final String	MAGENTA         = "\u001B[35m"
	static final String	CYAN            = "\u001B[36m"
	static final String	WHITE           = "\u001B[37m"

	static final String	DARK_GRAY       = "\u001B[1;30m"
	static final String	LIGHT_RED       = "\u001B[1;31m"
	static final String	LIGHT_GREEN     = "\u001B[1;32m"
	static final String LIGHT_YELLOW    = "\u001B[1;33m"
	static final String	LIGHT_BLUE      = "\u001B[1;34m"
	static final String	LIGHT_PURPLE    = "\u001B[1;35m"
	static final String	LIGHT_CYAN      = "\u001B[1;36m"

	static String color(String text, String ansiValue) {
		ansiValue + text + NORMAL
	}

}