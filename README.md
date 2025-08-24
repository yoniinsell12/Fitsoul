# FitSoul - AI-Powered Fitness App

FitSoul is a modern fitness application designed to provide personalized workout experiences through AI coaching, comprehensive workout tracking, and social fitness features.

## Project Technologies

<img src="https://skillicons.dev/icons?i=kotlin,firebase,androidstudio,gradle,git&perline=7" />

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit for native Android UI
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **Firebase**: Backend services (Auth, Firestore, Analytics)
- **Material 3**: Design system
- **OkHttp**: Network requests
- **DataStore**: Preferences management
- **Coil**: Image loading
- **Lottie**: Advanced animations

## Installation + Running The Application

Steps:

1. Clone the repository to your local machine:
   ```sh
   git clone https://github.com/yourusername/fitsoul.git
   ```

2. Open folder:
   ```sh
   cd fitsoul
   ```

3. Open the project in Android Studio:
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned repository folder and click "Open"

4. Set up Firebase:
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project with package name `com.fitsoul.app`
   - Download the `google-services.json` file and place it in the app directory
   - Enable Authentication, Firestore, and Analytics in your Firebase project

5. Configure API Keys (Optional - for AI features):
   - Create a file named `local.properties` in the root project directory if it doesn't exist
   - Add the following line with your OpenRouter API key:
     ```
     OPENROUTER_API_KEY=your_api_key_here
     ```

6. Build and run the project:
   - Connect an Android device or use an emulator
   - Click the "Run" button in Android Studio

## Features

### AI Coach
- **Personalized Workout Generation**: AI-powered workout plans tailored to user preferences and fitness levels
- **Offline-First Design**: Local workout templates ensure functionality without internet connection
- **Conversational Interface**: Natural language interaction with the AI coach
- **Quick Actions**: One-tap access to common workout requests

### Workout Management
- **Workout Library**: Pre-built and custom workout routines
- **Progress Tracking**: Monitor completion rates and performance metrics
- **Live Session Tracking**: Real-time workout guidance and tracking

### User Experience
- **Modern Material 3 Design**: Clean, intuitive interface with consistent design language
- **Responsive UI**: Smooth animations and transitions for an engaging experience
- **Social Features**: Friend activity feed to stay connected with fitness community

### Technical Features
- **Firebase Integration**: Authentication, Firestore database, and analytics
- **Location Services**: GPS tracking for outdoor workouts
- **Offline Support**: Full functionality without internet connection
- **Push Notifications**: Workout reminders and friend activity updates

## Development Challenges & Solutions

### Challenge: AI Integration with Limited API Access
**Solution**: Implemented an offline-first approach with pre-built workout templates to prevent API rate limiting issues while still providing personalized workout experiences.

### Challenge: Maintaining Performance with Rich UI
**Solution**: Utilized Jetpack Compose's efficient rendering and lazy loading patterns to ensure smooth scrolling and transitions even with complex UI elements.

### Challenge: Location Tracking Battery Optimization
**Solution**: Implemented foreground services with adaptive polling intervals to balance accurate tracking with battery conservation.

### Challenge: Cross-Device User Experience
**Solution**: Created a responsive design system with consistent components that adapt to different screen sizes and orientations.

### Challenge: Offline Data Synchronization
**Solution**: Built a robust local caching system that syncs with remote database when connectivity is restored.

## Future Enhancements

- Wearable device integration
- Video-based form analysis
- Expanded AI coaching capabilities
- Nutrition tracking and recommendations
- Group workout challenges

## Project Screenshots

*Screenshots will be added here*

---

FitSoul combines cutting-edge AI technology with proven fitness principles to deliver a comprehensive fitness solution that adapts to each user's unique journey.
