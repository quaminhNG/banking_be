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
│   └── response/

├── exception/

├── middleware/

├── modules/
│   ├── account/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   └── mapper/
│   │
│   ├── ledger/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── domain/
│   │
│   ├── transaction/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── idempotency/
│   │
│   └── transfer/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── domain/

└── infrastructure/
    ├── persistence/
    ├── messaging/
    ├── cache/
    └── external/