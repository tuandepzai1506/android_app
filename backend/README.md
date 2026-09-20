#Cau truc Firestore
│
├── users
│   └── <Firebase Auth UID>
│       ├── username
│       ├── fullName
│       ├── email
│       ├── role
│       ├── preferences
│       └── createdAt
│
├── places
│   └── <Google Place ID>
│       ├── placeId
│       ├── name
│       ├── category
│       ├── address
│       ├── latitude
│       ├── longitude
│       ├── priceLevel
│       ├── rating
│       ├── reviewCount
│       ├── googleMapsUri
│       └── websiteUri
│
└── favorite_places
    └── <Auth UID>_<Place ID>
        ├── userId
        ├── placeId
        └── createdAt

#Vi tri luu key Firebase
android_app/firebase/serviceAccountKey.json