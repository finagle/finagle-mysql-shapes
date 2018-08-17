package com.linecorp.falcon.mysql

import org.scalatest._
import com.twitter.finagle.mysql.{ Client => MysqlClient }
import scala.util.{Try, Success, Failure}
import com.linecorp.falcon.mysql.generic._
import org.scalatest.concurrent.ScalaFutures

class RowDecoderSpec extends fixture.AsyncFlatSpec with MysqlSuite {

  case class Foo(id: Long, name: String)

  it should "decode a row into a case class" in { f: FixtureParam =>

    val result = f.client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.as[Foo]
    }

    fromTwitter(result) map { o =>
      assert(o == List(Success(Foo(1, "test"))))
    }
  }

  it should "decode a row into a tuple" in { f: FixtureParam =>

    import com.linecorp.falcon.mysql.generic.tuples._

    val result = f.client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.as[(Long, String)]
    }

    fromTwitter(result) map { o =>
      assert(o == List(Success((1, "test"))))
    }
  }

}
