package com.example.util.db

import com.augustnagro.magnum.DbCodec
import io.github.iltotore.iron.*

inline given ironDbCodec[A, Description](using
  DbCodec[A],
  Constraint[A, Description]
): DbCodec[A :| Description] =
  DbCodec[A].biMap(_.refineUnsafe[Description], identity)
