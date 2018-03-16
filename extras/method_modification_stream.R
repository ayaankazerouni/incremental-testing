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
  methodmods = fread('~/Desktop/event-stream.csv')
  methodmods = methodmods %>% rowwise() %>%
    mutate(pieces = str_split(project, "_")) %>%
    mutate(
      project = pieces[1],
      userName = pieces[2],
      assignment = str_replace_all(pieces[3], "([:digit:])", " \\1"),
      pieces = NULL,
      time = as.numeric(time)
    ) %>%
    rename(commitHash = commit)
  return(methodmods)
}
getEventStream = function(launchdata, methodmods, file) {
  if(!is.null(file)) {
    events = fread(file = file) %>%
      group_by(userName, assignment)
  } else if (!is.null(launchdata) & !is.null(methodmods)) {
    events = bind_rows(methodmods, launchdata) %>%
      arrange(userName, assignment, time) %>%
      group_by(userName, assignment)
  } else {
    stop("Specify file OR (launchdata AND methodmods).")
  }
  return(events)
}
events = getEventStream(file = "~/Desktop/event-stream.csv")
head(events)
getEventStream = function(launchdata, methodmods, file) {
  if (is.null(file) & is.null(launchdata) & is.null(methodmods)) {
    stop("Specify file | launchdata | methodmods")
  }
  if(!is.null(file)) {
    events = fread(file = file) %>%
      group_by(userName, assignment)
  } else if (!is.null(launchdata) & is.null(methodmods)) {
    events = launchdata %>% group_by(userName, assignment)
  } else if (is.null(launchdata) & !is.null(methodmods)) {
    events = methodmods %>% group_by(userName, assignment)
  } else (!is.null(launchdata) & !is.null(methodmods)) {
    events = bind_rows(methodmods, launchdata) %>%
      arrange(userName, assignment, time) %>%
      group_by(userName, assignment)
  }
  return(events)
}

getEventStream = function(launchdata, methodmods, file) {
  if (is.null(file) & is.null(launchdata) & is.null(methodmods)) {
    stop("Specify file | launchdata | methodmods")
  }
  
  if(!is.null(file)) {
    events = fread(file = file) %>%
      group_by(userName, assignment)
  } else if (!is.null(launchdata) & is.null(methodmods)) {
    events = launchdata %>% group_by(userName, assignment)
  } else if (is.null(launchdata) & !is.null(methodmods)) {
    events = methodmods %>% group_by(userName, assignment)
  } else {
    events = bind_rows(methodmods, launchdata) %>%
      arrange(userName, assignment, time)
  }
  
  return(events)
}