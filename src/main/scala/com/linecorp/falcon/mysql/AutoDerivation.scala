package com.linecorp.falcon.mysql

import scala.util.{ Try, Success, Failure }
import com.twitter.finagle.mysql.{ Row, Value }
import shapeless._

trait AutoDerivation {

  // HList
  implicit def deriveHNil: IndexedRowDecoder[HNil] = IndexedRowDecoder.instance {
    _.headOption match {
      case None => Success(HNil)
      case _    => RowDecoder.fail("Cannot convert to HNil")
    }
  }

  implicit def deriveHCons[V, T <: HList]
    (implicit rv: Lazy[ValueDecoder[V]], rt: Lazy[IndexedRowDecoder[T]])
      : IndexedRowDecoder[V :: T] = IndexedRowDecoder.instance {
    case x +: xs => for {
      front <- rv.value.from(x)
      back  <- rt.value.fromValues(xs)
    } yield front :: back

    case _ => RowDecoder.fail("Cannot convert to HList")
  }

  // Anything with a Generic

  implicit def deriveClass[A, R]
    (implicit gen: Generic.Aux[A, R], decoder: IndexedRowDecoder[R])
      : IndexedRowDecoder[A] = IndexedRowDecoder.instance {
    decoder.fromValues(_).map(gen.from)
  }
}
