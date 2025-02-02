package com.auth0.android.request.internal

import com.auth0.android.result.UserIdentity
import com.auth0.android.result.UserProfile
import com.auth0.android.util.UserIdentityMatcher
import com.auth0.android.util.UserProfileMatcher
import com.google.gson.JsonParseException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.hamcrest.collection.IsMapWithSize
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*

public class UserProfileGsonTest : GsonBaseTest() {

    @Before
    public fun setUp() {
        gson = GsonProvider.gson
    }

    @Test
    @Throws(Exception::class)
    public fun shouldFailWithInvalidJson() {
        Assert.assertThrows(JsonParseException::class.java) {
            pojoFrom(json(INVALID), UserProfile::class.java)
        }
    }

    @Test
    @Throws(Exception::class)
    public fun shouldFailWithEmptyJson() {
        Assert.assertThrows(JsonParseException::class.java) {
            pojoFrom(json(EMPTY_OBJECT), UserProfile::class.java)
        }
    }

    @Test
    @Throws(Exception::class)
    public fun shouldHandleNullBooleans() {
        val userProfile = pojoFrom(
            StringReader(
                """{"email_verified": null}"""
            ), UserProfile::class.java
        )
        assertThat(userProfile, `is`(notNullValue()))
    }

    @Test
    @Throws(Exception::class)
    public fun shouldNotRequireUserId() {
        val userProfile = pojoFrom(
            StringReader(
                """{
  "picture": "https://secure.gravatar.com/avatar/cfacbe113a96fdfc85134534771d88b4?s=480&r=pg&d=https%3A%2F%2Fssl.gstatic.com%2Fs2%2Fprofiles%2Fimages%2Fsilhouette80.png",
  "name": "info @ auth0",
  "nickname": "a0",
  "identities": [
    {
      "user_id": "1234567890",
      "provider": "auth0",
      "connection": "Username-Password-Authentication",
      "isSocial": false
    }
  ],
  "created_at": "2014-07-06T18:33:49.005Z"
}"""
            ), UserProfile::class.java
        )
        assertThat(userProfile, `is`(notNullValue()))
    }

    @Test
    @Throws(Exception::class)
    public fun shouldNotRequireName() {
        val userProfile = pojoFrom(
            StringReader(
                """{
  "picture": "https://secure.gravatar.com/avatar/cfacbe113a96fdfc85134534771d88b4?s=480&r=pg&d=https%3A%2F%2Fssl.gstatic.com%2Fs2%2Fprofiles%2Fimages%2Fsilhouette80.png",
  "nickname": "a0",
  "user_id": "auth0|1234567890",
  "identities": [
    {
      "user_id": "1234567890",
      "provider": "auth0",
      "connection": "Username-Password-Authentication",
      "isSocial": false
    }
  ],
  "created_at": "2014-07-06T18:33:49.005Z"
}"""
            ), UserProfile::class.java
        )
        assertThat(userProfile, `is`(notNullValue()))
    }

    @Test
    @Throws(Exception::class)
    public fun shouldNotRequireNickname() {
        val userProfile = pojoFrom(
            StringReader(
                """{
  "picture": "https://secure.gravatar.com/avatar/cfacbe113a96fdfc85134534771d88b4?s=480&r=pg&d=https%3A%2F%2Fssl.gstatic.com%2Fs2%2Fprofiles%2Fimages%2Fsilhouette80.png",
  "name": "info @ auth0",
  "user_id": "auth0|1234567890",
  "identities": [
    {
      "user_id": "1234567890",
      "provider": "auth0",
      "connection": "Username-Password-Authentication",
      "isSocial": false
    }
  ],
  "created_at": "2014-07-06T18:33:49.005Z"
}"""
            ), UserProfile::class.java
        )
        assertThat(userProfile, `is`(notNullValue()))
    }

    @Test
    @Throws(Exception::class)
    public fun shouldNotRequirePicture() {
        val userProfile = pojoFrom(
            StringReader(
                """{
  "name": "info @ auth0",
  "nickname": "a0",
  "user_id": "auth0|1234567890",
  "identities": [
    {
      "user_id": "1234567890",
      "provider": "auth0",
      "connection": "Username-Password-Authentication",
      "isSocial": false
    }
  ],
  "created_at": "2014-07-06T18:33:49.005Z"
}"""
            ), UserProfile::class.java
        )
        assertThat(userProfile, `is`(notNullValue()))
    }

    @Test
    @Throws(Exception::class)
    public fun shouldReturnOAuthProfile() {
        val profile = pojoFrom(json(PROFILE_OAUTH), UserProfile::class.java)
        assertThat(
            profile.getId(),
            `is`("google-oauth2|9883254263433883220")
        )
        assertThat(profile.name, `is`(nullValue()))
        assertThat(profile.nickname, `is`(nullValue()))
        assertThat(profile.pictureURL, `is`(nullValue()))
        assertThat(
            profile.getIdentities(), `is`(
                emptyCollectionOf(
                    UserIdentity::class.java
                )
            )
        )
        assertThat(profile.getUserMetadata(), IsMapWithSize.anEmptyMap())
        assertThat(profile.getAppMetadata(), IsMapWithSize.anEmptyMap())
        assertThat(profile.getExtraInfo(), IsMapWithSize.aMapWithSize(1))
        assertThat(
            profile.getExtraInfo(),
            hasEntry("sub", "google-oauth2|9883254263433883220" as Any)
        )
    }

    @Test
    @Throws(Exception::class)
    public fun shouldReturnProfileWithOnlyRequiredValues() {
        val profile = pojoFrom(json(PROFILE_BASIC), UserProfile::class.java)
        assertThat(
            profile,
            UserProfileMatcher.isNormalizedProfile(ID, NAME, NICKNAME)
        )
        assertThat(profile.getIdentities(), hasSize(1))
        assertThat(
            profile.getIdentities(),
            hasItem(
                UserIdentityMatcher.isUserIdentity(
                    "1234567890",
                    "auth0",
                    "Username-Password-Authentication"
                )
            )
        )
        assertThat(profile.getUserMetadata(), IsMapWithSize.anEmptyMap())
        assertThat(profile.getAppMetadata(), IsMapWithSize.anEmptyMap())
        assertThat(profile.getExtraInfo(), IsMapWithSize.anEmptyMap())
    }

    @Test
    @Throws(Exception::class)
    public fun shouldReturnNormalizedProfile() {
        val profile = pojoFrom(json(PROFILE), UserProfile::class.java)
        assertThat(
            profile,
            UserProfileMatcher.isNormalizedProfile(ID, NAME, NICKNAME)
        )
        assertThat(profile.getIdentities(), hasSize(1))
        assertThat(
            profile.getIdentities(),
            hasItem(
                UserIdentityMatcher.isUserIdentity(
                    "1234567890",
                    "auth0",
                    "Username-Password-Authentication"
                )
            )
        )
        assertThat(profile.getUserMetadata(), IsMapWithSize.anEmptyMap())
        assertThat(profile.getAppMetadata(), IsMapWithSize.anEmptyMap())
        assertThat(profile.getExtraInfo(), notNullValue())
    }

    @Test
    @Throws(Exception::class)
    public fun shouldReturnProfileWithOptionalFields() {
        val profile = pojoFrom(json(PROFILE_FULL), UserProfile::class.java)
        assertThat(
            profile,
            UserProfileMatcher.isNormalizedProfile(ID, NAME, NICKNAME)
        )
        assertThat(profile.email, equalTo("info@auth0.com"))
        assertThat(profile.givenName, equalTo("John"))
        assertThat(profile.familyName, equalTo("Foobar"))
        assertThat(profile.isEmailVerified, `is`(false))
        assertThat(
            profile.createdAt,
            equalTo(
                SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    Locale.US
                ).parse("2014-07-06T18:33:49.005Z")
            )
        )
    }

    @Test
    @Throws(Exception::class)
    public fun shouldReturnProfileWithMultipleIdentities() {
        val profile = pojoFrom(json(PROFILE_FULL), UserProfile::class.java)
        assertThat(
            profile,
            UserProfileMatcher.isNormalizedProfile(ID, NAME, NICKNAME)
        )
        assertThat(
            profile.getIdentities(),
            hasItem(
                UserIdentityMatcher.isUserIdentity(
                    "1234567890",
                    "auth0",
                    "Username-Password-Authentication"
                )
            )
        )
        assertThat(
            profile.getIdentities(),
            hasItem(
                UserIdentityMatcher.isUserIdentity(
                    "999997950999976",
                    "facebook",
                    "facebook"
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    public fun shouldReturnProfileWithExtraInfo() {
        val profile = pojoFrom(json(PROFILE_FULL), UserProfile::class.java)
        assertThat(
            profile,
            UserProfileMatcher.isNormalizedProfile(ID, NAME, NICKNAME)
        )
        assertThat(
            profile.getExtraInfo(),
            hasEntry("multifactor", listOf("google-authenticator"))
        )
        assertThat(
            profile.getExtraInfo(),
            not(
                anyOf(
                    hasKey("user_id"),
                    hasKey("name"),
                    hasKey("nickname"),
                    hasKey("picture"),
                    hasKey("email"),
                    hasKey("created_at")
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    public fun shouldReturnProfileWithMetadata() {
        val profile = pojoFrom(json(PROFILE_FULL), UserProfile::class.java)
        assertThat(
            profile,
            UserProfileMatcher.isNormalizedProfile(ID, NAME, NICKNAME)
        )
        assertThat(
            profile.getUserMetadata(),
            hasEntry("first_name", "Info" as Any)
        )
        assertThat(
            profile.getUserMetadata(),
            hasEntry("last_name", "Auth0" as Any)
        )
        assertThat(
            profile.getUserMetadata(),
            hasEntry("first_name", "Info" as Any)
        )
        assertThat(
            profile.getAppMetadata(),
            hasEntry("role", "admin" as Any)
        )
        assertThat(profile.getAppMetadata(), hasEntry("tier", 2.0 as Any))
        assertThat(
            profile.getAppMetadata(),
            hasEntry("blocked", false as Any)
        )
    }

    private companion object {
        private const val NICKNAME = "a0"
        private const val NAME = "info @ auth0"
        private const val ID = "auth0|1234567890"
        private const val PROFILE_OAUTH = "src/test/resources/profile_oauth.json"
        private const val PROFILE_FULL = "src/test/resources/profile_full.json"
        private const val PROFILE_BASIC = "src/test/resources/profile_basic.json"
        private const val PROFILE = "src/test/resources/profile.json"
    }
}