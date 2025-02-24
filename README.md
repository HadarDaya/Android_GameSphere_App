# 🎮 Android GameSphere App

### 📌 Overview

**GameSphere** is a mobile application designed for exploring, rating, and managing video games.  
The app consists of two user roles: **regular users** and **administrator**, each with unique capabilities:

- **Unregistered Users**:
  - Browse an extensive collection of games, including detailed descriptions, images, and trailers.(cannot rate games or add them to favorites).

- **Registered Users**:
  - Browse an extensive collection of games, including detailed descriptions, images, and trailers.
  - Rate games and add them to their favorites list.
  - Edit personal details such as username, email, and password.

- **Administrator**:
  - Enjoy all the privileges of regular users (excluding personal detail edits).
  - Add new games to the app, including image and video trailer uploads, and detailed game information.

The application is built using **Android SDK** and integrates **Firebase** for user authentication and database management.  
**Cloudinary** is utilized for optimized cloud-based image handling, ensuring secure and efficient image storage.

## 🚀 Features

- **Game Exploration 🎮**:  
  View detailed game pages with comprehensive descriptions, high-quality trailers, and captivating images.
- **User Ratings & Favorites ⭐**:  
  Rate games and add them to a personal favorites list.
- **Game Management (Admin Only ⚙️)**:  
  Add new games, including image uploads and Firebase data storage.
- **Cloud Storage ☁️🖼️**:  
  Securely upload and store images using Cloudinary's optimized cloud storage.
- **User Authentication 🔐**:  
  Secure login and registration using Firebase Authentication.
- **Custom UI Components 🎨**:  
  The app provides a seamless user experience with custom dialogs, Lottie animations, and custom-designed AlertDialogs.
- **Filtering & Sorting 🔍**:  
  Users can easily search, filter, and sort games based on preferences.
- **Real-Time Updates ⏱️**:  
  Firebase ensures data consistency across all users in real time.
- **Modular Code Design ⚙️**:  
  The app is designed with modular adapters and helpers to manage data and UI updates efficiently.

## 💻 Technologies Used

🔹 **Programming Language 🖥️**: Java  
🔹 **Database 🗃️**: Firebase Realtime Database  
🔹 **Authentication 🔐**: Firebase Authentication  
🔹 **Cloud Storage ☁️🖼️**: Cloudinary (for storing game images)  
🔹 **UI Components 📱**: RecyclerView, ImageView, AlertDialog  
🔹 **Development Tools 🛠️**: Android Studio  

## 🔧 Installation

1️⃣ Clone the repository:  
`git clone https://github.com/HadarDaya/Android_GameSphere_App.git`

2️⃣ Open the project in **Android Studio**.

3️⃣ Set up **Firebase**:  
   - Add `google-services.json` in the app directory.  
   - Enable **Firebase Authentication** and **Realtime Database**.

4️⃣ Configure **Cloudinary**:  
   - Set up a Cloudinary account and update the image upload functionality with your credentials.

5️⃣ Build and run the application on an **emulator** or a **physical device**.



## 📖 Usage

📝 **Register** or **login** using Firebase Authentication.  
🎮 Browse the game list, view details, **and add new games** (admin only).  
📸 Upload images to **Cloudinary** and store game details in **Firebase**.  
⭐ Rate games and check **community ratings**.  
🔍 Search, filter, and sort game collections.

## Screenshots and Descriptions of the App: 📱 ✨ 📸:

#### 1. **Home Screen 🏠**
This is the main page where users can explore all the available games.

<img src="https://github.com/user-attachments/assets/4dcda2e6-2fac-497b-96d0-b73a2fc646f6" alt="Home Screen" style="width: 48%;"/>

---

#### 2. **Login Screen 🔑**
The screen where users log into the app using a username and password using Firebase Authentication.

<img src="https://github.com/user-attachments/assets/4eb2d06d-a55d-493a-b798-120d30b6eb5c" alt="Login Screen" style="width: 48%;"/>

---

#### 3. **Registration Screen ✍️**
New users can create an account using Firebase Authentication, by providing necessary details.

<img src="https://github.com/user-attachments/assets/1d5d8edb-0306-4724-856a-d4c3f65705ee" alt="Registration Screen" style="width: 48%;"/>

---

#### 4. **Game Details Page 📋**
Users can view game details, including description, images, and trailers.

<img src="https://github.com/user-attachments/assets/353478c5-1312-487e-844b-d67adb98dcc4" alt="Game Details" style="width: 48%;"/>

---

#### 5. **Profile Screen 👤⚙️**
This is the profile page, where users can view and edit their personal details.  
Admins and regular users both have access to this screen.

<div style="display: flex; justify-content: space-between;">
  <img src="https://github.com/user-attachments/assets/130d9b3c-fc07-4969-8321-dd3867347420" alt="Profile Screen admin" style="width: 48%;"/>
  <img src="https://github.com/user-attachments/assets/f6fef5ff-5c75-40bd-8eb1-48004a15db26" alt="Profile Screen user" style="width: 48%;"/>
</div>

---

#### 6. **Rating and Favorites ⭐**
Registered users can rate games and add them to their favorites list.

<img src="https://github.com/user-attachments/assets/a7a9404b-03c2-41f8-a522-620f37f2474b" alt="Rating Screen" style="width: 60%;"/>

---
**🎉 Enjoy using GameSphere and have fun exploring games! 🎮🔥**











