all:
	javac -cp src src/ca/ipredict/controllers/MainController.java

run: all
	java -Xmx8g -cp src ca.ipredict.controllers.MainController ./datasets
clean:
	find . -name "*.class" -type f -delete
