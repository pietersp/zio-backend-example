package com.example.domain

import zio.schema.*

case class Phone(number: String) derives Schema
