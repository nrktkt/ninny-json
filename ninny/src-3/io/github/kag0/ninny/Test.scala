package io.github.kag0.ninny


object Test extends App {
  import scala.deriving.Mirror
  import scala.compiletime.summonAll

  case class A(i: Int, s: String)

  val mirror = summon[Mirror.Of[A]]    
  type ValueOfs = Tuple.Map[mirror.MirroredElemLabels, ValueOf]
  val valueOfs: Tuple.Map[mirror.MirroredElemLabels, ValueOf] = summonAll[ValueOfs]

  type ValueOfMap[A <: Tuple] = Tuple.Map[A, ValueOf]
  /*
  def values[Out <: Tuple](t: ValueOfMap[Out]): Out = t match
    case (h: ValueOf[Tuple.Head[Out]]) *: (t: Tuple.Tail[ValueOfMap[Out]]) => h.value *: values[Tuple.Tail[Out]](t1)
    case _ => EmptyTuple

  val out = values[mirror.MirroredElemLabels](valueOfs) // (i,s)
  println(out)
  */
}
