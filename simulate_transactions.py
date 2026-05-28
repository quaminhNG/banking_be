import sys
import time
import uuid
import json
from concurrent.futures import ThreadPoolExecutor, as_completed
import requests

BASE_URL = "http://localhost:8080"
NUM_TRANSACTIONS = 1000
REPORT_FILE = "test_results.md"

if len(sys.argv) > 1:
    NUM_TRANSACTIONS = int(sys.argv[1])
if len(sys.argv) > 2:
    REPORT_FILE = sys.argv[2]
if len(sys.argv) > 3:
    BASE_URL = sys.argv[3]

def register_user():
    username = f"user_{uuid.uuid4().hex[:8]}"
    password = "password123"
    url = f"{BASE_URL}/api/v1/auth/register"
    payload = {"username": username, "password": password}
    try:
        response = requests.post(url, json=payload)
        if response.status_code == 200:
            data = response.json()
            return data.get("token"), data.get("accountId")
        else:
            print(f"Failed to register user: {response.text}")
            return None, None
    except Exception as e:
        print(f"Error registering user: {e}")
        return None, None

def send_deposit(token, account_id):
    url = f"{BASE_URL}/api/v1/transaction/deposit"
    headers = {"Authorization": f"Bearer {token}"}
    idempotency_key = str(uuid.uuid4())
    payload = {
        "accountId": account_id,
        "amount": 100.0,
        "idempotencyKey": idempotency_key
    }
    
    start_time = time.time()
    try:
        response = requests.post(url, json=payload, headers=headers)
        duration = time.time() - start_time
        return {
            "status_code": response.status_code,
            "duration": duration,
            "success": response.status_code == 200,
            "response": response.text[:200]  # limit response text
        }
    except Exception as e:
        duration = time.time() - start_time
        return {
            "status_code": "ERROR",
            "duration": duration,
            "success": False,
            "response": str(e)
        }

def main():
    print(f"Starting test with {NUM_TRANSACTIONS} transactions against {BASE_URL}...")
    token, account_id = register_user()
    if not token or not account_id:
        print("Could not proceed without token or accountId")
        return

    print(f"Registered user. AccountId: {account_id}")
    
    results = []
    start_test_time = time.time()
    
    # Using 50 workers to simulate high concurrency
    with ThreadPoolExecutor(max_workers=50) as executor:
        futures = [executor.submit(send_deposit, token, account_id) for _ in range(NUM_TRANSACTIONS)]
        for future in as_completed(futures):
            results.append(future.result())
            
    end_test_time = time.time()
    total_time = end_test_time - start_test_time
    
    success_count = sum(1 for r in results if r["success"])
    fail_count = len(results) - success_count
    
    durations = [r["duration"] for r in results]
    avg_duration = sum(durations) / len(durations) if durations else 0
    max_duration = max(durations) if durations else 0
    min_duration = min(durations) if durations else 0
    
    # Generate report
    report = f"""# Kết quả Test Luồng Thread Giao Dịch

- **Thời gian thực hiện**: {time.strftime('%Y-%m-%d %H:%M:%S')}
- **URL mục tiêu**: {BASE_URL}
- **Tổng số transaction giả lập**: {NUM_TRANSACTIONS}
- **Tổng thời gian**: {total_time:.2f} giây
- **Throughput**: {NUM_TRANSACTIONS / total_time:.2f} trans/sec

## Tóm tắt kết quả
- **Thành công**: {success_count}
- **Thất bại**: {fail_count}
- **Thời gian phản hồi trung bình**: {avg_duration:.4f} giây
- **Thời gian phản hồi lớn nhất**: {max_duration:.4f} giây
- **Thời gian phản hồi nhỏ nhất**: {min_duration:.4f} giây

## Chi tiết (Mẫu 10 kết quả đầu tiên)
| STT | Status Code | Thời gian (s) | Response (rút gọn) |
| --- | --- | --- | --- |
"""
    for i, r in enumerate(results[:10]):
        report += f"| {i+1} | {r['status_code']} | {r['duration']:.4f} | {r['response']} |\n"
        
    with open(REPORT_FILE, "w", encoding="utf-8") as f:
        f.write(report)
        
    print(f"Report written to {REPORT_FILE}")

if __name__ == "__main__":
    main()
