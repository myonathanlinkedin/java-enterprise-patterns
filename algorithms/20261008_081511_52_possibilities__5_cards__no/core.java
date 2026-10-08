import java.util.*;

enum Suit {
    CLUBS, DIAMONDS, HEARTS, SPADES
}

enum Rank {
    ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7),
    EIGHT(8), NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13);

    private final int value;
    Rank(int v) { this.value = v; }
    int getValue() { return value; }

    static Rank fromValue(int v) {
        for (Rank r : values()) {
            if (r.value == v) return r;
        }
        throw new IllegalArgumentException("Invalid rank value: " + v);
    }
}

final class Card implements Comparable<Card> {
    final Suit suit;
    final Rank rank;

    Card(Suit s, Rank r) {
        this.suit = Objects.requireNonNull(s);
        this.rank = Objects.requireNonNull(r);
    }

    @Override
    public int compareTo(Card o) {
        int sc = suit.ordinal() - o.suit.ordinal();
        return (sc != 0) ? sc : rank.getValue() - o.rank.getValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Card)) return false;
        Card c = (Card) obj;
        return suit == c.suit && rank == c.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }

    @Override
    public String toString() {
        return rank.name() + " of " + suit.name();
    }
}

final class EncodeResult {
    final Card hidden;
    final List<Card> shown; // ordered 4 cards

    EncodeResult(Card hidden, List<Card> shown) {
        this.hidden = hidden;
        this.shown = Collections.unmodifiableList(new ArrayList<>(shown));
    }
}

public class FiveCardTrick {
    // permutations mapping distance 1..6 to order of three cards
    private static final int[][] PERMUTATIONS = {
        {0,1,2},
        {0,2,1},
        {1,0,2},
        {1,2,0},
        {2,0,1},
        {2,1,0}
    };

    // encode a hand of 5 distinct cards
    public static EncodeResult encode(List<Card> hand) {
        if (hand == null || hand.size() != 5) {
            throw new IllegalArgumentException("Hand must contain exactly 5 cards");
        }
        // Find a pair with same suit where distance 1..6 exists
        for (int i = 0; i < hand.size(); i++) {
            for (int j = i + 1; j < hand.size(); j++) {
                Card c1 = hand.get(i);
                Card c2 = hand.get(j);
                if (c1.suit != c2.suit) continue;
                int d1 = forwardDistance(c1.rank.getValue(), c2.rank.getValue());
                int d2 = forwardDistance(c2.rank.getValue(), c1.rank.getValue());
                if (d1 >= 1 && d1 <= 6) {
                    return buildResult(c1, c2, d1, hand);
                }
                if (d2 >= 1 && d2 <= 6) {
                    return buildResult(c2, c1, d2, hand);
                }
            }
        }
        throw new IllegalStateException("No suitable pair found – should be impossible");
    }

    private static EncodeResult buildResult(Card key, Card hidden, int distance, List<Card> hand) {
        // key is the first shown card, hidden is the secret card
        List<Card> remaining = new ArrayList<>();
        for (Card c : hand) {
            if (!c.equals(key) && !c.equals(hidden)) {
                remaining.add(c);
            }
        }
        // sort remaining to have a canonical base order
        remaining.sort(Comparator.naturalOrder());
        // apply permutation for distance (1-indexed)
        int[] perm = PERMUTATIONS[distance - 1];
        List<Card> ordered = new ArrayList<>();
        ordered.add(key);
        for (int idx : perm) {
            ordered.add(remaining.get(idx));
        }
        return new EncodeResult(hidden, ordered);
    }

    // decode the hidden card from the ordered 4 shown cards
    public static Card decode(List<Card> shown) {
        if (shown == null || shown.size() != 4) {
            throw new IllegalArgumentException("Shown list must contain exactly 4 cards");
        }
        Card key = shown.get(0);
        List<Card> three = new ArrayList<>(shown.subList(1, 4));
        // sort three to get base order
        List<Card> base = new ArrayList<>(three);
        base.sort(Comparator.naturalOrder());
        // determine which permutation matches the shown order
        int permIndex = -1;
        outer:
        for (int i = 0; i < PERMUTATIONS.length; i++) {
            int[] perm = PERMUTATIONS[i];
            for (int j = 0; j < 3; j++) {
                if (!three.get(j).equals(base.get(perm[j]))) {
                    continue outer;
                }
            }
            permIndex = i; // 0-based
            break;
        }
        if (permIndex == -1) {
            throw new IllegalStateException("Invalid permutation in shown cards");
        }
        int distance = permIndex + 1; // 1..6
        int hiddenRankVal = key.rank.getValue() + distance;
        if (hiddenRankVal > 13) hiddenRankVal -= 13;
        Rank hiddenRank = Rank.fromValue(hiddenRankVal);
        return new Card(key.suit, hiddenRank);
    }

    private static int forwardDistance(int from, int to) {
        int diff = to - from;
        if (diff <= 0) diff += 13;
        return diff;
    }
}
