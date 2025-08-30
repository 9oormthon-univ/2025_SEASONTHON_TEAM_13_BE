package cloud.emusic.emotionmusicapi.controller;

import cloud.emusic.emotionmusicapi.dto.request.HelloRequest;
import cloud.emusic.emotionmusicapi.dto.response.HelloResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/hello")
public class HelloController {
    @GetMapping("/parameter")//parmeterObject
    public HelloResponse parameter(@ParameterObject HelloRequest request) {
        return new HelloResponse("Hello, " + request.getName() + "!");
    }

    @GetMapping("/json")
    public HelloResponse json(HelloRequest request) {
        return new HelloResponse("Hello, " + request.getName() + "!");
    }

}
