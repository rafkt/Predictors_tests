all:
	javac -cp ".:java-string-similarity-0.24-SNAPSHOT.jar:src" src/ca/ipredict/controllers/MainController.java

run: all
	java -cp ".:java-string-similarity-0.24-SNAPSHOT.jar:src" ca.ipredict.controllers.MainController ./datasets
clean:
	find . -name "*.class" -type f -delete
