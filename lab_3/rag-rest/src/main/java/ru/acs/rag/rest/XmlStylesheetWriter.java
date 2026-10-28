package ru.acs.rag.rest;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Задание 5: добавить в начало каждого XML-ответа тег со ссылкой на XSL-преобразование:
 * MessageBodyWriter — это «конвертер» JAX-RS: он превращает объект, который вернул метод ресурса,
 * в байты тела ответа. Стандартный конвертер XML (на JAXB) такой тег не пишет, поэтому
 * регистрируем свой ({@code @Provider}); свои провайдеры JAX-RS предпочитает встроенным.
 */
@Provider
@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
public class XmlStylesheetWriter implements MessageBodyWriter<Object> {

    static final String XSL_PATH = "/xsl/rag.xsl";

    @Context
    private ServletContext servletContext;

    private final Map<Class<?>, JAXBContext> contexts = new ConcurrentHashMap<>();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    @Override
    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out)
            throws IOException, WebApplicationException {
        try {
            Marshaller marshaller = contexts.computeIfAbsent(type, XmlStylesheetWriter::newContext)
                    .createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

            Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<?xml-stylesheet type=\"text/xsl\" href=\""
                    + servletContext.getContextPath() + XSL_PATH + "\"?>\n");
            marshaller.marshal(value, writer);
            writer.flush();
        } catch (JAXBException | IllegalStateException e) {
            throw new WebApplicationException("Ошибка формирования XML: " + e.getMessage(), e);
        }
    }

    private static JAXBContext newContext(Class<?> type) {
        try {
            return JAXBContext.newInstance(type);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }
}
