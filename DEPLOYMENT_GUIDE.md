# Maven Central Deployment Guide for Predict4Java

This guide walks you through publishing predict4java to Maven Central Repository.

## Prerequisites

### 1. Sonatype OSSRH Account
- Create an account at https://issues.sonatype.org/
- Create a JIRA ticket to claim your groupId (com.badgersoft)
- Wait for approval (usually 1-2 business days)
- Example ticket: https://issues.sonatype.org/browse/OSSRH-XXXXX

### 2. GPG Key for Signing
Generate a GPG key if you don't have one:

```bash
# Generate key
gpg --gen-key

# List keys to get your key ID
gpg --list-keys

# Publish your public key to key server
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### 3. Maven Settings Configuration
Add credentials to `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>YOUR_SONATYPE_USERNAME</username>
      <password>YOUR_SONATYPE_PASSWORD</password>
    </server>
  </servers>
  
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

## Deployment Steps

### Step 1: Verify Everything Builds

```bash
# Clean build with tests
mvn clean verify

# Check that all required files are generated
ls -la target/predict4java-*.jar
```

Expected files:
- `predict4java-1.2.0.jar` - Main artifact
- `predict4java-1.2.0-sources.jar` - Source code
- `predict4java-1.2.0-javadoc.jar` - JavaDoc

### Step 2: Deploy to Staging Repository

```bash
# Deploy to OSSRH staging
mvn clean deploy -P release

# Or if you want to skip tests (not recommended for releases)
mvn clean deploy -DskipTests -P release
```

This will:
1. Build the project
2. Run tests
3. Generate sources and javadoc JARs
4. Sign all artifacts with GPG
5. Upload to OSSRH staging repository

### Step 3: Release from Staging

If `autoReleaseAfterClose` is set to `true` in the pom.xml (it is), the artifact will automatically be released to Maven Central after validation.

Otherwise, manually release via:
1. Login to https://oss.sonatype.org/
2. Click "Staging Repositories"
3. Find your repository (com.badgersoft-XXXX)
4. Click "Close" and wait for validation
5. Click "Release" to publish to Maven Central

### Step 4: Verify Publication

After 10-30 minutes, verify at:
- https://repo1.maven.org/maven2/com/badgersoft/predict4java/
- https://search.maven.org/artifact/com.badgersoft/predict4java

After 2-4 hours, it will be available in Maven Central search.

## Version Management

### For Development (Snapshots)
```xml
<version>1.2.1-SNAPSHOT</version>
```

Deploy snapshots with:
```bash
mvn clean deploy
```

Snapshots go to: https://oss.sonatype.org/content/repositories/snapshots/

### For Releases
```xml
<version>1.2.0</version>
```

Always increment version for each release:
- Major: Breaking changes (2.0.0)
- Minor: New features, backward compatible (1.2.0)
- Patch: Bug fixes (1.2.1)

## Quick Deployment Commands

```bash
# 1. Update version in pom.xml (remove -SNAPSHOT)
# 2. Commit changes
git add pom.xml
git commit -m "Release version 1.2.0"
git tag -a v1.2.0 -m "Version 1.2.0 - Performance optimizations"

# 3. Deploy to Maven Central
mvn clean deploy -P release

# 4. Push to GitHub
git push origin main
git push origin v1.2.0

# 5. Update to next development version
# Change version to 1.2.1-SNAPSHOT in pom.xml
git add pom.xml
git commit -m "Prepare for next development iteration"
git push origin main
```

## Troubleshooting

### Issue: GPG Signing Fails
```bash
# Make sure GPG is in your PATH
which gpg

# Test signing manually
gpg --sign test.txt

# If using GPG 2.x, you might need to add to settings.xml:
<gpg.executable>gpg2</gpg.executable>
```

### Issue: Authentication Failed
- Verify credentials in `~/.m2/settings.xml`
- Check that your Sonatype JIRA ticket is approved
- Ensure server ID matches: `<id>ossrh</id>`

### Issue: Validation Errors
Common requirements:
- ✅ POM has name, description, url
- ✅ POM has license information
- ✅ POM has developer information
- ✅ POM has SCM information
- ✅ All artifacts are signed with GPG
- ✅ Sources JAR is included
- ✅ Javadoc JAR is included

### Issue: "No public key" Error
```bash
# Publish your key to multiple servers
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
gpg --keyserver pgp.mit.edu --send-keys YOUR_KEY_ID
```

## Release Checklist

Before deploying:
- [ ] All tests pass: `mvn clean test`
- [ ] Version updated (no -SNAPSHOT)
- [ ] CHANGELOG.md updated
- [ ] README.md updated with new version
- [ ] Git tag created
- [ ] GPG key is published
- [ ] Maven settings configured
- [ ] Sonatype account is approved

After deploying:
- [ ] Verify on Maven Central
- [ ] Update GitHub release notes
- [ ] Announce release
- [ ] Update version to next SNAPSHOT

## Maven Central Sync Time

- **Staging to Central**: 10-30 minutes
- **Search Index**: 2-4 hours
- **Full propagation**: Up to 24 hours

## Using the Published Artifact

Once published, users can add to their `pom.xml`:

```xml
<dependency>
    <groupId>com.badgersoft</groupId>
    <artifactId>predict4java</artifactId>
    <version>1.2.0</version>
</dependency>
```

## Additional Resources

- [OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [Maven GPG Plugin](https://maven.apache.org/plugins/maven-gpg-plugin/)
- [Nexus Staging Plugin](https://github.com/sonatype/nexus-maven-plugins/tree/main/staging/maven-plugin)
- [Working with PGP Signatures](https://central.sonatype.org/publish/requirements/gpg/)

## Support

For issues with:
- **Sonatype/OSSRH**: https://issues.sonatype.org/
- **Maven Central**: https://central.sonatype.org/
- **This project**: https://github.com/badgersoftdotcom/predict4java/issues
