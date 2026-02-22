# Betting Management App

A native Android betting management application built with Kotlin, Jetpack Compose, and Room database.

## Features

### Database Schema (Room ORM)
- **SettingsEntity**: Single configuration row with commission %, limits, multipliers
- **VoucherEntity**: Batch metadata with timestamp, raw text, total amount
- **EntryEntity**: Individual bets with foreign key to voucher, number, bet type, amount
- **MasterHistoryEntity**: Forwarded batches log
- **ClearedLimitEntity**: Historical risk tracking

### 4-Tab Interface
1. **Entry Tab**: Text input, parsing logic, submit button, live table
2. **Vouchers Tab**: Scrollable list with edit/delete/forward options
3. **Master History Tab**: Historical view with reversal capability
4. **Report Tab**: Self/Master report with filtering and number analysis

### Real-time Dashboard
- **Total Sales Card**: Real-time sales calculation with limit progress
- **Commission Card**: Commission based on settings
- **Max Payout Card**: Maximum possible loss with risk level
- **Net Profit Card**: Profit/loss calculation with trend indicator

### Parsing Engine
- Parse shorthand: `123=100*50` (direct) and `123r50` (rolled)
- Permutation algorithm for rolled bets (6 combinations)
- Input validation and error messages

## Architecture
- **MVVM Pattern**: ViewModel for each tab
- **Repository Pattern**: Single source of truth for database operations
- **StateFlow/LiveData**: Reactive UI updates
- **Dependency Injection**: Hilt for dependency management
- **Compose Navigation**: Tab-based navigation

## Technical Stack
- Kotlin 1.9+
- Jetpack Compose 1.5+
- Room 2.6+
- Coroutines/Flow
- Material 3 design system
- Hilt for dependency injection

## Project Structure

```
betting-app/
├── database/
│   ├── AppDatabase.kt          # Room database setup
│   ├── SettingsEntity.kt       # Settings table entity
│   ├── VoucherEntity.kt        # Voucher table entity
│   ├── EntryEntity.kt          # Entry table entity
│   ├── MasterHistoryEntity.kt  # Master history entity
│   ├── ClearedLimitEntity.kt   # Cleared limits entity
│   ├── SettingsDao.kt          # Settings DAO
│   ├── VoucherDao.kt           # Voucher DAO
│   ├── EntryDao.kt             # Entry DAO
│   ├── MasterHistoryDao.kt     # Master history DAO
│   └── ClearedLimitDao.kt      # Cleared limits DAO
├── repository/
│   └── BettingRepository.kt    # Repository with business logic
├── parsing/
│   └── BetParser.kt            # Input parsing engine
├── ui/
│   ├── dashboard/              # Dashboard components
│   │   ├── DashboardHeader.kt
│   │   ├── TotalSalesCard.kt
│   │   ├── CommissionCard.kt
│   │   ├── MaxPayoutCard.kt
│   │   └── NetProfitCard.kt
│   ├── tabs/                   # Tab components
│   │   ├── EntryTab.kt
│   │   ├── VouchersTab.kt
│   │   ├── MasterHistoryTab.kt
│   │   └── ReportTab.kt
│   └── components/             # Reusable UI components
├── MainActivity.kt             # Main activity with tab navigation
└── build.gradle.kts            # Build configuration
```

## Key Features

### Real-time Calculations
- Total sales with limit caps
- Commission calculation
- Maximum potential payout
- Net profit/loss

### Data Management
- CRUD operations for all entities
- Transaction support
- Data consistency checks
- Error handling

### UI/UX
- Dark/light theme support
- Responsive design
- Real-time updates
- Intuitive navigation

## Setup Instructions

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Build and run on emulator or device

## Usage

### Adding Bets
1. Go to Entry tab
2. Enter bets in format:
   - Direct: `123=100*80`
   - Rolled: `123r50`
3. Multiple bets can be entered on separate lines
4. Click Submit to create voucher

### Managing Vouchers
1. View all vouchers in Vouchers tab
2. Edit, delete, or forward vouchers
3. Forwarded vouchers appear in Master History

### Reports
1. Self Report shows your daily limit usage
2. Master Report shows forwarded amounts
3. Number analysis shows grouped bets by number

## License
This project is for educational purposes.