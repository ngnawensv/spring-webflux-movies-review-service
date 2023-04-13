import java.util.List;
import moviesreviewservice.domain.Review;
import moviesreviewservice.exeception.ReviewDataException;
import moviesreviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import moviesreviewservice.MoviesReviewServiceApplication;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(classes = MoviesReviewServiceApplication.class,webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReviewsIntgTest {

  @Autowired
  WebTestClient webTestClient;

  @Autowired
  ReviewRepository reviewRepository;

  static String URL_REVIEW = "/v1/reviews";

  @BeforeEach
  void setUp() {
    var reviewList = List.of(
        new Review(null,1L,"Awersome Movie",9.0),
        new Review(null,1L,"Awersome Movie1",9.0),
        new Review(null,2L,"Excellent Movie",8.0)
    );
    reviewRepository.saveAll(reviewList).blockLast();
  }

  @AfterEach
  void tearDown() {
    reviewRepository.deleteAll().block();
  }

  @Test
  void addReview() {
    //given
    var review=new Review(null,1L,"Awersome Movie",9.0);
    //when
    webTestClient
        .post()
        .uri(URL_REVIEW)
        .bodyValue(review)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Review.class)
        .consumeWith(movieInfoEntityExchangeResult -> {
          var saveReview = movieInfoEntityExchangeResult.getResponseBody();
          assert saveReview != null;
          assert saveReview.getReviewId() != null;
        });
    //then
  }

  @Test
  void getAllReviews(){
    webTestClient
        .get()
        .uri(URL_REVIEW)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Review.class)
        .hasSize(3);
  }

  @Test
  void getAllReviewsByMovieInfoId(){
    var uri = UriComponentsBuilder.fromUriString(URL_REVIEW)
        .queryParam("movieInfoId",2L)
        .buildAndExpand().toUri();
    webTestClient
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Review.class)
        .hasSize(1);
  }
}
