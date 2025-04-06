package com.example.repository

import com.example.domain.{Phone, PhoneId, PhoneNumber}
import zio.*

trait PhoneRepository {
  def create(phone: Phone): UIO[PhoneId]
  def retrieve(phoneId: PhoneId): UIO[Option[Phone]]
  def retrieveByNumber(phoneNumber: PhoneNumber): UIO[Option[Phone]]
  def update(phoneId: PhoneId, phone: Phone): UIO[Unit]
  def delete(phoneId: PhoneId): UIO[Unit]
}
