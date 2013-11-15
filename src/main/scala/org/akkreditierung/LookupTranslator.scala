package org.akkreditierung

import org.apache.commons.lang3.text.translate.{NumericEntityEscaper, EntityArrays, AggregateTranslator, CharSequenceTranslator}
import java.io.IOException
import java.io.Writer
import java.util.HashMap
import java.util
import org.apache.commons.lang3.StringEscapeUtils

object HTML4Escaper  {

  val ESCAPE_HTML4 = new AggregateTranslator(new LookupTranslator(EntityArrays.BASIC_ESCAPE), new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE), new LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE), NumericEntityEscaper.above(131))
  def escapeHtml4(input: String) = ESCAPE_HTML4.translate(input)
  def unescapeHtml4(input: String) = StringEscapeUtils.unescapeHtml4(input)
}

class LookupTranslator(lookup: Array[Array[String]]) extends CharSequenceTranslator {
  private final val lookupMap: HashMap[CharSequence, CharSequence] = new util.HashMap[CharSequence, CharSequence]()
  private final var shortest: Int = 0
  private final var longest: Int = 0

  var _shortest: Int = Integer.MAX_VALUE
  var _longest: Int = 0

  if (lookup != null) {
    for (seq <- lookup) {
      this.lookupMap.put(seq(0), seq(1))
      val sz: Int = seq(0).length
      if (sz < _shortest) {
        _shortest = sz
      }
      if (sz > _longest) {
        _longest = sz
      }
    }
  }
  shortest = _shortest
  longest = _longest

  /**
   * {@inheritDoc}
   */
  def translate(input: CharSequence, index: Int, out: Writer): Int = {
    var max: Int = longest
    if (index + longest > input.length) {
      max = input.length - index
    }
    {
      var i: Int = max
      while (i >= shortest) {
        {
          val subSeq: CharSequence = input.subSequence(index, index + i)
          val result: CharSequence = lookupMap.get(subSeq)
          if (result != null) {
            out.write(result.toString)
            return i
          }
        }
        ({
          i -= 1;
          i + 1
        })
      }
    }
    return 0
  }
}