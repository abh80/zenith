package util

import analysis.semantics.{Scope, Symbol}
import util.LocationResolver.getLoc

object AnalysisUtil {
  def declareSymbol(scope: Scope, symbol: Symbol): Result[Scope] =
    if scope.define(symbol) then Right(scope) else Left(List(DuplicateSymbolDeclaration(getLoc(symbol.nodeId), symbol.name)))
}
