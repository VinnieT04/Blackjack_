import java.util.ArrayList;

public class Card {
  String value;
  String suit;

  public Card(String value, String suit) {
    this.value = value;
    this.suit = suit;
  }

  @Override
  public String toString(){
    return value + suit;
  }

  public static ArrayList<Card> buildDeck(){
    ArrayList<Card> deck = new ArrayList<>();
    String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    String[] suits = {"H", "C", "S", "D"};

    for (int i = 0; i < suits.length; i++) {
      for (int j = 0; j < values.length; j++) {
        Card card = new Card(values[j], suits[i]);
        deck.add(card);
      }
    }
    return deck;
  }

  public int getValue() {
    switch (value) {
      case "J": case "Q": case "K":
        return 10;
      case "A": return 11;
      default:
        return Integer.parseInt(value);
    }
  }

  public boolean isAce() {
    return value.equals("A");
  }
}
