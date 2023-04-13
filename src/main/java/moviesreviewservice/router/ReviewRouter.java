package moviesreviewservice.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import moviesreviewservice.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * This class going to take the router equivalent to endpoint in the annoted controller
 */

@Configuration
public class ReviewRouter {

  @Bean
  public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler) {
    return route()
        .nest(path("/v1/reviews"), builder ->
            builder.POST("", request -> reviewHandler.addReview(request))
                .GET("", request -> reviewHandler.getReviews(request))
                .PUT("/{id}",request -> reviewHandler.updateReview(request))
                .DELETE("/{id}",request ->reviewHandler.deleteReview(request)))
        .GET("/v1/helloworld", (request -> ServerResponse.ok().bodyValue("helloworld")))
        /*.POST("/v1/reviews",request ->reviewHandler.addReview(request))
        .GET("/v1/reviews",request ->reviewHandler.getReviews(request))*/
        .build();
  }

}
