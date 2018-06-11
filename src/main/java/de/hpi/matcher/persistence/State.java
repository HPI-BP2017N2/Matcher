package de.hpi.matcher.persistence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class State {

    @Id private final long shopId;
    private final byte phase;
    private final List<Integer> imageIds;

}
