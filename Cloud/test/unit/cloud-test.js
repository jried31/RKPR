/* jshint -W097, -W030 */

"use strict";

var expect = require('expect.js'),
    async = require('async'),
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
    mockChatroom = {
    },
    CLASS = {
        VEHICLE: 'Vehicle',
        CHATROOM: 'Chatroom'
    },
    vehicle = null,
    chatroom = null;
    // set up dummy mock_vehicle before every test
    before(function(done) {

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

            done(err);
        });

        // kaiseki.createObject(CLASS.CHATROOM:Q


    });

    // clean up vehicle
    after(function(done) {
        kaiseki.deleteObject(CLASS.VEHICLE, vehicle.vehicleId, function() {
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

});
