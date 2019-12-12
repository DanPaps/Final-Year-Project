// Variable to store hotel database key globally
var hotelKey = null;
var currentHotel = {};

//init document

$(document).ready(function(){

    //logout action 
    $('#logout_room').on('click', (ev) =>{
        ev.preventDefault();
        //window.alert("logging out");
        loggingOut();

    });

    $('#nav_hotel_room').on('click', (ev) =>{
        ev.preventDefault();
       // window.alert("taking you to the profile");
         window.location = "hotel.html"

    });

    

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
          // ...
            // Load hotel data from database
          loadCurrentHotel(uid);
           
          //console.log(this.displayName);

        } else {
            // Navigate back to login page
            window.location = "index.html";          
        }
      });    

    //loadReservations();
      
    
});

 //load hotel data from database
 var loadCurrentHotel = function () {
    const ref = firebase.database().ref(`/GHotelBookMan/Hotels/${hotelKey}`);
    ref.once('value').then((snapshot) => {
       //
       if (snapshot.val()) {
           currentHotel = snapshot.val();
           console.log("hotel is => ", currentHotel);
           // Set hotel name at the navbar
        $('#hotel_title_bar_name').text(currentHotel.hotelName);
       }
    }).catch((error) => {
        setError(error.message);
    })

};

// sign out
var loggingOut = function () {
    firebase.auth().signOut().then(function() {
            // Sign-out successful.
            window.location = "reservations.html"; 
          }).catch(function(error) {
            // An error happened.
            setError(error.message)
          });
        
    };
    
    var setError = function(message){
        $('#error_content').text(message);
        $('#authModal').modal('show');
        
    };
    
    
    //search for 
    var searchFor = function () {
        var query = $('#search_field');
        setError(query.val());  
    };