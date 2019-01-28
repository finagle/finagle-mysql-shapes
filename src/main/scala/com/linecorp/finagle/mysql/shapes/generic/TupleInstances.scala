package com.linecorp.finagle.mysql.shapes.generic

import scala.util.{ Try, Success, Failure }
import shapeless._
import com.linecorp.finagle.mysql.shapes._

trait TupleInstances {

  implicit def deriveHNil: IndexedRowDecoder[HNil] = IndexedRowDecoder.instance {
    _.headOption match {
      case None => Success(HNil)
      case _    => RowDecoder.fail("Cannot convert to HNil")
    }
  }

  implicit def deriveHCons[V, T <: HList](implicit
    decodeV: Lazy[ValueDecoder[V]],
    decodeT: Lazy[IndexedRowDecoder[T]]
  ) : IndexedRowDecoder[V :: T] = IndexedRowDecoder.instance {
    case x +: xs => for {
      front <- decodeV.value.from(x)
      back  <- decodeT.value.fromValues(xs)
    } yield front :: back

    case _ => RowDecoder.fail("Cannot convert to HList")
  }

  implicit def deriveGeneric[A, R](implicit
    gen: Generic.Aux[A, R],
    decode: IndexedRowDecoder[R]
  ) : IndexedRowDecoder[A] = IndexedRowDecoder.instance {
    decode.fromValues(_).map(gen.from)
  }

}

object tuples extends TupleInstances

