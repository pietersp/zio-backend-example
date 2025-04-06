package com.example.util

import com.augustnagro.magnum.DbCodec
import io.github.iltotore.iron.*
import zio.schema.*

inline given ironSchema[A, Description](using
  Schema[A],
  Constraint[A, Description]
): Schema[A :| Description] =
  Schema[A].transformOrFail(_.refineEither[Description], Right(_))

inline given ironDbCodec[A, Description](using
  DbCodec[A],
  Constraint[A, Description]
): DbCodec[A :| Description] =
  DbCodec[A].biMap(_.refineUnsafe[Description], identity)
