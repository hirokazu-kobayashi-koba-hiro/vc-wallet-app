package org.idp.wallet.verifiable_credentials_library.util.date

import java.time.LocalDateTime
import java.time.ZoneOffset

object DateUtils {

  fun nowAsEpochSecond(): Long {
    // FIXME timezone
    return LocalDateTime.now().minusHours(9L).toEpochSecond(ZoneOffset.UTC)
  }
}
