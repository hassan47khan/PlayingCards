module org.example.playingcards {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.playingcards to javafx.fxml;
    exports org.example.playingcards;
}