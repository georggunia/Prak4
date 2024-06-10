In src reingehen und make simulate1 ausführen (am besten in linux)
```
make simulate1 
```
Falls ein Fehler (Integer Argument expected) kommt dann müssen trace1 und configurations in das UNIX format konvertiert werden
```
dos2unix trace1.txt
``` 
```
dos2unix configurations.txt
```
(ab und zu macht windows die txt files in DOS aus irgend einem Grund)

Danach wieder probieren (ggf davor make clean machen)
```
make clean
``` 

Der korrekte output für trace1 ist in [output4a](https://github.com/georggunia/Prak4/blob/14a488ef239859f1c1d40ba08db6552d93fb065c/src/output4a.txt#L1-L56) (also BIS zur zeile 57) 

Leider ist der echte output weit entfernt vom korrektem und ich weiß nicht warum

