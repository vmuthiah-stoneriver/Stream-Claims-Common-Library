//
// Built on Thu Apr 25 19:11:00 CEST 2013 by logback-translator
// For more information on configuration files in Groovy
// please see http://logback.qos.ch/manual/groovy.html

// For assistance related to this tool or configuration files
// in general, please contact the logback user mailing list at
//    http://qos.ch/mailman/listinfo/logback-user

// For professional support please see
//   http://www.qos.ch/shop/products/professionalSupport

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.WARN

scan()
appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "THREAD:%X{REQUEST_ID} %-5level %logger{36} - %msg%n"
  }
}
logger("org.hibernate.SQL", DEBUG)
logger("org.hibernate.type.descriptor.sql.BasicBinder", ALL)
logger("com.fiserv.isd.iip.core.data.query.rdbms.JDBCQuery", DEBUG)
logger("com.client.iip.integration.core.rules.ClientRulesUtils", INFO)

root(INFO, ["STDOUT"])

