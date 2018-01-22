package com.pradas.jopmatester;

import main.java.com.pradas.jopma.protocol.GrantParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@Controller
public class RequestController {

    private final static String UPLOAD_FOLDER = "FILE_UPLOADS/";

    @GetMapping("/")
    public String requestForm(Model model) {
        model.addAttribute("request", new Request());
        return "request";
    }

    @PostMapping("/request")
    public String requestSubmit(@ModelAttribute Request request, @RequestParam("file") MultipartFile file, Model model) {

        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GrantParser gp = new GrantParser(UPLOAD_FOLDER + file.getOriginalFilename());

        if (gp.needAuthentication()) {
            RequestAuth ra = new RequestAuth();
            ra.setFilePath(UPLOAD_FOLDER + file.getOriginalFilename());
            ra.setUrl(request.getUrl());
            ra.setParameters(request.getParameters());
            ra.setType(request.getType());
            ra.setHeaders(request.getHeaders());
            ra.setBody(request.getBody());
            model.addAttribute("requestAuth", ra);
            return "authentication";
        }

        model.addAttribute("response", gp.makeRequest(
                request.getUrl(),
                request.getParameters(),
                request.getType(),
                request.getHeaders(),
                request.getBody()
        ));
        return "result";
    }

    @PostMapping("/authentication")
    public String authenticationSubmit(@ModelAttribute RequestAuth requestAuth, Model model) {
        GrantParser gp = new GrantParser(requestAuth.getFilePath());

        gp.addUserCredentials(requestAuth.getUsername(), requestAuth.getPassword());

        model.addAttribute("response", gp.makeRequest(
                requestAuth.getUrl(),
                requestAuth.getParameters(),
                requestAuth.getType(),
                requestAuth.getHeaders(),
                requestAuth.getBody()
        ));

        return "result";
    }

}