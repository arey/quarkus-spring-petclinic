package org.springframework.samples.petclinic.system;

import io.quarkus.qute.Template;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jboss.logging.Logger;

class ExceptionMappers {

	private static final Logger LOG = Logger.getLogger(LocalDateParamConverter.class);

	private final Template error;

	ExceptionMappers(Template error) {
		this.error = error;
	}

	@ServerExceptionMapper
	public Response mapException(RuntimeException exception) {
		LOG.error("Internal server error", exception);
		return Response.ok(error.data("message", exception.getMessage())).build();
	}
}
