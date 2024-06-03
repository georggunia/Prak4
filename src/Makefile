
simulator.jar	*.java:	../libs/commons-cli-1.4.jar
	javac -encoding UTF8 -cp ../libs/commons-cli-1.4.jar  *.java
	jar --create --file=simulator.jar --manifest=Manifest *.class


simulate1:	simulator.jar trace1.txt
		./simulate.sh trace1.txt

simulate2:	simulator.jar trace2.txt
		./simulate.sh trace2.txt

simulate3:	simulator.jar trace3.txt
		./simulate.sh trace3.txt


simulateall:	simulate1 simulate2 simulate3


clean:
	rm -f *.class *.jar


