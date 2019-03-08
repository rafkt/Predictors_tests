all:
	javac -cp src src/ca/ipredict/controllers/MainController.java

run: all
	java -Xms4g -Xmx14g -cp src ca.ipredict.controllers.MainController ./datasets
clean:
	find . -name "*.class" -type f -delete
