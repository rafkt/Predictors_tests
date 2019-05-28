# Function returns the Jaccard index and Jaccard distance
# call like:
# df2 <- data.frame(
#IDS = c(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), 
#CESD = c(1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
# jaccard(df2, 1)
# JSim     JDist 
# 0.1860465 0.8139535 

jaccard_11 <- function(df, margin) {
  if (margin == 1 | margin == 2) {
    M_00 <- apply(df, margin, sum) == 0
    M_11 <- apply(df, margin, sum) == 2
    if (margin == 1) {
      df <- df[!M_00, ]
      JSim <- sum(M_11) / nrow(df)
    } else {
      df <- df[, !M_00]
      JSim <- sum(M_11) / length(df)
    }
    JDist <- 1 - JSim
    return(c(JSim = JSim, JDist = JDist))
  } else break
}


jaccard_00 <-function(df, margin) {
  if (margin == 1 | margin == 2) {
    M_00 <- apply(df, margin, sum) == 0
    M_11 <- apply(df, margin, sum) == 2
    if (margin == 1) {
      df <- df[!M_11, ]
      JSim <- sum(M_00) / nrow(df)
    } else {
      df <- df[, !M_11]
      JSim <- sum(M_00) / length(df)
    }
    JDist <- 1 - JSim
    return(c(JSim = JSim, JDist = JDist))
  } else break
}


readCSVintoMemory = function(filePattern){
  #Reads files given in the string above; the files can be in a form of a pattern
  setwd('/Users/rafaelktistakis/Repositories/Predictors_tests_another_git_clone/Predictors_tests/outputs/who_what')
  temp<-list.files(pattern=filePattern)
  myDataList <- lapply(temp,function(i){read.csv(i, header=FALSE)})
  return(myDataList)
}

#call like bible_char <- prepare("BIBLE_CHAR.*.csv", "sBP.BIBLE_CHAR.*.csv")
prepare = function(filePatternAll, filePattnerSBP){
  #Combine two R data_frames
  all <- readCSVintoMemory(filePatternAll)
  sbp <- readCSVintoMemory(filePattnerSBP)
  for (fold in 1:14){
    combined <- abind::abind(all[[fold]], sbp[[fold]], along=1)
    all[[fold]] <- combined
  }
  return(all)
}


#Give two predictor vectors and it will return the jaccard similarity of 00 and 11..
# example input: bible_char[[1]][1,2:358], bible_char[[2]][1,2:358]
# you can obtain the bible_char by calling prepare(...) ...
# replace 358 with the number of columns since it won't be the same for every dataset:
# ncol(bible_char[[1]]) ... 
jaccard_sim = function(one, another){
  df <- data.frame(A = as.numeric(one), B = as.numeric(another))
  return(c(JSim_00 = jaccard_00(df, 1), JSim_11 = jaccard_11(df, 1)))
}

#correlation coefficient (pearson)
# There is no need for different methon on one fold since it's a library call either way.
# For example run on:
# cor.test(as.numeric(bible_char[[1]][1, 2:358]), as.numeric(bible_char[[1]][2, 2:358]), method = "pearson")
# Again, get bible_char through prepare... 

#for all folds which are available
average_jaccard_sim = function(dataset, predictorA, predictorB){
  sum_00 = 0
  sum_11 = 0
  for (fold in 1:14){
    #access with fold ...
    # call above method as:
    # jaccard_sim(bible_char[[2]][1,2:358], bible_char[[2]][1,2:358])[1]
    # the last accesibility modifier can access the list of similarities, e.g. 11, 00, dist, sim .. etc.
    
    js = jaccard_sim(dataset[[fold]][predictorA, 2:ncol(dataset[[fold]])], bible_char[[fold]][predictorB, 2:ncol(dataset[[fold]])])
    sum_00 = sum_00 + js[1]
    sum_11 = sum_11 + js[3]
  }
  return(c(mean_JSim_00 = sum_00/14, mean_JSim_11 =sum_11/14))
}

#for all folds which are available
average_pearson_cor = function(dataset, predictorA, predictorB){
  sum_t = 0
  sum_df = 0
  sum_pvalue = 0
  for (fold in 1:14){
    pearson_cor = cor.test(as.numeric(dataset[[fold]][predictorA, 2:ncol(dataset[[fold]])]), as.numeric(dataset[[fold]][predictorB, 2:ncol(dataset[[fold]])]), method = "pearson")
    
    sum_t = sum_t + pearson_cor$statistic
    sum_df = sum_df + pearson_cor$parameter
    sum_pvalue = sum_pvalue + pearson_cor$p.value
  }
  return(c(t = sum_t/14, df = sum_df/14, p_value = sum_pvalue/14))
}

# The following functions, calculate usefull queries. Such queries can be in the form; "Somebody predicted correctly while somebody else not"..
# Each predictor's index in the data can be found with e.g. bible_char[[1]][1:8] which will return 
# [1] "DG"        "TDAG"      "CPT+"      "CPT"       "Mark1"     "AKOM"      "LZ78"      "CPT_App_0"
# This order should be the same for every dataset as we have it, since data was auto-generated through code experiments.
# Feel free to expand queries as needed later; all the functionality is included below. Also look at notion "usefull commands" for further commands
# that can be useful to me.. 
lossless_yes_lossy_no = function(dataset){
  av_per = 0
  #for (fold in 1:14){
  fold = 4
    sum = 0
    result <-           as.numeric(dataset[[fold]][3,2:ncol(dataset[[fold]])])
    result <- result | as.numeric(dataset[[fold]][4,2:ncol(dataset[[fold]])])
    result <- result | as.numeric(dataset[[fold]][8,2:ncol(dataset[[fold]])])
    
    result <- result & !as.numeric(dataset[[fold]][1,2:ncol(dataset[[fold]])])
    result <- result & !as.numeric(dataset[[fold]][2,2:ncol(dataset[[fold]])])
    result <- result & !as.numeric(dataset[[fold]][5,2:ncol(dataset[[fold]])])
    result <- result & !as.numeric(dataset[[fold]][6,2:ncol(dataset[[fold]])])
    result <- result & !as.numeric(dataset[[fold]][7,2:ncol(dataset[[fold]])])
    
    sum = sum(TRUE == result[1:length(result)])
    print(sum)
    
    av_per = av_per + sum/length(result)
  #}
  
    return(result) 
  #return(c(Percentage = av_per/1 * 100))
}

lossy_yes_lossless_no = function(dataset){
  av_per = 0
  #for (fold in 1:14){
  fold = 4
    sum = 0
    result <-          as.numeric(dataset[[fold]][1,2:ncol(dataset[[fold]])])
    result <- result || as.numeric(dataset[[fold]][2,2:ncol(dataset[[fold]])])
    result <- result || as.numeric(dataset[[fold]][5,2:ncol(dataset[[fold]])])
    result <- result || as.numeric(dataset[[fold]][6,2:ncol(dataset[[fold]])])
    result <- result || as.numeric(dataset[[fold]][7,2:ncol(dataset[[fold]])])
    
    result <- result & !as.numeric(dataset[[fold]][3,2:ncol(dataset[[fold]])])
    result <- result & !as.numeric(dataset[[fold]][4,2:ncol(dataset[[fold]])])
    result <- result & !as.numeric(dataset[[fold]][8,2:ncol(dataset[[fold]])])
    
    sum = sum(TRUE == result[1:length(result)])
    print(sum) 
    av_per = av_per + sum/length(result)
  #}
  
  return(c(Percentage = av_per/1 * 100))
}

cptPlus_yes_subSeq_no = function(dataset){
  av_per = 0
  for (fold in 1:14){
    sum = 0
    result <-          as.numeric(dataset[[fold]][3,2:ncol(dataset[[fold]])])
    result <- result & !as.numeric(dataset[[fold]][8,2:ncol(dataset[[fold]])])
    
    sum = sum(TRUE == result[1:length(result)])
    
    av_per = av_per + sum/length(result)
  }
  
  return(c(Percentage = av_per/14 * 100))
}

subseq_yes_cptPlus_no = function(dataset){
  av_per = 0
  for (fold in 1:14){
    sum = 0
    result <-          as.numeric(dataset[[fold]][8,2:ncol(dataset[[fold]])])
    result <- result & !as.numeric(dataset[[fold]][3,2:ncol(dataset[[fold]])])
    
    sum = sum(TRUE == result[1:length(result)])
    
    av_per = av_per + sum/length(result)
  }
  
  return(c(Percentage = av_per/14 * 100))
}

load_all_datasets = function(){
  Bms <<- prepare("BMS.*.csv", "sBP.BMS.*.csv")
  Sign <<- prepare("SIGN.*.csv", "sBP.SIGN.*.csv")
  Msnbc <<- prepare("MSNBC.*.csv", "sBP.MSNBC.*.csv")
  Bible_w <<- prepare("BIBLE_WORD.*.csv", "sBP.BIBLE_WORD.*.csv")
  Bible_c <<- prepare("BIBLE_CHAR.*.csv", "sBP.BIBLE_CHAR.*.csv")
  Kosarak <<- prepare("KOSARAK.*.csv", "sBP.KOSARAK.*.csv")
  Fifa <<- prepare("FIFA.*.csv", "sBP.FIFA.*.csv")
}

