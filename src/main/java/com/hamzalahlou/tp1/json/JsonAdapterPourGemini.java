package com.hamzalahlou.tp1.json;

import com.hamzalahlou.tp1.llm.LlmClientPourGemini;
import com.hamzalahlou.tp1.llm.RequeteException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

@Dependent
public class JsonAdapterPourGemini implements Serializable {

    @Inject
    private LlmClientPourGemini llmClient;

    private String systemRole;

    // json de la conversation (on renvoie tout a chaque fois)
    private String requeteJson;

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
    }

    public LlmInteraction envoyerRequete(String question) throws RequeteException {
        if (requeteJson == null) {
            requeteJson = creerRequeteJson(systemRole, question);
        } else {
            requeteJson = ajouteQuestionDansJsonRequete(requeteJson, question);
        }
        String reponseJson = llmClient.envoyerRequete(requeteJson);
        String reponse = extractReponse(reponseJson);
        LlmInteraction interaction = new LlmInteraction(prettyPrinting(requeteJson), prettyPrinting(reponseJson), reponse);
        requeteJson = ajouterMessage(requeteJson, "model", reponse);
        return interaction;
    }

    private JsonObject message(String role, String texte) {
        return Json.createObjectBuilder()
                .add("role", role)
                .add("parts", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder().add("text", texte)))
                .build();
    }

    private JsonObject lire(String json) {
        JsonReader reader = Json.createReader(new StringReader(json));
        JsonObject obj = reader.readObject();
        reader.close();
        return obj;
    }

    private String creerRequeteJson(String role, String question) {
        JsonObject requete = Json.createObjectBuilder()
                .add("system_instruction", Json.createObjectBuilder()
                        .add("parts", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("text", role))))
                .add("contents", Json.createArrayBuilder().add(message("user", question)))
                .build();
        return requete.toString();
    }

    private String ajouteQuestionDansJsonRequete(String json, String question) {
        return ajouterMessage(json, "user", question);
    }

    private String ajouterMessage(String json, String role, String texte) {
        JsonObject requete = lire(json);
        JsonArrayBuilder contents = Json.createArrayBuilder(requete.getJsonArray("contents"))
                .add(message(role, texte));
        return Json.createObjectBuilder(requete).add("contents", contents).build().toString();
    }

    private String extractReponse(String json) {
        JsonObject racine = lire(json);
        JsonArray candidates = racine.getJsonArray("candidates");
        JsonObject content = candidates.getJsonObject(0).getJsonObject("content");
        return content.getJsonArray("parts").getJsonObject(0).getString("text");
    }

    private String prettyPrinting(String json) {
        JsonObject obj = lire(json);
        JsonWriterFactory factory = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
        StringWriter sw = new StringWriter();
        JsonWriter writer = factory.createWriter(sw);
        writer.writeObject(obj);
        writer.close();
        return sw.toString();
    }
}
