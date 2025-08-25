package com.knut4.backend.domain.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knut4.backend.domain.auth.dto.LoginRequest;
import com.knut4.backend.domain.auth.dto.SignUpRequest;
import com.knut4.backend.domain.recommendation.dto.RecommendationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HistoryIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("사용자 추천 기록이 사용자별로 페이징 조회된다")
    void historyPagingPerUser() throws Exception {
	// signup + login userA
	var signupA = new SignUpRequest("histUserA","pass12345","1990-01-01");
	mockMvc.perform(post("/api/public/auth/signup").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(signupA)))
		.andExpect(status().isOk());
	var loginA = new LoginRequest("histUserA","pass12345");
	String loginAJson = mockMvc.perform(post("/api/public/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginA)))
		.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	String tokenA = objectMapper.readTree(loginAJson).get("accessToken").asText();

	// produce 3 recommendations for A
	for (int i=0;i<3;i++) {
	    RecommendationRequest req = new RecommendationRequest("sunny", List.of("매콤"), 10000, 37.1, 126.9);
	    mockMvc.perform(post("/api/private/recommendations").header("Authorization","Bearer "+tokenA)
		    .contentType(MediaType.APPLICATION_JSON)
		    .content(objectMapper.writeValueAsString(req)))
		    .andExpect(status().isOk());
	}

	// signup + login userB
	var signupB = new SignUpRequest("histUserB","pass12345","1990-01-01");
	mockMvc.perform(post("/api/public/auth/signup").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(signupB)))
		.andExpect(status().isOk());
	var loginB = new LoginRequest("histUserB","pass12345");
	String loginBJson = mockMvc.perform(post("/api/public/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginB)))
		.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	String tokenB = objectMapper.readTree(loginBJson).get("accessToken").asText();

	// one recommendation for B
	RecommendationRequest reqB = new RecommendationRequest("sunny", List.of("담백"), 8000, 37.2, 127.0);
	mockMvc.perform(post("/api/private/recommendations").header("Authorization","Bearer "+tokenB)
		.contentType(MediaType.APPLICATION_JSON)
		.content(objectMapper.writeValueAsString(reqB)))
		.andExpect(status().isOk());

	// query page for userA (size 2)
	String pageJson = mockMvc.perform(get("/api/private/history")
			.param("page","0").param("size","2")
			.header("Authorization","Bearer "+tokenA))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content.length()" ).value(2))
		.andReturn().getResponse().getContentAsString();
	int totalA = objectMapper.readTree(pageJson).get("totalElements").asInt();
	assertThat(totalA).isEqualTo(3);

	// ensure userB only sees 1
	String pageBJson = mockMvc.perform(get("/api/private/history").header("Authorization","Bearer "+tokenB))
		.andExpect(status().isOk())
		.andReturn().getResponse().getContentAsString();
	int totalB = objectMapper.readTree(pageBJson).get("totalElements").asInt();
	assertThat(totalB).isEqualTo(1);
    }
}
