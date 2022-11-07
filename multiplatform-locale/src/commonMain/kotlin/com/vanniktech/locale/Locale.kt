package com.vanniktech.locale

data class Locale(
  val language: Language,
  val country: Country?,
) {
  override fun toString() = listOfNotNull(
    language.identifier,
    country?.identifier,
  ).joinToString(separator = "-")

  /** Returns the optional [GooglePlayStoreLocale] that can be used for localizing the Google Play Store. */
  fun googlePlayStoreLocale(): GooglePlayStoreLocale? = GooglePlayStoreLocale.values().firstNotNullOfOrNull { playStoreLocale ->
    playStoreLocale.takeIf {
      val isIndonesian = it.name == "id" && language == Language.INDONESIAN
      fromOrNull(it.name, inferDefaultCountry = true) == this || isIndonesian
    }
  }

  /** Returns the optional [AppleAppStoreLocale] that can be used for localizing the Apple App Store. */
  fun appleAppStoreLocale(): AppleAppStoreLocale? = AppleAppStoreLocale.values().firstNotNullOfOrNull { appStoreLocale ->
    appStoreLocale.takeIf {
      val isIndonesian = it.name == "id" && language == Language.INDONESIAN
      val isHebrew = it.name == "he" && language == Language.HEBREW
      val isChineseTaiwan = it.name == "zh_Hant" && language == Language.CHINESE && country == Country.TAIWAN
      fromOrNull(it.name, inferDefaultCountry = true) == this || isIndonesian || isHebrew || isChineseTaiwan
    }
  }

  companion object {
    fun from(locale: String, inferDefaultCountry: Boolean) = fromOrNull(locale, inferDefaultCountry) ?: error("Can't get locale for $locale")
    fun fromOrNull(locale: String?, inferDefaultCountry: Boolean): Locale? {
      val language = Language.fromLocaleOrNull(locale)
      val country = Country.fromLocaleOrNull(locale)

      return if (language != null) {
        Locale(
          language,
          country ?: when (inferDefaultCountry) {
            true -> language.defaultCountry
            else -> null
          },
        )
      } else {
        null
      }
    }

    fun fromAndroidValuesDirectoryNameOrNull(androidValuesDirectoryName: String, inferDefaultCountry: Boolean): Locale? {
      require(androidValuesDirectoryName.startsWith("values"))

      val name = androidValuesDirectoryName.removePrefix("values").removePrefix("-")
      return when (name.isBlank()) {
        true -> Locale(Language.ENGLISH, null)
        else -> fromOrNull(name.replace("-r", "-"), inferDefaultCountry = inferDefaultCountry)
      }
    }
  }
}

internal fun localeSplit(locale: String?) = when {
  locale == null -> emptyList()
  locale.contains("-") -> locale.split("-")
  locale.contains("_") -> locale.split("_")
  else -> listOf(locale)
}
