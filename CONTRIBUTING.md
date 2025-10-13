# Contributing to Mini QuickFIX

Thank you for your interest in contributing to Mini QuickFIX! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Code Review Process](#code-review-process)

## Code of Conduct

We are committed to providing a welcoming and inclusive environment. Please be respectful and professional in all interactions.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally
3. Create a new branch for your feature or bugfix
4. Make your changes
5. Submit a pull request

## Development Setup

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Git
- Docker (optional, for containerization)

### Clone and Build

```bash
git clone https://github.com/alaneel/mini-quickfix.git
cd mini-quickfix
mvn clean install
```

### Running Tests

```bash
# Run all tests
mvn test

# Run with code coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Running Code Quality Checks

```bash
# Run all quality checks
mvn verify

# Run individual checks
mvn checkstyle:check
mvn pmd:check
mvn spotbugs:check
```

## Coding Standards

### Java Code Style

We follow standard Java coding conventions with the following specifics:

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Maximum 120 characters
- **Naming Conventions**:
  - Classes: PascalCase
  - Methods: camelCase
  - Constants: UPPER_SNAKE_CASE
  - Variables: camelCase

### Code Quality

- Write self-documenting code with clear variable and method names
- Add Javadoc comments for all public classes and methods
- Keep methods short and focused (< 150 lines)
- Maintain high code coverage (> 80% for new code)
- Use SLF4J for logging, never System.out.println()

### Example

```java
/**
 * Parses a FIX message from a string representation.
 *
 * @param fixString the FIX message string to parse
 * @return the parsed FixMessage
 * @throws FixException if the message format is invalid
 */
public static FixMessage parse(String fixString) throws FixException {
    if (fixString == null || fixString.isEmpty()) {
        throw new FixException("FIX message string is null or empty");
    }
    // Implementation...
}
```

## Testing

### Unit Tests

- Write unit tests for all new features and bug fixes
- Use JUnit 5 for test framework
- Use Mockito for mocking dependencies
- Use AssertJ for fluent assertions
- Aim for > 80% code coverage

### Test Structure

```java
@DisplayName("FixParser Tests")
class FixParserTest {

    @Test
    @DisplayName("Should parse valid FIX message")
    void testParseValidMessage() {
        // Given
        String fixString = "8=FIX.4.4|35=D|...";

        // When
        FixMessage message = FixParser.parseReadable(fixString);

        // Then
        assertThat(message.getMsgType()).isEqualTo("D");
    }
}
```

### Integration Tests

- Place integration tests in `src/test/java` with `IT` suffix
- Use `@Tag("integration")` annotation
- Test real scenarios with actual network connections when applicable

## Submitting Changes

### Branch Naming

Use descriptive branch names:
- `feature/add-market-data-support`
- `bugfix/fix-sequence-number-issue`
- `docs/update-readme`

### Commit Messages

Follow the conventional commits format:

```
type(scope): subject

body

footer
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

Example:
```
feat(parser): add support for repeating groups

Implement parsing logic for FIX repeating groups with proper
validation and error handling.

Closes #123
```

### Pull Request Process

1. **Before Creating PR**:
   - Update your branch with latest main: `git rebase main`
   - Run all tests: `mvn clean test`
   - Run code quality checks: `mvn verify`
   - Update documentation if needed

2. **Creating the PR**:
   - Use a clear, descriptive title
   - Fill out the PR template completely
   - Link related issues
   - Add screenshots for UI changes

3. **PR Template**:
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Describe how you tested your changes

## Checklist
- [ ] Tests pass locally
- [ ] Code follows style guidelines
- [ ] Documentation updated
- [ ] No new warnings
```

## Code Review Process

### Review Criteria

Code reviews will check for:
- **Functionality**: Does it work as intended?
- **Tests**: Are there adequate tests?
- **Code Quality**: Is it maintainable and readable?
- **Performance**: Are there any performance concerns?
- **Security**: Are there any security issues?

### Review Timeline

- Initial review within 2 business days
- Follow-up reviews within 1 business day
- Approved PRs merged within 1 business day

### Addressing Feedback

- Respond to all review comments
- Make requested changes in new commits
- Mark conversations as resolved when addressed
- Re-request review after addressing feedback

## Building and Testing Locally

### Full Build

```bash
# Clean build with all checks
mvn clean verify

# Build Docker image
docker build -t mini-quickfix:dev .

# Run with Docker Compose
docker-compose up
```

### Quick Development Cycle

```bash
# Fast build without quality checks
mvn clean package -DskipTests

# Run specific test class
mvn test -Dtest=FixParserTest

# Run single test method
mvn test -Dtest=FixParserTest#testParseValidMessage
```

## Project Structure

```
miniQuickFix/
├── src/
│   ├── main/java/com/example/fix/
│   │   ├── message/          # Message handling
│   │   ├── session/          # Session management
│   │   ├── validation/       # Message validation
│   │   ├── store/            # Persistence
│   │   ├── event/            # Event handling
│   │   ├── transport/        # Network transport
│   │   └── types/            # Enumerations
│   └── test/java/            # Test files
├── .github/workflows/        # CI/CD pipelines
├── pom.xml                   # Maven configuration
├── Dockerfile                # Docker build
├── docker-compose.yml        # Docker orchestration
└── checkstyle.xml           # Code style rules
```

## Getting Help

- Open an issue for bugs or feature requests
- Join our community discussions
- Read the project documentation
- Check existing issues and PRs

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

Thank you for contributing to Mini QuickFIX!
