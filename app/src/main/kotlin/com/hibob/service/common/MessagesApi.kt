package com.hibob.service.common

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.hibob.authentication.User
import com.hibob.authentication.UserInfo
import com.hibob.service.bobConnector.BobClient
import jakarta.inject.Provider
import org.springframework.stereotype.Component
import java.text.MessageFormat
import java.util.*

@Component
class MessagesApi(
    private val userInfoProvider: Provider<UserInfo>,
    private val bobClient: BobClient,
) {
    // expected file names are "messages_{locale}_{country}.properties" / "messages_{locale}.properties" / "messages.properties"
    private val bundles =
        CacheBuilder.newBuilder().build(
            object : CacheLoader<Locale, Messages>() {
                override fun load(locale: Locale): Messages = Messages(ResourceBundle.getBundle("messages", locale))
            },
        )

    fun forLocale(locale: Locale): Messages = bundles.get(locale)

    fun forLoggedInUser(): Messages = forLocale(getLocale(userInfoProvider.get().user))

    fun forUser(user: User): Messages = forLocale(getLocale(user))

    private fun getLocale(user: User): Locale {
        val language = bobClient.getUserSettings(user.principal).language
        return language?.let { Locale(it) } ?: Locale.ENGLISH
    }
}

class Messages(private val bundle: ResourceBundle) {
    fun format(key: String, vararg args: Any): String {
        if (bundle.containsKey(key)) {
            return MessageFormat.format(bundle.getString(key), *args)
        } else {
            return key
        }
    }
}
