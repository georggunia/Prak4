Enter src and decompress trace2.txt and trace3.txt
```
gzip -d filename.gz
```
After that, run make simulateall to simulate all traces (or make simulate1 to simulate only trace1)
```
make simulateall
```
```
make simulate1
```
If thers any Error message (Integer Argument expected) then you need to convert configurations (and maybe the traces) to the UNIX format 
```
dos2unix configurations.txt
```
```
dos2unix trace1.txt
``` 
(sometimes windows or wsl convert the files to a DOS format thats why that happens=
After that try again (maybe run make clean beforehand)
```
make clean
```
Correct Output for simualateall is in output4a
I.e output for trace1 is in [output4a](https://github.com/georggunia/Prak4/blob/14a488ef239859f1c1d40ba08db6552d93fb065c/src/output4a.txt#L1-L56) (till line 57) 


