# MemoryPlaces
Memory Places is a Kotlin Application in which you can save your memories using a title, a description, a date, a location and an image.

Consists from:
- A **MainActivity** with the recycler view of saved items in an SQL database. For each item there are implemented the below methods:
1. SwipeToEditCallback: By swiping right you are able to edit the existing item and all of its parameters.
2. SwipeToDeleteCallback: By swiping left you are able to delete the existing item from the recycler view and the SQL database.
3. OnClickListener: By clicking the item you can view the memory place object details.

- An **AddMemoryPlace** activity in order to insert all the requested information in order to save the memory place object.
Notes:
1. Regarding the location, you can manually enter the location or use your GPS signal in order to pass the current location by using the class GetAddressFromLatLng.
2. Permissions for access in storage, camera usage and GPS Provider are required for the first time a memory place object is created.

- A **MemoryPlaceDetails** activity for viewing all the details of the selected item as well as the ability to view the location you have entered in google maps from MapActivity activity.

***Please take into consideration that in order to use the location functionality you have to create your own Map API Key from https://console.cloud.google.com/***



![memory1](https://github.com/ThanosArab/MemoryPlaces/assets/75016979/9eb3d220-f60f-43f7-b016-19a4b37a52fb) ![memory2](https://github.com/ThanosArab/MemoryPlaces/assets/75016979/74fcead7-cff4-4228-95c8-a22901df77d5)
![memory3](https://github.com/ThanosArab/MemoryPlaces/assets/75016979/9b489026-6679-4e9d-b398-68fc5626938e) ![memory4](https://github.com/ThanosArab/MemoryPlaces/assets/75016979/48a4dd27-01c3-4c06-862d-be9e4384567e)


## Dependencies

    implementation 'androidx.core:core-ktx:1.10.1'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'

    implementation 'de.hdodenhof:circleimageview:3.0.1'

    implementation 'com.google.android.gms:play-services-maps:18.1.0'
    implementation 'com.google.android.libraries.places:places:3.2.0'
    implementation 'com.google.android.gms:play-services-location:21.0.1'

    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.1'


## Launch
Download the zip file via Github and insert your own Map API Key in build.gradle file and strings.xml.
