package com.linecorp.finagle.mysql.generic

import scala.util.{ Try, Success, Failure }
import shapeless._
import shapeless.labelled.{ field, FieldType }
import com.twitter.finagle.mysql._
import com.linecorp.finagle.mysql._

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
    val value = row.apply(column).getOrElse(NullValue)

    val result = for {
      front <- decodeV.value.from(value).map(field[K](_))
      back  <- decodeT.value.from(row)
    } yield front :: back

    result recoverWith {
      case e: ValueException => RowDecoder.fail(s"Column $column: ${e.getMessage}")
    }

  }

  implicit def deriveLabelledGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    decode: RowDecoder[R]
  ) : RowDecoder[A] = RowDecoder.instance {
    decode.from(_).map(gen.from)
  }

  implicit def deriveEnumerationCNil : ValueDecoder[CNil] = ValueDecoder.instance {
    _ => ValueDecoder.fail("Enumeration decoding failed")
  }

  implicit def deriveEnumerationCCons[K <: Symbol, V, R <: Coproduct](
    implicit
    witness: Witness.Aux[K],
    decodeV: LabelledGeneric.Aux[V, HNil],
    decodeR: ValueDecoder[R]
  ): ValueDecoder[FieldType[K, V] :+: R] = ValueDecoder.instance { value =>
    val name = witness.value.name

    value match {
      case StringValue(s) if s == name => Success(Inl(field[K](decodeV.from(HNil))))
      case StringValue(_)              => decodeR.from(value).map(Inr(_))
      case _                           => ValueDecoder.fail("failed to decode Enumeration")
    }
  }

  implicit def deriveEnumeration[A, Repr <: Coproduct](
    implicit
    gen: LabelledGeneric.Aux[A, Repr],
    decode: ValueDecoder[Repr]
  ): ValueDecoder[A] = ValueDecoder.instance {
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



