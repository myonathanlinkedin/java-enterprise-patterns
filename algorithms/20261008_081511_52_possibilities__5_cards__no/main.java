import java.util.*;

public class Main {
    private static final Random RAND = new Random();

    public static void main(String[] args) {
        testAllRandomHands(1000);
        testEdgeCases();
        System.out.println("All tests passed.");
    }

    private static void testAllRandomHands(int trials) {
        for (int i = 0; i < trials; i++) {
            List<Card> hand = randomHand();
            FiveCardTrick.EncodeResult enc = FiveCardTrick.encode(hand);
            Card decoded = FiveCardTrick.decode(enc.shown);
            assert decoded.equals(enc.hidden) : "Decoded card does not match hidden card. Hand: " + hand;
        }
    }

    private static void testEdgeCases() {
        // All cards of same suit, ranks 1..5
        List<Card> hand = Arrays.asList(
            new Card(Suit.HEARTS, Rank.ACE),
            new Card(Suit.HEARTS, Rank.TWO),
            new Card(Suit.HEARTS, Rank.THREE),
            new Card(Suit.HEARTS, Rank.FOUR),
            new Card(Suit.HEARTS, Rank.FIVE)
        );
        FiveCardTrick.EncodeResult enc = FiveCardTrick.encode(hand);
        Card decoded = FiveCardTrick.decode(enc.shown);
        assert decoded.equals(enc.hidden);
    }

    private static List<Card> randomHand() {
        Set<Card> set = new HashSet<>();
        while (set.size() < 5) {
            Suit s = Suit.values()[RAND.nextInt(Suit.values().length)];
            Rank r = Rank.values()[RAND.nextInt(Rank.values().length)];
            set.add(new Card(s, r));
        }
        return new ArrayList<>(set);
    }
}
