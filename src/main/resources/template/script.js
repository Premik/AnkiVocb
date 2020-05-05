

function coverWord(w) {
    var placeholder = '-';
    var chrs = Array.from(w)
    var end = chrs.length;
    var start = 0;
    if (chrs.length > 1) start++;
    if (chrs.length > 5) end--;
    if (chrs.length > 8) start++;

    for (var i = start; i < end; i++) {
        chrs[i] = placeholder;
    }
    return chrs.join("");
}
function showHint() {
    var hint = document.getElementById("hint");
    hint.innerText = coverWord("{{native}}");
    document.getElementById("hintButton").style.display = "none";
}
