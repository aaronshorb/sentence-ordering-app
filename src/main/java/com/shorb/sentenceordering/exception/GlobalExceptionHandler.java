package com.shorb.sentenceordering.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("message", "You do not have access to this page.");
        return "error/403";
    }

    @ExceptionHandler({
            ResourceNotFoundException.class,
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex, Model model) {
        String message = ex instanceof ResourceNotFoundException
                ? ex.getMessage()
                : "The requested page was not found.";
        model.addAttribute("message", message);
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleServerError(Exception ex, Model model) {
        model.addAttribute("message", "Something went wrong.");
        return "error/500";
    }
}
