import sbt._
import Keys._

import play.Project._

object ApplicationBuild extends Build {

    val appName         = "w3act"
    val appVersion      = "0.2"

    val appDependencies = Seq(
      javaCore,
      javaJdbc,
      javaEbean,
      "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
      "com.maxmind.geoip2" % "geoip2" % "0.7.0",
      "org.apache.commons" % "commons-email" % "1.3.2",
      "org.apache.commons" % "commons-lang3" % "3.3.2",
//      "org.apache.commons" % "commons-validator" % "1.4.0",
      "com.typesafe" %% "play-plugins-mailer" % "2.1.0",
      "org.mindrot" % "jbcrypt" % "0.3m",
      "commons-io" % "commons-io" % "2.3",
	  "com.rabbitmq" % "amqp-client" % "3.3.1",
	  "org.avaje" % "ebean" % "2.7.1"  
    )
    
    val main = play.Project(appName, appVersion, appDependencies).settings(
        // Add your own project settings here      
	    testOptions in Test ~= { args =>
	      for {
	        arg <- args
	        val ta: Tests.Argument = arg.asInstanceOf[Tests.Argument]
	        val newArg = if(ta.framework == Some(TestFrameworks.JUnit)) ta.copy(args = List.empty[String]) else ta
	      } yield newArg
	    },
    	ebeanEnabled := true
    )    

}
            
