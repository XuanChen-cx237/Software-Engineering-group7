
# Personal Finance Manager (Java Swing Application)

## Overview

This is a Java-based desktop application developed with the Swing framework. It offers users a streamlined way to manage personal finances through budgeting, data analysis, and intelligent financial assistance. The system is modular in design, ensuring clarity, extensibility, and maintainability.

### Key Modules

- **BudgetObserver**: Tracks user expenditures in real time against customized budgets. Alerts are issued when spending nears or exceeds defined thresholds, and monthly reports are generated for financial review.
- **AIConsultant**: An integrated advisory module that offers personalized financial advice based on user queries. It interfaces with an external AI API and provides actionable, context-aware suggestions.
- **CSVImporter**: Enables efficient importing of transaction data from user-provided CSV files, including automatic field mapping and validation.
- **Graphical Interface**: Built using Java Swing, the GUI allows users to interact with their financial data intuitively through multiple panels, including budgets, analytics, and consultation.

---

## Features

### ✅ Real-Time Budget Management

- Define budgets by spending category.
- Visual feedback and warnings on budget consumption.
- Automatic monthly budget analysis and summaries.

### 📈 Financial Data Visualization

- Dynamic charts and graphs display spending trends.
- Filter and compare categories over specific time frames.
- Data-backed insights to help users adjust behavior.

### 📂 CSV Import Capability

- Users can bulk import historical or third-party financial data.
- Data is parsed, validated, and stored automatically.
- Supports common CSV formats from banks or export tools.

### 🤖 Intelligent Financial Assistant

- Users can ask financial questions in natural language.
- The assistant provides tailored responses considering user context.
- Built-in safeguards ensure relevance and usefulness of suggestions.

---

## System Architecture

```
+------------------+       +---------------------+       +----------------------+
|     Swing GUI    | <---> |   Core Service Layer| <---> | External AI Platform |
|  (User Interface)|       |  (Budget, CSV, Logic)|      |  (e.g., OpenAI API)  |
+------------------+       +---------------------+       +----------------------+
```

The application separates concerns between interface, logic, and integration for clear development and maintenance.

---

## Prerequisites

- Java JDK 8 or later
- Apache Maven
- Internet access for AI service integration
- An API Key for AI (e.g., from OpenAI)

---

## Setup & Installation

1. **Clone the repository**  
   ```bash
   git clone https://github.com/your-org/finance-manager.git
   cd finance-manager
   ```

2. **Set up API Key**  
   Configure your environment:
   ```bash
   export OPENAI_API_KEY=your_key_here
   ```

3. **Edit Configuration**  
   Update the `config.properties` file with your API credentials and any local preferences.

4. **Build the application**  
   Use Maven to compile and package:
   ```bash
   mvn clean install
   ```

---

## Running the Application

After a successful build, you can run the application with:

```bash
java -jar target/finance-manager-1.0.jar
```

Ensure that `config.properties` is available in the classpath and properly configured.

---

## Usage

| Panel               | Description |
|---------------------|-------------|
| **Budget Panel**     | Define spending limits and track usage across time. |
| **Transactions Panel** | Import and browse historical financial transactions. |
| **Analytics Panel**   | Visualize financial trends with charts and summaries. |
| **AI Consultant Panel** | Ask financial questions and receive contextual advice. |

---

## Testing

To execute unit tests:

```bash
mvn test
```

Sample CSV files for testing imports are located in the `/sample-data` directory.

---

## Troubleshooting

| Issue                       | Solution |
|----------------------------|----------|
| API connection errors      | Ensure your `OPENAI_API_KEY` is valid and network is connected. |
| UI not displaying properly | Verify Java version and ensure Swing is supported. |
| CSV not importing correctly| Confirm file format and delimiter are correct; check logs for details. |

---

## Contribution Guide

We welcome community contributions! To contribute:

1. Fork this repository.
2. Create a new feature branch.
3. Make and test your changes.
4. Submit a pull request with a clear explanation of your modifications.

---

## License

This project is licensed under the MIT License. See the [LICENSE](./LICENSE) file for details.

---

## Contact

For inquiries or bug reports, please reach out to:

📧 Email: 1355590559@qq.com  
🔗 GitHub: [InChalezy](https://github.com/InChalezy)
