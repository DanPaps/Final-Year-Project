// Global variable to store the current loaded hotel
var currentHotel = null;

// Default location
var position = null;

// profile image content
var image = '';

$(document).ready(function() {
    //check user login state
    firebase.auth().onAuthStateChanged(function(user) {
        if (user) {
          // User is signed in.
          var displayName = user.displayName;
          var email = user.email;
          var emailVerified = user.emailVerified;
          var photoURL = user.photoURL;
          var isAnonymous = user.isAnonymous;
          var uid = user.uid;
          var providerData = user.providerData;
         // Load hotel data from database
          loadCurrentHotel(uid);
        } else {
            // Navigate back to login page
            window.location = "index.html";          
        }
      });    
      
    // Get user's locaiton when page is loaded
    getLocation();


    $('#update_profile').on('click', (ev) =>{
        ev.preventDefault();
        updateProfile();
    });

});

// //load hotel data from database
var loadCurrentHotel = function (uid) {
    const ref = firebase.database().ref('/GHotelBookMan/Hotels');
    
// //Get hotels
ref.once('value', function(snapshot) {
    snapshot.forEach(function(childSnapshot) {
      var childKey = childSnapshot.key;
      var childData = childSnapshot.val();
      
      this.currentHotel = childData;

      if (uid == childKey) {
        // Load hotel reservations  
        setupProfile();
        
        // Set hotel name at the navbar
        $('#hotel_title_bar_name').text(childData.hotelName);

      }

    });
  });
};

// Set hotel profile details 
var setupProfile = function () {
    // Profile row
    var imageSource = $('#profile_hotel_img');
    var name = $('#profile_hotel_name');
    var ratings = $('#profile_hotel_ratings');
    var details = $('#profile_hotel_details');

    // Input row
    var input_name = $('#name_field');
    var input_details = $('#about_field');
    var input_ratings = $('#ratings_field');

    if (currentHotel) {
        // Input row details
        input_name.val(currentHotel.hotelName);
        if (currentHotel.hotelDetails != '') {
            input_details.val(currentHotel.hotelDetails);
        }
        input_ratings.val(currentHotel.hotelRating);


        // Profile row details
        name.text(currentHotel.hotelName);
        ratings.text(`Ratings: ${currentHotel.hotelRating} stars`);
        if (currentHotel.hotelPic != null && currentHotel.hotelPic != '') {
            imageSource.attr('src',currentHotel.hotelPic);
        }
        details.text(currentHotel.hotelDetails);
    } else {
        setError("Hotel data could not be retrieved from the database");
    }

};

// Checks the validity of the ratings field
var checkContent = function () {
  var ratings = $('#ratings_field');
  if (ratings.val() > 5 || ratings.val() < 1) {
      return setError("Invalid number entered for ratings. please add a number between 1 and 5");
  }else{
      return true;
  }
};

// upload profile image
var uploadProfile = function () {
    var input = document.getElementById('inputfile');
    
    // Get file
    var currentFile = input.files.item(0);

    console.log(currentFile);

    // Create file metadata including the content type
    var metadata = {
        contentType: currentFile.type
    };

    // Log file name and metadata to console for debugging
    console.log(currentFile.name, " => ", metadata);

    // Create storage reference
    var ref = firebase.storage().ref('/GHotelBookMan/Hotels').child(`${currentFile.name}`);

    // Upload file and get task
    var task = ref.put(currentFile, metadata);

    // Monitor task for progress
    task.on('state_changed',
        // Shows progress of task
        function progress(snapshot) {
            var percentage = (snapshot.bytesTransferred / snapshot.totalBytes) * 100;
            console.log(percentage);
            
        },
        // Shows any errors occurring during progress
        function error(err) {
            // Handle unsuccessful uploads
            setError(err.message);
        },
        // Shows when task is completed
        function complete() {
            setError("Image Upload was successful");
            // Handle successful uploads on complete
            image = task.snapshot.downloadURL;
            console.log(image);

            // Push data to database
            var about = $('#about_field');
            var ratings = $('#ratings_field');
            var location = $('#location_field');
            var name = $('#name_field');

            // Data model to be uploaded to the database
            var data = {
                hotelName: name.val(),
                hotelDetails: about.val(),
                hotelRating: ratings.val(),
                hotelLocation: location.val(),
                latitude: position.lat,
                longitude: position.lng
            };

            if (image != '') {
                data['hotelPic'] = image;
            }
            
            if (checkContent()) {
                // Upload to database reference
                var db = firebase.database().ref(`/GHotelBookMan/Hotels/${firebase.auth().currentUser.uid}`);
                db.update(data).then(() => {
                    setError("Uploaded successfully");
                    window.location = "reservations.html";
                });
            }else{
                setError(checkContent());
            }
        });
};

var setError = function(message){
    $('#error_content').text(message);
    $('#authModal').modal('show');
    
};

// Create a new google map object
var getMap = function(center) {
    return new google.maps.Map(document.getElementById('map'), {
        zoom: 14,
        center: center
    });
};

var showHotel = function(location) {
    // Get map by id
    var map = $('#map');

};

// get user's current location
var getLocation = function() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(geoSuccess, geoError);
    }
};

// Show user's location on map
var geoSuccess = function(position) {
    var lat = position.coords.latitude;
    var lng = position.coords.longitude;
    plotPosition(lat, lng);
};

// Show error to user
var geoError = function() {
    setLocationText("Geocoder failed.");
};

// Plot user's location on the map
var plotPosition = function(newLat, newLng) {
    position = {
        lat: newLat,
        lng: newLng
    }
    var marker = new google.maps.Marker({
        position: position,
        map: getMap(position)
    });

    var latlng = new google.maps.LatLng(newLat, newLng);

   var geocoder = new google.maps.Geocoder();

    geocoder.geocode({'latLng': latlng}, function(results, status) {
      if(status == google.maps.GeocoderStatus.OK) {
          console.log(results)
          if(results[1]) {
              //formatted address
              var address = results[0].formatted_address;
              setLocationText(address);
          } else {
            setLocationText("No results found");
          }
      } else {
        setLocationText("Geocoder failed due to: " + status);
      }
    });

};



var updateProfile = function () {
    var imageName = $('#selected_picture_name');
    if (imageName.text() != '') {
        uploadProfile();
    } else {
        var about = $('#about_field');
        var ratings = $('#ratings_field');
        var location = $('#location_field');
        var name = $('#name_field');

        // Data model to be uploaded to the database
        var data = {
            hotelName: name.val(),
            hotelDetails: about.val(),
            hotelRating: ratings.val(),
            hotelLocation: location.val(),
            latitude: position.lat,
            longitude: position.lng
        };

        if (image != '') {
            data['hotelPic'] = image;
        }
        
        if (checkContent()) {
            // Upload to database reference
            var db = firebase.database().ref(`/GHotelBookMan/Hotels/${firebase.auth().currentUser.uid}`);
            db.update(data).then(() => {
                setError("Uploaded successfully");
                window.location = "reservations.html";
            });
        }else{
            setError(checkContent());
        }
    }
    
};

/**
       * @constructor
       */
      var ClickEventHandler = function(map, origin) {
        var origin = origin;
        var map = map;
        var directionsService = new google.maps.DirectionsService;
        var directionsDisplay = new google.maps.DirectionsRenderer;
        directionsDisplay.setMap(map);
        var placesService = new google.maps.places.PlacesService(map);
        var infowindow = new google.maps.InfoWindow;
        var infowindowContent = document.getElementById('infowindow-content');
        infowindow.setContent(this.infowindowContent);

        // Listen for clicks on the map.
        this.map.addListener('click', this.handleClick.bind(this));
      };

      ClickEventHandler.prototype.handleClick = function(event) {
        console.log('You clicked on: ' + event.latLng);
        // If the event has a placeId, use it.
        if (event.placeId) {
          console.log('You clicked on place:' + event.placeId);

          // Calling e.stop() on the event prevents the default info window from
          // showing.
          // If you call stop here when there is no placeId you will prevent some
          // other map click event handlers from receiving the event.
          event.stop();
          this.calculateAndDisplayRoute(event.placeId);
          this.getPlaceInformation(event.placeId);
        }
      };

      ClickEventHandler.prototype.calculateAndDisplayRoute = function(placeId) {
        var me = this;
        this.directionsService.route({
          origin: this.origin,
          destination: {placeId: placeId},
          travelMode: 'WALKING'
        }, function(response, status) {
          if (status === 'OK') {
            me.directionsDisplay.setDirections(response);
          } else {
            window.alert('Directions request failed due to ' + status);
          }
        });
      };

      ClickEventHandler.prototype.getPlaceInformation = function(placeId) {
        var me = this;
        this.placesService.getDetails({placeId: placeId}, function(place, status) {
          if (status === 'OK') {
            me.infowindow.close();
            me.infowindow.setPosition(place.geometry.location);
            me.infowindowContent.children['place-icon'].src = place.icon;
            me.infowindowContent.children['place-name'].textContent = place.name;
            me.infowindowContent.children['place-id'].textContent = place.place_id;
            me.infowindowContent.children['place-address'].textContent =
                place.formatted_address;
            me.infowindow.open(me.map);
          }
        });
      };

//set users location with the current location
var setLocationText = function (message) {
    $('#location_field').val(message);
}