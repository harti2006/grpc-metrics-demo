package com.github.harti2006.grpc.metrics;

import com.github.harti2006.grpc.example.HelloReply;
import com.github.harti2006.grpc.example.HelloRequest;
import com.github.harti2006.grpc.example.MyServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.NoSuchElementException;

@GrpcService
public class ExampleGrpcService extends MyServiceGrpc.MyServiceImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        throw new NoSuchElementException("Message not found");
    }

    @Override
    public void sayGoodBye(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        responseObserver.onError(Status.NOT_FOUND.withDescription("goodbye message not found").asException());
    }
}
