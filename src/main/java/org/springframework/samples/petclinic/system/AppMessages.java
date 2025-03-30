package org.springframework.samples.petclinic.system;

import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@MessageBundle(value = "messages", locale = "en")
public interface AppMessages {

	@Message
	String welcome();

	@Message
	String required();

	@Message
	String notFound();

	@Message
	String duplicate();

	@Message
	String nonNumeric();

	@Message
	String duplicateFormSubmission();

	@Message
	String typeMismatch_date();

	@Message
	String typeMismatch_birthDate();

}
