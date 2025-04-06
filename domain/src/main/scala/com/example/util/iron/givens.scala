package com.example.util.iron

import io.github.iltotore.iron.*
import zio.schema.Schema

inline given ironSchema[A, Description](using
  Schema[A],
  Constraint[A, Description]
): Schema[A :| Description] =
  Schema[A].transformOrFail(_.refineEither[Description], Right(_))
