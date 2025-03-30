package org.springframework.samples.petclinic.system;

import io.quarkus.qute.i18n.Localized;
import io.quarkus.qute.i18n.MessageBundles;

import java.util.List;
import java.util.Locale;

public class I18nHelper {

	private I18nHelper() {
		// Helper class
	}

	public static AppMessages lookupAppMessages(String acceptLanguage) {
		List<Locale.LanguageRange> languageRanges = Locale.LanguageRange.parse(acceptLanguage);
		Locale locale = Locale.lookup(languageRanges,
				List.of(Locale.forLanguageTag("en"), Locale.forLanguageTag("de"), Locale.forLanguageTag("fa"),
						Locale.forLanguageTag("ko"), Locale.forLanguageTag("pt"), Locale.forLanguageTag("ru"),
						Locale.forLanguageTag("tr")));
		if (locale == null) {
			locale = Locale.forLanguageTag("en");
		}
		return MessageBundles.get(AppMessages.class, Localized.Literal.of(locale.toLanguageTag()));
	}

}
