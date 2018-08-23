package com.linecorp.falcon.mysql.generic

import scala.util.{ Try, Success, Failure }
import shapeless._
import shapeless.labelled.{ field, FieldType }
import com.linecorp.falcon.mysql._

trait Instances extends LowPriorityInstances {

  implicit def deriveRowHNil: RowDecoder[HNil] = RowDecoder.instance {
    _ => Success(HNil)
  }

  implicit def deriveSymbolLabelledHCons[K <: Symbol, V, T <: HList](implicit
    witness: Witness.Aux[K],
    decodeV: Lazy[ValueDecoder[V]],
    decodeT: Lazy[RowDecoder[T]]
  ): RowDecoder[FieldType[K, V] :: T] = RowDecoder.instance { row =>
    val column = witness.value.name

    row.apply(column) match {
      case Some(value) => for {
        front <- decodeV.value.from(value).map(field[K](_))
        back  <- decodeT.value.from(row)
      } yield front :: back

      case None => RowDecoder.fail(s"Column $column not found")
    }
  }

  implicit def deriveLabelledGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    decode: RowDecoder[R]
  ) : RowDecoder[A] = RowDecoder.instance {
    decode.from(_).map(gen.from)
  }

}

trait LowPriorityInstances {
  implicit def deriveNestedRecord[K <: Symbol, V, T <: HList](implicit
    witness: Witness.Aux[K],
    decodeV: Lazy[RowDecoder[V]],
    decodeT: Lazy[RowDecoder[T]]
  ): RowDecoder[FieldType[K, V] :: T] = RowDecoder.instance { row =>
    for {
      front <- decodeV.value.from(row).map(field[K](_))
      back  <- decodeT.value.from(row)
    } yield front :: back
  }

}



