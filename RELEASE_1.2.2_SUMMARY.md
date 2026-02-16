# Release 1.2.2 Summary

## Release Date
February 16, 2026

## Overview
Version 1.2.2 is a patch release that fixes timezone and locale issues in the `SatPassTime` class, ensuring consistent behavior across all geographic locations and system configurations.

## Changes

### Fixed
- **Timezone/Locale Issues in SatPassTime** (PR #6)
  - Date formatters now explicitly use `Locale.US` for consistent formatting
  - Timezone explicitly set to UTC for all date/time operations
  - Resolves test failures when running in non-US locales (e.g., Australian timezone)
  - Ensures consistent date/time display regardless of system locale settings

### Technical Details
- Added imports: `DateFormatSymbols`, `Locale`, `TimeZone`
- Moved date formatter initialization to static initializer block
- Applied explicit locale (`Locale.US`) to `SimpleDateFormat` instances
- Set timezone to UTC for both `TIME_FORMAT` and `DATE_FORMAT`

## Testing
- ✅ All 149 tests pass
- ✅ PassPredictorTest specifically validated (previously failing in Australian timezone)
- ✅ No regressions introduced
- ✅ Clean build with zero errors

## Deployment

### Maven Central
- **Status**: Uploaded and awaiting manual publishing
- **Deployment ID**: 55648cc7-14d0-449f-8be6-52103500f75f
- **Publishing URL**: https://central.sonatype.com/publishing/deployments

### Artifacts
- Main JAR: `predict4java-1.2.2.jar`
- Sources JAR: `predict4java-1.2.2-sources.jar`
- Javadoc JAR: `predict4java-1.2.2-javadoc.jar`
- All artifacts signed with GPG

### GitHub
- **Commit**: 45d44e6
- **Tag**: v1.2.2
- **Branch**: master
- **Repository**: https://github.com/g4dpz/predict4java

## Installation

Once published on Maven Central, users can update to version 1.2.2:

### Maven
```xml
<dependency>
    <groupId>uk.me.g4dpz</groupId>
    <artifactId>predict4java</artifactId>
    <version>1.2.2</version>
</dependency>
```

### Gradle
```gradle
implementation 'uk.me.g4dpz:predict4java:1.2.2'
```

## Impact

### Who Should Upgrade?
- **High Priority**: Users running applications in non-US locales
- **High Priority**: International deployments (especially Australia, Europe, Asia)
- **Medium Priority**: All users for consistency and future-proofing
- **Low Priority**: US-only deployments (but still recommended)

### Breaking Changes
None. This is a backward-compatible patch release.

### Migration
No code changes required. Simply update the version number in your dependency configuration.

## Next Steps

1. **Manual Publishing** (Author action required)
   - Visit https://central.sonatype.com/publishing/deployments
   - Review deployment 55648cc7-14d0-449f-8be6-52103500f75f
   - Click "Publish" to release to Maven Central
   - Artifacts will be available within 15-30 minutes

2. **Verification** (After publishing)
   - Verify artifacts appear on Maven Central
   - Test download and usage in a sample project
   - Update documentation if needed

3. **Communication**
   - Announce release on GitHub
   - Update project website/documentation
   - Notify users of the fix

## Credits

- **Original Issue**: Reported by mattryall in PR #6
- **Fix Applied**: From https://github.com/g4dpz/predict4java/pull/6/changes
- **Testing**: All 149 existing tests validated
- **Release**: Version 1.2.2 prepared and deployed

## Related Links

- **GitHub Release**: https://github.com/g4dpz/predict4java/releases/tag/v1.2.2
- **Pull Request**: https://github.com/g4dpz/predict4java/pull/6
- **Maven Central**: https://central.sonatype.com/artifact/uk.me.g4dpz/predict4java/1.2.2
- **Changelog**: See CHANGELOG.md for complete history

---

**Status**: ✅ Ready for manual publishing on Maven Central
**Build**: ✅ Successful (149/149 tests passing)
**Deployment**: ✅ Uploaded to Maven Central staging
**Git**: ✅ Committed and tagged (v1.2.2)
