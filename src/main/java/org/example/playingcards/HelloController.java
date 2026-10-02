package org.example.playingcards;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Controller for hello.fxml: the 24 card game*/

public class HelloController {
    private static final String[] SUITS = {"clubs", "diamonds", "hearts", "spades"};

    /**
     * ranks: Ace = 1, 2-10, Jack = 11, Queen = 12, King = 13
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

            return name + "_of_" + suit + ".png"; //e.g 6_of_diamonds.png
        }
    }

    @FXML private ImageView card1, card2, card3, card4;
    @FXML private TextField tfSolution;
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
    private void onRefresh() {
        dealNewHand();
        tfExpression.requestFocus();
    }

    @FXML
    private void onFindSolution() {
        String solution = Expr24.solve(cardValues());
        tfSolution.setText(solution != null ? solution : "No solution");
    }
    @FXML
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
            show(Alert.AlertType.WARNING, "Wrong numbers",
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
        List<Card> deck = new ArrayList<>();

        for (String suit : SUITS) {
            for (int rank = 1; rank <= 13; rank++) {
                deck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(deck);
        hand.clear();
        hand.addAll(deck.subList(0, 4));

        for (int i = 0; i < 4; i++) {
            cardViews[i].setImage(loadImage(hand.get(i)));
        }

        tfSolution.clear();
        tfExpression.clear();
    }

private int[] cardValues() {
        return hand.stream().mapToInt(Card::rank).toArray();
}
private Image loadImage(Card card) {
        String path = "png/" + card.filename();
        try(InputStream in = HelloController.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Card image not found: " + path + "(is the png folder under src/main/resources/org/example/playingcards/?)");
            return new Image(in);
        } catch (IOException e) {
            throw new IllegalStateException("Card image not found: " + path, e);
        }
}
private void show(Alert.AlertType type, String header, String message) {
    Alert alert = new Alert(type);
    alert.setTitle("24 Card Game");
    alert.setHeaderText(header);
    alert.setContentText(message);
    alert.showAndWait();
}
}
