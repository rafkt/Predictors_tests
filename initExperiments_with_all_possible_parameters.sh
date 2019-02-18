#!/bin/bash
#tweak this for the purpose of this experiment
#currently only this command works
# sed "s/parameters.put(\"noiseRatio\", \"1.0f\");/parameters.put(\"noiseRatio\", \"1.2f\");/g" SPICEProfile.java > tmp.java

# This script is generally a collection of attempts to replace code within java files. It has been reused several times for several experiments purposes. Always the
# main tool is bash and sed command. Look at the comments for several alterations of the script. 

clear

declare -a arr=("BIBLE_CHARProfile.java"
                "BIBLE_WORDProfile.java")

<<<<<<< HEAD
#deleted the rest of datasets - put them back if you wanna run full experiment or revert commit


#for ((i=0; i<=10; i+=3))
for i in `seq -f "%g" 0 0.1 1`
do 
    for j in `seq -f "%g" 0 1 10`
    do
        cd src/ca/ipredict/predictor/profile/   
        for d in "${arr[@]}"
        do
            sed "s/parameters.put(\"noiseRatio\".*/parameters.put(\"noiseRatio\", \"${i}f\");/g" $d > $d."_tmp$i$j".java
            mv $d."_tmp$i$j".java $d
            rm -rf $d."_tmp$i$j".java
            
            sed "s/parameters.put(\"minPredictionRatio\".*/parameters.put(\"minPredictionRatio\", \"${j}f\");/g" $d > $d."_tmp$i$j".java
            mv $d."_tmp$i$j".java $d
            rm -rf $d."_tmp$i$j".java    
            # mv tmp.java 

            sed "s/parameters.put(\"splitMethod\".*/parameters.put(\"splitMethod\", \"0\");/g" $d > $d."_tmp$i$j".java
            mv $d."_tmp$i$j".java $d
            rm -rf $d."_tmp$i$j".java

            sed "s/parameters.put(\"splitLength\".*/parameters.put(\"splitLength\", \"1f\");/g" $d > $d."_tmp$i$j".java
            mv $d."_tmp$i$j".java $d
            rm -rf $d."_tmp$i$j".java

        done
        cd ../../../../..
        make clean
        make all
        java -cp src ca.ipredict.controllers.MainController ./datasets
    done
done

#deleted run for splitlength values; revert this file if you want full experiment
=======
#commenting in for doing only the experiments involving splitMethod & splitLength
#
##for ((i=0; i<=10; i+=3))
#for i in `seq -f "%g" 0 0.1 1`
#do 
#    for j in `seq -f "%g" 0 1 10`
#    do
#        cd src/ca/ipredict/predictor/profile/   
#        for d in "${arr[@]}"
#        do
#            sed "s/parameters.put(\"noiseRatio\".*/parameters.put(\"noiseRatio\", \"${i}f\");/g" $d > $d."_tmp$i$j".java
#            mv $d."_tmp$i$j".java $d
#            rm -rf $d."_tmp$i$j".java
#            
#            sed "s/parameters.put(\"minPredictionRatio\".*/parameters.put(\"minPredictionRatio\", \"${j}f\");/g" $d > $d."_tmp$i$j".java
#            mv $d."_tmp$i$j".java $d
#            rm -rf $d."_tmp$i$j".java    
#            # mv tmp.java 
#
#            sed "s/parameters.put(\"splitMethod\".*/parameters.put(\"splitMethod\", \"0\");/g" $d > $d."_tmp$i$j".java
#            mv $d."_tmp$i$j".java $d
#            rm -rf $d."_tmp$i$j".java
#
#            sed "s/parameters.put(\"splitLength\".*/parameters.put(\"splitLength\", \"1f\");/g" $d > $d."_tmp$i$j".java
#            mv $d."_tmp$i$j".java $d
#            rm -rf $d."_tmp$i$j".java
#
#        done
#        cd ../../../../..
#        make clean
#        make all
#        java -cp src ca.ipredict.controllers.MainController ./datasets
#    done
#done
#uncomment here if you need to run the above section again


for i in `seq -f "%g" 0.8 0.1 1` #was 0 0.1 1`
do 
    for j in `seq -f "%g" 0 1 10` #was 0 1 10`
    do
        for l in `seq -f "%g" 0.1 0.1 0.9` #was 0.1 0.1 0.9`
        do
            cd src/ca/ipredict/predictor/profile/   
            for d in "${arr[@]}"
            do
                sed "s/parameters.put(\"noiseRatio\".*/parameters.put(\"noiseRatio\", \"${i}f\");/g" $d > $d."_tmp$i$j".java
                mv $d."_tmp$i$j".java $d
                rm -rf $d."_tmp$i$j".java
                
                sed "s/parameters.put(\"minPredictionRatio\".*/parameters.put(\"minPredictionRatio\", \"${j}f\");/g" $d > $d."_tmp$i$j".java
                mv $d."_tmp$i$j".java $d
                rm -rf $d."_tmp$i$j".java    
                # mv tmp.java 

                sed "s/parameters.put(\"splitMethod\".*/parameters.put(\"splitMethod\", \"1\");/g" $d > $d."_tmp$i$j".java
                mv $d."_tmp$i$j".java $d
                rm -rf $d."_tmp$i$j".java

                sed "s/parameters.put(\"splitLength\".*/parameters.put(\"splitLength\", \"${l}f\");/g" $d > $d."_tmp$i$j".java
                mv $d."_tmp$i$j".java $d
                rm -rf $d."_tmp$i$j".java

            done
            cd ../../../../..
            make clean
            make all
            java -cp src ca.ipredict.controllers.MainController ./datasets 
        done
    done
done

#run the same for different split legnths

>>>>>>> 96a3bb2c8a5caba24e50ecbec0bacd09097fc947



# declare -a arr=("100"
# 				"200"
# 				"300"
# 				"400")
# # 				"500"
# # 				"600"
# # 				"700"
# # 				"800"
# # 				"900")
# 				# "1000"
# 				# "1500"
# 				# "2000")


# for i in "${arr[@]}"
# do
#         echo $i

#         if [ "$i" -ne "100" ]; then
#                 sed "s/evaluator.addDataset(Format.QUEST$previous,-1);/evaluator.addDataset(Format.QUEST$i,-1);/g" src/ca/ipredict/controllers/MainController.java > tmp.java
#                 mv tmp.java src/ca/ipredict/controllers/MainController.java

#         fi

#         ant build
#         cd bin
#         java -Xmx12g -Xms12g -XX:-UseConcMarkSweepGC ca.ipredict.controllers.MainController

#         #mv testing_seq $(echo $i"_testing_seq")
#         #mv training_seq $(echo $i"_training_seq")

#         cd ..

# 	previous=$i

# done
