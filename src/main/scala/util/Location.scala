package util

import java.io.File
import scala.util.parsing.input.Position

final case class Location(file: File, pos: Position)