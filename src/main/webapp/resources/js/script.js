function copierConversation() {
    var zone = document.getElementById('form:conversation');
    navigator.clipboard.writeText(zone.value);
}
