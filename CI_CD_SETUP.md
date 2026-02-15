# CI/CD Setup Guide

Quick setup guide for enabling CI/CD features in predict4java.

## ✅ Already Configured

The following are already set up and working:

- ✅ GitHub Actions workflows (`.github/workflows/maven-ci.yml`)
- ✅ CodeQL security scanning (`.github/workflows/codeql-analysis.yml`)
- ✅ Dependabot configuration (`.github/dependabot.yml`)
- ✅ Multi-version Java testing (11, 17, 21)
- ✅ Code quality checks (Checkstyle, SpotBugs)
- ✅ Build artifact generation
- ✅ Build status badges in README

## 🔧 Optional Setup

### 1. Codecov Integration (Recommended)

**Purpose**: Track code coverage trends and visualize coverage reports

**Steps:**

1. **Sign up for Codecov**
   - Visit https://codecov.io/
   - Sign in with your GitHub account
   - Authorize Codecov to access your repositories

2. **Add Repository**
   - Navigate to https://codecov.io/gh/g4dpz
   - Click "Add new repository"
   - Select `predict4java`

3. **Get Upload Token**
   - Go to repository settings in Codecov
   - Copy the upload token

4. **Add Token to GitHub**
   - Go to https://github.com/g4dpz/predict4java/settings/secrets/actions
   - Click "New repository secret"
   - Name: `CODECOV_TOKEN`
   - Value: Paste the token from Codecov
   - Click "Add secret"

5. **Verify**
   - Push a commit or create a PR
   - Check GitHub Actions run
   - Coverage should upload to Codecov
   - View at https://codecov.io/gh/g4dpz/predict4java

**Benefits:**
- Visual coverage reports
- Coverage trends over time
- PR comments with coverage changes
- Coverage badge (already in README)

**Cost**: Free for open source projects

---

### 2. Maven Central Deployment (For Releases)

**Purpose**: Automate releases to Maven Central

**Prerequisites:**
- OSSRH account (https://issues.sonatype.org/)
- GPG key for signing artifacts
- Maven Central credentials

**Steps:**

1. **Create GPG Key** (if not already done)
   ```bash
   gpg --gen-key
   gpg --list-keys
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

2. **Export GPG Key**
   ```bash
   gpg --export-secret-keys YOUR_KEY_ID | base64
   ```

3. **Add Secrets to GitHub**
   - `OSSRH_USERNAME`: Your Sonatype username
   - `OSSRH_PASSWORD`: Your Sonatype password
   - `GPG_PRIVATE_KEY`: Base64-encoded private key
   - `GPG_PASSPHRASE`: Your GPG passphrase

4. **Create Release Workflow** (future enhancement)
   - Trigger on version tags
   - Build with `-P release`
   - Deploy to Maven Central

**Note**: This is only needed when you're ready to publish releases. The current setup is complete for development.

---

## 🚀 Testing the CI/CD Pipeline

### 1. Verify Workflows are Active

```bash
# Check workflow files exist
ls -la .github/workflows/

# Should show:
# - maven-ci.yml
# - codeql-analysis.yml
```

### 2. Trigger a Build

**Option A: Push to master**
```bash
git add .
git commit -m "ci: test CI/CD pipeline"
git push origin master
```

**Option B: Create a Pull Request**
```bash
git checkout -b test-ci
git add .
git commit -m "ci: test CI/CD pipeline"
git push origin test-ci
# Create PR on GitHub
```

### 3. Monitor the Build

1. Go to https://github.com/g4dpz/predict4java/actions
2. Click on the latest workflow run
3. Watch the jobs execute:
   - Test (3 parallel jobs for Java 11, 17, 21)
   - Quality (Checkstyle + SpotBugs)
   - Build (Package + Upload artifacts)

### 4. Expected Results

**Test Job:**
- ✅ All 149 tests pass on Java 11
- ✅ All 149 tests pass on Java 17
- ✅ All 149 tests pass on Java 21
- ✅ Coverage report generated (Java 11)
- ⚠️ Coverage upload skipped (no CODECOV_TOKEN yet)

**Quality Job:**
- ✅ Checkstyle runs (72 violations expected)
- ✅ SpotBugs runs (should be clean)
- ✅ Site reports generated

**Build Job:**
- ✅ Package created
- ✅ JAR files verified
- ✅ Artifacts uploaded

**Total Time**: ~5-7 minutes

---

## 📊 Viewing Results

### GitHub Actions Dashboard

**URL**: https://github.com/g4dpz/predict4java/actions

**Features:**
- View all workflow runs
- Filter by workflow, branch, status
- Download build artifacts
- View logs for each step

### Build Status Badge

The README now includes a build status badge:

```markdown
[![Build Status](https://github.com/g4dpz/predict4java/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/g4dpz/predict4java/actions)
```

This badge shows:
- ✅ Green: All checks passing
- ❌ Red: Build failing
- 🟡 Yellow: Build in progress

### Coverage Badge (After Codecov Setup)

```markdown
[![codecov](https://codecov.io/gh/g4dpz/predict4java/branch/master/graph/badge.svg)](https://codecov.io/gh/g4dpz/predict4java)
```

---

## 🔍 Troubleshooting

### Build Fails Immediately

**Check:**
1. Workflow syntax is valid (YAML)
2. GitHub Actions is enabled for the repository
3. No syntax errors in pom.xml

**Fix:**
```bash
# Validate workflow locally (requires act)
act -l

# Or push and check GitHub Actions logs
```

### Tests Fail in CI but Pass Locally

**Common Causes:**
1. Different Java version
2. Missing environment variables
3. Timezone differences
4. File path issues

**Fix:**
```bash
# Test with same Java version as CI
sdk use java 11.0.x-tem
mvn clean test

# Check for hardcoded paths or assumptions
```

### Coverage Upload Fails

**Expected**: This will fail until `CODECOV_TOKEN` is added

**Fix:**
1. Set up Codecov (see Optional Setup above)
2. Add `CODECOV_TOKEN` to GitHub Secrets
3. Re-run the workflow

### Quality Checks Show Violations

**Expected**: Checkstyle shows 72 violations (acceptable for scientific code)

**Note**: Quality checks use `continue-on-error: true`, so they won't fail the build

---

## 📚 Documentation

### Complete Guides

- **[CI_CD_GUIDE.md](CI_CD_GUIDE.md)** - Comprehensive CI/CD documentation
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Maven Central deployment
- **[.github/workflows/README.md](.github/workflows/README.md)** - Workflow documentation

### Quick References

- **Run tests locally**: `mvn clean test`
- **Run quality checks**: `mvn checkstyle:check && mvn compile spotbugs:check`
- **Build artifacts**: `mvn clean package`
- **View coverage**: `mvn jacoco:report && open target/site/jacoco/index.html`

---

## ✨ What's Next?

### Immediate (No Setup Required)

1. ✅ Push code and watch CI run
2. ✅ Create PRs and see automated checks
3. ✅ Download build artifacts from Actions
4. ✅ Review Dependabot PRs for updates

### Optional Enhancements

1. 🔧 Set up Codecov for coverage tracking
2. 🔧 Configure Maven Central deployment
3. 🔧 Add custom quality gates
4. 🔧 Set up release automation

### Future Improvements

- Add performance benchmarking to CI
- Create release workflow for Maven Central
- Add integration tests
- Set up nightly builds
- Add deployment previews

---

## 🎯 Success Criteria

Your CI/CD is working correctly when:

- ✅ Builds run automatically on push/PR
- ✅ All 149 tests pass on Java 11, 17, 21
- ✅ Build artifacts are generated
- ✅ Quality checks complete (even with violations)
- ✅ Build status badge shows green
- ✅ Dependabot creates update PRs

---

## 💡 Tips

1. **Always run tests locally first**: `mvn clean test`
2. **Check CI logs for warnings**: Even passing builds may have warnings
3. **Keep dependencies updated**: Review Dependabot PRs regularly
4. **Monitor build times**: Optimize if builds take too long
5. **Use draft PRs**: Test CI without requesting reviews

---

## 📞 Support

**Issues**: https://github.com/g4dpz/predict4java/issues  
**Email**: dave@g4dpz.me.uk  
**Documentation**: See [CI_CD_GUIDE.md](CI_CD_GUIDE.md)

---

## Summary

The CI/CD pipeline is **ready to use** with no additional setup required. Codecov integration is optional but recommended for coverage tracking. All workflows will run automatically on your next push or PR.

Happy coding! 🚀
