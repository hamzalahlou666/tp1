package com.hamzalahlou.tp1.bb;

import com.hamzalahlou.tp1.json.JsonAdapterPourGemini;
import com.hamzalahlou.tp1.json.LlmInteraction;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Named
@ViewScoped
public class Bb implements Serializable {

    private Map<String, String> roles = new LinkedHashMap<>();

    private String roleKey = "Helpful assistant";
    private boolean roleSystemeChangeable = true;

    private String question;
    private String reponse;
    private String conversation = "";

    private boolean debug;
    private String texteRequeteJson;
    private String texteReponseJson;

    @Inject
    private JsonAdapterPourGemini jsonAdapter;

    public Bb() {
        roles.put("Helpful assistant",
                "You are a helpful assistant. You help the user to find the information they need. "
                        + "If the user type a question, you answer it.");
        roles.put("Traducteur anglais-français",
                "You are an interpreter. You translate from English to French and from French to English. "
                        + "If the user type a French text, you translate it into English. "
                        + "If the user type an English text, you translate it into French. "
                        + "If the text contains only one to three words, give some examples of usage of these words "
                        + "in English and in French. Make a bullet list of the examples.");
        roles.put("Guide touristique",
                "You are a tourist guide. If the user type the name of a country, you give two of the most "
                        + "interesting places to visit there, with the approximate distances between them. "
                        + "If the user type the name of a place, you give its approximate distance to the country's "
                        + "other major sights.");
        // role perso pour le bonus
        roles.put("Grand-mère marocaine",
                "Tu es une grand-mere marocaine. Tu reponds en francais avec des mots en darija, "
                        + "tu dis 'a wldi' a l'utilisateur, tu donnes un proverbe marocain et tu proposes un the a la fin.");
    }

    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }
        if (roleSystemeChangeable) {
            jsonAdapter.setSystemRole(roles.get(roleKey));
        }
        try {
            LlmInteraction interaction = jsonAdapter.envoyerRequete(question);
            reponse = interaction.reponseExtraite();
            texteRequeteJson = interaction.questionJson();
            texteReponseJson = interaction.reponseJson();
            roleSystemeChangeable = false;
            afficherConversation();
            question = "";
        } catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Probleme de connexion avec l'API du LLM", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
        return null;
    }

    public String nouveauChat() {
        return "index?faces-redirect=true";
    }

    private void afficherConversation() {
        conversation += "== User:\n" + question + "\n== Assistant:\n" + reponse + "\n";
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation;
    }

    public void setConversation(String conversation) {
        this.conversation = conversation;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }

    public void setTexteRequeteJson(String texteRequeteJson) {
        this.texteRequeteJson = texteRequeteJson;
    }

    public String getTexteReponseJson() {
        return texteReponseJson;
    }

    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }
}
