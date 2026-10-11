import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core domain model and in‑memory services for a simplified food‑delivery
 * micro‑service simulation. All logic is pure, deterministic and uses only
 * Java SE standard library.
 */
public class Core {

    /* ---------- Domain Types ---------- */

    public enum OrderStatus {
        PLACED, PREPARING, READY_FOR_PICKUP, PICKED_UP, DELIVERED, CANCELLED
    }

    public static final class Order {
        private final long id;
        private final long restaurantId;
        private final List<String> items;
        private OrderStatus status;
        private Long deliveryAgentId; // nullable

        public Order(long id, long restaurantId, List<String> items) {
            this.id = id;
            this.restaurantId = restaurantId;
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
            this.status = OrderStatus.PLACED;
        }

        public long getId() { return id; }
        public long getRestaurantId() { return restaurantId; }
        public List<String> getItems() { return items; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public Long getDeliveryAgentId() { return deliveryAgentId; }
        public void setDeliveryAgentId(Long agentId) { this.deliveryAgentId = agentId; }
    }

    public static final class Restaurant {
        private final long id;
        private final String name;
        private final Map<String, Double> menu; // item -> price

        public Restaurant(long id, String name, Map<String, Double> menu) {
            this.id = id;
            this.name = Objects.requireNonNull(name);
            this.menu = Collections.unmodifiableMap(new HashMap<>(menu));
        }

        public long getId() { return id; }
        public String getName() { return name; }
        public Map<String, Double> getMenu() { return menu; }
    }

    public static final class DeliveryAgent {
        private final long id;
        private final String name;
        private boolean available = true;

        public DeliveryAgent(long id, String name) {
            this.id = id;
            this.name = Objects.requireNonNull(name);
        }

        public long getId() { return id; }
        public String getName() { return name; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }

    /* ---------- Service Interfaces ---------- */

    public interface OrderService {
        Order placeOrder(long restaurantId, List<String> items);
        Order getOrder(long orderId);
        void updateStatus(long orderId, OrderStatus newStatus);
    }

    public interface RestaurantService {
        Restaurant getRestaurant(long restaurantId);
        void addRestaurant(Restaurant restaurant);
    }

    public interface DeliveryService {
        Optional<DeliveryAgent> assignAgent(Order order);
        void releaseAgent(long agentId);
        DeliveryAgent getAgent(long agentId);
        void addAgent(DeliveryAgent agent);
    }

    /* ---------- In‑Memory Implementations ---------- */

    public static final class InMemoryOrderService implements OrderService {
        private final Map<Long, Order> orders = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override
        public Order placeOrder(long restaurantId, List<String> items) {
            long id = idGen.getAndIncrement();
            Order order = new Order(id, restaurantId, items);
            orders.put(id, order);
            return order;
        }

        @Override
        public Order getOrder(long orderId) {
            Order o = orders.get(orderId);
            if (o == null) throw new NoSuchElementException("Order not found: " + orderId);
            return o;
        }

        @Override
        public void updateStatus(long orderId, OrderStatus newStatus) {
            Order o = getOrder(orderId);
            o.setStatus(newStatus);
        }
    }

    public static final class InMemoryRestaurantService implements RestaurantService {
        private final Map<Long, Restaurant> restaurants = new HashMap<>();

        @Override
        public Restaurant getRestaurant(long restaurantId) {
            Restaurant r = restaurants.get(restaurantId);
            if (r == null) throw new NoSuchElementException("Restaurant not found: " + restaurantId);
            return r;
        }

        @Override
        public void addRestaurant(Restaurant restaurant) {
            Objects.requireNonNull(restaurant);
            restaurants.put(restaurant.getId(), restaurant);
        }
    }

    public static final class InMemoryDeliveryService implements DeliveryService {
        private final Map<Long, DeliveryAgent> agents = new HashMap<>();

        @Override
        public Optional<DeliveryAgent> assignAgent(Order order) {
            for (DeliveryAgent a : agents.values()) {
                if (a.isAvailable()) {
                    a.setAvailable(false);
                    order.setDeliveryAgentId(a.getId());
                    return Optional.of(a);
                }
            }
            return Optional.empty(); // none available
        }

        @Override
        public void releaseAgent(long agentId) {
            DeliveryAgent a = getAgent(agentId);
            a.setAvailable(true);
        }

        @Override
        public DeliveryAgent getAgent(long agentId) {
            DeliveryAgent a = agents.get(agentId);
            if (a == null) throw new NoSuchElementException("Agent not found: " + agentId);
            return a;
        }

        @Override
        public void addAgent(DeliveryAgent agent) {
            Objects.requireNonNull(agent);
            agents.put(agent.getId(), agent);
        }
    }

    /* ---------- Orchestrator ---------- */

    /**
     * Simple orchestrator that mimics a request flow across services.
     * No concurrency is introduced; all operations are sequential and
     * deterministic.
     */
    public static final class Dispatcher {
        private final OrderService orderService;
        private final RestaurantService restaurantService;
        private final DeliveryService deliveryService;

        public Dispatcher(OrderService orderService,
                          RestaurantService restaurantService,
                          DeliveryService deliveryService) {
            this.orderService = Objects.requireNonNull(orderService);
            this.restaurantService = Objects.requireNonNull(restaurantService);
            this.deliveryService = Objects.requireNonNull(deliveryService);
        }

        /**
         * Full lifecycle: place order → prepare → assign delivery → deliver.
         * Returns the final Order object.
         */
        public Order processOrder(long restaurantId, List<String> items) {
            // 1. place
            Order order = orderService.placeOrder(restaurantId, items);
            orderService.updateStatus(order.getId(), OrderStatus.PREPARING);

            // 2. simulate preparation (instant for this model)
            orderService.updateStatus(order.getId(), OrderStatus.READY_FOR_PICKUP);

            // 3. assign delivery agent
            Optional<DeliveryAgent> optAgent = deliveryService.assignAgent(order);
            if (!optAgent.isPresent()) {
                orderService.updateStatus(order.getId(), OrderStatus.CANCELLED);
                return order;
            }
            orderService.updateStatus(order.getId(), OrderStatus.PICKED_UP);

            // 4. simulate delivery (instant)
            orderService.updateStatus(order.getId(), OrderStatus.DELIVERED);
            // release agent for future orders
            deliveryService.releaseAgent(optAgent.get().getId());

            return order;
        }
    }
}
