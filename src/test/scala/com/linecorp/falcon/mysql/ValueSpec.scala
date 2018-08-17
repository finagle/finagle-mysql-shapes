package com.linecorp.falcon.mysql

import org.scalatest._
import com.twitter.finagle.mysql.{ Client => MysqlClient }
import scala.util.{Try, Success, Failure}
import org.scalatest.concurrent.ScalaFutures
import com.linecorp.falcon.mysql.syntax._

class ValueDecoderSpec extends fixture.AsyncFlatSpec with MysqlSuite {

  it should "decode a column by name" in { f: FixtureParam =>

    val result = f.client.select("SELECT * FROM test WHERE id = 1") { row =>
      row.get[String]("name")
    }

    fromTwitter(result) map { o =>
      assert(o == List(Success("test")))
    }
  }

}
