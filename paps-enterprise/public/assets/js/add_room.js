
// Variable to store hotel database key globally
var hotelKey = null;
var currentHotel = {};

$(document).ready(function () {
    
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
          hotelKey = uid;
        // Load hotel data from database
          loadCurrentHotel();
        

        } else {
            // Navigate back to login page
            window.location = "index.html";          
        }
      });    

});

 //load hotel data from database
 var loadCurrentHotel = function () {
    const ref = firebase.database().ref(`/GHotelBookMan/Hotels/${hotelKey}`);
    ref.once('value').then((snapshot) => {
       //
       if (snapshot.val()) {
           currentHotel = snapshot.val();
           console.log("hotel is => ", currentHotel);
       }
    }).catch((error) => {
        setError(error.message);
    })
 
};

var addRoom = function () {
     // get all fields
     var room = $('#room_number');
     var floor = $('#floor_number');
     var price = $('#room_price');
     var type = $('#room_type');


     // Database reference for room
     if (hotelKey == null) {
         setError("Cannot retrieve this hotel's details from our database");
     } else {
        var ref = firebase.database().ref(`/GHotelBookMan/Hotels/${hotelKey}/Rooms`).push();
         // Data model for uploading room to database
     var data = {
            roomNumber: room.val(),
            floorNumber: floor.val(),
            price: price.val(),
            type: type.val(),
            availability: true,
            key: ref.getKey()
        };
        ref.set(data).then((result) => {
            setError("data upload successfully");
            window.location = "rooms.html";
        }).catch((error) => {
            setError(error.message);
        });
     }
};

var setError = function(message){
    $('#error_content').text(message);
    $('#authModal').modal('show');
    
};