package com.github.jdussouillez.client;

import com.github.jdussouillez.client.service.ProductService;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

@QuarkusMain
public class Main implements QuarkusApplication {

    @Inject
    protected ProductService productService;

    @Override
    public int run(final String... args) throws InterruptedException {
        var limit = args.length == 1 ? Integer.parseInt(args[0]) : null;
        var counter = new AtomicInteger();
        return productService.fetch(limit)
            .onSubscription()
            .invoke(() -> Loggers.MAIN.info("Fetching {} products...", () -> limit != null ? limit : "all"))
            .onCompletion()
            .invoke(() -> Loggers.MAIN.info("Products fetched!"))
            .onFailure()
            .invoke(ex -> Loggers.MAIN.error("Error when fetching products", ex))
            .group()
            .intoLists()
            .of(500)
            .call(products -> productService.persist(products))
            .invoke(products -> {
                int nbProcessed = counter.addAndGet(products.size());
                if (nbProcessed % 10_000 == 0) {
                    Loggers.MAIN.info("Products processed: {}", nbProcessed);
                }
            })
            .collect()
            .last()
            .replaceWith(0)
            .onFailure()
            .recoverWithItem(1)
            .await()
            .indefinitely();
    }
}
