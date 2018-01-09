package com.pradas.jopmatester;

import main.java.com.pradas.jopma.protocol.Grant;
import main.java.com.pradas.jopma.protocol.GrantFactory;
import main.java.com.pradas.jopma.protocol.ResourceOwnerGrant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RequestController {

    @GetMapping("/")
    public String requestForm(Model model) {
        model.addAttribute("request", new Request());
        return "request";
    }

    @PostMapping("/request")
    public String requestSubmit(@ModelAttribute Request request, Model model) {

        GrantFactory gf = new GrantFactory();

        Grant g = gf.getGrant(request.getGrantType());

        if (request.getGrantType().equals("resourceownergrant") && !((ResourceOwnerGrant) g).hasValidToken()) {
            RequestAuth ra = new RequestAuth();
            ra.setGrantType(request.getGrantType());
            ra.setUrl(request.getUrl());
            ra.setParameters(request.getParameters());
            ra.setType(request.getType());
            ra.setHeaders(request.getHeaders());
            ra.setBody(request.getBody());
            model.addAttribute("requestAuth", ra);
            return "authentication";
        }

        model.addAttribute("response", g.makeRequest(
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
        GrantFactory gf = new GrantFactory();

        Grant g = gf.getGrant(requestAuth.getGrantType());

        ((ResourceOwnerGrant) g).addUserCredentials(requestAuth.getUsername(), requestAuth.getPassword());

        model.addAttribute("response", g.makeRequest(
                requestAuth.getUrl(),
                requestAuth.getParameters(),
                requestAuth.getType(),
                requestAuth.getHeaders(),
                requestAuth.getBody()
        ));

        return "result";
    }

}