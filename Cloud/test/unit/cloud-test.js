/* jshint -W097, -W030 */

"use strict";

var expect = require('expect.js'),
    async = require('async'),
    _ = require('underscore'),
    cloud = require('../../cloud.js'),
    config = require('../../config.js'),
    KaisekiInc = require('kaiseki'),
    kaiseki = new KaisekiInc(config.APP_ID, config.REST_API_KEY);

describe('cloud', function() {

    var mock_vehicle = {
        license: '123456',
        make: '123456',
        model: '123456',
        ownerId: '123456123142',
        alertLevel: '0',
        status: 'RVD',
        stolenDate: "01-17-2014 21:00:00",
        recoveredDate: "01-22-2014 21:00:00"
    },
    CLASS = {
        VEHICLE: 'Vehicle',
        CHATROOM: 'Chatroom'
    },
    vehicle = null,
    chatroom = null;
    // set up dummy objects before test
    before(function(done) {

        async.series([function(next) {
            kaiseki.createObject(CLASS.VEHICLE, mock_vehicle, function(err, res, body, success) {
                expect( success ).to.equal(true);
                expect( err ).to.not.exist;
                vehicle = body;

                expect( vehicle.license ).to.equal( mock_vehicle.license );
                expect( vehicle.make ).to.equal( mock_vehicle.make );
                expect( vehicle.model ).to.equal( mock_vehicle.model );
                expect( vehicle.ownerId ).to.equal( mock_vehicle.ownerId );
                expect( vehicle.alertLevel ).to.equal( mock_vehicle.alertLevel );
                expect( vehicle.stolenDate ).to.equal( mock_vehicle.stolenDate );
                expect( vehicle.recoveredDate ).to.equal( mock_vehicle.recoveredDate );

                expect( vehicle.createdAt ).to.exist;
                expect( vehicle.vehicleId ).to.exist;

                next(err);
            });
        }, function(next) {

            kaiseki.createObject(CLASS.CHATROOM, {
                vehicleId: vehicle.objectId,
                roomName: 'testChatroom',
                members: [vehicle.ownerId]
            }, function(err, res, body, success) {

                expect( success ).to.equal(true);
                expect( err ).to.not.exist;
                chatroom = body;

                expect(chatroom.vehicleId).to.equal( vehicle.objectId );
                expect(chatroom.roomName).to.equal('testChatroom');
                expect(chatroom.members).to.contain(vehicle.ownerId);
                next();

            });
        }], function() {
            done();
        });
    });

    // clean up test objects 
    after(function(done) {
        async.parallel([function(cb) {
            kaiseki.deleteObject(CLASS.VEHICLE, vehicle.objectId, function(err, res, body, success) {
                expect( success ).to.equal(true);
                cb();
            });
        }, function(cb) {
            kaiseki.deleteObject(CLASS.CHATROOM, chatroom.objectId, function(err, res, body, success) {
                expect( success ).to.equal(true);
                cb();
            });
        }], function() {
            done();
        });
    });

    describe('#refreshRecoveredVehicle', function(){
        it('should be refreshed', function(done){
            async.series([
                function(next) {
                    cloud.refreshRecoveredVehicles(function(){
                        next(null);
                    });
                },

                function(next) {
                    kaiseki.getObject(CLASS.VEHICLE, vehicle.objectId, null, function(err, res, body, success) {
                        var refreshedObject  = body;

                        expect( success ).to.equal(true);
                        expect( err ).to.exist;
                        expect( err ).to.not.exist;

                        expect( refreshedObject.license ).to.equal( vehicle.license );
                        expect( refreshedObject.make ).to.equal( vehicle.make );
                        expect( refreshedObject.model ).to.equal( vehicle.model );
                        expect( refreshedObject.ownerId ).to.equal( vehicle.ownerId );

                        // check that fields were actually cleared
                        expect( refreshedObject.alertLevel ).to.equal(undefined);
                        expect( refreshedObject.status ).to.equal(undefined);
                        expect( refreshedObject.stolenDate ).to.equal(undefined);
                        expect( refreshedObject.recoveredDate ).to.equal(undefined);

                        expect( refreshedObject.createdAt ).to.exist;
                        expect( refreshedObject.vehicleId ).to.exist;
                        next(null);
                    }); 
                },

            ], function(err, results) {
                done(err);
            });
        });
    });

    describe('Chatroom', function() {
        describe('#getVehicleChatroom', function() {
            it('should be a valid chatroom', function(done) {
                cloud._getVehicleChatroom(vehicle.objectId, function(data) {
                    data = data[0];
                    expect(chatroom.vehicleId).to.equal(data.vehicleId);
                    expect(chatroom.roomName).to.equal(data.roomName);
                    expect(_.isEqual(chatroom.members, data.members)).to.equal(true);
                    done();
                });
            });
            it('should be an  invalid chatroom', function(done) {
                cloud._getVehicleChatroom('invalidId123123', function(data) {
                    expect(data).to.be.empty();
                    done();
                });
            });
        });

        describe('#updateChatroomMembers', function() {

            it('should update chatroom members', function(done) {
                var newMember = '12345testMember',
                    roomMembers =  chatroom.members.concat(newMember);
                async.series([function(next){
                    cloud._updateChatroomMembers(chatroom.objectId, roomMembers , function(){
                        // success
                        next();
                    });
                },function(next){
                    kaiseki.getObject(CLASS.CHATROOM, chatroom.objectId, null, function(err, res, body, success) {
                        // check that updates were actually made
                        chatroom = body;
                        expect(_.isEqual(chatroom.members, roomMembers)).to.equal(true);
                        next();
                    });
                }],function(){
                    done();
                });
            });

            it('should be an  invalid chatroom', function(done) {
                cloud._updateChatroomMembers('invalidId123123', null, null, function(data) {
                    expect(data.error).to.not.equal(undefined);
                    done();
                });
            });
        });
    });

    describe('#updateStolenVehicleChatroomUsers', function() {
            it('should update chatroom members for stolen vehicle', function(done) {
                var newMember = '12345NewTestMember',
                    roomMembers =  chatroom.members.concat(newMember);
                async.series([function(next){
                    cloud._updateStolenVehicleChatroomUsers(vehicle, roomMembers , function(){
                        // success
                        next();
                    });
                },function(next){
                    kaiseki.getObject(CLASS.CHATROOM, chatroom.objectId, null, function(err, res, body, success) {
                        // check that updates were actually made
                        chatroom = body;
                        expect(_.isEqual(chatroom.members, roomMembers)).to.equal(true);
                        next();
                    });
                }],function(){
                    done();
                });
            });
    });

});
