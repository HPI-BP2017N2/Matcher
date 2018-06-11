package de.hpi.matcher.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonPropertyOrder({ "code", "status", "data", "message" })
@Getter
@Setter(AccessLevel.PRIVATE)
public class SuccessResponse extends AbstractResponse {

    public ResponseEntity<Object> send() {
        return super.send(HttpStatus.OK);
    }
}