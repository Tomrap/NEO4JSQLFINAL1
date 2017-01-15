package com.controller;

import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by John on 2017-01-15.
 */
@Controller
public class HomeController {
    @RequestMapping(value="/", method=GET)
    public String home() {
        return "home";
    }
}
