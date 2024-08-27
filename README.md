# vc-wallet-app

## app

### run

### demo with authlete trial
You can issue verifiable-credentials of vc+sd-jwt format with authlete trial service.

#### steps

1. move to following web page
   1. https://trial.authlete.net/api/offer/issue
2. login
   1. id: john
   2. pass: john
3. click a submit button
4. scan qr with vc-wallet-android-app
   1. select format of vc+sd-jwt

### demo with eudi

https://issuer.eudiw.dev/credential_offer_choice

# verifiable-credentials-library

## overview
This library provides protocol of OID4VC and OID4VP and multi account management.

This library is implemented based on [OpenID for Verifiable Credentials](https://openid.net/sg/openid4vc/) and [OpenID for Verifiable Presentation](https://openid.github.io/OpenID4VP/openid-4-verifiable-presentations-wg-draft.html).

## feature
The library provides the following functionality:

- Account management
  - [x] OIDC
- Key management
  - [x] generate key-pair
  - web3-eth
    - [x] generate key-pair
    - [x] restore key-pair with seed 
- Document management
   - [ ] Storage encryption
   - [x] separate with namespace
- OpenID for Verifiable Credential Issuance
   - protocol
      - [x] Authorization Code Flow
      - [x] Pre-authorization Code Flow
      - [x] Support for deferred issuing
      - [x] Dynamic registration of clients
   - use-case
      - [x] same device
      - [x] cross device
   - format
      - [ ] mso_mdoc
      - [x] sd-jwt-vc
      - [x] jwt_vc_json
      - [ ] did_jwt_vc
      - [ ] jwt_vc_json-ld
      - [ ] ldp_vc
   - extension
      - [x] Support for DPoP JWT in authorization
- OpenID for Verifiable Presentations
   - protocol
      - [ ] For pre-registered verifiers
      - [ ] Dynamic registration of verifiers
   - use-case
     - [ ] remote
     - [ ] proximity

## Requirements
- Android 8 (API level 26) or higher

## preparation

- Select a ID provider in order to manage this wallet account.
- Register public client for this wallet app.


## How to use

### setting

Add the following dependencies to your app's build.gradle file.

```
implementation(project(":verifiable-credentials-library"))
```

Configure resources of deepLink for redirection handling of login with OIDC.  

example
```
<resources>
    <string name="com_vc_wallet_client_id">sKUsWLY5BCzdXAggk78km7kOjfQP1rWR</string>
    <string name="com_vc_wallet_domain">dev-l6ns7qgdx81yv2rs.us.auth0.com</string>
    <string name="com_vc_wallet_scheme">org.idp.verifiable.credentials</string>
</resources>
```


### initialize

```kotlin
class MainActivity : FragmentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VerifiableCredentialsClient.initialize(this)
    }
}
```

### library's activity

This way is to enable using library's activity of verifiable credential.

```kotlin
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VerifiableCredentialsClient.initialize(this)
        val request =
            OpenIdConnectRequest(
                issuer = "https://dev-l6ns7qgdx81yv2rs.us.auth0.com",
                clientId = "sKUsWLY5BCzdXAggk78km7kOjfQP1rWR",
                scope = "openid profile phone email address offline_access",
                state = UUID.randomUUID().toString(),
                nonce = UUID.randomUUID().toString(),
                redirectUri =
                "org.idp.verifiable.credentials://dev-l6ns7qgdx81yv2rs.us.auth0.com/android/org.idp.wallet.app/callback")
        VerifiableCredentialsClient.start(context = this, request = request, forceLogin = true)
    }
}

```

### custom activity

This way is to enable custom activity of verifiable credential with VerifiableCredentialsApi.

VerifiableCredentialsApi provides following function.

1. handlePreAuthorization
2. handleAuthorizationCode
3. handleDeferredCredential
4. findCredentials
5. findCredentialIssuanceResults


