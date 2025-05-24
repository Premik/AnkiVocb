/*
  
{{Front}}

<div class="hintPane">
<button id="hintButton" onclick="showHint()" type="button">Hint</button>
<div id="hint"></div>

</div>

<script>
  
 */
function coverWord(w) {	
	var placeholder = '_';
	var chrs = Array.from(w)	
	var end = chrs.length;
	var start = 0;
	if (chrs.length>1) start++;
	if (chrs.length>5) end--;
	if (chrs.length>8) start++;		
	
	for (var i=start;i<end;i++) {
		chrs[i] = placeholder;	
	}
	return chrs.join("");
}
function showHint() {
	var hint = document.getElementById("hint");
	hint.innerText = coverWord("{{Back}}");
         document.getElementById("hintButton").style.display = "none";
}


/*
 * console.log(coverWord("")); console.log(coverWord("a"));
 * console.log(coverWord("to")); console.log(coverWord("then"));
 * console.log(coverWord("continue"));
 * console.log(coverWord("x𝟘𝟙𝟚𝟛'word!!"));
 * console.log(coverWord("𝟘d!𝟘!"));
 */
