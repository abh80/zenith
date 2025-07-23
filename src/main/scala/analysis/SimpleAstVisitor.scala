package analysis

import ast.{AstNode, AstVisitor}

class SimpleAstVisitor(protected val nodes: List[AstNode[?]]) extends AstVisitor
