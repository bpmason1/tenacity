//package what.should.i.be

import colossus.protocols.http.HttpCode;
import colossus.protocols.http.HttpRequest;
import colossus.protocols.http.HttpResponse;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.apache.commons.lang3.reflect.FieldUtils;

@FunctionalInterface
public interface TenacityHandlerFunction<RQ, RS, P> {
    RS apply(RQ fn, P[] param);
}

public class Tenacity {

    private static final int[] codesToRetry = {500, 502, 503};
    private static final int sleepMs = 10;
    private static short retries = 3;

    public static Function<HttpRequest, HttpResponse> retry(Function<HttpRequest, HttpResponse> fn) {
        return (request) -> {
            HttpResponse response;
            for(int i = 0; i <= retries; ++i) {
                response = fn.apply(request);

                try{
                    int status = (int)FieldUtils.readField(response.code(), "code", true);
                    if(IntStream.of(codesToRetry).noneMatch(x -> x == status)) {
                        return response;
                    }
                    Thread.sleep(sleepMs * i);
                } catch(Exception ex) {
                  System.out.println(ex);
                }
            }
            return response;
        };
    }


    public static TenacityHandlerFunction<HttpRequest, HttpResponse, String> retry(TenacityHandlerFunction<HttpRequest, HttpResponse, String> fn) {
        return (HttpRequest request, String params) -> {
            HttpResponse response;
            for(int i = 0; i <= retries; ++i) {
                response = fn.apply(request, params);

                try{
                    int status = (int)FieldUtils.readField(response.code(), "code", true);
                    if(IntStream.of(codesToRetry).noneMatch(x -> x == status)) {
                        return response;
                    }
                    Thread.sleep(sleepMs * i);
                } catch(Exception ex) {
                  System.out.println(ex);
                }
            }
            return response;
        };
    }

}

