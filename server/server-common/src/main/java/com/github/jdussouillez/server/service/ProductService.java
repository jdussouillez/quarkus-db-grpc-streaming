package com.github.jdussouillez.server.service;

import com.github.jdussouillez.server.bean.Product;
import static com.github.jdussouillez.server.jooq.Product.PRODUCT;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.TableField;

@ApplicationScoped
public class ProductService {

    @Inject
    protected DSLContext dslContext;

    @Inject
    protected SqlService sqlService;

    public Multi<Product> getAll(final Integer limit) {
        var query = dslContext
            .select(
                PRODUCT.PRODUCTS.ID,
                PRODUCT.PRODUCTS.DESIGNATION,
                PRODUCT.PRODUCTS.STOCK,
                PRODUCT.PRODUCTS.PICTURE_URL,
                PRODUCT.PRODUCTS.BLUEPRINT_URL,
                PRODUCT.PRODUCTS.WEIGHT,
                PRODUCT.PRODUCTS.VOLUME,
                PRODUCT.PRODUCTS.OBSOLETE
            )
            .from(PRODUCT.PRODUCTS);
        if (limit != null) {
            query.limit(limit);
        }
        return sqlService.selectStream(
            query,
            row -> new Product()
                .id(get(row, PRODUCT.PRODUCTS.ID))
                .designation(get(row, PRODUCT.PRODUCTS.DESIGNATION))
                .stock(get(row, PRODUCT.PRODUCTS.STOCK))
                .pictureUrl(get(row, PRODUCT.PRODUCTS.PICTURE_URL))
                .blueprintUrl(get(row, PRODUCT.PRODUCTS.BLUEPRINT_URL))
                .weight(get(row, PRODUCT.PRODUCTS.WEIGHT))
                .volume(get(row, PRODUCT.PRODUCTS.VOLUME))
                .obsolete(get(row, PRODUCT.PRODUCTS.OBSOLETE))
        );
    }

    private static <T> T get(final Row row, final TableField<?, T> field) {
        return row.get(field.getType(), field.getName());
    }
}
