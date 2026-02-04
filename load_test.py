import requests
import concurrent.futures
import time
import uuid
import random

# CONFIGURATION
BASE_URL = "http://localhost:8080/api/v1"
RESTAURANT_ID = "rest-1"
TOTAL_USERS = 105  # We have 100 items in stock. 105 users means 5 MUST fail.
USER_ID_PREFIX = "test-user-"

# METRICS
results = {
    "orders_placed": 0,
    "out_of_stock": 0,
    "payments_successful": 0,
    "payment_failures": 0,
    "errors": 0
}

def get_burger_id():
    """Fetch the Menu to get the actual Item UUID"""
    try:
        response = requests.get(f"{BASE_URL}/menu/{RESTAURANT_ID}")
        data = response.json()['data']
        # Just grab the first item (The Burger)
        return data[0]['id']
    except Exception as e:
        print(f"❌ CRITICAL: Could not fetch menu. is the server running? {e}")
        exit(1)

def simulate_user_flow(user_num, item_id):
    """The lifecycle of a single hungry user"""
    user_id = str(uuid.uuid4())

    # 1. PLACE ORDER
    order_payload = {
        "userId": user_id,
        "restaurantId": RESTAURANT_ID,
        "items": {
            item_id: 1 # Everyone buys 1 burger
        }
    }

    try:
        # Measure Latency
        start_time = time.time()

        # A. Order Request
        order_res = requests.post(f"{BASE_URL}/orders", json=order_payload)

        if order_res.status_code == 200:
            order_data = order_res.json()
            order_id = order_data['data']
            results["orders_placed"] += 1
            # print(f"✅ User {user_num}: Ordered! ID: {order_id}")

            # B. Pay Request (Simulate Webhook)
            # Generate a fake transaction ID
            txn_id = f"txn_{uuid.uuid4()}"
            pay_url = f"{BASE_URL}/payment/webhook?amount=150.00&txnId={txn_id}&orderId={order_id}"

            pay_res = requests.post(pay_url)

            if pay_res.status_code == 200:
                results["payments_successful"] += 1
            else:
                results["payment_failures"] += 1
                print(f"⚠️ User {user_num}: Payment Failed! {pay_res.text}")

        else:
            # Check if it was an Out of Stock error
            if "Inventory not sufficient" in order_res.text:
                results["out_of_stock"] += 1
                # print(f"🛑 User {user_num}: Out of Stock!")
            else:
                results["errors"] += 1
                print(f"❌ User {user_num}: Error {order_res.status_code} - {order_res.text}")

    except Exception as e:
        results["errors"] += 1
        print(f"🔥 User {user_num}: Exception {e}")

# --- MAIN EXECUTION ---
print(f"🚀 STARTING LOAD TEST: {TOTAL_USERS} Users attacking {RESTAURANT_ID}...")
print(f"🎯 Target: Buying 1 Burger each. Total Stock: 100 (Usually)")

burger_id = get_burger_id()
print(f"🍔 Target Item ID: {burger_id}")

start_test = time.time()

# Run concurrent threads
with concurrent.futures.ThreadPoolExecutor(max_workers=50) as executor:
    futures = [executor.submit(simulate_user_flow, i, burger_id) for i in range(TOTAL_USERS)]
    concurrent.futures.wait(futures)

end_test = time.time()
duration = end_test - start_test

print("\n" + "="*40)
print("📊 LOAD TEST RESULTS")
print("="*40)
print(f"⏱️  Duration:        {duration:.2f} seconds")
print(f"⚡ Throughput:      {TOTAL_USERS / duration:.2f} TPS")
print("-" * 20)
print(f"✅ Orders Placed:   {results['orders_placed']}")
print(f"💰 Payments Paid:   {results['payments_successful']}")
print(f"🛑 Out of Stock:    {results['out_of_stock']}  <-- (Should be 5 if stock was 100)")
print(f"❌ Errors:          {results['errors']}")
print("="*40)