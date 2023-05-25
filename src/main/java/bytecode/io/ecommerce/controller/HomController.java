package bytecode.io.ecommerce.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class HomController {

    @GetMapping("/")
    public String home() {
        return "You successfully logged in";
    }
}
