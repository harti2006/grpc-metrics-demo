package com.github.harti2006.grpc.metrics;

import com.github.harti2006.grpc.example.HelloRequest;
import com.github.harti2006.grpc.example.MyServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "grpc.server.port=-1",
                "grpc.server.in-process-name=test",
                "grpc.client.myService.negotiation-type=plaintext",
                "grpc.client.myService.address=in-process:test",
        })
class GrpcMetricsDemoApplicationTests {

    @GrpcClient("myService")
    private MyServiceGrpc.MyServiceBlockingStub myServiceStub;

    @Autowired
    private TestRestTemplate http;

    private final BasicJsonTester json = new BasicJsonTester(getClass());

    private JsonContent<Object> getJsonContent(String endpoint) {
        final String response = http.getForObject(endpoint, String.class);
        assertThat(response).describedAs("response of " + endpoint).isNotBlank();
        System.out.println("response of " + endpoint);
        System.out.println(response);
        System.out.println();
        return json.from(response);
    }

    private ObjectAssert<Number> assertThatCountMeasurement(JsonContent<Object> jsonContent) {
        return assertThat(jsonContent)
                .<Number>extractingJsonPathArrayValue("$.measurements[?(@.statistic==\"COUNT\")].value")
                .first();
    }

    @Test
    void reproduceBug() {
        String sayHelloProcessingMetrics = "/actuator/metrics/grpc.server.processing.duration?tag=method:SayHello&tag=service:com.github.harti2006.grpc.example.MyService";
        assertThatCountMeasurement(getJsonContent(sayHelloProcessingMetrics)).isEqualTo(0.0);

        try {
            //noinspection ResultOfMethodCallIgnored
            myServiceStub.sayHello(HelloRequest.getDefaultInstance());
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
        }

        assertThatCountMeasurement(getJsonContent(sayHelloProcessingMetrics)).isEqualTo(1.0);

        assertThatCountMeasurement(getJsonContent(sayHelloProcessingMetrics + "&tag=statusCode:OK")).isEqualTo(0.0);

        assertThatCountMeasurement(getJsonContent(sayHelloProcessingMetrics + "&tag=statusCode:NOT_FOUND")).isEqualTo(1.0);
    }

    @Test
    void reproduceBugWithoutGrpcAdvice() {
        String sayGoodByeProcessingMetrics = "/actuator/metrics/grpc.server.processing.duration?tag=method:SayGoodBye&tag=service:com.github.harti2006.grpc.example.MyService";
        assertThatCountMeasurement(getJsonContent(sayGoodByeProcessingMetrics)).isEqualTo(0.0);

        try {
            //noinspection ResultOfMethodCallIgnored
            myServiceStub.sayGoodBye(HelloRequest.getDefaultInstance());
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
        }

        assertThatCountMeasurement(getJsonContent(sayGoodByeProcessingMetrics)).isEqualTo(1.0);

        assertThatCountMeasurement(getJsonContent(sayGoodByeProcessingMetrics + "&tag=statusCode:OK")).isEqualTo(0.0);

        assertThatCountMeasurement(getJsonContent(sayGoodByeProcessingMetrics + "&tag=statusCode:NOT_FOUND")).isEqualTo(1.0);
    }
}
