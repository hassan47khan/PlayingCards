module org.example.playingcards {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.playingcards to javafx.fxml;
    exports org.example.playingcards;
}