package com.lucas.marvel.client

sealed trait MarvelApiError extends Throwable {
  def message: String
}
case class MarvelApiHttpError(code: Int, message: String) extends MarvelApiError

case class UnknownError(throwable: Throwable) extends MarvelApiError {
  override def message: String = throwable.getMessage
}

case class JsonDecodingError(throwable: Throwable) extends MarvelApiError {
  override def message: String = throwable.getMessage
}
