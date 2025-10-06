package com.example.client

import zio.*

/** Configuration for the API client */
final case class ApiClientConfig(
  baseUrl: String,
  timeout: Duration = 30.seconds,
  retries: Int = 3
)

object ApiClientConfig {
  
  /** Creates a layer from ZIO Config */
  val layer: Layer[Config.Error, ApiClientConfig] =
    ZLayer {
      for {
        baseUrl <- ZIO.config[String](Config.string("api.client.base-url"))
        timeout <- ZIO.config[Duration](Config.duration("api.client.timeout"))
          .orElse(ZIO.succeed(30.seconds))
        retries <- ZIO.config[Int](Config.int("api.client.retries"))
          .orElse(ZIO.succeed(3))
      } yield ApiClientConfig(baseUrl, timeout, retries)
    }

  /** Creates a layer with default local configuration */
  val localLayer: ULayer[ApiClientConfig] =
    ZLayer.succeed(
      ApiClientConfig(
        baseUrl = "http://localhost:8080",
        timeout = 30.seconds,
        retries = 3
      )
    )

  /** Creates a layer with custom base URL and defaults for other settings */
  def withBaseUrl(url: String): ULayer[ApiClientConfig] =
    ZLayer.succeed(
      ApiClientConfig(
        baseUrl = url,
        timeout = 30.seconds,
        retries = 3
      )
    )
}
