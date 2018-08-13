package com.linecorp.falcon.mysql

import scala.util.Try
import com.twitter.finagle.mysql._

trait RowImplicits {

  implicit class RichRow(row: Row) {

    def get[A: ValueDecoder](column: String): Try[A] =
      row.indexOf(column) match {
        case Some(x) => ValueDecoder[A].from(row.values(x))
        case None    => ValueDecoder.fail(s"column not found")
      }

    def as[A: RowDecoder]: Try[A] = RowDecoder[A].from(row)

  }

}
