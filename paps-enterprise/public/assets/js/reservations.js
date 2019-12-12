//init document
$(document).ready(function(){

    //logout action 
    $('#logout').on('click', (ev) =>{
        ev.preventDefault();
        loggingOut();

    });

    $('#nav_hotel').on('click', (ev) =>{
        ev.preventDefault();
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
         // Load hotel data from database
          loadCurrentHotel(uid);
        } else {
            // Navigate back to login page
            window.location = "index.html";          
        }
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
      
      if (uid == childKey) {
        // Load hotel reservations  
        loadReservations();

        // Set hotel name at the navbar
        $('#hotel_title_bar_name').text(childData.hotelName);

      }

    });
  });
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

// data stucture of reservations 
var data = {
    username:"Dennis",
    profile:"",
    status:true, // true for reserved and false is revocked
    timestamp:new Date().getMilliseconds(),
    roomType:"Economy",
    paid:false,
    pin:"654656546fc6drfcvr5dmrlv554"
};

var addReservations = function () {
    firebase.database().ref('/GHotelbookMan/Reservations').push().set(data).then((res) =>{
        
    }).catch((error) =>{
        setError(error.message)
    })
};

//load reservations
var loadReservations = function () {
    const ref = firebase.database().ref('/GHotelBookMan/Reservations/OyXHvi8zqgP8L8u8M55ECKQVpLa2');

    var row = $('#reservationsRow');

        ref.on("value", function(snapshot) { 
        // console.log(snapshot.val()); 
        snapshot.forEach(function(childSnapshot) {
            var childKey = childSnapshot.key;
            var childData = childSnapshot.val();

            // ref.child(`${childData.val()}`)      

             //load data into cards
             row.append(
                `<div class="col-lg-3 col-md-4 col-sm-8">
                <div class="card card-stats">
                    <div class="card-header" data-background-color="primary">
                        <img src="${childData.user.picture}" alt="Loading image">
                    </div>
                    <br>
                    <div class="card-content">
                        <p class="category" style ="margin-top: 10%;"><b>Username:</b> ${childData.user.username}</p>
                        <!--<p class="category"><b>Status:</b> ${childData.status}</p>-->
                        <!--<p class="category"><b>Duration:</b> ${childData.status}</p>-->
                        <p class="category"><b>Type:</b> ${childData.room.type}</p>
                        <p class="category"><i class="material-icons text-primary">attach_money</i> 
                        <!--<b>Paid:</b> ${childData.paid}</p>-->
                    </div>
                    <div class="card-footer">
                        <div class="stats"> 
                            <a>${childData.key}</a>
                        </div>
                    </div>
                </div>
            </div>`
            );

        });
        });


    
}







//test
//load hotel data from database
// var loadCurrentHotel = function (email) {
//     const ref = firebase.database().ref('/GHotelBookMan/Hotels');

// ref.on("value", function(snapshot) { 
//  // console.log(snapshot.val()); 
//   snapshot.forEach(function(childSnapshot) {
//     var childKey = childSnapshot.key;
//     var childData = childSnapshot.val();
//    // console.log(childData);
//     if(childData.hotelEmail == email){
//         console.log(childData);
//     }

//     // ...
//   });
// });
// }




// //test
// var playersRef = firebase.database().ref("players/"); 
// playersRef.push({  
//       number: 12, 
// 	  name: "home",
//       age: 30 
  
// });