def generateProductToJson = {
  val out = new StringBuilder()
  out ++= "package nrktkt.ninny\n"
  out ++= "trait ProductToJson {\n"
  for (i <- 1 to 22) {

    out ++= s"def forProduct${i}[Target, "
    val aTypes = (0 until i).map(j => s"A$j").mkString(", ")
    out ++= aTypes
    out ++= s"]("
    out ++= (0 until i).map(j => s"nameA$j: String").mkString(", ")
    out ++= s")(f: Target => ("
    out ++= aTypes
    out ++= "))(implicit "
    out ++=
      (0 until i)
        .map(j => s"a${j}ToJson: ToJson[A$j]")
        .mkString(", ")

    out ++= "): ToSomeJsonObject[Target] = target => {\n"
    out ++= "val ("
    out ++= (0 until i).map(j => s"a$j").mkString(", ")
    out ++= ") = f(target)\n"
    out ++= "obj("
    out ++= (0 until i).map(j => s"(nameA$j, a$j)").mkString(", ")
    out ++= ")\n}\n"
  }

  out += '}'
  out.result()
}

def generateProductFromJson = {
  val out = new StringBuilder()
  out ++= "package nrktkt.ninny\n"
  out ++= "trait ProductFromJson {\n"
  for (i <- 1 to 22) {

    out ++= s"def forProduct${i}[Target, "
    val aTypes = (0 until i).map(j => s"A$j").mkString(", ")
    out ++= aTypes
    out ++= s"]("
    out ++= (0 until i).map(j => s"nameA$j: String").mkString(", ")
    out ++= s")(f: ("
    out ++= aTypes
    out ++= ") => Target)(implicit "
    out ++=
      (0 until i).map(j => s"a${j}FromJson: FromJson[A$j]").mkString(", ")

    out ++= "): FromJson[Target] = json => for {\n"
    out ++=
      (0 until i)
        .map(j => s"a$j <- (json / nameA$j).to[A$j]")
        .mkString("\n")

    out ++= "}\nyield f("
    out ++= (0 until i).map(j => s"a$j").mkString(", ")
    out ++= ")\n"
  }

  out += '}'
  out.result()
}

def generateProductToAndFromJson = {
  val out = new StringBuilder()
  out ++= "package nrktkt.ninny\n"
  out ++= "import nrktkt.ninny.ast._\n"
  out ++= "trait ProductToAndFromJson {\n"
  for (i <- 1 to 22) {

    out ++= s"def forProduct${i}[Target, "
    val aTypes = (0 until i).map(j => s"A$j").mkString(", ")
    out ++= aTypes
    out ++= s"]("
    out ++= (0 until i).map(j => s"nameA$j: String").mkString(", ")
    out ++= s")(fFrom: ("
    out ++= aTypes
    out ++= ") => Target,\n"
    out ++= "fTo: Target => ("
    out ++= s"$aTypes))(implicit "
    out ++=
      (0 until i)
        .map(j => s"a${j}ToAndFromJson: ToAndFromJson[A$j]")
        .mkString(", ")

    out ++= ") = new ToAndFromJson[Target] {\n"
    val namesCsv = (0 until i).map(j => s"nameA$j").mkString(", ")
    out ++= s"val _toJson: ToSomeJson[Target] = ToJson.forProduct$i($namesCsv)(fTo)\n"
    out ++= s"val _fromJson: FromJson[Target] = FromJson.forProduct$i($namesCsv)(fFrom)\n"

    out ++= """
          def from(json: Option[JsonValue]) = _fromJson.from(json)
          def toSome(target: Target)        = _toJson.toSome(target)
          """
    out ++= "}\n"
  }

  out += '}'
  out.result()
}
