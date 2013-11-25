package org.akkreditierung

import java.sql.{Date, Timestamp}
import java.util.Calendar

object DateUtil {
  def nowDateTime() : Timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis)
  def nowDateTimeOpt() : Option[Timestamp] = Some(nowDateTime())
}
