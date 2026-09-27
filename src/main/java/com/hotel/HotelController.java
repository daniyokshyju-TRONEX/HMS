package com.hotel;

import java.time.LocalDate;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HotelController {
    private final JdbcTemplate db;
    public HotelController(JdbcTemplate db) { this.db = db; }

    @GetMapping("/rooms")
    public List<Map<String,Object>> rooms(@RequestParam(required=false) Integer roomNumber,
                                          @RequestParam(required=false) Boolean available) {
        String sql = roomNumber != null ? "SELECT * FROM rooms WHERE room_number=?" :
            Boolean.TRUE.equals(available) ? "SELECT * FROM rooms WHERE availability_status='AVAILABLE' ORDER BY room_number" :
            "SELECT * FROM rooms ORDER BY room_number";
        List<Map<String,Object>> rows = roomNumber != null ? db.queryForList(sql, roomNumber) : db.queryForList(sql);
        return rows.stream().map(this::room).toList();
    }

    @PostMapping("/rooms")
    public Map<String,Object> addRoom(@RequestParam int roomNumber, @RequestParam(defaultValue="SINGLE") String roomType,
                                      @RequestParam double pricePerNight) {
        db.update("INSERT INTO rooms(room_number,room_type,price_per_night,availability_status) VALUES(?,?,?,'AVAILABLE')",
                roomNumber, roomType.toUpperCase(), pricePerNight);
        return ok("Room added successfully");
    }
    @DeleteMapping("/rooms")
    public Map<String,Object> deleteRoom(@RequestParam int roomNumber) {
        db.update("DELETE FROM rooms WHERE room_number=?", roomNumber); return ok("Room deleted successfully");
    }

    @GetMapping("/customers")
    public List<Map<String,Object>> customers(@RequestParam(required=false) Integer customerId,
                                              @RequestParam(required=false) String name) {
        List<Map<String,Object>> rows = customerId != null ? db.queryForList("SELECT * FROM customers WHERE customer_id=?", customerId) :
            name != null && !name.isBlank() ? db.queryForList("SELECT * FROM customers WHERE full_name LIKE ? ORDER BY customer_id", "%"+name+"%") :
            db.queryForList("SELECT * FROM customers ORDER BY customer_id");
        return rows.stream().map(this::customer).toList();
    }
    @PostMapping("/customers")
    public Map<String,Object> addCustomer(@RequestParam String fullName, @RequestParam String phoneNumber,
                                          @RequestParam String email, @RequestParam String address, @RequestParam String governmentId) {
        db.update("INSERT INTO customers(full_name,phone_number,email,address,government_id) VALUES(?,?,?,?,?)",
                fullName, phoneNumber, email, address, governmentId);
        Integer id = db.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        Map<String,Object> out = ok("Customer added successfully"); out.put("customerId", id); return out;
    }
    @DeleteMapping("/customers")
    public Map<String,Object> deleteCustomer(@RequestParam int customerId) {
        if (count("SELECT COUNT(*) FROM reservations WHERE customer_id=?", customerId) > 0)
            throw new IllegalArgumentException("Cannot delete this customer because they have reservations.");
        db.update("DELETE FROM customers WHERE customer_id=?", customerId); return ok("Customer deleted successfully");
    }

    @GetMapping("/reservations")
    public List<Map<String,Object>> reservations(@RequestParam(required=false) Integer reservationId) {
        List<Map<String,Object>> rows = reservationId == null ? db.queryForList("SELECT * FROM reservations ORDER BY reservation_id") :
            db.queryForList("SELECT * FROM reservations WHERE reservation_id=?", reservationId);
        return rows.stream().map(this::reservation).toList();
    }
    @PostMapping("/reservations")
    public Map<String,Object> addReservation(@RequestParam int customerId, @RequestParam int roomNumber,
            @RequestParam String checkInDate, @RequestParam String checkOutDate, @RequestParam int numberOfGuests) {
        LocalDate in=LocalDate.parse(checkInDate), out=LocalDate.parse(checkOutDate);
        if (!out.isAfter(in) || numberOfGuests < 1) throw new IllegalArgumentException("Invalid dates or number of guests.");
        Map<String,Object> room = one("SELECT * FROM rooms WHERE room_number=?", roomNumber);
        if (room == null || !"AVAILABLE".equals(room.get("availability_status"))) throw new IllegalArgumentException("Room is not available.");
        if (count("SELECT COUNT(*) FROM reservations WHERE room_number=? AND reservation_status IN ('PENDING','CONFIRMED','CHECKED_IN') AND check_in_date < ? AND check_out_date > ?", roomNumber, out, in) > 0)
            throw new IllegalArgumentException("Room already has an overlapping reservation.");
        double total = (out.toEpochDay()-in.toEpochDay()) * ((Number)room.get("price_per_night")).doubleValue();
        db.update("INSERT INTO reservations(customer_id,room_number,check_in_date,check_out_date,number_of_guests,reservation_status,total_amount) VALUES(?,?,?,?,?,'PENDING',?)",
                customerId,roomNumber,in,out,numberOfGuests,total);
        Map<String,Object> result=ok("Reservation created successfully"); result.put("reservationId", db.queryForObject("SELECT LAST_INSERT_ID()", Integer.class)); result.put("estimatedTotal",total); return result;
    }
    @DeleteMapping("/reservations")
    public Map<String,Object> cancel(@RequestParam int reservationId) {
        Map<String,Object> reservation = requireReservation(reservationId);
        if (!Set.of("PENDING", "CONFIRMED").contains(reservation.get("reservation_status")))
            throw new IllegalStateException("Only pending or confirmed reservations can be cancelled.");
        db.update("UPDATE reservations SET reservation_status='CANCELLED' WHERE reservation_id=?", reservationId);
        return ok("Reservation cancelled successfully");
    }

    @PostMapping("/checkin")
    @Transactional
    public Map<String,Object> checkin(@RequestParam int reservationId) {
        Map<String,Object> r=requireReservation(reservationId);
        if (!Set.of("PENDING", "CONFIRMED").contains(r.get("reservation_status")))
            throw new IllegalStateException("Only pending or confirmed reservations can be checked in.");
        db.update("UPDATE reservations SET reservation_status='CHECKED_IN' WHERE reservation_id=?",reservationId);
        db.update("UPDATE rooms SET availability_status='OCCUPIED' WHERE room_number=?",r.get("room_number"));
        return ok("Reservation checked in successfully");
    }
    @PostMapping("/checkout")
    @Transactional
    public Map<String,Object> checkout(@RequestParam int reservationId) {
        Map<String,Object> r=requireReservation(reservationId);
        if (!"CHECKED_IN".equals(r.get("reservation_status"))) throw new IllegalStateException("Reservation must be checked in before checkout.");
        Map<String,Object> room=one("SELECT * FROM rooms WHERE room_number=?",r.get("room_number"));
        double bill=days(r)*((Number)room.get("price_per_night")).doubleValue();
        db.update("UPDATE reservations SET reservation_status='CHECKED_OUT',total_amount=? WHERE reservation_id=?",bill,reservationId);
        db.update("UPDATE rooms SET availability_status='AVAILABLE' WHERE room_number=?",r.get("room_number"));
        Map<String,Object> out=ok("Reservation checked out successfully"); out.put("reservationId",reservationId); out.put("bill",bill); return out;
    }

    @GetMapping("/payments")
    public List<Map<String,Object>> payments() { return db.queryForList("SELECT * FROM payments ORDER BY payment_id").stream().map(this::payment).toList(); }
    @PostMapping("/payments")
    public Map<String,Object> payment(@RequestParam int reservationId,@RequestParam double amount,@RequestParam(defaultValue="CASH") String paymentMethod) {
        requireReservation(reservationId); if (amount<=0) throw new IllegalArgumentException("Payment amount must be greater than zero.");
        db.update("INSERT INTO payments(reservation_id,amount,payment_date,payment_method,payment_status) VALUES(?,?,CURRENT_DATE,?,'PAID')",reservationId,amount,paymentMethod.toUpperCase());
        Map<String,Object> out=ok("Payment recorded successfully"); out.put("paymentId",db.queryForObject("SELECT LAST_INSERT_ID()",Integer.class)); return out;
    }
    @GetMapping("/bill")
    public Map<String,Object> bill(@RequestParam int reservationId) {
        Map<String,Object> r=requireReservation(reservationId), room=one("SELECT * FROM rooms WHERE room_number=?",r.get("room_number"));
        Map<String,Object> c=one("SELECT * FROM customers WHERE customer_id=?",r.get("customer_id")), p=one("SELECT payment_status FROM payments WHERE reservation_id=? ORDER BY payment_id DESC LIMIT 1",reservationId);
        double total=days(r)*((Number)room.get("price_per_night")).doubleValue();
        Map<String,Object> out=new LinkedHashMap<>(); out.put("reservationId",reservationId); out.put("customer",c.get("full_name")); out.put("room",r.get("room_number")); out.put("roomType",room.get("room_type")); out.put("checkIn",r.get("check_in_date")); out.put("checkOut",r.get("check_out_date")); out.put("numberOfNights",days(r)); out.put("pricePerNight",room.get("price_per_night")); out.put("totalAmount",total); out.put("paymentStatus",p==null?"PENDING":p.get("payment_status")); return out;
    }
    @GetMapping("/reports")
    public Map<String,Object> reports() { Map<String,Object> r=new LinkedHashMap<>(); r.put("totalRooms",count("SELECT COUNT(*) FROM rooms")); r.put("availableRooms",count("SELECT COUNT(*) FROM rooms WHERE availability_status='AVAILABLE'")); r.put("occupiedRooms",count("SELECT COUNT(*) FROM rooms WHERE availability_status='OCCUPIED'")); r.put("totalCustomers",count("SELECT COUNT(*) FROM customers")); r.put("activeReservations",count("SELECT COUNT(*) FROM reservations WHERE reservation_status IN ('PENDING','CONFIRMED','CHECKED_IN')")); r.put("completedReservations",count("SELECT COUNT(*) FROM reservations WHERE reservation_status='CHECKED_OUT'")); r.put("totalPayments",count("SELECT COUNT(*) FROM payments")); return r; }

    private Map<String,Object> room(Map<String,Object> r){ return Map.of("roomNumber",r.get("room_number"),"roomType",r.get("room_type"),"pricePerNight",r.get("price_per_night"),"availabilityStatus",r.get("availability_status")); }
    private Map<String,Object> customer(Map<String,Object> r){ return Map.of("customerId",r.get("customer_id"),"fullName",r.get("full_name"),"phoneNumber",r.get("phone_number"),"email",r.get("email"),"address",r.get("address"),"governmentId",r.get("government_id")); }
    private Map<String,Object> reservation(Map<String,Object> r){ return Map.of("reservationId",r.get("reservation_id"),"customerId",r.get("customer_id"),"roomNumber",r.get("room_number"),"checkInDate",r.get("check_in_date"),"checkOutDate",r.get("check_out_date"),"numberOfGuests",r.get("number_of_guests"),"status",r.get("reservation_status"),"totalAmount",r.get("total_amount")); }
    private Map<String,Object> payment(Map<String,Object> r){ return Map.of("paymentId",r.get("payment_id"),"reservationId",r.get("reservation_id"),"amount",r.get("amount"),"paymentDate",r.get("payment_date"),"paymentMethod",r.get("payment_method"),"paymentStatus",r.get("payment_status")); }
    private Map<String,Object> ok(String message){return new LinkedHashMap<>(Map.of("success",true,"message",message));}
    private long count(String sql,Object... args){return db.queryForObject(sql,Long.class,args);}
    private Map<String,Object> one(String sql,Object... args){List<Map<String,Object>> x=db.queryForList(sql,args);return x.isEmpty()?null:x.get(0);}
    private Map<String,Object> requireReservation(int id){Map<String,Object> r=one("SELECT * FROM reservations WHERE reservation_id=?",id);if(r==null)throw new IllegalArgumentException("Reservation not found with ID: "+id);return r;}
    private long days(Map<String,Object> r){return date(r.get("check_out_date")).toEpochDay()-date(r.get("check_in_date")).toEpochDay();}
    private LocalDate date(Object value) {
        if (value instanceof java.sql.Date) return ((java.sql.Date)value).toLocalDate();
        if (value instanceof LocalDate) return (LocalDate)value;
        return LocalDate.parse(String.valueOf(value));
    }
    @ExceptionHandler(Exception.class) ResponseEntity<Map<String,String>> error(Exception e){return ResponseEntity.badRequest().body(Map.of("error",Optional.ofNullable(e.getMessage()).orElse("Request failed"))); }
}
