package com.lucas.utils

import java.security.MessageDigest

object DigestUtils {

  def md5Hash(s: String): String =
    MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02x".format(_)).mkString
}
