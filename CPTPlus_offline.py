# -*- coding: utf-8 -*-
"""
    Created in Thu March  22 10:47:00 2016
    
    @author: Remi Eyraud & Sicco Verwer
    
    Usage: python 3gram_baseline.py train_file prefixes_file output_file
    Role: learn a 3-gram on the whole sequences of train_file, then generates a ranking of the 5 most probable symbols for each prefix of prefixes_file, stores these ranking in output_file (one ranking per line, in the same order than in the prefix file)
    Example: python 3gram_baseline.py ../train/0.spice.train ../prefixes/0.spice.prefix.public 0.spice.ranking
"""




from numpy import *
from decimal import *
from sys import *
import math
import string

train_file = argv[1] #whole sequences, PAutomaC/SPiCe format
prefixes_file = argv[2] #all prefixes, PAutomaC/SPiCe format
output_file = argv[3] #to store the rankings on the prefixes
targets_file = argv[4] #contains all the answers for calculating the final score

from jpype import *
startJVM(getDefaultJVMPath(), "-ea")
CPT_Controller = JPackage('ca').ipredict.controllers
CPT = CPT_Controller.MainController

def find_proba(letter,target):
    for i in range(len(target)):
        if target[i]==letter:
            return float(target[i+1])
    return 0

def learn(train_file):
    """ Put here the learning part """

    CPT.prepareCPTPlus("/Users/rafaelktistakis/Repositories/spice_CPT-/SPiCe offline data-benchmark-framework/SPiCe_Offline/train/", train_file, 1)
    return list()

def next_symbols_ranking(model, prefix):
    """ Put here the ranking computation """
    return CPT.getPrediction(prefix)
    #return "3 2 1 0 -1"

def readset(f):
 sett = []
 line = f.readline()
 l = line.strip().split(" ")
 num_strings = int(l[0])
 alphabet_size = int(l[1])
 for n in range(num_strings):
     line = f.readline()
     l = line.strip().split(" ")
     sett = sett + [[int(i) for i in l[1:len(l)]]]
 return alphabet_size, sett


def list_to_string(l):
    s=str(l[0])
    for x in l[1:]:
        s+= " " + str(x)
    return(s)


print ("Start Learning")
model = learn(train_file)
print ("Learning Ended")

print("Start rankings computation")
#open prefixes
p = open( "/Users/rafaelktistakis/Repositories/spice_CPT-/SPiCe offline data-benchmark-framework/SPiCe_Offline/prefixes/" + prefixes_file,"r")
o = open(output_file, "w")
#get rid of first line of prefixes_file (needed since it contains nb of example, size of the alphabet)
p.readline()

for prefix in p.readlines():
    ranking = next_symbols_ranking(model, prefix)
    for i in ranking.split(" "):
        o.write(i + ' ')
    o.write('\n')

print("End of rankings computation")
p.close()
o.close()

shutdownJVM()

#calculating Score - Code copied from the score_computation.py in Code folder

r = open(output_file, "r")
t = open("/Users/rafaelktistakis/Repositories/spice_CPT-/SPiCe offline data-benchmark-framework/SPiCe_Offline/targets/" + targets_file, "r")

score = 0
nb_prefixes = 0
for ts in t.readlines():
    nb_prefixes += 1
    rs = r.readline()
    target = string.split(ts)
    ranking = string.split(rs)

    denominator = float(target[0])
    prefix_score = 0
    k=1
    for elmnt in ranking:
        if k == 1:
            seen = [elmnt]
            p = find_proba(elmnt,target)
            prefix_score += p/math.log(k+1,2)
        elif elmnt not in seen:
            p = find_proba(elmnt,target)
            prefix_score += p/math.log(k+1,2)
            seen = seen + [elmnt]
        k += 1
        if k > 5:
           break
#print(nb_prefixes, su)
    score += prefix_score/denominator
final_score = score/nb_prefixes
print(final_score)
r.close()
t.close()



