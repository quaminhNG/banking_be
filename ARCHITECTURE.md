src/main/java/com/banking/

├── BankingApplication.java

├── config/
│   ├── database/
│   ├── security/
│   └── transaction/

├── common/
│   ├── constants/
│   ├── enums/
│   ├── utils/
│   └── response/ (Standardized API responses)

├── exception/ (Global centralized error handling)

├── infrastructure/
│   ├── persistence/
│   ├── messaging/
│   ├── cache/
│   └── externalbank/ (CORE: Third-party Bank Integrations)
│       ├── providers/ (VCB, TCB, MB, ACB, VP, BIDV, Vietin, Mock)
│       ├── ExternalBankProvider.java (The Interface/Contract)
│       └── ExternalBankManager.java (Adapter Factory)

├── modules/
│   ├── auth/ (JWT & Identity Management)
│   ├── account/ (Internal Banking Accounts)
│   ├── ledger/ (Internal Balance Management & Snapshots)
│   ├── audit/ (Transaction Logs & Auditing)
│   ├── transaction/ 
│   │   ├── idempotency/ (Double-spend prevention)
│   │   └── entity/ (Transaction History with DB Indexes)
│   │
│   └── transfer/ (Smart Routing System)
│       ├── controller/ (Rerouting between internal/external)
│       ├── service/ (TransferService & ExternalTransferService)
│       └── dto/ (Unified Request/Response Models)

└── README.md & ARCHITECTURE.md (Project Documentation)