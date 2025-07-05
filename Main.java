import java.time.LocalDate;
import java.util.*;

// Interface for shippable products
interface Shippable {
    String getName();
    double getWeight();
}

// Interface for expirable products
interface Expirable {
    boolean isExpired();
}

// Abstract base class for all products
abstract class Product {
    String name;
    double price;
    int quantity;

    Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    void decreaseQuantity(int qty) {
        quantity -= qty;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}

// Product that expires and needs shipping (e.g. Cheese)
class ExpiringShippableProduct extends Product implements Expirable, Shippable {
    LocalDate expiryDate;
    double weight;

    ExpiringShippableProduct(String name, double price, int quantity, LocalDate expiryDate, double weight) {
        super(name, price, quantity);
        this.expiryDate = expiryDate;
        this.weight = weight;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public double getWeight() {
        return weight;
    }
}

// Product that needs shipping only (e.g. TV)
class ShippableProduct extends Product implements Shippable {
    double weight;

    ShippableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}

// Product that expires but doesn't need shipping (e.g. Biscuits)
class ExpiringProduct extends Product implements Expirable {
    LocalDate expiryDate;

    ExpiringProduct(String name, double price, int quantity, LocalDate expiryDate) {
        super(name, price, quantity);
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
}

// Digital product (e.g. Scratch Card)
class DigitalProduct extends Product {
    DigitalProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }
}

// Customer class with name and balance
class Customer {
    String name;
    double balance;

    Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    void pay(double amount) {
        if (balance < amount) {
            throw new RuntimeException("Not enough balance.");
        }
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }
}

// One item in the cart
class CartItem {
    Product product;
    int quantity;

    CartItem(Product product, int quantity) {
        if (quantity > product.getQuantity()) {
            throw new RuntimeException("Not enough stock for " + product.getName());
        }
        this.product = product;
        this.quantity = quantity;
    }

    double getTotalPrice() {
        return product.getPrice() * quantity;
    }

    Product getProduct() {
        return product;
    }

    int getQuantity() {
        return quantity;
    }
}

// The cart class
class Cart {
    List<CartItem> items = new ArrayList<>();

    void add(Product product, int quantity) {
        items.add(new CartItem(product, quantity));
    }

    List<CartItem> getItems() {
        return items;
    }

    boolean isEmpty() {
        return items.isEmpty();
    }
}

// Shipping service to print shipping details
class ShippingService {
    static void ship(List<CartItem> cartItems) {
        double totalWeight = 0;
        System.out.println("** Shipment notice **");
        for (CartItem item : cartItems) {
            if (item.getProduct() instanceof Shippable) {
                Shippable s = (Shippable) item.getProduct();
                double itemWeight = s.getWeight() * item.getQuantity();
                totalWeight += itemWeight;
                System.out.printf("%dx %s %.0fg\n", item.getQuantity(), s.getName(), itemWeight * 1000);
            }
        }
        System.out.printf("Total package weight %.1fkg\n", totalWeight);
    }
}

// Checkout service
class CheckoutService {
    static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            throw new RuntimeException("Cart is empty.");
        }

        double subtotal = 0;
        double shippingFee = 30;
        boolean needsShipping = false;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            // Check expiry
            if (product instanceof Expirable && ((Expirable) product).isExpired()) {
                throw new RuntimeException(product.getName() + " is expired.");
            }

            // Check stock
            if (item.getQuantity() > product.getQuantity()) {
                throw new RuntimeException(product.getName() + " is out of stock.");
            }

            subtotal += item.getTotalPrice();

            if (product instanceof Shippable) {
                needsShipping = true;
            }
        }

        double total = subtotal + (needsShipping ? shippingFee : 0);

        if (customer.getBalance() < total) {
            throw new RuntimeException("Customer does not have enough balance.");
        }

        if (needsShipping) {
            ShippingService.ship(cart.getItems());
        }

        customer.pay(total);

        for (CartItem item : cart.getItems()) {
            item.getProduct().decreaseQuantity(item.getQuantity());
        }

        // Print receipt
        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.printf("%dx %s %.0f\n", item.getQuantity(), item.getProduct().getName(), item.getTotalPrice());
        }
        System.out.println("----------------------");
        System.out.printf("Subtotal %.0f\n", subtotal);
        System.out.printf("Shipping %.0f\n", needsShipping ? shippingFee : 0);
        System.out.printf("Amount %.0f\n", total);
        System.out.printf("Customer balance %.0f\n", customer.getBalance());
    }
}

// Main method to test everything
public class Main {
    public static void main(String[] args) {
        // Create products
        Product cheese = new ExpiringShippableProduct("Cheese", 100, 5, LocalDate.now().plusDays(2), 0.2);
        Product biscuits = new ExpiringProduct("Biscuits", 150, 2, LocalDate.now().plusDays(1));
        Product tv = new ShippableProduct("TV", 3000, 3, 5.0);
        Product scratchCard = new DigitalProduct("Scratch Card", 50, 10);

        // Create a customer
        Customer customer = new Customer("Mariam", 1000);

        // Add products to cart
        Cart cart = new Cart();
        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        // Checkout
        try {
            CheckoutService.checkout(customer, cart);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
