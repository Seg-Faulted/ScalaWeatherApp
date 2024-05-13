error id: jar:file://<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/typesafe/slick/slick_3/3.5.0/slick_3-3.5.0-sources.jar!/slick/collection/heterogeneous/syntax.scala:[258..259) in Input.VirtualFile("jar:file://<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/typesafe/slick/slick_3/3.5.0/slick_3-3.5.0-sources.jar!/slick/collection/heterogeneous/syntax.scala", "package slick.collection.heterogeneous

/** Extra syntax for heterogenous collections. */
object syntax {
  // Use :: for types and extractors
  type :: [+H, +T <: HList] = HCons[H, T]
  val :: = HCons

  type HNil = slick.collection.heterogeneous.HNil.type
}
")
jar:file://<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/typesafe/slick/slick_3/3.5.0/slick_3-3.5.0-sources.jar!/slick/collection/heterogeneous/syntax.scala
jar:file://<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/com/typesafe/slick/slick_3/3.5.0/slick_3-3.5.0-sources.jar!/slick/collection/heterogeneous/syntax.scala:10: error: expected identifier; obtained rbrace
}
^
#### Short summary: 

expected identifier; obtained rbrace