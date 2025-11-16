# Maven Central Deployment - Quick Start

## What You Need to Do

### 1. One-Time Setup (First Release Only)

#### A. Create Sonatype Account
1. Go to https://issues.sonatype.org/
2. Create an account
3. Create a new JIRA ticket requesting access to `com.badgersoft` groupId
4. Wait for approval (1-2 business days)

#### B. Generate GPG Key
```bash
# Generate key (use your email)
gpg --gen-key

# Get your key ID
gpg --list-keys
# Look for something like: 1234ABCD5678EFGH

# Publish to key servers
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
```

#### C. Configure Maven Settings
Create/edit `~/.m2/settings.xml`:

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

### 2. For Each Release

#### Step 1: Prepare Release
```bash
# Make sure everything works
mvn clean verify

# Update version in pom.xml (already done: 1.2.0)
# Update CHANGELOG.md (already done)

# Commit and tag
git add .
git commit -m "Release version 1.2.0"
git tag -a v1.2.0 -m "Version 1.2.0 - Performance optimizations"
git push origin main
git push origin v1.2.0
```

#### Step 2: Deploy to Maven Central
```bash
# This single command does everything:
mvn clean deploy -P release
```

This will:
- ✅ Build the project
- ✅ Run tests
- ✅ Generate sources JAR
- ✅ Generate javadoc JAR
- ✅ Sign all artifacts with GPG
- ✅ Upload to Maven Central staging
- ✅ Automatically release (if configured)

#### Step 3: Verify (10-30 minutes later)
```bash
# Check if it's available
curl -I https://repo1.maven.org/maven2/com/badgersoft/predict4java/1.2.0/predict4java-1.2.0.jar

# Or visit
open https://search.maven.org/artifact/com.badgersoft/predict4java/1.2.0/jar
```

#### Step 4: Prepare Next Development Version
```bash
# Update pom.xml version to 1.2.1-SNAPSHOT
git add pom.xml
git commit -m "Prepare for next development iteration"
git push origin main
```

## Current Status

✅ **Ready to Deploy!**

Your project is now configured for Maven Central deployment:
- ✅ Version updated to 1.2.0 (release version)
- ✅ POM has all required metadata
- ✅ GPG signing configured
- ✅ Nexus staging plugin configured
- ✅ Release profile created
- ✅ Distribution management configured

## What Happens When You Deploy

```
mvn clean deploy -P release
         ↓
    Build & Test
         ↓
  Generate JARs (main, sources, javadoc)
         ↓
    Sign with GPG
         ↓
  Upload to OSSRH Staging
         ↓
   Automatic Validation
         ↓
  Release to Maven Central
         ↓
  Available in 10-30 minutes!
```

## Testing Before Deployment

```bash
# Dry run - build everything without deploying
mvn clean verify -P release -Dgpg.skip=true

# Check what will be deployed
ls -la target/*.jar target/*.asc
```

Expected files:
- `predict4java-1.2.0.jar` + `.asc` signature
- `predict4java-1.2.0-sources.jar` + `.asc` signature
- `predict4java-1.2.0-javadoc.jar` + `.asc` signature

## Common Issues & Solutions

### "No public key found"
```bash
# Publish your key to multiple servers
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
```

### "Authentication failed"
- Check credentials in `~/.m2/settings.xml`
- Verify Sonatype JIRA ticket is approved
- Server ID must be `ossrh`

### "Validation failed"
- Ensure all required POM elements are present (✅ already done)
- Check that sources and javadoc JARs are generated
- Verify GPG signatures are created

## After Successful Deployment

Users can add your library:

```xml
<dependency>
    <groupId>com.badgersoft</groupId>
    <artifactId>predict4java</artifactId>
    <version>1.2.0</version>
</dependency>
```

## Need Help?

- **Detailed Guide**: See `DEPLOYMENT_GUIDE.md`
- **Step-by-Step Checklist**: See `RELEASE_CHECKLIST.md`
- **Sonatype Support**: https://issues.sonatype.org/
- **Maven Central Guide**: https://central.sonatype.org/publish/

## Quick Reference

| Task | Command |
|------|---------|
| Build & Test | `mvn clean verify` |
| Deploy to Maven Central | `mvn clean deploy -P release` |
| Skip Tests (not recommended) | `mvn clean deploy -P release -DskipTests` |
| Dry Run | `mvn clean verify -P release -Dgpg.skip=true` |
| Check Staging | Visit https://oss.sonatype.org/ |
| Verify Publication | Visit https://search.maven.org/ |

---

**You're all set!** Just complete the one-time setup, then run `mvn clean deploy -P release` to publish.
