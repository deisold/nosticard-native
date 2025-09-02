# NostiCard MVP

A cross-platform React Native mobile app that transforms photos into nostalgic postcards with vintage filters, frames, and personal greetings.

## 🎯 Project Overview

**NostiCard** bridges generations with simple, warm, personal postcards — like in the old times, but in a modern way. Perfect for adult children & grandchildren sending greetings to grandparents, and anyone who enjoys retro-style digital messages.

## 📋 Implementation Status

**Current Status**: 🚧 **MVP Foundation Complete** (60% implemented)

- ✅ **Architecture & Foundation**: Complete MVVM setup
- ✅ **Core Screens**: Home, Photo Picker, Editor implemented  
- ⚠️ **Missing Screens**: Preview, My Cards, Settings, Onboarding, Upgrade
- ⚠️ **Image Processing**: Skia filter pipeline (stub implementation)
- ✅ **State Management**: Zustand stores fully implemented
- ✅ **Service Layer**: All services with interface contracts
- ⚠️ **Testing**: Service layer ready, UI tests needed

See [IMPLEMENTATION.md](./IMPLEMENTATION.md) for detailed feature checklist.

## ✨ Features (MVP)

### Core Functionality
- **Photo Selection**: Gallery picker or camera capture
- **Nostalgic Filters**: Classic B&W, Sepia Memories, Faded Color
- **Vintage Effects**: Randomized scratches, dust, adjustable grain & vignette
- **Frames**: White border, deckle edge, stamp edge options
- **Personal Text**: Draggable text with handwriting fonts (220 char limit)
- **Export & Share**: Native sharing with Mail, SMS, WhatsApp, etc.

### Storage & Management
- Local storage with MMKV + File System
- Auto-save with 1.5s debounce
- Draft and completed card states
- 200 card soft limit with cleanup prompts

### Monetization
- **Free**: Basic features with watermark, 1080px export
- **Pro ($9.99)**: Remove watermark, HD export (1800px), PDF export, all frames
- **Style Packs ($0.99-$2.99)**: Additional presets, overlays, frames

## 🏗 Architecture

### MVVM Pattern
- **Views**: React Native components (screens/components)
- **ViewModels**: Business logic with EventEmitter pattern
- **Services**: Data persistence, image processing, IAP, sharing

### Technology Stack
- **Framework**: React Native (plain RN, no Expo)
- **Language**: TypeScript with strict configuration
- **State Management**: Zustand stores
- **Image Processing**: @shopify/react-native-skia
- **Storage**: react-native-mmkv + react-native-fs
- **Navigation**: @react-navigation/native + stack
- **IAP**: react-native-iap
- **Sharing**: react-native-share

### Dependency Injection
- Service Provider pattern with React Context
- Interface-based service contracts
- Testable architecture with service mocks

## 📁 Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── common/         # Button, Card, EmptyState
│   └── cards/          # Card-specific components
├── screens/            # Screen components
│   ├── HomeScreen.tsx
│   ├── PhotoPickerScreen.tsx
│   └── EditorScreen.tsx
├── viewmodels/         # Business logic layer
│   ├── BaseViewModel.ts
│   ├── HomeViewModel.ts
│   └── EditorViewModel.ts
├── services/           # Data and external services
│   ├── interfaces.ts
│   ├── ImageService.ts
│   ├── StorageService.ts
│   ├── IAPService.ts
│   └── ShareService.ts
├── stores/             # Zustand state management
│   ├── appStore.ts
│   └── editorStore.ts
├── types/              # TypeScript definitions
│   ├── index.ts
│   ├── errors.ts
│   └── events.ts
├── utils/              # Utilities and providers
│   ├── ServiceProvider.tsx
│   └── constants.ts
├── theme/              # Design system
└── navigation/         # Navigation configuration
```

## 🚀 Getting Started

### Prerequisites
- **Node.js 18+** (React Native 0.76.8 compatible)
- **Java 17-21** (Java 24+ not supported by React Native build tools)
- React Native development environment
- iOS/Android development tools
- Android SDK API 35

### Installation

1. **Install dependencies:**
```bash
npm install
```

2. **Set up environment:**
```bash
cp .env.example .env
# Configure your IAP product IDs and settings
```

3. **iOS Setup:**
```bash
cd ios && pod install && cd ..
```

4. **Run the app:**
```bash
# iOS
npm run ios

# Android
npm run android
```

### 🔄 Version Information

**Current React Native Version**: `0.76.8` (Stable)

**Key Updates in RN 0.76.8:**
- Stable New Architecture support  
- React 18.3.1 compatibility
- Enhanced debugging and dev tools
- Performance improvements
- Better TypeScript integration

**Dependencies:**
- React 18.3.1 (stable concurrent features)
- Zustand v5.0.0 (latest state management)
- React Navigation v6.1.18 (RN 0.76 compatible)
- Skia v1.5.0 (enhanced graphics performance)

### Development Commands

```bash
npm run lint          # ESLint
npm run typecheck     # TypeScript checking
npm start            # Metro bundler
npm test             # Jest tests
```

## 🎨 Design System

### Color Palette (Vintage Theme)
- **Primary**: #8B4513 (Saddle Brown)
- **Secondary**: #D2B48C (Tan)
- **Background**: #FDF6E3 (Cream)
- **Text**: #2F1B14 (Dark Brown)
- **Accent**: #B22222 (Fire Brick)

### Typography
- **Headers**: Georgia (serif)
- **Body**: System font (sans-serif)  
- **Handwriting**: Dancing Script, Great Vibes
- **Typewriter**: Courier New

## 🔧 Configuration

### Environment Variables (.env)
- IAP product IDs
- Export quality settings
- Storage paths
- Feature flags

### Key Constants
- Max cards: 200
- Text limit: 220 characters
- Auto-save debounce: 1.5 seconds
- Export sizes: 1080px (free), 1800px (pro)

## 🧪 Testing Strategy

### Service Layer Testing
- Interface-based mocking
- Unit tests for ViewModels
- Storage service testing

### UI Testing
- Component testing with React Native Testing Library
- Navigation flow testing
- Error state handling

## 📱 Platform Support

### iOS
- iOS 12.0+
- Camera/photo library permissions
- In-app purchases
- Native sharing

### Android
- Android API 21+
- Storage permissions
- Camera permissions
- Google Play billing

## 🚀 Deployment

### Pre-deployment Checklist
- [ ] Configure IAP products in App Store/Play Console
- [ ] Set up analytics (optional)
- [ ] Test restore purchases flow
- [ ] Verify export functionality
- [ ] Test sharing across platforms

### Build Commands
```bash
# iOS Release
npx react-native run-ios --configuration Release

# Android Release
npx react-native run-android --variant=release
```

## 🚀 **Next Steps for MVP Completion**

### **Immediate Priority** (Critical for launch)
1. **Complete Skia filter implementation** - Core image processing
2. **Build PreviewScreen** - Export and sharing functionality  
3. **Implement MyCardsScreen** - Card management interface
4. **Add SettingsScreen** - User preferences and IAP restore

### **Implementation Roadmap**
- **Phase 1**: Complete missing screens (PreviewScreen, MyCardsScreen, SettingsScreen)
- **Phase 2**: Finish image processing pipeline with Skia
- **Phase 3**: Build UpgradeScreen for monetization
- **Phase 4**: Add OnboardingScreen for user experience
- **Phase 5**: Platform setup and app store readiness

See [IMPLEMENTATION.md](./IMPLEMENTATION.md) for detailed feature checklist and completion status.

## 🔮 Future Enhancements (Post-MVP)

### Planned Features
- Additional filter packs and vintage effects
- Cloud sync for cross-device access
- Social sharing features
- Print service integration
- Advanced text editing with more fonts

### Technical Improvements
- Comprehensive testing suite
- Performance optimizations
- Accessibility features
- Error boundaries and crash reporting
- Image caching strategy

## 📄 License

Private project - All rights reserved.

## 🤝 Contributing

This is a private MVP. Development guidelines:
- Follow MVVM architecture
- Use TypeScript strictly
- Write tests for business logic
- Follow design system
- Keep services interface-based

---

**NostiCard** - *Turn your photos into nostalgic postcards* ✉️