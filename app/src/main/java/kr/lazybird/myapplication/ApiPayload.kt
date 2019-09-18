package kr.lazybird.myapplication

import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

interface JSONConvertable {
    fun toJSON(): String = Gson().toJson(this)
}

inline fun <reified T: JSONConvertable> String.toObject(): T = Gson().fromJson(this, T::class.java)

data class Agent (
    @SerializedName("name") val name: String,
    @SerializedName("device_access_token") val deviceAccessToken: String,
    @SerializedName("status") val status: String
) : JSONConvertable

data class Payload (
    @SerializedName("account") val account: String,
    @SerializedName("password") val password: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("agent_list") val agentList: ArrayList<Agent>
) : JSONConvertable

//From JSON
//val json = "..."
//val object = json.toObject<User>()
//
//// To JSON
//val json = object.toJSON()