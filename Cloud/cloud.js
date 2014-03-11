/**
 * @overview  defines methods that can be used by a web service for tracking vehicles 
 */

/* jshint -W097, -W083 */
"use strict";

var KaisekiInc = require('kaiseki'),
    xmpp = require('node-xmpp'),
    // underscorejs
    // collection of useful Javascript functions
    // see http://underscorejs.org/
    _ = require('underscore'),
    util = require('./util.js'),
    config = require('./config.js'),
    timestamp = util.timestamp,
    diffTime = util.diffTime,

    // credentials for Jabber
    jabber_creds = {
        username: 'ridekeeper',
        password: 'cs117ridekeeper',
        jid: '718168-5815@chat.quickblox.com',
        room_jid: '5815_%s@muc.chat.quickblox.com',
        host: 'chat.quickblox.com',
        port: 5222,
        nickname: 'Big_Brother'
    },

    // Kaiseki Instances
    kaiseki = new KaisekiInc(config.APP_ID, config.REST_API_KEY),

    // the time difference between stolenDate and recoveredDate
    // this threshold determines when to reset vechiles to a clean not stolen state
    // 5 days in milliseconds
    REFRESH_THRESHOLD = 5 * 24 * 60 * 60 * 1000,

    // Alert Constants
    AlertLevel = {
        PARKED: 0, // do nothing
        MOVED:  1, // Slight movement (eg:tilt) no broken geofence yet notify user only 
        MOVED_LOC: 2,
        STOLEN: 3, // Broken geofence alert nearby users
        STOLEN_LOC: 4,
        RIDING: 5,//do nothing
        CRASHED: 6,//

        // TODO: change this to numbers?
        NRD:    "NRD", // not recovered
        RVD:    "RVD" // recovered
    },
    // dictionary to look up alert level name by integer value
    reverseAlertLevelDictionary = _.invert(AlertLevel);

/**
 * @function sendPushNotification
 *
 * @desc sends push notification to device based off of vehicle's owner id
 *
 * @param {Number} alertKey - the number corresponding the type of alert that is sent
 * @param {Object} vehicle - the vehicle and its metadata
 * @param {String} message - the message that we are sending
 */
function sendPushNotification(alertKey, vehicle, message) {
    var notification_data = {   // Data for Nofication
        where: { ownerId: vehicle.ownerId },
        data: {
            action: 'CUSTOMIZED',
            alertLevel: alertKey,
            message: message
        }
    };

    // Notify users via Push
    kaiseki.sendPushNotification(notification_data, function(err, res, body, success) {
        console.log(notification_data);

        if (success) {

            console.log('Owner notified.');

        } else {

            console.log(body.error);
        }
    });
}

/**
 * @function updateVehicleStatus
 *
 * @desc Update the vehicle's alert level and location
 * and send notification to owner
 *
 * @param {Object}  alert  - the metadata associated with the vehicle tracker
 * @param {Object}  vehicle - the vehicle and its metadata
 * @param {String}  message  - the message that we are sending 
 */
function updateVehicleStatus(alert, vehicle, message) {

    var alertKey = reverseAlertLevelDictionary[(parseInt(alert.lvl)).toString()],
        data = {
            alertLevel: alertKey,
            pos: alert.location
        };

    // store a timestamp of date vehicle is stolen
    if (alertKey === AlertLevel.STOLEN) {
        data.stolenDate = timestamp();
        data.recoveredDate = undefined;
    }

    // store a timestamp of date when vehicle is recovered
    if (alertKey === AlertLevel.RVD) {
        data.recoveredDate = timestamp();
    }

    kaiseki.updateObject('Vehicle', vehicle.objectId, data,
    function(err, res, body, success) {
        //send notifications w.r.t. the alert level
        if (err) {
            console.log(body.error);
        } else {
            if(message != null) {
                sendPushNotification(alertKey, vehicle, message);
            }
        }
    });
}

/**
 * @function updateVehicleLocation
 *
 * @param {Object}  alert  - the metadata associated with the vehicle tracker
 * @param {Object}  vehicle - the vehicle and its metadata
 */
function updateVehicleLocation(alert, vehicle) {

    kaiseki.createObject( 'VehicleLocationHistory', {
        'trackerId': alert.id,
        'ownerId': vehicle.ownerId,
        'pos': alert.location,
        'timestamp': timestamp()
    }, function(err, res, body, success) {

        if (!success) {
            console.log(body.error);
        }

    });
}

/**
 * @function notifyNearbyUsers
 *
 * @desc Cycle through every stolen vehicle and notify users within specified radius  that the vehicle is stolen.
 * 
 */
var notifyNearbyUsers = function() {
    console.log('Notifying users near stolen vehicles.');
    // First get all Vechiles that are stolen
    kaiseki.getObjects('Vehicle', {where: {alertLevel: "STOLEN"}}, function(err, res, body, success) {
        if (success) {
            console.log('we got some data back'); 
            
            for (var i = 0; i < body.length; ++i) {
                var veh = body[i];
                console.log('Notifying users near vehicleId: ' + veh['objectId']);

                // refer to https://parse.com/docs/rest#geo
                var geopoint_where = {
                    GeoPoint: {
                        '$nearSphere': {
                            __type: 'GeoPoint',
                            'latitude': veh['pos']['latitude'],
                            'longitude': veh['pos']['longitude']
                        },
                        '$maxDistanceInMiles': 0.5
                    }
                };
                
				var vehicleOwner = veh['ownerId'];  //Grab the owner of the vehicle
                console.log("We get this far buddy");
                kaiseki.getUsers(geopoint_where, function(err, res, body, success) {

                    if (success) {
                    
                        var nearby_users = [];
						
                        for (var j = 0; j < body.length; ++j) {
                            console.log(body[j]['objectId']);
                            nearby_users.push(body[j]['objectId']);
                        }
                        
						
                        // get chatroom assocated with vehicle
                        kaiseki.getObjects('Chatroom', { where: { vehicleId : veh['objectId']},limit:1, order:'-createdAt' }, function(err, res, body, success) {

                            if (success) {
                                console.log('Number of chatrooms found: '+body.length);
                                for (var k = 0; k < body.length; ++k) {
                                    var room = body[k];

                                    if (!room['members']) {
                                        room['members'] = [];
                                    }

                                    var new_users = _.difference(nearby_users, room['members']),
                                        // add new users to chat room
                                        members = room['members'].concat(new_users);


                                    if (new_users.length > 0) {
                                        console.log("Added " + new_users.length + " new members: " + new_users);
                                    }

                                    // Update chatroom members with new users
                                    kaiseki.updateObject('Chatroom', room['objectId'], { members: members }, function(err, res, body, success) {
                                        if (success) {
                                            console.log('New members successfully added to chatroom.');
                                            // Send push notification to all new users regarding stolen vehicle 
                                            for (var j = 0; j < new_users.length; ++j) {
                                                var notification_data = {
                                                    where: { objectId: new_users[j] },
                                                    data: {
                                                        action: 'CUSTOMIZED',
                                                        alertLevel: 'NEARBY',
                                                        message: 'Time to roll out. Help us recover this ' + body.make + ' ' + body.model+".",
                                                        room: room['objectId']
                                                    }
                                                };
                                                kaiseki.sendPushNotification(notification_data, function(err, res, body, success) {
                                                    if (success) {
                                                        // don't want to flood the console...
                                                        // console.log('Push notification successfully sent:', body);
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
                        });
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
};


/**
 * createChatroom
 *
 * @desc Creates a chatroom for the stolen vehicle.
 *
 * @param {String} vehicleId - a vehicle's id
 */
function createChatroom(vehicle) {

	var vehicleId = vehicle['objectId'];
    var roomName = vehicleId + (new Date().getTime()).toString();

    // create chatroom
    kaiseki.createObject('Chatroom', { vehicleId: vehicleId, roomName: roomName , members: [vehicle['ownerId']] }, function(err, res, body, success) {

        if (success) {

            var cl = new xmpp.Client({
                jid: jabber_creds.jid,
                password: jabber_creds.password,
                host: jabber_creds.host,
                port: jabber_creds.port
            });


            cl.on('online', function() {

                var room_jid = jabber_creds.room_jid.replace("%s", roomName);
                // join room (and request no chat history)
                cl.send(new xmpp.Element('presence', { to: room_jid + '/' + jabber_creds.nickname }).
                    c('x', { xmlns: 'http://jabber.org/protocol/muc' })
                );

                    // Request configuration form
                cl.send(new xmpp.Element('iq', { to: room_jid, id: 'create', type: 'get' }).
                    c('query', { xmlns: 'http://jabber.org/protocol/muc#owner' })
                );

                // Submit configuration form
                cl.send(new xmpp.Element('iq', { to: room_jid, id: 'create', type: 'set' }).
                    c('query', { xmlns: 'http://jabber.org/protocol/muc#owner' }).
                    c('x', { xmlns: 'jabber:x:data',type: 'submit' }).
                    // Set form type
                    c('field', { var: 'FORM_TYPE'}).
                    c('value').
                    t('http://jabber.org/protocol/muc#config').
                    up().up().
                    // Set persistent
                    c('field', { var: 'muc#roomconfig_persistentroom'}).
                    c('value').
                    t('1').
                    up().up().
                    // Set members only
                    c('field', { var: 'muc#roomconfig_membersonly'}).
                    c('value').
                    t('0').
                    up().up().
                    // Enable logging
                    c('field', { var: 'muc#roomconfig_enablelogging'}).
                    c('value').
                    t('1').
                    up().up().
                    // Set room name - room_id
                    c('field', { var: 'muc#roomconfig_roomname'}).
                    c('value').
                    t(vehicleId).
                    up().up().
                    // Set max history
                    c('field', { var: 'muc#maxhistoryfetch'}).
                    c('value').
                    t('1000')
                );

                // send keepalive data or server will disconnect us after 150s of inactivity
                setInterval(function() {
                    cl.send(new xmpp.Message({}));
                }, 30000);
            });
        } else {
            console.log(body.error);
        }
    });
}

/**
 * @function processTrackerAlert
 *
 * @desc process alert that is sent from vehicle tracker
 *
 * @param {Object} req - the request object
 * @param {Object} resp - the response object
 * @param {Function} next
 */
var processTrackerAlert = function(req, resp, next) {
    console.log('Got Data %s \n',req.body.id);

    // Parse message contents
    var alert = req.body;
    console.log(alert);

    // Find vehicle by trackerId
    kaiseki.getObjects(
        'Vehicle', {
            where: { trackerId: alert.id }, 
            limit: 1 
        }, function(err, res, body, success) {

            var vehicle = body[0],
                message = "";

            alert.location = {
                __type: 'GeoPoint',
                latitude: parseFloat(alert.lat),
                longitude: parseFloat(alert.lng)
            };

            // Respond to alert level accordingly
            switch (parseInt(alert.lvl)){
                case AlertLevel.PARKED:
                    message = vehicle.make + ' ' + vehicle.model + ' has been parked!';
                    updateVehicleStatus(alert, vehicle, message);
                    break;

                case AlertLevel.MOVED:
                    message = vehicle.make + ' ' + vehicle.model + ' has been moved!';
                    updateVehicleStatus(alert, vehicle, message);
                    break;

                case AlertLevel.MOVED_LOC:
                    updateVehicleLocation(alert, vehicle, null);
                    break;

                case AlertLevel.STOLEN:
                    message = vehicle.make + ' ' + vehicle.model + ' has been stolen!';
                    updateVehicleStatus(alert, vehicle, message);
                    createChatroom(vehicle);
                    //addUserToChatroom();
                    break;

                case AlertLevel.STOLEN_LOC:
                    updateVehicleLocation(alert, vehicle, null);
                    break;

                case AlertLevel.RIDING:
                    updateVehicleLocation(alert, vehicle, null);
                    break;

                case AlertLevel.CRASHED:
                    message = vehicle.make + ' ' + vehicle.model + ' has been crashed!';
                    updateVehicleStatus(alert, vehicle, message);
                    break;
            }
        });
};

/**
 * @function refereshRecoveredVehicles
 *
 * @desc after a certain interval of vehicle recovery,
 *       resets vehicle status metadata
 *
 * @param {Function} onSuccess - callback on success
 * @param {Function} onError - callback on error
 */
var refreshRecoveredVehicles = function(onSuccess, onError) {

    kaiseki.getObjects(
        'Vehicle', {

            where: {
                recoveredDate: {$exists: true} 
            }

        }, function(err, res, body, success) {

            var diff = 0,
                deleteField = {__op: "Delete"};

            body.forEach(function(vehicle, index) {

                diff = diffTime(vehicle.recoveredDate, vehicle.stolenDate);

                if (diff !== false && diff >= REFRESH_THRESHOLD) {

                    kaiseki.updateObject('Vehicle', vehicle.objectId,
                    {
                        alertLevel: deleteField,
                        stolenDate: deleteField,
                        recoveredDate: deleteField,
                        status: deleteField 
                    },
                    function(err, res, body, success) {

                        if (success) {
                            if (onSuccess) {
                                onSuccess(body);
                            }
                        }

                        if (err) {
                            console.log(body.error);
                            if (onError) {
                                onError(body);
                            }
                        }
                    });
                }
            });
        });
};


module.exports = {
    processTrackerAlert:  processTrackerAlert,
    notifyNearbyUsers: notifyNearbyUsers,
    refreshRecoveredVehicles: refreshRecoveredVehicles
};
