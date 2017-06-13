all:
	javac -cp src src/ca/ipredict/controllers/MainController.java
	javac -cp src src/ca/ipredict/controllers/SpiCeController.java

run: all
	java -cp src ca.ipredict.controllers.MainController ./datasets
spice: all
	java -cp src ca.ipredict.controllers.SpiCeController ./datasets
clean:
	find . -name "*.class" -type f -delete
