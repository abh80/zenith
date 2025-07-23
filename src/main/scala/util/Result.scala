package util

type Result[T] = Either[List[Problem], T]

object Result {
  def success[T](data: T): Result[T] = Right(data)
}