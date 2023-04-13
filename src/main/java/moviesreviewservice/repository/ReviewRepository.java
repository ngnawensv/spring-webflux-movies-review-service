package moviesreviewservice.repository;

import moviesreviewservice.domain.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReviewRepository extends ReactiveMongoRepository<Review,String> {
Flux<Review> findReviewsByMovieInfoId(Long movieInfoId);
}
