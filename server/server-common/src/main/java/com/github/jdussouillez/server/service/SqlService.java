package com.github.jdussouillez.server.service;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Function;
import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Select;
import org.jooq.conf.ParamType;

@ApplicationScoped
public class SqlService {

    @Getter
    @Inject
    protected DSLContext dslContext;

    @Getter
    @Inject
    protected PgPool dbPool;

    public <T> Multi<T> select(final Select<?> query, final Function<Row, T> mapper) {
        return dbPool.query(getSQL(query))
            .mapping(mapper::apply)
            .execute()
            .onItem()
            .transformToMulti(RowSet::toMulti);
    }

    public <T> Multi<T> selectStream(final Select<?> query, final Function<Row, T> mapper) {
        return dbPool.getConnection()
            .call(connection -> connection.begin()
                // OPTIMIZE: find a better way to open a transaction and commit it when the Multi completes
                .withContext((tr, ctx) -> tr.invoke(t -> ctx.put("tr", t)))
            )
            .chain(connection -> connection.prepare(getSQL(query)))
            .onItem()
            .transformToMulti(statement -> statement.createStream(500).toMulti())
            .map(mapper::apply)
            .withContext((multi, ctx) -> multi
                .onCompletion()
                .call(() -> ((Transaction) ctx.get("tr")).commit())
            );
    }

    private static String getSQL(final Query query) {
        return query.getSQL(ParamType.INLINED);
    }
}
