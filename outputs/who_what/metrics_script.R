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
jaccard_sim = function(one, another){
  df <- data.frame(A = as.numeric(one), B = as.numeric(another))
  return(c(JSim_00 = jaccard_00(df, 1), JSim_11 = jaccard_11(df, 1)))
}

#correlation coefficient (pearson)
pearson_cor = function(){
  
}

#for all folds which are available
average_jaccard_sim = function(){
  for (fold in 1:14){
    #access with fold ...
    # call above method as:
    # accard_sim(bible_char[[2]][1,2:358], bible_char[[2]][1,2:358])[1]
    # the last accesibility modifier can access the list of similarities, e.g. 11, 00, dist, sim .. etc.
  }
}

#for all folds which are available
average_pearson_cor = function(){
  for (fold in 1:14){
    
  }
}
