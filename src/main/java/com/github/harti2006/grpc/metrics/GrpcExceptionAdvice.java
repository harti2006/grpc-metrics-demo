package com.github.harti2006.grpc.metrics;

import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

import java.util.NoSuchElementException;

@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler
    public Status handleNotFound(NoSuchElementException e) {
        return Status.NOT_FOUND.withDescription(e.getMessage());
    }
}
