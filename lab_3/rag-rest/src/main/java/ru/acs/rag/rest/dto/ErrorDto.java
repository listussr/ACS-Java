package ru.acs.rag.rest.dto;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Описание ошибки — возвращается вместе с кодами 400/404/500 в том же формате (XML/JSON), что и данные.
 * XML: <error status="404"> <message>Документ 99 не найден</message> <detail>...</detail> </error>
 */
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"message", "details"})
@JsonbPropertyOrder({"status", "message", "details", "apiRoot"})
public class ErrorDto {

    @XmlAttribute
    private String apiRoot;

    @XmlAttribute
    private int status;

    private String message;

    @XmlElement(name = "detail")
    private List<String> details = new ArrayList<>();

    public ErrorDto() {
    }

    public ErrorDto(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getApiRoot() { return apiRoot; }
    public void setApiRoot(String apiRoot) { this.apiRoot = apiRoot; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }
}
