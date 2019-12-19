package org.kablambda.frpspringboot;

import nz.sodium.Cell;
import nz.sodium.Stream;
import nz.sodium.StreamSink;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class RestEndpoint {
    StreamSink<Action>  actionStream;

    private static class Action {
        final DeferredResult<ResponseEntity<String>> output;
        final String p;

        private Action(final DeferredResult<ResponseEntity<String>> output, final String p) {
            this.output = output;
            this.p = p;
        }
    }
    public RestEndpoint() {
        actionStream = new StreamSink<>();
        actionStream.map(action -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Action(action.output, action.p.toUpperCase() + System.currentTimeMillis());
        }).listen(a -> a.output.setResult(ResponseEntity.ok(a.p)));
    }

    @RequestMapping(method = GET, path = "test")
    public String test(@RequestParam(value="p") String p) {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return p.toUpperCase();
    }

    @RequestMapping(method = GET, path = "test-a")
    public DeferredResult<ResponseEntity<String>> async(@RequestParam(value="p") String p) {
        DeferredResult<ResponseEntity<String>> output = new DeferredResult<>();
        actionStream.send(new Action(output, p));
        return output;
    }
}
