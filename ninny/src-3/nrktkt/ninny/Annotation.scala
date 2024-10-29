package nrktkt.ninny

import scala.quoted._

object Annotation {
  type Aux[A, O] = Annotation[A] { type Out = O }

  transparent inline given derived[T]: Annotation[T] =
    ${genAnnotationImpl[T]}
  
  def genAnnotationImpl[T: Type](using Quotes): Expr[Annotation[T]] = {
    import quotes.reflect._
    val classSymbol = TypeRepr.of[T].typeSymbol 
    val objectSymbol = classSymbol.companionModule
    val tycons = TypeRepr.of[scala.*:[_, _]] match
      case AppliedType(a, _) => a
    val (tupleExpr, tupleType) = classSymbol.caseFields.reverse.foldLeft[(Term, TypeRepr)]((Expr(EmptyTuple).asTerm, TypeRepr.of[EmptyTuple.type])){ case ((tpleExpr, tpleTpe), symbol) =>
      tpleTpe.asType match
        case ('[t]) =>
          val valueOpt = 
            symbol.annotations.collect {
              case Apply(Select(New(ident),_), List(Literal(StringConstant(strVal)))) => strVal
            } match
              case head :: next => Some(head)
              case Nil => None
          valueOpt match
            case Some(value) =>
              val tpe = ConstantType(StringConstant(value))
              tpe.asType match 
                case '[x] =>
                  (Apply(TypeApply(Select.unique(tpleExpr, "*:"), List(TypeTree.of[x], TypeTree.of[t])), List(Literal(StringConstant(value)))), AppliedType(tycons, List(tpe, tpleTpe)))
            case None => 
              (Apply(TypeApply(Select.unique(tpleExpr, "*:"), List(TypeTree.of[Null], TypeTree.of[t])), List('{null}.asTerm)), AppliedType(tycons, List(TypeRepr.of[Null], tpleTpe)))
    }
    tupleType.asType match
      case '[t] =>
        '{
          new Annotation[T] {
            type Out = t
            def apply(): Out = ${tupleExpr.asExprOf[t]}
          }.asInstanceOf[Annotation.Aux[T, t]]
        }
  }
}
trait Annotation[A] {
  type Out <: Tuple
  def apply(): Out
}
