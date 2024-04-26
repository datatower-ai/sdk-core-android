package ai.datatower.analytics.network

abstract class DTHttpException(msg: String): Exception(msg)

class ServiceUnavailableException(msg: String): DTHttpException(msg)

class RemoteVerificationException(
    val responseCode: Int,
    val response: String
): DTHttpException("Event is not valid (remote), response code: $responseCode, response: $response")