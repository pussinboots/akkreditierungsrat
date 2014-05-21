package org.akkreditierung.model

import org.specs2.mutable.Specification
import scala.util.Properties

class DBSpec extends Specification {
  "DB object" should {
    "read database url from environment" in {
//      System.setProperty("CLEARDB_DATABASE_URL", "mysql://testurl")
//      DB.dbConfigUrl must beEqualTo("mysql://testurl")
      DB.dbConfigUrl must beEqualTo("mysql://root:mysql@127.0.0.1:3306/heroku_9852f75c8ae3ea1")
    }
    "parse heroku database to jdbc url" in {
      val dbConf = DB.parseDbUrl("mysql://root:password@127.0.0.1/heroku_9852f75c8ae3ea1")
      dbConf._1 must beEqualTo("jdbc:mysql://127.0.0.1/heroku_9852f75c8ae3ea1?useSSL=true&useUnicode=yes&characterEncoding=UTF-8&reconnect=true")
      dbConf._2 must beEqualTo("root")
      dbConf._3 must beEqualTo("password")
    }

    "parse default heroku database to jdbc url" in {
      val dbConf = DB.parseConfiguredDbUrl()
      dbConf._1 must beEqualTo("jdbc:mysql://127.0.0.1:3306/heroku_9852f75c8ae3ea1?useSSL=true&useUnicode=yes&characterEncoding=UTF-8&reconnect=true")
      dbConf._2 must beEqualTo("root")
      dbConf._3 must beEqualTo("mysql")
    }

    "parse heroku database to jdbc url" in {
      Properties.propIsSet("javax.net.ssl.keyStore") must beFalse
      Properties.propIsSet("javax.net.ssl.keyStorePassword") must beFalse
      DB.WithSSL()
      Properties.propOrNull("javax.net.ssl.keyStore") must beEqualTo("keystore")
      Properties.propOrNull("javax.net.ssl.keyStorePassword") must beEqualTo("")
    }
  }
}
