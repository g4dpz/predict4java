# Release Checklist for Predict4Java

Use this checklist when preparing a new release.

## Pre-Release Preparation

### 1. Code Quality
- [ ] All tests pass: `mvn clean test`
- [ ] No compiler warnings: `mvn clean compile`
- [ ] Code review completed
- [ ] Documentation updated

### 2. Version Management
- [ ] Update version in `pom.xml` (remove `-SNAPSHOT`)
- [ ] Update `CHANGELOG.md` with release notes
- [ ] Update `README.md` if needed
- [ ] Update version in documentation examples

### 3. Testing
```bash
# Run full test suite
mvn clean verify

# Check package contents
mvn clean package -DskipTests
ls -la target/*.jar

# Verify all required JARs are created:
# - predict4java-X.X.X.jar
# - predict4java-X.X.X-sources.jar
# - predict4java-X.X.X-javadoc.jar
```

### 4. Git Preparation
```bash
# Commit version changes
git add pom.xml CHANGELOG.md README.md
git commit -m "Release version X.X.X"

# Create and push tag
git tag -a vX.X.X -m "Version X.X.X - Brief description"
git push origin main
git push origin vX.X.X
```

## Maven Central Deployment

### 5. Prerequisites Check
- [ ] Sonatype OSSRH account created and approved
- [ ] GPG key generated and published to key servers
- [ ] Maven settings.xml configured with credentials
- [ ] Internet connection stable

### 6. Deploy to Maven Central
```bash
# Deploy with release profile
mvn clean deploy -P release

# If you need to skip tests (not recommended)
mvn clean deploy -P release -DskipTests
```

### 7. Verify Deployment
- [ ] Check staging repository at https://oss.sonatype.org/
- [ ] Verify all artifacts are signed (`.asc` files present)
- [ ] Verify sources and javadoc JARs are included
- [ ] Wait for automatic release (if configured) or manually release

### 8. Verify Publication
After 10-30 minutes:
- [ ] Check Maven Central: https://repo1.maven.org/maven2/com/badgersoft/predict4java/
- [ ] Check Maven Search: https://search.maven.org/artifact/com.badgersoft/predict4java

After 2-4 hours:
- [ ] Verify searchable on Maven Central
- [ ] Test dependency resolution in a sample project

## Post-Release

### 9. GitHub Release
- [ ] Create GitHub release from tag
- [ ] Copy CHANGELOG entry to release notes
- [ ] Attach JAR files (optional)
- [ ] Publish release

### 10. Update to Next Development Version
```bash
# Update version to next SNAPSHOT
# Example: 1.2.0 -> 1.2.1-SNAPSHOT
vim pom.xml

git add pom.xml
git commit -m "Prepare for next development iteration"
git push origin main
```

### 11. Communication
- [ ] Announce release on GitHub
- [ ] Update project website (if applicable)
- [ ] Notify users/community
- [ ] Update any dependent projects

### 12. Documentation
- [ ] Update API documentation
- [ ] Update migration guides
- [ ] Update examples with new version

## Quick Commands Reference

```bash
# Full release process
mvn clean verify                          # Test everything
git tag -a vX.X.X -m "Version X.X.X"     # Tag release
git push origin main --tags               # Push to GitHub
mvn clean deploy -P release               # Deploy to Maven Central

# Verify deployment
curl -I https://repo1.maven.org/maven2/com/badgersoft/predict4java/X.X.X/predict4java-X.X.X.jar

# Test in sample project
mvn dependency:get -Dartifact=com.badgersoft:predict4java:X.X.X
```

## Rollback Procedure

If something goes wrong:

### Before Release to Central
1. Login to https://oss.sonatype.org/
2. Find your staging repository
3. Click "Drop" to delete it
4. Fix issues and redeploy

### After Release to Central
- **Cannot be undone!** Maven Central is immutable
- Release a new patch version with fixes
- Update documentation to skip the problematic version

## Troubleshooting

### GPG Signing Issues
```bash
# Test GPG
echo "test" | gpg --clearsign

# List keys
gpg --list-keys

# Publish key
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### Authentication Issues
- Verify `~/.m2/settings.xml` credentials
- Check Sonatype JIRA ticket status
- Ensure server ID matches: `ossrh`

### Validation Failures
- Check all required metadata in pom.xml
- Verify all artifacts are signed
- Ensure sources and javadoc JARs exist

## Version Numbering Guide

Follow [Semantic Versioning](https://semver.org/):

- **MAJOR** (X.0.0): Breaking changes, incompatible API changes
- **MINOR** (1.X.0): New features, backward compatible
- **PATCH** (1.2.X): Bug fixes, backward compatible

Examples:
- `1.1.4` → `1.2.0` (new features, optimizations)
- `1.2.0` → `1.2.1` (bug fixes)
- `1.2.1` → `2.0.0` (breaking changes, Java 11 requirement)

## Support

- **Sonatype Issues**: https://issues.sonatype.org/
- **Maven Central Guide**: https://central.sonatype.org/publish/
- **Project Issues**: https://github.com/badgersoftdotcom/predict4java/issues
