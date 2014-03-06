(function() {

    "use strict";

    var KaisekiInc = require('kaiseki'),
        xmpp = require('node-xmpp'),

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

        // Kaiseki Constants 
        APP_ID = 'TfBH3NJxzbOaxpksu5YymD4lP9bPlytcfZMG8i5a',
        REST_API_KEY = 'IYkecC2bwjTYy4suhkGTEn7yrJaLC7ygVc5Esedd',
        // Kaiseki Instances
        kaiseki = new KaisekiInc(APP_ID, REST_API_KEY),

        // Alert Constants
        AlertLevel = {
            PARKED: 0, // do nothing
            MOVED:  1, // Slight movement (eg:tilt) no broken geofence yet notify user only 
            MOVED_LOC: 2,
            STOLEN: 3, // Broken geofence alert nearby users
            STOLEN_LOC: 4,
            RIDING: 5,
            CRASHED: 6,
            
            NRD:    "NRD",// not recovered
            RVD:    "RVD"// recovered
        };

    /**
    * updateVehicleStatus
    *
    * Updates the status of a vehicle. Generally called by the GeogramONE, using
    * information read from the sensor.
    *
    * @param string    id              Vehicle ID
    * @param string    status          New vehicle status.
    * @param GeoPoint  location        Vehicle location
    */
    var processTrackerAlert = function(req,resp,next){
        console.log('Got Data %s \n',req.body.id);

        // Parse message contents
        var alert = req.body;
        console.log(alert);
        
        //Find vehicle by trackerId
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
                        updateStatus(alert, vehicle, message);
                        break;

                    case AlertLevel.MOVED:
                        message = vehicle.make + ' ' + vehicle.model + ' has been moved!';
                        updateStatus(alert, vehicle, message);
                        break;

                    case AlertLevel.MOVED_LOC:
                        updateLocation(alert, vehicle, null);
                        break;

                    case AlertLevel.STOLEN:
                        message = vehicle.make + ' ' + vehicle.model + ' has been stolen!';
                        updateStatus(alert, vehicle, message);
                        createChatroom(vehicle.objectId);
                        break;

                    case AlertLevel.STOLEN_LOC:
                        updateLocation(alert, vehicle, null);
                        break;

                    case AlertLevel.RIDING:
                        updateLocation(alert, vehicle, null);
                        break;

                    case AlertLevel.CRASHED:
                        message = vehicle.make + ' ' + vehicle.model + ' has been crashed!';
                        updateStatus(alert, vehicle, message);
                        break;
                }
            });
    };

    //Update the vehicle's alert level and location
    function updateStatus(alert, vehicle, message){
        var alertKey = AlertLevel.getKeyByValue(parseInt(alert.lvl)).toString();
        kaiseki.updateObject('Vehicle', vehicle.objectId, {
            'alertLevel': alertKey,
            'pos': alert.location
        }, function(err, res, body, success) {
            //send notifications w.r.t. the alert level
            if (err) {
                console.log(body.error);
            } else {
                if(message != null) {
                    sendNotification(alertKey, vehicle, message);
                }
            }
        });
    }

    function updateLocation(alert, vehicle){

        kaiseki.createObject( 'VehicleLocationHistory', {
            'trackerId': alert.id,
            'ownerId': vehicle.ownerId,
            'pos': alert.location,
            'timestamp':timestamp()
        }, function(err, res, body, success) {

            if (!success) {
                console.log(body.error);
            }

        });
    }

    function sendNotification(alertKey, vehicle, message) {
        var notification_data = {   //Data for Nofication
            where: { ownerId: vehicle.ownerId },
            data: {
                action: 'CUSTOMIZED',
                alertLevel: alertKey,
                message: message
            }
        };

        //Notify users via Push
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
    * notifyNearbyUsers()
    *
    * Cycle through every stolen vehicle and notify users within
    * .3 miles that the vehicle is stolen.
    */
    var notifyNearbyUsers = function() {
        console.log('Notifying users near stolen vehicles.');
        kaiseki.getObjects('Vehicle', {where: {alertLevel: "STOLEN"}}, function(err, res, body, success) {
            if (success) {
                console.log('we got some data back');   
                for (var i = 0; i < body.length; ++i) {
                    var veh = body[i];
                    console.log('Notifying users near vehicleId: ' + veh['objectId']);

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

                    console.log("We get this far buddy");
                    kaiseki.getUsers(geopoint_where, function(err, res, body, success) {
                        if (success) {
                            var nearby_users = [];
                            for (var j = 0; j < body.length; ++j) {
                                console.log(body[j]['objectId']);
                                nearby_users.push(body[j]['objectId']);
                            }

                            kaiseki.getObjects('Chatroom', { where: { vehicleId : veh['objectId']},limit:1, order:'-createdAt' }, function(err, res, body, success) {
                                if (success) {
                                    console.log('Number of chatrooms found: '+body.length);
                                    for (var k = 0; k < body.length; ++k) {
                                        var room = body[k];

                                        if (!room['members']) {
                                            room['members'] = [];
                                        }

                                        var new_users = arr_diff(nearby_users, room['members']);
                                        var new_members = array_unique(room['members'].concat(nearby_users));

                                        if (new_users.length > 0) {
                                            console.log("Added " + new_users.length + " new members: " + new_users);
                                        }

                                        // Update chatroom
                                        kaiseki.updateObject('Chatroom', room['objectId'], { members: new_members }, function(err, res, body, success) {
                                            if (success) {
                                                console.log('New members successfully added to chatroom.');
                                                // Notify users
                                                for (var j = 0; j < new_users.length; ++j) {
                                                    var notification_data = {
                                                        where: { objectId: new_users[j] },
                                                        data: {
                                                            action: 'CUSTOMIZED',
                                                            alertLevel: 'NEARBY',
                                                            message: 'Time to roll out. Help us recover this ' + body.make + ' ' + body.model+".",
                                                            room:room['objectId']
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

    function array_unique(array) {

        // duplicate the array
        var a = array.concat();

        // remove duplicated elements
        for(var i = 0; i < a.length; ++i) {

            for(var j = i + 1; j < a.length; ++j) {

                if(a[i] === a[j]) {
                    a.splice(j--, 1);
                }
            }
        }

        return a;
    }

    /**
    * Removes all items in a1 that are also in a2
    */
    function arr_diff(a1, a2)
    {
        var a=[], diff=[];
        for(var i = 0; i < a1.length; i++) {
            a[a1[i]]=true;
        }

        for(var j = 0; j < a2.length; j++) {

            if (a[a2[j]]) {
                delete a[a2[j]];
            } else {
                a[a2[j]] = true;
            }
        }

        for (var k in a) {
            diff.push(k);
        }
        return diff;
    }

    /**
     * Finds the key in an object by its value.
     */
    Object.prototype.getKeyByValue = function( value ) {

        for( var prop in this ) {

            if( this.hasOwnProperty( prop ) ) {

                if( this[ prop ] === value ) {
                    return prop;
                }
            }
        }
    };


    /**
     * Return a timestamp with the format 'm/d/yy h:MM:ss TT'
     * @type {Date}
     */
    function timestamp() {

        var d       = new Date(),
            month   = d.getMonth() + 1,
            date    = d.getDate(),
            year    = d.getFullYear(),
            hour    = d.getHours() - 2,
            min     = d.getMinutes(),
            sec     = d.getSeconds();

        // append 0 to single digit numbers
        if (month < 10) {
            month = '0' + month;
        }
        if (date < 10) {
            date = '0' + date;
        }
        if (hour < 10) {
            hour = '0' + hour;
        }
        if (min < 10) {
            min = '0' + min;
        }
        if (sec < 10) {
            sec = '0' + sec;
        }

        return month + '-' + date + '-' + year + ' ' + hour + ':' + min + ':' + sec;
    }

    /**
    * createChatroom
    *
    * Creates a chatroom for the stolen vehicle.
    *
    * @param {String} id Vehicle ID
    */
    function createChatroom(vehicleId) {

        var roomName = vehicleId + (new Date().getTime()).toString();

        // create chatroom
        kaiseki.createObject('Chatroom', { vehicleId: vehicleId, roomName: roomName }, function(err, res, body, success) {

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



    exports.processTrackerAlert = processTrackerAlert;
    exports.notifyNearbyUsers = notifyNearbyUsers;

})();
