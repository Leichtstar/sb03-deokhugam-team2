package com.twogether.deokhugam.book.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.twogether.deokhugam.apiclient.NaverBookClient;
import com.twogether.deokhugam.book.dto.NaverBookDto;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookInfoController.class)
class BookInfoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NaverBookClient naverBookClient;

  @Test
  @DisplayName("ISBN으로 도서 정보 조회")
  void getBookInfoByIsbn() throws Exception {
    // given
    String isbn = "9788960773417";
    NaverBookDto bookDto = new NaverBookDto(
        "더쿠의 심리학",
        "박인규",
        "더쿠에 대한 심도깊은 해설",
        "이북리더즈",
        LocalDate.of(1989, 5, 12),
        isbn,
        null // 썸네일은 차후 구현
    );

    given(naverBookClient.fetchInfoByIsbn(isbn)).willReturn(bookDto);

    // when & then
    mockMvc.perform(get("/api/books/info")
            .header("X-Naver-Client-Id", "apiId")
            .header("X-Naver-Client-Secret", "secretkey")
            .param("isbn", isbn)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("더쿠의 심리학"))
        .andExpect(jsonPath("$.author").value("박인규"))
        .andExpect(jsonPath("$.description").value("더쿠에 대한 심도깊은 해설"))
        .andExpect(jsonPath("$.publisher").value("이북리더즈"))
        .andExpect(jsonPath("$.isbn").value(isbn));
  }
}