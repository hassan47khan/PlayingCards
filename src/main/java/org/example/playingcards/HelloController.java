package org.example.playingcards;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Controller for hello.fxml: the 24 card game*/

public class HelloController {
    private static final String[] SUITS = {"clubs", "diamonds", "hearts", "spades"};

    /**
     * ranks: Ace = 1, 2-10, Ace = 11, Jack = 12, Queen = 13, King = 14
     */

    private record Card(int rank, String suit) {
        String filename() {
            String name = switch (rank) {
                case 1 -> "ace";
                case 11 -> "jack";
                case 12 -> "queen";
                case 13 -> "king";
                default -> String.valueOf(rank);
            };

            return name + " _of_" + suit + ".png"; //e.g 6_of_diamonds.png
        }
    }

    @FXML private ImageView card1, card2, card3, card4;
    @FXML private TextField tfSoltuion;
    @FXML private TextField tfExpression;

    private ImageView[] cardViews;
    private List<Card> hand = new ArrayList<>();

    /** Called automatically by the FXML loader */
    @FXML
    private void initialize() {
        cardViews = new ImageView[]{card1, card2, card3, card4};
        dealNewHand();
    }

    // -------------------- Button handlers --------------------

    @FXML
    private void onRefresh(){
        dealNewHand();
        tfExpression.requestFocus();
    }

    @FXML
    private void onFindSolution(){
        String solution = Expr24solve(cardValues());
        tfSoltuion.setText(solution != null ? solution : "No solution");
    }
    private void onVerify(){
        Expr24.Result result;
        try{
            result = Expr24.evaluate(tfExpression.getText());
        } catch (Expr24.ExprException ex) {
           show(Alert.AlertType.ERROR, "Invalid expression", ex.getMessage());
           return;
        }
        List<Integer> used = new ArrayList<>(result.numbers());
        List<Integer> expected = new ArrayList<>();
        for (int v : cardValues()) expected.add(v);
        Collections.sort(used);
        Collections.sort(expected);

        if (!used.equals(expected)){
            show(Alert.AlertType.WARNING, "Wrong numbers"
                    "Use exactly the four card values, each once: " +expected
                            + "\nYour expression used: " + used);
        } else if (!result.value().equals(Expr24.TARGET)) {
            show(Alert.AlertType.WARNING, "Not 24",
                    "Your expression evaluates to " + result.value() + ", not 24.");
        } else {
            show(Alert.AlertType.INFORMATION, "Correct!", "Your expression evaluates to 24.");
        }
    }

// ---------------------- Helpers

private void dealNewHand() {
    List<Card> cards = new ArrayList<>();

for (String suit : SUITS) {
}
