package com.hamzalahlou.tp1.llm;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;

@ApplicationScoped
public class LlmClientPourGemini implements Serializable {

    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent";

    private String cle;
    private transient Client clientRest;

    public LlmClientPourGemini() {
        cle = System.getenv("GEMINI_KEY");
        clientRest = ClientBuilder.newClient();
    }

    public String envoyerRequete(String requeteJson) throws RequeteException {
        if (cle == null) {
            throw new RequeteException("GEMINI_KEY pas definie");
        }
        WebTarget target = clientRest.target(URL).queryParam("key", cle);
        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(requeteJson, MediaType.APPLICATION_JSON));
        String corps = response.readEntity(String.class);
        if (response.getStatus() != 200) {
            throw new RequeteException(response.getStatus() + " : " + corps);
        }
        return corps;
    }

    @PreDestroy
    public void close() {
        if (clientRest != null) {
            clientRest.close();
        }
    }
}
