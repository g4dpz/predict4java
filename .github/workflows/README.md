# GitHub Actions Workflows

This directory contains the CI/CD workflows for predict4java.

## Active Workflows

### 1. Java CI with Maven (`maven-ci.yml`)
**Status**: ✅ Active  
**Purpose**: Continuous integration for testing, quality checks, and builds

**Features:**
- Multi-version Java testing (11, 17, 21)
- Parallel job execution for faster feedback
- Code coverage reporting with Codecov
- Checkstyle and SpotBugs quality checks
- Artifact generation and retention

**Triggers:**
- Push to `master` branch
- Pull requests to `master` branch

**Jobs:**
1. **Test** - Run tests on Java 11, 17, and 21
2. **Quality** - Run Checkstyle and SpotBugs
3. **Build** - Build artifacts and verify

### 2. CodeQL Analysis (`codeql-analysis.yml`)
**Status**: ✅ Active  
**Purpose**: Security vulnerability scanning

**Features:**
- Automated security analysis
- Vulnerability detection
- Code quality insights

**Triggers:**
- Push to `master` branch
- Pull requests to `master` branch
- Scheduled: Every Friday at 5:00 AM UTC

## Workflow Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Push/PR to master                     │
└─────────────────────┬───────────────────────────────────┘
                      │
          ┌───────────┴───────────┐
          │                       │
          ▼                       ▼
    ┌─────────┐           ┌──────────────┐
    │  Test   │           │   Quality    │
    │ (3 JVMs)│           │  (Checkstyle │
    │         │           │  + SpotBugs) │
    └────┬────┘           └──────┬───────┘
         │                       │
         └───────────┬───────────┘
                     │
                     ▼
              ┌──────────┐
              │  Build   │
              │ (Package │
              │ + Upload)│
              └──────────┘
```

## Configuration Files

### Dependabot (`dependabot.yml`)
**Purpose**: Automated dependency updates

**Monitors:**
- Maven dependencies (weekly)
- GitHub Actions (weekly)

**Settings:**
- Max 5 PRs for Maven dependencies
- Max 3 PRs for GitHub Actions
- Auto-labels: `dependencies`, `maven`, `github-actions`
- Commit prefixes: `chore` (Maven), `ci` (Actions)

## Build Status

View workflow runs: https://github.com/g4dpz/predict4java/actions

**Current Status:**
- ✅ Java CI with Maven
- ✅ CodeQL Analysis

## Coverage Reporting

Coverage reports are automatically uploaded to Codecov after test runs on Java 11.

**View Coverage:**
- Codecov Dashboard: https://codecov.io/gh/g4dpz/predict4java
- Coverage Badge: ![codecov](https://codecov.io/gh/g4dpz/predict4java/branch/master/graph/badge.svg)

**Current Metrics:**
- Instruction Coverage: 94%
- Branch Coverage: 78%
- Total Tests: 149

## Secrets Required

For full CI/CD functionality, configure these secrets in GitHub Settings:

| Secret | Purpose | Required For |
|--------|---------|--------------|
| `CODECOV_TOKEN` | Upload coverage reports | Coverage reporting |
| `OSSRH_USERNAME` | Maven Central deployment | Release workflow (future) |
| `OSSRH_PASSWORD` | Maven Central deployment | Release workflow (future) |
| `GPG_PRIVATE_KEY` | Sign artifacts | Release workflow (future) |
| `GPG_PASSPHRASE` | Unlock GPG key | Release workflow (future) |

## Local Testing

Test workflows locally before pushing:

```bash
# Run tests
mvn clean test

# Run quality checks
mvn checkstyle:check
mvn compile spotbugs:check

# Build artifacts
mvn clean package

# Generate coverage report
mvn jacoco:report
```

## Troubleshooting

### Workflow Fails on Test Job
- Check test logs in GitHub Actions
- Run tests locally: `mvn clean test`
- Ensure all tests pass before pushing

### Quality Job Shows Violations
- Checkstyle and SpotBugs use `continue-on-error: true`
- Review violations but build will not fail
- Fix critical issues before merging

### Coverage Upload Fails
- Verify `CODECOV_TOKEN` is set in GitHub Secrets
- Check Codecov service status
- Review workflow logs for upload errors

### Build Artifacts Missing
- Ensure test and quality jobs completed
- Check build job logs for errors
- Verify Maven package phase succeeded

## Maintenance

### Updating Workflows

1. Edit workflow files in `.github/workflows/`
2. Test changes in a feature branch
3. Review workflow run results
4. Merge to master when validated

### Adding New Jobs

1. Define job in `maven-ci.yml`
2. Set dependencies with `needs: [job1, job2]`
3. Add appropriate steps
4. Test in pull request

### Modifying Java Versions

Edit the matrix in `maven-ci.yml`:

```yaml
strategy:
  matrix:
    java: [ '11', '17', '21', '23' ]  # Add/remove versions
```

## Documentation

For detailed CI/CD information, see:
- [CI_CD_GUIDE.md](../../CI_CD_GUIDE.md) - Complete CI/CD documentation
- [DEPLOYMENT_GUIDE.md](../../DEPLOYMENT_GUIDE.md) - Maven Central deployment
- [CONTRIBUTING.md](../../CONTRIBUTING.md) - Contribution guidelines

## Support

For workflow issues:
1. Check GitHub Actions logs
2. Review [CI_CD_GUIDE.md](../../CI_CD_GUIDE.md)
3. Open an issue: https://github.com/g4dpz/predict4java/issues
4. Contact: dave@g4dpz.me.uk
