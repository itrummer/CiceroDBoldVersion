package web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;

@Controller
@SpringBootApplication
public class HomeController {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(HomeController.class, args);
    }

}
