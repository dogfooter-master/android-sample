package kr.lazybird.myapplication

import android.content.Context
import android.util.Log
import com.android.volley.Request
import org.json.JSONObject


sealed class ApiRoute {

    val timeOut: Int
        get() {
            return 3000
        }


    val baseUrl: String
        get() {
            return "https://flowork.ai"
        }

    data class Signin(var account: String, var password: String, var ctx: Context) : ApiRoute()
    data class GetAgentList(var accessToken: String, var ctx: Context) : ApiRoute()
//    data class GetUser(var ctx: Context) : ApiRoute()
//    data class GetFeature(var householdID: Int, var ctx: Context) : ApiRoute()

    val httpMethod: Int
        get() {
            Log.d("SWS", "httpMethod ${this}")
            return when (this) {
                is Signin -> Request.Method.POST
                is GetAgentList -> Request.Method.POST
//                is GetUser -> Request.Method.GET
//                is GetFeature -> Request.Method.GET
            }
        }

    val params: HashMap<String, String>
        get() {
            return when (this) {
                is Signin -> {
                    Log.d("SWS", "params ${this.account}")
                    hashMapOf(Pair("account", this.account), Pair("password", this.password))
                }
                else -> hashMapOf()
            }
        }

    val jsonBody: ByteArray
        get() {
            val jsonBody = JSONObject()
            val jsonData = JSONObject()
            when (this) {
                is Signin -> {
                    jsonData.put("category", "public")
                    jsonData.put("service", "SignIn")
                    jsonData.put("account", this.account)
                    jsonData.put ("password", this.password)
                }
                is GetAgentList -> {
                    jsonData.put("category", "private")
                    jsonData.put("service", "GetAgentList")
                    jsonData.put("access_token", this.accessToken)
                }
                else -> {

                }
            }
            jsonBody.put("data", jsonData)
            return jsonBody.toString().toByteArray()
        }

    val headers: HashMap<String, String>
        get() {
            val map: HashMap<String, String> = hashMapOf()
            map["Accept"] = "application/json"
            return when (this) {
//                is GetUser -> {
//                    map["Authorization"] = "Bearer ${UserDefaults(this.ctx).accessToken}"
//                    map
//                }
//                is GetFeature -> {
//                    map["Authorization"] = "Bearer ${UserDefaults(this.ctx).accessToken}"
//                    map
//                }
                else -> map
            }
        }

    val url: String
        get() {
            return "$baseUrl/${when (this@ApiRoute) {
                is Signin -> "api"
                is GetAgentList -> "api"
//                is GetUser -> "account/profile"
//                is GetFeature -> "household/$householdID/feature"
            }}"
        }
}