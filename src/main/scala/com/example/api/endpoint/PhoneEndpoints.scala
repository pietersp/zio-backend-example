package com.example.api.endpoint

import com.example.domain.{Phone, PhoneId, PhoneIdDescription}
import com.example.error.AppError.{PhoneAlreadyExists, PhoneNotFound}
import zio.http.*
import zio.http.codec.*
import zio.http.endpoint.Endpoint
import com.example.util.given

trait PhoneEndpoints extends Codecs {
  val createPhone =
    Endpoint(Method.POST / "phone")
      .in[Phone](Doc.p("Phone to be created"))
      .out[PhoneId](Doc.p("ID of the created phone"))
      .outError[PhoneAlreadyExists](
        Status.Conflict,
        Doc.p("The phone number already exists")
      )
      ?? Doc.p("Create a new phone")

  val getPhoneById =
    Endpoint(Method.GET / "phone" / idCodec[PhoneIdDescription]())
      .out[Phone](Doc.p("Phone"))
      .outError[PhoneNotFound](
        Status.NotFound,
        Doc.p("The phone was not found")
      )
      ?? Doc.p("Obtain the phone with the given `id`")

  val updatePhone =
    Endpoint(Method.PUT / "phone" / idCodec[PhoneIdDescription]())
      .in[Phone](Doc.p("Phone to be updated"))
      .out[Unit]
      .outError[PhoneNotFound](
        Status.NotFound,
        Doc.p("The phone was not found")
      )
      ?? Doc.p("Update the phone with the given `id`")

  val deletePhone =
    Endpoint(Method.DELETE / "phone" / idCodec[PhoneIdDescription]())
      .out[Unit]
      ?? Doc.p("Delete the phone with the given `id`")

}
