# finagle-mysql-shapes
[![Build Status](https://travis-ci.org/finagle/finagle-mysql-shapes.svg?branch=master)](https://travis-ci.org/finagle/finagle-mysql-shapes)

This library adds support to Twitter's [Finagle][Finagle] MySQL client for boilerplate-free transformation of rows into case classes.


## Install

To use this library configure your sbt project with the following line:

```sbt
libraryDependencies += "com.linecorp" %% "finagle-mysql-shapes" % "0.1.0"
```

## Documentation

 - [API docs](todo)


## Examples

This project uses [shapeless][shapeless]'s generic representation of case classes for automatic derivation of [`RowDecoder[A]`](src/main/scala/com/linecorp/falcon/mysql/RowDecoder.scala) instances.

Given a client, e.g.:

```scala
import com.twitter.finagle.Mysql

val client = Mysql.client.withCredentials(...)
```
and the following relational schema:
```sql
CREATE TABLE users
  (
    firstName VARCHAR(255), NOT NULL
    lastName VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    fruit ENUM('Melon', 'Mango') NOT NULL,
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  ) engine=innodb DEFAULT charset=utf8
```
Marshalling [rows][Row] into case classes:

```scala
import com.linecorp.finagle.mysql.generic._

case class User(firstName: String, lastName: String, address: Option[String])

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
import com.linecorp.finagle.mysql.generic.tuples._

val result = client.select("SELECT * FROM users WHERE id = 1") { row =>
  row.as[(String, String)]
}
```

Decoding `ENUM` columns into sealed trait hierarchies:
```scala
import com.linecorp.finagle.mysql.generic._

sealed trait Fruit
case object Melon extends Fruit
case object Mango extends Fruit

val result = client.select("SELECT fruit FROM users WHERE id = 4") { row =>
  row.get[Fruit](column = "fruit")
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
## How to contribute

See [CONTRIBUTING.md](CONTRIBUTING.md)


## LICENSE
```
Copyright 2019 LINE Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```
See [LICENSE](LICENSE) for more detail.


[Finagle]: https://twitter.github.io/finagle/
[Row]: https://twitter.github.io/finagle/docs/com/twitter/finagle/mysql/Row.html
[shapeless]: https://github.com/milessabin/shapeless
[circe]: https://github.com/circe/circe
