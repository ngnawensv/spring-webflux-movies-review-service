import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static reactor.core.publisher.Mono.when;

import java.time.LocalDate;
import java.util.List;
import moviesreviewservice.MoviesReviewServiceApplication;
import moviesreviewservice.domain.Review;
import moviesreviewservice.exceptionhandler.GlobalErrorHandler;
import moviesreviewservice.handler.ReviewHandler;
import moviesreviewservice.repository.ReviewRepository;
import moviesreviewservice.router.ReviewRouter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//@WebFluxTest
@SpringBootTest(classes = MoviesReviewServiceApplication.class,webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})//this annotation allow you to inject automatically the beans
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ReviewsUnitTest {

 /* @MockBean
  ReviewRepository reviewRepository;*/
 ReviewRepository reviewRepository1 = mock(ReviewRepository.class, Mockito.RETURNS_DEEP_STUBS);
  //ReviewRepository reviewRepository2 = mock(ReviewRepository.class); //Another approch to inject mock bean
  @Autowired
  WebTestClient webTestClient;
  static String URL_REVIEW = "/v1/reviews";

  @Test
  void addReview() {
    //given
    var review=new Review(null,1L,"Awersome Movie",9.0);
    when(reviewRepository1.save(isA(Review.class)))
        .thenReturn(Mono.just(new Review("abc",1L,"Awersome Movie",9.0)));

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
  }


  @Test
  void addReview_validattion() {
    //given
    var review=new Review(null,null,"Awersome Movie",-9.0);
    when(reviewRepository1.save(isA(Review.class)))
        .thenReturn(Mono.just(new Review("abc",1L,"Awersome Movie",9.0)));

    //when
    webTestClient
        .post()
        .uri(URL_REVIEW)
        .bodyValue(review)
        .exchange()
        .expectStatus()
        .isBadRequest();

  }

  @Test
  void addReview_validattion2() {
    //given
    var review=new Review(null,null,"Awersome Movie",-9.0);
    when(reviewRepository1.save(isA(Review.class)))
        .thenReturn(Mono.just(new Review("abc",1L,"Awersome Movie",9.0)));

    //when
    webTestClient
        .post()
        .uri(URL_REVIEW)
        .bodyValue(review)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(String.class)
        .isEqualTo("rating.movieInfoId: must not be null,rating.negative: please pass non-negative value");

  }

  @Test
  void getAllReviews(){

    var movieInfos =  List.of(
        new Review(null,1L,"Awersome Movie",9.0),
        new Review(null,1L,"Awersome Movie1",9.0),
        new Review(null,2L,"Excellent Movie",8.0)
    );

    when(reviewRepository1.findAll()).thenReturn(Flux.fromIterable(movieInfos));

    webTestClient
        .get()
        .uri(URL_REVIEW)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Review.class);
        //.hasSize(3);
  }
}
