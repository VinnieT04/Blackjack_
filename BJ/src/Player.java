import java.util.ArrayList;

public class Player {
  private ArrayList<Card> playerCards;
  int playerSum;
  int playerAceCount;

  private int balance = 1000; //starting money
  private int currentBet = 0; //starting bet

  public Player() {
    this.playerCards = new ArrayList<>();
    this.playerSum = 0;
    this.playerAceCount = 0;
  }

  public void addPlayerCards(Card card) {
    playerCards.add(card);
    playerSum += card.getValue();

    if (card.isAce()) {
      playerAceCount++;
    }
  }

  public void showPlayerHand() {
    System.out.println(" hand: " + playerCards);
  }

    public ArrayList<Card> getPlayerCards() {
    return playerCards;
  }

    public int getSum() {
    int sum = playerSum;
    int aces = playerAceCount;
   
    while (sum > 21 && aces > 0) {
      sum -= 10;
      aces--;
    }
   return sum;
  }

  public int getBalance() {
    return balance;
  }

  public int getBet() {
    return currentBet;
  }

  public void addBet(int amount) {
    currentBet += amount;
    balance -= amount;
  }
  
  // public void doubleBet() {
  //   balance -= currentBet;
  //   currentBet *= 2;
  // }

  public void winBet() {
    balance += currentBet * 2;
    currentBet = 0;
  }

  public void pushBet() {
    balance += currentBet;  // player gets bet back
    currentBet = 0;
  }

  public void resetBet() {
    currentBet = 0;
  }
}
