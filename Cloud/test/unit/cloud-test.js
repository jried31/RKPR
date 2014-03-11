/* jshint -W097, -W030 */

"use strict";

var expect = require('expect.js'),
    async = require('async'),
    cloud = require('../../cloud.js'),
    config = require('../../config.js'),
    KaisekiInc = require('kaiseki'),
    kaiseki = new KaisekiInc(config.APP_ID, config.REST_API_KEY);

describe('cloud', function() {

    var vehicle = {
        license: '123456',
        make: '123456',
        model: '123456',
        ownerId: '123456123142',
        alertLevel: '0',
        status: 'RVD',
        stolenDate: "01-17-2014 21:00:00",
        recoveredDate: "01-22-2014 21:00:00"
    },
    className = 'Vehicle',
    object = null;
    // set up dummy vehicle before every test
    before(function(done) {

        kaiseki.createObject(className, vehicle, function(err, res, body, success) {
            expect( success ).to.equal(true);
            expect( err ).to.not.exist;
            object = body;

            expect( object.license ).to.equal( vehicle.license );
            expect( object.make ).to.equal( vehicle.make );
            expect( object.model ).to.equal( vehicle.model );
            expect( object.ownerId ).to.equal( vehicle.ownerId );
            expect( object.alertLevel ).to.equal( vehicle.alertLevel );
            expect( object.stolenDate ).to.equal( vehicle.stolenDate );
            expect( object.recoveredDate ).to.equal( vehicle.recoveredDate );

            expect( object.createdAt ).to.exist;
            expect( object.objectId ).to.exist;

            done(err);
        });


    });

    // clean up object
    after(function(done) {
        kaiseki.deleteObject(className, object.objectId, function() {
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
                    kaiseki.getObject(className, object.objectId, null, function(err, res, body, success) {
                        var refreshedObject  = body;

                        expect( success ).to.equal(true);
                        expect( err ).to.exist;
                        expect( err ).to.not.exist;

                        expect( refreshedObject.license ).to.equal( object.license );
                        expect( refreshedObject.make ).to.equal( object.make );
                        expect( refreshedObject.model ).to.equal( object.model );
                        expect( refreshedObject.ownerId ).to.equal( object.ownerId );

                        // check that fields were actually cleared
                        expect( refreshedObject.alertLevel ).to.equal(undefined);
                        expect( refreshedObject.status ).to.equal(undefined);
                        expect( refreshedObject.stolenDate ).to.equal(undefined);
                        expect( refreshedObject.recoveredDate ).to.equal(undefined);

                        expect( refreshedObject.createdAt ).to.exist;
                        expect( refreshedObject.objectId ).to.exist;
                        next(null);
                    }); 
                },

            ], function(err, results) {
                done(err);
            });
        });
    });

});
