package org.akkreditierung

import org.specs2.mutable.Specification
import org.akkreditierung.model.{Studiengang, Job}
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.text.translate.{NumericEntityEscaper, EntityArrays, AggregateTranslator, CharSequenceTranslator}

class StringEscapeUrilsSpec extends Specification {
  "The StringEscapeUrils" should {
    "escape to right html entity from" in {
      "unicode chars" in {
        HTML4Escaper.escapeHtml4("""
          \u0084Elektrotechnik, Informationstechnik und Technische Informatik\u0093 ist Bestandteil
          """) must beEqualTo("""
          &#132;Elektrotechnik, Informationstechnik und Technische Informatik&#147; ist Bestandteil
          """)
      }
    }
    "escape to right unicode char from" in {
      "html entity" in {
        HTML4Escaper.unescapeHtml4("""
          &#132;Elektrotechnik, Informationstechnik und Technische Informatik&#147; ist Bestandteil
          """) must beEqualTo("""
          \u0084Elektrotechnik, Informationstechnik und Technische Informatik\u0093 ist Bestandteil
          """)
      }
    }
  }
}
