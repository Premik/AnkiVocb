
function coverWord(w) {
    var placeholder = '-';
    var chrs = Array.from(w)
    var end = chrs.length;
    var start = 0;
    if (chrs.length > 1) start++;
    if (chrs.length > 4) end--;
    if (chrs.length > 7) start++;

    for (var i = start; i < end; i++) {
        chrs[i] = placeholder;
    }
    return chrs.join("");
}
function showHint() {
    
    var hints = document.getElementsByClassName('hint');
    for (var i = 0; i < hints.length; i++) {
        var covered = coverWord(hints[i].textContent);
        hints[i].textContent = covered;
        hints[i].classList.remove('hintCover');
    };
    
    var imgCover = document.getElementsByClassName('hintCover');
        for (var i = 0; i < imgCover.length; i++) {                
            imgCover[i].classList.add('hintUncover');
            imgCover[i].classList.remove('hintCover');
        }
    
    document.getElementById("hintButton").style.display = "none";
}
