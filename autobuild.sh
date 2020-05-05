#! /bin/bash
function block_for_change 
{
  inotifywait -r  -e modify,move,create,delete .
}

while block_for_change; do
  echo CHANGE
  #ansible builder -m import_role -a 'name=buildHtml'
  #wmctrl -c "ICS Tool" 
  chromium "file:///data/src/AnkiVocb/src/main/resources/template/Template.html" &
  
done
