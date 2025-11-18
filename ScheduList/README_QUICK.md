# Quick Reference - GitHub Actions & Testing

## 📋 Quick Commands

### Local Testing
```bash
# Run all unit tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run specific test class
./gradlew test --tests TaskTest

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint

# Full build (includes tests and lint)
./gradlew build

# Clean build
./gradlew clean build
```

### Check Test Results
```bash
# Open unit test report (Windows)
start app/build/reports/tests/testDebugUnitTest/index.html

# Open lint report (Windows)
start app/build/reports/lint-results-debug.html
```

## 🔧 GitHub Actions Setup Checklist

### Step 1: Add Required Secrets
Go to GitHub Repository → Settings → Secrets and variables → Actions

Add these secrets:
- ✅ `GOOGLE_WEB_CLIENT_ID` - Your OAuth 2.0 Web Client ID
- ✅ `GOOGLE_SERVICES_JSON` - Content of your google-services.json file

### Step 2: Push Your Code
```bash
git add .
git commit -m "Add GitHub Actions CI/CD and automated testing"
git push origin main
```

### Step 3: Verify Workflow
1. Go to your repository on GitHub
2. Click the "Actions" tab
3. You should see the workflow running

## 📁 What Was Added

### GitHub Workflows
```
.github/workflows/
├── android-ci.yml        # Main CI: build, test, lint
├── android-release.yml   # Release builds on tags
└── pr-checks.yml         # PR validation
```

### Unit Tests (app/src/test/)
```
data/model/
├── TaskTest.kt           # 7 tests
├── EventTest.kt          # 7 tests
└── UserTest.kt           # 6 tests
```

### Instrumented Tests (app/src/androidTest/)
```
ui/
└── MainActivityTest.kt   # 2 tests
data/model/
└── DataModelInstrumentedTest.kt  # 5 tests
util/
└── GridSpacingItemDecorationTest.kt  # 3 tests
```

### Documentation
```
TESTING.md          # Comprehensive testing guide
CI_CD_SETUP.md      # Complete setup documentation
README_QUICK.md     # This file
```

## 🚀 Workflows Overview

### android-ci.yml
**Triggers:** Push/PR to main/develop
**Jobs:**
- Build & Unit Tests (runs in ~2-3 min)
- Lint Checks (runs in parallel)
- Instrumented Tests (runs in ~5-7 min with emulator)

### android-release.yml
**Triggers:** Push tag (e.g., `v1.0.0`)
**Output:**
- Release APK
- Release AAB (Android App Bundle)
- GitHub Release with artifacts

### pr-checks.yml
**Triggers:** Pull request opened/updated
**Checks:**
- PR title validation
- Quick build check
- Unit tests
- Adds comment with build status

## 🏷️ Creating a Release

```bash
# Create and push a version tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

This will automatically:
1. Build release APK and AAB
2. Create GitHub release
3. Upload artifacts to release

## ✅ Test Status

**Current:**
- ✅ 20 unit tests passing
- ✅ Build successful
- ✅ Lint configured (warnings reported, not blocking)

**Coverage Areas:**
- Data models (Task, Event, User)
- Enums (EnergyLevel, ReminderType)
- UI utilities
- Basic activity tests

## 🐛 Troubleshooting

### Tests fail locally
```bash
# Clean and rebuild
./gradlew clean test

# Check specific test output
./gradlew test --tests TaskTest --info
```

### CI fails on GitHub
1. Check the Actions tab for error details
2. Verify secrets are correctly set
3. Check if google-services.json is valid JSON

### Lint issues
```bash
# View lint report
./gradlew lint
# Report is at: app/build/reports/lint-results-debug.html
```

### Build fails
```bash
# Sync Gradle
./gradlew --refresh-dependencies

# Clear cache
./gradlew clean cleanBuildCache
```

## 📊 Viewing Reports on GitHub

After workflow runs:
1. Go to Actions tab
2. Click on a workflow run
3. Scroll to "Artifacts" section
4. Download:
   - `test-reports` - HTML test reports
   - `lint-results` - Lint analysis
   - `build-outputs` - APK files

## 🎯 Next Steps

### Expand Test Coverage
```kotlin
// Add repository tests
class UserRepositoryTest { /* ... */ }

// Add ViewModel tests  
class MainViewModelTest { /* ... */ }

// Add navigation tests
class NavigationTest { /* ... */ }
```

### Add Code Coverage
```kotlin
// In app/build.gradle.kts
plugins {
    id("jacoco")
}
```

### Add Static Analysis
```kotlin
// Add Detekt for Kotlin
plugins {
    id("io.gitlab.arturbosch.detekt")
}
```

## 📚 Resources

- [Full Testing Guide](TESTING.md)
- [Complete Setup Documentation](CI_CD_SETUP.md)
- [Android Testing](https://developer.android.com/training/testing)
- [GitHub Actions](https://docs.github.com/en/actions)

## 💡 Tips

1. **Run tests before pushing**
   ```bash
   ./gradlew test
   ```

2. **Use descriptive commit messages**
   - CI workflows show commit messages
   - Helps identify what changed

3. **Monitor CI regularly**
   - Check Actions tab after pushing
   - Fix failing tests quickly

4. **Keep tests fast**
   - Unit tests should run in seconds
   - Mock external dependencies

5. **Write tests for new features**
   - Add tests with new code
   - Helps prevent regressions

---

**Setup Status:** ✅ Complete and Ready to Use!

For detailed information, see [TESTING.md](TESTING.md) and [CI_CD_SETUP.md](CI_CD_SETUP.md)

