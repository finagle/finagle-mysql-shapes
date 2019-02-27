package com.linecorp.finagle.mysql.shapes

import scala.util.{Try, Success, Failure}
import com.twitter.finagle.mysql._
import shapeless._

package object syntax {
  /**
    * Enriches
    * [[https://twitter.github.io/finagle/docs/com/twitter/finagle/mysql/Row.html com.twitter.finagle.mysql.Row]]
    * instances with methods to decode to a specific type.
    */
  implicit class RichRow(val row: Row) extends AnyVal {

    /**
      *  Decode a column into the specified type.
      */
    def get[A: ValueDecoder](column: String): Try[A] =
      row.apply(column) match {
        case Some(value) => ValueDecoder[A].from(value)
        case None        => ValueDecoder.fail(s"column not found")
      }

    /**
      *  Decode a row into the specified type.
      */
    def as[A: RowDecoder]: Try[A] = RowDecoder[A].from(row)

  }

}
