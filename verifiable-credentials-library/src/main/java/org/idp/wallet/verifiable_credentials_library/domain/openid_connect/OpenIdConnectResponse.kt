package org.idp.wallet.verifiable_credentials_library.domain.openid_connect

import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.UserinfoResponse
import org.idp.wallet.verifiable_credentials_library.domain.user.User

class OpenIdConnectResponse(
    val tokenResponse: TokenResponse,
    val userinfoResponse: UserinfoResponse? = null
) {

  fun toUser(id: String): User {
    return User(
        id = id,
        sub = userinfoResponse?.sub ?: "",
        givenName = userinfoResponse?.givenName,
        familyName = userinfoResponse?.familyName,
        middleName = userinfoResponse?.middleName,
        nickname = userinfoResponse?.nickname,
        preferredUsername = userinfoResponse?.preferredUsername,
        profile = userinfoResponse?.profile,
        picture = userinfoResponse?.picture,
        website = userinfoResponse?.website,
        email = userinfoResponse?.email,
        emailVerified = userinfoResponse?.emailVerified,
        gender = userinfoResponse?.gender,
        birthdate = userinfoResponse?.birthdate,
        zoneinfo = userinfoResponse?.zoneinfo,
        locale = userinfoResponse?.locale,
        phoneNumber = userinfoResponse?.phoneNumber,
        phoneNumberVerified = userinfoResponse?.phoneNumberVerified,
        address = userinfoResponse?.address,
        updatedAt = userinfoResponse?.updatedAt)
  }

  fun sub(): String {
    return userinfoResponse?.sub ?: ""
  }
}
