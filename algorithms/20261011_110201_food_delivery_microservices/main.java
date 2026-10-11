public class Main {
    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        // Initialise core services
        Core.InMemoryOrderService orderService = new Core.InMemoryOrderService();
        Core.InMemoryRestaurantService restaurantService = new Core.InMemoryRestaurantService();
        Core.InMemoryDeliveryService deliveryService = new Core.InMemoryDeliveryService();

        // Populate sample data
        Core.Restaurant r1 = new Core.Restaurant(
                1L,
                "Pasta Palace",
                java.util.Map.of("Spaghetti", 8.5, "Ravioli", 9.0)
        );
        restaurantService.addRestaurant(r1);

        Core.DeliveryAgent a1 = new Core.DeliveryAgent(1L, "Alice");
        Core.DeliveryAgent a2 = new Core.DeliveryAgent(2L, "Bob");
        deliveryService.addAgent(a1);
        deliveryService.addAgent(a2);

        // Dispatcher under test
        Core.Dispatcher dispatcher = new Core.Dispatcher(orderService, restaurantService, deliveryService);

        // ---- Test 1: Successful order flow ----
        Core.Order order1 = dispatcher.processOrder(1L, java.util.List.of("Spaghetti"));
        assertTrue(order1.getStatus() == Core.OrderStatus.DELIVERED,
                "Order should be delivered");
        assertTrue(order1.getDeliveryAgentId() != null,
                "Delivery agent must be assigned");
        // Agent should be released after delivery
        assertTrue(deliveryService.getAgent(order1.getDeliveryAgentId()).isAvailable(),
                "Agent must be available after order completion");

        // ---- Test 2: No agents available ----
        // Occupy both agents manually
        Core.Order dummy1 = orderService.placeOrder(1L, java.util.List.of("Ravioli"));
        Core.Order dummy2 = orderService.placeOrder(1L, java.util.List.of("Ravioli"));
        deliveryService.assignAgent(dummy1);
        deliveryService.assignAgent(dummy2);
        // Now process a new order; it should be cancelled
        Core.Order order2 = dispatcher.processOrder(1L, java.util.List.of("Spaghetti"));
        assertTrue(order2.getStatus() == Core.OrderStatus.CANCELLED,
                "Order must be cancelled when no agents are free");
        // Clean up: release dummy agents for further tests
        deliveryService.releaseAgent(dummy1.getDeliveryAgentId());
        deliveryService.releaseAgent(dummy2.getDeliveryAgentId());

        // ---- Test 3: Invalid restaurant handling ----
        boolean threw = false;
        try {
            dispatcher.processOrder(999L, java.util.List.of("Spaghetti"));
        } catch (java.util.NoSuchElementException e) {
            threw = true;
        }
        assertTrue(threw, "Processing with unknown restaurant should throw");

        // ---- Test 4: Order status transitions ----
        Core.Order order3 = orderService.placeOrder(1L, java.util.List.of("Ravioli"));
        assertTrue(order3.getStatus() == Core.OrderStatus.PLACED, "Initial status PLACED");
        orderService.updateStatus(order3.getId(), Core.OrderStatus.PREPARING);
        assertTrue(order3.getStatus() == Core.OrderStatus.PREPARING, "Status PREPARING");
        orderService.updateStatus(order3.getId(), Core.OrderStatus.READY_FOR_PICKUP);
        assertTrue(order3.getStatus() == Core.OrderStatus.READY_FOR_PICKUP, "Status READY_FOR_PICKUP");

        // ---- Simple benchmark (deterministic) ----
        long start = System.nanoTime();
        int loops = 10_000;
        for (int i = 0; i < loops; i++) {
            dispatcher.processOrder(1L, java.util.List.of("Spaghetti"));
        }
        long durationNs = System.nanoTime() - start;
        double opsPerSec = loops * 1_000_000_000.0 / durationNs;
        System.out.printf("Processed %d orders in %.2f ms (%.0f ops/s)%n",
                loops, durationNs / 1_000_000.0, opsPerSec);

        System.out.println("All tests passed.");
    }
}
