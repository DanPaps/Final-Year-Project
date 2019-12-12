//init document
$(document).ready(function(){
//variables
$('#reg_btn').on('click', function(ev){
        ev.preventDefault();
        signUp();
    });
$('#lg_btn').on('click', function(ev){
        ev.preventDefault();
        login();
    });
});



// sign up user with user and password      
var signUp =function () {
    var hotel_email = $('#reg_email');
    var hotel_password = $('#reg_password');
    var hotel_wallet_number = $('#reg_wallet');
    var hotel_name = $('#reg_name');
   

    firebase.auth().createUserWithEmailAndPassword(hotel_email.val(),hotel_password.val())
    .then(function(result){
        // Create object to upload to database
        var data = {
            hotelDetails:'',
            hotelName:hotel_name.val(),
            hotelPic:'',
            hotelEmail:hotel_email.val(),
            hotelWallet:hotel_wallet_number.val(),
            hotelRating:3,
            latitude: 0.00,
            longitude:0.00,
            key: firebase.auth().currentUser.uid
        };

        //push to db
        if(pushDb(data)){
            return; 
        }
        

    }).catch(function(error){
          // Handle Errors here.
          var errorCode = error.code;
          var errorMessage = error.message;
          // Display error
        setError(errorMessage);
    });
    
};


// login user with user and password
var login =function () {
    var email = $('#lg_email');
    var password = $('#lg_password');

    firebase.auth().signInWithEmailAndPassword(email.val(),password.val())
    .then(function(result){
      window.location = "reservations.html";
    
    }).catch(function(error){
         // Handle Errors here.
         var errorCode = error.code;
         var errorMessage = error.message;
         // Display error
        setError(errorMessage);
    });
   
};

 function pushDb(data) {
      // Upload to database
      firebase.database().ref(`/GHotelBookMan/Hotels/${data.key}`).set(data).then(function(result){
        // Navigate to dashboard
     window.location = "hotel.html";
    }).catch(function(error){
        // Handle Errors here.
        var errorCode = error.code;
        var errorMessage = error.message;
        // Display error
        setError(errorMessage);
    });    
};

var setError = function(message){
    $('#error_content').text(message);
    $('#authModal').modal('show');
    
}


