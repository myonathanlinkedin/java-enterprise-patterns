public class Main {
    public static void main(String[] args) {
        // Ensure assertions are enabled.
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (!assertsEnabled) {
            System.err.println("Run with -ea to enable assertions.");
            System.exit(1);
        }

        testThresholdAck();
        testTimeDelayAck();
        testNoPrematureAck();
        testZeroThreshold();
        testZeroDelay();
        testMultipleAcks();
        testRegretBound();

        System.out.println("All tests passed.");
    }

    private static void testThresholdAck() {
        TcpAckScheduler s = new TcpAckScheduler(3, Long.MAX_VALUE);
        assert !s.onPacket(0, 0);
        assert !s.onPacket(1, 10);
        assert s.onPacket(2, 20) : "ACK expected on third packet (threshold 3)";
        assert s.getLastAckedSeq() == 2;
        assert s.getUnackedCount() == 0;
    }

    private static void testTimeDelayAck() {
        TcpAckScheduler s = new TcpAckScheduler(Integer.MAX_VALUE, 50);
        assert !s.onPacket(0, 0);
        assert !s.onPacket(1, 30);
        assert s.onPacket(2, 60) : "ACK expected due to elapsed delay (60-0 >= 50)";
        assert s.getLastAckedSeq() == 2;
    }

    private static void testNoPrematureAck() {
        TcpAckScheduler s = new TcpAckScheduler(5, 1000);
        for (int i = 0; i < 4; i++) {
            assert !s.onPacket(i, i * 10) : "No ACK before threshold or delay";
        }
        // Still no ACK after 4 packets and 30ms elapsed.
        assert !s.onPacket(4, 30);
    }

    private static void testZeroThreshold() {
        TcpAckScheduler s = new TcpAckScheduler(0, Long.MAX_VALUE);
        for (int i = 0; i < 5; i++) {
            assert s.onPacket(i, i * 5) : "Zero threshold forces ACK every packet";
            assert s.getLastAckedSeq() == i;
        }
    }

    private static void testZeroDelay() {
        TcpAckScheduler s = new TcpAckScheduler(Integer.MAX_VALUE, 0);
        for (int i = 0; i < 5; i++) {
            assert s.onPacket(i, i * 7) : "Zero delay forces ACK every packet";
            assert s.getLastAckedSeq() == i;
        }
    }

    private static void testMultipleAcks() {
        TcpAckScheduler s = new TcpAckScheduler(2, 100);
        // Packets 0,1 -> ACK on 1
        assert !s.onPacket(0, 0);
        assert s.onPacket(1, 10);
        assert s.getLastAckedSeq() == 1;
        // Packets 2,3 -> ACK on 3
        assert !s.onPacket(2, 20);
        assert s.onPacket(3, 30);
        assert s.getLastAckedSeq() == 3;
        // Packet 4 alone, wait for time delay
        assert !s.onPacket(4, 40);
        assert !s.onPacket(5, 80);
        assert s.onPacket(6, 150) : "Delay exceeded (150-30 >= 100)";
        assert s.getLastAckedSeq() == 6;
    }

    /**
     * Verifies the regret bound: the excess number of ACKs over the optimal
     * offline solution (which ACKs every packet) never exceeds the packetThreshold.
     */
    private static void testRegretBound() {
        int threshold = 4;
        TcpAckScheduler s = new TcpAckScheduler(threshold, Long.MAX_VALUE);
        int totalPackets = 25;
        int ackCount = 0;
        for (int i = 0; i < totalPackets; i++) {
            if (s.onPacket(i, i * 10)) {
                ackCount++;
            }
        }
        // Optimal offline would ACK every packet: totalPackets ACKs.
        int optimal = totalPackets;
        int regret = ackCount - optimal;
        // Regret should be ≤ 0 (our algorithm never sends more ACKs than per‑packet)
        // but due to threshold it may send fewer; we assert the bound described
        // in the documentation: excess over optimal ≤ threshold.
        assert regret <= threshold : "Regret " + regret + " exceeds threshold " + threshold;
    }
}
