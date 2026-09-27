const state = {
  rooms: [],
  customers: [],
  reservations: [],
  payments: [],
  reports: {}
};

async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);
  const text = await response.text();
  try {
    return JSON.parse(text);
  } catch (error) {
    return { error: text };
  }
}

function setMessage(elementId, message, isError = false) {
  const node = document.getElementById(elementId);
  node.textContent = message;
  node.style.color = isError ? '#b91c1c' : '#0f766e';
}

function badgeClass(value) {
  const normalized = String(value || '').toUpperCase();
  const map = {
    AVAILABLE: 'status-available',
    OCCUPIED: 'status-occupied',
    MAINTENANCE: 'status-maintenance',
    CONFIRMED: 'status-confirmed',
    PENDING: 'status-pending',
    CANCELLED: 'status-cancelled',
    CHECKED_IN: 'status-checked-in',
    CHECKED_OUT: 'status-checked-out',
    PAID: 'status-confirmed',
    FAILED: 'status-cancelled'
  };
  return map[normalized] || 'status-pending';
}

function formatCurrency(value) {
  return Number(value || 0).toFixed(2);
}

function renderBillPreview(result) {
  const panel = document.getElementById('billPreview');
  if (!result || result.error) {
    panel.textContent = result && result.error ? result.error : 'No bill generated yet.';
    panel.style.color = '#b91c1c';
    return;
  }

  panel.style.color = '#1f2937';
  panel.innerHTML = `
    <strong>HOTEL BILL</strong><br>
    Reservation ID: ${result.reservationId}<br>
    Customer: ${result.customer}<br>
    Room: ${result.room} (${result.roomType})<br>
    Check-in: ${result.checkIn}<br>
    Check-out: ${result.checkOut}<br>
    Number of Nights: ${result.numberOfNights}<br>
    Price per Night: ${formatCurrency(result.pricePerNight)}<br>
    Total Amount: ${formatCurrency(result.totalAmount)}<br>
    Payment Status: ${result.paymentStatus}
  `;
}

async function loadReports() {
  const reports = await fetchJson('/api/reports');
  state.reports = reports;
  document.getElementById('totalRooms').textContent = reports.totalRooms ?? 0;
  document.getElementById('availableRooms').textContent = reports.availableRooms ?? 0;
  document.getElementById('occupiedRooms').textContent = reports.occupiedRooms ?? 0;
  document.getElementById('totalCustomers').textContent = reports.totalCustomers ?? 0;
  document.getElementById('activeReservations').textContent = reports.activeReservations ?? 0;
  document.getElementById('totalPayments').textContent = reports.totalPayments ?? 0;
}

async function loadRooms() {
  const rooms = await fetchJson('/api/rooms');
  state.rooms = Array.isArray(rooms) ? rooms : [];
  const tbody = document.getElementById('roomsTableBody');
  tbody.innerHTML = state.rooms.map(room => `
    <tr>
      <td>${room.roomNumber}</td>
      <td>${room.roomType}</td>
      <td>${formatCurrency(room.pricePerNight)}</td>
      <td><span class="status-pill ${badgeClass(room.availabilityStatus)}">${room.availabilityStatus}</span></td>
    </tr>
  `).join('') || '<tr><td colspan="4">No rooms found</td></tr>';
}

async function loadCustomers() {
  const customers = await fetchJson('/api/customers');
  state.customers = Array.isArray(customers) ? customers : [];
  const tbody = document.getElementById('customersTableBody');
  tbody.innerHTML = state.customers.map(customer => `
    <tr>
      <td>${customer.customerId}</td>
      <td>${customer.fullName}</td>
      <td>${customer.phoneNumber}</td>
      <td>${customer.email}</td>
    </tr>
  `).join('') || '<tr><td colspan="4">No customers found</td></tr>';
}

async function loadReservations() {
  const reservations = await fetchJson('/api/reservations');
  state.reservations = Array.isArray(reservations) ? reservations : [];
  const tbody = document.getElementById('reservationsTableBody');
  tbody.innerHTML = state.reservations.map(reservation => `
    <tr>
      <td>${reservation.reservationId}</td>
      <td>${reservation.customerId}</td>
      <td>${reservation.roomNumber}</td>
      <td>${reservation.checkInDate} to ${reservation.checkOutDate}</td>
      <td><span class="status-pill ${badgeClass(reservation.status)}">${reservation.status}</span></td>
    </tr>
  `).join('') || '<tr><td colspan="5">No reservations found</td></tr>';
}

async function loadPayments() {
  const payments = await fetchJson('/api/payments');
  state.payments = Array.isArray(payments) ? payments : [];
  const tbody = document.getElementById('paymentsTableBody');
  tbody.innerHTML = state.payments.map(payment => `
    <tr>
      <td>${payment.paymentId}</td>
      <td>${payment.reservationId}</td>
      <td>${formatCurrency(payment.amount)}</td>
      <td>${payment.paymentMethod}</td>
      <td><span class="status-pill ${badgeClass(payment.paymentStatus)}">${payment.paymentStatus}</span></td>
    </tr>
  `).join('') || '<tr><td colspan="5">No payments found</td></tr>';
}

async function loadAllData() {
  await Promise.all([
    loadReports(),
    loadRooms(),
    loadCustomers(),
    loadReservations(),
    loadPayments()
  ]);
}

async function searchRoomForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const roomNumber = formData.get('roomNumber');
  const result = await fetchJson(`/api/rooms?roomNumber=${encodeURIComponent(roomNumber)}`);
  const rooms = Array.isArray(result) ? result : [];
  if (rooms.length === 0) {
    setMessage('roomSearchMessage', 'Room not found.', true);
    return;
  }
  const room = rooms[0];
  setMessage('roomSearchMessage', `Room ${room.roomNumber}: ${room.roomType}, ₹${formatCurrency(room.pricePerNight)}, ${room.availabilityStatus}`);
  await loadRooms();
}

async function deleteRoomForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const params = new URLSearchParams(formData);
  const result = await fetchJson('/api/rooms', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  });

  if (result.success) {
    setMessage('roomDeleteMessage', result.message || 'Room deleted successfully.');
    event.target.reset();
    await loadAllData();
  } else {
    setMessage('roomDeleteMessage', result.error || 'Delete failed.', true);
  }
}

async function searchCustomerForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const customerId = formData.get('customerId');
  const name = formData.get('name');
  const url = customerId ? `/api/customers?customerId=${encodeURIComponent(customerId)}` : `/api/customers?name=${encodeURIComponent(name || '')}`;
  const result = await fetchJson(url);
  const customers = Array.isArray(result) ? result : [];

  if (customers.length === 0) {
    setMessage('customerSearchMessage', 'Customer not found.', true);
    return;
  }

  const customer = customers[0];
  setMessage('customerSearchMessage', `Customer ${customer.customerId}: ${customer.fullName} (${customer.email})`);
  await loadCustomers();
}

async function deleteCustomerForm(event) {
  event.preventDefault();
  const confirmed = window.confirm(
    'Delete this customer only? Customers with reservations cannot be deleted.'
  );
  if (!confirmed) {
    return;
  }

  const formData = new FormData(event.target);
  const params = new URLSearchParams(formData);
  const result = await fetchJson('/api/customers', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  });

  if (result.success) {
    setMessage('customerDeleteMessage', result.message || 'Customer deleted successfully.');
    event.target.reset();
    await loadAllData();
  } else {
    setMessage('customerDeleteMessage', result.error || 'Delete failed.', true);
  }
}

async function cancelReservationForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const params = new URLSearchParams(formData);
  const result = await fetchJson('/api/reservations', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  });

  if (result.success) {
    setMessage('reservationDeleteMessage', result.message || 'Reservation cancelled successfully.');
    event.target.reset();
    await loadAllData();
  } else {
    setMessage('reservationDeleteMessage', result.error || 'Cancellation failed.', true);
  }
}

async function submitRoomForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const params = new URLSearchParams(formData);
  const result = await fetchJson('/api/rooms', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  });

  if (result.success) {
    setMessage('roomMessage', result.message || 'Room added successfully.');
    event.target.reset();
    await loadAllData();
  } else {
    setMessage('roomMessage', result.error || 'Room add failed.', true);
  }
}

async function submitCustomerForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const params = new URLSearchParams(formData);
  const result = await fetchJson('/api/customers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  });

  if (result.success) {
    setMessage('customerMessage', result.message || 'Customer added successfully.');
    event.target.reset();
    await loadAllData();
  } else {
    setMessage('customerMessage', result.error || 'Customer add failed.', true);
  }
}

async function submitReservationForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const params = new URLSearchParams(formData);
  const result = await fetchJson('/api/reservations', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  });

  if (result.success) {
    setMessage('reservationMessage', `${result.message} (Reservation ID: ${result.reservationId})`);
    event.target.reset();
    await loadAllData();
  } else {
    setMessage('reservationMessage', result.error || 'Reservation failed.', true);
  }
}

async function submitCheckInForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const params = new URLSearchParams(formData);
  const result = await fetchJson('/api/checkin', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  });

  if (result.success) {
    setMessage('checkinMessage', result.message || 'Check-in successful.');
    event.target.reset();
    await loadAllData();
  } else {
    setMessage('checkinMessage', result.error || 'Check-in failed.', true);
  }
}

async function submitCheckoutForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const params = new URLSearchParams(formData);
  const result = await fetchJson('/api/checkout', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  });

  if (result.success) {
    setMessage('checkoutMessage', `${result.message} Bill: ${formatCurrency(result.bill)}`);
    event.target.reset();
    await loadAllData();
  } else {
    setMessage('checkoutMessage', result.error || 'Check-out failed.', true);
  }
}

async function submitPaymentForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const params = new URLSearchParams(formData);
  const result = await fetchJson('/api/payments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  });

  if (result.success) {
    setMessage('paymentMessage', result.message || 'Payment recorded successfully.');
    event.target.reset();
    await loadAllData();
  } else {
    setMessage('paymentMessage', result.error || 'Payment failed.', true);
  }
}

async function submitBillForm(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  const reservationId = formData.get('reservationId');
  const result = await fetchJson(`/api/bill?reservationId=${encodeURIComponent(reservationId)}`);
  renderBillPreview(result);
}

document.getElementById('roomForm').addEventListener('submit', submitRoomForm);
document.getElementById('customerForm').addEventListener('submit', submitCustomerForm);
document.getElementById('reservationForm').addEventListener('submit', submitReservationForm);
document.getElementById('checkinForm').addEventListener('submit', submitCheckInForm);
document.getElementById('checkoutForm').addEventListener('submit', submitCheckoutForm);
document.getElementById('paymentForm').addEventListener('submit', submitPaymentForm);
document.getElementById('billForm').addEventListener('submit', submitBillForm);
document.getElementById('roomSearchForm').addEventListener('submit', searchRoomForm);
document.getElementById('roomDeleteForm').addEventListener('submit', deleteRoomForm);
document.getElementById('customerSearchForm').addEventListener('submit', searchCustomerForm);
document.getElementById('customerDeleteForm').addEventListener('submit', deleteCustomerForm);
document.getElementById('reservationDeleteForm').addEventListener('submit', cancelReservationForm);
document.getElementById('refreshAllBtn').addEventListener('click', loadAllData);
document.getElementById('loadRoomsBtn').addEventListener('click', loadRooms);
document.getElementById('loadCustomersBtn').addEventListener('click', loadCustomers);
document.getElementById('loadReservationsBtn').addEventListener('click', loadReservations);
document.getElementById('loadPaymentsBtn').addEventListener('click', loadPayments);

loadAllData();
