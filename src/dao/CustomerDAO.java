import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    public int addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (full_name, phone_number, email, address, government_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getFullName());
            statement.setString(2, customer.getPhoneNumber());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getAddress());
            statement.setString(5, customer.getGovernmentId());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customer_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                customers.add(mapCustomer(resultSet));
            }
        }
        return customers;
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCustomer(resultSet);
                }
            }
        }
        return null;
    }

    public List<Customer> searchCustomerByName(String name) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE full_name LIKE ? ORDER BY customer_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + name + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(mapCustomer(resultSet));
                }
            }
        }
        return customers;
    }

    public void updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, phone_number = ?, email = ?, address = ?, government_id = ? WHERE customer_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getFullName());
            statement.setString(2, customer.getPhoneNumber());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getAddress());
            statement.setString(5, customer.getGovernmentId());
            statement.setInt(6, customer.getId());
            statement.executeUpdate();
        }
    }

    public void deleteCustomer(int customerId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            List<Reservation> reservations = new ReservationDAO().getReservationsByCustomerId(customerId);
            if (!reservations.isEmpty()) {
               throw new SQLException(
                       "Cannot delete this customer because they have "
                               + reservations.size() + " reservation(s). Cancel or complete the reservation first.");
            }

            String sql = "DELETE FROM customers WHERE customer_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
               statement.setInt(1, customerId);
               int deletedRows = statement.executeUpdate();
               if (deletedRows == 0) {
                   throw new SQLException("Customer not found with ID: " + customerId);
               }
            }
        }
    }

    private Customer mapCustomer(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer();
        customer.setId(resultSet.getInt("customer_id"));
        customer.setFullName(resultSet.getString("full_name"));
        customer.setPhoneNumber(resultSet.getString("phone_number"));
        customer.setEmail(resultSet.getString("email"));
        customer.setAddress(resultSet.getString("address"));
        customer.setGovernmentId(resultSet.getString("government_id"));
        return customer;
    }
}
