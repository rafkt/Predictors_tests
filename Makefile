all:
	javac -cp ".:simmetrics-core-4.1.1-jar-with-dependencies.jar:src" src/ca/ipredict/controllers/MainController.java
	#javac -cp src src/ca/ipredict/controllers/MainController.java

run: all
	java -Xmx8g -cp ".:simmetrics-core-4.1.1-jar-with-dependencies.jar:src" ca.ipredict.controllers.MainController ./datasets
	#java -cp src ca.ipredict.controllers.MainController ./datasets
clean:
	find . -name "*.class" -type f -delete
