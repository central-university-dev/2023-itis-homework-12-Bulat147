package ru.shop.backend.search.api;

import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SearchApiIntegrationTest extends AbstractApiTest {

    @Test
    public void test() throws Exception {
        mockMvc.perform(get("/api/search").param("text", "some"))
                .andExpect(status().isOk());
    }

}
