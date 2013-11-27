var restify 		= require('restify');
var request 		= require('request');
var kaiseki_inc 	= require('kaiseki');
var xmpp			= require('node-xmpp');

// instantiate
var APP_ID = 'OZzFan5hpI4LoIqfd8nAJZDFZ3ZLJ70ZvkYCNJ6f';
var REST_API_KEY = 'bPlqPguhK51mbRXaYcfnf73uTri07sk6uB64ZdPb';
var kaiseki = new kaiseki_inc(APP_ID, REST_API_KEY);

/**
 * The magic status codes, which you will see here and there:
 OK : status ok
 LFT: lifted
 TLT: titled
 STL: stolen
 RVD: recovered
 NRD: not recoved
 */

/**
 * A "cronjob" to run the notify task. Yeah,
 * it leaks memory.
 */
 var minutes = 1, interval = minutes * 1000 * 60;
 setInterval(notifyNearbyUsers, interval);


/**
* Return a timestamp with the format "m/d/yy h:MM:ss TT"
* @type {Date}
*/

function timestamp()
{
	var d 		= new Date();
	var month 	= d.getMonth() + 1;
	var date 	= d.getDate();
	var year 	= d.getFullYear();
	var hour 	= d.getHours() - 2;
	var min 	= d.getMinutes();
	var sec 	= d.getSeconds();

	if(month < 10)
		month = '0' + month;
	if(date < 10)
		date = '0' + date;
	if(hour < 10)
		hour = '0' + hour;
	if(min < 10)
		min = '0' + min;
	if(sec < 10)
		sec = '0' + sec;

	return month + '-' + date + '-' + year + " " + hour + ":" + min + ":" + sec;
}

/**
 * updateVehicleStatus
 *
 * Updates the status of a vehicle. Generally called by the GeogramONE, using
 * information read from the sensor.
 *
 * @param string 	id 				Vehicle ID
 * @param string 	status			New vehicle status.
 * @param GeoPoint 	location		Vehicle location
 */
function updateVehicleStatus(req,resp,next){
	console.log('Got Data %s \n',req.body.id);
	var tmpObj = req.body;
	var position = {location: {
			__type: 'GeoPoint',
			latitude: parseFloat(tmpObj.lat),
			longitude: parseFloat(tmpObj.lng)
		}
	};
  
	kaiseki.updateObject('Vehicle', tmpObj.id,
	                   {'status': tmpObj.status.toUpperCase(), 
	                    'pos': position.location},
	          function(err, res, body, success) {
	          	if (success) {
	          		console.log("Marked " + tmpObj.id + " as " + tmpObj.status);
	          		sendTiltNotification(tmpObj.id);
	          		sendStolenNotification(tmpObj.id);
	          	} else {
					console.log(body.error);
	          	}
	          }
	);
	resp.send('ok');
}
var server = restify.createServer();
server.use(restify.bodyParser({ mapParams: false }));

server.post('/update', updateVehicleStatus);

server.listen(8081, function() {
	console.log('%s listening at %s', server.name, server.url);
});

/**
 * sendTiltNotification
 *
 * If necessary, notifies the owner of a vehicle that their vehicle
 * has been tilted.
 *
 * @param 	string		id 			Vehicle ID
 */
 function sendTiltNotification(id) {
	// first, fetch vehicle info
	kaiseki.getObject('Vehicle', id, { }, function(err, res, body, success) {
		// then, send the owner notification if the vehicle is tilted
		if (body.status == "TLT") {
			console.log(id + ' tilted. Notifying owner.');
			var notification_data = {
					where: { objectId: body.ownerId },
					data: {
						alert: "Your " + body.make + " " + body.model + " has been tilted."
					}
				};
				kaiseki.sendPushNotification(notification_data, function(err, res, body, success) {
					if (success) {
						kaiseki.updateObject('Vehicle', id, { status: 'OK' }, function(err, res, body, success) {
							if (success)
								console.log('Owner notified.');
							else
								console.log(body.error);
						});
					}
					else {
						console.log(body.error);
					}
				});
		}
	});
 }

/**
 * sendStolenNotification
 *
 * If necessary, notifies the owner of a vehicle that their vehicle has
 * been stolen. This will also create the chatroom for the vehicle.
 *
 * @param 	string		id 		Vehicle ID
 */
function sendStolenNotification(id) {
	// first, fetch vehicle info
	kaiseki.getObject('Vehicle', id, { }, function(err, res, body, success) {
		// then, send the owner notification if the vehicle is tilted
		if (body.status == "STL") {
			console.log(id + " stolen! Notifying owner.");
			var notification_data = {
					where: { objectId: body.ownerId },
					data: {
						alert: "Your " + body.make + " " + body.model + " has been stolen."
					}
				};
				kaiseki.sendPushNotification(notification_data, function(err, res, body, success) {
					if (success) {
						console.log("Owner notified.");
						createChatroom(id);
					}
					else {
						console.log(body.error);
					}
				});
		}
	});
}

/**
 * createChatroom
 *
 * Creates a chatroom for the stolen vehicle.
 *
 * @param 	string		id 		Vehicle ID
 */
function createChatroom(id) {
	kaiseki.createObject('Chatroom', { vehicleId: id }, function(err, res, body, success) {
		if (success)
			console.log("Created chatroom for " + id); // @todo also create physical xmpp room
		else
			console.log(body.error);
	});
}

/**
 * notifyNearbyUsers()
 *
 * Cycle through every stolen vehicle and notify users within
 * .5 miles that the vehicle is stolen.
 */
function notifyNearbyUsers() {
	console.log('Notifying users near stolen vehicles.');
	kaiseki.getObjects('Vehicle', {  where: { status: "STL" } }, function(err, res, body, success) {
		if (success) {
			for (var i = 0; i < body.length; ++i) {
				var veh = body[i];
				console.log("Notifying users near " + veh['objectId']);

				var geopoint_where = {
					GeoPoint: {
						"$nearSphere": {
							__type: "GeoPoint",
							"latitude": veh['pos']['latitude'],
							"longitude": veh['pos']['longitude']
						},
						"$maxDistanceInMiles": 0.5
					}
				};

				kaiseki.getUsers(geopoint_where, function(err, res, body, success) {
					if (success) {
						var nearby_owners = new Array();
						for (var j = 0; j < body.length; ++j) {
							nearby_owners.push(body[j]['objectId']);
						}
						kaiseki.getObjects('Chatroom', { where: { vehicleId: veh['objectId'] } }, function(err, res, body, success) {
							if (success) {
								for (var k = 0; k < body.length; ++k) {
									var room = body[k];

									if (room['members'] === undefined)
										room['members'] = new Array();

									var new_users = arr_diff(nearby_owners, room['members']);
									var new_members = array_unique(room['members'].concat(nearby_owners));
									// Update chatroom
									kaiseki.updateObject('Chatroom', room['objectId'], { members: new_members }, function(err, res, body, success) {
										if (success) {
											// Notify users
											for (var j = 0; j < new_users.length; ++j) {
												var notification_data = {
													where: { objectId: new_users[j] },
													data: {
														alert: "A nearby vehicle has been stolen!"
													}
												};
												kaiseki.sendPushNotification(notification_data, function(err, res, body, success) {
													if (success) {
														// don't want to flood the console...
													}
													else {
														console.log(body.error);
													}
												});
											}
										}
										else {
											console.log(body.error);
										}
									});
								}
							} else {
								console.log(body.error);
							}
						})
					} else {
						console.log(body.error);
					}
				});
			}
		}
		else {
			console.log(body.error);
		}
	});
}

function array_unique(array) {
    var a = array.concat();
    for(var i=0; i<a.length; ++i) {
        for(var j=i+1; j<a.length; ++j) {
            if(a[i] === a[j])
                a.splice(j--, 1);
        }
    }

    return a;
};

/**
 * Removes all items in a1 that are also in a2
 */
function arr_diff(a1, a2)
{
  var a=[], diff=[];
  for(var i=0;i<a1.length;i++)
    a[a1[i]]=true;
  for(var i=0;i<a2.length;i++)
    if(a[a2[i]]) delete a[a2[i]];
    else a[a2[i]]=true;
  for(var k in a)
    diff.push(k);
  return diff;
}