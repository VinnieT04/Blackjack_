import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import javax.swing.*;
import java.net.URL;

public class BlackJack {
  ArrayList<Card> deck = Card.buildDeck();
  Random random = new Random();

  Player player = new Player();
  Dealer dealer = new Dealer();

  private boolean isBetActive = false;

  private int playerWins = 0;
  private int dealerWins = 0;


  int boardWidth = 1000;
  int boardHeight = 800;

  JFrame frame = new JFrame("BLACKJACK");
  JLabel resultLabel = new JLabel();
  JLabel scoreBoardLabel = new JLabel();

  //ANIMATION
    private ArrayList<AnimatedCard> animatedCard = new ArrayList<>();
    private Timer animationTimer;
    private boolean isAnimating = false;

  // --- chip icons (loaded from your src/chips/ paths)
  // Add a helper method to reliably load icons from the classpath
  private ImageIcon loadChipIcon(String filename) {
      // Assuming your images are in a folder named 'chips' on the classpath root
      URL url = getClass().getResource("/chips/" + filename);
      if (url == null) {
          // Print a helpful error message if an image is missing
          System.err.println("ERROR: Chip image not found at: /chips/" + filename);
      }
      // Return the loaded icon, or a null icon if loading failed
      return (url != null) ? new ImageIcon(url) : new ImageIcon();
  }

    // --- chip icons (loaded using the reliable classpath method)
    private ImageIcon chip10 = loadChipIcon("chip_10.png");
    private ImageIcon chip20 = loadChipIcon("chip_20.png");
    private ImageIcon chip50 = loadChipIcon("chip_50.png");
    private ImageIcon chip100 = loadChipIcon("chip_100.png");
    private ImageIcon chip500 = loadChipIcon("chip_500.png");
    private ImageIcon chipD = loadChipIcon("chip_d.png");

    private ImageIcon blank500 = loadChipIcon("chip_blank_1.png");
    private ImageIcon blank100 = loadChipIcon("chip_blank_2.png");
    private ImageIcon blank50 = loadChipIcon("chip_blank_3.png");
    private ImageIcon blank20 = loadChipIcon("chip_blank_4.png");
    private ImageIcon blank10 = loadChipIcon("chip_blank_5.png");

  // ... rest of your BlackJack class remains the same ...

  // mapping numbered -> blank icon (your requested mapping)
  Map<Integer, ImageIcon> chipMap = Map.of(
    10, blank10,
    20, blank20,
    50, blank50,
    100, blank100,
    500, blank500
  );

  // UI panels for chips
  private JPanel tableChipPanel;        // horizontal blanks above player cards
  private JPanel chipSelectionPanel;    // vertical numbered chips on right
  private JLabel moneyLabel;            // shows current money

  // Betting state (kept here so we don't need to change Player class)
  private int playerMoney = player.getBalance();   // adjust starting money here
  private int currentBet = player.getBet();

  // Buttons we need to disable/enable
  private JButton hitButton;
  private JButton standButton;

  private JButton newGameButton = new JButton("New Game");

  //animated cards class
    class AnimatedCard
  {
      Card card;
      double x, y;
      double targetX, targetY;
      double speed = 15.0;
      boolean isDealer, isHidden;
      int cardIndex;

      AnimatedCard(Card card, double startX, double startY, double targetX, double targetY, boolean isDealer, boolean isHidden, int cardIndex) {
          this.card = card;
          this.x = startX;
          this.y = startY;
          this.targetX = targetX;
          this.targetY = targetY;
          this.isDealer = isDealer;
          this.isHidden = isHidden;
          this.cardIndex = cardIndex;
      }

      boolean update() {
          double dx = targetX - x;
          double dy = targetY - y;
          double distance = Math.sqrt(dx * dx + dy * dy);

          if (distance < speed) {
              x = targetX;
              y = targetY;
              return true; // animation complete
          }

          x += (dx / distance) * speed;
          y += (dy / distance) * speed;
          return false;
      }
  }

  // ---- Background Panel ----
  class BackgroundPanel extends JPanel {
    Image backgroundImage;
    Image hiddenCardImage;

    BackgroundPanel(String imagePath) {
      URL bgUrl = getClass().getResource("/" + imagePath);
      if (bgUrl != null) {
        backgroundImage = new ImageIcon(bgUrl).getImage();
      } else {
        System.err.println("ERROR: Background image not found at: /" + imagePath);
      }

      URL hiddenUrl = getClass().getResource("/card_pngs/card_backs/card_back_2.png");
      if (hiddenUrl != null) {
        hiddenCardImage = new ImageIcon(hiddenUrl).getImage();
      } else {
        System.err.println("ERROR: Hidden card image not found at: /card_pngs/card_backs/card_back_2.png");
      }
    }

    // Helper: Check if a specific card is currently flying in the air
    private boolean isCardAnimating(Card c) {
        for (AnimatedCard ac : animatedCard) {
            // Check if the card object in the animation list is the same instance
            if (ac.card == c) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 1. Draw Background
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        int cardWidth = 70;
        int cardHeight = 100;
        int spacing = 25;

        try {
            // 2. Draw DEALER'S Static Cards (Cards already landed)
            // We removed the "if (!isAnimating)" check here so they stay visible!
            int y = 230;
            ArrayList<Card> dealerCards = dealer.getDealerCards();
            int totalWidth = 0;
            if (dealerCards.size() > 0) {
                totalWidth = dealerCards.size() * cardWidth + (dealerCards.size() - 1) * spacing;
            }
            int startX = (getWidth() - totalWidth) / 2;

            for (int i = 0; i < dealerCards.size(); i++) {
                Card c = dealerCards.get(i);
                
                // CRITICAL FIX: If this specific card is currently animating, 
                // skip drawing the static version so we don't see it twice.
                if (isCardAnimating(c)) continue; 

                int x = startX + (i * (cardWidth + spacing));

                if (i == 0 && !dealer.isShowHidden()) {
                    if (hiddenCardImage != null) {
                        g.drawImage(hiddenCardImage, x, y, cardWidth, cardHeight, this);
                    }
                } else {
                    String value = c.value;
                    String suit = c.suit;
                    String path = "/card_pngs/card_faces/" + value + suit + ".png";
                    URL cardUrl = getClass().getResource(path);
                    if (cardUrl != null) {
                        Image cardImg = new ImageIcon(cardUrl).getImage();
                        g.drawImage(cardImg, x, y, cardWidth, cardHeight, this);
                    }
                }
            }

            // 3. Draw PLAYER'S Static Cards (Cards already landed)
            int playerY = 450;
            ArrayList<Card> playerCards = player.getPlayerCards();
            int totalPlayerWidth = 0;
            if (playerCards.size() > 0) {
                totalPlayerWidth = playerCards.size() * cardWidth + (playerCards.size() - 1) * spacing;
            }
            int playerStartX = (getWidth() - totalPlayerWidth) / 2;

            for (int i = 0; i < playerCards.size(); i++) {
                Card c = playerCards.get(i);

                // CRITICAL FIX: Skip if this card is flying
                if (isCardAnimating(c)) continue;

                int x = playerStartX + (i * (cardWidth + spacing));
                String value = c.value;
                String suit = c.suit;
                String path = "/card_pngs/card_faces/" + value + suit + ".png";
                URL cardUrl = getClass().getResource(path);
                if (cardUrl != null) {
                    Image cardImg = new ImageIcon(cardUrl).getImage();
                    g.drawImage(cardImg, x, playerY, cardWidth, cardHeight, this);
                }
            }

            // 4. Draw Animated Cards (The moving ones) on TOP
            for (AnimatedCard ac : animatedCard) {
                if (ac.isHidden) {
                    if (hiddenCardImage != null) {
                        g.drawImage(hiddenCardImage, (int)ac.x, (int)ac.y, cardWidth, cardHeight, this);
                    }
                } else {
                    String value = ac.card.value;
                    String suit = ac.card.suit;
                    String path = "/card_pngs/card_faces/" + value + suit + ".png";
                    URL cardURL = getClass().getResource(path);
                    if (cardURL != null) {
                        Image cardImg = new ImageIcon(cardURL).getImage();
                        g.drawImage(cardImg, (int)ac.x, (int)ac.y, cardWidth, cardHeight, this);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  }

  BlackJack() {

    // create and show frame and main UI
    frame.setSize(boardWidth, boardHeight);
    frame.setLocationRelativeTo(null);
    frame.setResizable(false);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    BackgroundPanel gamePanel = new BackgroundPanel("table.png");
    gamePanel.setLayout(null); // we use absolute positioning (as you had)
    gamePanel.setPreferredSize(new Dimension(boardWidth, boardHeight));

    // ---- CURSOR ----
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Image gloveImage = toolkit.getImage(getClass().getResource("/glove.png"));
    Cursor gloveCursor = toolkit.createCustomCursor(
            gloveImage,
            new Point(0, 0),
            "glove cursor"
    );
    gamePanel.setCursor(gloveCursor);

    // Create tableChipPanel (blank chips above player's cards)
    tableChipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    tableChipPanel.setOpaque(false);
    // position above player cards: adjust Y to fine-tune
    int tableChipWidth = 500;
    int tableChipHeight = 50;
    int tableChipX = (boardWidth - tableChipWidth) / 2;
    int tableChipY = 400; // a bit above player cards (playerY was 450)
    tableChipPanel.setBounds(tableChipX, tableChipY, tableChipWidth, tableChipHeight);
    gamePanel.add(tableChipPanel);

    // Create chipSelectionPanel (vertical on right)
    chipSelectionPanel = new JPanel();
    chipSelectionPanel.setLayout(new BoxLayout(chipSelectionPanel, BoxLayout.Y_AXIS));
    chipSelectionPanel.setOpaque(false);
    int chipPanelW = 100;
    int chipPanelH = 300;
    int chipPanelX = boardWidth - chipPanelW - 20;
    int chipPanelY = 250;
    chipSelectionPanel.setBounds(chipPanelX, chipPanelY, chipPanelW, chipPanelH);
    gamePanel.add(chipSelectionPanel);

    // Money label
    moneyLabel = new JLabel("Money: $" + playerMoney, SwingConstants.LEFT);
    moneyLabel.setFont(new Font("Arial", Font.BOLD, 18));
    moneyLabel.setForeground(Color.WHITE);
    moneyLabel.setBounds(20, 20, 400, 30);
    gamePanel.add(moneyLabel);

    // Result & scoreboard
    resultLabel = new JLabel("", SwingConstants.CENTER);
    int labelWidth = 400;
    int labelHeight = 60;
    int centerX = (boardWidth - labelWidth) / 2;
    int centerY = (boardHeight - labelHeight) / 2;
    resultLabel.setBounds(centerX, centerY, labelWidth, labelHeight);
    resultLabel.setFont(new Font("Arial", Font.BOLD, 36));
    resultLabel.setForeground(Color.YELLOW);
    resultLabel.setOpaque(false);
    gamePanel.add(resultLabel);

    scoreBoardLabel = new JLabel("Player: 0  |  Dealer: 0", SwingConstants.RIGHT);
    scoreBoardLabel.setBounds(0, 10, 800, 50);
    scoreBoardLabel.setFont(new Font("Arial", Font.BOLD, 20));
    scoreBoardLabel.setForeground(Color.WHITE);
    gamePanel.add(scoreBoardLabel);

    // Buttons
    hitButton = new JButton("Hit");
    hitButton.setBounds(350, 580, 105, 90);
    standButton = new JButton("Stay");
    standButton.setBounds(518, 580, 105, 90);

    hitButton.setFocusable(false);
    standButton.setFocusable(false);

    hitButton.setOpaque(false);
    hitButton.setContentAreaFilled(false);
    hitButton.setBorderPainted(true);

    standButton.setOpaque(false);
    standButton.setContentAreaFilled(false);
    standButton.setBorderPainted(true);

    newGameButton.setBounds(750, 50, 120, 50);
    newGameButton.setFocusable(false);
    newGameButton.setOpaque(false);
    newGameButton.setContentAreaFilled(false);
    newGameButton.setBorderPainted(true);

    gamePanel.add(hitButton);
    gamePanel.add(standButton);
    gamePanel.add(newGameButton);

    // Create the numbered chip selection buttons (right side) using images
    addChipSelectionImage(500, chip500);
    addChipSelectionImage(100, chip100);
    addChipSelectionImage(50, chip50);
    addChipSelectionImage(20, chip20);
    addChipSelectionImage(10, chip10);

    // D (double) button as image
    JLabel dLabel = new JLabel(chipD);
    dLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    dLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    dLabel.setToolTipText("Double Bet (D)");
    dLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        handleDoubleBet();
      }
    });
    chipSelectionPanel.add(Box.createVerticalStrut(8));
    chipSelectionPanel.add(dLabel);

    //ANIMATION TIMER
      animationTimer = new Timer(16, new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              boolean allComplete = true;
              for (AnimatedCard ac : animatedCard) {
                  if (!ac.update()) {
                      allComplete = false;
                  }
              }

              gamePanel.repaint();

              if (allComplete) {
                  animationTimer.stop();
                  animatedCard.clear();
                  isAnimating = false;
                  gamePanel.repaint(); // Final repaint to show static cards
              }
          }
      });

    // Action listeners for Hit/Stay
    hitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          if(isAnimating)
          {
              return;
          }
          // player draws a card
          if (!deck.isEmpty()) {
            Card newCard = deck.remove(0);
            player.addPlayerCards(newCard);

            //animate new card
            animateSingleCard(newCard, false, player.getPlayerCards().size() - 1);
            System.out.println("Player hits: " + newCard.value + " of " + newCard.suit);
          }

          Timer checkTimer = new Timer(500, new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                  ((Timer)evt.getSource()).stop();
                  int total = player.getSum();
                  updateScores();
                  if (total > 21) {
                      resultLabel.setForeground(Color.RED);
                      resultLabel.setText("You busted with " + total + "!");
                      dealer.setShowHidden(true);
                      hitButton.setEnabled(false);
                      standButton.setEnabled(false);

                      dealerWins++;
                      isBetActive = false;
                      clearTableChips();
                      currentBet = 0;
                  }
                  updateScores();
                  gamePanel.repaint();
              }
          });
          checkTimer.setRepeats(false);
          checkTimer.start();
      }
    });


      standButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          if(isAnimating)
          {
              return;
          }
          dealer.setShowHidden(true);
          dealerPlay();
          updateScores();
          hitButton.setEnabled(false);
          standButton.setEnabled(false);

          isBetActive = false;

          gamePanel.repaint();
          // After round ends, clear chips and reset bet
          clearTableChips();
          currentBet = 0;
          updateMoneyLabel();
      }
    });

    newGameButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isAnimating) return;

        updateScores();
        resetRound();
        beforePlay();  // open betting dialog again
        frame.repaint();
    }
    });

    gamePanel.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        // LEFT CLICK → HIT
        if (SwingUtilities.isLeftMouseButton(e)) {
          if (hitButton.isEnabled()) {
            hitButton.doClick();
          }
        }

          // RIGHT CLICK → STAY
        if (SwingUtilities.isRightMouseButton(e)) {
          if (standButton.isEnabled()) {
            standButton.doClick();
          }
        }
      }
    });


    frame.setContentPane(gamePanel);
    frame.revalidate();
    frame.setVisible(true);

    beforePlay(); // this will call startGame() after confirm

    frame.repaint();
    updateScores();
  }

  private void resetRound() {
    // Clear animations
    animatedCard.clear();
    isAnimating = false;
    if (animationTimer != null) animationTimer.stop();

    // Clear cards
    player.getPlayerCards().clear();
    dealer.getDealerCards().clear();

    // Restes hands
    player.resetHand();
    dealer.resetHand();

    // Clear table chips
    clearTableChips();

    // Reset bet
    currentBet = 0;
    isBetActive = false;

    // Reset labels
    resultLabel.setText("");
    resultLabel.setForeground(Color.YELLOW);

    // Re-enable buttons
    hitButton.setEnabled(true);
    standButton.setEnabled(true);

    updateMoneyLabel();

  }


  private void animateSingleCard(Card card, boolean isDealer, int cardIndex)
  {
      int cardWidth = 70;
      int spacing = 25;

      double startX = 50;
      double startY = 100;

      double targetX, targetY;

      if(isDealer)
      {
          int totalCards = dealer.getDealerCards().size();
          int totalWidth = totalCards * cardWidth + (totalCards - 1) * spacing;
          int startXPos = (boardWidth - totalWidth) / 2;
          targetX = startXPos + (cardIndex * (cardWidth + spacing));
          targetY = 230;
      }
      else
      {
          int totalCards = player.getPlayerCards().size();
          int totalWidth = totalCards * cardWidth + (totalCards - 1) * spacing;
          int startXPos = (boardWidth - totalWidth) / 2;
          targetX = startXPos + (cardIndex * (cardWidth + spacing));
          targetY = 450;
      }

      animatedCard.clear();
      animatedCard.add(new AnimatedCard(card, startX, startY, targetX, targetY, isDealer, false, cardIndex));
      isAnimating = true;
      animationTimer.start();
  }

  // Adds a numbered chip image to the chipSelectionPanel (right vertical area)
  private void addChipSelectionImage(int value, ImageIcon icon) {
    JLabel label = new JLabel(icon);
    label.setAlignmentX(Component.CENTER_ALIGNMENT);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.setToolTipText("Bet $" + value);
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        handleChipSelection(value);
      }
    });
    chipSelectionPanel.add(Box.createVerticalStrut(8));
    chipSelectionPanel.add(label);
  }

  // Handle selecting a numbered chip: check money, deduct, show blank chip on table
  private void handleChipSelection(int value) {
      if(!isBetActive)
      {
          return;
      }
    if (playerMoney < value) {
      // not enough funds
      JOptionPane.showMessageDialog(frame, "Not enough money to bet $" + value);
      return;
    }

    // Deduct money and add to currentBet
    playerMoney -= value;
    currentBet += value;

    // create blank chip corresponding to this value and add to tableChipPanel
    ImageIcon blank = chipMap.get(value);
    if (blank != null) {
      JLabel blankLabel = new JLabel(blank);
      // each blank chip added horizontally
      tableChipPanel.add(blankLabel);
      tableChipPanel.revalidate();
      tableChipPanel.repaint();
    }

    updateMoneyLabel();
  }

  // Double bet logic (D)
  private void handleDoubleBet() {
    if (currentBet <= 0) {
      JOptionPane.showMessageDialog(frame, "Place an initial bet before doubling.");
      return;
    }
    if (playerMoney < currentBet) {
      JOptionPane.showMessageDialog(frame, "Not enough money to double the bet.");
      return;
    }

    // Deduct equal amount and double the bet
    playerMoney -= currentBet;
    currentBet *= 2;

    // visually add same number of blank chips that correspond to the previous selection
    // We'll simply add one blank chip using the last bet denomination detection heuristics:
    // For a better visual, you can track the last chosen value; here we pick blank for the smallest chip (10) if unknown.
    // Simpler: add a generic blank (choose blank10 if available).
    ImageIcon blank = blank10; // you could improve by storing last chosen denomination
    JLabel blankLabel = new JLabel(blank);
    tableChipPanel.add(blankLabel);
    tableChipPanel.revalidate();
    tableChipPanel.repaint();

    updateMoneyLabel();
  }

  // Clear chips from table at end of round
  private void clearTableChips() {
    tableChipPanel.removeAll();
    tableChipPanel.revalidate();
    tableChipPanel.repaint();
  }

  // Update money label
  private void updateMoneyLabel() {
    moneyLabel.setText("Money: $" + playerMoney + "   Bet: $" + currentBet);
  }

  public void beforePlay() {
    // Show bet dialog before starting the round (player sets bet here)
    JDialog betDialog = new JDialog(frame, "Place your bet", true);
    betDialog.setSize(465, 380);
    betDialog.setLayout(null);
    betDialog.setLocationRelativeTo(frame);

    JLabel balLabel = new JLabel("Balance: $" + playerMoney, SwingConstants.CENTER);
    balLabel.setBounds(60, 20, 300, 30);
    balLabel.setFont(new Font("Arial", Font.BOLD, 18));
    betDialog.add(balLabel);

    JLabel betLabel = new JLabel("Your Bet: $0", SwingConstants.CENTER);
    betLabel.setBounds(60, 60, 300, 30);
    betLabel.setFont(new Font("Arial", Font.BOLD, 18));
    betDialog.add(betLabel);

    // ---- Chip Buttons (numbered) ----
    int[] chips = {500, 100, 50, 20, 10};
    int x = 30;
    for (int amount : chips) {
      // use an image if available - otherwise fallback to text button
      JButton chipBtn;
      ImageIcon icon = null;
      switch (amount) {
        case 10: icon = chip10; break;
        case 20: icon = chip20; break;
        case 50: icon = chip50; break;
        case 100: icon = chip100; break;
        case 500: icon = chip500; break;
      }
      if (icon != null) {
        chipBtn = new JButton(icon);
      } else {
        chipBtn = new JButton("$" + amount);
      }
      chipBtn.setBounds(x, 120, 70, 50);
      final int amt = amount;
      chipBtn.addActionListener(e -> {
        if (playerMoney >= amt) {
          // deduct here for the pre-game betting dialog
          playerMoney -= amt;
          currentBet += amt;
          betLabel.setText("Your Bet: $" + currentBet);
          balLabel.setText("Balance: $" + playerMoney);
        } else {
          JOptionPane.showMessageDialog(betDialog, "Not enough money for $" + amt);
        }
      });
      betDialog.add(chipBtn);
      x += 80;
    }

    // ---- D (Double Bet) button ----
    JButton doubleBet = new JButton("D");
    doubleBet.setBounds(160, 190, 100, 50);
    doubleBet.addActionListener(e -> {
      if (currentBet <= 0) {
        JOptionPane.showMessageDialog(betDialog, "Place an initial bet before doubling.");
        return;
      }
      if (playerMoney < currentBet) {
        JOptionPane.showMessageDialog(betDialog, "Not enough balance to double.");
        return;
      }
      playerMoney -= currentBet;
      currentBet *= 2;
      betLabel.setText("Your Bet: $" + currentBet);
      balLabel.setText("Balance: $" + playerMoney);
    });
    betDialog.add(doubleBet);

    // ---- Confirm Button ----
    JButton confirm = new JButton("Confirm");
    confirm.setBounds(150, 260, 120, 40);
    confirm.addActionListener(e -> {
      if (currentBet > 0) {
        // Add blank chips to main table for visual (corresponding to selections)
        // We'll add blanks grouped by denominations: (simple approach) create one blank per chip unit selected
        // For nice visual mapping, you'd track exactly which denominations were selected. For now, we add a number
        // of blank chips equal to the count of chips selected, but we don't track denominations in dialog.
        betDialog.dispose();
        startGame();
      } else {
        JOptionPane.showMessageDialog(betDialog, "You must place a bet first.");
      }
    });
    betDialog.add(confirm);

    betDialog.setVisible(true);

    // After the dialog we want the table UI to already reflect the bet,
    // so add blank chips corresponding to the currentBet. We'll add them as a set of blank10s by default.
    // (Better: keep a record of chosen denominations during the dialog; simplified here for clarity.)
  }

  // ---- Game logic ANIMATED----
  public void startGame()
  {
      shuffleDeck();
      isBetActive = true;
      // reset hands
      player.getPlayerCards().clear();
      dealer.getDealerCards().clear();

      int cardWidth = 70;
      int spacing = 25;
      double deckX = 50;
      double deckY = 100;

      animatedCard.clear();

      Card p1 = deck.remove(0);
      Card d1 = deck.remove(0);
      Card p2 = deck.remove(0);
      Card d2 = deck.remove(0);

      // Calculate target positions
      int p1TargetX = (boardWidth - (2 * cardWidth + spacing)) / 2;
      int d1TargetX = (boardWidth - (2 * cardWidth + spacing)) / 2;
      int p2TargetX = p1TargetX + cardWidth + spacing;
      int d2TargetX = d1TargetX + cardWidth + spacing;

      // Animate cards in sequence with delays
      Timer sequenceTimer = new Timer(400, null); // 400ms between each card
      final int[] cardIndex = {0}; // trick to use mutable int in lambda

      sequenceTimer.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              switch (cardIndex[0]) {
                  case 0: // Player's first card
                      player.addPlayerCards(p1);
                      animatedCard.clear();
                      animatedCard.add(new AnimatedCard(p1, deckX, deckY, p1TargetX, 450, false, false, 0));
                      isAnimating = true;
                      if (!animationTimer.isRunning()) {
                          animationTimer.start();
                      }
                      updateScores();
                      break;

                  case 1: // Dealer's hidden card
                      dealer.setHiddenCard(d1);
                      animatedCard.clear();
                      animatedCard.add(new AnimatedCard(d1, deckX, deckY, d1TargetX, 230, true, true, 0));
                      isAnimating = true;
                      if (!animationTimer.isRunning()) {
                          animationTimer.start();
                      }
                      updateScores();
                      break;

                  case 2: // Player's second card
                      player.addPlayerCards(p2);
                      animatedCard.clear();
                      animatedCard.add(new AnimatedCard(p2, deckX, deckY, p2TargetX, 450, false, false, 1));
                      isAnimating = true;
                      if (!animationTimer.isRunning()) {
                          animationTimer.start();
                      }
                      updateScores();
                      break;

                  case 3: // Dealer's visible card
                      dealer.addDealerCards(d2);
                      animatedCard.clear();
                      animatedCard.add(new AnimatedCard(d2, deckX, deckY, d2TargetX, 230, true, false, 1));
                      isAnimating = true;
                      if (!animationTimer.isRunning()) {
                          animationTimer.start();
                      }
                      updateScores();
                      sequenceTimer.stop();

                      break;
              }
              cardIndex[0]++;
          }
      });

      sequenceTimer.start();
      updateScores();
      updateMoneyLabel();

    // After dealing, we should show the blank chips corresponding to the bet on the table.
    // For better fidelity, track exact chips chosen earlier. Simple fallback: represent the bet
    // as a number of blank10 chips across the table (user experience: you can extend this tracking).
    tableChipPanel.removeAll();
    int remaining = currentBet;
    int[] denoms = {500, 100, 50, 20, 10};
    for (int d : denoms) {
      while (remaining >= d) {
        ImageIcon blank = chipMap.get(d);
        if (blank != null) {
          tableChipPanel.add(new JLabel(blank));
        }
        remaining -= d;
      }
    }
    tableChipPanel.revalidate();
    tableChipPanel.repaint();

    // enable buttons for the round
    hitButton.setEnabled(true);
    standButton.setEnabled(true);
    resultLabel.setText("");
  }

  public void shuffleDeck() {
    for (int i = 0; i < deck.size(); i++) {
      int j = random.nextInt(deck.size());
      Card currentCard = deck.get(i);
      Card randomCard = deck.get(j);
      deck.set(i, randomCard);
      deck.set(j, currentCard);
    }
    System.out.println("SHUFFLED");
  }

  public void dealerPlay() {
    dealer.showDealerHand(true);

    while (dealer.getSum() < 17 && !deck.isEmpty()) {
      Card c = deck.remove(0);
      dealer.addDealerCards(c);
      System.out.println("Dealer draws: " + c.value + " of " + c.suit);
    }

    int dealerValue = dealer.getSum();
    int playerValue = player.getSum();

    String result;
    if (dealerValue > 21) {
      result = "Dealer busts! You win!";
      // pay out: player gets 2x bet (bet already subtracted)
      playerMoney += currentBet * 2;
      playerWins++;
      updateScores();
    } else if (dealerValue > playerValue) {
      result = "Dealer wins!";
      updateScores();
      dealerWins++;
      // player loses bet (already subtracted)
    } else if (dealerValue < playerValue) {
      result = "You win!";
      playerMoney += currentBet * 2;
      playerWins++;
      updateScores();
    } else {
      result = "Tie!";
      // push: return bet
      playerMoney += currentBet;
      updateScores();
    }

    resultLabel.setText(result);
    updateMoneyLabel();
  }

  private void updateScores() {
    // Current round totals (safe if hands are empty)
    int playerScore = 0;
    if (player.getPlayerCards() != null && !player.getPlayerCards().isEmpty()) {
        playerScore = player.getSum();
    }

    int dealerScore = 0;
    if (dealer.getDealerCards() != null && !dealer.getDealerCards().isEmpty()) {
        dealerScore = dealer.isShowHidden() ? dealer.getSum() : dealer.getVisibleSum();
    }

    // Display both persistent wins and current round totals.
    // Format: "Wins P: X  D: Y  |  Player: N  |  Dealer: M"
    String scoreboardText = String.format(
        "Wins P: %d  D: %d    |    Player: %d  |  Dealer: %d",
        playerWins, dealerWins, playerScore, dealerScore
    );
    scoreBoardLabel.setText(scoreboardText);
    scoreBoardLabel.setBounds(300, 10, 800, 40);
    scoreBoardLabel.setHorizontalAlignment(SwingConstants.CENTER);

  }
}
