package moviesreviewservice.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import moviesreviewservice.domain.Review;
import moviesreviewservice.exeception.ReviewDataException;
import moviesreviewservice.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReviewHandler {

  private ReviewRepository reviewRepository;
  @Autowired
  private Validator validator;

  public ReviewHandler(ReviewRepository reviewRepository, Validator validator) {
    this.reviewRepository = reviewRepository;
  }

  public Mono<ServerResponse> addReview(ServerRequest request) {
    return request.bodyToMono(Review.class) //Extracting the request body
        .doOnNext(this::validate)
        .flatMap(reviewRepository::save) //Saving the actual review
        .flatMap(
            saveReview -> ServerResponse.status(HttpStatus.CREATED) //Building the status response
                .bodyValue(saveReview));
  }

  private void validate(Review review) {
    var constraintViolations=validator.validate(review);
    log.info("constraintViolations : {}", constraintViolations);
    if(constraintViolations.size()>0) {
      var errorMessage = constraintViolations.stream().map(ConstraintViolation::getMessage)
          .sorted()
          .collect(Collectors.joining(","));
      throw new ReviewDataException(errorMessage);
    }
  }

  public Mono<ServerResponse> getReviews(ServerRequest request) {
   var movieInfoId= request.queryParam("movieInfoId");
    Flux<Review> reviewsFlux ;
   if(movieInfoId.isPresent()){
     reviewsFlux= reviewRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
   }else {
     reviewsFlux = reviewRepository.findAll();
   }
    return ServerResponse.ok().body(reviewsFlux, Review.class);
  }

  public Mono<ServerResponse> updateReview(ServerRequest request) {
    var reviewId=request.pathVariable("id");
    var existingReview= reviewRepository.findById(reviewId);
   return existingReview
        .flatMap(review -> request.bodyToMono(Review.class)
            .map(reqReview -> {
              review.setComment(reqReview.getComment());
              review.setRating(reqReview.getRating());
              return review;
            })
            .flatMap(reviewRepository::save)
            .flatMap(saveReview ->ServerResponse.ok().bodyValue(saveReview))
        );
  }

  public Mono<ServerResponse> deleteReview(ServerRequest request) {
    var reviewId=request.pathVariable("id");
    var existingReview= reviewRepository.findById(reviewId);
    return existingReview.flatMap(review -> reviewRepository.deleteById(reviewId)
        .then(ServerResponse.noContent().build())  //Construct the new response because the delete method return the Mono<Void>
    );

  }
}
