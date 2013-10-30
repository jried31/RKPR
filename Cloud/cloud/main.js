// The painful, monolithic file of node.js code. This isn't separated as,
// like many node.js applications, only main.js is loaded on startup. Apparently,
// including jobs as modules can cause fits with scheduled jobs, so we don't do that
// either. Oi vey.

// Send Tilt Notification
// When a vehicle is tilted, we just notify the owner--nothing big.
// It's up to the owner to login and mark the vehicle as stolen.
Parse.Cloud.job( "sendTiltNotification", function( request, status ) {
	Parse.Cloud.useMasterKey();

	var Vehicle = Parse.Object.extend( "Vehicle" );
	var vehicleQuery = new Parse.Query( Vehicle );

	vehicleQuery.equalTo( "tilt", true );

	vehicleQuery.find( {
		success: function( results ) {
			console.log( results.length + " vehicles to notify of tilt." );
			for ( var i = 0; i < results.length; i++ ) {
				var object = results[i];
				var query = new Parse.Query( Parse.Installation );
        		query.equalTo( "ownerId", object.get( 'ownerId' ) );

        		console.log( "Preparing push notification " + i );

        		Parse.Push.send( {
        			where: query,
        			data: {
        				alert: "Your " + object.get( 'make' ) + " " + object.get( 'model' ) + " has been tilted."
        			}
        		}, {
        			success: function() {
        			},
        			error: function( error ) {
        				status.error( error );
        			}
        		} );

        		    status.message( "Successfully notified vehicle " + i + " of tilt. " );

    				object.set( "tilt", false ); // we undo the status so we don't send duplicates
    				object.save();
			}
			status.success( "Succesfully notified tilted vehicles.") ;
		},
		error: function( error ) {
    		success.error( "Error: " + error.code + " " + error.message );
    	}
	} );
} );

// Update Stolen Status
// Update the stolen status of a vehicle. At this time, we consider
// a stolen vehicle to be any lifted vehicle.
Parse.Cloud.job( "updateStolenStatus", function( request, status ) {
	Parse.Cloud.useMasterKey();

	var Vehicle = Parse.Object.extend( "Vehicle" );
	var vehicleQuery = new Parse.Query( Vehicle );

	vehicleQuery.equalTo( "lift", true );

	vehicleQuery.find( {
		success: function( results ) {
			console.log( results.length + " vehicles to notify of lift." );
			for ( var i = 0; i < results.length; i++ ) {
				var object = results[i];
				var query = new Parse.Query( Parse.Installation );
        		query.equalTo( "ownerId", object.get( 'ownerId' ) );

        		console.log( "Preparing push notification " + i );
        		Parse.Push.send( {
        			where: query,
        			data: {
        				alert: "Your " + object.get( 'make' ) + " " + object.get( 'model' ) + " has been stolen!"
        			}
        		}, {
        			success: function() {
        			},
        			error: function( error ) {
        				status.error ( error );
        			}
        		} );

				status.message( "Successfully notified vehicle " + i + " of tilt. ");

				object.set( "lift", false ); // we undo the status so we don't send duplicates
				object.set( "stolen", true );

				object.save();

        		// @todo create chatroom
        	}
        	status.success( "Successfully notified stolen vehicles." );
		},
		error: function( error ) {
    		success.error( "Error: " + error.code + " " + error.message );
    	}
	} );
} );