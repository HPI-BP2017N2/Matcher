package de.hpi.matcher.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonPropertyOrder({ "code", "status", "message"})
public class ErrorResponse extends AbstractResponse {

    public ErrorResponse() {
        super();
        setMessage("HTTP resquest not allowed when matching from queue.");
    }

    public ResponseEntity<Object> send() {
        return super.send(HttpStatus.FORBIDDEN);
    }

}