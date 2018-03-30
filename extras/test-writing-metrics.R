#! /usr/bin/env Rscript

require(dplyr)
require(tibble)
require(stringr)

getLaunchData = function(sensordata.path='data/fall-2016/cs3114-sensordata.csv') {
  launchdata = data.table::fread(launchdata.path)
  launchdata = launchdata %>% filter(Type == 'Launch') %>%
    select(userId, email, CASSIGNMENTNAME, time, Type, Subtype, LaunchType) %>%
    mutate(
      Subtype = ifelse(Subtype == "", LaunchType, Subtype),
      userName = str_replace(email, "(@.*$)", ""),
      email = NULL,
      LaunchType = NULL
    ) %>%
    rename(assignment = CASSIGNMENTNAME)
  return(launchdata)
}

getMethodModifications = function(methodmods.path='~/Desktop/event-stream.csv') {
  methodmods = data.table::fread(methodmods.path)
  methodmods = methodmods %>%
    mutate(time = as.numeric(time)) # so that it can be merged with sensordata
  return(methodmods)
}

getEventStream = function(launchdata, methodmods, file) {
  if (!is.null(file)) {
    events = data.table::fread(file = file) %>% arrange(userName, assignment, time)
  }
  else if (!is.null(launchdata) & !is.null(methodmods)) {
    events = bind_rows(methodmods, launchdata) %>%
      arrange(userName, assignment, time)
  } else {
    stop('Please pass launchdata and methodmods, or a file path.')
  }
  
  return(events)
}

# allEvents %>% group_by(userName, assignment) %>% do(computeMethodCoevolution(.))
computeMethodCoevolution = function(eventStream, inWs=TRUE) {
  methods = eventStream %>% computeMethodTestingEffort()
  lowerbound = quantile(methods$testing.effort, 0.4)
  
  if (inWs) {
    methodWs = eventStream %>% 
      computeWorkSessions() %>%
      right_join(methods, by = "methodId") %>%
      filter(testing.effort > lowerbound) %>%
      group_by(wsId, methodId) %>%
        summarise(
          changes = sum(modsToMethod[Type == 'MODIFY_SELF']), 
          changes.test = sum(modsToMethod[Type == 'MODIFY_TESTING_METHOD'])
        ) %>%
      mutate(
        balance.inWs = changes.test / (changes + changes.test)
      ) %>%
      group_by(methodId) %>%
        summarise(balance.median = median(balance.inWs)) %>%
      summarise(methodBalance.inWs.median = median(balance.median))
  } else {
    balance = methods %>% 
      filter(testing.effort > lowerbound) %>%
      summarise(methodBalance.median = median(testing.effort))
    
    return(balance)
  }
}

computeMethodTestingEffort = function(eventStream) {
  eventStream %>% 
    group_by(methodId) %>%
      summarise(
        changes = sum(modsToMethod[Type == 'MODIFY_SELF']),
        changes.test = sum(modsToMethod[Type == 'MODIFY_TESTING_METHOD'])
      ) %>% 
      mutate(
        testing.effort = changes.test / (changes + changes.test)
      )
}

# allEvents %>% group_by(userName, assignment) %>% computeProjectCoevolution()
computeProjectCoevolution = function(eventStream) {
  eventStream %>% 
    mutate(wsCoevolution = testEditSizeStmt / (editSizeStmt + testEditSizeStmt)) %>%
    summarise(
      meanCoevolution = mean(wsCoevolution),
      medianCoevolution = median(wsCoevolution),
      skewCoevolution = (3 * (meanCoevolution - medianCoevolution)) / sd(wsCoevolution),
      projectWideRatio = sum(testEditSizeStmt) / (sum(testEditSizeStmt) + sum(editSizeStmt)),
      numWs = n()
    )
}

computeWorkSessions = function(eventStream) {
  eventStream %>% 
    arrange(time) %>%
    mutate(
      time = as.integer(time / 1000),
      gap = c(0, diff(time) >= 3600),
      wsId = as.factor(cumsum(gap)),
      gap = NULL
    )
}

# allEvents %>% group_by(userName, assignment) %>% computeWorkBeforeTestCreation
computeWorkBeforeTestCreation = function(eventStream) {
  eventStream %>%
    mutate(
      commitTestInvoked = as.character(commitTestInvoked),
      commitDeclared = as.character(commitDeclared),
      dateDeclared = as.integer(dateDeclared / 1000), # to seconds
      dateTestInvoked = as.integer(dateTestInvoked / 1000) # to seconds
    ) %>%
    summarise(
      avgTestAdditions = mean(testAdditions, na.rm = T),
      avgTestRemovals = mean(testRemovals, na.rm = T),
      avgSolutionAdditions = mean(solutionAdditions, na.rm = T),
      avgSolutionRemovals = mean(solutionRemovals, na.rm = T),
      avgAdditions = mean(additions, na.rm = T),
      avgRemovals = mean(removals, na.rm = T),
      timeInterval = mean((dateTestInvoked - dateDeclared) / 3600) # difference in hours
    )
}

# allEvents %>% group_by(userName, assignment) %>% do(computeAverageRecency(.))
computeAverageRecency = function(eventStream) {
  eventStream %>% arrange(methodId, time) %>%
    group_by(methodId) %>%
      mutate(
        startTime = first(time, order_by = time),
        endTime = last(time, order_by = time),
        endEdit = sum(modsToMethod),
        changeLinePos = cumsum(modsToMethod),
        mappedTime = linMap(time, startTime, endTime),
        mappedLineTime = linMap(changeLinePos, 0, endEdit)
      ) %>%
      summarise(
        averageRecency = mean(mappedTime[Type == 'MODIFY_TESTING_METHOD']),
        lineRecency = mean(mappedLineTime[Type == 'MODIFY_TESTING_METHOD'])
      ) %>%
    summarise(
      averageRecency = mean(averageRecency, na.rm = TRUE),
      lineRecency = mean(lineRecency, na.rm = TRUE)
    )
}

linMap = function(x, domainMin, domainMax) {
  (x - domainMin) / (domainMax - domainMin)
}

filterMethods = function(eventStream) {
  eventStream %>% rowwise() %>% 
    mutate(
      pieces = str_split(methodId, ","),
      className = pieces[1],
      methodName = pieces[2],
      pieces = NULL
    ) %>%
    filter(
      !startsWith(methodName, "get"), # getters
      !startsWith(methodName, "set"), # setters
      methodName != className, # constructors
      is.na(str_extract(methodName, "print")), # printers
      is.na(str_extract(methodName, "dump")), # printers
      methodName != "toString", # printers
      methodName != "main" # main method
    )
}