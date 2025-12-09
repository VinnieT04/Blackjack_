import java.util.ArrayList;
import java.util.Random;

public class Deck {
  private ArrayList<Card> deck;
  private Random random;

  public Deck() {
    this.deck = new ArrayList<>();
    this.random = new Random();
  }

  public void buildDeck(){
    String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    String[] suits = {"H", "C", "S", "D"};

    for (int i = 0; i < suits.length; i++) {
      for (int j = 0; j < values.length; j++) {
        Card card = new Card(values[j], suits[i]);
        deck.add(card);
      }
    }
    System.out.println(deck);
  }

  public ArrayList<Card> getDeck() {
    return deck;
  }

  public void shuffleDeck() {
    deck.clear();
    for (int i = 0; i < deck.size(); i++) {
      int j = random.nextInt(deck.size());
      Card currentCard = deck.get(i);
      Card randomCard = deck.get(j);

      deck.set(i, randomCard);
      deck.set(j, currentCard);
    }
    System.out.println("SHUFFLED");
    System.out.println(deck);
  }
}
