# vc-wallet-app

## app

### demo with authlete trial
You can issue verifiable-credentials of vc+sd-jwt format with authlete trial service.

### steps

1. move to following web page
   1. https://trial.authlete.net/api/offer/issue
2. login
   1. id: john
   2. pass: john
3. click a submit button
4. scan qr with vc-wallet-android-app
   1. select format of vc+sd-jwt

## feature
The library provides the following functionality:

- Account management
  - [x] OIDC
- Key management
  - [x] generate key-pair
  - web3-eth
    - [x] generate key-pair
    - [ ] restore key-pair with seed 
- Document management
   - [ ] Storage encryption
   - [ ] separate with namespace
- OpenID for Verifiable Credential Issuance
   - protocol
      - [ ] Authorization Code Flow
      - [x] Pre-authorization Code Flow
      - [ ] Support for deferred issuing
      - [ ] Dynamic registration of clients
   - use-case
      - [ ] same device
      - [x] cross device
   - format
      - [ ] mso_mdoc
      - [x] sd-jwt-vc
      - [x] jwt_vc_json
      - [ ] did_jwt_vc
      - [ ] jwt_vc_json-ld
      - [ ] ldp_vc
   - extension
      - [ ] Support for DPoP JWT in authorization
- OpenID for Verifiable Presentations
   - protocol
      - [ ] For pre-registered verifiers
      - [ ] Dynamic registration of verifiers
   - use-case
     - [ ] remote
     - [ ] proximity
