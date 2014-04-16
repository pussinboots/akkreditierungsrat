package org.akkreditierung.ui

import java.util.Locale

import org.specs2.mutable._
import org.apache.wicket.util.tester.{FormTester, WicketTester}
import org.akkreditierung.model.DB
import org.akkreditierung.test.{SlickDbBefore}
import scala.slick.session.Database
import Database.threadLocalSession

class WicketApplicationTest extends Specification {

  val dfLocale = Locale.getDefault()
  val optionIntConverter = new OptionIntConverter() 

  "OptionIntConverter" should {
    "convert string to Object instance" in {
	optionIntConverter.convertToObject("test string", dfLocale) must beEqualTo(Option("test string"))	
	optionIntConverter.convertToObject("", dfLocale) must beEqualTo(Option(""))
	optionIntConverter.convertToObject(null, dfLocale) must beEqualTo(None)
    }

    "convert Object to string" in {
	optionIntConverter.convertToString(Some("test string"), dfLocale) must beEqualTo("test string")
	optionIntConverter.convertToString(Some(""), dfLocale) must beEqualTo("")
	optionIntConverter.convertToString(None, dfLocale) must beEqualTo("")
    }

    "getTargetType" in {
    	optionIntConverter.getTargetType() must beEqualTo(classOf[Option[_]]) 
    }
  }
}
