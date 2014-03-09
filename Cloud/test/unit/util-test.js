/* jshint -W097 */

"use strict";

var expect = require('expect.js'),
    util = require('../../util.js'),
    timestamp = util.timestamp,
    diffTime = util.diffTime;


describe('Util', function() {

    describe('#timestamp', function() {
        it('should be formatted as mm-dd-yyyy hh:MM:ss', function(){

            var datePattern = /^(\d){2}-(\d){2}-(\d){4} (\d){2}:(\d){2}:(\d){2}$/;
            expect( datePattern.test(timestamp()) ).to.be(true);

        });
    });

    describe('#diffTime', function() {

        it('it should return the correct difference in milliseconds', function() {

            var actual = diffTime("01-22-2014 21:00:00","01-22-2014 17:00:00"),
                // 4 hours in milliseconds
                expected = 4 * 60 * 60 * 1000;

            expect( actual ).to.be(expected);
        });

        it('it should return the correct difference of 5 days in milliseconds', function() {

            var actual = diffTime("01-22-2014 21:00:00","01-17-2014 21:00:00"),
                // 5 days in milliseconds
                expected = 5 *24 * 60 * 60 * 1000;

            expect( actual ).to.be(expected);
        });

        it('it should return false for incorrect date parsing', function() {

            var actual = diffTime("","01-17-2014 21:00:00"),
                expected = false;

            expect( actual ).to.be(expected);
        });
    });

});

