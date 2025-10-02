package com.vaadin.starter.bakery.app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.backend.data.Role;
import com.vaadin.starter.bakery.backend.data.entity.Customer;
import com.vaadin.starter.bakery.backend.data.entity.HistoryItem;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.data.entity.OrderItem;
import com.vaadin.starter.bakery.backend.data.entity.PickupLocation;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.repositories.OrderRepository;
import com.vaadin.starter.bakery.backend.repositories.PickupLocationRepository;
import com.vaadin.starter.bakery.backend.repositories.ProductRepository;
import com.vaadin.starter.bakery.backend.repositories.UserRepository;

/**
 * A Spring component responsible for generating demo data for the bakery application.
 * This class creates sample users, products, orders, and pickup locations to populate
 * the database with realistic test data for development and demonstration purposes.
 * 
 * The data generation process creates:
 * <ul>
 * <li>Multiple users with different roles (admin, baker, barista)</li>
 * <li>A variety of bakery products with random names and prices</li>
 * <li>Pickup locations for orders</li>
 * <li>Historical orders spanning over two years with realistic progression</li>
 * <li>Order history tracking with realistic state transitions</li>
 * </ul>
 * 
 * The data generation only occurs if the database is empty, ensuring existing data
 * is not overwritten during application startup.
 * 
 * @author Vaadin Bakery Team
 * @version 1.0
 * @since 1.0
 */
@SpringComponent
public class DataGenerator implements HasLogger {

	/** Available filling types for product names */
	private static final String[] FILLING = new String[] { "Strawberry", "Chocolate", "Blueberry", "Raspberry",
			"Vanilla" };
	
	/** Available product types for product names */
	private static final String[] TYPE = new String[] { "Cake", "Pastry", "Tart", "Muffin", "Biscuit", "Bread", "Bagel",
			"Bun", "Brownie", "Cookie", "Cracker", "Cheese Cake" };
	
	/** Pool of first names for generating customer names */
	private static final String[] FIRST_NAME = new String[] { "Ori", "Amanda", "Octavia", "Laurel", "Lael", "Delilah",
			"Jason", "Skyler", "Arsenio", "Haley", "Lionel", "Sylvia", "Jessica", "Lester", "Ferdinand", "Elaine",
			"Griffin", "Kerry", "Dominique" };
	
	/** Pool of last names for generating customer names */
	private static final String[] LAST_NAME = new String[] { "Carter", "Castro", "Rich", "Irwin", "Moore", "Hendricks",
			"Huber", "Patton", "Wilkinson", "Thornton", "Nunez", "Macias", "Gallegos", "Blevins", "Mejia", "Pickett",
			"Whitney", "Farmer", "Henry", "Chen", "Macias", "Rowland", "Pierce", "Cortez", "Noble", "Howard", "Nixon",
			"Mcbride", "Leblanc", "Russell", "Carver", "Benton", "Maldonado", "Lyons" };

	/** Random number generator with fixed seed for reproducible data generation */
	private final Random random = new Random(1L);

	/** Repository for managing order entities */
	private OrderRepository orderRepository;
	
	/** Repository for managing user entities */
	private UserRepository userRepository;
	
	/** Repository for managing product entities */
	private ProductRepository productRepository;
	
	/** Repository for managing pickup location entities */
	private PickupLocationRepository pickupLocationRepository;
	
	/** Password encoder for hashing user passwords */
	private PasswordEncoder passwordEncoder;

	/**
	 * Constructs a new DataGenerator instance with the required dependencies.
	 * 
	 * @param orderRepository the repository for managing order entities
	 * @param userRepository the repository for managing user entities
	 * @param productRepository the repository for managing product entities
	 * @param pickupLocationRepository the repository for managing pickup location entities
	 * @param passwordEncoder the password encoder for hashing user passwords
	 */
	@Autowired
	public DataGenerator(OrderRepository orderRepository, UserRepository userRepository,
			ProductRepository productRepository, PickupLocationRepository pickupLocationRepository,
			PasswordEncoder passwordEncoder) {
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.pickupLocationRepository = pickupLocationRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Initializes the database with demo data if it's currently empty.
	 * This method is automatically called after bean construction via the @PostConstruct annotation.
	 * 
	 * The generated data includes:
	 * <ul>
	 * <li>Users with different roles (admin, baker, barista) including deletable test users</li>
	 * <li>Products for orders and standalone deletable products</li>
	 * <li>Pickup locations (Store and Bakery)</li>
	 * <li>Historical orders spanning two years with realistic state progression</li>
	 * </ul>
	 * 
	 * The method will skip data generation if users already exist in the database,
	 * preventing overwriting of existing data.
	 */
	@PostConstruct
	public void loadData() {
		if (userRepository.count() != 0L) {
			getLogger().info("Using existing database");
			return;
		}

		getLogger().info("Generating demo data");

		getLogger().info("... generating users");
		User baker = createBaker(userRepository, passwordEncoder);
		User barista = createBarista(userRepository, passwordEncoder);
		createAdmin(userRepository, passwordEncoder);
		// A set of products without constrains that can be deleted
		createDeletableUsers(userRepository, passwordEncoder);

		getLogger().info("... generating products");
		// A set of products that will be used for creating orders.
		Supplier<Product> productSupplier = createProducts(productRepository, 8);
		// A set of products without relationships that can be deleted
		createProducts(productRepository, 4);

		getLogger().info("... generating pickup locations");
		Supplier<PickupLocation> pickupLocationSupplier = createPickupLocations(pickupLocationRepository);

		getLogger().info("... generating orders");
		createOrders(orderRepository, productSupplier, pickupLocationSupplier, barista, baker);

		getLogger().info("Generated demo data");
	}

	/**
	 * Populates a customer entity with realistic random data.
	 * 
	 * @param customer the customer entity to populate
	 */
	private void fillCustomer(Customer customer) {
		String first = getRandom(FIRST_NAME);
		String last = getRandom(LAST_NAME);
		customer.setFullName(first + " " + last);
		customer.setPhoneNumber(getRandomPhone());
		if (random.nextInt(10) == 0) {
			customer.setDetails("Very important customer");
		}
	}

	/**
	 * Generates a random phone number in the format +1-555-XXXX.
	 * 
	 * @return a randomly generated phone number string
	 */
	private String getRandomPhone() {
		return "+1-555-" + String.format("%04d", random.nextInt(10000));
	}

	/**
	 * Creates historical orders spanning multiple years with realistic trends and distributions.
	 * Generates orders from two years ago to one month in the future with an upward trend.
	 * 
	 * @param orderRepo the order repository for persistence
	 * @param productSupplier supplier providing products for order items
	 * @param pickupLocationSupplier supplier providing pickup locations for orders
	 * @param barista the barista user responsible for orders
	 * @param baker the baker user responsible for order fulfillment
	 */
	private void createOrders(OrderRepository orderRepo, Supplier<Product> productSupplier,
			Supplier<PickupLocation> pickupLocationSupplier, User barista, User baker) {
		int yearsToInclude = 2;
		LocalDate now = LocalDate.now();
		LocalDate oldestDate = LocalDate.of(now.getYear() - yearsToInclude, 1, 1);
		LocalDate newestDate = now.plusMonths(1L);

		// Create first today's order
		Order order = createOrder(productSupplier, pickupLocationSupplier, barista, baker, now);
		order.setDueTime(LocalTime.of(8, 0));
		order.setHistory(order.getHistory().subList(0, 1));
		order.setItems(order.getItems().subList(0, 1));
		orderRepo.save(order);

		for (LocalDate dueDate = oldestDate; dueDate.isBefore(newestDate); dueDate = dueDate.plusDays(1)) {
			// Create a slightly upwards trend - everybody wants to be
			// successful
			int relativeYear = dueDate.getYear() - now.getYear() + yearsToInclude;
			int relativeMonth = relativeYear * 12 + dueDate.getMonthValue();
			double multiplier = 1.0 + 0.03 * relativeMonth;
			int ordersThisDay = (int) (random.nextInt(10) + 1 * multiplier);
			for (int i = 0; i < ordersThisDay; i++) {
				orderRepo.save(createOrder(productSupplier, pickupLocationSupplier, barista, baker, dueDate));
			}
		}
	}

	/**
	 * Creates a single order with realistic random data and history.
	 * Each order contains 1-4 items, random customer info, pickup location,
	 * and appropriate state based on the due date.
	 * 
	 * @param productSupplier supplier providing products for order items
	 * @param pickupLocationSupplier supplier providing pickup locations
	 * @param barista the barista who created the order
	 * @param baker the baker responsible for fulfillment
	 * @param dueDate the due date for order completion
	 * @return a fully configured order entity
	 */
	private Order createOrder(Supplier<Product> productSupplier, Supplier<PickupLocation> pickupLocationSupplier,
			User barista, User baker, LocalDate dueDate) {
		Order order = new Order(barista);

		fillCustomer(order.getCustomer());
		order.setPickupLocation(pickupLocationSupplier.get());
		order.setDueDate(dueDate);
		order.setDueTime(getRandomDueTime());
		order.changeState(barista, getRandomState(order.getDueDate()));

		int itemCount = random.nextInt(3);
		List<OrderItem> items = new ArrayList<>();
		for (int i = 0; i <= itemCount; i++) {
			OrderItem item = new OrderItem();
			Product product;
			do {
				product = productSupplier.get();
			} while (containsProduct(items, product));
			item.setProduct(product);
			item.setQuantity(random.nextInt(10) + 1);
			if (random.nextInt(5) == 0) {
				if (random.nextBoolean()) {
					item.setComment("Lactose free");
				} else {
					item.setComment("Gluten free");
				}
			}
			items.add(item);
		}
		order.setItems(items);

		order.setHistory(createOrderHistory(order, barista, baker));

		return order;
	}

	/**
	 * Creates a realistic order history based on the order's final state and timeline.
	 * History includes order placement, confirmation, preparation, and delivery/cancellation.
	 * 
	 * @param order the order for which to create history
	 * @param barista the barista who created and managed the order
	 * @param baker the baker who fulfilled the order
	 * @return a list of history items representing the order lifecycle
	 */
	private List<HistoryItem> createOrderHistory(Order order, User barista, User baker) {
		ArrayList<HistoryItem> history = new ArrayList<>();
		HistoryItem item = new HistoryItem(barista, "Order placed");
		item.setNewState(OrderState.NEW);
		LocalDateTime orderPlaced = order.getDueDate().minusDays(random.nextInt(5) + 2L).atTime(random.nextInt(10) + 7,
				00);
		item.setTimestamp(orderPlaced);
		history.add(item);
		if (order.getState() == OrderState.CANCELLED) {
			item = new HistoryItem(barista, "Order cancelled");
			item.setNewState(OrderState.CANCELLED);
			item.setTimestamp(orderPlaced.plusDays(random
					.nextInt((int) orderPlaced.until(order.getDueDate().atTime(order.getDueTime()), ChronoUnit.DAYS))));
			history.add(item);
		} else if (order.getState() == OrderState.CONFIRMED || order.getState() == OrderState.DELIVERED
				|| order.getState() == OrderState.PROBLEM || order.getState() == OrderState.READY) {
			item = new HistoryItem(baker, "Order confirmed");
			item.setNewState(OrderState.CONFIRMED);
			item.setTimestamp(orderPlaced.plusDays(random.nextInt(2)).plusHours(random.nextInt(5)));
			history.add(item);

			if (order.getState() == OrderState.PROBLEM) {
				item = new HistoryItem(baker, "Can't make it. Did not get any ingredients this morning");
				item.setNewState(OrderState.PROBLEM);
				item.setTimestamp(order.getDueDate().atTime(random.nextInt(4) + 4, 0));
				history.add(item);
			} else if (order.getState() == OrderState.READY || order.getState() == OrderState.DELIVERED) {
				item = new HistoryItem(baker, "Order ready for pickup");
				item.setNewState(OrderState.READY);
				item.setTimestamp(order.getDueDate().atTime(random.nextInt(2) + 8, random.nextBoolean() ? 0 : 30));
				history.add(item);
				if (order.getState() == OrderState.DELIVERED) {
					item = new HistoryItem(baker, "Order delivered");
					item.setNewState(OrderState.DELIVERED);
					item.setTimestamp(order.getDueDate().atTime(order.getDueTime().minusMinutes(random.nextInt(120))));
					history.add(item);
				}
			}
		}

		return history;
	}

	/**
	 * Checks if a list of order items already contains a specific product.
	 * 
	 * @param items the list of order items to check
	 * @param product the product to search for
	 * @return true if the product is found in the items list, false otherwise
	 */
	private boolean containsProduct(List<OrderItem> items, Product product) {
		for (OrderItem item : items) {
			if (item.getProduct() == product) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Generates a random due time for orders within business hours (8 AM, 12 PM, or 4 PM).
	 * 
	 * @return a random LocalTime representing the order due time
	 */
	private LocalTime getRandomDueTime() {
		int time = 8 + 4 * random.nextInt(3);

		return LocalTime.of(time, 0);
	}

	/**
	 * Determines a realistic order state based on the due date relative to today.
	 * Uses probability distributions to simulate real-world order progression:
	 * <ul>
	 * <li>Past orders: 90% delivered, 10% cancelled</li>
	 * <li>Future orders (1-2 days): 80% new, 10% problem, 10% cancelled</li>
	 * <li>Due today/tomorrow: 60% ready, 20% delivered, 10% problem, 10% cancelled</li>
	 * <li>Far future: always new</li>
	 * </ul>
	 * 
	 * @param due the due date of the order
	 * @return an appropriate OrderState based on timing and probabilities
	 */
	private OrderState getRandomState(LocalDate due) {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		LocalDate twoDays = today.plusDays(2);

		if (due.isBefore(today)) {
			if (random.nextDouble() < 0.9) {
				return OrderState.DELIVERED;
			} else {
				return OrderState.CANCELLED;
			}
		} else {
			if (due.isAfter(twoDays)) {
				return OrderState.NEW;
			} else if (due.isAfter(tomorrow)) {
				// in 1-2 days
				double resolution = random.nextDouble();
				if (resolution < 0.8) {
					return OrderState.NEW;
				} else if (resolution < 0.9) {
					return OrderState.PROBLEM;
				} else {
					return OrderState.CANCELLED;
				}
			} else {
				double resolution = random.nextDouble();
				if (resolution < 0.6) {
					return OrderState.READY;
				} else if (resolution < 0.8) {
					return OrderState.DELIVERED;
				} else if (resolution < 0.9) {
					return OrderState.PROBLEM;
				} else {
					return OrderState.CANCELLED;
				}
			}

		}
	}

	/**
	 * Generic utility method to get a random element from an array.
	 * 
	 * @param <T> the type of array elements
	 * @param array the array from which to select a random element
	 * @return a randomly selected element from the array
	 */
	private <T> T getRandom(T[] array) {
		return array[random.nextInt(array.length)];
	}

	/**
	 * Creates pickup locations and returns a supplier for random selection.
	 * Creates two locations: "Store" and "Bakery".
	 * 
	 * @param pickupLocationRepository the repository for persisting pickup locations
	 * @return a supplier that returns random pickup locations
	 */
	private Supplier<PickupLocation> createPickupLocations(PickupLocationRepository pickupLocationRepository) {
		List<PickupLocation> pickupLocations = Arrays.asList(
				pickupLocationRepository.save(createPickupLocation("Store")),
				pickupLocationRepository.save(createPickupLocation("Bakery")));
		return () -> pickupLocations.get(random.nextInt(pickupLocations.size()));
	}

	/**
	 * Creates a simple pickup location with the specified name.
	 * 
	 * @param name the name of the pickup location
	 * @return a new PickupLocation entity with the given name
	 */
	private PickupLocation createPickupLocation(String name) {
		PickupLocation store = new PickupLocation();
		store.setName(name);
		return store;
	}

	/**
	 * Creates multiple products and returns a supplier that uses Gaussian distribution
	 * for realistic random selection. Products get weighted distribution where some
	 * products are more likely to be selected than others.
	 * 
	 * @param productsRepo the repository for persisting products
	 * @param numberOfItems the number of products to create
	 * @return a supplier that returns products selected with Gaussian distribution
	 */
	private Supplier<Product> createProducts(ProductRepository productsRepo, int numberOfItems) {
		List<Product> products  = new ArrayList<>();
		for (int i = 0; i < numberOfItems; i++) {
			Product product = new Product();
			product.setName(getRandomProductName());
			double doublePrice = 2.0 + random.nextDouble() * 100.0;
			product.setPrice((int) (doublePrice * 100.0));
			products.add(productsRepo.save(product));
		}
		return () -> {
			double cutoff = 2.5;
			double g = random.nextGaussian();
			g = Math.min(cutoff, g);
			g = Math.max(-cutoff, g);
			g += cutoff;
			g /= (cutoff * 2.0);
			return products.get((int) (g * (products.size() - 1)));
		};
	}

	/**
	 * Generates a realistic bakery product name by combining fillings and types.
	 * Creates names like "Strawberry Cake" or "Chocolate Vanilla Pastry".
	 * 
	 * @return a randomly generated product name string
	 */
	private String getRandomProductName() {
		String firstFilling = getRandom(FILLING);
		String name;
		if (random.nextBoolean()) {
			String secondFilling;
			do {
				secondFilling = getRandom(FILLING);
			} while (secondFilling.equals(firstFilling));

			name = firstFilling + " " + secondFilling;
		} else {
			name = firstFilling;
		}
		name += " " + getRandom(TYPE);

		return name;
	}

	/**
	 * Creates and persists a baker user with the predefined credentials.
	 * The baker account is not locked and can be used for order fulfillment.
	 * 
	 * @param userRepository the repository for persisting the user
	 * @param passwordEncoder the encoder for hashing the password
	 * @return the created and persisted baker user entity
	 */
	private User createBaker(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return userRepository.save(
				createUser("baker@vaadin.com", "Heidi", "Carter", passwordEncoder.encode("baker"), Role.BAKER, false));
	}

	/**
	 * Creates and persists a barista user with the predefined credentials.
	 * The barista account is locked (password required) and can create orders.
	 * 
	 * @param userRepository the repository for persisting the user
	 * @param passwordEncoder the encoder for hashing the password
	 * @return the created and persisted barista user entity
	 */
	private User createBarista(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return userRepository.save(createUser("barista@vaadin.com", "Malin", "Castro",
				passwordEncoder.encode("barista"), Role.BARISTA, true));
	}

	/**
	 * Creates and persists an admin user with full system privileges.
	 * The admin account is locked (password required) and has administrative access.
	 * 
	 * @param userRepository the repository for persisting the user
	 * @param passwordEncoder the encoder for hashing the password
	 * @return the created and persisted admin user entity
	 */
	private User createAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return userRepository.save(
				createUser("admin@vaadin.com", "Göran", "Rich", passwordEncoder.encode("admin"), Role.ADMIN, true));
	}

	/**
	 * Creates additional test users that can be safely deleted during testing.
	 * These users don't have relationships with orders, making them safe to remove.
	 * 
	 * @param userRepository the repository for persisting the users
	 * @param passwordEncoder the encoder for hashing passwords
	 */
	private void createDeletableUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		userRepository.save(
				createUser("peter@vaadin.com", "Peter", "Bush", passwordEncoder.encode("peter"), Role.BARISTA, false));
		userRepository
				.save(createUser("mary@vaadin.com", "Mary", "Ocon", passwordEncoder.encode("mary"), Role.BAKER, true));
	}

	/**
	 * Creates a user entity with the specified details.
	 * This is a private utility method for user creation.
	 * 
	 * @param email the user's email address (used as username)
	 * @param firstName the user's first name
	 * @param lastName the user's last name
	 * @param passwordHash the hashed password
	 * @param role the user's role (admin, barista, baker)
	 * @param locked whether the account is locked (requires password)
	 * @return a new User entity with the specified properties
	 */
	private User createUser(String email, String firstName, String lastName, String passwordHash, String role,
			boolean locked) {
		User user = new User();
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setPasswordHash(passwordHash);
		user.setRole(role);
		user.setLocked(locked);
		return user;
	}
}
