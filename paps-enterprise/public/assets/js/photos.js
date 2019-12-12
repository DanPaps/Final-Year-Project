  // Picture row
  var row = $('#picture-row');

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
            // Load hotel data from database
          loadCurrentHotel(uid);
           
        } else {
            // Navigate back to login page
            window.location = "index.html";          
        }
      });          
    
});

var uploadSingleRoom = function (files) {
    var file = files.item(0);
    $('#selected_picture_name1').text(file.name);
    uploadImage(file,"Single");
};

var uploadDoubleRoom = function (files) {
    var file = files.item(0);
    $('#selected_picture_name2').text(file.name);
    uploadImage(file,"Double");
};

var uploadEconomyRoom = function (files) {
    var file = files.item(0);
    $('#selected_picture_name3').text(file.name);
    uploadImage(file,"Economy");
};

// //load hotel data from database
var loadCurrentHotel = function (uid) {
    const ref = firebase.database().ref('/GHotelBookMan/Hotels');
    
// //Get hotels
ref.once('value', function(snapshot) {
    snapshot.forEach(function(childSnapshot) {
      var childKey = childSnapshot.key;
      var childData = childSnapshot.val();

      if (uid == childKey) {
        // Set hotel name at the navbar
        $('#hotel_title_bar_name').text(childData.hotelName);

        // Set single room image
        $('#single_image').attr('src',childData.RoomPictures.Single.imageUrl);

        // Set double room image
        $('#double_image').attr('src',childData.RoomPictures.Double.imageUrl);

        // Set economy room image
        $('#economy_image').attr('src',childData.RoomPictures.Economy.imageUrl);

        // Get all images as single object
        var imagesList = childData.Images;

        // get keys of each Json object
        var keys = Object.keys(imagesList);

        // loop through list by key to get image urls
        for (var i = 0; i< keys.length; i++){
            var imageUrl = imagesList[keys[i]].imageUrl;
            console.log(imageUrl);
            
            // Append images to grid
            row.append(`
                <div class="img-responsive col-md-4">
                    <img src="${imageUrl}" alt="./assets/img/content_placeholder.svg" srcset="" style="width: 100%; height: 200px; margin-bottom: 20px;">
                </div>
            `);
        }
        
      }

    });
  });
};

// upload image
var uploadImage = function (file,path) {
    var uid = firebase.auth().currentUser.uid;
    // Create storage reference
    var ref = firebase.storage().ref(`/GHotelBookMan/Hotels/${uid}`).child(file.name + ".jpg");

    // Upload file and get task
    var task = ref.put(file, {contentType: "image/jpeg"});

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
            // Handle successful uploads on complete
            var image = task.snapshot.downloadURL;
            console.log(image);

            // Data model to be uploaded to the database
            var data = {
                imageUrl: image,
                name: path
            };
            
            var db = firebase.database().ref(`/GHotelBookMan/Hotels/${firebase.auth().currentUser.uid}/RoomPictures/${path}`);
            db.set(data).then(() => {
                setError("Uploaded successfully");
            });
        });
};

// Upload Hotel images
var uploadHotelPic = function (files) {
  for(var i = 0; i < files.length; i++){
       // Append images to grid
       row.append(`
       <div class="img-responsive col-md-4">
           <img src="./assets/img/content_placeholder.svg" alt="./assets/img/content_placeholder.svg" srcset="" style="width: 100%; height: 200px; margin-bottom: 20px;">
       </div>
   `);
      // Append images to grid
      pushImage(files.item(i));
  }
};

// Push batch images to storage and store references in the database
var pushImage = function (file) {
    var uid = firebase.auth().currentUser.uid;
    // Create storage reference
    var ref = firebase.storage().ref(`/GHotelBookMan/Hotels/Images/${uid}`).child(file.name + ".jpg");

    // Upload file and get task
    var task = ref.put(file, {contentType: file.type});
 
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
            // Handle successful uploads on complete
            var image = task.snapshot.downloadURL;
            console.log(image);

            // Data model to be uploaded to the database
            var data = {
                imageUrl: image
            };
            
            var db = firebase.database().ref(`/GHotelBookMan/Hotels/${uid}/Images`).push();
            db.set(data).then(() => {
                row.empty();
                loadCurrentHotel(uid);
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