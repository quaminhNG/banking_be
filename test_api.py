import requests
import json
import time

BASE_URL = "http://localhost:8080/api/v1"
MD_FILE = "output.md"

with open(MD_FILE, "w", encoding="utf-8") as f:
    f.write("# API Endpoints Testing Results\n\n")

def write_md(title, req_meth, req_url, req_hdrs, req_body, res_status, res_body):
    with open(MD_FILE, "a", encoding="utf-8") as f:
        f.write(f"## {title}\n")
        f.write(f"**Endpoint:** `{req_meth} {req_url}`\n\n")
        f.write("### Request\n")
        if req_hdrs:
            # We obscure the actual token for brevity, just keeping a placeholder in output
            display_headers = req_hdrs.copy()
            if "Authorization" in display_headers:
                display_headers["Authorization"] = "Bearer <TOKEN>"
            f.write("**Headers:**\n```json\n" + json.dumps(display_headers, indent=2) + "\n```\n")
        if req_body:
            f.write("**Body:**\n```json\n" + json.dumps(req_body, indent=2) + "\n```\n")
        
        f.write("\n### Response\n")
        f.write(f"**Status:** `{res_status}`\n")
        f.write("**Body:**\n```json\n")
        try:
            f.write(json.dumps(res_body, indent=2))
        except:
            f.write(str(res_body))
        f.write("\n```\n\n")

timestamp = int(time.time())
username = f"user_{timestamp}"

# 1. Register Success
req_body = {"username": username, "password": "Password@123"}
res = requests.post(f"{BASE_URL}/auth/register", json=req_body)
res_json = res.json() if res.content else ""
write_md("1. Register (Success)", "POST", "/api/v1/auth/register", None, req_body, res.status_code, res_json)

# 2. Register Fail (Duplicate)
res = requests.post(f"{BASE_URL}/auth/register", json=req_body)
res_json = res.json() if res.content else ""
write_md("2. Register (Fail - Duplicate Username)", "POST", "/api/v1/auth/register", None, req_body, res.status_code, res_json)

# 3. Register Fail (Validation Error)
req_body_invalid = {"username": ""}
res = requests.post(f"{BASE_URL}/auth/register", json=req_body_invalid)
res_json = res.json() if res.content else ""
write_md("3. Register (Fail - Validation Missing Fields)", "POST", "/api/v1/auth/register", None, req_body_invalid, res.status_code, res_json)

# 4. Login Success
res = requests.post(f"{BASE_URL}/auth/login", json=req_body)
res_json = res.json() if res.content else ""
token = res_json.get("token", "") if isinstance(res_json, dict) else ""
user_account_id = res_json.get("accountId", "") if isinstance(res_json, dict) else ""
write_md("4. Login (Success)", "POST", "/api/v1/auth/login", None, req_body, res.status_code, res_json)

# 5. Login Fail (Wrong Credentials)
req_body_fail = {"username": username, "password": "WrongPassword@123"}
res = requests.post(f"{BASE_URL}/auth/login", json=req_body_fail)
res_json = res.json() if res.content else ""
write_md("5. Login (Fail - Wrong Credentials)", "POST", "/api/v1/auth/login", None, req_body_fail, res.status_code, res_json)

headers = {"Authorization": f"Bearer {token}"} if token else {}
target_account = user_account_id if user_account_id else "ACC_123"

# 6. Create Account (Often requires ADMIN, lets see success/fail)
req_body_acc = {"amount": 500.0}
res = requests.post(f"{BASE_URL}/accounts", json=req_body_acc, headers=headers)
res_json = res.json() if res.content else ""
write_md("6. Create Account (Might need ADMIN)", "POST", "/api/v1/accounts", headers, req_body_acc, res.status_code, res_json)

# 7. Deposit Success
req_dep = {"accountId": target_account, "amount": 1000.0, "idempotencyKey": f"dep_{timestamp}"}
res = requests.post(f"{BASE_URL}/transaction/deposit", json=req_dep, headers=headers)
res_json = res.json() if res.content else ""
write_md("7. Deposit (Success)", "POST", "/api/v1/transaction/deposit", headers, req_dep, res.status_code, res_json)

# 8. Deposit Fail (Negative Amount)
req_dep_fail = {"accountId": target_account, "amount": -100.0, "idempotencyKey": f"dep_fail_{timestamp}"}
res = requests.post(f"{BASE_URL}/transaction/deposit", json=req_dep_fail, headers=headers)
res_json = res.json() if res.content else ""
write_md("8. Deposit (Fail - Negative Amount)", "POST", "/api/v1/transaction/deposit", headers, req_dep_fail, res.status_code, res_json)

# 9. Withdraw Success
req_wit = {"accountId": target_account, "amount": 200.0, "idempotencyKey": f"wit_{timestamp}"}
res = requests.post(f"{BASE_URL}/transaction/withdraw", json=req_wit, headers=headers)
res_json = res.json() if res.content else ""
write_md("9. Withdraw", "POST", "/api/v1/transaction/withdraw", headers, req_wit, res.status_code, res_json)

# 10. Withdraw Fail (Insufficient funds / bad account)
req_wit_fail = {"accountId": target_account, "amount": 999999.0, "idempotencyKey": f"wit_fail_{timestamp}"}
res = requests.post(f"{BASE_URL}/transaction/withdraw", json=req_wit_fail, headers=headers)
res_json = res.json() if res.content else ""
write_md("10. Withdraw (Fail - Insufficient Funds)", "POST", "/api/v1/transaction/withdraw", headers, req_wit_fail, res.status_code, res_json)

# 11. Transfer Valid
req_tra = {
    "fromAccountId": target_account,
    "toAccountId": "TARGET_ACC_123", # Assuming this doesn't exist, will fail but show transfer failure
    "amount": 100.0,
    "currency": "VND",
    "idempotencyKey": f"tra_{timestamp}",
    "toBankCode": ""
}
res = requests.post(f"{BASE_URL}/transaction/transfer", json=req_tra, headers=headers)
res_json = res.json() if res.content else ""
write_md("11. Transfer (Local Bank)", "POST", "/api/v1/transaction/transfer", headers, req_tra, res.status_code, res_json)

# 12. Transfer External Sandbox
req_tra_ext = {
    "fromAccountId": target_account,
    "toAccountId": "EXT_ACC",
    "amount": 100.0,
    "currency": "VND",
    "idempotencyKey": f"tra_ext_{timestamp}",
    "toBankCode": "VCB"
}
res = requests.post(f"{BASE_URL}/transaction/transfer", json=req_tra_ext, headers=headers)
res_json = res.json() if res.content else ""
write_md("12. Transfer (External Bank VCB)", "POST", "/api/v1/transaction/transfer", headers, req_tra_ext, res.status_code, res_json)

# 13. Ledger Balance Valid
res = requests.get(f"{BASE_URL}/ledger/balance/{target_account}", headers=headers)
res_json = res.json() if res.content else ""
write_md("13. Ledger Balance", "GET", f"/api/v1/ledger/balance/{target_account}", headers, None, res.status_code, res_json)

# 14. Ledger Balance Invalid/Unauthorized (Other person's id)
res = requests.get(f"{BASE_URL}/ledger/balance/OTHER_123", headers=headers)
res_json = res.json() if res.content else ""
write_md("14. Ledger Balance (Fail - Unauthorized access)", "GET", "/api/v1/ledger/balance/OTHER_123", headers, None, res.status_code, res_json)

print("Done recording endpoints to output.md")
