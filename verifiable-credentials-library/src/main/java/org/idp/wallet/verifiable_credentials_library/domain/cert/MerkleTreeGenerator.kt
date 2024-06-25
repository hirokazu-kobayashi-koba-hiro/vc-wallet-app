package org.idp.wallet.verifiable_credentials_library.domain.cert

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.ipfs.multibase.Multibase
import java.security.MessageDigest
import java.util.Date
import kotlin.jvm.Throws
import org.bouncycastle.util.encoders.Hex

class MerkleTreeGenerator(normalizedData: String) {
  private val tree = MerkleTree()

  init {
    val hashed = hashByteArray(normalizedData)
    println(hashed)
    tree.addLeaf(hashed)
    tree.makeTree()
  }

  private fun hashByteArray(data: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
    return hashBytes.joinToString("") { "%02x".format(it) }
  }

  private fun ensureString(value: ByteArray): String {
    return value.toString(Charsets.UTF_8)
  }

  fun getBlockchainData(): ByteArray {
    tree.makeTree()
    val merkleRoot = ensureString(tree.root!!)
    return merkleRoot.toByteArray(Charsets.UTF_8)
  }

  fun generateProof(
      transactionId: String,
      verificationMethod: String,
      chain: String
  ): Map<String, Any> {
    val root = ensureString(tree.root!!)
    val targetHash = ensureString(tree.leaves[0])
    val merkleJson =
        mapOf(
            "path" to emptyList<Any>(),
            "merkleRoot" to root,
            "targetHash" to targetHash,
            "anchors" to listOf(toBlink(chain, transactionId)))

    val proofValue = Encoder(merkleJson).encode()
    val merkleProof =
        mapOf(
            "type" to "MerkleProof2019",
            "created" to Date().toString(),
            "proofValue" to proofValue,
            "proofPurpose" to "assertionMethod",
            "verificationMethod" to verificationMethod)

    return merkleProof
  }

  private fun toBlink(chain: String, transactionId: String): String {
    return when (chain) {
      "ethereum_ropsten" -> "blink:eth:ropsten:$transactionId"
      "ethereum_goerli" -> "blink:eth:goerli:$transactionId"
      "ethereum_sepolia" -> "blink:eth:sepolia:$transactionId"
      "ethereum_mainnet" -> "blink:eth:mainnet:$transactionId"
      else -> throw IllegalArgumentException("UnknownChainError: $chain")
    }
  }
}

class MerkleTree {
  val leaves = mutableListOf<ByteArray>()
  var root: ByteArray? = null

  fun addLeaf(data: String) {
    val hashedData = hash(data)
    leaves.add(hashedData)
  }

  fun makeTree() {
    if (leaves.isEmpty()) throw IllegalStateException("No leaves in the tree")
    root = buildTree(leaves)
  }

  fun getRoot(): String {
    return root?.let { Hex.toHexString(it) }
        ?: throw IllegalStateException("Tree has not been built")
  }

  fun getLeaf(index: Int): String {
    return Hex.toHexString(leaves[index])
  }

  private fun hash(data: String): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(data.toByteArray(Charsets.UTF_8))
  }

  private fun buildTree(nodes: List<ByteArray>): ByteArray {
    if (nodes.size == 1) return nodes[0]
    val parents = mutableListOf<ByteArray>()
    for (i in nodes.indices step 2) {
      val left = nodes[i]
      val right = if (i + 1 < nodes.size) nodes[i + 1] else nodes[i]
      parents.add(hash(Hex.toHexString(left) + Hex.toHexString(right)))
    }
    return buildTree(parents)
  }
}

// validate function placeholder
fun validate(json: Map<String, Any>): Boolean {
  // Implement validation logic
  return true
}

// Keymap placeholder
object Keymap {
  val root = mapOf("anchors" to 1, "path" to 2, "merkleRoot" to 3, "targetHash" to 4)

  val chain =
      mapOf(
          "eth" to
              Chain(
                  id = 1,
                  networks = mapOf("mainnet" to 1, "ropsten" to 3, "goerli" to 4, "sepolia" to 5)))

  val path = mapOf("left" to 1, "right" to 2)
}

data class Chain(val id: Int, val networks: Map<String, Int>)

class Encoder(private val json: Map<String, Any>) {
  init {
    val valid = validate(json)
    if (!valid) throw IllegalArgumentException("JSON is invalid. Cannot construct Encoder.")
  }

  private fun constructRootMap(): List<Pair<Int, Any>> {
    return json.keys.map { key ->
      var value = json[key]
      if (value is String) {
        value = encodeCbor(value.hexStringToByteArray())
      }
      if (value is List<*> && key == "anchors") {
        value = constructAnchorsMap(value)
      }
      if (value is List<*> && key == "path") {
        value = constructPathMap(value)
      }

      Keymap.root[key]!! to value!!
    }
  }

  private fun constructAnchorsMap(anchors: List<*>): List<List<Pair<Int, Any>>> {
    return anchors.map { anchor ->
      val values = (anchor as String).split(":").toMutableList()
      values.removeAt(0)

      values.mapIndexed { index, value ->
        when (index) {
          0 -> index to Keymap.chain[value]!!.id
          1 -> index to Keymap.chain[values[index - 1]]!!.networks[value]!!
          else -> index to encodeCbor(value.toByteArray())
        }
      }
    }
  }

  private fun constructPathMap(path: List<*>): List<Pair<Int, Any>> {
    return path.flatMap { item ->
      (item as Map<String, String>).map { (key, value) ->
        Keymap.path[key]!! to encodeCbor(value.hexStringToByteArray())
      }
    }
  }

  @Throws(Exception::class)
  fun encode(): String {
    val map = constructRootMap()
    val encoded = encodeCbor(map)
    return Multibase.encode(Multibase.Base.Base58BTC, encoded)
  }

  private fun encodeCbor(data: Any): ByteArray {
    val cborFactory = CBORFactory()
    val objectMapper = ObjectMapper(cborFactory)
    return objectMapper.writeValueAsBytes(data)
  }

  private fun String.hexStringToByteArray(): ByteArray {
    val len = this.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
      data[i / 2] =
          ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
      i += 2
    }
    return data
  }
}

class Decoder(private val base58: String) {
  init {
    if (!isValidBase58(base58)) {
      throw IllegalArgumentException("Base58 string is invalid. Cannot construct Decoder.")
    }
  }

  private fun isValidBase58(base58: String): Boolean {
    return try {
      Multibase.decode(base58)
      true
    } catch (e: Exception) {
      false
    }
  }

  private fun constructRootJSON(decoded: List<List<Any>>): Map<String, Any> {
    val rootKeymap = Keymap.root.entries.associate { (k, v) -> v to k }

    return decoded.fold(mutableMapOf()) { acc, value ->
      val key = rootKeymap[value[0] as Int]
      var value = value[1]

      if (value is List<*>) {
        when (key) {
          "anchors" -> value = constructAnchorsJSON(value as List<List<Any>>)
          "path" -> value = constructPathJSON(value as List<List<Any>>)
        }
      }

      if (value is ByteArray) {
        value = decodeCbor(value).toString()
      }

      if (key != null) {
        acc[key] = value
      }
      acc
    }
  }

  private fun constructAnchorsJSON(anchors: List<List<Any>>): List<String> {
    val chainKeymap = Keymap.chain.entries.associate { (k, v) -> v.id to k }

    return anchors.map { anchor ->
      anchor.fold("blink") { acc, value ->
        when (value) {
          0 -> "$acc:${chainKeymap[value as Int]}"
          1 -> {
            val chain = acc.split(":").last()
            val networkKeymap =
                Keymap.chain[chain]!!.networks.entries.associate { (k, v) -> v to k }
            "$acc:${networkKeymap[value as Int]}"
          }
          else -> "$acc:${decodeCbor(value as ByteArray)}"
        }
      }
    }
  }

  private fun constructPathJSON(path: List<List<Any>>): List<Map<String, String>> {
    val pathKeymap = Keymap.path.entries.associate { (k, v) -> v to k }

    return path.map { item ->
      val key = pathKeymap[item[0] as Int].toString()
      val value = decodeCbor(item[1] as ByteArray).toString()
      mapOf(key to value)
    }
  }

  @Throws(Exception::class)
  fun decode(): Map<String, Any> {
    val encoded = Multibase.decode(base58)
    val map = decodeCbor(encoded) as List<List<Any>>
    return constructRootJSON(map)
  }

  private fun decodeCbor(data: ByteArray): Any {
    val cborFactory = CBORFactory()
    val objectMapper = ObjectMapper(cborFactory)
    val result = objectMapper.readValue(data, Any::class.java)
    return result
  }
}
