package com.lucas.marvel.client.response

/**
 * Represents the generalized top-level response from the Marvel API More info:
 * https://developer.marvel.com/documentation/apiresults
 *
 * This is actually a subset of the full response, but it's enough for our
 * purposes.
 */
case class ApiResponseWrapper[T <: ApiResponseEntity](code: Int, status: String, data: ApiResponseContainer[T])

case class ApiResponseContainer[T <: ApiResponseEntity](offset: Int, total: Int, results: List[T])
