# INEZ
Written by Paul Heinemeyer 

Instruction(German):
1. Öffnen Sie das Proejekt mit Eclipse.
2. Installieren Sie die Dependecies mithilfe von Maven.
3. Führen Sie das Projekt über die Main-Klasse aus.

Hinweise:
- Sie können über das Textfeld Produkte zu Ihrer Einkaufsliste hinzufügen.
- Über die Optionen können Sie sich Vorschläge anzeigen und/oder Mengen ergänzen lassen. Das ganze wurde mit verschiedenen Eingabe-Optionen getestet: z.B (Milch, Apfelsaft, MIneralwasser, Cola)
- Es gibt immer eine Prefix-Menge(z.B 1x, 2x etc.) und eine normale Menge(z.B 1-Liter, 1L etc.)
- Prefix-Mengen werden immer automatisch ergänzt
- Desweiteren kann man über die Optionen Rechtschreibhilfe ein oder austellen.
- Die Unittest-Klasse heißt BrainTest.java und kann seperat über Eclipse ausgeführt werden.

Erklärung zum Algorhitmus für die Produkt-Vorschläge:
    Die erste Idee um gute Produkt-Vorschläge für ein Input zu bekommen, war einen
    Distance-Algorhitmus zu verwenden um Produkte zu finden, welche nah an der Eingabe liegen.
    Leider waren die Ergebnisse nur damit sehr ungenau. Deshlab kam die Überlegung auf nur Nomen zu
    vergleichen, da diese das Produkt letztentlich identifzieren. Dazu werden Nomen und andere
    Wortarten analysiert und entsprechend bewertet. Desto mehr Nomen in einem Produkt-Namen
    vorkommen, desto unwahrscheinlicher das es sich um das gesuchte Produkt handelt. Z.B
    bei Eingabe 'Milch', sollte nicht unbedingt das Produkt 'Cremedusche Milch & Honig' vorgeschlagen
    werden. Letztendlich ergeben sich die Ergebnisse aus dem Distanz-Algorhitmus und dem
    Wortart-Algorhitmus. 
    
Bei Rückfragen, schreiben Sie mir gerne an paul_heinemeyer@outlook.de
    
