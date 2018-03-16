require(dplyr)
require(tibble)
require(stringr)
require(data.table)

getLaunchData = function(sensordata.path='data/fall-2016/cs3114-sensordata.csv') {
  launchdata = fread(launchdata.path)
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
  methodmods = fread(methodmods.path)
  methodmods = methodmods %>%
    mutate(time = as.numeric(time)) # so that it can be merged with sensordata
  return(methodmods)
}

getEventStream = function(launchdata, methodmods, file) {
  if (!is.null(file)) {
    events = fread(file = file) %>% arrange(userName, assignment, time)
  }
  else if (!is.null(launchdata) & !is.null(methodmods)) {
    events = bind_rows(methodmods, launchdata) %>%
      arrange(userName, assignment, time)
  } else {
    stop('Please pass launchdata and methodmods, or a file path.')
  }
  
  return(events)
}

computeMethodCoevolution = function(eventStream) {
  eventStream %>% computeWorkSessions() %>%
    group_by(wsId, methodId) %>%
      summarise(
        changes = sum(modsToMethod[Type == 'MODIFY_SELF']), 
        changes.test = sum(modsToMethod[Type == 'MODIFY_TESTING_METHOD'])
      ) %>%
    mutate(
      balance.inWs = changes.test / (changes + changes.test),
      changes = NULL,
      changes.test = NULL
    ) %>% 
    group_by(methodId) %>%
      summarise(balance.mean = mean(balance.inWs), balance.median = median(balance.inWs)) %>%
    summarise(methodBalance = mean(balance.mean))
}

computeProjectCoevolution = function(eventStream) {
  eventStream %>% 
    mutate(wsCoevolution = testEditSizeStmt / (editSizeStmt + testEditSizeStmt)) %>%
    summarise(coevolution = mean(wsCoevolution))
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