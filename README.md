# finagle-mysql-shapes

This library adds support to Twitter's [Finagle][Finagle] MySQL client for boilerplate-free marshalling of rows into case classes.


## Install

To use this library configure your sbt project with the following line:

```sbt
libraryDependencies += "com.linecorp" %% "finagle-mysql-shapes" % "0.1.0"
```

## Documentation

 - [API docs](todo)


## Examples

This project uses [shapeless][shapeless]'s generic representation of case classes for automatic derivation of [`RowDecoder[A]`](src/main/scala/com/linecorp/falcon/mysql/RowDecoder.scala) instances.

First some imports:

```scala
import com.linecorp.finagle.mysql.shapes._
import com.twitter.finagle.Mysql
```

Marshalling [rows][Row] into case classes:

```scala
case class User(firstName: String, lastName: String)

val client = Mysql.client.withCredentials(...)

val result = client.select("SELECT * FROM users WHERE id = 1") {
  row => RowDecoder[User].from(row)
}
```

For a slightly nicer syntax:
```scala
import com.linecorp.finagle.mysql.syntax._

val result = client.select("SELECT * FROM users WHERE id = 1") {
  row => row.as[User]
}
```

A streamlined way of unpacking rows into tuples:
```scala
import com.linecorp.finagle.mysql.shapes.tuples._

val result = client.select("SELECT * FROM test WHERE id = 1") { row =>
  row.as[(String, String)]
}
```


You can also provide your own instance of `RowDecoder[A]`:

```scala
import com.linecorp.finagle.mysql.RowDecoder

implicit val decoder: RowDecoder[User] = RowDecoder.instance { row =>
  for {
    f <- row.get[String]("firstName")
    l <- row.get[String]("lastName")
  } yield User(f, l)
}
```

See the [`RowSpec`](src/test/scala/com/linecorp/falcon/mysql/RowSpec.scala) integration test suite for more examples.

### JSON 

To parse JSON columns this library provides integration with [circe][circe]. 
```scala
import com.linecorp.finagle.mysql.circe._
```

You can use other JSON libraries by providing an instance of [`JsonDecoder[A]`](src/main/scala/com/linecorp/falcon/mysql/RowDecoder.scala):

```scala
trait JsonDecoder[A] {
  def decode(json: String): Try[A]
}
```


[Finagle]: https://twitter.github.io/finagle/
[Row]: https://twitter.github.io/finagle/docs/com/twitter/finagle/mysql/Row.html
[shapeless]: https://github.com/milessabin/shapeless
[circe]: https://github.com/circe/circe
