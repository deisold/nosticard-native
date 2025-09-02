# NostiCard Setup Instructions

## Prerequisites

1. **Node.js 20.19.4+** - Required for React Native 0.81.0
2. **Java 17+** - Required for Android builds
3. **Android Studio** - For Android development
4. **Xcode** - For iOS development (macOS only)

## Initial Setup

### 1. Install Dependencies
```bash
npm install
```

### 2. Download Gradle Wrapper (First Time Only)
The Gradle wrapper JAR needs to be downloaded:
```bash
cd android
./gradlew wrapper --gradle-version 8.10.2
cd ..
```

### 3. iOS Setup (macOS only)
```bash
cd ios && pod install && cd ..
```

### 4. Environment Configuration
```bash
cp .env.example .env
# Edit .env with your IAP product IDs and configuration
```

## Running the App

### Android
```bash
# Start Metro bundler
npm start

# In another terminal
npx react-native run-android
```

### iOS
```bash
# Start Metro bundler  
npm start

# In another terminal
npx react-native run-ios
```

## Troubleshooting

### Node.js Version Issues
If you see Node.js version warnings, upgrade to Node.js 20.19.4+:
```bash
# Using nvm (recommended)
nvm install 20.19.4
nvm use 20.19.4

# Or download from nodejs.org
```

### Android Build Issues
1. Ensure JAVA_HOME is set to Java 17+
2. Make sure Android SDK is properly configured
3. Clear gradle cache if needed:
```bash
cd android && ./gradlew clean && cd ..
```

### Metro Cache Issues
```bash
npx react-native start --reset-cache
```

## Development Workflow

1. **Start Metro**: `npm start`
2. **Run on device**: `npx react-native run-android` or `npx react-native run-ios`
3. **Type checking**: `npm run typecheck`
4. **Linting**: `npm run lint`
5. **Testing**: `npm test`

## Next Steps

After successful setup, see [IMPLEMENTATION.md](./IMPLEMENTATION.md) for development roadmap and feature implementation status.