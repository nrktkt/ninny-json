package nrktkt.ninny
import scala.quoted._

object DefaultOptions {
  type Aux[A, O] = DefaultOptions[A] { type Out = O }

  transparent inline given derived[T]: DefaultOptions[T] =
    ${genDefaultsImpl[T]}
  
  def genDefaultsImpl[T: Type](using Quotes): Expr[DefaultOptions[T]] = {
    import quotes.reflect._
    val classSymbol = TypeRepr.of[T].typeSymbol 
    val objectSymbol = classSymbol.companionModule
    val tycons = TypeRepr.of[scala.*:[_, _]] match
      case AppliedType(a, _) => a
    val (tupleExpr, tupleType) = classSymbol.caseFields.zipWithIndex.reverse.foldLeft[(Term, TypeRepr)]((Expr(EmptyTuple).asTerm, TypeRepr.of[EmptyTuple.type])){ case ((tpleExpr, tpleTpe), (symbol, idx)) =>
      (tpleTpe.asType, TypeRepr.of[T].memberType(symbol).asType) match
        case ('[t], '[g]) =>
          if symbol.flags.is(Flags.HasDefault) then 
            val value = objectSymbol.declaredMethod("$lessinit$greater$default$" + (idx + 1)).head
            val expr = '{Some(${Select(Ref(objectSymbol), value).asExprOf[g]})}
            (Apply(TypeApply(Select.unique(tpleExpr, "*:"), List(TypeTree.of[Option[g]], TypeTree.of[t])), List(expr.asTerm)), AppliedType(tycons, List(TypeRepr.of[Option[g]], tpleTpe)))
          else
            (Apply(TypeApply(Select.unique(tpleExpr, "*:"), List(TypeTree.of[Option[g]], TypeTree.of[t])), List('{None}.asTerm)), AppliedType(tycons, List(TypeRepr.of[Option[g]], tpleTpe)))
    }
    tupleType.asType match
      case '[t] =>
        '{
          new DefaultOptions[T] {
            type Out = t
            def apply(): Out = ${tupleExpr.asExprOf[t]}
          }.asInstanceOf[DefaultOptions.Aux[T, t]]
        }
  } 
}
trait DefaultOptions[A] {
  type Out <: Tuple
  def apply(): Out
}
