package ru.acs.rag.rest;

import jakarta.ejb.EJBException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.acs.rag.rest.dto.ErrorDto;

/**
 * Обработчики ошибок (ExceptionMapper): превращают исключения в HTTP-ответ с кодом и телом ErrorDto
 */
public final class ErrorMappers {

    private ErrorMappers() {
    }

    /** Строит ответ-ошибку*/
    static Response error(int status, String message, HttpHeaders headers, UriInfo uriInfo) {
        ErrorDto dto = new ErrorDto(status, message);
        dto.setApiRoot(new DtoMapper(uriInfo).api());
        return Response.status(status).entity(dto).type(chooseType(headers)).build();
    }

    static MediaType chooseType(HttpHeaders headers) {
        for (MediaType accepted : headers.getAcceptableMediaTypes()) {
            if (accepted.isWildcardType()) {
                return MediaType.APPLICATION_JSON_TYPE;
            }
            if (accepted.isCompatible(MediaType.APPLICATION_XML_TYPE) || accepted.isCompatible(MediaType.TEXT_XML_TYPE)) {
                return MediaType.APPLICATION_XML_TYPE;
            }
            if (accepted.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                return MediaType.APPLICATION_JSON_TYPE;
            }
        }
        return MediaType.APPLICATION_JSON_TYPE;
    }

    @Provider
    public static class WebErrors implements ExceptionMapper<WebApplicationException> {
        @Context private HttpHeaders headers;
        @Context private UriInfo uriInfo;

        @Override
        public Response toResponse(WebApplicationException e) {
            return error(e.getResponse().getStatus(), e.getMessage(), headers, uriInfo);
        }
    }

    @Provider
    public static class ValidationErrors implements ExceptionMapper<ConstraintViolationException> {
        @Context private HttpHeaders headers;
        @Context private UriInfo uriInfo;

        @Override
        public Response toResponse(ConstraintViolationException e) {
            Response response = error(400, "Некорректные данные", headers, uriInfo);
            ErrorDto dto = (ErrorDto) response.getEntity();
            for (ConstraintViolation<?> v : e.getConstraintViolations()) {
                dto.getDetails().add(v.getMessage());
            }
            return response;
        }
    }

    @Provider
    public static class JsonErrors implements ExceptionMapper<ProcessingException> {
        @Context private HttpHeaders headers;
        @Context private UriInfo uriInfo;

        @Override
        public Response toResponse(ProcessingException e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            return error(400, "Некорректный JSON: " + cause.getMessage(), headers, uriInfo);
        }
    }


    @Provider
    public static class EjbErrors implements ExceptionMapper<EJBException> {
        @Context private HttpHeaders headers;
        @Context private UriInfo uriInfo;

        @Override
        public Response toResponse(EJBException e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof IllegalArgumentException) {
                return error(400, cause.getMessage(), headers, uriInfo);
            }
            return error(500, "Внутренняя ошибка: " + cause.getMessage(), headers, uriInfo);
        }
    }
}
