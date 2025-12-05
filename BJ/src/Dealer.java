import java.util.ArrayList;

public class Dealer {
  Card hiddenCard;
  private ArrayList<Card> dealerCards;
  int dealerSum;
  int dealerAceCount;
  boolean showHidden = false;

  public Dealer() {
    this.dealerCards = new ArrayList<Card>();
    this.dealerSum = 0;
    this.dealerAceCount = 0;
  }

  public void setHiddenCard(Card hidden) {
    hiddenCard = hidden;
    addDealerCards(hidden);
  }

  public void addDealerCards(Card card) {
    dealerCards.add(card);
    dealerSum += card.getValue();

    if (card.isAce()) {
      dealerAceCount++;
    }
  }

  public void showDealerHand(boolean reveal) {
    if (reveal) {
      System.out.println("Dealer's hand: " + dealerCards);
    } else {
      System.out.print("Dealer's hand: [Hidden");
      for (int i = 1; i < dealerCards.size(); i++) {
        System.out.print(", " + dealerCards.get(i));
      }
      System.out.println("]");
    }
  }

  public boolean isShowHidden() {
    return showHidden;
  }

  public void setShowHidden(boolean showHidden) {
    this.showHidden = showHidden;
  }

  public Card getHidden() {
    return hiddenCard;
  }

  public ArrayList<Card> getDealerCards() {
    return dealerCards;
  }

    public int getSum() { // Calculates if Ace is 11 or 1 to sum dealer's score
    int sum = dealerSum;
    int aces = dealerAceCount;

    while (sum > 21 && aces > 0) {
      sum -= 10;
      aces--;
    }
    return sum;
  }

  public int getVisibleSum() {
    if (showHidden) return getSum();
    if (dealerCards.size() > 1) {
        return dealerCards.get(1).getValue(); // only visible card
    }
    return 0;
  }
}
