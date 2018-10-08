def LOG_PATH = System.getProperty('logpath', 'logs')
new File(LOG_PATH).mkdirs()

appender("stdout", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d [%thread] [%level] %logger - %msg%n"
    }
}

appender("ROLLING", RollingFileAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d [%thread] [%level] %logger - %msg%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "${LOG_PATH}/log-%d{yyyy.MM.dd}.txt"
    }
}

root(DEBUG, ["stdout", "ROLLING"])
