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
           getAllRooms();
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

    // Add picture
    var addPicture = function () {
    };
    
    // get rooms from database
    var getAllRooms = function () {
       if (hotelKey == null) {
           setError("Hotel cannnot be retrieved from the database");
       } else {
            // Database reference
        const db = firebase.database().ref(`/GHotelBookMan/Hotels/${hotelKey}/Rooms`);

        var rowSingle = $('#single_room_content');
        var rowDouble = $('#double_room_content');
        var rowEconomy = $('#economy_room_content');
        
        // make call to database
        db.on("value", function(snapshot) { 
            console.log(snapshot.val()); 
            snapshot.forEach(function(childSnapshot) {
                var roomKey = childSnapshot.key;
                var roomData = childSnapshot.val();
    
                //load data into cards
                if (roomData.type == "Single") {
                    rowSingle.append(
                        `<div class="col-lg-3 col-md-4 col-sm-8">
                        <div class="card card-stats">
                            <div class="card-content">
                                <p class="tim-note"><b>Room Number: </b>${roomData.roomNumber} </p>
                                <p class="tim-note"><b>Floor Number: </b>${roomData.floorNumber} </p>
                                <p class="tim-note"><b>Price: </b>GHC ${roomData.price} </p>
                            </div>
                            <div class="card-footer text-center">
                                <p class="tim-note"><b>Availability: </b>${roomData.availability} </p>
                            </div>
                        </div>
                    </div>`
                    );
                }else if (roomData.type == "Double") {
                    rowDouble.append(
                        `<div class="col-lg-3 col-md-4 col-sm-8">
                            <div class="card card-stats">
                                <div class="card-content">
                                    <p class="tim-note"><b>Room Number: </b>${roomData.roomNumber} </p>
                                    <p class="tim-note"><b>Floor Number: </b>${roomData.floorNumber} </p>
                                    <p class="tim-note"><b>Price: </b>GHC ${roomData.price} </p>
                                </div>
                                <div class="card-footer text-center">
                                    <p class="tim-note"><b>Availability: </b>${roomData.availability} </p>
                                </div>
                            </div>
                        </div>`
                    );
                }else{
                    rowEconomy.append(
                        `<div class="col-lg-3 col-md-4 col-sm-8">
                        <div class="card card-stats">
                            <div class="card-content">
                                <p class="tim-note"><b>Room Number: </b>${roomData.roomNumber} </p>
                                <p class="tim-note"><b>Floor Number: </b>${roomData.floorNumber} </p>
                                <p class="tim-note"><b>Price: </b>GHC ${roomData.price} </p>
                            </div>
                            <div class="card-footer text-center">
                                <p class="tim-note"><b>Availability: </b>${roomData.availability} </p>
                            </div>
                        </div>
                    </div>`
                    );
                }
            });
        });
       }
    };