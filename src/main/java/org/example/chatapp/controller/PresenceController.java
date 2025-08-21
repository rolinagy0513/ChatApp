package org.example.chatapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chatapp.service.impl.PresenceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/Presence")
@Slf4j
public class PresenceController {

    private final PresenceService service;

    @GetMapping("/getAll")
    public Set<String> getAllOnlineUsers(){
        return service.getOnlineUsers();
    }

}
