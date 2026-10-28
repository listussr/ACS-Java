package ru.acs.rag.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/** Вспомогательный класс: вывод сообщений пользователю через <h:messages/>. */
final class Messages {

    private Messages() {
    }

    static void info(String text) {
        add(FacesMessage.SEVERITY_INFO, text);
    }

    /** Показывает причину ошибки. */
    static void error(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        add(FacesMessage.SEVERITY_ERROR, "Ошибка: " + cause.getMessage());
    }

    /** Сообщение переживёт redirect на другую страницу. */
    static void keepAfterRedirect() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
    }

    private static void add(FacesMessage.Severity severity, String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, text, null));
    }
}
