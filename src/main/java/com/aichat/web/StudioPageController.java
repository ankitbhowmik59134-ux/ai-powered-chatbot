package com.aichat.web;

import com.aichat.service.ChatStudioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudioPageController {

    private final ChatStudioService studio;

    public StudioPageController(ChatStudioService studio) {
        this.studio = studio;
    }

    @GetMapping("/")
    public String studio(Model model) {
        model.addAttribute("liveMode", studio.liveMode());
        model.addAttribute("personas", studio.personas());
        return "studio";
    }
}
