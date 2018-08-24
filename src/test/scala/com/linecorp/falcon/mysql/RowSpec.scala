package com.linecorp.falcon.mysql

import org.scalatest._
import scala.util.{Try, Success, Failure}
import io.circe.{ Decoder, Json }
import com.twitter.finagle.mysql._
import com.linecorp.falcon.mysql.syntax._
import com.linecorp.falcon.mysql.circe._

class RowDecoderSpec extends fixture.AsyncFlatSpec with MysqlSuite with Matchers {

  override def populate(data: PreparedStatement) =
    for {
      _ <- data.modify(1, "test", "{}")
      _ <- data.modify(2, "some", """{"foo": "bar", "bar": true}""")
      _ <- data.modify(3, "some", null)
    } yield ()

  it should "decode a row into a case class" in { client: FixtureParam =>

    import com.linecorp.falcon.mysql.generic._

    case class Foo(id: Long, name: String, data: Json)

    val result = client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.as[Foo]
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Success(Foo(_,_,_))) =>
      }
    }
  }

  it should "decode a row into a nested record" in { client: FixtureParam =>

    import com.linecorp.falcon.mysql.generic._

    case class Bar(id: Long, data: Json)

    case class Foo(name: String, bar: Bar)

    val result = client.select("SELECT * FROM test WHERE id = 2") { row =>
      row.as[Foo]
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Success(Foo(_,Bar(_,_)))) =>
      }
    }
  }


  it should "decode a row with a Json column using an implicit decoder" in { client: FixtureParam =>

    import com.linecorp.falcon.mysql.generic._

    case class Data(foo: String, bar: Boolean)

    implicit val decodeData: Decoder[Data] =
      Decoder.forProduct2("foo", "bar")(Data.apply)

    case class Foo(id: Long, name: String, data: Data)

    val result = client.select("SELECT * FROM test WHERE id = 2") { row =>
      row.as[Foo]
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Success(Foo(_,_,Data(_,_)))) =>
      }
    }
  }

  it should "decode a row into a tuple" in { client: FixtureParam =>

    import com.linecorp.falcon.mysql.generic.tuples._

    val result = client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.as[(Long, String, Json, java.sql.Timestamp)]
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Success((_,_,_,_))) =>
      }
    }
  }


  it should "decode a nullable column into an Option" in { client: FixtureParam =>

    import com.linecorp.falcon.mysql.generic._

    case class Foo(id: Long, name: String, data: Option[Json])

    val result = client.select("SELECT * FROM test WHERE id = 3") { row =>
      row.as[Foo]
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Success(Foo(_,_,None))) =>
      }
    }
  }

  it should "decode a row with custom decoder" in { client: FixtureParam =>

    case class Foo(id: Long, name: String, data: Json)

    implicit val decoder: RowDecoder[Foo] = RowDecoder.instance { row =>
      for {
        id   <- row.get[Long]("id")
        name <- row.get[String]("name")
        data <- row.get[Json]("data")
      } yield Foo(id, name, data)
    }

    val result = client.select("SELECT * FROM test WHERE id = 2") { row =>
      row.as[Foo]
    }

    fromTwitter(result) map { o =>
      o should matchPattern {
        case List(Success(Foo(_,_,_))) =>
      }
    }
  }

}
