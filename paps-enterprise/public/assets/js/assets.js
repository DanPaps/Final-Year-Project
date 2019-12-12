// Variable to store hotel database key globally
var hotelKey = null;
var currentHotel = {};
// Empty array for objects
var resultString = [];

//init document
$(document).ready(function(){

    //logout action 
    $('#logout_room').on('click', (ev) =>{
        ev.preventDefault();
        //window.alert("logging out");
        loggingOut();

    });

    $("#update_assets").on('click', (ev) =>{
        ev.preventDefault();
        uploadAssets();

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
          loadCurrentHotel(uid);
        } else {
            // Navigate back to login page
            window.location = "index.html";          
        }
      });    

    
});

// send data from the text box to the database
  var uploadAssets = function() {
    var result = $('input[type="checkbox"]:checked');

    // looping through the checkboxes in the result collection

    if (result.length > 0) {
        // Empty the list before adding new items
        while (resultString.length > 0) {
            resultString.pop();
        }

        // Add each item to the list
        result.each(function(){
            resultString.push($(this).val());
        });

        const assetsDb = firebase.database().ref(`/GHotelBookMan/Hotels/${hotelKey}/Assets`);
        data = {
            facilities: resultString
        };
        
        assetsDb.set(data).then(() => {
            setError("Updated assets successfully");
            loadCurrentHotel();
        }).catch((error) => {
            setError(error.message);
        });

    }else{
        $('#container_id').html("No Checkbox selected");
    }
}



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

        // Array of checkboxes
        var checkboxes = [];
        var carPack = $('#check_car_park');
        var pool = $('#check_pool');
        var restaurant = $('#check_restaurant');
        var airCon = $('#check_aircondition');
        var massage = $('#check_massage_room');
        var gym = $('#check_gym');
        var saloon = $('#check_saloon_haircut');
        var elevators = $('#check_elevators');
        var gaming = $('#check_gaming_room');
        var heliPad = $('#check_heli_pad');
        var movieHall = $('#check_movie_hall');

        // Append all ids to the checkboxes
        checkboxes.push(carPack,pool,restaurant,airCon,massage,gym,saloon,elevators,gaming,heliPad,movieHall);

        // Store all facilities in the result array
        resultString = currentHotel.Assets.facilities;     
        resultString.forEach((item,index,arr) => {
           checkboxes.forEach((val,i,list) => {
                if(checkboxes[i].val() == item){
                   checkboxes[i].prop('checked',true); 
                }
           });
        });

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