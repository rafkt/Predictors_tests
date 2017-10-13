#!/bin/bash
#tweak this for the purpose of this experiment
#currently only this command works
# sed "s/parameters.put(\"noiseRatio\", \"1.0f\");/parameters.put(\"noiseRatio\", \"1.2f\");/g" SPICEProfile.java > tmp.java

clear

declare -a arr=("BIBLE.txt" "BMS1_spmf.txt" "BMS2.txt" "FIFA.txt" "Kosarak_converted.txt" "LEVIATHAN.txt" "MSNBC.txt" "NASA_access_log_Aug95.txt" "NASA_access_log_Jul95.txt" "SIGN.txt")



#for ((i=0; i<=10; i+=3))
for i in `seq -f "%g" 0.1 0.1 1`
do 
    for j in `seq -f "%g" 1 1 9`
    do       
        for d in "${arr[@]}"
        do
            sed "s/parameters.put(\"noiseRatio\", \"1.0f\");/parameters.put(\"noiseRatio\", \"1.2f\");/g" SPICEProfile.java > tmp.java
            mv tmp.java 
        done
    done
done


declare -a arr=("100"
				"200"
				"300"
				"400")
# 				"500"
# 				"600"
# 				"700"
# 				"800"
# 				"900")
				# "1000"
				# "1500"
				# "2000")


for i in "${arr[@]}"
do
        echo $i

        if [ "$i" -ne "100" ]; then
                sed "s/evaluator.addDataset(Format.QUEST$previous,-1);/evaluator.addDataset(Format.QUEST$i,-1);/g" src/ca/ipredict/controllers/MainController.java > tmp.java
                mv tmp.java src/ca/ipredict/controllers/MainController.java

        fi

        ant build
        cd bin
        java -Xmx12g -Xms12g -XX:-UseConcMarkSweepGC ca.ipredict.controllers.MainController

        #mv testing_seq $(echo $i"_testing_seq")
        #mv training_seq $(echo $i"_training_seq")

        cd ..

	previous=$i

done
