package org.tensorflow.demo;

public class SavedWord {
    String wordId;
    String wordSaved;
    String languageSaved;
    String apiLanguage;


    public SavedWord(){

    }


    public SavedWord(String wordId, String wordSaved, String languageSaved, String apiLanguage) {
        this.wordId = wordId;
        this.wordSaved = wordSaved;
        this.languageSaved = languageSaved;
        this.apiLanguage = apiLanguage;
    }

    public String getWordId() {
        return wordId;
    }

    public String getWordSaved() {
        return wordSaved;
    }

    public String getLanguageSaved() {
        return languageSaved;
    }
    public String getApiLanguage() {
        return apiLanguage;
    }

}
