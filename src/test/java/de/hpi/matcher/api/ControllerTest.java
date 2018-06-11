package de.hpi.matcher.api;

import de.hpi.matcher.properties.MatcherProperties;
import de.hpi.matcher.services.MatcherService;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(secure = false)
@Getter(AccessLevel.PRIVATE)
public class ControllerTest {

    @Getter(AccessLevel.PRIVATE) private final static long SHOP_ID = 1234L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatcherService service;

    @MockBean
    MatcherProperties properties;

    @Test
    public void matchUnique() throws Exception {
        doNothing().when(getService()).matchShop(getSHOP_ID(), (byte)0);
        doReturn(true).when(getProperties()).isCollectTrainingData();

        getMockMvc()
                .perform(post("/matchUnique/" + SHOP_ID))
                .andExpect(status().isOk());

        verify(getService()).matchShop(getSHOP_ID(), (byte)0);
    }

    @Test
    public void doNotMatchWhenFlagSet() throws Exception {
        doNothing().when(getService()).matchShop(getSHOP_ID(), (byte)0);
        doReturn(false).when(getProperties()).isCollectTrainingData();

        getMockMvc()
                .perform(post("/matchUnique/" + SHOP_ID))
                .andExpect(status().isForbidden());
    }
}