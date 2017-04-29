package web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@SpringBootApplication
public class HomeController {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(HomeController.class, args);
    }

    @RequestMapping("/")
    public String index() {
        return "index";
    }

}
