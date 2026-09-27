import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public int addCustomer(Customer customer) throws SQLException {
        return customerDAO.addCustomer(customer);
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.getAllCustomers();
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        return customerDAO.getCustomerById(customerId);
    }

    public List<Customer> searchCustomerByName(String name) throws SQLException {
        return customerDAO.searchCustomerByName(name);
    }

    public void updateCustomer(Customer customer) throws SQLException {
        customerDAO.updateCustomer(customer);
    }

    public void deleteCustomer(int customerId) throws SQLException {
        customerDAO.deleteCustomer(customerId);
    }
}
